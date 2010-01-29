/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.ks.rss;

import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.test.ConfigurationUnit;
import org.exoplatform.ks.test.ConfiguredBy;
import org.exoplatform.ks.test.ContainerScope;
import org.exoplatform.ks.test.jcr.AbstractJCRTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy( {
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/rss-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/storage-configuration.xml") })
public class TestForumFeedGenerator extends AbstractJCRTestCase {

  ForumFeedGenerator generator = null;
  KSDataLocation locator = null;
  @BeforeMethod
  protected void setUp() throws Exception {
    locator = new KSDataLocation(getRepository(), getWorkspace());
    generator = new ForumFeedGenerator(locator);
  }

  //@Test
  public void testItemAdded() throws Exception {
    String path = feedFixture();

    generator.itemAdded("/" + path);

    assertNodeExists(locator.getForumHomeLocation() + "/category001/"+RSS.RSS_NODE_NAME);
    assertNodeExists(locator.getForumHomeLocation() + "/category001/forum001/"+RSS.RSS_NODE_NAME);
    assertNodeExists(locator.getForumHomeLocation() + "/category001/forum001/topic001/"+RSS.RSS_NODE_NAME);
    
    Node categoryFeed = getNode(locator.getForumHomeLocation() + "/category001/"+RSS.RSS_NODE_NAME);
    Assert.assertTrue(categoryFeed.getProperty(RSS.CONTENT_PROPERTY).getString().contains("XXXmessage"));
    Assert.assertTrue(categoryFeed.getProperty(RSS.CONTENT_PROPERTY).getString().contains("XXXtitle"));
    Assert.assertTrue(categoryFeed.getProperty(RSS.CONTENT_PROPERTY).getString().contains("XXXauthor"));


  }
  
  
  @Test
  public void testItemRemoved() throws Exception {
    String path = feedFixture();
   
    generator.itemRemoved("/" + locator.getForumHomeLocation() + "/category001/forum001/topic001");

    assertNodeExists(locator.getForumHomeLocation() + "category001/"+RSS.RSS_NODE_NAME);
    assertNodeExists(locator.getForumHomeLocation() + "category001/forum001/"+RSS.RSS_NODE_NAME);
    assertNodeExists(locator.getForumHomeLocation() + "category001/forum001/topic001/"+RSS.RSS_NODE_NAME);
    
    Node categoryFeed = getNode("category001/"+RSS.RSS_NODE_NAME);
    Assert.assertTrue(!categoryFeed.getProperty(RSS.CONTENT_PROPERTY).getString().contains("XXXmessage"));
    Assert.assertTrue(!categoryFeed.getProperty(RSS.CONTENT_PROPERTY).getString().contains("XXXtitle"));
    Assert.assertTrue(!categoryFeed.getProperty(RSS.CONTENT_PROPERTY).getString().contains("XXXauthor"));


  }

  private String feedFixture() throws Exception {
    String forumService = locator.getForumHomeLocation();
    addNode(forumService);
    
    addNode(forumService + "/category001", "exo:forumCategory");
    Node category = getNode(forumService + "/category001");
    category.setProperty("exo:id", "category001");
    addNode(forumService + "/category001/forum001", "exo:forum");
    
    Node forum = getNode(forumService + "/category001/forum001");
    forum.setProperty("exo:id", "forum001");
    
    addNode(forumService + "/category001/forum001/topic001", "exo:topic");
    Node topic = getNode(forumService + "/category001/forum001/topic001");
    topic.setProperty("exo:id", "topic001");
    
    String path = forumService + "/category001/forum001/topic001/post001";
    addNode(path, "exo:post");

    Session session = getSession();
    Node post = session.getRootNode().getNode(path);
    post.setProperty("exo:link", "http://test?objectId=post001");
    post.setProperty("exo:id", "post001");
    post.setProperty("exo:isFirstPost", false);
    post.setProperty("exo:isApproved", true);
    post.setProperty("exo:isActiveByTopic", true);
    post.setProperty("exo:message", "XXXmessage");
    post.setProperty("exo:name", "XXXtitle");
    post.setProperty("exo:createdDate", new GregorianCalendar());
    post.setProperty("exo:owner", "XXXauthor");
    
    post.save();
    return path;
  }

}
