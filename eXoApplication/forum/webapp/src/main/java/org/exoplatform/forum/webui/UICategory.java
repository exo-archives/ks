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
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.webui.popup.UICategoryForm;
import org.exoplatform.forum.webui.popup.UIExportForm;
import org.exoplatform.forum.webui.popup.UIForumForm;
import org.exoplatform.forum.webui.popup.UIImportForm;
import org.exoplatform.forum.webui.popup.UIMoveForumForm;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */


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
				@EventConfig(listeners = UICategory.UnWatchActionListener.class),
				@EventConfig(listeners = UICategory.RSSActionListener.class),
				@EventConfig(listeners = UICategory.AdvancedSearchActionListener.class)
		}
)
@SuppressWarnings({ "unused", "unchecked"})
public class UICategory extends BaseForumForm	{
	private UserProfile userProfile = null;
	private String categoryId ;
	private Category category ;
	private boolean	isEditCategory = false ;
	private boolean	isEditForum = false ;
	private boolean useAjax = true;
  private int dayForumNewPost = 0;
	private List<Forum> forums = new ArrayList<Forum>() ;
	private List<Watch> listWatches = new ArrayList<Watch>();
	private Map<String, Topic> MaptopicLast =new HashMap<String, Topic>(); 
	
	static public boolean isUnWatch = false;
	
	public static String unwatchEmail = "";
	
	public UICategory() throws Exception {
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		setActions(new String[]{"EditCategory","ExportCategory","ImportForum","DeleteCategory", "WatchOption","AddForum","EditForum","SetLocked",
				"SetUnLock","SetOpen","SetClose","MoveForum","RemoveForum"});
	}
	
  private UserProfile getUserProfile() throws Exception {
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class); 
		useAjax = forumPortlet.isUseAjax();
		dayForumNewPost = forumPortlet.getDayForumNewPost();
		userProfile = forumPortlet.getUserProfile() ;
		listWatches = forumPortlet.getWatchinhByCurrentUser();
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
		return getForumService().getScreenName(userName);
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
	
	public String getCategoryId() {
	  return this.categoryId;
  }
	
	private Category getCategory() throws Exception{
		if(this.isEditCategory || this.category == null) {
			try {
				this.category = getForumService().getCategory(this.categoryId);
			}catch (Exception e) {
				e.printStackTrace();
			}
			this.isEditCategory = false ;
		}
		return category ;
	}

	 private Category refreshCategory() throws Exception{
      try {
        this.category = getForumService().getCategory(this.categoryId);
      }catch (Exception e) {
        e.printStackTrace();
      }
	    return category ;
	  }

	
	private boolean isShowForum(String id) {
	  if(this.getAncestorOfType(UIForumPortlet.class).getInvisibleCategories().isEmpty()) return true;
		List<String> list = new ArrayList<String>();
		list.addAll(this.getAncestorOfType(UIForumPortlet.class).getInvisibleForums());
		return (list.contains(id))?true:false;
	}
	
