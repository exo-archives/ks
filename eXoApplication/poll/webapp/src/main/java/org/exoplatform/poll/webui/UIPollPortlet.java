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
package org.exoplatform.poll.webui;

import javax.portlet.PortletMode;

import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
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

	public UIPollPortlet() throws Exception {
		addChild(UIPoll.class, null, null).setRendered(false);
		addChild(UIPollManagement.class, null, null).setRendered(true);
		addChild(UIPopupAction.class, null, "UIPollPopupAction");
	}

	public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
		PortletRequestContext portletReqContext = (PortletRequestContext) context;
		if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
			UIPoll uipoll = getChild(UIPoll.class).setRendered(true);
			uipoll.setPollId();
			getChild(UIPollManagement.class).setRendered(false);
		} else if (portletReqContext.getApplicationMode() == PortletMode.EDIT) {
			getChild(UIPoll.class).setRendered(false);
			((UIPollManagement) getChild(UIPollManagement.class).setRendered(true)).updateGrid();
		}
		super.processRender(app, context);
	}

	public void renderPopupMessages() throws Exception {
		UIPopupMessages popupMess = getUIPopupMessages();
		if (popupMess == null)
			return;
		WebuiRequestContext context = RequestContext.getCurrentInstance();
		popupMess.processRender(context);
	}

	public boolean isAdmin() {
		return isAdmin;
	}

	public void cancelAction() throws Exception {
		WebuiRequestContext context = RequestContext.getCurrentInstance();
		UIPopupAction popupAction = getChild(UIPopupAction.class);
		popupAction.deActivate();
		context.addUIComponentToUpdateByAjax(popupAction);
	}

}
