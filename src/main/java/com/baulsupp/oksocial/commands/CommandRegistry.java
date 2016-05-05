package com.baulsupp.oksocial.commands;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class CommandRegistry {
  private List<ShellCommand> commands;

  public CommandRegistry() {
    commands = Lists.newArrayList(ServiceLoader.load(ShellCommand.class).iterator());
  }

  public List<String> names() {
    return commands.stream().map(ShellCommand::name).collect(Collectors.toList());
  }

  public Optional<ShellCommand> getCommandByName(String name) {
    return commands.stream().filter(c -> c.name().equals(name)).findFirst();
  }
}
