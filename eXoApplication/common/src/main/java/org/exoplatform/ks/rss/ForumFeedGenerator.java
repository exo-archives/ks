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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.exoplatform.ks.common.jcr.JCRTask;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.ks.common.jcr.VoidReturn;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public final class ForumFeedGenerator extends RSSProcess implements FeedContentProvider, FeedListener {

  private static final Log LOG = ExoLogger.getLogger(ForumFeedGenerator.class);
  
  
  public static final String FORUM_RSS_TYPE = "exo:forumRSS".intern();
  public static final String KS_FORUM = "forum".intern();
  public static final String FORUM_APP = "ForumService".intern();
  

  public ForumFeedGenerator(KSDataLocation locator) {
    super(locator);
  }

  public ForumFeedGenerator() {
    super();
  }

  public void itemAdded(String path) {
    try {
      ItemSavedTask task = new ItemSavedTask(path,false);
      dataLocator.getSessionManager().executeAndSave(task);
    } catch (Exception e) {
      LOG.error("itemAdded failed for " + path, e);
    }
  }
  public void itemUpdated(String path) {
    try {
      ItemSavedTask task = new ItemSavedTask(path,true);
      dataLocator.getSessionManager().executeAndSave(task);
    } catch (Exception e) {
      LOG.error("itemUpdated failed for" + path, e);
    }
  }
  
  public void itemRemoved(String path)  {
    try {
      ItemRemovedTask task = new ItemRemovedTask(path);
      dataLocator.getSessionManager().executeAndSave(task);
    } catch (Exception e) {
      LOG.error("itemRemoved failed for " + path, e);
    }
  }

  
  class ItemSavedTask implements JCRTask<VoidReturn> {
    private String path;
    private boolean updated;
    
   public ItemSavedTask(String path, boolean updated) {
      this.path = path;
      this.updated = updated;
   }

   public VoidReturn execute(Session session) throws Exception {
     Node node;
     node = (Node)getCurrentSession().getItem(path);
     String linkItem = getPageLink() + "?portal:componentId=forum&portal:type=action&portal:isSecure=false&uicomponent=UIBreadcumbs&"
       + "op=ChangePath&objectId=";
     if(node.isNodeType("exo:post")){
       linkItem = node.getProperty("exo:link").getString();
       linkItem = linkItem.substring(0, linkItem.indexOf("objectId=")+9);
       postUpdated(path, linkItem, updated);
     } else if (node.isNodeType("exo:topic")) {
       topicUpdated(path, linkItem, updated);
     } else if (node.isNodeType("exo:forum")) {
       forumUpdated(path, linkItem, updated);
     }
     return VoidReturn.VALUE;
   }
    
  }
  
  class ItemRemovedTask implements JCRTask<VoidReturn> {
    private String path;

    
   public ItemRemovedTask(String path) {
      this.path = path;
   }

   public VoidReturn execute(Session session) throws Exception {
     String objectId = null;
     String description = null;
     objectId = path.substring(path.lastIndexOf("/") + 1);
     Node node = null;

     if(objectId.contains("post")){
        while(node == null){
           try{
             node = (Node)getCurrentSession().getItem(path);
           }catch(PathNotFoundException pn){
             path = path.substring(0, path.lastIndexOf("/"));
             node = null;
           }
         }
        
        while(node.isNodeType("exo:forumCategory") || node.isNodeType("exo:forum") || node.isNodeType("exo:topic")){
             description = new PropertyReader(node).string("exo:description", " ");
             removeItemInFeed(objectId, node, description);
             node = node.getParent();
         }
     } else {
       path = path.substring(0, path.lastIndexOf("/"));
       while(node == null){
         try{
           node = (Node)getCurrentSession().getItem(path);
         }catch(PathNotFoundException pn){
           objectId = path.substring(path.lastIndexOf("/") + 1);
           path = path.substring(0, path.lastIndexOf("/"));
           node = null;
         }
       }
       while(node.isNodeType("exo:forumCategory") || node.isNodeType("exo:forum") || node.isNodeType("exo:topic")){
           description = new PropertyReader(node).string("exo:description", " ");
         if(node.isNodeType("exo:forum") || node.isNodeType("exo:forumCategory")) {
           removeRSSItem(objectId, node, description);
         } else {
           removeItemInFeed(objectId, node, description);

           }
         node = node.getParent();
       }
     }
   
     return VoidReturn.VALUE;
   }

  }
  

  /**
   * Remove one item from RSS feed based on id of object which is changed 
   * @param itemId        id of object
   * @param node            Node content RSS feed
   * @param feedDescription description about RSS feed
   * @throws Exception
   */
  protected void removeItemInFeed(String itemId, Node node, String feedDescription) throws Exception{
    RSS data = new RSS(node);
    SyndFeed feed = data.removeEntry(itemId);    
    String title = new PropertyReader(node).string("exo:name", "Root");
    feed.setTitle(title);
    feed.setDescription(feedDescription);
    data.saveFeed(feed, FORUM_RSS_TYPE);
  }
  
  

  @SuppressWarnings("unchecked")
  protected void removeRSSItem(String itemId, Node node, String description) throws Exception {
    RSS data = new RSS(node);
    SyndFeed feed = data.read();
    
    List<SyndEntry> entries = feed.getEntries();
    Node removeNode = getNodeById(itemId); 

    if (removeNode.isNodeType("exo:topic")){
      List<Node> listRemovePosts = getListRemove(removeNode,"exo:post");
      removeItem(entries ,listRemovePosts);
    } else if (removeNode.isNodeType("exo:forum")){
      List<Node> listRemoveForum = getListRemove(removeNode,"exo:topic");

      for (Node n : listRemoveForum) {
        List<Node> listRemovePosts = getListRemove(n,"exo:post");
        removeItem(entries ,listRemovePosts);
      }
      removeItem(entries ,listRemoveForum);
    }
    
    feed.setEntries(entries);
    String title = new PropertyReader(node).string("exo:name", "Root");
    feed.setTitle(title);
    feed.setDescription(description);
    data.saveFeed(feed, FORUM_RSS_TYPE);
  }
  
  protected void removeItem(List<SyndEntry> entries,List<Node> listRemove) throws RepositoryException{
    List<SyndEntry> entries1 = new ArrayList<SyndEntry>();

    boolean flag  = true;
    for(SyndEntry syndEntry : entries){
      flag  = true;
        for(Node post : listRemove){
          if(syndEntry.getUri().equals(post.getName())){
            flag = false;
            break;
          }
        }
        if(flag){
          entries1.add(syndEntry);
        }
    }
    entries.clear();
    entries.addAll(entries1);
  }
   
  protected List<Node> getListRemove(Node removeNode, String childNodeType) throws RepositoryException{
    List<Node> listRemove = new ArrayList<Node>();
    NodeIterator nodeIterator = removeNode.getNodes();
    Node nodePost = null;

    while(nodeIterator.hasNext()){
      nodePost = nodeIterator.nextNode();
      if(nodePost.isNodeType(childNodeType)){
        listRemove.add(nodePost);
      }  
    }
 
    return listRemove;
  }
  
  protected void topicUpdated(String path, String linkItem, final boolean updated){
    try {
      Node topicNode = (Node)getCurrentSession().getItem(path);
      Node forumNode = topicNode.getParent();
      Node categoryNode = forumNode.getParent();
      boolean categoryHasRestrictedAudience = (hasProperty(categoryNode, "exo:viewer"));
      boolean forumHasRestrictedAudience = (hasProperty(forumNode, "exo:viewer"));
      boolean debug = LOG.isDebugEnabled();
      String topicName = topicNode.getName() ;
      if (categoryHasRestrictedAudience || forumHasRestrictedAudience) {
        if (debug) {
          LOG.debug("Post" + topicName +" was not added to feed because category or forum has restricted audience");
        }
        return;
      }      
      
      PropertyReader topic = new PropertyReader(topicNode);
      SyndContent description;
      List<String> listContent = new ArrayList<String>();
      String message = topic.string("exo:description");
      listContent.add(message);
      description = new SyndContentImpl();
      description.setType(RSS.PLAIN_TEXT);
      description.setValue(message);
      final String title = topic.string("exo:name");
      final Date created = topic.date("exo:createdDate");
      final String owner = topic.string("exo:owner");
      SyndEntry entry = RSS.createNewEntry(topicName, title, linkItem, listContent, description,created , owner);
      entry.setLink(linkItem + topicNode.getName());
      
      // save topic, forum and category feeds
      for(Node node : new Node[]{forumNode, categoryNode}) {
        PropertyReader reader = new PropertyReader(node);
        String desc = reader.string("exo:description", " ");
        
        RSS data = new RSS(node);
        if (data.feedExists()) {
          SyndFeed feed = null;
          if (updated) {
            feed = data.removeEntry(topicName);
          }
          feed = data.addEntry(entry);
          feed.setDescription(reader.string("exo:description", " "));  
          data.saveFeed(feed, FORUM_RSS_TYPE);
        } else {          
          SyndFeed feed = RSS.createNewFeed(node.getProperty("exo:name").getString(), node.getProperty("exo:createdDate").getDate().getTime());
          feed.setLink(linkItem + node.getName());          
          feed.setEntries(Arrays.asList(new SyndEntry[]{entry}));
          feed.setDescription(desc);  
          data.saveFeed(feed, FORUM_RSS_TYPE);       
        }        
      }
      
      //update posts in topic
      NodeIterator nodeIterator = topicNode.getNodes();
      Node postNode = null;
      while(nodeIterator.hasNext()){
        postNode = nodeIterator.nextNode();
        if(postNode.isNodeType("exo:post")){
          try {
            postUpdated(postNode, linkItem, updated);
          } catch (Exception e) {
            if(LOG.isDebugEnabled()) {
              LOG.debug("Failed to generate feed for post " + postNode.getPath(), e);
            }
          }
        }
      }
    } catch (PathNotFoundException e) {
      LOG.warn("Failed to get properties", e);
    } catch (Exception e) {
      LOG.error("Failed to update topics", e);
    }
  }
  
   protected void forumUpdated(String path, String linkItem, final boolean updated){
      try {
        Node forumNode = (Node)getCurrentSession().getItem(path);
        NodeIterator nodeIterator = forumNode.getNodes();
        Node topicNode = null;
        while(nodeIterator.hasNext()){
          topicNode = nodeIterator.nextNode();
          if(topicNode.isNodeType("exo:topic")){
            topicUpdated(topicNode.getPath(), linkItem, updated);
          }
        }
      } catch (PathNotFoundException e) {
        LOG.warn("Failed to get item with path " + path, e);
      } catch (RepositoryException e) {
        LOG.warn("Failed to get item with path " + path, e);
      }
    }
  
  private void postUpdated(Node postNode, String linkItem, final boolean updated) throws Exception {
    boolean debug = LOG.isDebugEnabled();
    Node topicNode = postNode.getParent();
    String postName = postNode.getName();
    PropertyReader post = new PropertyReader(postNode);
    PropertyReader topic = new PropertyReader(topicNode);
    boolean isFirstPost = post.bool("exo:isFirstPost");//postNode.hasProperty("exo:isFirstPost") && postNode.getProperty("exo:isFirstPost").getBoolean();
    boolean notApproved = !post.bool("exo:isApproved");//(postNode.hasProperty("exo:isApproved") && !postNode.getProperty("exo:isApproved").getBoolean());
    boolean isPrivatePost = hasProperty(topicNode, "exo:userPrivate") && !"exoUserPri".equals(topic.strings("exo:userPrivate")[0]); //(postNode.hasProperty("exo:userPrivate") && !postNode.getProperty("exo:userPrivate").getValues()[0].getString().equals("exoUserPri"));
    boolean topicHasLimitedViewers = hasProperty(topicNode, "exo:canView"); //(topicNode.hasProperty("exo:canView") && topicNode.getProperty("exo:canView").getValues()[0].getString().trim().length() > 0);
    
    if ((isFirstPost && notApproved) || isPrivatePost || topicHasLimitedViewers) {
      if (debug) {
        LOG.debug("Post" + postName +" was not added to feed because it is private or topic has restricted audience or it is approval pending");
      }
      return;
    }
    
    Node forumNode = topicNode.getParent();
    Node categoryNode = forumNode.getParent();
    boolean categoryHasRestrictedAudience = (hasProperty(categoryNode, "exo:viewer"));
    boolean forumHasRestrictedAudience = (hasProperty(forumNode, "exo:viewer"));
    
    if (categoryHasRestrictedAudience || forumHasRestrictedAudience) {
      if (debug) {
        LOG.debug("Post" + postName +" was not added to feed because category or forum has restricted audience");
      }
      return;
    }
    
    boolean inactive = !post.bool("exo:isActiveByTopic");//(postNode.hasProperty("exo:isActiveByTopic") && !postNode.getProperty("exo:isActiveByTopic").getBoolean());
    boolean hidden = post.bool("exo:isHidden");//(postNode.hasProperty("exo:isHidden") && postNode.getProperty("exo:isHidden").getBoolean());
    
    
    if(notApproved || inactive || hidden) {
      
      if (updated) {
        removePostFromParentFeeds(postNode, topicNode, forumNode, categoryNode);
      }
      if (debug) {
        LOG.debug("Post" + postName +" was not added to feed because because it is hidden, inactive or not approved");
      }
      return;
    }
    
    SyndContent description;
    List<String> listContent = new ArrayList<String>();
    String message = post.string("exo:message");
    listContent.add(message);
    description = new SyndContentImpl();
    description.setType(RSS.PLAIN_TEXT);
    description.setValue(message);
    final String title = post.string("exo:name");
    final Date created = post.date("exo:createdDate");
    final String owner = post.string("exo:owner");
    SyndEntry entry = RSS.createNewEntry(postName, title, linkItem, listContent, description,created , owner);
    entry.setLink(linkItem + topicNode.getName());
    
    // save topic, forum and category feeds
    for(Node node : new Node[]{topicNode, forumNode, categoryNode}) {
      PropertyReader reader = new PropertyReader(node);
      String desc = reader.string("exo:description", " ");
      
      RSS data = new RSS(node);
      if (data.feedExists()) {
        SyndFeed feed = null;
        if (updated) {
          feed = data.removeEntry(postName);
        }
        feed = data.addEntry(entry);
        feed.setDescription(reader.string("exo:description", " "));  
        data.saveFeed(feed, FORUM_RSS_TYPE);
      } else {
        SyndFeed feed   = RSS.createNewFeed(title, created);
        feed.setLink(linkItem + node.getName());
        feed.setEntries(Arrays.asList(new SyndEntry[]{entry}));
        feed.setDescription(desc);  
        data.saveFeed(feed, FORUM_RSS_TYPE);
      }
    }
  }

  protected void postUpdated(String path, String linkItem, final boolean updated){
    try{
      Node postNode = (Node)getCurrentSession().getItem(path);
      postUpdated(postNode, linkItem, updated);
    }catch(Exception e){
      LOG.error("Failed to generate feed for post" + path, e);
    }
  }

  private void removePostFromParentFeeds(Node postNode, Node topicNode, Node forumNode, Node categoryNode) throws Exception {
    for(Node node : new Node[]{topicNode, forumNode, categoryNode}){
      String description = new PropertyReader(node).string("exo:description", " ");
      RSS data = new RSS(node);
      SyndFeed feed = data.removeEntry(postNode.getName());    
      String title = new PropertyReader(node).string("exo:name", "Root");
      feed.setTitle(title);
      feed.setDescription(description);
      data.saveFeed(feed, FORUM_RSS_TYPE);
    }
  }
  
  private boolean hasProperty(Node node, String property) throws Exception {
    if(node.hasProperty(property) && node.getProperty(property).getValues().length > 0 && node.getProperty(property).getValues()[0].getString().trim().length() > 0)
      return true;
    else return false;
  }
  
  
  
  

  

  
  public InputStream getFeedContent(String targetId) {
    return dataLocator.getSessionManager().executeAndSave(new GetFeedStreamTask(targetId));
  }
  

  class GetFeedStreamTask implements JCRTask<InputStream> {

    private String targetId;

    public GetFeedStreamTask(String targetId) {
      this.targetId = targetId;
    }

    public InputStream execute(Session session) throws Exception {
      Node parentNode = getNodeById(targetId);
      return getFeedStream(parentNode, FORUM_RSS_TYPE, "FORUM RSS FEED");
    }
    

    
  }
  

}
