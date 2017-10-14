package com.baulsupp.oksocial.jjs

import com.baulsupp.oksocial.Main
import com.baulsupp.oksocial.location.Location
import com.baulsupp.oksocial.output.OutputHandler
import com.baulsupp.oksocial.util.FileContent
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class OkShell @Throws(Exception::class)
private constructor() {
    val client: OkHttpClient
    val requestBuilder: Request.Builder
    private val engine: ScriptEngine
    private val main: Main?
    val outputHandler: OutputHandler<Response>

    init {
        main = Main()
        main.initialise()
        client = main.client!!
        requestBuilder = main.createRequestBuilder()
        outputHandler = main.outputHandler!!

        val m = ScriptEngineManager()
        engine = m.getEngineByName("nashorn")

        listenForMainExit()
    }

    fun listenForMainExit() {
        val main = Thread.currentThread()

        val t = Thread({
            try {
                main.join()
            } catch (e: InterruptedException) {
            }

            close()
        }, "exit listener")
        t.isDaemon = true
        t.start()
    }

    @Throws(IOException::class)
    fun query(url: String): String {
        return execute(requestBuilder.url(url).build())
    }

    @Throws(IOException::class)
    fun execute(request: Request): String {
        val call = client.newCall(request)

        val response = call.execute()

        try {
            val responseString = response.body()!!.string()

            if (!response.isSuccessful) {
                val msg: String = if (responseString.isNotEmpty()) {
                    responseString
                } else {
                    response.code().toString() + " " + response.message()
                }

                throw RuntimeException(msg)
            }

            return responseString
        } finally {
            response.body()!!.close()
        }
    }

    fun warmup(url: String) {
        val request = requestBuilder.url(url).build()
        val call = client.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // ignore
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                // ignore
                response.close()
            }
        })
    }

    @Throws(IOException::class)
    fun show(url: String) {
        val request = requestBuilder.url(url).build()

        val call = client.newCall(request)

        val response = call.execute()

        outputHandler.showOutput(response)
    }

    fun credentials(name: String): Any? {
        if (main != null) {
            val interceptor = main.serviceInterceptor!!.getByName(name)

            if (interceptor != null) {
                return main.credentialsStore!!.readDefaultCredentials(interceptor.serviceDefinition())
            }
        }

        return null
    }

    @Throws(IOException::class)
    fun location(): Location? {
        return main!!.locationSource.read()
    }

    private fun close() {
        client.connectionPool().evictAll()
        client.dispatcher().executorService().shutdownNow()
    }

    override fun toString(): String {
        return "OkShell"
    }

    companion object {

        @Throws(Exception::class)
        @JvmStatic
        fun instance(): OkShell {
            return OkShell()
        }

        @Throws(IOException::class)
        fun readParam(param: String): String {
            return FileContent.readParamString(param)
        }
    }
}
