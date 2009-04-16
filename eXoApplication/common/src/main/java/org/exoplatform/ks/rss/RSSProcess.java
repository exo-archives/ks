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
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
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
import com.sun.syndication.feed.synd.SyndFeedImpl;
import com.sun.syndication.io.SyndFeedOutput;

public class RSSProcess extends RSSGenerate {
	public static String cateid = null;
	protected String linkItem = "";
	protected final String KS_RSS_TYPE = "exo:KSRSS".intern();
	protected final String KS_FAQ = "faq".intern();
	protected final String KS_FORUM = "forum".intern();
	protected final String FAQ_APP = "faqApp".intern();
	protected final String FORUM_APP = "ForumService".intern();
	
	
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
	public void generateRSS(String path, int typeEvent) throws Exception	{
		SessionProvider sProvider = SessionProvider.createSystemProvider() ;
		linkItem = this.getPageLink();
		if(path.indexOf(FAQ_APP)>0){
			generateFAQRSS(path, typeEvent, sProvider);
		} else {
			generateForumsRSS(path, typeEvent, sProvider);
		}
		sProvider.close();
	}
	
	public void generateForumsRSS(String path, int typeEvent, SessionProvider sProvider ) throws Exception{
		appHomeNode = getKSServiceHome(sProvider, FORUM_APP);
		Node node = null;
		linkItem += "?portal:componentId=forum&portal:type=action&portal:isSecure=false&uicomponent=UIBreadcumbs&" +
								"op=ChangePath&objectId=";
		if(typeEvent != Event.NODE_REMOVED){
			node = (Node)appHomeNode.getSession().getItem(path);
			if(node.isNodeType("exo:forum")){
				generateCategoryRSS(path, typeEvent);
			} else if(node.isNodeType("exo:topic")){
					generateForumRSS(path, typeEvent);
			} else if(node.isNodeType("exo:post")){
				generateTopicRSS(path, typeEvent);
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
			if(node.isNodeType("exo:forumCategory") || node.isNodeType("exo:forum") || node.isNodeType("exo:topic")){
				String description = null;
				if(node.hasProperty("exo:description"))
					description = node.getProperty("exo:description").getString();
				else
					description= " ";
				removeRSSItem(objectId, node, description);
			}
		}
	}
	
	protected void generateCategoryRSS(String path, int typeEvent) throws Exception{
		Node forumNode = (Node)appHomeNode.getSession().getItem(path);
		Node categoryNode = forumNode.getParent();
		if(forumNode.getProperty("exo:isClosed").getBoolean()){
			if(categoryNode.hasProperty("exo:description"))
				removeRSSItem(forumNode.getName(), categoryNode, categoryNode.getProperty("exo:description").getString());
			else
				removeRSSItem(forumNode.getName(), categoryNode, " ");
			return;
		}
		boolean isNew = false;
		if(categoryNode.hasProperty("exo:userPrivate") && categoryNode.getProperty("exo:userPrivate").getValues().length > 1) return;
		
		SyndFeed feed = this.createNewFedd();
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		SyndEntry entry;
		SyndContent description;
		Node RSSNode = null;
		try{
			RSSNode = categoryNode.getNode(KS_RSS);
			getRSSData(RSSNode, data);
			entries.addAll(getDetailRss(data, forumNode.getName()));
			isNew = true;
		} catch (PathNotFoundException e){
			RSSNode = categoryNode.addNode(KS_RSS, KS_RSS_TYPE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		feed.setTitle(categoryNode.getProperty("exo:name").getString());
		if(categoryNode.hasProperty("exo:description"))
			feed.setDescription(categoryNode.getProperty("exo:description").getString());
		else
			feed.setDescription(" ");
		
		List<String> listContent = new ArrayList<String>();
		if(forumNode.hasProperty("exo:description"))
			listContent.add(forumNode.getProperty("exo:description").getString());
		else 
			listContent.add(" ");
		description = new SyndContentImpl();
		description.setType(descriptionType);
		if(forumNode.hasProperty("exo:description"))description.setValue(forumNode.getProperty("exo:description").getString());
		else description.setValue(forumNode.getProperty("exo:owner").getString());
		entry = createNewEntry(forumNode.getName(), forumNode.getProperty("exo:name").getString(), 
														linkItem, listContent, description);
		entries.add(0, entry);
		feed.setEntries(entries);

		SyndFeedOutput output = new SyndFeedOutput();
		data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
		addNodeRSS(categoryNode, RSSNode, data, isNew);
	}
	
	protected void generateForumRSS(String path, int typeEvent) throws Exception{
		boolean isNew = false;
		Node topicNode = (Node)appHomeNode.getSession().getItem(path);
		Node forumNode = topicNode.getParent();
		if((topicNode.hasProperty("exo:isClosed") && topicNode.getProperty("exo:isClosed").getBoolean())||
				(topicNode.hasProperty("exo:isApproved") && !topicNode.getProperty("exo:isApproved").getBoolean())||
				(topicNode.hasProperty("exo:isActive") && !topicNode.getProperty("exo:isActive").getBoolean())||
				(topicNode.hasProperty("exo:isActiveByForum") && !topicNode.getProperty("exo:isActiveByForum").getBoolean())){
			if(forumNode.hasProperty("exo:description"))
				removeRSSItem(topicNode.getName(), forumNode, forumNode.getProperty("exo:description").getString());
			else
				removeRSSItem(topicNode.getName(), forumNode, " ");
			return;
		}
		SyndFeed feed = this.createNewFedd();
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		SyndEntry entry;
		SyndContent description;
		Node RSSNode = null;
		try{
			RSSNode = forumNode.getNode(KS_RSS);
			getRSSData(RSSNode, data);
			entries.addAll(getDetailRss(data, topicNode.getName()));
			isNew = true;
		} catch (PathNotFoundException e){
			RSSNode = forumNode.addNode(KS_RSS, KS_RSS_TYPE);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		feed.setTitle(forumNode.getProperty("exo:name").getString());
		if(forumNode.hasProperty("exo:description"))
			feed.setDescription(forumNode.getProperty("exo:description").getString());
		else
			feed.setDescription(" ");
		
		List<String> listContent = new ArrayList<String>();
		if(topicNode.hasProperty("exo:description"))
			listContent.add(topicNode.getProperty("exo:description").getString());
		else 
			listContent.add(" ");
		description = new SyndContentImpl();
		description.setType(descriptionType);
		if(topicNode.hasProperty("exo:description"))description.setValue(topicNode.getProperty("exo:description").getString());
		else description.setValue(topicNode.getProperty("exo:owner").getString());
		entry = createNewEntry(topicNode.getName(), topicNode.getProperty("exo:name").getString(), 
														linkItem, listContent, description);
		entries.add(0, entry);
		feed.setEntries(entries);

		SyndFeedOutput output = new SyndFeedOutput();
		data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
		addNodeRSS(forumNode, RSSNode, data, isNew);
	}
	
	protected void generateTopicRSS(String path, int typeEvent){
		boolean isNew = false;
		try{
			Node postNode = (Node)appHomeNode.getSession().getItem(path);
			Node topicNode = postNode.getParent();
			if((postNode.hasProperty("exo:isApproved") && !postNode.getProperty("exo:isApproved").getBoolean())||
					(postNode.hasProperty("exo:isActiveByTopic") && !postNode.getProperty("exo:isActiveByTopic").getBoolean())||
					(postNode.hasProperty("exo:isHidden") && postNode.getProperty("exo:isHidden").getBoolean())){
				if(topicNode.hasProperty("exo:description"))	
					removeRSSItem(postNode.getName(), topicNode, topicNode.getProperty("exo:description").getString());
				else
					removeRSSItem(postNode.getName(), topicNode, " ");
				return;
			}
			SyndFeed feed = this.createNewFedd();
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			SyndEntry entry;
			SyndContent description;
			Node RSSNode = null;
			try{
				RSSNode = topicNode.getNode(KS_RSS);
				getRSSData(RSSNode, data);
				entries.addAll(getDetailRss(data, postNode.getName()));
				isNew = true;
			} catch (PathNotFoundException e){
				RSSNode = topicNode.addNode(KS_RSS, KS_RSS_TYPE);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			feed.setTitle(topicNode.getProperty("exo:name").getString());
			if(topicNode.hasProperty("exo:description"))
				feed.setDescription(topicNode.getProperty("exo:description").getString());
			else
				feed.setDescription(" ");
			
			List<String> listContent = new ArrayList<String>();
			listContent.add(postNode.getProperty("exo:message").getString());
			description = new SyndContentImpl();
			description.setType(descriptionType);
			if(postNode.hasProperty("exo:modifiedBy"))description.setValue(postNode.getProperty("exo:modifiedBy").getString());
			else description.setValue(postNode.getProperty("exo:owner").getString());
			entry = createNewEntry(postNode.getName(), postNode.getProperty("exo:message").getString(), 
															linkItem, listContent, description);
			entry.setLink(linkItem + topicNode.getName());
			if(postNode.hasProperty("exo:isFirstPost") && postNode.getProperty("exo:isFirstPost").getBoolean())
				entries.add(0, entry);
			else {
				try{
					entries.add(1, entry);
				}catch(Exception e){
					entries.add(0, entry);
				}
			}
			feed.setEntries(entries);

			SyndFeedOutput output = new SyndFeedOutput();
			data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
			addNodeRSS(topicNode, RSSNode, data, isNew);
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
		try{
			appHomeNode = getKSServiceHome(sProvider, FAQ_APP);
			SyndFeed feed = this.createNewFedd();
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			SyndEntry entry;
			SyndContent description;
			Node categoryNode = null;
			Node RSSNode = null;
			
			if(typeEvent != 2) {
				linkItem += "?portal:componentId=faq&portal:type=action&portal:isSecure=false&uicomponent=UIQuestions&op=ViewQuestion&" +
					"objectId=";
				Node addedQuestion = (Node)appHomeNode.getSession().getItem(path) ;
				if(!addedQuestion.isNodeType("exo:faqQuestion")) return;
				if(!addedQuestion.getProperty("exo:isActivated").getBoolean() || 
						!addedQuestion.getProperty("exo:isApproved").getBoolean()) return;
				categoryNode = getCategoryNodeById(addedQuestion.getProperty("exo:categoryId").getString(), sProvider);
				try{
					RSSNode = categoryNode.getNode(KS_RSS);
					getRSSData(RSSNode, data);
					entries.addAll(getDetailRss(data, addedQuestion.getName()));
				} catch (PathNotFoundException e){
					RSSNode = categoryNode.addNode(KS_RSS, KS_RSS_TYPE);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				try{
					feed.setTitle(categoryNode.getProperty("exo:name").getString());
					if(categoryNode.hasProperty("exo:description")) feed.setDescription(categoryNode.getProperty("exo:description").getString());
					else feed.setDescription("eXo link:" + eXoLink);
				} catch (Exception e){
					feed.setTitle(categoryNode.getName());
					feed.setDescription(" ");
				}

				try {
					
					List<String> listContent = new ArrayList<String>();
					String content = "";
					if(addedQuestion.hasNode("faqAnswerHome") && addedQuestion.getNode("faqAnswerHome").hasNodes()) {
						for(String answer : getAnswers(addedQuestion))	
							content += " <b><u>Answer:</u></b> " + answer + ". ";
					}
					if(addedQuestion.hasNode("faqCommentHome") && addedQuestion.getNode("faqCommentHome").hasNodes()){
						for(String comment : getComments(addedQuestion)) 
							content += " <b><u>Comment:</u></b> " + comment + ". ";
					}
					listContent.add(content);
					description = new SyndContentImpl();
					description.setType(descriptionType);
					description.setValue(addedQuestion.getProperty("exo:name").getString() + ". " + content);
					
					if(categoryNode.hasProperty("exo:id"))linkItem += categoryNode.getName();
					else linkItem += "Root";
					linkItem += "/" + addedQuestion.getName() + "/0";
					entry = createNewEntry(addedQuestion.getName(), addedQuestion.getProperty("exo:title").getString(), 
																	linkItem, listContent, description);
					entry.setLink(linkItem);
					entries.add(0, entry);
					feed.setEntries(entries);

					SyndFeedOutput output = new SyndFeedOutput();
					data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
					addNodeRSS(categoryNode, RSSNode, data, isNew);
					/*
					// test file xml 
					Writer writer = new FileWriter("maivanha.xml");
					output.output(feed,writer);
					writer.close();
					System.out.println("--------------------->finish write xml file");*/
				} catch (Exception ex) {
						ex.printStackTrace();
				}
			} else {
				categoryNode = getCategoryNodeById(cateid, sProvider);
				if(categoryNode.hasProperty("exo:description"))
					removeRSSItem(path.substring(path.lastIndexOf("/") + 1), categoryNode, categoryNode.getProperty("exo:description").getString());
				else
					removeRSSItem(path.substring(path.lastIndexOf("/") + 1), categoryNode, " ");
				cateid = null;
			}
		}catch(Exception e) {
			e.printStackTrace() ;
		}finally{
			sProvider.close() ;
		}
	}
	
	public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
		Node categoryHome = appHomeNode.getNode("catetories");	
		if(categoryId != null && categoryId.trim().length() > 0 && !categoryId.equals("null") && !categoryId.equals("FAQService")){
			QueryManager qm = categoryHome.getSession().getWorkspace().getQueryManager();
			StringBuffer queryString = new StringBuffer("/jcr:root" + categoryHome.getPath() 
					+ "//element(*,exo:faqCategory)[@exo:id='").append(categoryId).append("']") ;
			Query query = qm.createQuery(queryString.toString(), Query.XPATH);
			QueryResult result = query.execute();
			return result.getNodes().nextNode() ;
		} else{
			return categoryHome;
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
					if(answerNode.hasProperty("exo:responses")) 
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
		try{
			if(appType.equals(KS_FAQ)) parentNode = getCategoryNodeById(objectId, sProvider);
			else{
				parentNode = getKSServiceHome(sProvider, FORUM_APP);
				QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
				StringBuffer queryString = new StringBuffer("/jcr:root" + parentNode.getPath() 
						+ "//*[@exo:id='").append(objectId).append("']") ;
				Query query = qm.createQuery(queryString.toString(), Query.XPATH);
				QueryResult result = query.execute();
				parentNode = result.getNodes().nextNode() ;
			}
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
		Node RSSNode = null;
		InputStream inputStream = null;
		if(!parentNode.hasNode(KS_RSS)){
			String feedType = "rss_2.0";
			SyndFeed feed = new SyndFeedImpl();
			List<SyndEntry> entries = new ArrayList<SyndEntry>();
			RSSNode = parentNode.addNode(KS_RSS, KS_RSS_TYPE);
			try{
				feed.setTitle(parentNode.getProperty("exo:name").getString());
				if(parentNode.hasProperty("categoryNode"))feed.setDescription(parentNode.getProperty("exo:description").getString());
				else feed.setDescription(" ");
			} catch (Exception e){
				feed.setTitle(parentNode.getName());
				feed.setDescription(" ");
			}
			feed.setLink(eXoLink);
			feed.setFeedType(feedType);
			feed.setEntries(entries);
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
	
	protected Node getNodeById(String objectId, SessionProvider sProvider) throws Exception{
		Node parentNode = getKSServiceHome(sProvider, FORUM_APP);
		QueryManager qm = parentNode.getSession().getWorkspace().getQueryManager();
		StringBuffer queryString = new StringBuffer("/jcr:root" + parentNode.getPath() 
				+ "//*[@exo:id='").append(objectId).append("']") ;
		Query query = qm.createQuery(queryString.toString(), Query.XPATH);
		QueryResult result = query.execute();
		parentNode = result.getNodes().nextNode() ;
		return parentNode;
	}
	
	public InputStream getRSSOfMultiObjects(String[] objectIds, SessionProvider sProvider) throws Exception{
		InputStream inputStream = null;
		SyndFeed feed = this.createNewFedd();
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		Node objectNode = null;
		Node RSSNode = null;
		for(String objectId : objectIds){
			objectNode = getNodeById(objectId, sProvider);
			try{
				RSSNode = objectNode.getNode(KS_RSS);
				getRSSData(RSSNode, data);
				entries.addAll(getDetailRss(data, null));
			} catch (Exception e){}
		}
		feed.setTitle("FORUM RSS FEED");
		feed.setDescription(" ");
		feed.setEntries(entries);
		SyndFeedOutput output = new SyndFeedOutput();
		inputStream = new ByteArrayInputStream(output.outputString(feed).getBytes());
		return inputStream;
	}
}
