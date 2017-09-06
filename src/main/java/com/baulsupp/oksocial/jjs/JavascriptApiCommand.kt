package com.baulsupp.oksocial.jjs

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.authenticator.AuthInterceptor
import com.baulsupp.oksocial.commands.MainAware
import com.baulsupp.oksocial.commands.ShellCommand
import com.google.common.base.Throwables
import com.google.common.collect.Lists
import com.baulsupp.oksocial.output.util.UsageException
import java.nio.charset.StandardCharsets
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.Collections
import java.util.Optional
import java.util.function.Function
import java.util.stream.Collectors
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager
import javax.script.ScriptException
import jdk.nashorn.api.scripting.ScriptObjectMirror
import okhttp3.OkHttpClient
import okhttp3.Request

import java.util.stream.Collectors.joining

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
                               requestBuilder: Request.Builder, arguments: MutableList<String>): List<Request> {
        var multiple = false

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
        engine.put("credentials", Function<String, Any> { this.credentials(it) })

        val lines = Files.lines(script, StandardCharsets.UTF_8).skip(1).collect<String, *>(joining("\n"))

        if (multiple) {
            // TODO how to do this without engine.eval
            engine.put("a", arguments)
            val argumentsJs = engine.eval("Java.from(a)")

            engine.put("arguments", argumentsJs)

            val result = eval(engine, lines)

            return toRequestList(requestBuilder, result)
        } else {
            return arguments.stream().map { item ->
                engine.put("item", item)
                val result = eval(engine, lines)

                toRequest(requestBuilder, result)
            }.collect<List<Request>, Any>(Collectors.toList())
        }
    }

    fun credentials(name: String): Any? {
        if (main != null) {
            val interceptor = main!!.serviceInterceptor.getByName(name)

            if (interceptor.isPresent) {
                val credentials = main!!.credentialsStore.readDefaultCredentials<*>(interceptor.get().serviceDefinition())

                return credentials.orElse(null)
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
