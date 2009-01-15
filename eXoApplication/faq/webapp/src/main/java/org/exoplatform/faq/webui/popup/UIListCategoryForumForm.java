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
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.forum.service.Category;
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
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 12-01-2009 - 10:30:08  
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/UIListCategoryForumForm.gtmpl",
		events = {
			@EventConfig(listeners = UIListCategoryForumForm.CloseActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIListCategoryForumForm.AddCategoryActionListener.class, phase = Phase.DECODE)
		}
)
public class UIListCategoryForumForm extends UIForm implements UIPopupComponent{
	List<Category> listcate = new ArrayList<Category>();
	public UIListCategoryForumForm() {
	  
  }
	
	public List<Category> getListCategory() throws Exception {
	  ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	  SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
	  listcate = forumService.getCategories(sProvider);
	  return listcate;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
  private String[] getPathName(String id) throws Exception {
		for (Category cate : listcate) {
	    if(cate.getId().equals(id)) return new String[]{cate.getPath(),cate.getCategoryName()};
    }
		return null;
	}
	
	static	public class CloseActionListener extends EventListener<UIListCategoryForumForm> {
		public void execute(Event<UIListCategoryForumForm> event) throws Exception {
			UIListCategoryForumForm uiForm = event.getSource() ;
			try {
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (Exception e) {
	      UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
	      portlet.cancelAction();
      }
		}
	}
	
	static	public class AddCategoryActionListener extends EventListener<UIListCategoryForumForm> {
		public void execute(Event<UIListCategoryForumForm> event) throws Exception {
			UIListCategoryForumForm uiForm = event.getSource() ;
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID);
			UIFAQPortlet portlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			UISettingForm settingForm = portlet.findFirstComponentOfType(UISettingForm.class);
			settingForm.setPathCatygory(uiForm.getPathName(cateId));
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
