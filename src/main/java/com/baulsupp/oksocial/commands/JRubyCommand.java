package com.baulsupp.oksocial.commands;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jruby.Ruby;
import org.jruby.RubyRuntimeAdapter;
import org.jruby.ast.executable.Script;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.builtin.IRubyObject;

public class JRubyCommand {
  public static ShellCommand load(Path scriptPath) throws Exception {
    List<String> lines = Files.readAllLines(scriptPath);

    List<String> loadPaths = new ArrayList<String>();
    Ruby runtime = JavaEmbedUtils.initialize(loadPaths);
    RubyRuntimeAdapter evaler = JavaEmbedUtils.newRuntimeAdapter();

    try (InputStream fis = Files.newInputStream(scriptPath)) {
      IRubyObject x = evaler.parse(runtime, fis, scriptPath.toString(), 0).run();

      return (ShellCommand) x.toJava(ShellCommand.class);
    }
  }
}
