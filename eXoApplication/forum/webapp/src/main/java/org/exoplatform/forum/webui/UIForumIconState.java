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

import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.webui.application.portlet.PortletApplication;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template = "app:/templates/forum/webui/UIForumIconState.gtmpl",
    events = {
      @EventConfig(listeners = UIForumIconState.IconStateParamActionListener.class)      
    }  
)
public class UIForumIconState extends UIContainer {
  private boolean isForumIcon = true;

  public UIForumIconState() throws Exception {
  }

  public void updateInfor(boolean isIconForum) {
    this.isForumIcon = isIconForum;
  }

  public boolean getIsIconForum() {
    return this.isForumIcon;
  }

  static public class IconStateParamActionListener extends EventListener<UIForumIconState> {
    public void execute(Event<UIForumIconState> event) throws Exception {
      UIForumIconState forumIconState = event.getSource();
      ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
      forumIconState.isForumIcon = params.isForumIcon();
    }
  }
}
