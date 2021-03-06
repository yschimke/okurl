package com.baulsupp.okurl.okhttp

import com.baulsupp.okurl.okhttp.WireSharkListenerFactory.WireSharkKeyLoggerListener.Launch
import com.baulsupp.okurl.okhttp.WireSharkListenerFactory.WireSharkKeyLoggerListener.Launch.CommandLine
import com.baulsupp.okurl.okhttp.WireSharkListenerFactory.WireSharkKeyLoggerListener.Launch.Gui
import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.TlsVersion
import okhttp3.TlsVersion.TLS_1_2
import okhttp3.TlsVersion.TLS_1_3
import okhttp3.internal.SuppressSignatureCheck
import okio.ByteString.Companion.toByteString
import java.io.File
import java.lang.ProcessBuilder.Redirect
import java.util.logging.Handler
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger
import javax.crypto.SecretKey
import javax.net.ssl.SSLSession
import javax.net.ssl.SSLSocket


/**
 * Logs SSL keys to a log file, allowing Wireshark to decode traffic and be examined with http2
 * filter. The approach is to hook into JSSE log events for the messages between client and server
 * during handshake, and then take the agreed masterSecret from private fields of the session.
 *
 * Copy WireSharkKeyLoggerListener to your test code to use in development.
 *
 * This logs TLSv1.2 on a JVM (OpenJDK 11+) without any additional code.  For TLSv1.3
 * an existing external tool is required.
 *
 * See https://stackoverflow.com/questions/61929216/how-to-log-tlsv1-3-keys-in-jsse-for-wireshark-to-decode-traffic
 *
 * Steps to run in your own code
 *
 * 1. In your main method `WireSharkListenerFactory.register()`
 * 2. Create Listener factory `val eventListenerFactory = WireSharkListenerFactory(
logFile = File("/tmp/key.log"), tlsVersions = tlsVersions, launch = launch)`
 * 3. Register with `client.eventListenerFactory(eventListenerFactory)`
 * 4. Launch wireshark if not done externally `val process = eventListenerFactory.launchWireShark()`
 */
@SuppressSignatureCheck
class WireSharkListenerFactory(
  private val logFile: File,
  private val tlsVersions: List<TlsVersion>? = null,
  private val launch: Launch? = null
) : EventListener.Factory {
  override fun create(call: Call): EventListener {
    return WireSharkKeyLoggerListener(logFile, launch == null)
  }

  fun launchWireShark(): Process? {
    when (launch) {
      null -> {
        if (tlsVersions?.contains(TLS_1_2) == true) {
          println("TLSv1.2 traffic will be logged automatically and available via wireshark")
        }

        if (tlsVersions?.contains(TLS_1_3) == true) {
          println("TLSv1.3 requires an external command run before first traffic is sent")
          println("Follow instructions at https://github.com/neykov/extract-tls-secrets for TLSv1.3")
          println("Pid: ${ProcessHandle.current().pid()}")

          Thread.sleep(10000)
        }
      }
      CommandLine -> {
        return ProcessBuilder(
          "tshark", "-l", "-V", "-o", "tls.keylog_file:$logFile", "-Y", "http2", "-O", "http2,tls")
          .redirectInput(File("/dev/null"))
          .redirectOutput(Redirect.INHERIT)
          .redirectError(Redirect.INHERIT)
          .start().also {
            // Give it time to start collecting
            Thread.sleep(200)
          }
      }
      Gui -> {
        return ProcessBuilder(
          "nohup", "wireshark", "-o", "tls.keylog_file:$logFile", "-S", "-l", "-Y", "http2", "-k")
          .redirectInput(File("/dev/null"))
          .redirectOutput(File("/dev/null"))
          .redirectError(Redirect.INHERIT)
          .start().also {
            // Give it time to start collecting
            Thread.sleep(2000)
          }
      }
    }

    return null
  }

  class WireSharkKeyLoggerListener(
    private val logFile: File,
    private val verbose: Boolean = false
  ) : EventListener() {
    var random: String? = null
    lateinit var currentThread: Thread

    private val loggerHandler = object : Handler() {
      override fun publish(record: LogRecord) {
        // Try to avoid multi threading issues with concurrent requests
        if (Thread.currentThread() != currentThread) {
          return
        }

        // https://timothybasanov.com/2016/05/26/java-pre-master-secret.html
        // https://security.stackexchange.com/questions/35639/decrypting-tls-in-wireshark-when-using-dhe-rsa-ciphersuites
        // https://stackoverflow.com/questions/36240279/how-do-i-extract-the-pre-master-secret-using-an-openssl-based-client

        // TLSv1.2 Events
        // Produced ClientHello handshake message
        // Consuming ServerHello handshake message
        // Consuming server Certificate handshake message
        // Consuming server CertificateStatus handshake message
        // Found trusted certificate
        // Consuming ECDH ServerKeyExchange handshake message
        // Consuming ServerHelloDone handshake message
        // Produced ECDHE ClientKeyExchange handshake message
        // Produced client Finished handshake message
        // Consuming server Finished handshake message
        // Produced ClientHello handshake message
        //
        // Raw write
        // Raw read
        // Plaintext before ENCRYPTION
        // Plaintext after DECRYPTION
        val message = record.message
        val parameters = record.parameters

        if (parameters != null && !message.startsWith("Raw") && !message.startsWith("Plaintext")) {
          if (verbose) {
            println(record.message)
            println(record.parameters[0])
          }

          // JSSE logs additional messages as parameters that are not referenced in the log message.
          val parameter = parameters[0] as String

          if (message == "Produced ClientHello handshake message") {
            random = readClientRandom(parameter)
          }
        }
      }

      override fun flush() {}

      override fun close() {}
    }

    private fun readClientRandom(param: String): String? {
      val matchResult = randomRegex.find(param)

      return if (matchResult != null) {
        matchResult.groupValues[1].replace(" ", "")
      } else {
        null
      }
    }

    override fun secureConnectStart(call: Call) {
      // Register to capture "Produced ClientHello handshake message".
      currentThread = Thread.currentThread()
      logger.addHandler(loggerHandler)
    }

    override fun secureConnectEnd(
      call: Call,
      handshake: Handshake?
    ) {
      logger.removeHandler(loggerHandler)
    }

    override fun callEnd(call: Call) {
      // Cleanup log handler if failed.
      logger.removeHandler(loggerHandler)
    }

    override fun connectionAcquired(
      call: Call,
      connection: Connection
    ) {
      if (random != null) {
        val sslSocket = connection.socket() as SSLSocket
        val session = sslSocket.session

        val masterSecretHex = session.masterSecret?.encoded?.toByteString()
          ?.hex()

        if (masterSecretHex != null) {
          val keyLog = "CLIENT_RANDOM $random $masterSecretHex"

          if (verbose) {
            println(keyLog)
          }
          logFile.appendText("$keyLog\n")
        }
      }

      random = null
    }

    enum class Launch {
      Gui, CommandLine
    }
  }

  companion object {
    private lateinit var logger: Logger

    private val SSLSession.masterSecret: SecretKey?
      get() = javaClass.getDeclaredField("masterSecret")
        .apply {
          isAccessible = true
        }
        .get(this) as? SecretKey

    val randomRegex = "\"random\"\\s+:\\s+\"([^\"]+)\"".toRegex()

    fun register() {
      // Enable JUL logging for SSL events, must be activated early or via -D option.
      System.setProperty("javax.net.debug", "")
      logger = Logger.getLogger("javax.net.ssl")
        .apply {
          level = Level.FINEST
          useParentHandlers = false
        }
    }
  }
}
