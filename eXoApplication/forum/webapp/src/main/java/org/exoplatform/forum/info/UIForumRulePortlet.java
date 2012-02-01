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

import java.util.List;

import javax.portlet.PortletSession;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UIPostRules;
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
 * Author : Hung Nguyen Quang *          hung.nguyen@exoplatform.com
 * Mar 04, 2008
 */

@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/forum/webui/info/UIForumRulePortlet.gtmpl",
   events = {
        @EventConfig(listeners = UIForumRulePortlet.ForumRuleEventActionListener.class)
    }
)
public class UIForumRulePortlet extends UIPortletApplication {
  private boolean isRenderChild = false;

  public UIForumRulePortlet() throws Exception {
    addChild(UIPostRules.class, null, null).setRendered(isRenderChild);
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    if (!ForumUtils.isAjaxRequest()) {
      PortletSession portletSession = ((PortletRequestContext) context).getRequest().getPortletSession();
      ForumParameter params = (ForumParameter) portletSession.getAttribute(UIForumPortlet.RULE_EVENT_PARAMS, PortletSession.APPLICATION_SCOPE);
      this.postRulesInit(params);
    }
    super.processRender(app, context);
  }

  public void postRulesInit(ForumParameter params) {
    this.isRenderChild = params.isRenderRule();
    UIPostRules postRules = this.getChild(UIPostRules.class);
    List<String> infoRules = params.getInfoRules();
    postRules.setCanCreateNewThread(Boolean.parseBoolean(infoRules.get(1)));
    postRules.setCanAddPost(Boolean.parseBoolean(infoRules.get(2)));
    if (!ForumUtils.isEmpty(infoRules.get(0))) {
      postRules.setLock(Boolean.parseBoolean(infoRules.get(0)));
    }
    postRules.setRendered(this.isRenderChild);
  }
  
  static public class ForumRuleEventActionListener extends EventListener<UIForumRulePortlet> {
    public void execute(Event<UIForumRulePortlet> event) throws Exception {
      UIForumRulePortlet forumRulePortlet = event.getSource();
      ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
      forumRulePortlet.postRulesInit(params);
      event.getRequestContext().addUIComponentToUpdateByAjax(forumRulePortlet);
    }
  }
}
