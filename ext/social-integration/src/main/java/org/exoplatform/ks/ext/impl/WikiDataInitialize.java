package org.exoplatform.ks.ext.impl;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.social.core.space.SpaceListenerPlugin;
import org.exoplatform.social.core.space.spi.SpaceLifeCycleEvent;

public class WikiDataInitialize extends SpaceListenerPlugin {

  private final InitParams params;
  
  public WikiDataInitialize(InitParams params) {
    this.params = params;
  }
  
  @Override
  public void applicationActivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void applicationAdded(SpaceLifeCycleEvent event) {
    
  }

  @Override
  public void applicationDeactivated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void applicationRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void grantedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void joined(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void left(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void revokedLead(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void spaceCreated(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void spaceRemoved(SpaceLifeCycleEvent event) {
    // TODO Auto-generated method stub
    
  }

}
