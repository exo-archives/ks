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
package org.exoplatform.forum.info;

import javax.portlet.PortletSession;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.popup.UIQuickReplyForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.web.application.RequestContext;
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
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/templates/forum/webui/info/UIForumQuickReplyPortlet.gtmpl",
    events = {
      @EventConfig(listeners = UIForumQuickReplyPortlet.QuickReplyEventActionListener.class)
    }
)
public class UIForumQuickReplyPortlet extends UIPortletApplication {
  private boolean isRenderChild = false;

  public UIForumQuickReplyPortlet() throws Exception {
    addChild(UIQuickReplyForm.class, null, null).setRendered(false);
    addChild(UIPopupAction.class, null, "UIForumPopupAction");
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    if (!ForumUtils.isAjaxRequest()) {
      PortletSession portletSession = ((PortletRequestContext) context).getRequest().getPortletSession();
      ForumParameter params = (ForumParameter) portletSession.getAttribute(UIForumPortlet.QUICK_REPLY_EVENT_PARAMS, PortletSession.APPLICATION_SCOPE);
      this.quickReplyFormInit(params);
    }
    super.processRender(app, context);
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    UIPopupAction popupAction = getChild(UIPopupAction.class);
    popupAction.deActivate();
    context.addUIComponentToUpdateByAjax(popupAction);
  }
  
  public void quickReplyFormInit(ForumParameter params) {
    this.isRenderChild = params.isRenderQuickReply();
    if (this.isRenderChild && params.getCategoryId() == null)
      this.isRenderChild = false;
    UIQuickReplyForm quickReplyForm = this.getChild(UIQuickReplyForm.class);
    if (this.isRenderChild) {
      quickReplyForm.setInitForm(params.getCategoryId(), params.getForumId(), params.getTopicId(), params.isModerator());
    }
    quickReplyForm.setRendered(this.isRenderChild);
  }

  static public class QuickReplyEventActionListener extends EventListener<UIForumQuickReplyPortlet> {
    public void execute(Event<UIForumQuickReplyPortlet> event) throws Exception {
      UIForumQuickReplyPortlet quickReplyPortlet = event.getSource();
      ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
      quickReplyPortlet.quickReplyFormInit(params);
      event.getRequestContext().addUIComponentToUpdateByAjax(quickReplyPortlet);
    }
  }
}
