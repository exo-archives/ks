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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.ext.impl;

import java.util.Map;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.social.webui.activity.BaseUIActivity;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 2, 2011  
 */

public class BaseKSActivity extends BaseUIActivity {
  public static final Log log = ExoLogger.getLogger(WikiUIActivity.class);

  public String getUriOfAuthor() {
    try {
      return new StringBuilder().append("<a href='").append(getOwnerIdentity().getProfile().getUrl()).append("'>")
                                .append(getOwnerIdentity().getProfile().getFullName()).append("</a>").toString();
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("Failed to get Url of user: %s", getOwnerIdentity().getProfile().getId()), e);
      }
    }
    return "";
  }

  public String getUserFullName(String userId) {
    return getOwnerIdentity().getProfile().getFullName();
  }

  public String getUserProfileUri(String userId) {
    return getOwnerIdentity().getProfile().getUrl();
  }

  public String getUserAvatarImageSource(String userId) {
    return getOwnerIdentity().getProfile().getAvatarUrl();
  }

  public String getSpaceAvatarImageSource(String spaceIdentityId) {
    try {
      String spaceId = getOwnerIdentity().getRemoteId();
      SpaceService spaceService = getApplicationComponent(SpaceService.class);
      Space space = spaceService.getSpaceById(spaceId);
      if (space != null) {
        return space.getAvatarUrl();
      }
    } catch (Exception e) {
      log.warn(String.format("Failed to getSpaceById: %s",spaceIdentityId), e);
    }
    return null;
  }
  
  public String getActivityParamValue(String key) {
    String value = null;
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      value = params.get(key);
    }
    return value != null ? value : "";
  }

}
