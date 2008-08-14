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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIMoveForumForm.gtmpl",
		events = {
			@EventConfig(listeners = UIMoveForumForm.SaveActionListener.class), 
			@EventConfig(listeners = UIMoveForumForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIMoveForumForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_CATEGORY_SELECTBOX = "SelectCategory" ;
	private List<Forum> forums_ ;
	private String categoryId_ ;
	private String newCategoryId_ ;
	private boolean isForumUpdate = false;
	
	public void setListForum(List<Forum> forums, String categoryId) {
		forums_ = forums ;
		categoryId_ = categoryId ;
	}
	
	public UIMoveForumForm() throws Exception {
	}
	
	public void setForumUpdate(boolean isForumUpdate) {
		this.isForumUpdate = isForumUpdate ;
	}
	
	@SuppressWarnings("unused")
  private List<Category> getCategories() throws Exception {
		ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		List<Category> categorys =	new ArrayList<Category>();
		for (Category category :forumService.getCategories(ForumSessionUtils.getSystemProvider())) {
			if( !category.getId().equals(categoryId_) ) {
				categorys.add(category) ;
			}
		}
		return categorys ;
	}
	
	@SuppressWarnings("unused")
  private boolean getSeclectedCategory(String cactegoryId) throws Exception {
		if(cactegoryId.equalsIgnoreCase(this.newCategoryId_)) return true;
		else return false ;
	}
	
	public void activate() throws Exception {
	}
	public void deActivate() throws Exception {
	}
	
	static	public class SaveActionListener extends EventListener<UIMoveForumForm> {
    public void execute(Event<UIMoveForumForm> event) throws Exception {
			UIMoveForumForm uiForm = event.getSource() ;
			ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			String categoryPath = event.getRequestContext().getRequestParameter(OBJECTID);
			List<Forum> forums = uiForm.forums_ ;
			String categoryId = categoryPath.substring((categoryPath.lastIndexOf("/")+1))	;
			try {
				forumService.moveForum(ForumSessionUtils.getSystemProvider(), forums, categoryPath) ;
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
				if(uiForm.isForumUpdate) {
					forumPortlet.updateIsRendered(ForumUtils.FORUM);
					UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
					uiForumContainer.setIsRenderChild(true) ;
					UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class);
					uiForumContainer.getChild(UIForumDescription.class).setForum(forums.get(0));
					uiTopicContainer.setUpdateForum(categoryId, forums.get(0)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				} else {
					UICategory uiCategory = forumPortlet.findFirstComponentOfType(UICategory.class);
					event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory) ;
				}
      } catch (Exception e) {
      	UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      	uiApp.addMessage(new ApplicationMessage("UIMoveForumForm.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
      	return;
      }
		}
	}
 
	static	public class CancelActionListener extends EventListener<UIMoveForumForm> {
    public void execute(Event<UIMoveForumForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}