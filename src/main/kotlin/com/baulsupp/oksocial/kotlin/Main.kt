package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.commands.CommandLineClient
import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.oksocial.util.ClientException
import io.airlift.airline.Command
import io.airlift.airline.ParseOptionConversionException
import io.airlift.airline.ParseOptionMissingValueException
import kotlinx.coroutines.experimental.runBlocking
import org.conscrypt.OpenSSLProvider
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.io.File
import java.security.Security
import javax.script.ScriptException
import kotlin.reflect.KClass

@Command(name = Main.NAME, description = "Kotlin scripting for APIs")
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
    com.baulsupp.oksocial.kotlin.args = runArguments.drop(1)

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
      throw UsageException(message.lines().first().substring(badprefix.length))
    }
  }

  companion object {
    const val NAME = "okscript"

    @JvmStatic
    fun main(vararg args: String) = runBlocking {
      Security.insertProviderAt(OpenSSLProvider(), 1)

      try {
        val result = CommandLineClient.fromArgs<Main>(*args).run()
        System.exit(result)
      } catch (e: ParseOptionMissingValueException) {
        System.err.println("${com.baulsupp.oksocial.Main.command}: ${e.message}")
        System.exit(-1)
      } catch (e: ParseOptionConversionException) {
        System.err.println("${com.baulsupp.oksocial.Main.command}: ${e.message}")
        System.exit(-1)
      } catch (e: UsageException) {
        System.err.println("${com.baulsupp.oksocial.Main.command}: ${e.message}")
        System.exit(-1)
      } catch (e: Throwable) {
        e.printStackTrace()
        System.exit(-1)
      }
    }
  }
}
