//@file:JvmName(name="Main")

package com.baulsupp.oksocial.kotlin

import com.baulsupp.oksocial.util.LoggingUtil
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
  LoggingUtil.configureLogging(false, false)

  val factory = KotlinAppScriptFactory()
  val engine = factory!!.scriptEngine
  val bindings = engine.createBindings()

  if (args.size < 1) {
    System.err.println("usage: okscript file.kts arguments");
    exitProcess(-2)
  }

  val script = args.get(0)
  val arguments = args.drop(1)

  bindings.put("arguments", arguments)

  engine.eval(File(script).readText())
//  kscript.a
}