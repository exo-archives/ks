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
package org.exoplatform.forum.webui.popup;

import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template = "app:/templates/forum/webui/popup/UIRSSForm.gtmpl",
		events = {
				@EventConfig(listeners = UIRSSForm.CancelActionListener.class)
		}
)
public class UIRSSForm extends UIForm	{
	private String rssLink;
	 
	public UIRSSForm() throws Exception {
	}
	
	public void setRSSLink(String rssLink){
		PortalRequestContext portalContext = Util.getPortalRequestContext();
		String url = portalContext.getRequest().getRequestURL().toString();
		url = url.replaceFirst("http://", "") ;
		url = url.substring(0, url.indexOf("/")) ;
		url = "http://" + url;
		this.rssLink = url + rssLink;
	}

	static public class CancelActionListener extends EventListener<UIRSSForm> {
		public void execute(Event<UIRSSForm> event) throws Exception {
			UIRSSForm commentForm = event.getSource() ;
			UIForumPortlet portlet = commentForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}