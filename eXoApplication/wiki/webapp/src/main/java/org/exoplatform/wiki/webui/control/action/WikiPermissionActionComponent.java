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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPermissionForm;
import org.exoplatform.wiki.webui.UIWikiPermissionForm.Scope;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.control.action.core.AbstractEventActionComponent;
import org.exoplatform.wiki.webui.control.filter.AdminSpacePermissionFilter;
import org.exoplatform.wiki.webui.control.listener.BrowseContainerActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Dec 29, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",
  events = {
    @EventConfig(listeners = WikiPermissionActionComponent.WikiPermissionActionListener.class)
  }
)
public class WikiPermissionActionComponent extends AbstractEventActionComponent {
  
  public static final String                   ACTION  = "WikiPermission";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new AdminSpacePermissionFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }

  @Override
  public String getActionName() {
    return ACTION;
  }

  @Override
  public boolean isAnchor() {
    return false;
  }

  public static class WikiPermissionActionListener extends BrowseContainerActionListener<WikiPermissionActionComponent> {
    @Override
    protected void processEvent(Event<WikiPermissionActionComponent> event) throws Exception {
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L1);
      UIWikiPermissionForm uiWikiPermissionForm = uiPopupContainer.createUIComponent(UIWikiPermissionForm.class, null, "UIWikiSpacePermissionForm");
      uiPopupContainer.activate(uiWikiPermissionForm, 800, 0);
      uiWikiPermissionForm.setScope(Scope.WIKI);
      WikiService wikiService = uiWikiPermissionForm.getApplicationComponent(WikiService.class);
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      uiWikiPermissionForm.setPermission(wikiService.getWikiPermission(pageParams.getType(), pageParams.getOwner()));
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
      super.processEvent(event);
    }
  }

}
