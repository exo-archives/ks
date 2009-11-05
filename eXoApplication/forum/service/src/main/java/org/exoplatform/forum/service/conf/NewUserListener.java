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
package org.exoplatform.forum.service.conf;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {

  private static Log log = ExoLogger.getLogger(NewUserListener.class);

  public NewUserListener(InitParams params) throws Exception {

  }

  public void postSave(User user, boolean isNew) throws Exception {
    if (isNew) {
      try {
        UserProfile template = newDefaultProfileTemplte();
        getForumService().addMember(user, template);

      } catch (Exception e) {
        log.warn("Error while adding new forum member: " + e.getMessage(), e);
      }

    } else {

      try {
        getForumService().updateUserProfile(user);
      } catch (Exception e) {
        log.warn("Error while updating forum profile: " + e.getMessage(), e);
      }
    }
  }

  /**
   * @TODO implement by using  init-params
   * @return
   */
  private UserProfile newDefaultProfileTemplte() {
    return null;
  }

  private ForumService getForumService() {
    return (ForumService) ExoContainerContext.getCurrentContainer()
                                             .getComponentInstanceOfType(ForumService.class);
  }

  @Override
  public void postDelete(User user) throws Exception {
    try {
      getForumService().removeMember(user);
    } catch (Exception e) {
      log.warn("failed to remove member : " + e.getMessage(), e);
    }

  }
}
