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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.ks.test.ConfigurationUnit;
import org.exoplatform.ks.test.ConfiguredBy;
import org.exoplatform.ks.test.ContainerScope;
import org.exoplatform.ks.test.jcr.AbstractJCRTestCase;
import org.testng.annotations.Test;

import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy( {
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/portal/rss-configuration.xml"),
  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/storage-configuration.xml") })
public class TestRSS extends AbstractJCRTestCase {

  @Test
  public void testSaveFeed() throws Exception {

    Node target = addNode("FeedTarget");
    RSS rss = new RSS(target);
    SyndFeed feed = RSS.createNewFeed("foo", new Date());
    rss.saveFeed(feed, "nt:unstructured");
    
    String feedPath = "FeedTarget/" + RSS.RSS_NODE_NAME;
    assertNodeExists(feedPath);
    
    Node feedNode = getNode(feedPath);
    assertBinaryPropertyNotEmpty(feedNode, RSS.CONTENT_PROPERTY);
    
    String prop = feedNode.getProperty(RSS.CONTENT_PROPERTY).getString();
    assertTrue(prop.contains("foo"));
    
    feed.setTitle("bar");
    String prop2 = feedNode.getProperty(RSS.CONTENT_PROPERTY).getString();
    rss.saveFeed(feed, "nt:unstructured");
    assertTrue(prop2.contains("foo"));
    
  }

  @Test
  public void testFeedExists() {
    Node target = addNode("FeedTarget2");
    RSS rss = new RSS(target);
    assertFalse(rss.feedExists());
    
    SyndFeed feed = RSS.createNewFeed("foo", new Date());
    rss.saveFeed(feed, "nt:unstructured");
    assertTrue(rss.feedExists()); 
  }
  
  @Test
  public void testAddEntry() throws Exception {
    Node target = addNode("FeedTarget3");
    RSS rss = new RSS(target);
    SyndFeed feed = RSS.createNewFeed("feed3", new Date());
    rss.saveFeed(feed, "nt:unstructured");
    SyndContentImpl desc = new SyndContentImpl();
    desc.setType(RSS.PLAIN_TEXT);
    desc.setValue("value");
    SyndEntry entry = RSS.createNewEntry("uri", "foo", "http://link", Arrays.asList("c1","c2"), desc, new Date(), "author");
    SyndFeed d = rss.addEntry(entry);
    
  
    assertFeedEntry(d, "uri");  
    rss.saveFeed(d, "nt:unstructured");
    
    
    String feedPath = "FeedTarget3/" + RSS.RSS_NODE_NAME;
    Node feedNode = getNode(feedPath);
    assertFeedContains(feedNode, "foo");
    

  }
  
  @Test
  public void testRemoveEntry() throws Exception {
    Node target = addNode("FeedTarget4");
    RSS rss = new RSS(target);
    SyndFeed feed = RSS.createNewFeed("feed4", new Date());

    SyndContentImpl desc = new SyndContentImpl();
    desc.setType(RSS.PLAIN_TEXT);
    desc.setValue("value");
    SyndEntry entry1 = RSS.createNewEntry("uri", "foo", "http://link", Arrays.asList("c1","c2"), desc, new Date(), "author");
    SyndEntry entry2 = RSS.createNewEntry("uri2", "bar", "http://link2", Arrays.asList("c1","c2"), desc, new Date(), "author");
    feed.setEntries(Arrays.asList(entry1, entry2));
    rss.saveFeed(feed, "nt:unstructured");
    Node feedNode = getNode("FeedTarget4/" + RSS.RSS_NODE_NAME);
    assertFeedContains(feedNode, "uri");
    
    SyndFeed d = rss.removeEntry("uri");
    assertNotFeedEntry(d, "uri");
    assertFeedEntry(d, "uri2");
   
    rss.saveFeed(d, "nt:unstructured");
    feedNode = getNode("FeedTarget4/" + RSS.RSS_NODE_NAME);
    assertFeedNotContains(feedNode, "foo");
    assertFeedContains(feedNode, "bar");
  }


  private void assertFeedContains(Node feedNode, String value) throws Exception {
    String feedContent = feedNode.getProperty(RSS.CONTENT_PROPERTY).getString();
    assertTrue("feed content should contain " + value, feedContent.contains(value));
  }
  

  private void assertFeedNotContains(Node feedNode, String value) throws Exception {
    String feedContent = feedNode.getProperty(RSS.CONTENT_PROPERTY).getString();
    assertFalse("feed content should not contain " + value, feedContent.contains(value));
  }

  
  private void assertNotFeedEntry(SyndFeed feed, String uri) {
    List<SyndEntry> entries = feed.getEntries();
    SyndEntry found = null;
    for (SyndEntry sentry : entries) {
      if (sentry.getUri().equals(uri)) {
        found = sentry;
      }
    }
    assertNull("SyndEntry found", found);
  }
  
  private void assertFeedEntry(SyndFeed feed, String uri) {
    List<SyndEntry> entries = feed.getEntries();
    SyndEntry found = null;
    for (SyndEntry sentry : entries) {
      if (sentry.getUri().equals(uri)) {
        found = sentry;
      }
    }
    assertNotNull("SyndEntry not found", found);
  }

}
