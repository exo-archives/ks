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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.webui.form.input.UICheckBoxInput;
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
  private static final String ANY_OWNER = "any";
  
  private static final Log log = ExoLogger.getLogger(UIWikiPermissionEntry.class);

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
      
      
      addChild((UIComponent) new UICheckBoxInput(permissions[i].getPermissionType()
                                                                            .toString()
          + this.permissionEntry.getId(), "", permissions[i].isAllowed()).setValue(permissions[i].isAllowed()));
    }
  }
  
  public String getEntryFullName() {
    if (permissionEntry.getFullName() != null) {
      return permissionEntry.getFullName();
    }
    
    String id = permissionEntry.getId();
    if (ANY_OWNER.equals(id)) {
      permissionEntry.setFullName(id);
      return permissionEntry.getFullName();
    }

    OrganizationService organizationService = (OrganizationService) getApplicationComponent(OrganizationService.class);
    try {
      switch (permissionEntry.getIdType()) {
      case USER:
        UserHandler userHandler = organizationService.getUserHandler();
        permissionEntry.setFullName(userHandler.findUserByName(id).getFullName());
        break;
      case GROUP:
        GroupHandler groupHandler = organizationService.getGroupHandler();
        permissionEntry.setFullName(groupHandler.findGroupById(id).getGroupName());
        break;
      case MEMBERSHIP:
        int index = id.indexOf(':');
        if (index == -1) {
          permissionEntry.setFullName(id);
        } else {
          String membership = id.split(":")[0];
          String groupId = id.split(":")[1];
          
          GroupHandler groupHandler1 = organizationService.getGroupHandler();
          String groupName = groupHandler1.findGroupById(groupId).getGroupName();
          
          // Uppercase the first char
          groupName = groupName.substring(0, 1).toUpperCase() + groupName.substring(1);
          
          WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
          String key = context.getApplicationResourceBundle().getString("UIWikiPermissionForm.PermissionEntry.fullName");
          permissionEntry.setFullName(key.replace("{0}", membership).replace("{1}", groupName));
        }
        break;
      }
    } catch (Exception ex) {
      if (log.isDebugEnabled()) {
        log.debug("Exception when determineFullName", ex);
      }
    }
    return permissionEntry.getFullName();
  }
}
