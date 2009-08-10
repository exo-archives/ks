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
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIExportForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIImportForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIRSSForm;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
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

@SuppressWarnings({ "unused", "unchecked"})
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
				@EventConfig(listeners = UICategory.OpenLastReadTopicActionListener.class),
				@EventConfig(listeners = UICategory.AddBookMarkActionListener.class),
				@EventConfig(listeners = UICategory.AddWatchingActionListener.class),
				@EventConfig(listeners = UICategory.RSSActionListener.class),
				@EventConfig(listeners = UICategory.AdvancedSearchActionListener.class)
		}
)
public class UICategory extends UIForm	{
	private UserProfile userProfile = null;
	private String categoryId ;
	private Category category ;
	private boolean	isEditCategory = false ;
	private boolean	isEditForum = false ;
	private boolean useAjax = true;
  private int dayForumNewPost = 0;
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
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class); 
		useAjax = forumPortlet.isUseAjax();
		dayForumNewPost = forumPortlet.getDayForumNewPost();
		userProfile = forumPortlet.getUserProfile() ;
		return this.userProfile ;
	}

	public String getRSSLink(String cateId){
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return RSS.getRSSLink("forum", pcontainer.getPortalContainerInfo().getContainerName(), cateId);
	}
	
  private int getDayForumNewPost() {
		return dayForumNewPost;
	}
  
  private String getLastReadPostOfForum(String forumId) throws Exception {
		return userProfile.getLastPostIdReadOfForum(forumId);
	}
  
  private String getScreenName(String userName) throws Exception {
		return forumService.getScreenName(userName);
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

	public void updateByLink(Category category) {
		this.categoryId = category.getId() ;
		this.isEditCategory = false ;
		this.isEditForum = true ;
		this.category = category;
	}
	
	private Category getCategory() throws Exception{
		if(this.isEditCategory || this.category == null) {
			try {
				this.category = forumService.getCategory(this.categoryId);
			}catch (Exception e) {
				e.printStackTrace();
			}
			this.isEditCategory = false ;
		}
		return category ;
	}
	
	private boolean isShowForum(String id) {
		List<String> list = new ArrayList<String>();
		list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleForums());
		if(list.isEmpty()) return true;
		else {
			if(list.contains(id)) return true;
			else return false;
		}
	}
	
  private List<Forum> getForumList() throws Exception {
		if(this.isEditForum) {
			String strQuery = "";
			if(this.userProfile.getUserRole() > 0) strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
			try {
				this.forums = forumService.getForums(this.categoryId, strQuery);
			}catch (Exception e) {
				e.printStackTrace();
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
	
	private Forum getForum(String forumId) throws Exception {
		for(Forum forum : this.forums) {
			if(forum.getId().equals(forumId)) return forum;
		}
		return null ;
	}
	
	private Topic getLastTopic(String topicPath) throws Exception {
		Topic topic;
		try {
			topic = forumService.getTopicByPath(topicPath, true) ;
		}catch (Exception e) {
			topic = null;
			e.printStackTrace();
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
			popupAction.activate(popupContainer, 550, 380) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			uiCategory.isEditCategory = true ;
		}
	}

	static public class DeleteCategoryActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource() ;			
			try{
				uiCategory.forumService.removeCategory(uiCategory.categoryId) ;
			} catch (Exception e) {
			}
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
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
				try {
					for (Forum forum : forums) {
						if(forum.getIsLock()) continue ;
						forum.setIsLock(true) ;
						uiCategory.forumService.modifyForum(forum, 2);
					}
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
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
				try {
					for (Forum forum : forums) {
						if(!forum.getIsLock()) continue ;
						forum.setIsLock(false) ;
						uiCategory.forumService.modifyForum(forum, 2);
					}
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
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
				try {
					for (Forum forum : forums) {
						forum.setIsClosed(false) ;
						uiCategory.forumService.modifyForum(forum, 1);
					}
					uiCategory.isEditForum = true ;
				}catch (Exception e) {
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
				try {
					for (Forum forum : forums) {
						forum.setIsClosed(true) ;
						uiCategory.forumService.modifyForum(forum, 1);
					}
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
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
				try {
					for (Forum forum : forums) {
						uiCategory.forumService.removeForum(uiCategory.categoryId, forum.getId()) ;
					}
					uiCategory.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).setUpdateForumLinks() ;
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
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
			uiTopicDetail.setUpdateForum(uiCategory.getForum(id[0])) ;
			uiTopicDetail.setTopicFromCate(uiCategory.categoryId ,id[0], topic) ;
			String lastPostId = "";
			uiTopicDetail.setLastPostId(lastPostId);
			if(lastPostId == null || lastPostId.length() < 0) lastPostId = "lastpost";
			uiTopicDetail.setIdPostView(lastPostId) ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(uiCategory.categoryId, id[0], topic.getId()) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+id[0] + " "));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenLastReadTopicActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiCategory = event.getSource();
			WebuiRequestContext context = event.getRequestContext() ; 
			String path = context.getRequestParameter(OBJECTID)	;//cateid/forumid/topicid/postid/
			String []id = path.trim().split("/");
			Topic topic = (Topic)uiCategory.forumService.getObjectNameById(id[2], Utils.TOPIC);
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			if(topic == null) {
				Object[] args = { "" };
				UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", args, ApplicationMessage.WARNING)) ;
				context.addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			} else {
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				uiForumContainer.getChild(UIForumDescription.class).setForum(uiCategory.getForum(id[0]));
				uiTopicDetail.setUpdateForum(uiCategory.getForum(id[1])) ;
				uiTopicDetail.setTopicFromCate(uiCategory.categoryId ,id[1], topic) ;
				String lastPostId = id[3];
				uiTopicDetail.setLastPostId(lastPostId);
				if(lastPostId == null || lastPostId.length() < 0) lastPostId = "lastpost";
				uiTopicDetail.setIdPostView(lastPostId) ;
				uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(uiCategory.categoryId, id[1], topic.getId()) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+id[1] + " "));
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
			context.addUIComponentToUpdateByAjax(forumPortlet) ;
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
				List<String> forumIdsOfModerator = new ArrayList<String>();
				if(uiCategory.userProfile.getUserRole() == 0){ 
					type.append("true,").append(Utils.FORUM).append("/").append(Utils.TOPIC).append("/").append(Utils.POST);
				} else {
					type.append("false,").append(Utils.FORUM).append("/").append(Utils.TOPIC).append("/").append(Utils.POST);
					if(uiCategory.userProfile.getUserRole() == 1) {
						String []strings = uiCategory.userProfile.getModerateForums();
						for (int i = 0; i < strings.length; i++) {
		          String str = strings[i].substring(strings[i].lastIndexOf("/")+1);
		          if(str.length() > 0)
		          	forumIdsOfModerator.add(str);
	          }
					}
				}
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				UICategories categories = categoryContainer.getChild(UICategories.class);
				categories.setIsRenderChild(true) ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSearch> list = forumService.getQuickSearch(text, type.toString(), path, uiCategory.userProfile.getUserId(),
																				forumPortlet.getInvisibleCategories(), forumPortlet.getInvisibleForums(), forumIdsOfModerator);
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
					path = "ForumNormalIcon//" + forum.getForumName() + "//" + forumId;
				} else if(type.equals("category")) {
					path = path.substring(path.indexOf("//")+2) ;
					Category category = uiContainer.getCategory() ;
					path = "CategoryNormalIcon//" + category.getCategoryName() + "//" + path;
				} else {
					path = path.substring(t+2) ;
					String topicId = path.substring(path.lastIndexOf("/")+1);
					Topic topic = uiContainer.getTopic(topicId) ;
					path = "ThreadNoNewPost//" + topic.getTopicName() + "//" + topicId;
				}
				String userName = uiContainer.userProfile.getUserId() ;
				try {
					uiContainer.forumService.saveUserBookmark(userName, path, true) ;
				}catch (Exception e) {
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
			List<String> values = new ArrayList<String>();
			String userName = category.userProfile.getUserId();
			try {
				values.add(ForumSessionUtils.getUserByUserId(userName).getEmail());
				category.forumService.addWatch(1, path, values, ForumSessionUtils.getCurrentUser()) ;
				category.isEditCategory = true;
				Object[] args = { };
				UIApplication uiApp = category.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.successfully", args, ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(category) ;
			} catch (Exception e) {
				e.printStackTrace();
				Object[] args = { };
				UIApplication uiApp = category.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.fall", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			}
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
			uiCategory.isEditCategory = true;
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			Category category = uiCategory.getCategory();
			if(category == null){
				UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.catagory-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				return;
			}
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIExportForm exportForm = popupAction.createUIComponent(UIExportForm.class, null, null) ;
			exportForm.setObjectId(category);
			popupAction.activate(exportForm, 450, 300) ;
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
			Category cate = category.getCategory();
			if(cate == null){
				UIApplication uiApp = category.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UITopicContainer.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				return;
			}
			importForm.setPath(cate.getPath());
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
	
	static public class RSSActionListener extends EventListener<UICategory> {
		public void execute(Event<UICategory> event) throws Exception {
			UICategory uiForm = event.getSource();
			String cateId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String userId = uiForm.getUserProfile().getUserId();
			if(!userId.equals(UserProfile.USER_GUEST)){
				uiForm.forumService.addWatch(-1, cateId, null, userId);
			}
			String rssLink = uiForm.getRSSLink(cateId);
			UIForumPortlet portlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("ForumRSSForm") ;
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}