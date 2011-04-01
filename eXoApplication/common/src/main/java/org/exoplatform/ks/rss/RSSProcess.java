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
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public abstract class RSSProcess {

  public int               maxSize = 20;

  protected KSDataLocation dataLocator;

  private static final Log LOG     = ExoLogger.getLogger(RSSProcess.class);

  public RSSProcess() {
    this.dataLocator = (KSDataLocation) ExoContainerContext.getCurrentContainer()
                                                           .getComponentInstanceOfType(KSDataLocation.class);
  }

  public RSSProcess(KSDataLocation dataLocator) {
    this.dataLocator = dataLocator;
  }

  public RSSProcess(InitParams params, KSDataLocation dataLocator) {
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

  protected String getPageLink() throws Exception {
    // when run JUnit-test, you must comment content in this function and return
    // null.
    try {
      PortalRequestContext portalContext = Util.getPortalRequestContext();
      return (portalContext.getRequest().getRequestURL().toString()).replaceFirst("private",
                                                                                  "public");
    } catch (Exception e) {
      return null;
    }
    // Use for JUnit-test.
    // return null;
  }

  protected InputStream getFeedStream(Node parentNode, String feedNodetype, String feedTitle) throws Exception {
    RSS feed = loadOrCreateFeed(parentNode, feedNodetype, feedTitle);
    return feed.getContent();
  }

  protected RSS loadOrCreateFeed(Node parentNode, String feedNodetype) throws Exception {
    return loadOrCreateFeed(parentNode, feedNodetype, null);
  }

  protected RSS loadOrCreateFeed(Node parentNode, String feedNodetype, String feedTitle) throws Exception {
    RSS rss = new RSS(parentNode);
    if (!rss.feedExists()) {
      PropertyReader reader = new PropertyReader(parentNode);
      String title = reader.string("exo:name", feedTitle);
      String description = reader.string("exo:description", " ");
      SyndFeed feed = RSS.createNewFeed(title, new Date());
      List<SyndEntry> entries = new ArrayList<SyndEntry>();
      feed.setDescription(description);
      feed.setEntries(entries);

      rss = new RSS(parentNode);
      rss.saveFeed(feed, feedNodetype);

    }
    return rss;
  }

  /**
   * Get one node in FORUM application by id
   * 
   * @param objectId id of node which is got
   * @param sProvider the session provider
   * @return node
   * @throws Exception
   */
  protected Node getNodeById(String objectId) throws Exception {
    Node parentNode = getForumServiceHome();
    QueryManager qm = dataLocator.getSessionManager()
                                 .getCurrentSession()
                                 .getWorkspace()
                                 .getQueryManager();
    StringBuffer queryString = new StringBuffer("/jcr:root" + parentNode.getPath()
        + "//*[@exo:id='").append(objectId).append("']");
    
    Query query = qm.createQuery(queryString.toString(), Query.XPATH);
    QueryResult result = query.execute();
    parentNode = result.getNodes().nextNode();
    return parentNode;
  }

  protected Node getForumServiceHome() throws Exception {
    String path = dataLocator.getForumHomeLocation();
    return dataLocator.getSessionManager().getCurrentSession().getRootNode().getNode(path);
  }

  public KSDataLocation getDataLocator() {
    return dataLocator;
  }

  public void setDataLocator(KSDataLocation dataLocator) {
    this.dataLocator = dataLocator;
  }

}
