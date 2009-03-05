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
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIAddWatchingForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/UICategories.gtmpl",
		events = {
			@EventConfig(listeners = UICategories.OpenCategoryActionListener.class),
			@EventConfig(listeners = UICategories.OpenForumLinkActionListener.class),
			@EventConfig(listeners = UICategories.AddBookMarkActionListener.class),
			@EventConfig(listeners = UICategories.AddWatchingActionListener.class),
			@EventConfig(listeners = UICategories.OpenLastTopicLinkActionListener.class)
		}
)
public class UICategories extends UIContainer	{
	protected ForumService forumService ;
	private Map<String, List<Forum>> mapListForum = new HashMap<String, List<Forum>>() ;
	private Map<String, Topic> maptopicLast = new HashMap<String, Topic>() ;
	private List<Category> categoryList = new ArrayList<Category>() ;
	private Map<String, Forum> AllForum = new HashMap<String, Forum>() ;
	public final String FORUM_LIST_SEARCH = "forumListSearch";
	private boolean isGetForumList = false ;
	private boolean isRenderChild = false ;
	private UserProfile userProfile ;
	public UICategories() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addChild(UIForumListSearch.class, null, null).setRendered(isRenderChild) ;
	}
	
	public void setIsRenderChild(boolean isRenderChild) {this.isRenderChild = isRenderChild ;}
	@SuppressWarnings("unused")
	private boolean getIsRendered() throws Exception {
		this.getChild(UIForumListSearch.class).setRendered(isRenderChild) ;
		return isRenderChild ;
	}
	
	@SuppressWarnings({ "deprecation", "unused" })
	private UserProfile getUserProfile() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		return this.userProfile ;
	}
	
	@SuppressWarnings("unused")
	private boolean isOnline(String userId) throws Exception {
		return this.forumService.isOnline(userId) ;
	}
	
	//Function Public getObject 
	public List<Category> getCategorys() { return this.categoryList ; }
	public List<Category> getPrivateCategories() {
		List<Category> list = new ArrayList<Category>() ;
		for (Category cate : this.categoryList) {
			if(cate.getUserPrivate() != null && cate.getUserPrivate().length > 0) {
				list.add(cate) ;
			}
		}
		return list;
	}
	public List<Forum> getForums(String categoryId) { return mapListForum.get(categoryId) ; }
	public Map<String, Forum> getAllForum() { 
		return AllForum ;
	}
		
	
	private List<Category> getCategoryList() throws Exception {
		this.getAncestorOfType(UIForumPortlet.class).getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
		this.categoryList = forumService.getCategories(ForumSessionUtils.getSystemProvider());
		if(this.categoryList.size() > 0)
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(true) ;
		else 
			((UICategoryContainer)getParent()).getChild(UIForumActionBar.class).setHasCategory(false) ;
		return this.categoryList;
	}	
	
	public void setIsgetForumList(boolean isGetForumList) { this.isGetForumList = isGetForumList ; }
	
	private List<Forum> getForumList(String categoryId) throws Exception {
		List<Forum> forumList = null ;
		String strQuery = "";
		if(this.userProfile.getUserRole() > 0) strQuery = "(@exo:isClosed='false') or (exo:moderators='" + this.userProfile.getUserId() + "')";
		forumList = forumService.getForums(ForumSessionUtils.getSystemProvider(), categoryId, strQuery);
		if(mapListForum.containsKey(categoryId)) {
			mapListForum.remove(categoryId) ;
		}
		mapListForum.put(categoryId, forumList) ;
		String forumId ;
		for (Forum forum : forumList) {
			forumId = forum.getId() ;
			if(AllForum.containsKey(forumId)) AllForum.remove(forumId) ;
			AllForum.put(forumId, forum) ;
		}
		return forumList;
	}
	
	private Forum getForumById(String categoryId, String forumId) throws Exception {
		Forum forum_ = new Forum() ; 
		if(!mapListForum.isEmpty() && !isGetForumList) {
			for(Forum forum : mapListForum.get(categoryId)) {
				if(forum.getId().equals(forumId)) {forum_ = forum ; break;}
			}
		}
		if(forum_ == null) {
			forumService.getForum(ForumSessionUtils.getSystemProvider(), categoryId, forumId) ;
		}
		return forum_ ;
	}
	
	@SuppressWarnings("unused")
	private Topic getLastTopic(String topicPath) throws Exception {
		Topic topicLast = new Topic() ;
		topicLast = maptopicLast.get(topicLast.getId()) ;
		if(topicLast == null) {
			topicLast = forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), topicPath, true) ;
			if(topicLast != null)maptopicLast.put(topicLast.getId(), topicLast) ;
		}
		return topicLast ;
	}
	
	private Topic getTopic(String topicId, String path) throws Exception {
		Topic topic = new Topic() ;
		topic = this.maptopicLast.get(topicId) ;
		if(topic == null) {
			SessionProvider sysSession = SessionProviderFactory.createSystemProvider() ;
			try {
			String forumHomePath = forumService.getForumHomePath(sysSession) ;
			topic = forumService.getTopicByPath(sysSession, forumHomePath + "/" + path, false) ;
			} finally {
				if (sysSession != null) sysSession.close();
			}
		}
		return topic ;
	}
	
	private Category getCategory(String categoryId) throws Exception {
		for(Category category : this.getCategoryList()) {
			if(category.getId().equals(categoryId)) return category ;
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
	private boolean getIsPrivate(UserProfile userProfile, Category category) throws Exception {
		String userId = userProfile.getUserId() ;
		if(userProfile.getUserRole() == 0) return true ;
		if(category.getOwner().equals(userId)) return true ;
		String []uesrs = category.getUserPrivate() ;
		if(uesrs != null && uesrs.length > 0 && !uesrs[0].equals(" ")) {
			return ForumServiceUtils.hasPermission(uesrs, userId) ;
		} else return true ;
	}

	static public class OpenCategoryActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UICategoryContainer categoryContainer = uiContainer.getParent() ;
			try {
				UICategory uiCategory = categoryContainer.getChild(UICategory.class) ;
				uiCategory.update(uiContainer.getCategory(categoryId), uiContainer.getForumList(categoryId)) ;
				categoryContainer.updateIsRender(false) ;
				((UIForumPortlet)categoryContainer.getParent()).getChild(UIForumLinks.class).setValueOption(categoryId);
			} catch (Exception e) {
				Object[] args = { "" };
				UIApplication uiApp = uiContainer.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.catagory-deleted", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(categoryContainer) ;
			}
		}
	}
	
	static public class OpenForumLinkActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = path.trim().split("/");
			UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FORUM);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
			uiForumContainer.getChild(UIForumDescription.class).setForum(categories.getForumById(id[0], id[1]));
			uiTopicContainer.updateByBreadcumbs(id[0], id[1], false) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenLastTopicLinkActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories categories = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = path.trim().split("/");
			Topic topic = categories.getTopic(id[2], path) ;
			UIForumPortlet forumPortlet = categories.getAncestorOfType(UIForumPortlet.class) ;
			if(topic == null) {
				Object[] args = { "" };
				UIApplication uiApp = categories.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			} else {
//				boolean isOpen = false;
//				String canviews[] = topic.getCanView();
//				if(canviews != null || canviews[0].equals(" ")) {
//					if(ForumUtils.isStringInStrings(canviews, forumPortlet.getUserProfile().getUserId())) ;
//				}
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
				uiForumContainer.setIsRenderChild(false) ;
				UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
				uiForumContainer.getChild(UIForumDescription.class).setForum(categories.getForumById(id[0], id[1]));
				uiTopicDetail.setTopicFromCate(id[0], id[1], topic, true) ;
				uiTopicDetail.setUpdateForum(categories.getForumById(id[0], id[1])) ;
				uiTopicDetail.setIdPostView("lastpost") ;
				uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(id[0], id[1], topic) ;
				forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1] + " "));
				categories.maptopicLast.clear() ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class AddBookMarkActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(path)) {
				String userName = uiContainer.userProfile.getUserId() ;
				String type = path.substring(0, path.indexOf("//")) ;
				if(type.equals("forum")) {
					path = path.substring(path.indexOf("//")+2) ;
					String categoryId = path.substring(0, path.indexOf("/")) ;
					String forumId = path.substring(path.indexOf("/")+1) ;
					Forum forum = uiContainer.getForumById(categoryId, forumId) ;
					path = "ForumNormalIcon//" + forum.getForumName() + "//" + path;
				}else if(type.equals("category")){
					path = path.substring(path.indexOf("//")+2) ;
					Category category = uiContainer.getCategory(path) ;
					path = "CategoryNormalIcon//" + category.getCategoryName() + "//" + path;
				} else {
					path = path.substring(path.indexOf("//")+2) ;
					String []id = path.trim().split("/");
					Topic topic = uiContainer.getTopic(id[2], path) ;
					path = "ThreadNoNewPost//" + topic.getTopicName() + "//" + path;
				}
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					uiContainer.forumService.saveUserBookmark(sProvider, userName, path, true) ;
				}finally {
					sProvider.close();
				}
				UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateUserProfileInfo() ;
			}
		}
	}

	static public class AddWatchingActionListener extends EventListener<UICategories> {
		public void execute(Event<UICategories> event) throws Exception {
			UICategories uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIForumPortlet forumPortlet = uiContainer.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIAddWatchingForm addWatchingForm = popupAction.createUIComponent(UIAddWatchingForm.class, null, null) ;
			addWatchingForm.initForm() ;
			addWatchingForm.setPathNode(path);
			popupAction.activate(addWatchingForm, 425, 250) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}
