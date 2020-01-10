package com.gitblit.plugin.pycheck;

import com.gitblit.git.GitblitReceivePack;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.ReceiveCommand;

import java.util.*;

import static com.gitblit.plugin.pycheck.Utils.CommandResult;

public class Hook {

  private static final int SKIP_MORE_ERRORS = 3;

  private static final String GIT_EXE_PATH = Utils.findExecutable("git");

  private final Checker checker;

  private final GitblitReceivePack receivePack;
  private final Collection<ReceiveCommand> commands;
  private final Set<ChangedFile> changedFiles;

  public Hook(GitblitReceivePack receivePack, Collection<ReceiveCommand> commands) {
    this.receivePack = receivePack;
    this.commands = commands;
    this.changedFiles = collectChangedFiles(commands);
    this.checker = new DefaultChecker();
  }

  public void run() {
    List<String> errors = new ArrayList<>();
    for (ChangedFile cf : changedFiles) {
      String content = getFileContent(cf.filename, cf.revision);
      if (!isPyFile(cf.filename, content)) {
        continue;
      }

      String error = checker.check(content);
      if (error.isEmpty()) {
        continue;
      }

      errors.add(formatErrorMessage(cf.filename, error));
      if (errors.size() > SKIP_MORE_ERRORS) {
        break;
      }
    }

    if (!errors.isEmpty()) {
      receivePack.sendMessage(StringUtils.join(errors, "\n"));
      ReceiveCommand firstCommand = commands.iterator().next();
      receivePack.sendRejection(firstCommand, "bad format");
    }
  }

  private String formatErrorMessage(String filename, String error) {
    return StringUtils.joinWith(
        "\n",
        StringUtils.repeat("-", 60),
        String.format(
            "bad format for file \"%s\", please format by \"black\" command.\n", filename),
        StringUtils.strip(error));
  }

  private boolean isPyFile(String filename, String content) {
    if (filename.endsWith(".py")) {
      return true;
    }

    String firstLine = content.split("\n")[0];
    return firstLine.startsWith("#!") && firstLine.contains("python");
  }

  private String getFileContent(String filename, String revision) {
    return Utils.execute(GIT_EXE_PATH, "show", revision + ":" + filename).output;
  }

  private Set<ChangedFile> collectChangedFiles(Collection<ReceiveCommand> commands) {
    Set<ChangedFile> ret = new LinkedHashSet<>();
    for (ReceiveCommand command : commands) {
      for (ChangedFile cf : collectChangedFiles(command)) {
        if (ret.contains(cf)) {
          continue;
        }

        ret.add(cf);
      }
    }
    return ret;
  }

  private Set<ChangedFile> collectChangedFiles(ReceiveCommand command) {
    CommandResult result =
        command.getOldId().equals(ObjectId.zeroId())
            ? Utils.execute(GIT_EXE_PATH, "ls-tree", "-r", command.getNewId().name(), "--name-only")
            : Utils.execute(
                GIT_EXE_PATH,
                "show",
                StringUtils.joinWith("...", command.getOldId().name(), command.getNewId().name()),
                "--pretty=format:",
                "--name-only");

    Set<ChangedFile> ret = new HashSet<>();
    for (String filename : result.output.split("\n")) {
      ret.add(new ChangedFile(filename, command.getNewId().name()));
    }
    return ret;
  }

  static class ChangedFile {
    String filename, revision;

    ChangedFile(String filename, String revision) {
      this.filename = filename;
      this.revision = revision;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ChangedFile that = (ChangedFile) o;
      return filename.equals(that.filename);
    }

    @Override
    public int hashCode() {
      return filename.hashCode();
    }
  }
}
