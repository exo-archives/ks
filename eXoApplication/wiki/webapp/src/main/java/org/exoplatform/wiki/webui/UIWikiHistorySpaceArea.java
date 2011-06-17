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
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 13, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiHistorySpaceArea.gtmpl"
)
public class UIWikiHistorySpaceArea extends UIWikiContainer {
  
  public static final String VERSION_LIST_COMPONENT = "UIWikiHistorySpaceArea_UIWikiPageVersionsList";

  public UIWikiHistorySpaceArea() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.SHOWHISTORY,
        WikiMode.COMPAREREVISION });
    
    addChild(UIWikiPageVersionsList.class, null, VERSION_LIST_COMPONENT);
    addChild(UIWikiPageVersionsCompare.class, null, null);
  }

  public boolean isShowVersion() {
    return getChild(UIWikiPageVersionsList.class).isRendered();
  }

  public boolean isShowHistorySpace(){
    WikiMode mode = getAncestorOfType(UIWikiPortlet.class).getWikiMode();
    if(mode.equals(WikiMode.VIEWREVISION) || mode.equals(WikiMode.SHOWHISTORY)) return true;
    return false;
  }
  
  public static void viewRevision(Event<?> event) throws Exception {
    UIWikiPortlet wikiPortlet = ((UIComponent) event.getSource()).getAncestorOfType(UIWikiPortlet.class);
    UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
    String versionName = event.getRequestContext().getRequestParameter(OBJECTID);
    UIWikiVersionSelect wikiVersionSelect = pageContentArea.getChild(UIWikiVersionSelect.class);
    wikiVersionSelect.setVersionName(versionName);
    wikiPortlet.changeMode(WikiMode.VIEWREVISION);
  }

}
