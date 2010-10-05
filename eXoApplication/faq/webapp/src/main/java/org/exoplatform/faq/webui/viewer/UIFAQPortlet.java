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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.webui.viewer;

import javax.portlet.PortletMode;

import org.exoplatform.faq.webui.popup.UIFAQSettingForm;
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
 * 					tu.duy@exoplatform.com
 * Jun 24, 2009 - 2:26:16 AM
 */

@ComponentConfig(
		lifecycle = UIApplicationLifecycle.class,
		template = "app:/templates/faq/webui/UIFAQPortlet.gtmpl"
)
public class UIFAQPortlet extends UIPortletApplication {
	public UIFAQPortlet() throws Exception {
		addChild(UIViewer.class, null, null);
	}

	public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
		PortletRequestContext portletReqContext = (PortletRequestContext) context;
		if (portletReqContext.getApplicationMode() == PortletMode.VIEW) {
			if (getChild(UIViewer.class) == null) {
				if (getChild(UIFAQSettingForm.class) != null) {
					removeChild(UIFAQSettingForm.class);
				}
				if (getChild(UIViewer.class) == null) {
					addChild(UIViewer.class, null, null);
				}
			}
		} else if (portletReqContext.getApplicationMode() == PortletMode.EDIT) {
			try {
				if (getChild(UIViewer.class) != null) {
					removeChild(UIViewer.class);
				}
				if (getChild(UIFAQSettingForm.class) == null) {
					addChild(UIFAQSettingForm.class, null, null);
				}
			} catch (Exception e) {
				log.error("Child must exist:", e);
			}
		}
		super.processRender(app, context);
	}

	public void renderPopupMessages() throws Exception {
		UIPopupMessages popupMess = getUIPopupMessages();
		if (popupMess == null)
			return;
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
		popupMess.processRender(context);
	}

}
