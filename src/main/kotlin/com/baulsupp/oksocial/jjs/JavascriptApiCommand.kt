package com.baulsupp.oksocial.jjs

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.commands.MainAware
import com.baulsupp.oksocial.commands.ShellCommand
import com.baulsupp.oksocial.output.util.UsageException
import com.google.common.collect.Lists
import jdk.nashorn.api.scripting.ScriptObjectMirror
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.function.Function
import java.util.stream.Collectors.joining
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException

class JavascriptApiCommand : ShellCommand, MainAware {
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
        var multiple = false
        val args = arguments.toMutableList()

        if (args[0] == "-m") {
            multiple = true
            args.removeAt(0)
        }

        val script = FileSystems.getDefault().getPath(args.removeAt(0))

        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")

        eval(engine, "param = Java.type(\"com.baulsupp.oksocial.jjs.OkShell\").readParam;")

        engine.put("client", client)
        engine.put("clientBuilder", client.newBuilder())
        engine.put("requestBuilder", requestBuilder)
        engine.put("credentials", Function<String, Any?> { t -> credentials(t) })

        val lines = Files.lines(script, StandardCharsets.UTF_8).skip(1).collect(joining("\n"))

        if (multiple) {
            // TODO how to do this without engine.eval
            engine.put("a", args)
            val argumentsJs = engine.eval("Java.from(a)")

            engine.put("arguments", argumentsJs)

            val result = eval(engine, lines)

            return toRequestList(requestBuilder, result)
        } else {
            return args.map { item ->
                engine.put("item", item)
                val result = eval(engine, lines)

                toRequest(requestBuilder, result)
            }
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

    private fun toRequestList(requestBuilder: Request.Builder, result: Any?): List<Request> {
        return if (result is ScriptObjectMirror) {

            val list = Lists.newArrayList<Request>()

            result.values.mapTo(list) { toRequest(requestBuilder, it) }

            list
        } else {
            listOf(toRequest(requestBuilder, result))
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
