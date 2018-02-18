package com.baulsupp.oksocial.kotlin

import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase
import org.jetbrains.kotlin.cli.common.repl.ScriptArgsWithTypes
import org.jetbrains.kotlin.com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.org.apache.log4j.Level
import org.jetbrains.kotlin.script.jsr223.KotlinStandardJsr223ScriptTemplate
import java.io.File
import java.lang.management.ManagementFactory
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine

class KotlinAppScriptFactory : KotlinJsr223JvmScriptEngineFactoryBase() {

  override fun getScriptEngine(): ScriptEngine =
    KotlinAppScriptEngine(
      Disposer.newDisposable(),
      this,
      classpath(),
      KotlinStandardJsr223ScriptTemplate::class.qualifiedName!!,
      { ctx, types ->
        ScriptArgsWithTypes(arrayOf(ctx.getBindings(ScriptContext.ENGINE_SCOPE)), types ?: emptyArray())
      },
      arrayOf(Bindings::class)
    )

  private fun classpath(): List<File> {
    return ManagementFactory.getRuntimeMXBean().classPath.split(File.pathSeparator).map { File(it) }.toList()
  }

  companion object {
    init {
      Logger.setFactory(BridgeLoggerFactory::class.java)
    }
  }
}

class BridgeLoggerFactory : Logger.Factory {
  override fun getLoggerInstance(name: String): Logger {
    return BridgeLogger(name)
  }
}

class BridgeLogger(name: String) : Logger() {
  val logger = java.util.logging.Logger.getLogger(name)!!

  override fun warn(p0: String?, p1: Throwable?) {
//    logger.log(WARNING, p0, p1)
  }

  override fun setLevel(p0: Level?) {
  }

  override fun info(p0: String?) {
//    logger.log(INFO, p0)
  }

  override fun info(p0: String?, p1: Throwable?) {
//    logger.log(INFO, p0, p1)
  }

  override fun error(p0: String?, p1: Throwable?, vararg p2: String?) {
//    logger.log(SEVERE, p0?.format(p2), p1)
  }

  override fun isDebugEnabled(): Boolean {
    return false
//    return logger.isLoggable(FINER)
  }

  override fun debug(p0: String?) {
//    logger.finer(p0)
  }

  override fun debug(p0: Throwable?) {
//    logger.log(FINER, "", p0)
  }

  override fun debug(p0: String?, p1: Throwable?) {
//    logger.log(FINER, p0, p1)
  }
}
