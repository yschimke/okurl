package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.commands.CommandLineClient
import com.baulsupp.oksocial.credentials.InMemoryCredentialsStore
import io.airlift.airline.Command
import java.io.File
import javax.script.ScriptException

@Command(name = Main.NAME, description = "Kotlin scripting for APIs")
class Main : CommandLineClient() {
  init {
    this.credentialsStore = InMemoryCredentialsStore()
  }

  override fun initialise() {
    super.initialise()

    OkShell.instance = OkShell(this)
  }

  override fun runCommand(runArguments: List<String>): Int {
    val engine = KotlinAppScriptFactory().scriptEngine

    if (runArguments.isEmpty()) {
      System.err.println("usage: okscript file.kts arguments")
      return -2
    }

    val script = runArguments[0]
    val arguments = runArguments.drop(1)

    engine.put("arguments", arguments)

    try {
      engine.eval(File(script).readText())
      return 0
    } catch (se: ScriptException) {
      val cause = se.cause
      throw cause ?: se
    }
  }

  companion object {
    const val NAME = "okscript"

    @JvmStatic
    fun main(vararg args: String) {
      val result = CommandLineClient.fromArgs<Main>(*args).run()
      System.exit(result)
    }
  }
}