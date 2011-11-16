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
package org.exoplatform.poll.webui;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletMode;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.poll.Utils;
import org.exoplatform.portal.config.UserACL;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityConstants;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SAS 
 * Author : Vu Duy Tu 
 *          tu.duy@exoplatform.com 
 * 24 June 2010, 08:00:59
 */

@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/templates/poll/webui/UIPollPortlet.gtmpl"
)
public class UIPollPortlet extends UIPortletApplication {
  private boolean isAdmin = false;

  private String      userId  = "";

  private PortletMode portletMode;
  public UIPollPortlet() throws Exception {
    addChild(UIPoll.class, null, null).setRendered(false);
    addChild(UIPollManagement.class, null, null).setRendered(true);
    addChild(UIPopupAction.class, null, "UIPollPopupAction");
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    PortletRequestContext portletReqContext = (PortletRequestContext) context;
    portletMode = portletReqContext.getApplicationMode();
    if (portletMode == PortletMode.VIEW) {
      UIPoll uipoll = getChild(UIPoll.class).setRendered(true);
      hasGroupAdminOfGatein();
      uipoll.setPollId();
      getChild(UIPollManagement.class).setRendered(false);
    } else if (portletMode == PortletMode.EDIT) {
      getChild(UIPoll.class).setRendered(false);
      ((UIPollManagement) getChild(UIPollManagement.class).setRendered(true)).updateGrid();
    }
    super.processRender(app, context);
  }

  public boolean isAdmin() {
    return isAdmin;
  }

  public String getUserId() {
    return userId;
  }

  private void hasGroupAdminOfGatein() {
    isAdmin = false;
    try {
      UserACL userACL = (UserACL) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(UserACL.class);
      List<String> list = new ArrayList<String>();
      Identity identity = ConversationState.getCurrent().getIdentity();
      userId = identity.getUserId();
      if (Utils.isEmpty(userId) || IdentityConstants.ANONIM.equals(userId)) {
        userId = UserHelper.getCurrentUser();
        if(!Utils.isEmpty(userId)) {
          list.add(userId);
        }
      } else {
        list.addAll(identity.getGroups());
      }
      for (String str : list) {
        if (str.equals(userACL.getSuperUser()) || str.equals(userACL.getAdminGroups()))
          isAdmin = true;
      }
    } catch (Exception e) {
      log.debug("Failed to check permision for user by component UserACL", e);
    }
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = RequestContext.getCurrentInstance();
    UIPopupAction popupAction = getChild(UIPopupAction.class);
    popupAction.deActivate();
    context.addUIComponentToUpdateByAjax(popupAction);
  }

}
