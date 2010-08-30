/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.wiki.tree ;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.webui.popup.UIWikiMovePageForm;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 6, 2010  
 */
@ComponentConfig(template = "app:/templates/wiki/webui/tree/UITreeExplorer.gtmpl")
public class UITreeExplorer extends UIContainer {

  public UITreeExplorer() throws Exception {
  }

  public String getRestUrl() {
    StringBuilder sb = new StringBuilder();
    sb.append("/").append(PortalContainer.getCurrentPortalContainerName()).append("/");
    sb.append(PortalContainer.getCurrentRestContextName()).append("/wiki/tree/");
    return sb.toString();
  }
  
  public String getCurrentPagePath() {
    UIWikiMovePageForm movePageForm = this.getAncestorOfType(UIWikiMovePageForm.class);
    UIFormStringInput currentLocationInput = movePageForm.getUIStringInput(movePageForm.CURRENT_LOCATION);
    return currentLocationInput.getValue();
  }
  
}
