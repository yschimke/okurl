package com.baulsupp.oksocial.commands

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.kotlin.request
import com.baulsupp.oksocial.output.UsageException
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory
import java.nio.file.FileSystems
import javax.script.ScriptEngine
import javax.script.ScriptException
import kotlin.system.exitProcess

class OkApiCommand : ShellCommand, MainAware {
  private lateinit var main: Main

  override fun name(): String {
    return "okapi"
  }

  override fun setMain(main: Main) {
    this.main = main
  }

  override fun buildRequests(
    client: OkHttpClient,
    arguments: List<String>
  ): List<Request> {
    val args = arguments.toMutableList()

    val script = FileSystems.getDefault().getPath(args.removeAt(0))

    val engine = KotlinJsr223JvmLocalScriptEngineFactory().scriptEngine

    val lines = script.toFile().readText()

    if (args.size < 1) {
      System.err.println("usage: okapi file.kts arguments")
      exitProcess(-2)
    }

    return args.map { item ->
      engine.put("item", item)
      eval(engine, "val item = bindings[\"item\"] as String")
      val result = eval(engine, lines)

      toRequest(result)
    }
  }

  fun credentials(name: String): Any? {
    if (!this::main.isInitialized) {
      val interceptor = main.authenticatingInterceptor.getByName(name)

      if (interceptor != null) {
        return main.credentialsStore.get(interceptor.serviceDefinition, main.token())
      }
    }

    return null
  }

  fun eval(engine: ScriptEngine, script: String): Any? {
    return try {
      engine.eval(script)
    } catch (e: ScriptException) {
      var cause: Throwable? = e.cause
      while (cause != null) {
        if (cause is UsageException) {
          throw cause
        }
        cause = cause.cause
      }

      throw RuntimeException(e)
    }
  }

  private fun toRequest(o: Any?): Request {
    return when (o) {
      is Request -> o
      is String -> request(o)
      null -> throw NullPointerException()
      else -> throw IllegalStateException("unable to use result " + o + " " + o.javaClass)
    }
  }
}
