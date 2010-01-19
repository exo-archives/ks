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


import java.util.Date;
import java.util.List;

import org.chromattic.api.ChromatticBuilder;
import org.chromattic.apt.InstrumentorImpl;
import org.exoplatform.ks.discussion.api.Channel;
import org.exoplatform.ks.discussion.api.Discussion;
import org.exoplatform.ks.discussion.api.Message;
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

  

  /*@Test
  public void testGetWorkspace() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    Workspace workspace = discussionService.getWorkspace();
    Assert.assertNotNull(workspace, "discussion workspace is null");
    Channel channel = workspace.getDefaultChannel();
    Assert.assertNotNull(channel, "default channel was null");
    Assert.assertNotNull(channel.getId());
  }*/
  
  //@Test
  /*public void testCreateDiscussion() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    Message message = new TestMessage("title", "author", "body", new Date());
    Discussion disc = discussionService.startDiscussion(message);
    Assert.assertNotNull(disc);
    String name = disc.getName();
    Assert.assertNotNull(name);
    Channel channel = disc.getChannel();
    Assert.assertEquals(channel.getName(), "default");
  }*/
  
  
  /*@Test(expectedExceptions=IllegalArgumentException.class)
  public void testDiscussionWithoutStartMessage() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    discussionService.startDiscussion(null);
  }*/
  
  /*@Test
  public void testFindDiscussionById() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    Message message = new TestMessage("title", "author", "body", new Date());
    Discussion disc = discussionService.startDiscussion(message);
    String discussionId = disc.getId();
    Discussion actual = discussionService.findDiscussion(discussionId);
    Assert.assertNotNull(actual);
    Assert.assertEquals(actual.getName(), disc.getName());
    Assert.assertNotNull(disc.getStartMessage());
    
  }*/
  
  //@Test
 /* public void testAddReply() {
    DiscussionServiceImpl discussionService = new TestDiscussionService();
    Message message = new TestMessage("title", "author", "body", new Date());
   
    Discussion disc = discussionService.startDiscussion(message);
    String messageId = disc.getStartMessage().getId();
    
    Message reply = new TestMessage(null, "author2", "body2", null);
    Message actual = discussionService.reply(messageId, reply);
    
    Assert.assertNotNull(actual);
    Assert.assertEquals(actual.getAuthor(), "author2");
    Assert.assertNotNull(actual.getTimestamp()); // timestamp is added if missing
    
  }*/
  
  class TestDiscussionService extends DiscussionServiceImpl {
    
    public TestDiscussionService() {
      super();
      builder.setOption(ChromatticBuilder.INSTRUMENTOR_CLASSNAME, InstrumentorImpl.class.getName());
      
      // we redefine
      builder.setOption(ChromatticBuilder.SESSION_LIFECYCLE_CLASSNAME, TestSessionLifeCycle.class.getName()); 
      
    }
    
  }
  
  
  
  public class TestMessage implements Message {

    private String title;
    private String author;
    private String body;
    private Date timestamp;

    public TestMessage(String title, String author, String body, Date timestamp) {
      this.title = title;
      this.author = author;
      this.body=body;
      this.timestamp = timestamp;
    }

    public String getAuthor() {

      return author;
    }

    public String getBody() {

      return body;
    }

    public Message getParent() {

      return null;
    }

    public List<Message> getReplies() {

      return null;
    }

    public Date getTimestamp() {

      return timestamp;
    }

    public String getTitle() {

      return title;
    }

    public String getId() {
      
      return null;
    }

  }  
  
  
}
