package com.baulsupp.okurl.commands

import com.baulsupp.okurl.ws.OkWsCommand

class CommandRegistry {
  private val commands: List<ShellCommand> by lazy {
    listOf(OkurlCommand(), OkWsCommand())
  }

  fun names(): List<String> {
    return commands.map { it.name() }
  }

  fun getCommandByName(name: String): ShellCommand? {
    return commands.firstOrNull { c -> c.name() == name }
  }
}
