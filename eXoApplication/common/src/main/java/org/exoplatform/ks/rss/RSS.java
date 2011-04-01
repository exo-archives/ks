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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.rss;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.w3c.dom.Document;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *          ha.mai@exoplatform.com
 * Jan 12, 2009, 5:55:37 PM
 */
public class RSS {
  private static final Log LOG = ExoLogger.getLogger(RSS.class);
  protected static final String CONTENT_PROPERTY = "exo:content".intern();
  protected static final String RSS_NODE_NAME = "ks.rss".intern();  
  protected static final String RSS_2_0 = "rss_2.0".intern();
  public static final String PLAIN_TEXT = "text/plain".intern();
  public static final String DEFAULT_FEED_LINK = "http://www.exoplatform.com".intern();

  
  private String fileName ;
  private Node itemNode;
  
  public RSS(Node node) {
    itemNode = node;
  }
  
  public static String getRSSLink(String appType, String portalName, String objectId){
    return "/" + PortalContainer.getInstance().getRestContextName() + "/ks/" + appType + "/rss/" + objectId;   
  }
  
  
  public static String getUserRSSLink(String apptype, String userId) {
    return "/" + PortalContainer.getInstance().getRestContextName() +  "/ks/" + apptype + "/rss/user/"+userId;
  }
  
  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }
  
  public InputStream getContent() {
    try {
      return getFeedNode().getProperty(CONTENT_PROPERTY).getValue().getStream();
    } catch (Exception e) {
      throw new RuntimeException("Failed to get feed content ", e);
    }

  }
  
  public void setContent(InputStream is) {
    try {
      getFeedNode().setProperty(CONTENT_PROPERTY, is);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get feed content ", e);
    }

  }

  private Node getFeedNode() throws Exception {
//    System.out.println("\n\n itemNode: " + itemNode.getPath());
    try {
      return itemNode.getNode(RSS_NODE_NAME);
    } catch (Exception e) {
      return itemNode.addNode(RSS_NODE_NAME, "exo:forumRSS");
//      throw new RuntimeException("Failed to get feed node", e);
    }
  }

  /**
   * Read a SyndFeed from the
   * @return
   * @throws Exception
   */
  public SyndFeed read() {
    try {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    InputStream inputStream = getContent();
    Document doc = docBuilder.parse(inputStream);
    doc.getDocumentElement().normalize();

    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed = input.build(doc);
    return feed;
    }
    catch (Exception e) {
      LOG.error("Failed to read RSS feed");
      throw new RuntimeException(e);
    }
  }
  
  /**
   * Remove an item for the feed
   * @param uri
   * @return
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public SyndFeed removeEntry(String uri) {
    SyndFeed feed = read();
    List<SyndEntry> entries = feed.getEntries();
    if(uri != null && uri.trim().length() > 0){
      for(SyndEntry syndEntry : entries){
        if(syndEntry.getUri().equals(uri)){
          entries.remove(syndEntry);
          break;
        }
      }
    }
    feed.setEntries(entries);
    return feed;
  }
  
  /**
   * Adds an entry to the feed
   * @param newEntry
   * @return
   */
  @SuppressWarnings("unchecked")
  public SyndFeed addEntry(SyndEntry newEntry) {
    SyndFeed feed = read();
    List<SyndEntry> entries = feed.getEntries();
    if (newEntry != null)
      entries.add(0, newEntry);
    feed.setEntries(entries);
    return feed;
  }
  

  public void saveFeed(SyndFeed feed, String rssNodeType) {
    try {
      boolean isNew = false;
      try {
        itemNode.getNode(RSS_NODE_NAME);
      } catch (PathNotFoundException pnfe) {
        LOG.debug("Feed node not found for " + itemNode.getName() + " creating...");
        itemNode.addNode(RSS_NODE_NAME, rssNodeType);
        isNew = true;
      }
      
      SyndFeedOutput output = new SyndFeedOutput();
      setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));

      if (isNew)
        itemNode.getSession().save();
      else
        itemNode.save();
    } catch (Exception e) {
      LOG.error("Failed to save feed content", e);
    }
  }



  public boolean feedExists() {
    try {
      return itemNode.hasNode(RSS_NODE_NAME);
    } catch (Exception e) {
      return false;
    }
  }
  
  
  /**
   * Create a new feed with some default content: link is the link to eXo web site
   * and feed type is <code>rss_2.0</code>
   * @return
   */
  public static SyndFeed createNewFeed(String title, Date pubDate){
    SyndFeed feed = new SyndFeedImpl();
    feed.setLink(DEFAULT_FEED_LINK);
    feed.setFeedType(RSS_2_0);
    feed.setTitle(title);
    feed.setPublishedDate(pubDate);
    feed.setDescription(" ");
    return feed;
  }
  
  /**
   * Create new entry
   * @param uri         uri of item
   * @param title       title of item
   * @param link        link to this item 
   * @param listContent the content of item
   * @param description the description for this item
   * @return  SyndEntry
   */
  public static SyndEntry createNewEntry(String uri, String title, String link, List<String> listContent, SyndContent description, Date pubDate, String author){
    SyndEntry entry = new SyndEntryImpl();
    entry.setUri(uri);
    entry.setTitle(title);
    entry.setLink(link + uri);
    entry.setContributors(listContent);
    entry.setDescription(description);
    entry.setPublishedDate(pubDate);
    entry.setAuthor(author);
    return entry;
  }

  public Node getItemNode() {
    return itemNode;
  }

  public void setItemNode(Node itemNode) {
    try {
      Node rssNode = itemNode.getNode(RSS_NODE_NAME);
      if (rssNode.hasProperty(CONTENT_PROPERTY)) {
        setContent(rssNode.getProperty(CONTENT_PROPERTY).getValue().getStream());
      } else {
        throw new IllegalArgumentException("Node does not have an RSS feed child");
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to set item node for RSS", e);
    }
    this.itemNode = itemNode;
  }
  
}
