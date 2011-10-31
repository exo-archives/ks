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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.resource.SkinConfig;
import org.exoplatform.portal.resource.SkinService;
import org.exoplatform.portal.webui.application.UIPortlet;
import org.exoplatform.portal.webui.page.UIPageBody;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WatchedMixin;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.webui.control.filter.IsUserFilter;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.MoreContainerActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 25, 2010  
 */
@ComponentConfig(     
      template = "app:/templates/wiki/webui/control/action/WatchPageActionComponent.gtmpl",
      events = { 
        @EventConfig(listeners = WatchPageActionComponent.WatchPageActionListener.class)
      }
)
public class WatchPageActionComponent extends UIComponent {
  
  protected static final String                  WATCH_PAGE = "WatchPage";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsUserFilter(), new IsViewModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;    
  }
  
  public boolean detectWatched(boolean isChangeState) throws Exception {
    RenderingService renderingService = (RenderingService) ExoContainerContext.getCurrentContainer()
                                                                              .getComponentInstanceOfType(RenderingService.class);
    ConversationState conversationState = ConversationState.getCurrent();
    String currentUserId = conversationState.getIdentity().getUserId();
    PageImpl currentPage = (PageImpl) Utils.getCurrentWikiPage();
    currentPage.makeWatched();
    WatchedMixin mixin = currentPage.getWatchedMixin();
    List<String> watchers = mixin.getWatchers();
    boolean isWatched = false;
    for (String watcher : watchers) {
      if (watcher.equals(currentUserId))
        isWatched = true;
    }
    for (String watcher : watchers) {
      if (watcher.equals(currentUserId))
        isWatched = true;
    }
    if (isChangeState) {
      if (isWatched) {
        // Stop watching
        watchers.remove(currentUserId);
      } else {
        // Begin watching
        watchers.add(currentUserId);        
      }
      if (renderingService.getCssURL() == null) {
        renderingService.setCssURL(getPortletCssLink());
      }
      mixin.setWatchers(watchers);
      currentPage.setWatchedMixin(mixin);
    }
    return isWatched;
  }

  private String getPortletCssLink() {
    SkinService skinService = (SkinService) PortalContainer.getComponent(SkinService.class);
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String requestURL = portalRequestContext.getRequest().getRequestURL().toString();
    String portalURI = portalRequestContext.getPortalURI();
    String domainURL = requestURL.substring(0, requestURL.indexOf(portalURI));
    UIPortal uiPortal = Util.getUIPortal();
    UIPortalApplication uiPortalApp = uiPortal.getAncestorOfType(UIPortalApplication.class);
    String currentSkin = uiPortalApp.getSkin();
    UIPageBody uiPageBody = uiPortal.findFirstComponentOfType(UIPageBody.class);
    if (uiPageBody != null) {
      UIPortlet currentPortlet = (UIPortlet) uiPageBody.findFirstComponentOfType(UIPortlet.class);
      SkinConfig skinConfig = skinService.getSkin(currentPortlet.getSkinId(), currentSkin);
      StringBuilder sb = new StringBuilder(domainURL);
      sb.append(skinConfig.getCSSPath());
      return sb.toString();
    }
    return null;
  }

  public static class WatchPageActionListener extends
                                             MoreContainerActionListener<WatchPageActionComponent> {
    @Override
    protected void processEvent(Event<WatchPageActionComponent> event) throws Exception {
      boolean isWatched = event.getSource().detectWatched(true);
      if (isWatched) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("WatchPageAction.msg.Stop-watching",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));
      } else {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("WatchPageAction.msg.Start-watching",
                                                                                       null,
                                                                                       ApplicationMessage.INFO));
      }
      super.processEvent(event);
    }
  }
  
}
