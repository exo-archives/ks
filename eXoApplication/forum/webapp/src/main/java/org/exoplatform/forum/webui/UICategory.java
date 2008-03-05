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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/forum/webui/UICategory.gtmpl",
		events = {
				@EventConfig(listeners = UICategory.EditCategoryActionListener.class),
				@EventConfig(listeners = UICategory.DeleteCategoryActionListener.class),
				@EventConfig(listeners = UICategory.AddForumActionListener.class),
				@EventConfig(listeners = UICategory.EditForumActionListener.class),
				@EventConfig(listeners = UICategory.SetLockedActionListener.class),
				@EventConfig(listeners = UICategory.SetUnLockActionListener.class),
				@EventConfig(listeners = UICategory.SetOpenActionListener.class),
				@EventConfig(listeners = UICategory.SetCloseActionListener.class),
				@EventConfig(listeners = UICategory.MoveForumActionListener.class),
				@EventConfig(listeners = UICategory.RemoveForumActionListener.class),
				@EventConfig(listeners = UICategory.OpenForumLinkActionListener.class),
				@EventConfig(listeners = UICategory.OpenLastTopicLinkActionListener.class)
		}
)
public class UICategory extends UIForm	{
	private String categoryId ;
	private Category category ;
	private boolean	isEditCategory = false ;
	private boolean	isEditForum = false ;
	private	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private List<Forum> forums = new ArrayList<Forum>() ;
	private List<Topic> topicLastList = new ArrayList<Topic>() ;
	public UICategory() throws Exception {
	}
	
