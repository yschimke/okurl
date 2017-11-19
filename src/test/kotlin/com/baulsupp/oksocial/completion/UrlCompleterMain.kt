package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.Main

import com.google.common.collect.Lists.newArrayList

object UrlCompleterMain {
  @Throws(Exception::class)
  fun main(args: Array<String>): Int {
    val main = Main()
    main.initialise()

    main.arguments = newArrayList(
        if (args.isNotEmpty()) args[0] else "https://graph.facebook.com/BooneStudio")
    return main.run()
  }
}
