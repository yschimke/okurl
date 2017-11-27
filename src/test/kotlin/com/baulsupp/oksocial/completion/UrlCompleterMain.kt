package com.baulsupp.oksocial.completion

import com.baulsupp.oksocial.Main

object UrlCompleterMain {
  fun main(args: Array<String>): Int {
    val main = Main()
    main.initialise()

    main.arguments = mutableListOf(
        if (args.isNotEmpty()) args[0] else "https://graph.facebook.com/BooneStudio")
    return main.run()
  }
}