  private List<Forum> getForumList() throws Exception {
		if(this.isEditForum) {
			String strQuery = "";
			if(this.userProfile.getUserRole() > 0) strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
			try {
				this.forums = getForumService().getForumSummaries(this.categoryId, strQuery);
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
	
	private Topic getLastTopic(Category cate, Forum forum) throws Exception {
	  
	  
		Topic topic = null;
		String topicPath = forum.getLastTopicPath();
		if (!ForumUtils.isEmpty(topicPath)) {
		  
		  try {
		    topic = getForumService().getTopicByPath(topicPath, true) ;
		  }catch (Exception e) {
		    topic = null;
		    log.warn(e);
		  }
		  if(topic != null) {
		    String topicId = topic.getId() ;
		    if (getAncestorOfType(UIForumPortlet.class).checkCanView(cate, forum, topic)) {		      
		      this.MaptopicLast.put(topicId, topic);
		    } else {
		      if(this.MaptopicLast.containsKey(topicId)) {
		        this.MaptopicLast.remove(topicId);
		      }
		      return null;
		    }
		  }
		  
		}
		return topic ;
	}
	
	private Topic getTopic(String topicId) throws Exception {
		if(this.MaptopicLast.containsKey(topicId)) {
			return	this.MaptopicLast.get(topicId);
		}
		return null ;
	}
	
  private boolean isWatching(String path) throws Exception {
		for (Watch watch : listWatches) {
			if(path.equals(watch.getNodePath())) return true;
    }
		return false;
	}

	private String getEmailWatching(String path) throws Exception {
		for (Watch watch : listWatches) {
			try {
				if(watch.getNodePath().endsWith(path)) return watch.getEmail();
      } catch (Exception e) {}
		}
		return "";
	}
	
	static public class EditCategoryActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
			UICategoryForm categoryForm = uiCategory.openPopup(UICategoryForm.class, "EditCategoryForm", 550, 380) ;
			categoryForm.setCategoryValue(uiCategory.getCategory(), true) ;
			uiCategory.isEditCategory = true ;
		}
	}

	static public class DeleteCategoryActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
			try{
				uiCategory.getForumService().removeCategory(uiCategory.categoryId) ;
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
	
	
	static public class AddForumActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
			UIForumForm forumForm = uiCategory.openPopup(UIForumForm.class, "AddNewForumForm", 650, 480) ;
			forumForm.initForm();
			forumForm.setCategoryValue(uiCategory.categoryId, false) ;
			forumForm.setForumUpdate(false) ;
			uiCategory.isEditForum = true ; 
		}
	}
	
