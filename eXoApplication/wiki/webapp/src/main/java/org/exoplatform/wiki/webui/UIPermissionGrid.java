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
package org.exoplatform.wiki.webui;

import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.service.PermissionEntry;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Jan 4, 2011  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIPermissionGrid.gtmpl"
)
public class UIPermissionGrid extends UIContainer {

  private List<PermissionEntry> permissionEntries;
  
  public List<PermissionEntry> getPermissionEntries() {
    return permissionEntries;
  }

  public void setPermissionEntries(List<PermissionEntry> permissionEntries) throws Exception {
    this.permissionEntries = permissionEntries;
    getChildren().clear();
    if (this.permissionEntries == null) {
      return;
    }
    for (int i = 0; i < this.permissionEntries.size(); i++) {
      UIWikiPermissionEntry permissionEntry = addChild(UIWikiPermissionEntry.class, null, "UIWikiPermissionEntry" + String.valueOf(i)  );
      permissionEntry.setPermissionEntry(this.permissionEntries.get(i));
    }
  }
  
}
