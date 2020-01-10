package com.gitblit.plugin.pycheck;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.gitblit.plugin.pycheck.Utils.CommandResult;

public class DefaultChecker implements Checker {

  private static final String BLACK_EXE_PATH = Utils.findExecutable("black");

  private static final int BLACK_COMMAND_FORMAT_ERROR_CODE = 123;
  private static final String FORMAT_ERROR_MESSAGE = "can not format, maybe syntax error!";

  private static final int DIFFERENCE_HIDE_MORE_LINES = 20;

  @Override
  public String check(String content) {
    File file;
    try {
      file = File.createTempFile("gitblit-", ".pycheck");
      file.deleteOnExit();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    try {
      FileWriter writer = new FileWriter(file);
      IOUtils.write(content, writer);
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    CommandResult result =
        Utils.executeQuietly(BLACK_EXE_PATH, "--diff", "-q", file.getAbsolutePath());
    if (result.code == BLACK_COMMAND_FORMAT_ERROR_CODE) {
      return FORMAT_ERROR_MESSAGE;
    }

    result.throwIfError();

    if (result.output.isEmpty()) {
      return "";
    }

    List<String> diff = Arrays.asList(result.output.split("\n"));
    if (diff.size() < 3) {
      return "";
    }

    // remove diff header
    List<String> ret = new ArrayList<>();
    ret.addAll(diff.subList(2, Math.min(DIFFERENCE_HIDE_MORE_LINES, diff.size() - 2)));
    ret.addAll(Arrays.asList("", "Omit more ......"));

    return StringUtils.join(ret, "\n");
  }
}
