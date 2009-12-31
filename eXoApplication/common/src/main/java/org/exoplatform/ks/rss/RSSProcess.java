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
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedOutput;

public abstract class RSSProcess extends RSSGenerate {


  public int maxSize = 20;
  protected KSDataLocation dataLocator;
  private static final Log log = ExoLogger.getLogger(RSSProcess.class);
	
	public RSSProcess() {
	  this.dataLocator = (KSDataLocation) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(KSDataLocation.class);
	}
  
  public RSSProcess(KSDataLocation dataLocator) throws Exception{
	  this.dataLocator = dataLocator;
	 }
	
	public RSSProcess(InitParams params, KSDataLocation dataLocator) throws Exception{
    this.dataLocator = dataLocator;
		init(params);
	}

  private void init(InitParams params) {
    if (params == null) {
		  return;
		}
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
  }
	

  protected Session getCurrentSession() {
    return dataLocator.getSessionManager().getCurrentSession();
  }

	/**
	 * Create RSS file for Applications in KS. System will be filter type of application
	 * automatically (for example: FAQ or FORUM) based on path of node is changed, after that, System will call function
	 * to create RSS for that application.
	 * @param	path			the path of node is changed
	 * @param	typeEvent	the type of event
	 * @throws Exeption
	 */
  public boolean generateRSS(String path, int typeEvent) throws Exception {
    try {
      String linkItem = this.getPageLink();
      generateFeed(path, typeEvent, linkItem);
    } catch (Exception e) {
      log.error("failed to generate feed for " + path, e);
      return false;
    }

    return true;
  }
	
	 private String getPageLink() throws Exception {
//   TODO: can not get org.exoplatform.portal.webui when run JUnit-test. So, when run JUnit-test, you must comment content in this function and return null.
   try{
     PortalRequestContext portalContext = Util.getPortalRequestContext();
     return (portalContext.getRequest().getRequestURL().toString()).replaceFirst("private", "public");
   }catch(Exception e){
     return null;
   }
//   Use for JUnit-test.
//   return null;
 }
	
	
	protected abstract void generateFeed(String path, int typeEvent, String linkBase) throws Exception;

	public abstract InputStream getFeedContent(String targetId) throws Exception;


	
	protected InputStream getFeedStream(Node parentNode, String feedNodetype, String feedTitle) throws Exception {
    Node RSSNode = null;
    InputStream inputStream = null;
    
    if(!parentNode.hasNode(KS_RSS)){
      String feedType = "rss_2.0";

      SyndFeed feed = createNewFeed(feedTitle, new Date());
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      RSSNode = parentNode.addNode(KS_RSS, feedNodetype);
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
      saveRssContent(parentNode, RSSNode, data, false);
      
      return inputStream;
    } else {
      RSSNode = parentNode.getNode(KS_RSS);
      return RSSNode.getProperty(RSSGenerate.CONTENT_PROPERTY).getStream();
    }
  }

  public KSDataLocation getDataLocator() {
    return dataLocator;
  }

  public void setDataLocator(KSDataLocation dataLocator) {
    this.dataLocator = dataLocator;
  }

	

}
