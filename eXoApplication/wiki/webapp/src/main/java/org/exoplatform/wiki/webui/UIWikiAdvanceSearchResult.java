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
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.SearchResult;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiAdvanceSearchResult.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiAdvanceSearchResult.DownloadAttachActionListener.class),
      @EventConfig(listeners = UIWikiAdvanceSearchResult.ViewPageActionListener.class),
      @EventConfig(listeners = UIWikiAdvanceSearchResult.ChangePageActionListener.class),
      @EventConfig(listeners = UIWikiAdvanceSearchResult.NextPageActionListener.class),
      @EventConfig(listeners = UIWikiAdvanceSearchResult.PrevPageActionListener.class)
  }    
)
public class UIWikiAdvanceSearchResult extends UIContainer {
  private PageList<SearchResult> results_ ;
  private String keyword ;
  private int pageIndex = 1 ;
  
  public void setResult(PageList<SearchResult> results) {
    results_ = results ;
    pageIndex = 1 ;
  }
  
  private PageList<SearchResult> getResults() {
    return results_ ;
  }
  
  private int getPageIndex() throws Exception {
    return pageIndex ;
  }
  
  public void setKeyword(String keyword) { this.keyword = keyword ;}
  
  private String getKeyword () {return keyword ;}
  
  private Wiki getWiki(SearchResult result) throws Exception {
    Wiki searchWiki = null;
    try {     
      if (WikiNodeType.WIKI_PAGE_CONTENT.equals(result.getType())) {
        ContentImpl searchContent= (ContentImpl)getObject(result.getPath(), result.getType());       
         searchWiki = searchContent.getParent().getWiki();
      } else {
        //Search Object is attachment
        AttachmentImpl searchAtt= (AttachmentImpl)getObject(result.getPath(), result.getType());
        searchWiki = searchAtt.getParentPage().getWiki();
      }
    } catch (Exception e) {
    }
    return searchWiki;
  }

  private String getWikiType(SearchResult result) throws Exception {
    try {
      return org.exoplatform.wiki.utils.Utils.getWikiType(getWiki(result));
    } catch (Exception e) {
    }
    return null;
  }

  private Object getObject(String path, String type) throws Exception {
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    return wservice.findByPath(path, type) ;    
  }
  
  private String getPageTitle(String path) throws Exception {
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    return wservice.getPageTitleOfAttachment(path) ;    
  }
  
  private String getWikiNodeUri(SearchResult result) throws Exception {
    Wiki wiki= getWiki(result);
    String wikiType= getWikiType(result);
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
  
  static public class DownloadAttachActionListener extends EventListener<UIWikiAdvanceSearchResult> {
    public void execute(Event<UIWikiAdvanceSearchResult> event) throws Exception {
      String params = event.getRequestContext().getRequestParameter(OBJECTID);
      String path = params.substring(0, params.lastIndexOf("/")) ;
      String fileName = params.substring(params.lastIndexOf("/") + 1) ;
      String downloadLink = Utils.getDownloadLink(path, fileName, null) ;
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
    }
  }
  static public class ViewPageActionListener extends EventListener<UIWikiAdvanceSearchResult> {
    @Override
    public void execute(Event<UIWikiAdvanceSearchResult> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      wikiPortlet.changeMode(WikiMode.VIEW);
    }
  }
  static public class ChangePageActionListener extends EventListener<UIWikiAdvanceSearchResult> {
    @Override
    public void execute(Event<UIWikiAdvanceSearchResult> event) throws Exception {
      UIWikiAdvanceSearchResult uiResult = event.getSource();
      String pageNumber = event.getRequestContext().getRequestParameter(OBJECTID) ;
      uiResult.pageIndex = Integer.parseInt(pageNumber) ;
      
    }
  }
  
  static public class NextPageActionListener extends EventListener<UIWikiAdvanceSearchResult> {
    @Override
    public void execute(Event<UIWikiAdvanceSearchResult> event) throws Exception {
      UIWikiAdvanceSearchResult uiResult = event.getSource();
      if(uiResult.pageIndex < uiResult.results_.getAvailablePage()) {
        uiResult.pageIndex = uiResult.pageIndex + 1 ;
      }      
    }
  }
  
  static public class PrevPageActionListener extends EventListener<UIWikiAdvanceSearchResult> {
    @Override
    public void execute(Event<UIWikiAdvanceSearchResult> event) throws Exception {
      UIWikiAdvanceSearchResult uiResult = event.getSource();
      if(uiResult.pageIndex > 1) {
        uiResult.pageIndex = uiResult.pageIndex - 1 ;
      }
    }
  }
}
