package com.baulsupp.oksocial.jjs

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.commands.MainAware
import com.baulsupp.oksocial.commands.ShellCommand
import com.baulsupp.oksocial.kotlin.KotlinAppScriptFactory
import com.baulsupp.oksocial.output.util.UsageException
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.FileSystems
import javax.script.ScriptEngine
import javax.script.ScriptException
import kotlin.system.exitProcess

class OkApiCommand : ShellCommand, MainAware {
  private var main: Main? = null

  override fun name(): String {
    return "okapi"
  }

  override fun setMain(main: Main) {
    this.main = main
  }

  @Throws(Exception::class)
  override fun buildRequests(client: OkHttpClient,
                             requestBuilder: Request.Builder, arguments: List<String>): List<Request> {
    val args = arguments.toMutableList()

    val script = FileSystems.getDefault().getPath(args.removeAt(0))

    val engine = KotlinAppScriptFactory().scriptEngine

    val lines = script.toFile().readText()

    if (args.size < 1) {
      System.err.println("usage: okapi file.kts arguments");
      exitProcess(-2)
    }

    return args.map { item ->
      engine.put("item", item)
      eval(engine, "val item = bindings[\"item\"] as String");
      val result = eval(engine, lines)

      toRequest(requestBuilder, result)
    }
  }

  fun credentials(name: String): Any? {
    if (main != null) {
      val interceptor = main!!.serviceInterceptor!!.getByName(name)

      if (interceptor != null) {
        return main!!.credentialsStore!!.readDefaultCredentials(interceptor.serviceDefinition())
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

  private fun toRequest(requestBuilder: Request.Builder, o: Any?): Request {
    return when (o) {
      is Request -> o
      is String -> requestBuilder.url((o as String?)!!).build()
      null -> throw NullPointerException()
      else -> throw IllegalStateException("unable to use result " + o + " " + o.javaClass)
    }
  }
}
