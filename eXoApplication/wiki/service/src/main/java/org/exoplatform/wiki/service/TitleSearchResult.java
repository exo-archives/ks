/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.service;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Sep 22, 2010  
 */
public class TitleSearchResult {
  private String fullTitle;

  private String type;

  private String path;

  private String uri;

  public TitleSearchResult() {
  }

  public TitleSearchResult(String fullTitle, String path, String type) throws Exception {
    this.fullTitle = fullTitle;
    this.type = type;
    this.path = path;
    setUri();
  }

  public String getFullTitle() {
    return fullTitle;
  }

  public void setFullTitle(String fullTitle) {
    this.fullTitle = fullTitle;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  private Wiki getWiki() throws Exception {
    Wiki searchWiki = null;
    try {
      if (WikiNodeType.WIKI_PAGE_CONTENT.equals(getType())) {
        ContentImpl searchContent = (ContentImpl) org.exoplatform.wiki.utils.Utils.getObject(getPath(),
                                                                                             getType());
        searchWiki = searchContent.getParent().getWiki();
      } else if (WikiNodeType.WIKI_ATTACHMENT.equals(getType())) {
        // Search Object is attachment
        AttachmentImpl searchAtt = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(getPath(),
                                                                                               getType());
        searchWiki = searchAtt.getParentPage().getWiki();
      }
    } catch (Exception e) {
    }
    return searchWiki;
  }

  private String getWikiType() throws Exception {
    try {
      return org.exoplatform.wiki.utils.Utils.getWikiType(getWiki());
    } catch (Exception e) {
    }
    return null;
  }

  public String getUri() {
    return uri;
  }

  private void setUri() throws Exception {
    StringBuilder sb = new StringBuilder();
    if (WikiNodeType.WIKI_PAGE_CONTENT.equals(getType())) {
      String temp = path.substring(0, path.lastIndexOf("/"));
      String wikiType= getWikiType();
      if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(wikiType)) {
        sb.append("/");
        sb.append(wikiType);
        sb.append("/");
        sb.append(getWiki().getOwner());
      }      
      sb.append(temp.substring(temp.lastIndexOf("/")));
    } else if (WikiNodeType.WIKI_ATTACHMENT.equals(getType())) {
      AttachmentImpl searchAtt = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(getPath(),
                                                                                             getType());
      sb.append(searchAtt.getDownloadURL());
    }
    uri = sb.toString();
  }
}
