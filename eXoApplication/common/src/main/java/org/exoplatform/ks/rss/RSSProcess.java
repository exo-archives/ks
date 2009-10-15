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
import javax.jcr.Value;
import javax.jcr.observation.Event;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;

public class RSSProcess extends RSSGenerate {
	//public static String cateid = null;
	protected String linkItem = "";
	
	public RSSProcess(InitParams params) throws Exception{
		super(null);
		PropertiesParam proParams = params.getPropertiesParam("rss-limit-config");
		if (proParams != null) {
			String maximum = proParams.getProperty("maximum.rss");
			if (maximum != null && maximum.length() > 0) {
				try {
					maxSize = Integer.parseInt(maximum);
				} catch (Exception e) {
					maxSize = 10;
    		}
    	}
    }
		System.out.println("\n\n-------------------------->max: " + maxSize);
	}
	
	public RSSProcess(NodeHierarchyCreator nodeHierarchyCreator){
		super(nodeHierarchyCreator);
	}
	
	public RSSProcess(SessionProvider sProvider, String serviceType){
		super(null);
		try {
			if(serviceType.equals(KS_FAQ)) appHomeNode = getKSServiceHome(sProvider, FAQ_APP);
			else appHomeNode = getKSServiceHome(sProvider, FORUM_APP);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create RSS file for Applications in KS. System will be filter type of application
	 * automatically (for example: FAQ or FORUM) based on path of node is changed, after that, System will call function
	 * to create RSS for that application.
	 * @param	path			the path of node is changed
	 * @param	typeEvent	the type of event
	 * @throws Exeption
	 */
	public boolean generateRSS(String path, int typeEvent) throws Exception	{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		linkItem = this.getPageLink();
		try{
			if(path.indexOf(FAQ_APP)>0){
				generateFAQRSS(path, typeEvent, sProvider);
			} else {
				generateForumsRSS(path, typeEvent, sProvider);
			}
		}catch(Exception e) {
			return false ;
		}finally{sProvider.close();}
		
		return true ;
	}
	
	public void generateForumsRSS(String path, int typeEvent, SessionProvider sProvider ) throws Exception{
		appHomeNode = getKSServiceHome(sProvider, FORUM_APP);
		Node node = null;
		linkItem += "?portal:componentId=forum&portal:type=action&portal:isSecure=false&uicomponent=UIBreadcumbs&" +
								"op=ChangePath&objectId=";
		if(typeEvent != Event.NODE_REMOVED){
			node = (Node)appHomeNode.getSession().getItem(path);
			if(node.isNodeType("exo:post")){
				generatePostRSS(path, typeEvent);
			}
		}else{
			String objectId = null;
			objectId = path.substring(path.lastIndexOf("/") + 1);
			path = path.substring(0, path.lastIndexOf("/"));
			while(node == null){
				try{
					node = (Node)appHomeNode.getSession().getItem(path);
				}catch(PathNotFoundException pn){
					objectId = path.substring(path.lastIndexOf("/") + 1);
					path = path.substring(0, path.lastIndexOf("/"));
					node = null;
				}
			}
			while(node.isNodeType("exo:forumCategory") || node.isNodeType("exo:forum") || node.isNodeType("exo:topic")){
				String description = null;
				if(node.hasProperty("exo:description"))
					description = node.getProperty("exo:description").getString();
				else
					description= " ";
				removeRSSItem(objectId, node, description);
				
				node = node.getParent();
			}
		}
	}
	
	protected void generatePostRSS(String path, int typeEvent){
		boolean isNew = false;
		try{
			Node postNode = (Node)appHomeNode.getSession().getItem(path);
			Node topicNode = postNode.getParent();
			
			if((postNode.hasProperty("exo:isFirstPost") && postNode.getProperty("exo:isFirstPost").getBoolean() &&
					(topicNode.hasProperty("exo:isApproved") && !topicNode.getProperty("exo:isApproved").getBoolean())) ||
					(postNode.hasProperty("exo:userPrivate") && 
							!postNode.getProperty("exo:userPrivate").getValues()[0].getString().equals("exoUserPri"))
					)
				return;
			
			Node forumNode = topicNode.getParent();
			Node categoryNode = forumNode.getParent();
			
			if((postNode.hasProperty("exo:isApproved") && !postNode.getProperty("exo:isApproved").getBoolean())||
					(postNode.hasProperty("exo:isActiveByTopic") && !postNode.getProperty("exo:isActiveByTopic").getBoolean())||
					(postNode.hasProperty("exo:isHidden") && postNode.getProperty("exo:isHidden").getBoolean())
					){
				if(typeEvent != Event.NODE_ADDED){
					if(topicNode.hasProperty("exo:description"))
						removeRSSItem(postNode.getName(), topicNode, topicNode.getProperty("exo:description").getString());
					else
						removeRSSItem(postNode.getName(), topicNode, " ");
					
					if(forumNode.hasProperty("exo:description"))
						removeRSSItem(postNode.getName(), forumNode, forumNode.getProperty("exo:description").getString());
					else
						removeRSSItem(postNode.getName(), forumNode, " ");
					
					if(categoryNode.hasProperty("exo:description"))
						removeRSSItem(postNode.getName(), categoryNode, categoryNode.getProperty("exo:description").getString());
					else
						removeRSSItem(postNode.getName(), categoryNode, " ");
				}
				return;
			}
			
			SyndContent description;
			List<String> listContent = new ArrayList<String>();
			listContent.add(postNode.getProperty("exo:message").getString());
			description = new SyndContentImpl();
			description.setType(descriptionType);
			description.setValue(postNode.getProperty("exo:message").getString());
			SyndEntry entry = createNewEntry(postNode.getName(), postNode.getProperty("exo:name").getString(), 
															linkItem, listContent, description, postNode.getProperty("exo:createdDate").getDate().getTime(), 
															postNode.getProperty("exo:owner").getString());
			entry.setLink(linkItem + topicNode.getName());
			Node RSSNode = null;
			SyndFeed feed = null;
			for(Node node : new Node[]{topicNode, forumNode, categoryNode}){
				isNew = false;
				data = new RSS();
				try{
					RSSNode = node.getNode(KS_RSS);
					getRSSData(RSSNode, data);
					feed = updateRSSFeed(data, postNode.getName(), entry);
				} catch (PathNotFoundException e){
					RSSNode = node.addNode(KS_RSS, FORUM_RSS_TYPE);
					isNew = true;
					feed = this.createNewFedd(node.getProperty("exo:name").getString(), node.getProperty("exo:createdDate").getDate().getTime());
					feed.setLink(linkItem + node.getName());
					feed.setEntries(Arrays.asList(new SyndEntry[]{entry}));
				} catch (Exception e) {
//					e.printStackTrace();
					continue;
				}
				
				if(node.hasProperty("exo:description"))
					feed.setDescription(node.getProperty("exo:description").getString());
				else
					feed.setDescription(" ");
	
				SyndFeedOutput output = new SyndFeedOutput();
				data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
				addNodeRSS(node, RSSNode, data, isNew);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Create RSS file for FAQ. Only use this function when use sure that <code>path</code> is FAQ
	 * @param	path			the path of node is changed
	 * @param	typeEvent	the type of event
	 * @param sProvider	the SessionProvider
	 */
	public void generateFAQRSS(String path, int typeEvent, SessionProvider sProvider){
		boolean isNew = false;
		//System.out.println("generateFAQRSS=====typeEvent====>" + typeEvent);
		//System.out.println("generateFAQRSS=====path====>" + path);
		try{
			appHomeNode = getKSServiceHome(sProvider, FAQ_APP);
			Node categoryNode = null;
			if(typeEvent != 2) { // Added node or edited properties
				SyndEntry entry;
				SyndContent description;
				Node RSSNode = null;
				String categoryLink = linkItem + "?portal:componentId=faq&portal:type=action&portal:isSecure=false&uicomponent=UICategories&op=OpenCategory&" +
																					"objectId=";
				linkItem += "?portal:componentId=faq&portal:type=action&portal:isSecure=false&uicomponent=UIQuestions&op=ViewQuestion&" +
											"objectId=";
				Node questionNode = (Node)appHomeNode.getSession().getItem(path) ;
				if(!questionNode.isNodeType("exo:faqQuestion") && !questionNode.isNodeType("exo:answer") && !questionNode.isNodeType("exo:answerHome") 
						&& !questionNode.isNodeType("exo:comment") && !questionNode.isNodeType("exo:commentHome")) return;
				else if(questionNode.isNodeType("exo:answer") || questionNode.isNodeType("exo:comment")) {
					questionNode = questionNode.getParent().getParent();
					if(!questionNode.isNodeType("exo:faqQuestion")) return ; // Worked on other languages
				}else if(questionNode.isNodeType("exo:answerHome") || questionNode.isNodeType("exo:commentHome")) {
					questionNode = questionNode.getParent() ;
					if(!questionNode.isNodeType("exo:faqQuestion")) return ; // Worked on other languages
				}
				String categoreDescription = "";
				//categoryNode = getCategoryNodeById(questionNode.getProperty("exo:categoryId").getString(), sProvider);
				categoryNode = questionNode.getParent().getParent() ;
				if(categoryNode.hasProperty("exo:description")) categoreDescription = categoryNode.getProperty("exo:description").getString();
				else categoreDescription = "eXo link:" + eXoLink;
				
				//System.out.println("questionNode=========>"+ questionNode.getPath());
				if(!questionNode.getProperty("exo:isActivated").getBoolean() || 
						!questionNode.getProperty("exo:isApproved").getBoolean()){
					removeRSSItem(questionNode.getName(), categoryNode, categoreDescription);
					return;
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
						feed = this.createNewFedd("", categoryNode.getProperty("exo:createdDate").getDate().getTime());
					else
						feed = this.createNewFedd("", new Date());
					feed.setLink(categoryLink + questionNode.getProperty("exo:categoryId").getString());
					feed.setEntries(Arrays.asList(new SyndEntry[]{entry}));
				} catch (Exception e) {
					//e.printStackTrace();
				}
				
				feed.setDescription(categoreDescription);
				try{
					feed.setTitle(categoryNode.getProperty("exo:name").getString());
				} catch (Exception e){
					feed.setTitle("Home");
				}

				SyndFeedOutput output = new SyndFeedOutput();
				data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
				addNodeRSS(categoryNode, RSSNode, data, isNew);
			} else { // removed node
				//categoryNode = getCategoryNodeById(cateid, sProvider);
				/*if(path.indexOf("/faqCommentHome") > 0 || path.indexOf("/faqAnswerHome") > 0){
					if(path.indexOf("/faqCommentHome") > 0) path = path.substring(0, path.indexOf("/faqCommentHome"));
					else path = path.substring(0, path.indexOf("/faqAnswerHome"));
					this.generateFAQRSS(path, Event.PROPERTY_CHANGED, sProvider);
				} else {*/
					String categoryPath = path.substring(0, path.indexOf("/questions/")) ;
					//System.out.println("categoryPath =====> " + categoryPath);
					categoryNode = (Node)appHomeNode.getSession().getItem(categoryPath) ;
					while(!categoryNode.isNodeType("exo:faqCategory")) {
						categoryNode = categoryNode.getParent() ;
					}
					removeRSSItem(path.substring(path.lastIndexOf("/") + 1), categoryNode, "");
					//cateid = null;
				//}
			}			
		}catch(Exception e) {
			//e.printStackTrace() ;
		}finally{
			sProvider.close() ;
		}
	}

	protected List<String> getAnswers(Node questionNode) throws Exception{
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

	protected List<String> getComments(Node questionNode) throws Exception{
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
	
	public InputStream getRSSNode(SessionProvider sProvider, String objectId, String appType) throws Exception{
		Node parentNode = null;
		String rssType = "";
		try{
			if(appType.equals(KS_FAQ)){
				parentNode = getKSServiceHome(sProvider, FAQ_APP).getNode(objectId) ;
				/*if(!parentNode.getProperty("exo:isView").getBoolean()){
					return null;
				}*/
				rssType = FAQ_RSS_TYPE;
			}else{
				parentNode = getKSServiceHome(sProvider, FORUM_APP);
				QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
				StringBuffer queryString = new StringBuffer("/jcr:root" + parentNode.getPath() 
						+ "//*[@exo:id='").append(objectId).append("']") ;
				Query query = qm.createQuery(queryString.toString(), Query.XPATH);
				QueryResult result = query.execute();
				parentNode = result.getNodes().nextNode() ;
				rssType = FORUM_RSS_TYPE;
			}
		} catch (Exception e){
			return null;
		}
		Node RSSNode = null;
		InputStream inputStream = null;
		if(!parentNode.hasNode(KS_RSS)){
			String feedType = "rss_2.0";
			String title = "FORUM RSS FEED";
			if(appType.equals(KS_FAQ))title = "FAQ RSS FEED";
			SyndFeed feed = createNewFedd(title, new Date());
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			RSSNode = parentNode.addNode(KS_RSS, rssType);
			try{
				feed.setTitle(parentNode.getProperty("exo:name").getString());
				if(parentNode.hasProperty("exo:description"))
					feed.setDescription(parentNode.getProperty("exo:description").getString());
				else feed.setDescription(" ");
			} catch (Exception e){
				feed.setTitle(parentNode.getName());
				feed.setDescription(" ");
			}
			feed.setLink(eXoLink);
			feed.setFeedType(feedType);
			feed.setEntries(entries);
			feed.setPublishedDate(new Date());
			RSS data = new RSS();
			SyndFeedOutput output = new SyndFeedOutput();
			inputStream = new ByteArrayInputStream(output.outputString(feed).getBytes());
			data.setContent(inputStream);
			addNodeRSS(parentNode, RSSNode, data, false);
			
			return inputStream;
		} else {
			RSSNode = parentNode.getNode(KS_RSS);
			return RSSNode.getProperty("exo:content").getStream();
		}
	}
	
	public List<String> getForumSubscription(String userId) throws Exception {
		List<String> list = new ArrayList<String>();
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		try {
			Node subscriptionNode = appHomeNode.getNode("ForumSystem/UserProfileHome/"+userId+"/forumSubscription"+userId);
			try {
				list.addAll(ValuesToList(subscriptionNode.getProperty("exo:categoryIds").getValues()));
				list.addAll(ValuesToList(subscriptionNode.getProperty("exo:forumIds").getValues()));
				list.addAll(ValuesToList(subscriptionNode.getProperty("exo:topicIds").getValues()));
			} catch (PathNotFoundException e) {
			}
    } catch (Exception e) {
    }finally {sProvider.close();}
		return list;
  }
	
	private List<String> ValuesToList(Value[] values) throws Exception {
		List<String> list = new ArrayList<String>();
		for (int i = 0; i < values.length; ++i) {
			list.add(values[i].getString());
		}
		return list;
	}
	
	public InputStream getRSSOfMultiObjects(String userId, SessionProvider sProvider) throws Exception{
		if(userId == null || userId.trim().length() == 0) return null;
		InputStream inputStream = null;
		Map<String, SyndEntry> mapEntries = new HashMap<String, SyndEntry>();
		Node RSSNode = null;
		appHomeNode = getKSServiceHome(sProvider, FORUM_APP);
		SyndEntry syndEntry = null;
		for(String objectId : getForumSubscription(userId)){
			try{
				RSSNode = getNodeById(objectId, sProvider).getNode(KS_RSS);
				getRSSData(RSSNode, data);
				for(Object entry : updateRSSFeed(data, null, null).getEntries()){
					syndEntry = (SyndEntry)entry;
					mapEntries.put(syndEntry.getUri(), syndEntry);
				}
			} catch (Exception e){}
		}
		SyndFeed feed = this.createNewFedd("FORUM RSS FEED", new Date());
		feed.setDescription(" ");
		feed.setEntries(Arrays.asList(mapEntries.values().toArray(new SyndEntry[]{})));
		SyndFeedOutput output = new SyndFeedOutput();
		inputStream = new ByteArrayInputStream(output.outputString(feed).getBytes());
		return inputStream;
	}
}
