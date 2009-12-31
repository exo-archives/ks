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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.observation.Event;

import org.exoplatform.ks.common.jcr.JCRTask;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.VoidReturn;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class AnswersFeedGenerator extends RSSProcess {

  public final String FAQ_RSS_TYPE = "exo:faqRSS".intern();
  public final String KS_FAQ = "faq".intern();
   public final String FAQ_APP = "faqApp".intern();
  
  public AnswersFeedGenerator(KSDataLocation dataLocator) throws Exception {
    super(dataLocator);
  }

  public AnswersFeedGenerator() {
    super();
  }

  /**
   * Create RSS file for FAQ. Only use this function when use sure that <code>path</code> is FAQ
   * @param path      the path of node is changed
   * @param typeEvent the type of event
   */
  protected void generateFeed(String path, int typeEvent, String linkItem){
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
     
     boolean isNew = false;
     RSS data = new RSS();
     //System.out.println("generateFAQRSS=====typeEvent====>" + typeEvent);
     //System.out.println("generateFAQRSS=====path====>" + path);
     try{


       Node categoryNode = null;
       if(typeEvent != Event.NODE_REMOVED) { // Added node or edited properties
         SyndEntry entry;
         SyndContent description;
         Node RSSNode = null;
         String categoryLink = linkItem + "?portal:componentId=faq&portal:type=action&portal:isSecure=false&uicomponent=UICategories&op=OpenCategory&" +
                                           "objectId=";
         linkItem += "?portal:componentId=faq&portal:type=action&portal:isSecure=false&uicomponent=UIQuestions&op=ViewQuestion&" +
                       "objectId=";
         Node questionNode = (Node)getCurrentSession().getItem(path) ;
         if(!questionNode.isNodeType("exo:faqQuestion") && !questionNode.isNodeType("exo:answer") && !questionNode.isNodeType("exo:answerHome") 
             && !questionNode.isNodeType("exo:comment") && !questionNode.isNodeType("exo:commentHome")) return VoidReturn.VALUE;
         else if(questionNode.isNodeType("exo:answer") || questionNode.isNodeType("exo:comment")) {
           questionNode = questionNode.getParent().getParent();
           if(!questionNode.isNodeType("exo:faqQuestion")) return VoidReturn.VALUE; // Worked on other languages
         }else if(questionNode.isNodeType("exo:answerHome") || questionNode.isNodeType("exo:commentHome")) {
           questionNode = questionNode.getParent() ;
           if(!questionNode.isNodeType("exo:faqQuestion")) return VoidReturn.VALUE ; // Worked on other languages
         }
         String categoreDescription = "";
         //categoryNode = getCategoryNodeById(questionNode.getProperty("exo:categoryId").getString(), sProvider);
         categoryNode = questionNode.getParent().getParent() ;
         if(categoryNode.hasProperty("exo:description")) categoreDescription = categoryNode.getProperty("exo:description").getString();
         else categoreDescription = "eXo link:" + eXoLink;
         
         //System.out.println("questionNode=========>"+ questionNode.getPath());
         if(!questionNode.getProperty("exo:isActivated").getBoolean() || 
             !questionNode.getProperty("exo:isApproved").getBoolean()){
           removeItemInFeed(questionNode.getName(), categoryNode, categoreDescription);
           return VoidReturn.VALUE;
         }
         
         // Create new entry
         List<String> listContent = new ArrayList<String>();
         String content = "";
         if(questionNode.hasNode("faqAnswerHome") && questionNode.getNode("faqAnswerHome").hasNodes()) {
           for(String answer : getAnswers(questionNode)) 
             content += " <b><u>Answer:</u></b> " + answer + ". ";
         }
         if(questionNode.hasNode("faqCommentHome") && questionNode.getNode("faqCommentHome").hasNodes()){
           for(String comment : getComments(questionNode)) 
             content += " <b><u>Comment:</u></b> " + comment + ". ";
         }
         listContent.add(content);
         description = new SyndContentImpl();
         description.setType(descriptionType);
         description.setValue(questionNode.getProperty("exo:name").getString() + ". " + content);
         String questionPath = questionNode.getPath() ; 
         //linkItem += questionNode.getProperty("exo:categoryId").getString() + "/" + questionNode.getName() + "/0";
           linkItem += questionPath.substring(questionPath.indexOf("/categories/") + 1) ;
         entry = createNewEntry(questionNode.getName(), questionNode.getProperty("exo:title").getString(), 
                                 linkItem, listContent, description, questionNode.getProperty("exo:createdDate").getDate().getTime(),
                                 questionNode.getProperty("exo:author").getString());
         entry.setLink(linkItem);
         
         // update for RSS Feed
         SyndFeed feed = null;
         try{
           RSSNode = categoryNode.getNode(KS_RSS);
           getRSSData(RSSNode, data);
           if(typeEvent == Event.NODE_ADDED) feed = updateRSSFeed(data, null, entry);
           else feed = updateRSSFeed(data, questionNode.getName(), entry);
         } catch (PathNotFoundException e){
           RSSNode = categoryNode.addNode(KS_RSS, FAQ_RSS_TYPE);
           isNew = true;
           if(categoryNode.hasProperty("exo:createdDate"))
             feed = createNewFeed("", categoryNode.getProperty("exo:createdDate").getDate().getTime());
           else
             feed = createNewFeed("", new Date());
           feed.setLink(categoryLink + questionNode.getProperty("exo:categoryId").getString());
           feed.setEntries(Arrays.asList(new SyndEntry[]{entry}));
         }
         
         feed.setDescription(categoreDescription);
         try{
           feed.setTitle(categoryNode.getProperty("exo:name").getString());
         } catch (Exception e){
           feed.setTitle("Home");
         }

         SyndFeedOutput output = new SyndFeedOutput();
         data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
         saveRssContent(categoryNode, RSSNode, data, isNew);
       } else { // removed node
         //categoryNode = getCategoryNodeById(cateid, sProvider);
         /*if(path.indexOf("/faqCommentHome") > 0 || path.indexOf("/faqAnswerHome") > 0){
           if(path.indexOf("/faqCommentHome") > 0) path = path.substring(0, path.indexOf("/faqCommentHome"));
           else path = path.substring(0, path.indexOf("/faqAnswerHome"));
           this.generateFAQRSS(path, Event.PROPERTY_CHANGED, sProvider);
         } else {*/
           String categoryPath = path.substring(0, path.indexOf("/questions/")) ;
           //System.out.println("categoryPath =====> " + categoryPath);
           categoryNode = (Node)getCurrentSession().getItem(categoryPath) ;
           while(!categoryNode.isNodeType("exo:faqCategory")) {
             categoryNode = categoryNode.getParent() ;
           }
           removeItemInFeed(path.substring(path.lastIndexOf("/") + 1), categoryNode, "");
           //cateid = null;
         //}
       }     
     }catch(Exception e) {
       e.printStackTrace() ;
     }     
     
    return VoidReturn.VALUE; 
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
      e.printStackTrace();
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
      e.printStackTrace();
    }
    return listComment;
  }
  
  
  public InputStream getFeedContent(String targetId) throws Exception{    
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


}
