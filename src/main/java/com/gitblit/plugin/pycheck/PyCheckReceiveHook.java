package com.gitblit.plugin.pycheck;

import com.gitblit.IStoredSettings;
import com.gitblit.extensions.ReceiveHook;
import com.gitblit.git.GitblitReceivePack;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.jgit.transport.ReceiveCommand;
import ro.fortsoft.pf4j.Extension;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Extension
public class PyCheckReceiveHook extends ReceiveHook {

  private Set<String> repositories;

  @Override
  public void onPreReceive(GitblitReceivePack receivePack, Collection<ReceiveCommand> commands) {
    if (repositories == null) {
      IStoredSettings settings = receivePack.getGitblit().getSettings();
      repositories =
          new HashSet<>(Arrays.asList(settings.getString("pycheck.repositories", "").split(",")));
    }

    if (commands.isEmpty()
        || !repositories.contains(receivePack.getRepository().getDirectory().getName())) {
      return;
    }

    ThreadAttributes.set(
        "currentDirectory", receivePack.getRepository().getDirectory().getAbsolutePath());

    try {
      new Hook(receivePack, commands).run();
    } catch (Throwable t) {
      receivePack.sendMessage(ExceptionUtils.getStackTrace(t));
      ReceiveCommand firstCommand = commands.iterator().next();
      receivePack.sendRejection(firstCommand, "bad format");
    }
  }

  @Override
  public void onPostReceive(GitblitReceivePack receivePack, Collection<ReceiveCommand> commands) {}
}
