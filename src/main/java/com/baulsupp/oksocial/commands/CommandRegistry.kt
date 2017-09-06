package com.baulsupp.oksocial.commands

import com.google.common.collect.Lists
import java.util.Optional
import java.util.ServiceLoader
import java.util.stream.Collectors

class CommandRegistry {
    private val commands: List<ShellCommand>

    init {
        commands = Lists.newArrayList(ServiceLoader.load(ShellCommand::class.java).iterator())
    }

    fun names(): List<String> {
        return commands.stream().map<String>(Function<ShellCommand, String> { it.name() }).collect<List<String>, Any>(Collectors.toList())
    }

    fun getCommandByName(name: String): Optional<ShellCommand> {
        return commands.stream().filter { c -> c.name() == name }.findFirst()
    }
}
