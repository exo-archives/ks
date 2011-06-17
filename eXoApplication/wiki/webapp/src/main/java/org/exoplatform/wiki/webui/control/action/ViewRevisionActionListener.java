/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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

import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.webui.UIWikiPageContentArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiVersionSelect;
import org.exoplatform.wiki.webui.WikiMode;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 14 Jun 2011  
 */
public class ViewRevisionActionListener extends EventListener<UIComponent> {

  @Override
  public void execute(Event<UIComponent> event) throws Exception {
    UIWikiPortlet wikiPortlet = ((UIComponent) event.getSource()).getAncestorOfType(UIWikiPortlet.class);
    UIWikiPageContentArea pageContentArea = wikiPortlet.findFirstComponentOfType(UIWikiPageContentArea.class);
    String versionName = event.getRequestContext().getRequestParameter(UIComponent.OBJECTID);
    UIWikiVersionSelect wikiVersionSelect = pageContentArea.getChild(UIWikiVersionSelect.class);
    wikiVersionSelect.setVersionName(versionName);
    wikiPortlet.changeMode(WikiMode.VIEWREVISION);
  }

}
