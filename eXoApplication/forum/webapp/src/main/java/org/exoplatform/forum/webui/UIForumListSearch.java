/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIViewPost;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *		tu.duy@exoplatform.com
 * 14 Apr 2008, 08:22:52	
 */
@ComponentConfig(
		template = "app:/templates/forum/webui/UIForumListSearch.gtmpl",
		events = {
			@EventConfig(listeners = UIForumListSearch.OpentContentActionListener.class),
			@EventConfig(listeners = UIForumListSearch.CloseActionListener.class)
		}
)
public class UIForumListSearch extends UIContainer {
	private List<ForumSearch> listEvent = null ;
	private boolean isShowIter = true;
	public final String SEARCH_ITERATOR = "forumSearchIterator";
	private JCRPageList pageList ;
	private UIForumPageIterator pageIterator ;
	public UIForumListSearch() throws Exception {
		pageIterator = addChild(UIForumPageIterator.class, null, SEARCH_ITERATOR);
	}
	
	public void setListSearchEvent(List<ForumSearch> listEvent) {
		this.listEvent = listEvent ;
		pageIterator.setSelectPage(1);
	}
	
	public boolean getIsShowIter() {
		return isShowIter ;
	}
	
	@SuppressWarnings("unchecked")
	public List<ForumSearch> getListEvent() {
		pageList = new ForumPageList(10, listEvent.size());
		pageList.setPageSize(10);
		pageIterator.updatePageList(pageList);
		isShowIter = true;
		if(pageList.getAvailablePage() <= 1) isShowIter = false;
		int pageSelect = (int) pageIterator.getPageSelected();
		List<ForumSearch>list = new ArrayList<ForumSearch>();
		try {
			list.addAll(pageList.getPageSearch(pageSelect, this.listEvent)) ;
		} catch (Exception e) {
		}
		pageSelect = pageList.getCurrentPage();
		return list ;
	}
	
	private ForumSearch getForumSearch(String id) {
		for (ForumSearch forumSearch : this.listEvent) {
			if(forumSearch.getId().equals(id)) return forumSearch ;
		}
		return null;
	}
	
	private boolean canView(Category category, Forum forum, Topic topic, Post post, UserProfile userProfile) throws Exception{
		if(userProfile.getUserRole() == 0) return true;
		boolean canView = true;
		boolean isModerator = false;
		if(category == null) return false;
		String[] listUsers = category.getUserPrivate();
		//check category is private:
		if(listUsers.length > 0 && listUsers[0].trim().length() > 0 && !ForumServiceUtils.hasPermission(listUsers, userProfile.getUserId())) 
			return false;
		
		// check forum
		if(forum != null){
			listUsers = forum.getModerators();
			if(userProfile.getUserRole() == 1 && (listUsers.length > 0 && listUsers[0].trim().length() > 0 && 
					ForumServiceUtils.hasPermission(listUsers, userProfile.getUserId()))) {
				isModerator = true;
				canView = true;
			} else if(forum.getIsClosed()) return false;
			else canView = true;
			
			// ckeck Topic:
			if(topic != null){
				if(!isModerator && !topic.getIsClosed() && topic.getIsActive() && topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsWaiting()){
					List<String> list = new ArrayList<String>();
					list = ForumUtils.addArrayToList(list, topic.getCanView());
					list = ForumUtils.addArrayToList(list, forum.getViewer());
					list = ForumUtils.addArrayToList(list, category.getViewer());
					if(!list.isEmpty()) list.add(topic.getOwner());
					if(!list.isEmpty() && !ForumServiceUtils.hasPermission(list.toArray(new String[]{}), userProfile.getUserId()))canView = false;
				}	else canView = false;
			}
		}
		
		return canView;
	}
	
	static	public class OpentContentActionListener extends EventListener<UIForumListSearch> {
		public void execute(Event<UIForumListSearch> event) throws Exception {
			UIForumListSearch uiForm = event.getSource() ;
			String objId = event.getRequestContext().getRequestParameter(OBJECTID);
			ForumSearch forumSearch = uiForm.getForumSearch(objId) ;
			
			String path = forumSearch.getPath();
			String type = forumSearch.getType() ;
			boolean isErro = false ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UserProfile userProfile = forumPortlet.getUserProfile();
			boolean isRead = true;
			ForumService forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
			
			String []id = path.split("/") ;
			String cateId="", forumId="", topicId="", postId="";
			for (int i = 0; i < id.length; i++) {
				if(id[i].indexOf(Utils.CATEGORY) >= 0) cateId = id[i];
				else if(id[i].indexOf(Utils.FORUM) >= 0) forumId = id[i];
				else if(id[i].indexOf(Utils.TOPIC) >= 0) topicId = id[i];
				else if(id[i].indexOf(Utils.POST) >= 0) postId = id[i];
			}
			Category category = null;
			Forum forum = null;
			Topic topic = null;
			Post post = null;
			try{
				category = forumService.getCategory(cateId) ;
				forum = forumService.getForum(cateId, forumId) ;
				topic = forumService.getTopic(cateId, forumId, topicId, userProfile.getUserId());
				post = forumService.getPost(cateId , forumId, topicId, path) ;
			} catch (Exception e) {
			}
			isRead = uiForm.canView(category, forum, topic, post, userProfile);
			
			if(type.equals(Utils.CATEGORY)) {
				String categoryId = forumSearch.getId() ;
				if(category != null) {
					if(isRead){
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.getChild(UICategory.class).update(category, null);
						categoryContainer.updateIsRender(false) ;
						forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(categoryId);
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					}
				} else isErro = true ;
			} else if(type.equals(Utils.FORUM)) {
				if(forum != null) {
					if(isRead) {
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						uiForumContainer.setIsRenderChild(true) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
						uiTopicContainer.setUpdateForum(cateId, forum, 0) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((cateId+"/"+forumId));
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					}
				} else isErro = true ;
			} else if(type.equals(Utils.TOPIC)){
				if(topic != null) {
					if(isRead){
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
						uiForumContainer.setIsRenderChild(false) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
						uiTopicDetail.setUpdateForum(forum) ;
						uiTopicDetail.setTopicFromCate(cateId, forumId, topic, 0) ;
						uiTopicDetail.setIdPostView("top") ;
						uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(cateId, forumId , topic.getId()) ;
						forumService.updateTopicAccess(forumPortlet.getUserProfile().getUserId(),	topic.getId()) ;
						forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((cateId + "/" + forumId + " "));
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					}
				} else isErro = true ;
			} else {
				if(post != null) {
					if(isRead){
						UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true)	;
						UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670) ;
						viewPost.setPostView(post) ;
						viewPost.setViewUserInfo(false) ;
						viewPost.setActionForm(new String[] {"Close", "OpenTopicLink"});
						event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					}
				} else isErro = true ;
			}
			if(isErro) {
				Object[] args = { };
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", args, ApplicationMessage.WARNING)) ;
				for(ForumSearch search : uiForm.listEvent){
					if(search.getId().equals(objId)){
						uiForm.listEvent.remove(search);
						return;
					}
				}
			}
			if(!isRead) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				String[] s = new String[]{};
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
				return;
			}
		}
	}
	static	public class CloseActionListener extends EventListener<UIForumListSearch> {
		public void execute(Event<UIForumListSearch> event) throws Exception {
			UIForumListSearch uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.findFirstComponentOfType(UICategories.class).setIsRenderChild(false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
}
