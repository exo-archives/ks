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
package org.exoplatform.wiki.webui.core;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 31, 2010  
 */
public class UIWikiComponent extends UIComponent {
  protected List<WikiMode> accept_Modes = new ArrayList<WikiMode>();

  @Override
  public void processRender(WebuiRequestContext context) throws Exception {

    WikiMode currentMode = getCurrentMode();    
    if (currentMode != null && accept_Modes.contains(currentMode))
      super.processRender(context);
  }

  public WikiMode getCurrentMode() {
    return getAncestorOfType(UIWikiPortlet.class).getWikiMode();
  }

  public List<WikiMode> getAccept_Modes() {
    return accept_Modes;
  }

  public void setAccept_Modes(List<WikiMode> acceptModes) {
    accept_Modes = acceptModes;
  }
}
