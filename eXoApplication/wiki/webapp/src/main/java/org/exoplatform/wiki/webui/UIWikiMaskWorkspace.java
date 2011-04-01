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
import java.util.List;

import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 31, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/UIMaskWorkspace.gtmpl",
  events = @EventConfig(phase = Phase.DECODE, listeners = UIMaskWorkspace.CloseActionListener.class)
)
public class UIWikiMaskWorkspace extends UIMaskWorkspace {
  public List<WikiMode> accept_Modes;
  
 
  public UIWikiMaskWorkspace() {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.EDITPAGE, WikiMode.ADDPAGE,
        WikiMode.ADDTEMPLATE, WikiMode.EDITTEMPLATE });
  }

  public void processRender(WebuiRequestContext context) throws Exception {

    WikiMode currentMode = getCurrentMode();    
    if (currentMode != null && accept_Modes.contains(currentMode))
      super.processRender(context);
  }

  public WikiMode getCurrentMode() {
    return getAncestorOfType(UIWikiPortlet.class).getWikiMode();
  }
}
