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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.utils.TreeUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 15 Nov 2010  
 */
@ComponentConfig(
                 lifecycle = Lifecycle.class,
                 template = "app:/templates/wiki/webui/UIWikiLocationContainer.gtmpl",
                 events = {
                     @EventConfig(listeners = UIWikiLocationContainer.ChangeNewLocationActionListener.class)                  
                   }
               )
public class UIWikiLocationContainer extends UIContainer {
  final static public String CURRENT_LOCATION   = "currentLocation";

  final static public String NEW_LOCATION       = "newLocation";

  final static public String CHANGE_NEWLOCATION = "ChangeNewLocation";
  
  public UIWikiLocationContainer() throws Exception {
    addChild(UIWikiBreadCrumb.class, null, CURRENT_LOCATION).setLink(false);
    addChild(UIWikiBreadCrumb.class, null, NEW_LOCATION).setLink(false);
  }
  
  static public class ChangeNewLocationActionListener extends EventListener<UIWikiLocationContainer> {
    public void execute(Event<UIWikiLocationContainer> event) throws Exception {

      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      UIWikiLocationContainer container = event.getSource();
      UIWikiBreadCrumb newlocation = container.getChildById(NEW_LOCATION);
      String value = event.getRequestContext().getRequestParameter(OBJECTID);
      value = TitleResolver.getId(value, false);
      WikiPageParams params = TreeUtils.getPageParamsFromPath(value);
      newlocation.setBreadCumbs(wikiService.getBreadcumb(params.getType(),
                                                         params.getOwner(),
                                                         params.getPageId()));
      event.getRequestContext().addUIComponentToUpdateByAjax(newlocation);
    }
  }
}
