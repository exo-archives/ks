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
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template = "app:/templates/forum/webui/UIForumDescription.gtmpl"
)
public class UIForumDescription extends UIContainer {
  private String  forumId;

  private String  categoryId;

  private Forum   forum   = null;

  private boolean isForum = false;

  private Log     log     = ExoLogger.getLogger(UIForumDescription.class);

  public UIForumDescription() throws Exception {
  }

  public void setForum(Forum forum) {
    this.forum = forum;
    this.isForum = false;
  }

  public void setForumIds(String categoryId, String forumId) {
    this.isForum = true;
    this.forumId = forumId;
    this.categoryId = categoryId;
  }

  protected Forum getForum() throws Exception {
    if (forum == null || isForum) {
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      try {
        return forumService.getForum(categoryId, forumId);
      } catch (Exception e) {
        log.debug(forumId + " must exist: " + e.getMessage() + "\n" + e.getCause());
        return null;
      }
    } else {
      return this.forum;
    }
  }
}
