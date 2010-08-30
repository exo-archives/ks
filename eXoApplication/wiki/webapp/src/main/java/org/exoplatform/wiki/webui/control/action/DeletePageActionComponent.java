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
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiBreadCrumb;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = DeletePageActionComponent.DeletePageActionListener.class)
  }
)
public class DeletePageActionComponent extends UIComponent {
  
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsViewModeFilter() });

  public DeletePageActionComponent() {
    
  }
  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class DeletePageActionListener extends UIPageToolBarActionListener<DeletePageActionComponent> {
    @Override
    protected void processEvent(Event<DeletePageActionComponent> event) throws Exception {
      String requestURL = Utils.getCurrentRequestURL();
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
      WikiPageParams params = pageResolver.extractWikiPageParams(requestURL) ;
      
      UIWikiBreadCrumb breadcumb = wikiPortlet.findFirstComponentOfType(UIWikiBreadCrumb.class) ;
      PortalRequestContext prContext = Util.getPortalRequestContext();
      String parentURL = breadcumb.getParentURL() ;
      if(WikiNodeType.Definition.WIKI_HOME_NAME.equals(params.getPageId())) {
        uiApp.addMessage(new ApplicationMessage("DeletePageAction.msg.Warning", null, ApplicationMessage.WARNING));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        prContext.getResponse().sendRedirect(parentURL);
        return ;        
      }      
      wikiPortlet.changeMode(WikiMode.DELETE_CONFIRM) ;
    }
  }
}
