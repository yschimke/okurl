package com.baulsupp.oksocial.commands

import java.util.ServiceLoader

class CommandRegistry {
  private val commands: List<ShellCommand> = ServiceLoader.load(ShellCommand::class.java).toList()

  fun names(): List<String> {
    return commands.map { it.name() }
  }

  fun getCommandByName(name: String): ShellCommand? {
    return commands.firstOrNull { c -> c.name() == name }
  }
}
