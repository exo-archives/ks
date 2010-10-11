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
package org.exoplatform.wiki.webui;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.service.SearchResult;
import org.exoplatform.wiki.webui.core.UIAdvancePageIterator;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAdvanceSearchResult.gtmpl"  
)
public class UIWikiAdvanceSearchResult extends UIContainer {
  
  private String keyword ;
  
  public UIWikiAdvanceSearchResult() throws Exception {
    addChild(UIAdvancePageIterator.class, null, "SearchResultPageIterator");
  }
  
  public void setResult(PageList<SearchResult> results) throws Exception {
    UIAdvancePageIterator pageIterator = this.getChild(UIAdvancePageIterator.class);
    pageIterator.setPageList(results);
    pageIterator.getPageList().getPage(1);  
  }
  
  public PageList<SearchResult> getResults() {
    UIAdvancePageIterator pageIterator = this.getChild(UIAdvancePageIterator.class);
    return pageIterator.getPageList();   
  } 
  
  public void setKeyword(String keyword) { this.keyword = keyword ;}
  
  private String getKeyword () {return keyword ;}
  
  private Wiki getWiki(SearchResult result) throws Exception {
    Wiki searchWiki = null;
    try {
      if (WikiNodeType.WIKI_PAGE_CONTENT.equals(result.getType())) {
        ContentImpl searchContent = (ContentImpl) org.exoplatform.wiki.utils.Utils.getObject(result.getPath(),
                                                                                             result.getType());
        searchWiki = searchContent.getParent().getWiki();
      } else if (WikiNodeType.WIKI_ATTACHMENT.equals(result.getType())) {
        AttachmentImpl searchAtt = (AttachmentImpl) org.exoplatform.wiki.utils.Utils.getObject(result.getPath(),
                                                                                               WikiNodeType.WIKI_ATTACHMENT);
        searchWiki = searchAtt.getParentPage().getWiki();
      }
    } catch (Exception e) {
    }
    return searchWiki;
  }
  
  private String getWikiNodeUri(SearchResult result) throws Exception {
    Wiki wiki= getWiki(result);
    String wikiType= org.exoplatform.wiki.utils.Utils.getWikiType(wiki);
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    StringBuilder sb = new StringBuilder(portalRequestContext.getPortalURI());
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    sb.append(pageNodeSelected);
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(wikiType)) {
      sb.append("/");
      sb.append(wikiType);
      sb.append("/");
      sb.append(wiki.getOwner());
    }
    return sb.toString();
  } 
}
