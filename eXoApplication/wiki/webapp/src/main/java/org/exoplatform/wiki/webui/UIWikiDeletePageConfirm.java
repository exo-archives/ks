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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.search.SearchResult;
import org.exoplatform.wiki.webui.core.UIWikiComponent;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiDeletePageConfirm.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiDeletePageConfirm.OKActionListener.class),
      @EventConfig(listeners = UIWikiDeletePageConfirm.CancelActionListener.class)
    }
)

public class UIWikiDeletePageConfirm extends UIWikiComponent {
  private WikiService wservice ;
  private String pageID ;
  private String owner ;
  public UIWikiDeletePageConfirm() throws Exception{
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.DELETEPAGE});
    wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
  }
  
  protected List<SearchResult> getRelativePages() {
    try{
      WikiPageParams params = Utils.getCurrentWikiPageParams() ;
      return wservice.searchRenamedPage(params.getType(), params.getOwner(), params.getPageId()) ;
    } catch (Exception e) {
      return new ArrayList<SearchResult>();
    }
  }
  
  protected PageImpl getCurrentPage() {
    try {
      WikiPageParams params = Utils.getCurrentWikiPageParams();
      pageID = params.getPageId();
      owner = params.getOwner();
      return (PageImpl) wservice.getPageById(params.getType(), params.getOwner(), params.getPageId());
    } catch (Exception e) {
      return null;
    }
  }
  
  protected String getCurrentPageId(){ return pageID ;}
  protected String getWiki(){ return owner ;}
  
  protected String getHomeURL() {
    return Util.getPortalRequestContext().getPortalURI() + "wiki";
  }
  
  static public class OKActionListener extends EventListener<UIWikiDeletePageConfirm> {
    public void execute(Event<UIWikiDeletePageConfirm> event) throws Exception {
      UIWikiDeletePageConfirm component = event.getSource() ;
      WikiService wService = (WikiService) PortalContainer.getComponent(WikiService.class);
      WikiPageParams params = Utils.getCurrentWikiPageParams() ;
      wService.deletePage(params.getType(), params.getOwner(), params.getPageId()) ;      
      
      component.getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.VIEW) ;
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiBreadCrumb breadcumb = wikiPortlet.findFirstComponentOfType(UIWikiBreadCrumb.class) ;
      PortalRequestContext prContext = Util.getPortalRequestContext();
      String parentURL = breadcumb.getParentURL() ;
      prContext.setResponseComplete(true);
      prContext.getResponse().sendRedirect(parentURL) ;      
    }
  }  
  
  static public class CancelActionListener extends EventListener<UIWikiDeletePageConfirm> {
    public void execute(Event<UIWikiDeletePageConfirm> event) throws Exception {
      UIWikiDeletePageConfirm component = event.getSource() ;
      //String pageId = event.getRequestContext().getRequestParameter(OBJECTID);
      component.getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.VIEW) ; 
      PortalRequestContext prContext = Util.getPortalRequestContext();
      prContext.setResponseComplete(true);
      prContext.getResponse().sendRedirect(Utils.getCurrentRequestURL()) ;
    }
  }
}
