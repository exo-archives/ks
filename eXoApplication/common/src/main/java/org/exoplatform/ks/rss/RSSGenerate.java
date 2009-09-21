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
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
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
 * Author : Ha Mai Van
 *					ha.mai@exoplatform.com
 * Apr 10, 2009, 10:10:14 AM
 */
public abstract class RSSGenerate {
	public static NodeHierarchyCreator nodeHierarchyCreator_;
	public Node appHomeNode;
	public final String KS_RSS = "ks.rss".intern();
	public final String feedType = "rss_2.0".intern();
	public final String descriptionType = "text/plain".intern();
	public final String eXoLink = "http://www.exoplatform.com".intern();
	protected final String contentProperty = "exo:content".intern();
	public RSS data;
	public int maxSize = 20;
	
	public final String FAQ_RSS_TYPE = "exo:faqRSS".intern();
	public final String FORUM_RSS_TYPE = "exo:forumRSS".intern();
	public final String KS_FAQ = "faq".intern();
	public final String KS_FORUM = "forum".intern();
	public final String FAQ_APP = "faqApp".intern();
	public final String FORUM_APP = "ForumService".intern();
	
	public RSSGenerate(NodeHierarchyCreator nodeHierarchyCreator){
		nodeHierarchyCreator_ = nodeHierarchyCreator;
		data = new RSS();
	}

	public Node getKSServiceHome(SessionProvider sProvider, String serviceType) throws Exception {
		if(nodeHierarchyCreator_ == null) nodeHierarchyCreator_ = (NodeHierarchyCreator)PortalContainer.getInstance().getComponent(NodeHierarchyCreator.class) ;
		return	nodeHierarchyCreator_.getPublicApplicationNode(sProvider).getNode(serviceType) ;
	}

	public String getPageLink() throws Exception {
		try{
			org.exoplatform.portal.application.PortalRequestContext portalContext = org.exoplatform.portal.webui.util.Util.getPortalRequestContext();
			return (portalContext.getRequest().getRequestURL().toString()).replaceFirst("private", "public");
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * Get FAQ category node by ID. Only use for FAQ RSS
	 * @param categoryId	id of category
	 * @param sProvider		the session provider
	 * @return						category node
	 * @throws Exception
	 */
	/*public Node getCategoryNodeById(String categoryId, SessionProvider sProvider) throws Exception {
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
	}*/
	
	/**
	 * Get one node in FORUM application by id
	 * @param objectId	id of node which is got
	 * @param sProvider	the session provider
	 * @return					node
	 * @throws Exception
	 */
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

	/**
	 * Add RSS node for a node in KS Application.
	 * @param nodeIsAdded	Node is added RSS 
	 * @param rssNode			rssNode which is added into KS node
	 * @param data				data of RSS feed
	 * @param isNew				is <code>true</code> if is add new RSS and <code>false</code> if is update
	 * @throws Exception
	 */
	public void addNodeRSS(Node nodeIsAdded, Node rssNode, RSS data, boolean isNew) throws Exception {
		try {
			rssNode.setProperty(contentProperty, data.getContent());
			if(isNew) nodeIsAdded.getSession().save();
			else nodeIsAdded.save();
    } catch (RepositoryException e) {
    	//e.printStackTrace() ;
    }
	}

	/**
	 * Get data of RSS node, content of RSS node will be set into the second parameter 
	 * @param rssNode		RSS node
	 * @param data			data which is used to store content of RSS node
	 * @throws Exception
	 */
	public void getRSSData(Node rssNode, RSS data) throws Exception {
		if(rssNode.hasProperty(contentProperty)) data.setContent(rssNode.getProperty(contentProperty).getValue().getStream());
	}

	/**
	 * Update content for RSSFeed.
	 * <p>The first, based on RSS parameter (the first parameter) to get all items.<br>
	 * After that, from id of object which is changed,the item of this object  will be removed from RSSFeed.<br>
	 * The finally, add new entry (the third parameter) into top of RSS Feed.<br></p>
	 * <p><b><u>Note:</u></b> 
	 * <i>Set the third parameter is <b><code>null</code></b> when remove an item</i></p>
	 * <i>Set the second parameter is <b><code>null</code></b> when get all items and add new entry with
	 * the third parameter</i></p>
	 * <i>Set the second and third parameter is <b><code>null</code></b> when get all items without add any item</i></p>
	 * @param data							RSS object
	 * @param removeItemId	id of object which is changed
	 * @return									List of items
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public SyndFeed updateRSSFeed(RSS data, String removeItemId, SyndEntry newEntry) throws Exception{
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(data.getContent());
		doc.getDocumentElement().normalize();
		
    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed = input.build(doc);
    List<SyndEntry> entries = feed.getEntries();
    if(removeItemId != null && removeItemId.trim().length() > 0){
	    for(SyndEntry syndEntry : entries){
	    	if(syndEntry.getUri().equals(removeItemId)){
	    		entries.remove(syndEntry);
	    		break;
	    	}
	    }
    }
		if(newEntry != null)entries.add(0, newEntry);
		feed.setEntries(entries);
		return feed;
	}
	
	/**
	 * Remove one item from RSS feed based on id of object which is changed 
	 * @param objectid				id of object
	 * @param node						Node content RSS feed
	 * @param feedDescription	description about RSS feed
	 * @throws Exception
	 */
	public void removeRSSItem(String objectid, Node node, String feedDescription) throws Exception{
		RSS data = new RSS();
		Node RSSNode = null;
		try{
			RSSNode = node.getNode(KS_RSS);
		}catch(PathNotFoundException pn){
			return;
		}
		getRSSData(RSSNode, data);
		
		SyndFeed feed = updateRSSFeed(data, objectid, null);
		try{
			feed.setTitle(node.getProperty("exo:name").getString());
		}catch(PathNotFoundException pn){
			feed.setTitle("Root");
		}
		feed.setDescription(feedDescription);
		
		SyndFeedOutput output = new SyndFeedOutput();
		data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
		addNodeRSS(node, RSSNode, data, false);
	}
	
	/**
	 * Create a new feed with some default content: link is the link to eXo web site
	 * and feed type is <code>rss_2.0</code>
	 * @return
	 */
	public SyndFeed createNewFedd(String title, Date pubDate){
		SyndFeed feed = new SyndFeedImpl();
		feed.setLink(eXoLink);
		feed.setFeedType(feedType);
		feed.setTitle(title);
		feed.setPublishedDate(pubDate);
		return feed;
	}
	
	/**
	 * Create new entry
	 * @param uri					uri of item
	 * @param title				title of item
	 * @param link				link to this item 
	 * @param listContent	the content of item
	 * @param description	the description for this item
	 * @return	SyndEntry
	 */
	public SyndEntry createNewEntry(String uri, String title, String link, List<String> listContent, SyndContent description, Date pubDate, String author){
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
	
	abstract public void generateFAQRSS(String path, int eventType, SessionProvider sProvider);
	abstract public void generateForumsRSS(String path, int typeEvent, SessionProvider sProvider ) throws Exception;
}
