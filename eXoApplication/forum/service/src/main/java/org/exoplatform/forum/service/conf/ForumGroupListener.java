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
package org.exoplatform.forum.service.conf;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupEventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 8, 2011  
 */
public class ForumGroupListener extends GroupEventListener {
  private static final Log log          = ExoLogger.getLogger(ForumGroupListener.class);

  private ForumService     forumService = null;

  public ForumGroupListener() {

  }

  private ForumService getForumService() {
    if (forumService == null) {
      forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    }
    return forumService;
  }

  public void preDelete(Group group) throws Exception {
    String groupId = group.getId();
    log.info("Calculate deleted group from forum: " + groupId);
    getForumService().calculateDeletedGroup(groupId, group.getGroupName());
  }

}
