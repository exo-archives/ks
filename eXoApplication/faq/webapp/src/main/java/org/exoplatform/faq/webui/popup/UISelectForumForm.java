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
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu 
 *          tu.duy@exoplatform.com
 * 14-01-2009 - 04:20:05
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class, 
		template = "app:/templates/faq/webui/popup/UISelectForumForm.gtmpl", 
		events = {
	    @EventConfig(listeners = UISelectForumForm.CloseActionListener.class, phase = Phase.DECODE),
	    @EventConfig(listeners = UISelectForumForm.AddForumActionListener.class, phase = Phase.DECODE) 
		}
)
    
public class UISelectForumForm extends UIForm implements UIPopupComponent {
	private String categoryId;
	private List<Forum> listForum;
	public UISelectForumForm() {
	}
	
	public void activate() throws Exception {	}
	public void deActivate() throws Exception {	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public List<Forum> getListForum() {
		listForum = new ArrayList<Forum>();
		if (categoryId != null && categoryId.trim().length() > 0) {
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
				String strQuery = "@exo:isClosed='false' and @exo:isLock='false'";
				listForum = forumService.getForums(sProvider, categoryId, strQuery);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				sProvider.close();
			}
		}
		return listForum;
	}
	
	private String[] getPathName(String id) throws Exception {
		for (Forum forum : listForum) {
	    if(forum.getId().equals(id)) return new String[]{forum.getId(),forum.getForumName()};
    }
		return null;
	}
	
	static public class CloseActionListener extends EventListener<UISelectForumForm> {
		public void execute(Event<UISelectForumForm> event) throws Exception {
			UISelectForumForm uiForm = event.getSource();
			UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			try {
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (Exception e) {
	      portlet.cancelAction();
      }
		}
	}

	static public class AddForumActionListener extends EventListener<UISelectForumForm> {
    public void execute(Event<UISelectForumForm> event) throws Exception {
			UISelectForumForm uiForm = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			UISettingForm settingForm = portlet.findFirstComponentOfType(UISettingForm.class);
			settingForm.setIdForum(uiForm.getPathName(forumId));
			event.getRequestContext().addUIComponentToUpdateByAjax(settingForm) ;
			try {
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (Exception e) {
	      portlet.cancelAction();
      }
		}
	}
}
