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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;
	
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIAddWatchingForm;
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIExportForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIImportForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
import org.exoplatform.webui.form.UIFormStringInput;
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
				@EventConfig(listeners = UICategory.SearchFormActionListener.class),
				@EventConfig(listeners = UICategory.EditCategoryActionListener.class),
				@EventConfig(listeners = UICategory.WatchOptionActionListener.class),
				@EventConfig(listeners = UICategory.ExportCategoryActionListener.class),
				@EventConfig(listeners = UICategory.ImportForumActionListener.class),
				@EventConfig(listeners = UICategory.DeleteCategoryActionListener.class,confirm="UICategory.confirm.DeleteCategory"),
				@EventConfig(listeners = UICategory.AddForumActionListener.class),
				@EventConfig(listeners = UICategory.EditForumActionListener.class),
				@EventConfig(listeners = UICategory.SetLockedActionListener.class),
				@EventConfig(listeners = UICategory.SetUnLockActionListener.class),
				@EventConfig(listeners = UICategory.SetOpenActionListener.class),
				@EventConfig(listeners = UICategory.SetCloseActionListener.class),
				@EventConfig(listeners = UICategory.MoveForumActionListener.class),
				@EventConfig(listeners = UICategory.RemoveForumActionListener.class),
				@EventConfig(listeners = UICategory.OpenForumLinkActionListener.class),
				@EventConfig(listeners = UICategory.OpenLastTopicLinkActionListener.class),
				@EventConfig(listeners = UICategory.AddBookMarkActionListener.class),
				@EventConfig(listeners = UICategory.AddWatchingActionListener.class),
				@EventConfig(listeners = UICategory.AdvancedSearchActionListener.class)
		}
)
public class UICategory extends UIForm	{
	private boolean useAjax = true;
	private UserProfile userProfile ;
	private String categoryId ;
	private Category category ;
	private boolean	isEditCategory = false ;
	private boolean	isEditForum = false ;
	private	ForumService forumService ;
	private List<Forum> forums = new ArrayList<Forum>() ;
	private Map<String, Topic> MaptopicLast =new HashMap<String, Topic>(); 
	public UICategory() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		setActions(new String[]{"EditCategory","ExportCategory","ImportForum","DeleteCategory", "WatchOption","AddForum","EditForum","SetLocked",
				"SetUnLock","SetOpen","SetClose","MoveForum","RemoveForum"});
	}
	
	private UserProfile getUserProfile() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		return this.userProfile ;
	}
	

	private void setIsUseAjax(){
		this.useAjax = this.getAncestorOfType(UIForumPortlet.class).isUseAjax();
	}
	
	public void update(Category category, List<Forum> forums) throws Exception {
		this.category = category ;
		if(forums == null) {
			this.isEditForum = true;
		} else {
			this.forums = forums ;
		}
		categoryId = category.getId() ;
		this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath((categoryId)) ;
	}
	
	public void updateByBreadcumbs(String categoryId) {
		this.categoryId = categoryId ;
		this.isEditCategory = true ;
		this.isEditForum = true ;
	}
	
	private Category getCategory() throws Exception{
		if(this.isEditCategory || this.category == null) {
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				this.category = forumService.getCategory(sProvider, this.categoryId);
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				sProvider.close();
			}
			this.isEditCategory = true ;
		}
		return this.category ;
	}
	
	@SuppressWarnings("unused")
	private boolean isShowForum(String id) {
		List<String> list = new ArrayList<String>();
		list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleForums());
		if(list.isEmpty()) return true;
		else {
			if(list.contains(id)) return true;
			else return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private List<Forum> getForumList() throws Exception {
		if(this.isEditForum) {
			String strQuery = "";
			if(this.userProfile.getUserRole() > 0) strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				this.forums = forumService.getForums(sProvider, this.categoryId, strQuery);
			}catch (Exception e) {
				e.printStackTrace();
			}finally {
				sProvider.close();
			}
			this.isEditForum = false ;
			this.getAncestorOfType(UICategoryContainer.class).getChild(UICategories.class).setIsgetForumList(true) ;
		}
		List<Forum> listForum = new ArrayList<Forum>(); 
		for(Forum forum : this.forums) {
			String forumId = forum.getId();
			if(getUIFormCheckBoxInput(forumId) != null) {
				getUIFormCheckBoxInput(forumId).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(forumId, forumId, false) );
			}
			if(isShowForum(forumId)) listForum.add(forum);
		}
		return listForum;
	}
	
	public void setIsEditCategory(boolean isEdit) {
		this.isEditCategory = isEdit ;
	}
	
	public void setIsEditForum(boolean isEdit) {
		this.isEditForum = isEdit ;
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
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		Topic topic;
		try {
			topic = forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), topicPath, true) ;
		}catch (Exception e) {
			topic = null;
			e.printStackTrace();
		}finally {
			sProvider.close();
		}
		if(topic != null) {
			String topicId = topic.getId() ;
			if(this.MaptopicLast.containsKey(topicId)) {
				this.MaptopicLast.remove(topicId);
			}
			this.MaptopicLast.put(topicId, topic);
		}
		return topic ;
	}
	
	private Topic getTopic(String topicId) throws Exception {
		if(this.MaptopicLast.containsKey(topicId)) {
			return	this.MaptopicLast.get(topicId);
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
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try{
				uiCategory.forumService.removeCategory(sProvider, uiCategory.categoryId) ;
			} finally {
				sProvider.close();
			}
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
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
			forumForm.initForm();
			forumForm.setCategoryValue(uiCategory.categoryId, false) ;
			forumForm.setForumUpdate(false) ;
			popupContainer.setId("AddNewForumForm") ;
			popupAction.activate(popupContainer, 650, 480) ;
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
				forumForm.setMode(false) ;
				forumForm.initForm();
				forumForm.setCategoryValue(uiCategory.categoryId, false) ;
				forumForm.setForumValue(forum, true);
				forumForm.setForumUpdate(false) ;
				popupContainer.setId("EditForumForm") ;
				popupAction.activate(popupContainer, 650, 480) ;
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
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						forums.add(uiCategory.getForum(((UIFormCheckBoxInput)child).getName()));
					}
				}
			}
			if(forums.size() > 0) {
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					for (Forum forum : forums) {
						if(forum.getIsLock()) continue ;
						forum.setIsLock(true) ;
						uiCategory.forumService.modifyForum(sProvider, forum, 2);
					}
					uiCategory.isEditForum = true ;
				} finally {
					sProvider.close();
				}
			} else {
				UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UICategory.msg.notCheck", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}	
		}
	}
	
	static public class SetUnLockActionListener extends EventListener<UICategory> {
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
			if(forums.size() > 0) {
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					for (Forum forum : forums) {
						if(!forum.getIsLock()) continue ;
						forum.setIsLock(false) ;
						uiCategory.forumService.modifyForum(sProvider, forum, 2);
					}
					uiCategory.isEditForum = true ;
				} finally {
					sProvider.close();
				}
			} else {
				UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UICategory.msg.notCheck", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
		}
	}
	
	static public class SetOpenActionListener extends EventListener<UICategory> {
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
			if(forums.size() > 0) {
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					for (Forum forum : forums) {
						forum.setIsClosed(false) ;
						uiCategory.forumService.modifyForum(sProvider, forum, 1);
					}
					uiCategory.isEditForum = true ;
				} finally {
					sProvider.close();
				}
			} 
			if(forums.size() == 0) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}	
		}
	}

	static public class SetCloseActionListener extends EventListener<UICategory> {
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
			if(forums.size() > 0) {
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					for (Forum forum : forums) {
						forum.setIsClosed(true) ;
						uiCategory.forumService.modifyForum(sProvider, forum, 1);
					}
					uiCategory.isEditForum = true ;
				} finally {
					sProvider.close();
				}
			} 
			if(forums.size() <= 0) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UICategory.msg.notCheck", args, ApplicationMessage.WARNING)) ;
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
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					for (Forum forum : forums) {
						uiCategory.forumService.removeForum(sProvider, uiCategory.categoryId, forum.getId()) ;
					}
					uiCategory.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).setUpdateForumLinks() ;
					uiCategory.isEditForum = true ;
				} finally {
					sProvider.close();
				}
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
			Forum forum = uiCategory.getForum(forumId) ;
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FORUM);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
			uiTopicContainer.setUpdateForum(uiCategory.categoryId, forum) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+forumId));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	
	static public class OpenLastTopicLinkActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource();
			String Id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = Id.trim().split("/");
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FORUM);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForum(uiCategory.getForum(id[0]));
			Topic topic = uiCategory.getTopic(id[1]) ;
			uiTopicDetail.setTopicFromCate(uiCategory.categoryId ,id[0], topic) ;
			uiTopicDetail.setUpdateForum(uiCategory.getForum(id[0])) ;
			uiTopicDetail.setIdPostView("lastpost") ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(uiCategory.categoryId, id[0], topic.getId()) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+id[0] + " "));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class SearchFormActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;
			String path = uiCategory.category.getPath() ;
			UIFormStringInput formStringInput = uiCategory.getUIStringInput(ForumUtils.SEARCHFORM_ID) ;
			String text = formStringInput.getValue() ;
			if(!ForumUtils.isEmpty(text) && !ForumUtils.isEmpty(path)) {
				String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=";
				for (int i = 0; i < special.length(); i++) {
					char c = special.charAt(i);
					if(text.indexOf(c) >= 0) {
						UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
						return ;
					}
				}
				StringBuffer type = new StringBuffer();
				if(uiCategory.getUserProfile().getUserRole() == 0){ 
					type.append("true,").append(Utils.FORUM).append("/").append(Utils.TOPIC).append("/").append(Utils.POST);
				} else {
					type.append("false,").append(Utils.FORUM).append("/").append(Utils.TOPIC).append("/").append(Utils.POST);
				}
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				UICategories categories = categoryContainer.getChild(UICategories.class);
				categories.setIsRenderChild(true) ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSearch> list = forumService.getQuickSearch(ForumSessionUtils.getSystemProvider(), text, type.toString(), path, ForumSessionUtils.getAllGroupAndMembershipOfUser(uiCategory.getUserProfile().getUserId()));
				UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class) ;
				listSearchEvent.setListSearchEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
				formStringInput.setValue("") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIQuickSearchForm.msg.checkEmpty", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
	
	static public class AddBookMarkActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(path)) {
				int t = path.indexOf("//");
				String type = path.substring(0, t) ;
				if(type.equals("forum")) {
					path = path.substring(t+2) ;
					String forumId = path.substring(path.indexOf("/")+1) ;
					Forum forum = uiContainer.getForum(forumId) ;
					path = "ForumNormalIcon//" + forum.getForumName() + "//" + path;
				} else if(type.equals("category")) {
					path = path.substring(path.indexOf("//")+2) ;
					Category category = uiContainer.getCategory() ;
					path = "CategoryNormalIcon//" + category.getCategoryName() + "//" + path;
				} else {
					path = path.substring(t+2) ;
					String topicId = path.substring(path.lastIndexOf("/")+1);
					Topic topic = uiContainer.getTopic(topicId) ;
					path = "ThreadNoNewPost//" + topic.getTopicName() + "//" + path;
				}
				String userName = uiContainer.userProfile.getUserId() ;
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					uiContainer.forumService.saveUserBookmark(sProvider, userName, path, true) ;
				}catch (Exception e) {
				} finally {
					sProvider.close();
				}
				UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateUserProfileInfo() ;
			}
		}
	}
	
	static public class AddWatchingActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory category = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIForumPortlet forumPortlet = category.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIAddWatchingForm addWatchingForm = popupAction.createUIComponent(UIAddWatchingForm.class, null, null) ;
			addWatchingForm.initForm() ;
			addWatchingForm.setPathNode(path);
			popupAction.activate(addWatchingForm, 425, 250) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static	public class AdvancedSearchActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			UISearchForm searchForm = forumPortlet.getChild(UISearchForm.class) ;
			searchForm.setUserProfile(forumPortlet.getUserProfile()) ;
			searchForm.setSelectType(Utils.CATEGORY) ;
			searchForm.setIsSearchForum(false);
			searchForm.setIsSearchTopic(false);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class ExportCategoryActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource();
			Category category = uiCategory.getCategory();
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			if(category == null){
				UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UITopicContainer.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				return;
			}
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIExportForm exportForm = popupAction.createUIComponent(UIExportForm.class, null, null) ;
			exportForm.setObjectId(category);
			popupAction.activate(exportForm, 450, 200) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class ImportForumActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory category = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIForumPortlet forumPortlet = category.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIImportForm importForm = popupAction.createUIComponent(UIImportForm.class, null, null) ;
			importForm.setPath(category.getCategory().getPath());
			popupAction.activate(importForm, 400, 150) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class WatchOptionActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource();
			Category category = uiCategory.category ;
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIWatchToolsForm watchToolsForm = popupAction.createUIComponent(UIWatchToolsForm.class, null, null) ;
			watchToolsForm.setPath(category.getPath());
			watchToolsForm.setEmails(category.getEmailNotification()) ;
			watchToolsForm.setIsTopic(true);
			popupAction.activate(watchToolsForm, 500, 365) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}