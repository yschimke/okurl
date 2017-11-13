package com.baulsupp.oksocial.kotlin

import com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.cli.common.repl.KotlinJsr223JvmScriptEngineFactoryBase
import org.jetbrains.kotlin.cli.common.repl.ScriptArgsWithTypes
import org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngine
import org.jetbrains.kotlin.script.jsr223.KotlinStandardJsr223ScriptTemplate
import java.io.File
import java.lang.management.ManagementFactory
import javax.script.Bindings
import javax.script.ScriptContext
import javax.script.ScriptEngine


class KotlinAppScriptFactory : KotlinJsr223JvmScriptEngineFactoryBase() {

  override fun getScriptEngine(): ScriptEngine =
      KotlinJsr223JvmLocalScriptEngine(
          Disposer.newDisposable(),
          this,
          classpath(),
          KotlinStandardJsr223ScriptTemplate::class.qualifiedName!!,
          { ctx, types -> ScriptArgsWithTypes(arrayOf(ctx.getBindings(ScriptContext.ENGINE_SCOPE)), types ?: emptyArray()) },
          arrayOf(Bindings::class)
      )

  private fun classpath(): List<File> {
    val classpathFromClassloader = ManagementFactory.getRuntimeMXBean().classPath.split(File.pathSeparator).map { File(it) }.toList()
        //classpathFromClassloader(KotlinAppScriptFactory::class.java.classLoader)!!

    println("Here " + KotlinAppScriptFactory::class.java.classLoader)
    classpathFromClassloader.forEach(::println)

    return classpathFromClassloader
  }
}