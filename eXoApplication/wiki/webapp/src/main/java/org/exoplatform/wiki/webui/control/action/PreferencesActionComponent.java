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
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.control.action.core.AbstractEventActionComponent;
import org.exoplatform.wiki.webui.control.filter.AdminSpacePermissionFilter;
import org.exoplatform.wiki.webui.control.listener.BrowseContainerActionListener;
import org.exoplatform.wiki.webui.popup.UIWikiSettingContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 25, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",                
  events = {
    @EventConfig(listeners = PreferencesActionComponent.PreferencesActionListener.class) 
  }
)
public class PreferencesActionComponent extends AbstractEventActionComponent {
  
  public static final String                   ACTION  = "Preferences";
  
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
  
  public static class PreferencesActionListener extends BrowseContainerActionListener<PreferencesActionComponent> {
    @Override
    protected void processEvent(Event<PreferencesActionComponent> event) throws Exception {
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L1);
      uiPopupContainer.activate(UIWikiSettingContainer.class, 800);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer);
      super.processEvent(event);
    }
  }

  
}
