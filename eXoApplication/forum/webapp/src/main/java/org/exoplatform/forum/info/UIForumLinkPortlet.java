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
package org.exoplatform.forum.info;

import javax.portlet.PortletSession;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletApplication;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Apr 23, 2009 - 7:42:07 AM  
 */

@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/forum/webui/info/UIForumLinkPortlet.gtmpl",
   events = {
       @EventConfig(listeners = UIForumLinkPortlet.ForumLinkEventActionListener.class)
   }
)
public class UIForumLinkPortlet extends UIPortletApplication {
  private boolean isRenderChild = false;

  public UIForumLinkPortlet() throws Exception {
    addChild(UIForumLinks.class, null, null).setRendered(isRenderChild);
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    if (!ForumUtils.isAjaxRequest()) {
      PortletSession portletSession = ((PortletRequestContext) context).getRequest().getPortletSession();
      ForumParameter params = (ForumParameter) portletSession.getAttribute(UIForumPortlet.FORUM_LINK_EVENT_PARAMS, PortletSession.APPLICATION_SCOPE);
      this.initUI(params);
    }
    super.processRender(app, context);
  }

  public boolean isRenderChild() {
    return isRenderChild;
  }

  public void setRenderChild(boolean isRenderChild) {
    this.isRenderChild = isRenderChild;
  }

  public void initUI(ForumParameter params) {
    this.isRenderChild = params.isRenderForumLink();
    UIForumLinks forumLink = this.getChild(UIForumLinks.class);
    forumLink.setValueOption(params.getPath());
    forumLink.setRendered(this.isRenderChild);
  }
  
  static public class ForumLinkEventActionListener extends EventListener<UIForumLinkPortlet> {
    public void execute(Event<UIForumLinkPortlet> event) throws Exception {
      UIForumLinkPortlet forumLinkPortlet = event.getSource();
      ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
      forumLinkPortlet.initUI(params);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumLinkPortlet);
    }
  }
}
