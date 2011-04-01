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
public final class AnswersFeedGenerator extends RSSProcess implements FeedContentProvider, FeedListener {

  private static final Log   LOG          = ExoLogger.getLogger(AnswersFeedGenerator.class);

  public static final String FAQ_RSS_TYPE = "exo:faqRSS".intern();

  public static final String KS_FAQ       = "faq".intern();

  public static final String FAQ_APP      = "faqApp".intern();
  
  public AnswersFeedGenerator() {
    super();
  }
  
  public AnswersFeedGenerator(KSDataLocation dataLocator) {
    super(dataLocator);
  }

  
  public void itemAdded(String path) {
    try {
      ItemSavedTask task = new ItemSavedTask(path,false);
      dataLocator.getSessionManager().executeAndSave(task);
    } catch (Exception e) {
      LOG.error("itemAdded failed for" + path, e);
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
  
  class ItemSavedTask implements JCRTask<VoidReturn> {
    private String path;
    private boolean updated;
    
   public ItemSavedTask(String path, boolean updated) {
      this.path = path;
      this.updated = updated;
   }

   public VoidReturn execute(Session session) throws Exception {
     itemSaved(path,updated);
     return VoidReturn.VALUE;
   }
    
  }
  
  protected void itemSaved(String path, boolean updated) throws Exception {
    // find the node corresponding to this event
    Node questionNode = findQuestionNode(path);
    if (questionNode == null) {
      LOG.debug("generate Feed event was ignored on " + path);
      return;
    }

    final String questionNodeName = questionNode.getName();
    PropertyReader question = new PropertyReader(questionNode);
        
    Node categoryNode = questionNode.getParent().getParent();
    PropertyReader category = new PropertyReader(categoryNode);
    String categoryDescription = category.string("exo:description", "eXo link:" + RSS.DEFAULT_FEED_LINK);
    String categoryTitle = category.string("exo:name", "Home");

    // only approved or activated questions
    if ((!question.bool("exo:isActivated") || !question.bool("exo:isApproved"))) {
      RSS rss = new RSS(categoryNode);
      SyndFeed feed = rss.removeEntry(questionNodeName);
      feed.setTitle(categoryTitle);
      feed.setDescription(categoryDescription);
      rss.saveFeed(feed, FAQ_RSS_TYPE);
      return;
    }
    
    SyndEntry entry = createQuestionEntry(questionNode);
    RSS data = new RSS(categoryNode);
    if (data.feedExists()) {
      SyndFeed feed = null;

      if (updated) {
        feed = data.removeEntry(questionNodeName);
      }
      feed = data.addEntry(entry);
      feed.setDescription(categoryDescription);
      feed.setTitle(categoryTitle);
      data.saveFeed(feed, FAQ_RSS_TYPE);
    } else {
      SyndFeed feed = RSS.createNewFeed(categoryTitle, category.date("exo:createdDate", new Date()));
      feed.setDescription(categoryDescription);
      String categoryLink = entry.getLink()
      + "?portal:componentId=faq&portal:type=action&portal:isSecure=false&uicomponent=UICategories&op=OpenCategory&"
      + "objectId=" + question.string("exo:categoryId");
      feed.setLink(categoryLink);
      feed.setEntries(Arrays.asList(new SyndEntry[] { entry }));
      data.saveFeed(feed, FAQ_RSS_TYPE);     
    }

  }

  private SyndEntry createQuestionEntry(Node questionNode) throws Exception  {
    String linkItem = getPageLink();
    linkItem += "?portal:componentId=faq&portal:type=action&portal:isSecure=false&uicomponent=UIQuestions&op=ViewQuestion&objectId=";
    linkItem += questionNode.getPath().substring(questionNode.getPath().indexOf("/categories/") + 1);   

    // Create new entry
    List<String> listContent = new ArrayList<String>();
    StringBuffer content = new StringBuffer();
    if (questionNode.hasNode("faqAnswerHome") && questionNode.getNode("faqAnswerHome").hasNodes()) {
      for (String answer : getAnswers(questionNode))
        content.append(" <b><u>Answer:</u></b> ").append(answer).append(". ");
    }
    if (questionNode.hasNode("faqCommentHome") && questionNode.getNode("faqCommentHome").hasNodes()) {
      for (String comment : getComments(questionNode))
        content.append(" <b><u>Comment:</u></b> ").append(comment).append(". ");
    }
    listContent.add(content.toString());
    
    
    SyndContent description = new SyndContentImpl();
    description.setType(RSS.PLAIN_TEXT);

    final String questionNodeName = questionNode.getName();
    PropertyReader question = new PropertyReader(questionNode);
    description.setValue(question.string("exo:name") + ". " + content); 
    final String title = question.string("exo:title");
    final Date created = question.date("exo:createdDate");
    final String owner = question.string("exo:author");
    
    SyndEntry entry = RSS.createNewEntry(questionNodeName, title, linkItem,  listContent, description, created, owner);
    entry.setLink(linkItem);
    return entry;
  }
  

  
  
  
  private Node findQuestionNode(String nodePath) {
    try {
    Node questionNode = (Node) getCurrentSession().getItem(nodePath);
    if (!questionNode.isNodeType("exo:faqQuestion") && !questionNode.isNodeType("exo:answer")
        && !questionNode.isNodeType("exo:answerHome") && !questionNode.isNodeType("exo:comment")
        && !questionNode.isNodeType("exo:commentHome"))
      return null;
    else if (questionNode.isNodeType("exo:answer") || questionNode.isNodeType("exo:comment")) {
      questionNode = questionNode.getParent().getParent();
      if (!questionNode.isNodeType("exo:faqQuestion"))
        return null; // Worked on other languages
    } else if (questionNode.isNodeType("exo:answerHome")
        || questionNode.isNodeType("exo:commentHome")) {
      questionNode = questionNode.getParent();
      if (!questionNode.isNodeType("exo:faqQuestion"))
        return null; // Worked on other languages
    }
    
    if (!questionNode.isNodeType("exo:faqQuestion")) {
      return null;
    }
    return questionNode;
    } catch(Exception e) {
      LOG.error("Failed to identify question Node for path" + nodePath, e);
      return null;
    }

  }
  
  List<String> getAnswers(Node questionNode) throws Exception{
    List<String> listAnswers = new ArrayList<String>();
    try{
      if(questionNode.hasNode("faqAnswerHome")){
        NodeIterator nodeIterator = questionNode.getNode("faqAnswerHome").getNodes();
        Node answerNode = null;
        int i = 0;
        while(nodeIterator.hasNext()){
          answerNode = nodeIterator.nextNode();
          if(answerNode.hasProperty("exo:responses") && answerNode.getProperty("exo:approveResponses").getBoolean() &&
              answerNode.getProperty("exo:activateResponses").getBoolean()) 
            listAnswers.add((answerNode.getProperty("exo:responses").getValue().getString())) ;
          i ++;
        }
      }
    } catch (Exception e){
      LOG.error("Failed to get answers for " + questionNode.getName(), e);
    }
    return listAnswers;
  }

  List<String> getComments(Node questionNode) throws Exception{
    List<String> listComment = new ArrayList<String>();
    try{
      if(questionNode.hasNode("faqCommentHome")){
        NodeIterator nodeIterator = questionNode.getNode("faqCommentHome").getNodes();
        Node commentNode = null;
        while(nodeIterator.hasNext()){
          commentNode = nodeIterator.nextNode();
          if(commentNode.hasProperty("exo:comments")) 
            listComment.add((commentNode.getProperty("exo:comments").getValue().getString())) ;
        }
      }
    } catch (Exception e){
      LOG.error("Failed to get comments for " + questionNode.getName(), e);
    }
    return listComment;
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
      Node parentNode = getAnswersServiceHome().getNode(targetId) ;
      return getFeedStream(parentNode, FAQ_RSS_TYPE, "ANSWERS RSS FEED");
    }
    
  }
  

  private Node getAnswersServiceHome() throws Exception {
    String path = dataLocator.getFaqHomeLocation();
    return dataLocator.getSessionManager().getCurrentSession().getRootNode().getNode(path);
  }

  public void itemRemoved(String path)  {
    dataLocator.getSessionManager().executeAndSave(new ItemRemovedTask(path));
  }

  class ItemRemovedTask implements JCRTask<VoidReturn> {

    private String path;

    public ItemRemovedTask(String path) {
      this.path = path;
    }

    public VoidReturn execute(Session session) throws Exception {
      try {
        String categoryPath = path.substring(0, path.indexOf("/questions/"));
        Node categoryNode = (Node) session.getItem(categoryPath);
        if(categoryNode == null ) categoryNode = (Node) getCurrentSession().getItem(categoryPath);
        while (!categoryNode.isNodeType("exo:faqCategory")) {
          categoryNode = categoryNode.getParent();
        }
        String itemId = path.substring(path.lastIndexOf("/") + 1);
        RSS rss = new RSS(categoryNode);
        SyndFeed feed = rss.removeEntry(itemId);
        String title = new PropertyReader(categoryNode).string("exo:name", "Root");
        feed.setTitle(title);
        rss.saveFeed(feed, FAQ_RSS_TYPE);
      } catch (Exception e) {
        LOG.debug("Failed to get RSS.", e);
      }
      return VoidReturn.VALUE;
    }
    
  }
  

}
