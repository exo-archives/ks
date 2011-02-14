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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.wiki.service.Permission;
import org.exoplatform.wiki.service.PermissionEntry;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Jan 4, 2011  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPermissionEntry.gtmpl"
)
public class UIWikiPermissionEntry extends UIContainer {

  private PermissionEntry permissionEntry;

  public PermissionEntry getPermissionEntry() {
    return permissionEntry;
  }

  public void setPermissionEntry(PermissionEntry permissionEntry) {
    this.permissionEntry = permissionEntry;
    getChildren().clear();
    if (this.permissionEntry == null) {
      return;
    }
    Permission[] permissions = this.permissionEntry.getPermissions();
    for (int i = 0; i < permissions.length; i++) {
      addChild((UIComponent) new UIFormCheckBoxInput<Boolean>(permissions[i].getPermissionType()
                                                                            .toString()
          + this.permissionEntry.getId(), "", permissions[i].isAllowed()).setValue(permissions[i].isAllowed()));
    }
  }
  
}
