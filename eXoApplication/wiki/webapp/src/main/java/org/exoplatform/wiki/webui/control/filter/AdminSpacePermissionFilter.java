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
package org.exoplatform.wiki.webui.control.filter;

import java.util.Map;

import org.exoplatform.portal.config.UserACL;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.ext.filter.UIExtensionAbstractFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.PermissionType;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Jan 17, 2011  
 */
public class AdminSpacePermissionFilter extends UIExtensionAbstractFilter {

  public AdminSpacePermissionFilter() {
    this(null);
  }

  public AdminSpacePermissionFilter(String messageKey) {
    super(messageKey, UIExtensionFilterType.MANDATORY);
  }

  @Override
  public boolean accept(Map<String, Object> context) throws Exception {
    // If an user is the super user or in the administration group or has the
    // create portal permission then he also has the admin space permission
    UserACL userACL = Util.getUIPortalApplication().getApplicationComponent(UserACL.class);
    if (userACL.hasCreatePortalPermission()) {
      return true;
    }
    String expAdminGroup = userACL.getAdminGroups();
    if (expAdminGroup != null) {
      expAdminGroup = expAdminGroup.startsWith("/") ? expAdminGroup : "/" + expAdminGroup;
      if (userACL.isUserInGroup(expAdminGroup)) {
        return true;
      }
    }

    return Utils.hasPermission(new String[] { PermissionType.ADMINSPACE.toString() });
  }

  @Override
  public void onDeny(Map<String, Object> context) throws Exception {
    // TODO Auto-generated method stub

  }

}
