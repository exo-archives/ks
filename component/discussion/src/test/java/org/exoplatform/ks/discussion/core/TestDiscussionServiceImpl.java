/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.discussion.core;


import org.chromattic.api.ChromatticBuilder;
import org.chromattic.apt.InstrumentorImpl;
import org.exoplatform.ks.discussion.api.Channel;
import org.exoplatform.ks.discussion.api.Discussion;
import org.exoplatform.ks.test.ConfigurationUnit;
import org.exoplatform.ks.test.ConfiguredBy;
import org.exoplatform.ks.test.ContainerScope;
import org.exoplatform.ks.test.jcr.AbstractJCRTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/discussion-configuration.xml")})
public class TestDiscussionServiceImpl extends AbstractJCRTestCase {

  @Test
  public void testGetWorkspace() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    Workspace workspace = discussionService.getWorkspace();
    Assert.assertNotNull(workspace, "discussion workspace is null");
    Channel channel = workspace.getDefaultChannel();
    Assert.assertNotNull(channel, "default channel was null");
    Assert.assertNotNull(channel.getId());
  }
  
  @Test
  public void testCreateDiscussion() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    Discussion disc = discussionService.createDiscussion();
    Assert.assertNotNull(disc);
    String name = disc.getName();
    Assert.assertNotNull(name);
    Channel channel = disc.getChannel();
    Assert.assertEquals(channel.getName(), "default");
  }
  
  
  @Test
  public void testFindDiscussionById() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    Discussion disc = discussionService.createDiscussion();
    String discussionId = disc.getId();
    Discussion actual = discussionService.findDiscussion(discussionId);
    Assert.assertNotNull(actual);
    Assert.assertEquals(actual.getName(), disc.getName());
    
  }
  
  class TestDiscussionService extends DiscussionServiceImpl {
    
    public TestDiscussionService() {
      super();
      builder.setOption(ChromatticBuilder.INSTRUMENTOR_CLASSNAME, InstrumentorImpl.class.getName());
      
      // we redefine
      builder.setOption(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, TestSessionLifeCycle.class.getName()); 
      
    }
    
  }
  
}
