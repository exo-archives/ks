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

import java.util.Arrays;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.WikiPortletPreference;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 7 Dec 2010  
 */
@ComponentConfig(
                 lifecycle = Lifecycle.class,
                 template = "app:/templates/wiki/webui/UIWikiMiddleArea.gtmpl",
                 events = {
                     @EventConfig(listeners = UIWikiMiddleArea.ShowHideActionListener.class)                     
                   }
               )
public class UIWikiMiddleArea extends UIWikiContainer {

  public static String SHOW_HIDE_ACTION = "ShowHide";

  public UIWikiMiddleArea() throws Exception {
    super();
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.EDITPAGE,
        WikiMode.ADDPAGE, WikiMode.ADVANCEDSEARCH, WikiMode.SHOWHISTORY, WikiMode.PAGE_NOT_FOUND,
        WikiMode.HELP, WikiMode.DELETEPAGE, WikiMode.VIEWREVISION, WikiMode.PAGEINFO,
        WikiMode.ADDTEMPLATE, WikiMode.EDITTEMPLATE, WikiMode.COMPAREREVISION, WikiMode.SPACESETTING });
    addChild(UIWikiNavigationContainer.class, null, null);
    addChild(UIWikiPageContainer.class, null, null);
    addChild(UIWikiPageSettingContainer.class, null, null);
  }
  
  protected boolean isNavigationRender() {
    WikiPortletPreference preferences = this.getAncestorOfType(UIWikiPortlet.class).getPortletPreferences();
    UIWikiNavigationContainer navigation = getChild(UIWikiNavigationContainer.class);
    return (navigation.getAccept_Modes().contains(navigation.getCurrentMode())
        && navigation.isRendered() && preferences.isShowNavigationTree());
  }
  
  protected boolean isPageSettingContainerRender() {
    UIWikiPageSettingContainer settingContainer = getChild(UIWikiPageSettingContainer.class);
    return (settingContainer.getAccept_Modes().contains(settingContainer.getCurrentMode()) && settingContainer.isRendered());
  }

  public static class ShowHideActionListener extends EventListener<UIWikiMiddleArea> {
    @Override
    public void execute(Event<UIWikiMiddleArea> event) throws Exception {
      UIWikiMiddleArea middleArea = event.getSource();
      UIWikiNavigationContainer navigation = middleArea.getChild(UIWikiNavigationContainer.class);
      navigation.setRendered(!navigation.isRendered());
      event.getRequestContext().addUIComponentToUpdateByAjax(middleArea);
    }
  }
}