	static public class EditForumActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
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
				UIForumForm forumForm = uiCategory.openPopup(UIForumForm.class, "EditForumForm", 650, 480) ;
				forumForm.setMode(false) ;
				forumForm.initForm();
				forumForm.setCategoryValue(uiCategory.categoryId, false) ;
				forumForm.setForumValue(forum, true);
				forumForm.setForumUpdate(false) ;
				uiCategory.isEditForum = true ;
			} else {
				warning("UICategory.msg.notCheck") ;
				return ;
			}
		}
	}

	static public class SetLockedActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
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
						uiCategory.getForumService().modifyForum(forum, 2);
					}
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
				}
			} else {
				warning("UICategory.msg.notCheck") ;
				return ;
			}	
		}
	}
	
	static public class SetUnLockActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
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
						uiCategory.getForumService().modifyForum(forum, 2);
					}
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
				}
			} else {
				warning("UICategory.msg.notCheck") ;
				return ;
			}
		}
	}
	
	static public class SetOpenActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
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
						uiCategory.getForumService().modifyForum(forum, 1);
					}
					uiCategory.isEditForum = true ;
				}catch (Exception e) {
				}
			} 
			if(forums.size() == 0) {
				warning("UICategory.msg.notCheck") ;
			}	
		}
	}

	static public class SetCloseActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
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
						uiCategory.getForumService().modifyForum(forum, 1);
					}
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
				}
			} 
			if(forums.size() <= 0) {
				warning("UICategory.msg.notCheck") ;
			}	
		}
	}

	static public class MoveForumActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
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
				UIMoveForumForm moveForumForm = uiCategory.openPopup(UIMoveForumForm.class, 315, 365) ;
				moveForumForm.setListForum(forums, uiCategory.categoryId);
				moveForumForm.setForumUpdate(false) ;
				uiCategory.isEditForum = true ;
			} else {
				warning("UICategory.msg.notCheck") ;
			}	
		}
	}
	
	static public class RemoveForumActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
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
						uiCategory.getForumService().removeForum(uiCategory.categoryId, forum.getId()) ;
					}
					uiCategory.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).setUpdateForumLinks() ;
					uiCategory.isEditForum = true ;
				} catch (Exception e) {
				}
			} else {
				warning("UICategory.msg.notCheck") ;
			}	
		}
	}
	
	static public class OpenForumLinkActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String forumId) throws Exception {
			Forum forum = uiCategory.getForumService().getForum(uiCategory.categoryId, forumId);
			if(forum != null){
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				uiForumContainer.setIsRenderChild(true) ;
				uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
				UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
				uiTopicContainer.setUpdateForum(uiCategory.categoryId, forum, 0) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+forumId));
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				uiCategory.isEditForum = true;
				warning("UITopicContainer.msg.forum-deleted") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory) ;
			}
		}
	}
	
	
	static public class OpenLastTopicLinkActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String Id) throws Exception {
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
			uiTopicDetail.setTopicFromCate(uiCategory.categoryId ,id[0], topic, 0) ;
			String lastPostId = "";
			uiTopicDetail.setLastPostId(lastPostId);
			if(lastPostId == null || lastPostId.length() < 0) lastPostId = "lastpost";
			uiTopicDetail.setIdPostView(lastPostId) ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(uiCategory.categoryId, id[0], topic.getId()) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption((uiCategory.categoryId+"/"+id[0] + " "));
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenLastReadTopicActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, String path) throws Exception {
			WebuiRequestContext context = event.getRequestContext() ; 
			String []id = path.trim().split("/");
			Topic topic = (Topic)uiCategory.getForumService().getObjectNameById(id[2], Utils.TOPIC);
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			if(topic == null) {
				warning("UIForumPortlet.msg.topicEmpty") ;
				forumPortlet.updateUserProfileInfo();
			} else {
				path = topic.getPath();
				Forum forum;
				if(path.indexOf(id[1]) < 0){
					if(id[id.length-1].indexOf(Utils.POST) == 0){
						path = path.substring(path.indexOf(Utils.CATEGORY))+"/"+id[id.length-1];
					}else {
						path = path.substring(path.indexOf(Utils.CATEGORY));
					}
					id = path.trim().split("/");
					forum = uiCategory.getForumService().getForum(id[0], id[1]);
					forumPortlet.updateUserProfileInfo();
				} else {
					forum = uiCategory.getForum(id[1]);
				}
				Category category = uiCategory.getCategory();
				if(forumPortlet.checkCanView(category, forum, topic)) {
					forumPortlet.updateIsRendered(ForumUtils.FORUM);
					UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
					UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
					uiForumContainer.setIsRenderChild(false) ;
					UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
					uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
					uiTopicDetail.setUpdateForum(forum) ;
					uiTopicDetail.setTopicFromCate(id[0] ,id[1], topic, 0) ;
					if(id[id.length-1].indexOf(Utils.POST) == 0){
						uiTopicDetail.setIdPostView(id[id.length-1]) ;
						uiTopicDetail.setLastPostId(id[id.length-1]);
					} else {
						uiTopicDetail.setIdPostView("lastpost") ;
					}
					uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1], topic.getId()) ;
					forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1] + " "));
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
				} else {
					uiCategory.userProfile.addLastPostIdReadOfForum(forum.getId(), "");
					uiCategory.getForumService().saveLastPostIdRead(uiCategory.userProfile.getUserId(), uiCategory.userProfile.getLastReadPostOfForum(),
							uiCategory.userProfile.getLastReadPostOfTopic());
					((UIApplication)forumPortlet).addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission",  new String[]{"this","topic"}, ApplicationMessage.WARNING)) ;
					context.addUIComponentToUpdateByAjax(uiCategory) ;
				}
			}
			context.addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class SearchFormActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
			String path = uiCategory.category.getPath() ;
			UIFormStringInput formStringInput = uiCategory.getUIStringInput(ForumUtils.SEARCHFORM_ID) ;
			String text = formStringInput.getValue() ;
			if(!ForumUtils.isEmpty(text) && !ForumUtils.isEmpty(path)) {
				String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=|:\"'";
				for (int i = 0; i < special.length(); i++) {
					char c = special.charAt(i);
					if(text.indexOf(c) >= 0) {
						warning("UIQuickSearchForm.msg.failure") ;
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
				List<ForumSearch> list = uiCategory.getForumService().getQuickSearch(text, type.toString(), path, uiCategory.userProfile.getUserId(),
																				forumPortlet.getInvisibleCategories(), forumPortlet.getInvisibleForums(), forumIdsOfModerator);
				UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class) ;
				listSearchEvent.setListSearchEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
				formStringInput.setValue("") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				warning("UIQuickSearchForm.msg.checkEmpty") ;
			}
		}
	}
	
	static public class AddBookMarkActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, String path) throws Exception {
			if(!ForumUtils.isEmpty(path)) {
				int t = path.indexOf("//");
				String type = path.substring(0, t) ;
				if(type.equals("forum")) {
					path = path.substring(t+2) ;
					String forumId = path.substring(path.indexOf("/")+1) ;
					Forum forum = uiCategory.getForum(forumId) ;
					path = "ForumNormalIcon//" + forum.getForumName() + "//" + forumId;
				} else if(type.equals("category")) {
					path = path.substring(path.indexOf("//")+2) ;
					Category category = uiCategory.getCategory() ;
					path = "CategoryNormalIcon//" + category.getCategoryName() + "//" + path;
				} else {
					path = path.substring(t+2) ;
					String topicId = path.substring(path.lastIndexOf("/")+1);
					Topic topic = uiCategory.getTopic(topicId) ;
					path = "ThreadNoNewPost//" + topic.getTopicName() + "//" + topicId;
				}
				String userName = uiCategory.userProfile.getUserId() ;
				try {
					uiCategory.getForumService().saveUserBookmark(userName, path, true) ;
				}catch (Exception e) {
				}
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateUserProfileInfo() ;
			}
		}
	}
	
	static public class AddWatchingActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String path) throws Exception {
			List<String> values = new ArrayList<String>();
			try {
				values.add(uiCategory.userProfile.getEmail());
				uiCategory.getForumService().addWatch(1, path, values, uiCategory.userProfile.getUserId()) ;
				uiCategory.isEditCategory = true;
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateWatchinh();
				isUnWatch = false;
				info("UIAddWatchingForm.msg.successfully") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory) ;
			} catch (Exception e) {
				e.printStackTrace();
				warning("UIAddWatchingForm.msg.fall") ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory) ;
		}
	}
	
	static public class UnWatchActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String path) throws Exception {
			try {
			  unwatchEmail = uiCategory.getEmailWatching(path);
				uiCategory.getForumService().removeWatch(1, path,uiCategory.userProfile.getUserId()+"/"+uiCategory.getEmailWatching(path)) ;
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateWatchinh();
				isUnWatch = true;
				info("UIAddWatchingForm.msg.UnWatchSuccessfully") ;
			} catch (Exception e) {
				e.printStackTrace();
				warning("UIAddWatchingForm.msg.UnWatchfall") ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory) ;
		}
	}
	
	static	public class AdvancedSearchActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
			UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			UISearchForm searchForm = forumPortlet.getChild(UISearchForm.class) ;
			searchForm.setUserProfile(forumPortlet.getUserProfile()) ;
			searchForm.setSelectType(Utils.FORUM) ;
			searchForm.setPath(uiCategory.category.getPath());
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class ExportCategoryActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String path) throws Exception {
			uiCategory.isEditCategory = true;
			Category category = uiCategory.getCategory();
			if(category == null){
				warning("UIForumPortlet.msg.catagory-deleted") ;
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				return;
			}
			UIExportForm exportForm = uiCategory.openPopup(UIExportForm.class, 450, 300) ;
			exportForm.setObjectId(category);
		}
	}
	
	static public class ImportForumActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String path) throws Exception {
			Category cate = uiCategory.getCategory();
			if(cate == null){
				warning("UITopicContainer.msg.forum-deleted") ;
				UIForumPortlet forumPortlet = uiCategory.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
				return;
			}
			UIImportForm importForm = uiCategory.openPopup(UIImportForm.class, 450, 160) ;
			importForm.setPath(cate.getPath());
		}
	}
	
	static public class WatchOptionActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String objectId) throws Exception {
		  Category category;
			if(UICategory.isUnWatch){
			   category = uiCategory.refreshCategory() ;
			}else {
			  category = uiCategory.category ;
			}
			 UIWatchToolsForm watchToolsForm = uiCategory.openPopup(UIWatchToolsForm.class, 500, 365) ;
       watchToolsForm.setPath(category.getPath());
       watchToolsForm.setEmails(category.getEmailNotification());
		}
	}
	
	static public class RSSActionListener extends BaseEventListener<UICategory> {
		public void onEvent(Event<UICategory> event, UICategory uiCategory, final String cateId) throws Exception {
			String userId = uiCategory.getUserProfile().getUserId();
			if(!userId.equals(UserProfile.USER_GUEST)){
				uiCategory.getForumService().addWatch(-1, cateId, null, userId);
				event.getRequestContext().addUIComponentToUpdateByAjax(uiCategory) ;
			}
		}
	}
}
