package com.gitblit.plugin.pycheck;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Utils {

  private static final String DEFAULT_PATH =
      "/usr/local/bin:/usr/local/sbin:/sbin:/bin:/usr/sbin:/usr/bin:";

  static String findExecutable(String name) {
    for (String s : (DEFAULT_PATH + System.getenv("PATH")).split(":")) {
      if (Files.isExecutable(Paths.get(s, name))) {
        return Paths.get(s, name).toString();
      }
    }

    throw new IllegalArgumentException("can not find executable for " + name);
  }

  static CommandResult execute(String... command) {
    CommandResult cr = executeQuietly(command);
    cr.throwIfError();
    return cr;
  }

  static CommandResult executeQuietly(String... command) {
    ProcessBuilder builder = new ProcessBuilder(command);
    Process process;
    try {
      builder.directory(new File(ThreadAttributes.get("currentDirectory")));
      process = builder.start();
    } catch (IOException e) {
      return CommandResult.error(1, e, command);
    }

    try {
      process.waitFor();
    } catch (InterruptedException e) {
      return CommandResult.error(2, e, command);
    }

    try {
      return new CommandResult(
          process.exitValue(),
          Arrays.toString(command),
          IOUtils.toString(process.getInputStream(), StandardCharsets.UTF_8),
          IOUtils.toString(process.getErrorStream(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      return CommandResult.error(3, e, command);
    }
  }

  static class CommandResult {
    int code;
    String command, output, error;

    CommandResult(int code, String command, String output, String error) {
      this.code = code;
      this.command = command;
      this.output = output;
      this.error = error;
    }

    static CommandResult error(int code, Exception ex, String... command) {
      return new CommandResult(
          code, Arrays.toString(command), "", ExceptionUtils.getStackTrace(ex));
    }

    void throwIfError() {
      if (code != 0) {
        throw new RuntimeException("run command error: " + this.toString());
      }
    }

    @Override
    public String toString() {
      return "CommandResult{"
          + "code="
          + code
          + ", command='"
          + command
          + '\''
          + ", output='"
          + output
          + '\''
          + ", error='"
          + error
          + '\''
          + '}';
    }
  }
}
