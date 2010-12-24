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

import org.exoplatform.services.security.ConversationState;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WatchedMixin;
import org.exoplatform.wiki.webui.control.filter.IsEditModeFilter;
import org.exoplatform.wiki.webui.control.filter.IsUserFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 25, 2010  
 */
@ComponentConfig(     
      template = "app:/templates/wiki/webui/action/WatchPageActionComponent.gtmpl",
      events = { 
        @EventConfig(listeners = WatchPageActionComponent.WatchPageActionListener.class)
      }
)
public class WatchPageActionComponent extends UIComponent {
  
  private static final String                  WATCH_PAGE = "WatchPage";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsUserFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;    
  }
  
  public boolean detectWatched(boolean isChangeState) throws Exception {
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
      mixin.setWatchers(watchers);
      currentPage.setWatchedMixin(mixin);
    }
    return isWatched;
  }

  public static class WatchPageActionListener extends
                                             UIPageToolBarActionListener<WatchPageActionComponent> {
    @Override
    protected void processEvent(Event<WatchPageActionComponent> event) throws Exception {
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      boolean isWatched = event.getSource().detectWatched(true);
      if (isWatched) {
        uiApp.addMessage(new ApplicationMessage("WatchPageAction.msg.Stop-watching",
                                                null,
                                                ApplicationMessage.INFO));
      } else {
        uiApp.addMessage(new ApplicationMessage("WatchPageAction.msg.Start-watching",
                                                null,
                                                ApplicationMessage.INFO));
      }
      super.processEvent(event);
    }
  }
  
}
