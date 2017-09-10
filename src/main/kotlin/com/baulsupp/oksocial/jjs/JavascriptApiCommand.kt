package com.baulsupp.oksocial.jjs

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.commands.MainAware
import com.baulsupp.oksocial.commands.ShellCommand
import com.baulsupp.oksocial.output.util.UsageException
import com.google.common.base.Throwables
import com.google.common.collect.Lists
import jdk.nashorn.api.scripting.ScriptObjectMirror
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Collectors.joining
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import kotlin.streams.toList

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
                               requestBuilder: Request.Builder, args: List<String>): List<Request> {
        var multiple = false
        val arguments = args.toMutableList()

        if (arguments[0] == "-m") {
            multiple = true
            arguments.removeAt(0)
        }

        val script = FileSystems.getDefault().getPath(arguments.removeAt(0))

        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")

        eval(engine, "param = Java.type(\"com.baulsupp.oksocial.jjs.OkShell\").readParam;")

        engine.put("client", client)
        engine.put("clientBuilder", client.newBuilder())
        engine.put("requestBuilder", requestBuilder)
        engine.put("credentials", this::credentials)

        val lines = Files.lines(script, StandardCharsets.UTF_8).skip(1).collect(joining("\n"))

        if (multiple) {
            // TODO how to do this without engine.eval
            engine.put("a", arguments)
            val argumentsJs = engine.eval("Java.from(a)")

            engine.put("arguments", argumentsJs)

            val result = eval(engine, lines)

            return toRequestList(requestBuilder, result)
        } else {
            return arguments.map { item ->
                engine.put("item", item)
                val result = eval(engine, lines)

                toRequest(requestBuilder, result)
            }
        }
    }

    fun credentials(name: String): Any? {
        if (main != null) {
            val interceptor = main!!.serviceInterceptor.getByName(name)

            if (interceptor != null) {
                val credentials = main!!.credentialsStore.readDefaultCredentials(interceptor.serviceDefinition())

                return credentials
            }
        }

        return null
    }

    fun eval(engine: ScriptEngine, script: String): Any {
        try {
            return engine.eval(script)
        } catch (e: ScriptException) {
            var cause: Throwable? = e.cause
            while (cause != null) {
                if (cause is UsageException) {
                    throw cause
                }
                cause = cause.cause
            }

            throw Throwables.propagate(e)
        }

    }

    private fun toRequestList(requestBuilder: Request.Builder, result: Any): List<Request> {
        if (result is ScriptObjectMirror) {

            val list = Lists.newArrayList<Request>()

            for (o in result.values) {
                list.add(toRequest(requestBuilder, o))
            }

            return list
        } else {
            return listOf(toRequest(requestBuilder, result))
        }
    }

    private fun toRequest(requestBuilder: Request.Builder, o: Any?): Request {
        if (o is Request) {
            return o
        } else if (o is String) {
            return requestBuilder.url((o as String?)!!).build()
        } else if (o == null) {
            throw NullPointerException()
        } else {
            throw IllegalStateException("unable to use result " + o + " " + o.javaClass)
        }
    }
}
