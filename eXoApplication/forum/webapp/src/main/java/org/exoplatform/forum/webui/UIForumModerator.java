/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 21, 2009 - 2:35:02 AM  
 */
@ComponentConfig(
    template = "app:/templates/forum/webui/UIForumModerator.gtmpl",
    events = {
        @EventConfig(listeners = UIForumModerator.CreatedLinkActionListener.class )
    }
)
public class UIForumModerator extends UIContainer {
  private List<String> moderators   = new ArrayList<String>();

  ForumService         forumService;

  private long         role         = 3;

  public UIForumModerator() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
  }

  protected long getUserRole() {
    return role;
  }

  public void setUserRole(long role) {
    this.role = role;
  }

  protected List<String> getModeratorsForum() throws Exception {
    return moderators;
  }

  public void setModeratorsForum(List<String> moderators) {
    this.moderators = moderators;
  }

  protected String getActionViewInfoUser(String linkType, String userName) {
    return getAncestorOfType(UIForumPortlet.class).getPortletLink(linkType, userName);
  }

  protected String getScreenName(String userId) throws Exception {
    return forumService.getScreenName(userId);
  }

  static public class CreatedLinkActionListener extends EventListener<UIForumModerator> {
    public void execute(Event<UIForumModerator> event) throws Exception {
    }
  }
}
