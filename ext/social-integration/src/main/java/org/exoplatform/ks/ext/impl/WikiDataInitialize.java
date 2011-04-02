package org.exoplatform.ks.ext.impl;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class WikiDataInitialize extends SpaceListenerPlugin {

  private final InitParams params;

  public WikiDataInitialize(InitParams params) {
    this.params = params;
  }

  public InitParams getParams() {
    return params;
  }

  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
  }

}