	private UserProfile getUserProfile() {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void update(Category category, List<Forum> forums) throws Exception {
		this.category = category ;
		this.forums = forums ;
		categoryId = category.getId() ;
		this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId)) ;
	}
	
	public void updateByBreadcumbs(String categoryId) {
		this.categoryId = categoryId ;
		this.isEditCategory = true ;
		this.isEditForum = true ;
	}
	
	private Category getCategory() throws Exception{
		if(this.isEditCategory) {
			this.category = forumService.getCategory(ForumSessionUtils.getSystemProvider(), this.categoryId);
			this.isEditCategory = true ;
		}
		return this.category ;
	}
	
	@SuppressWarnings("unchecked")
	private List<Forum> getForumList() throws Exception {
		if(this.isEditForum) {
			this.forums = forumService.getForums(ForumSessionUtils.getSystemProvider(), this.categoryId);
			this.isEditForum = false ;
      this.getAncestorOfType(UICategoryContainer.class).getChild(UICategories.class).setIsgetForumList(true) ;
		}
		for(Forum forum : this.forums) {
			if(getUIFormCheckBoxInput(forum.getId()) != null) {
				getUIFormCheckBoxInput(forum.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(forum.getId(), forum.getId(), false) );
			}
		}
		return this.forums;
	}
	
	public void setIsEditCategory(boolean isEdit) {
		this.isEditCategory = isEdit ;
	}
	
	@SuppressWarnings("unused")
	private Forum getForum(String forumId) throws Exception {
		for(Forum forum : this.forums) {
			if(forum.getId().equals(forumId)) return forum;
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
	private Topic getLastTopic(String topicPath) throws Exception {
		Topic topic = forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), topicPath) ;
		this.topicLastList.add(topic) ;
		return topic ;
	}
	
	private Topic getTopic(String topicId) throws Exception {
	if(this.topicLastList.size() > 0) {
		for(Topic topic : this.topicLastList) {
			if(topic.getId().equals(topicId)) return topic ;
		}
	}
	return null ;
	}
	
	static public class EditCategoryActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;			
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UICategoryForm categoryForm = popupContainer.addChild(UICategoryForm.class, null, null) ;
			categoryForm.setCategoryValue(uiCategory.getCategory(), true) ;
			popupContainer.setId("EditCategoryForm") ;
			popupAction.activate(popupContainer, 500, 340) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			uiCategory.isEditCategory = true ;
		}
	}

	static public class DeleteCategoryActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;			
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			forumPortlet.updateIsRendered(1);
			uiCategory.forumService.removeCategory(ForumSessionUtils.getSystemProvider(), uiCategory.categoryId) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath("ForumService") ;
			forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	
	static public class AddForumActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIForumForm forumForm = popupContainer.addChild(UIForumForm.class, null, null) ;
			forumForm.setCategoryValue(uiCategory.categoryId, false) ;
			forumForm.setForumUpdate(false) ;
			popupContainer.setId("AddNewForumForm") ;
			popupAction.activate(popupContainer, 650, 450) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			uiCategory.isEditForum = true ; 
		}
	}
	
	static public class EditForumActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;			
			List<UIComponent> children = uiCategory.getChildren() ;
			Forum forum = null ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forum = uiCategory.getForum(((UIFormCheckBoxInput)child).getName());
						break ;
					}
				}
			}
			if(forum != null) {
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIForumForm forumForm = popupContainer.addChild(UIForumForm.class, null, null) ;
				forumForm.setCategoryValue(uiCategory.categoryId, false) ;
				forumForm.setForumValue(forum, true);
				forumForm.setForumUpdate(false) ;
				popupContainer.setId("EditForumForm") ;
				popupAction.activate(popupContainer, 650, 450) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				uiCategory.isEditForum = true ;
			} else {
				UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UICategory.msg.notCheck", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
		}
	}

	static public class SetLockedActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			List<UIComponent> children = uiCategory.getChildren() ;
			List<Forum> forums = new ArrayList<Forum>() ;
			int i = 0 ;
			String sms = "";
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forums.add(uiCategory.getForum(((UIFormCheckBoxInput)child).getName()));
						if(forums.get(i).getIsLock()){sms = forums.get(i).getForumName(); break;}
						i++;
					}
				}
			}
			if((forums.size() > 0) && (sms.length() == 0)) {
				for (Forum forum : forums) {
					forum.setIsLock(true) ;
					uiCategory.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiCategory.categoryId, forum, false);
				}
				uiCategory.isEditForum = true ;
			}	
			UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
			if((forums.size() == 0) && (sms.length() == 0)) {
				uiApp.addMessage(new ApplicationMessage("UICategory.msg.notCheck", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				uiApp.addMessage(new ApplicationMessage("UICategory.msg.locked", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			}
		}
	}
	
	static public class SetUnLockActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			List<UIComponent> children = uiCategory.getChildren() ;
			List<Forum> forums = new ArrayList<Forum>() ;
			int i = 0 ;
			String sms = "";
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forums.add(uiCategory.getForum(((UIFormCheckBoxInput)child).getName()));
						if(!forums.get(i).getIsLock()){sms = forums.get(i).getForumName(); break;}
						i++;
					}
				}
			}
			if((forums.size() > 0) && (sms.length() == 0)) {
				for (Forum forum : forums) {
					forum.setIsLock(false) ;
					uiCategory.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiCategory.categoryId, forum, false);
				}
				uiCategory.isEditForum = true ;
			} 
			if((forums.size() == 0) && (sms.length() == 0)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UICategory.msg.unlock", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
	
	static public class SetOpenActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			List<UIComponent> children = uiCategory.getChildren() ;
			List<Forum> forums = new ArrayList<Forum>() ;
			int i = 0 ;
			String sms = "";
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forums.add(uiCategory.getForum(((UIFormCheckBoxInput)child).getName()));
						if(!forums.get(i).getIsClosed()){sms = forums.get(i).getForumName(); break;}
						i++;
					}
				}
			}
			if((forums.size() > 0) && (sms.length() == 0)) {
				for (Forum forum : forums) {
					forum.setIsClosed(false) ;
					uiCategory.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiCategory.categoryId, forum, false);
				}
				uiCategory.isEditForum = true ;
			} 
			if((forums.size() == 0) && (sms.length() == 0)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UICategory.msg.open", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetCloseActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			List<UIComponent> children = uiCategory.getChildren() ;
			List<Forum> forums = new ArrayList<Forum>() ;
			int i = 0 ;
			String sms = "";
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forums.add(uiCategory.getForum(((UIFormCheckBoxInput)child).getName()));
						if(forums.get(i).getIsClosed()){sms = forums.get(i).getForumName(); break;}
						i++;
					}
				}
			}
			if((forums.size() > 0) && (sms.length() == 0)) {
				for (Forum forum : forums) {
					forum.setIsClosed(true) ;
					uiCategory.forumService.saveForum(ForumSessionUtils.getSystemProvider(), uiCategory.categoryId, forum, false);
				}
				uiCategory.isEditForum = true ;
			} 
			if((forums.size() == 0) && (sms.length() == 0)) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
			if(sms.length() > 0) {
				Object[] args = { sms };
				throw new MessageException(new ApplicationMessage("UICategory.msg.close", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class MoveForumActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			List<UIComponent> children = uiCategory.getChildren() ;
			List<Forum> forums = new ArrayList<Forum>() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forums.add(uiCategory.getForum(((UIFormCheckBoxInput)child).getName()) );
					}
				}
			}
			if((forums.size() > 0)) {
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMoveForumForm moveForumForm = popupAction.createUIComponent(UIMoveForumForm.class, null, null) ;
				moveForumForm.setListForum(forums, uiCategory.categoryId);
				moveForumForm.setForumUpdate(false) ;
				popupAction.activate(moveForumForm, 315, 365) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				uiCategory.isEditForum = true ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
		}
	}
	
	static public class RemoveForumActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			List<UIComponent> children = uiCategory.getChildren() ;
			List<Forum> forums = new ArrayList<Forum>() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forums.add(uiCategory.getForum(((UIFormCheckBoxInput)child).getName()));
					}
				}
			}
			if((forums.size() > 0)) {
				for (Forum forum : forums) {
					uiCategory.forumService.removeForum(ForumSessionUtils.getSystemProvider(), uiCategory.categoryId, forum.getId()) ;
				}
				uiCategory.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).setUpdateForumLinks() ;
				uiCategory.isEditForum = true ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
		}
	}
	
	static public class OpenForumLinkActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			uiForumContainer.getChild(UIForumDescription.class).setForumIds(uiCategory.categoryId, forumId);
			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
			uiTopicContainer.setUpdateForum(uiCategory.categoryId, uiCategory.getForum(forumId)) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+forumId));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	
	static public class OpenLastTopicLinkActionListener extends EventListener<UICategory> {
    public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource();
			String Id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = Id.trim().split(",");
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForumIds(uiCategory.categoryId, id[0]);
			Topic topic = uiCategory.getTopic(id[1]) ;
			if(topic == null) {
				topic = uiCategory.forumService.getTopic(ForumSessionUtils.getSystemProvider(), uiCategory.categoryId, id[0], id[1], "Guest");
			}
			uiTopicDetail.setTopicFromCate(uiCategory.categoryId ,id[0], topic, true) ;
			uiTopicDetail.setUpdateForum(uiCategory.getForum(id[0])) ;
			uiTopicDetail.setIdPostView("true") ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(uiCategory.categoryId, id[0], id[1]) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+id[0] + " "));
			uiCategory.topicLastList.clear() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}