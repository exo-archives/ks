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
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiHistorySpaceArea.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiHistorySpaceArea.ReturnViewModeActionListener.class)
  }
)
public class UIWikiHistorySpaceArea extends UIWikiContainer {

  public static final String RETURN_VIEW_MODE = "ReturnViewMode";

  public UIWikiHistorySpaceArea() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.SHOWHISTORY});
    
    addChild(UIWikiPageVersionsList.class, null, null).setRendered(true);
    addChild(UIWikiPageVersionsCompare.class, null, null).setRendered(false);
  }

  public static void viewRevision(Event<?> event) throws Exception {
    UIWikiPortlet wikiPortlet = ((UIComponent) event.getSource()).getAncestorOfType(UIWikiPortlet.class);
    UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
    String versionName = event.getRequestContext().getRequestParameter(OBJECTID);
    wikiPortlet.findFirstComponentOfType(UIWikiVersionSelect.class).setVersionName(versionName);
    pageContentArea.renderVersion();
    wikiPortlet.changeMode(WikiMode.VIEWREVISION);
  }
  
  static public class ReturnViewModeActionListener extends EventListener<UIWikiHistorySpaceArea> {
    @Override
    public void execute(Event<UIWikiHistorySpaceArea> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      wikiPortlet.changeMode(WikiMode.VIEW);
    }
  }

}
