package com.baulsupp.okurl.kotlin

import com.baulsupp.oksocial.output.UsageException
import com.baulsupp.okurl.commands.CommandLineClient
import com.baulsupp.okurl.util.ClientException
import com.github.rvesse.airline.HelpOption
import com.github.rvesse.airline.SingleCommand
import com.github.rvesse.airline.annotations.Command
import com.github.rvesse.airline.parser.errors.ParseException
import kotlinx.coroutines.runBlocking
import org.conscrypt.OpenSSLProvider
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.io.File
import java.security.Security
import javax.inject.Inject
import javax.script.ScriptException
import kotlin.reflect.KClass

@Command(name = Main.NAME, description = "Kotlin scripting for APIs")
class Main : CommandLineClient() {
  @Inject
  override lateinit var help: HelpOption<Main>

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
    com.baulsupp.okurl.kotlin.args = runArguments.drop(1)

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

  override fun name(): String = NAME

  companion object {
    const val NAME = "okscript"

    @JvmStatic
    fun main(vararg args: String) = runBlocking {
      Security.insertProviderAt(OpenSSLProvider(), 1)

      try {
        val result = SingleCommand.singleCommand(Main::class.java).parse(*args).run()
        System.exit(result)
      } catch (e: Throwable) {
        when (e) {
          is ParseException, is UsageException -> {
            System.err.println("okurl: ${e.message}")
            System.exit(-1)
          }
          else -> {
            e.printStackTrace()
            System.exit(-1)
          }
        }
      }
    }
  }
}
