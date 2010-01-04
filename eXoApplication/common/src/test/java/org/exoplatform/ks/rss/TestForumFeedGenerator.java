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

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.test.ConfigurationUnit;
import org.exoplatform.ks.test.ConfiguredBy;
import org.exoplatform.ks.test.ContainerScope;
import org.exoplatform.ks.test.jcr.AbstractJCRTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy( {
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/rss-configuration.xml") })
public class TestForumFeedGenerator extends AbstractJCRTestCase {

  ForumFeedGenerator generator = null;

  @BeforeMethod
  protected void setUp() throws Exception {
    KSDataLocation locator = new KSDataLocation(getRepository(), getWorkspace());
    generator = new ForumFeedGenerator(locator);
  }

  @Test
  public void testItemAdded() throws Exception {
    addNode("category001", "exo:forumCategory");
    addNode("category001/forum001", "exo:forum");
    addNode("category001/forum001/topic001", "exo:topic");
    String path = "category001/forum001/topic001/post001";
    addNode(path, "exo:post");

    Session session = getSession();
    Node post = session.getRootNode().getNode(path);
    post.setProperty("exo:link", "http://test?objectId=post001");
    post.setProperty("exo:isFirstPost", true);
    // post.setProperty("exo:isApproved", true);
    post.save();

    generator.itemAdded("/" + path);

  }

}
