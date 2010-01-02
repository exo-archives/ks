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
package org.exoplatform.ks.rss;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import javax.jcr.Node;

import org.exoplatform.ks.test.ConfigurationUnit;
import org.exoplatform.ks.test.ConfiguredBy;
import org.exoplatform.ks.test.ContainerScope;
import org.exoplatform.ks.test.jcr.AbstractJCRTestCase;

import com.sun.syndication.feed.synd.SyndFeed;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml")})
public class TestRSS extends AbstractJCRTestCase {

  public void testSaveFeed() throws Exception {
    /*
    Node target = addNode("FeedTarget");
    RSS rss = new RSS(target);
    SyndFeed feed = RSS.createNewFeed("foo", new Date());
    rss.saveFeed(feed, "nt:unstructured");
    
    String feedPath = "FeedTarget/" + RSS.RSS_NODE_NAME;
    assertNodeExists(feedPath);
    
    Node feedNode = getNode(feedPath);
    assertPropertyNotEmpty(feedNode, RSS.CONTENT_PROPERTY);
    */

  }


  protected void assertPropertyNotEmpty(Node node, String property) {
    assertPropertyExists(node, property);

    try {
      InputStream is = node.getProperty(property).getStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      StringBuffer buff = new StringBuffer();
      String str;
      while ((str = reader.readLine()) != null) {
        buff.append(str);
      }
      reader.close();
      assertTrue("property " + property + " was empty", buff.length() > 0);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
   

}
