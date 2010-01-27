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
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
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
		template = "app:/templates/faq/webui/popup/UISelectCategoryForumForm.gtmpl",
		events = {
			@EventConfig(listeners = UISelectCategoryForumForm.CloseActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UISelectCategoryForumForm.AddCategoryActionListener.class, phase = Phase.DECODE)
		}
)
public class UISelectCategoryForumForm extends UIForm implements UIPopupComponent{
	private List<Category> listcate = new ArrayList<Category>();
	private ForumService forumService ;
	public UISelectCategoryForumForm() {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  }
	
  public void setListCategory() throws Exception {
  	listcate = forumService.getCategories();
	}
	
  List<Forum> getForums(String categoryId) {
  	List<Forum>listForum = new ArrayList<Forum>();
		if (categoryId != null && categoryId.trim().length() > 0) {
			try {
				String strQuery = "@exo:isClosed='false' and @exo:isLock='false'";
				listForum = forumService.getForums(categoryId, strQuery);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		return listForum;
	}
  
	@SuppressWarnings("unused")
	private List<Category> getCategories() throws Exception {
		return  this.listcate;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
  private List<String> getPathName(String allPath) throws Exception {
		int t = allPath.indexOf(";");
		List<String> list = new ArrayList<String>();
		if(t > 0){
			list.add(allPath.substring(0, t));
			list.add(allPath.substring(t+1));
		}
		return list;
	}
	
	static	public class CloseActionListener extends EventListener<UISelectCategoryForumForm> {
		public void execute(Event<UISelectCategoryForumForm> event) throws Exception {
			UISelectCategoryForumForm uiForm = event.getSource() ;
			try {
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
      } catch (Exception e) {
	      UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class);
	      portlet.cancelAction();
      }
		}
	}
	
	static	public class AddCategoryActionListener extends EventListener<UISelectCategoryForumForm> {
		public void execute(Event<UISelectCategoryForumForm> event) throws Exception {
			UISelectCategoryForumForm uiForm = event.getSource() ;
			String allPath = event.getRequestContext().getRequestParameter(OBJECTID);
			UIAnswersPortlet portlet = uiForm.getAncestorOfType(UIAnswersPortlet.class);
			UISettingForm settingForm = portlet.findFirstComponentOfType(UISettingForm.class);
			settingForm.setPathCatygory(uiForm.getPathName(allPath));
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
