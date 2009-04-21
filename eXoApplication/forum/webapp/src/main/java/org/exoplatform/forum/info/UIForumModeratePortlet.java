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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.info;

import org.exoplatform.forum.webui.UIForumModerator;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletApplication;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008
 */

@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/forum/webui/info/UIForumModeratePortlet.gtmpl",
   events = {
     	@EventConfig(listeners = UIForumModeratePortlet.ForumModerateEventActionListener.class)
   }
)

public class UIForumModeratePortlet extends UIPortletApplication {
	private boolean isRenderChild = false;
	public UIForumModeratePortlet() throws Exception {
		addChild(UIForumModerator.class, null, null).setRendered(isRenderChild);
  }
  
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
    super.processRender(app, context) ;
  }  
  
  static public class ForumModerateEventActionListener extends EventListener<UIForumModeratePortlet> {
		public void execute(Event<UIForumModeratePortlet> event) throws Exception {
			UIForumModeratePortlet forumModeratePortlet = event.getSource();
			ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
			forumModeratePortlet.isRenderChild = params.isRenderModerator();
			UIForumModerator quickReplyForm = forumModeratePortlet.getChild(UIForumModerator.class);
			quickReplyForm.setModeratorsForum(params.getModerators());
			quickReplyForm.setRendered(forumModeratePortlet.isRenderChild);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumModeratePortlet);
		}
	}
} 
