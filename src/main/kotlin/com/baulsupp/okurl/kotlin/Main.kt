package com.baulsupp.okurl.kotlin

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.commands.CommandLineClient
import com.baulsupp.okurl.util.ClientException
import kotlinx.coroutines.runBlocking
import okhttp3.Protocol
import okhttp3.internal.platform.Platform
import org.conscrypt.Conscrypt
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import picocli.CommandLine
import java.io.File
import java.security.Security
import javax.script.ScriptException
import kotlin.reflect.KClass
import kotlin.system.exitProcess

@CommandLine.Command(name = Main.NAME, description = ["Kotlin scripting for APIs"],
  mixinStandardHelpOptions = true, versionProvider = Main.Companion.VersionProvider::class)
class Main : CommandLineClient() {
  override fun initialise() {
    super.initialise()

    OkShell.instance = OkShell(this)
  }

  override fun runCommand(runArguments: List<String>): Int {
    val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    if (runArguments.isEmpty()) {
      System.err.println("usage: okscript file.kts arguments")
      return -2
    }

    val script = runArguments[0]
    args = runArguments.drop(1)

    try {
      engine.eval(File(script).readText())
      return 0
    } catch (se: ScriptException) {
      val cause = se.cause

      val message = se.message
      checkKnownException(UsageException::class, message)
      checkKnownException(ClientException::class, message)

      throw cause ?: se
    }
  }

  private fun checkKnownException(exceptionClass: KClass<*>, message: String?) {
    val badprefix = "${exceptionClass.qualifiedName}: "
    if (message != null && message.startsWith(badprefix)) {
      // TODO better handling
      throw usage(message.lines().first().substring(badprefix.length))
    }
  }

  override fun name(): String = NAME

  companion object {
    const val NAME = "okscript"

    class VersionProvider : CommandLine.IVersionProvider {
      override fun getVersion(): Array<String> {
        return arrayOf(
          "${Main.NAME} ${versionString()}",
          "Protocols: ${Protocol.values().joinToString(", ")}",
          "Platform: ${Platform.get()::class.java.simpleName}"
        )
      }
    }
  }
}

fun main(args: Array<String>): Unit = runBlocking<Unit> {
  Security.insertProviderAt(Conscrypt.newProviderBuilder().provideTrustManager(true).build(), 1)

  exitProcess(CommandLine(Main()).execute(*args))
}
