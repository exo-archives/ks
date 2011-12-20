/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.webui;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template = "app:/templates/forum/webui/UIPostRules.gtmpl"
)
public class UIPostRules extends UIContainer {
  private UserProfile userProfile;

  private boolean     canCreateNewThread = false;

  private boolean     canAddPost         = false;

  public boolean isCanCreateNewThread() {
    return canCreateNewThread;
  }

  public boolean isCanAddPost() {
    return canAddPost;
  }

  public UIPostRules() throws Exception {
  }

  protected UserProfile getUserProfile() throws Exception {
    if (this.userProfile == null) {
      try {
        this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
      } catch (Exception e) {
        ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
        userProfile = forumService.getDefaultUserProfile(UserHelper.getCurrentUser(), ForumUtils.EMPTY_STR);
      }
    }
    return this.userProfile;
  }

  public void setUserProfile(UserProfile userProfile) {
    this.userProfile = userProfile;
  }

  public void setLock(boolean isLock) {
    canCreateNewThread = !isLock;
    canAddPost = !isLock;
  }

  public void setCanAddPost(boolean canAddPost) {
    this.canAddPost = canAddPost;
  }

  public void setCanCreateNewThread(boolean canCreatThread) {
    this.canCreateNewThread = canCreatThread;
  }
}
