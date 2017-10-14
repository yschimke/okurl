package com.baulsupp.oksocial.commands

import com.google.common.collect.Lists
import java.util.ServiceLoader

class CommandRegistry {
    private val commands: List<ShellCommand>

    init {
        commands = Lists.newArrayList(ServiceLoader.load(ShellCommand::class.java).iterator())
    }

    fun names(): List<String> {
        return commands.map { it.name() }
    }

    fun getCommandByName(name: String): ShellCommand? {
        return commands.firstOrNull { c -> c.name() == name }
    }
}
