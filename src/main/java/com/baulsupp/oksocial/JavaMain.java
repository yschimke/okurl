package com.baulsupp.oksocial;

import com.baulsupp.oksocial.output.UsageException;
import com.google.common.collect.ImmutableMap;
import io.airlift.airline.*;
import io.airlift.airline.model.CommandMetadata;
import io.airlift.airline.model.MetadataLoader;

import java.util.Arrays;

public class JavaMain extends Main {
  public JavaMain() {
  }

  public static void main(String[] args) {
    CommandMetadata metadata = MetadataLoader.loadCommand(JavaMain.class);
    Parser parser = new Parser();
    ParseState state = parser.parseCommand(metadata, Arrays.asList(args));
//    this.validate(state);
    CommandMetadata command = state.getCommand();

    try {
      CommandFactory<JavaMain> commandFactory = aClass -> new JavaMain();
      JavaMain instance = ParserUtil.createInstance(JavaMain.class, command.getAllOptions(), state.getParsedOptions(), command.getArguments(), state.getParsedArguments(), command.getMetadataInjections(), ImmutableMap.of(CommandMetadata.class, metadata), commandFactory);
      int result = instance.runBlocking();
      System.exit(result);
    } catch (ParseOptionMissingValueException e) {
      System.err.println("$command: ${e.message}");
      System.exit(-1);
    } catch (ParseOptionConversionException e) {
      System.err.println("$command: ${e.message}");
      System.exit(-1);
    } catch (UsageException e) {
      System.err.println("${com.baulsupp.oksocial.Main.command}: ${e.message}");
      System.exit(-1);
    } catch (Throwable e) {
      e.printStackTrace();
      System.exit(-1);
    }
  }
}
