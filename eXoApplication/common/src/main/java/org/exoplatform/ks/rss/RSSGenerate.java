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
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.sun.syndication.feed.synd.SyndContent;
import com.sun.syndication.feed.synd.SyndContentImpl;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.feed.synd.SyndFeedImpl;
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
	
	public Node getKSServiceHome(SessionProvider sProvider, String serviceType) throws Exception {
		return	nodeHierarchyCreator_.getPublicApplicationNode(sProvider).getNode(serviceType) ;
	}
	
	public RSSGenerate(NodeHierarchyCreator nodeHierarchyCreator){
		if(nodeHierarchyCreator != null && nodeHierarchyCreator_ == null) nodeHierarchyCreator_ = nodeHierarchyCreator;
		data = new RSS();
	}

	public void addNodeRSS(Node nodeIsAdded, Node rssNode, RSS data, boolean isNew) throws Exception {
		rssNode.setProperty(contentProperty, data.getContent());
		if(isNew) nodeIsAdded.getSession().save();
		else nodeIsAdded.save();
	}

	public void getRSSData(Node rssNode, RSS data) throws Exception {
		if(rssNode.hasProperty(contentProperty)) data.setContent(rssNode.getProperty(contentProperty).getValue().getStream());
	}

	public List<SyndEntry> getDetailRss(RSS data, String idOfObjectChange) throws Exception{
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		SyndEntry entry;
		SyndContent description;
		
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(data.getContent());
		doc.getDocumentElement().normalize();
		NodeList listNodes = doc.getElementsByTagName("item");
		Element element = null;
		for(int i = 0; i < listNodes.getLength() && i < maxSize - 1; i ++){
		 	try{
				entry = new SyndEntryImpl();
				element = (Element) listNodes.item(i);
				if(element.getElementsByTagName("guid").item(0).getChildNodes().item(0).getNodeValue().equals(idOfObjectChange)) continue;
				entry.setTitle(element.getElementsByTagName("title").item(0).getChildNodes().item(0).getNodeValue());
				try{
					entry.setLink(element.getElementsByTagName("link").item(0).getChildNodes().item(0).getNodeValue());
				} catch (NullPointerException e){
					entry.setLink(" ");
				}
				description = new SyndContentImpl();
				description.setType("text/plain");
				try{
					description.setValue(element.getElementsByTagName("description").item(0).getChildNodes().item(0).getNodeValue());
				}catch(Exception e){
					description.setValue(" ");
				}
				entry.setDescription(description);
				entry.setUri(element.getElementsByTagName("guid").item(0).getChildNodes().item(0).getNodeValue());
				List<String> listContent = new ArrayList<String>();
				entry.setContents(listContent);
				entries.add(entry);
	 	} catch (Exception e){
	 		e.printStackTrace();
	 	}
	 }
	 return entries;
	}
	
	public void removeRSSItem(String objectid, Node node, String feedDescription) throws Exception{
		RSS data = new RSS();
		List<SyndEntry> entries = new ArrayList<SyndEntry>();
		
		Node RSSNode = null;
		try{
			RSSNode = node.getNode(KS_RSS);
		}catch(PathNotFoundException pn){
			return;
		}
		getRSSData(RSSNode, data);
		entries.addAll(getDetailRss(data, objectid));
		
		SyndFeed feed = this.createNewFedd();
		try{
			feed.setTitle(node.getProperty("exo:name").getString());
		}catch(PathNotFoundException pn){
			feed.setTitle("Root");
		}
		feed.setDescription(feedDescription);
		feed.setLink(eXoLink);
		feed.setFeedType(feedType);
		feed.setEntries(entries);
		
		SyndFeedOutput output = new SyndFeedOutput();
		data.setContent(new ByteArrayInputStream(output.outputString(feed).getBytes()));
		addNodeRSS(node, RSSNode, data, false);
	}
	
	public SyndFeed createNewFedd(){
		SyndFeed feed = new SyndFeedImpl();
		feed.setLink(eXoLink);
		feed.setFeedType(feedType);
		return feed;
	}
	
	public SyndEntry createNewEntry(String uri, String title, String link, List<String> listContent, SyndContent description){
		SyndEntry entry = new SyndEntryImpl();
		entry.setUri(uri);
		entry.setTitle(title);
		entry.setLink(link + uri);
		entry.setContributors(listContent);
		entry.setDescription(description);
		return entry;
	}
	
	public String getPageLink(){
		try{
			PortalRequestContext portalContext = Util.getPortalRequestContext();
			String url = (portalContext.getRequest().getRequestURL().toString()).replaceFirst("private", "public");
			return url;
		}catch(Exception e){
			return null;
		}
	}
	
	abstract public void generateFAQRSS(String path, int eventType, SessionProvider sProvider);
	abstract public void generateForumsRSS(String path, int typeEvent, SessionProvider sProvider ) throws Exception;
}
