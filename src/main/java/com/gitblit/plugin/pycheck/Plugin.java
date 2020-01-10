package com.gitblit.plugin.pycheck;

import com.gitblit.extensions.GitblitWicketPlugin;
import com.gitblit.wicket.GitblitWicketApp;
import ro.fortsoft.pf4j.PluginWrapper;
import ro.fortsoft.pf4j.Version;

public class Plugin extends GitblitWicketPlugin {

  public Plugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public void start() {
    log.info("{} STARTED.", getWrapper().getPluginId());
  }

  @Override
  public void stop() {
    log.info("{} STOPPED.", getWrapper().getPluginId());
  }

  @Override
  public void onInstall() {
    log.info("{} INSTALLED.", getWrapper().getPluginId());
  }

  @Override
  public void onUpgrade(Version oldVersion) {
    log.info("{} UPGRADED from {}.", getWrapper().getPluginId(), oldVersion);
  }

  @Override
  public void onUninstall() {
    log.info("{} UNINSTALLED.", getWrapper().getPluginId());
  }

  @Override
  protected void init(GitblitWicketApp app) {}
}
