package com.baulsupp.oksocial

object TestMain {
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        Main.main("https://graph.facebook.com/me")
    }
}
