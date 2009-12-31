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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.ks.common.jcr.JCRTask;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.VoidReturn;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.w3c.dom.Document;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ForumFeedGenerator extends RSSProcess {

  private static final Log log = ExoLogger.getLogger(ForumFeedGenerator.class);
  
  
  public final String FORUM_RSS_TYPE = "exo:forumRSS".intern();
  public final String KS_FORUM = "forum".intern();
  public final String FORUM_APP = "ForumService".intern();
  

  public ForumFeedGenerator(KSDataLocation locator) throws Exception {
    super(locator);
  }

  public ForumFeedGenerator() {
    super();
  }

  protected void generateFeed(String path, int typeEvent, String linkItem) throws Exception { 
    GenerateFeedTask task = new GenerateFeedTask(path, typeEvent, linkItem);
    dataLocator.getSessionManager().executeAndSave(task);
  }
  
 class GenerateFeedTask implements JCRTask<VoidReturn> {
   String path;
   int typeEvent;
   String linkItem;
   
   
  public GenerateFeedTask(String path, int typeEvent, String linkItem) {
     this.path = path;
     this.typeEvent = typeEvent;
     this.linkItem = linkItem;
  }


  public VoidReturn execute(Session session) throws Exception {
    linkItem += "?portal:componentId=forum&portal:type=action&portal:isSecure=false&uicomponent=UIBreadcumbs&"
      + "op=ChangePath&objectId=";

  if (typeEvent == Event.NODE_REMOVED) {
    itemRemoved(path, linkItem);
  } else if (typeEvent == Event.NODE_ADDED) {
    itemAdded(path, linkItem);
  } else if (typeEvent == Event.PROPERTY_CHANGED) {
    itemUpdated(path, linkItem);
  }
    return VoidReturn.VALUE;
  }
   
 }

  private Node getForumServiceHome() throws Exception {
    String path = dataLocator.getForumHomeLocation();
    return dataLocator.getSessionManager().getCurrentSession().getRootNode().getNode(path);
  }
  
  private void itemAdded(String path, String linkItem) throws Exception {
    itemSaved(path, linkItem, false);
  }
  private void itemUpdated(String path, String linkItem) throws Exception {
    itemSaved(path, linkItem, true);
  }

  private void itemSaved(String path, String linkItem, final boolean updated) throws Exception {
    Node node;
    node = (Node)getCurrentSession().getItem(path);

    if(node.isNodeType("exo:post")){
      linkItem = node.getProperty("exo:link").getString();
      linkItem = linkItem.substring(0, linkItem.indexOf("objectId=")+9);
      updatePostFeed(path, linkItem, updated);
    } else if (node.isNodeType("exo:topic")) {
      updateTopicFeed(path, linkItem, updated);
    } else if (node.isNodeType("exo:forum")) {
      updateForumFeed(path, linkItem, updated);
    }
  }
  


  public void itemRemoved(String path, String linkItem) throws Exception {
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
          if(node.hasProperty("exo:description"))
            description = node.getProperty("exo:description").getString();
          else
            description= " ";
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
        if(node.hasProperty("exo:description"))
          description = node.getProperty("exo:description").getString();
        else
          description= " ";
        if(node.isNodeType("exo:forum") || node.isNodeType("exo:forumCategory")){
          removeRSSItem(objectId, node, description);
        } else {
          removeItemInFeed(objectId, node, description);
        }
        node = node.getParent();
      }
    }
  }
  
  
  private void removeRSSItem(String objectid, Node itemNode, String feedDescription) throws Exception{
     RSS data = new RSS();
     Node feedNode = null;
     try{
       feedNode = itemNode.getNode(KS_RSS);
     }catch(PathNotFoundException pn){
       return;
     }
     getRSSData(feedNode, data);
     
     SyndFeed feed = updateRSSFeed(data, objectid);
     try{
       feed.setTitle(itemNode.getProperty("exo:name").getString());
     }catch(PathNotFoundException pn){
       feed.setTitle("Root");
     }
     feed.setDescription(feedDescription);
     
     SyndFeedOutput output = new SyndFeedOutput();
     data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
     saveRssContent(itemNode, feedNode, data, false);
   }
  
  @SuppressWarnings("unchecked")
  protected SyndFeed updateRSSFeed(RSS data, String removeItemId) throws Exception {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    Document doc = docBuilder.parse(data.getContent());
    doc.getDocumentElement().normalize();
    
    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed = input.build(doc);
    List<SyndEntry> entries = feed.getEntries();
    List<Node> listRemovePosts = new ArrayList<Node>();
    Node removeNode = getNodeById(removeItemId); 

    if (removeNode.isNodeType("exo:topic")){
      listRemovePosts = getListRemove(removeNode,"exo:post");
      removeItem(entries ,listRemovePosts);
    } else if (removeNode.isNodeType("exo:forum")){
      List<Node> listRemoveForum = new ArrayList<Node>();
      listRemoveForum = getListRemove(removeNode,"exo:topic");

      for (Node node : listRemoveForum) {
        listRemovePosts = getListRemove(node,"exo:post");
        removeItem(entries ,listRemovePosts);
      }
      removeItem(entries ,listRemoveForum);
    }

    feed.setEntries(entries);
    return feed;
  }
  
  protected void updateTopicFeed(String path, String linkItem, final boolean updated){
    try {
      Node topicNode = (Node)getCurrentSession().getItem(path);
      NodeIterator nodeIterator = topicNode.getNodes();
      Node postNode = null;
      while(nodeIterator.hasNext()){
        postNode = nodeIterator.nextNode();
        if(postNode.isNodeType("exo:post")){
          updatePostFeed(postNode.getPath(), linkItem, updated);
        }
      }
    } catch (PathNotFoundException e) {
    } catch (RepositoryException e) {
    }
  }
   protected void updateForumFeed(String path, String linkItem, final boolean updated){
      try {
        Node forumNode = (Node)getCurrentSession().getItem(path);
        NodeIterator nodeIterator = forumNode.getNodes();
        Node topicNode = null;
        while(nodeIterator.hasNext()){
          topicNode = nodeIterator.nextNode();
          if(topicNode.isNodeType("exo:topic")){
            updateTopicFeed(topicNode.getPath(), linkItem, updated);
          }
        }
      } catch (PathNotFoundException e) {
      } catch (RepositoryException e) {
      }
    }
  
  
  protected void updatePostFeed(String path, String linkItem, final boolean updated){
    boolean isNew = false;
    try{
      boolean debug = log.isDebugEnabled();
      Node postNode = (Node)getCurrentSession().getItem(path);
      Node topicNode = postNode.getParent();
      String postName = postNode.getName();
      
      boolean isFirstPost = postNode.hasProperty("exo:isFirstPost") && postNode.getProperty("exo:isFirstPost").getBoolean();
      boolean notApproved = (postNode.hasProperty("exo:isApproved") && !postNode.getProperty("exo:isApproved").getBoolean());
      boolean isPrivatePost = (postNode.hasProperty("exo:userPrivate") && !postNode.getProperty("exo:userPrivate").getValues()[0].getString().equals("exoUserPri"));
      boolean topicHasLimitedViewers = (topicNode.hasProperty("exo:canView") && topicNode.getProperty("exo:canView").getValues()[0].getString().trim().length() > 0);
      
      if ((isFirstPost && notApproved) || isPrivatePost || topicHasLimitedViewers) {
        if (debug) {
          log.debug("Post" + postName +" was not added to feed because it is private or topic has restricted audience or it is approval pending");
        }
        return;
      }
      
      Node forumNode = topicNode.getParent();
      Node categoryNode = forumNode.getParent();
      boolean categoryHasRestrictedAudience = (hasProperty(categoryNode, "exo:viewer"));
      boolean forumHasRestrictedAudience = (hasProperty(forumNode, "exo:viewer"));
      
      if (categoryHasRestrictedAudience || forumHasRestrictedAudience) {
        if (debug) {
          log.debug("Post" + postName +" was not added to feed because category or forum has restricted audience");
        }
        return;
      }
        
      
      boolean inactive = (postNode.hasProperty("exo:isActiveByTopic") && !postNode.getProperty("exo:isActiveByTopic").getBoolean());
      boolean hidden = (postNode.hasProperty("exo:isHidden") && postNode.getProperty("exo:isHidden").getBoolean());
      
      
      if(notApproved || inactive || hidden) {
        
        if (updated) {
          removePostFromParentFeeds(postNode, topicNode, forumNode, categoryNode);
        }
        if (debug) {
          log.debug("Post" + postName +" was not added to feed because because it is hidden, inactive or not approved");
        }
        return;
      }
      
      SyndContent description;
      List<String> listContent = new ArrayList<String>();
      listContent.add(postNode.getProperty("exo:message").getString());
      description = new SyndContentImpl();
      description.setType(descriptionType);
      description.setValue(postNode.getProperty("exo:message").getString());
      SyndEntry entry = createNewEntry(postName, postNode.getProperty("exo:name").getString(), 
                              linkItem, listContent, description, postNode.getProperty("exo:createdDate").getDate().getTime(), 
                              postNode.getProperty("exo:owner").getString());
      entry.setLink(linkItem + topicNode.getName());
      Node RSSNode = null;
      SyndFeed feed = null;
      for(Node node : new Node[]{topicNode, forumNode, categoryNode}){
        isNew = false;
        RSS data = new RSS();
        try{
          RSSNode = node.getNode(KS_RSS);
          getRSSData(RSSNode, data);
          feed = updateRSSFeed(data, postNode.getName(), entry);
        } catch (PathNotFoundException e){
          RSSNode = node.addNode(KS_RSS, FORUM_RSS_TYPE);
          isNew = true;
          feed = this.createNewFeed(node.getProperty("exo:name").getString(), node.getProperty("exo:createdDate").getDate().getTime());
          feed.setLink(linkItem + node.getName());
          feed.setEntries(Arrays.asList(new SyndEntry[]{entry}));
        } catch (Exception e) {
          log.error("Error while generating feed for " + node.getName(), e);
          continue;
        }
        
        if(node.hasProperty("exo:description"))
          feed.setDescription(node.getProperty("exo:description").getString());
        else
          feed.setDescription(" ");
  
        SyndFeedOutput output = new SyndFeedOutput();
        data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
        saveRssContent(node, RSSNode, data, isNew);
      }
    }catch(Exception e){
      log.error("Failed to generate feed for post" + path, e);
    }
  }

  private void removePostFromParentFeeds(Node postNode, Node topicNode, Node forumNode, Node categoryNode) throws Exception {
    final String postName = postNode.getName();
    if(topicNode.hasProperty("exo:description"))
      removeItemInFeed(postName, topicNode, topicNode.getProperty("exo:description").getString());
    else
      removeItemInFeed(postName, topicNode, " ");
    
    if(forumNode.hasProperty("exo:description"))
      removeItemInFeed(postName, forumNode, forumNode.getProperty("exo:description").getString());
    else
      removeItemInFeed(postName, forumNode, " ");
    
    if(categoryNode.hasProperty("exo:description"))
      removeItemInFeed(postName, categoryNode, categoryNode.getProperty("exo:description").getString());
    else
      removeItemInFeed(postName, categoryNode, " ");
  }
  
  private boolean hasProperty(Node node, String property) throws Exception {
    if(node.hasProperty(property) && node.getProperty(property).getValues().length > 0 && node.getProperty(property).getValues()[0].getString().trim().length() > 0)
      return true;
    else return false;
  }
  
  public InputStream getUserFeedContent(String userId) throws Exception {
    return dataLocator.getSessionManager().executeAndSave(new GetUserFeedStreamTask(userId));
  }
  
  
  class GetUserFeedStreamTask implements JCRTask<InputStream> {

    private String userId;

    public GetUserFeedStreamTask(String userId) {
      this.userId = userId;
    }

    public InputStream execute(Session session) throws Exception {
      if(userId == null || userId.trim().length() == 0) return null;
      InputStream inputStream = null;
      Map<String, SyndEntry> mapEntries = new HashMap<String, SyndEntry>();
      Node RSSNode = null;
      SyndEntry syndEntry = null;
      for(String objectId : getForumSubscription(userId)){
        try{
          RSSNode = getNodeById(objectId).getNode(KS_RSS);
          RSS data = new RSS();
          getRSSData(RSSNode, data);
          for(Object entry : updateRSSFeed(data, null, null).getEntries()){
            syndEntry = (SyndEntry)entry;
            mapEntries.put(syndEntry.getUri(), syndEntry);
          }
        } catch (Exception e){}
      }
      SyndFeed feed = createNewFeed("Forum subscriptions for " + userId, new Date());
      feed.setDescription(" ");
      feed.setEntries(Arrays.asList(mapEntries.values().toArray(new SyndEntry[]{})));
      SyndFeedOutput output = new SyndFeedOutput();
      inputStream = new ByteArrayInputStream(output.outputString(feed).getBytes());
      return inputStream;
    }
    
  }
  
  
  /**
   * Get one node in FORUM application by id
   * @param objectId  id of node which is got
   * @param sProvider the session provider
   * @return          node
   * @throws Exception
   */
  protected Node getNodeById(String objectId) throws Exception{
    Node parentNode = getForumServiceHome();
    QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + parentNode.getPath() 
        + "//*[@exo:id='").append(objectId).append("']") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    parentNode = result.getNodes().nextNode() ;
    return parentNode;
  }
  
  List<String> getForumSubscription(String userId) throws Exception {
    List<String> list = new ArrayList<String>();
    String subscriptionsPath = "ForumSystem/UserProfileHome/" + userId + "/forumSubscription" + userId;
    Node subscriptionNode = getForumServiceHome().getNode(subscriptionsPath);
    list.addAll(ValuesToList(subscriptionNode.getProperty("exo:categoryIds").getValues()));
    list.addAll(ValuesToList(subscriptionNode.getProperty("exo:forumIds").getValues()));
    list.addAll(ValuesToList(subscriptionNode.getProperty("exo:topicIds").getValues()));
    return list;
  }
  
  private List<String> ValuesToList(Value[] values) throws Exception {
    List<String> list = new ArrayList<String>();
    for (int i = 0; i < values.length; ++i) {
      list.add(values[i].getString());
    }
    return list;
  }
  
  public InputStream getFeedContent(String targetId) throws Exception {
    return dataLocator.getSessionManager().executeAndSave(new GetFeedStreamTask(targetId));
  }
  

  
  class GetFeedStreamTask implements JCRTask<InputStream> {

    private String targetId;

    public GetFeedStreamTask(String targetId) {
      this.targetId = targetId;
    }

    public InputStream execute(Session session) throws Exception {
      Node parentNode = getForumServiceHome();
      QueryManager qm = session.getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root").append(parentNode.getPath())
                                                              .append("//*[@exo:id='")
                                                              .append(targetId)
                                                              .append("']");
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      parentNode = result.getNodes().nextNode();
      return getFeedStream(parentNode, FORUM_RSS_TYPE, "FORUM RSS FEED");
    }
    
  }
  

}
