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

import javax.portlet.ActionResponse;
import javax.portlet.PortletSession;
import javax.xml.namespace.QName;

import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template = "app:/templates/forum/webui/UIForumContainer.gtmpl"
)
public class UIForumContainer extends UIContainer {
  public UIForumContainer() throws Exception {
    addChild(UIForumDescription.class, null, null);
    addChild(UITopicContainer.class, null, null);
    addChild(UITopicDetailContainer.class, null, null).setRendered(false);
    addChild(UIForumSummary.class, null, null);
  }

  public void setIsRenderChild(boolean isRender) {
    getChild(UITopicContainer.class).setRendered(isRender);
    getChild(UITopicDetailContainer.class).setRendered(!isRender);
    getChild(UIForumSummary.class).setRendered(isRender);
    if (isRender) {
      PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
      PortletSession portletSession = pcontext.getRequest().getPortletSession();
      ActionResponse actionRes = null;
      if (pcontext.getResponse() instanceof ActionResponse) {
        actionRes = (ActionResponse) pcontext.getResponse();
      }
      ForumParameter param = new ForumParameter();
      param.setRenderQuickReply(false);
      param.setRenderPoll(false);
      if (actionRes != null) {
        actionRes.setEvent(new QName("QuickReplyEvent"), param);
        actionRes.setEvent(new QName("ForumPollEvent"), param);
      } else {
        portletSession.setAttribute(UIForumPortlet.QUICK_REPLY_EVENT_PARAMS, param, PortletSession.APPLICATION_SCOPE);
        portletSession.setAttribute(UIForumPortlet.FORUM_POLL_EVENT_PARAMS, param, PortletSession.APPLICATION_SCOPE);
      }
    }
  }
}
