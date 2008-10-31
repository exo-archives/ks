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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
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
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIViewPost;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *		tu.duy@exoplatform.com
 * 14 Apr 2008, 08:22:52	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/UIForumListSearch.gtmpl",
		events = {
			@EventConfig(listeners = UIForumListSearch.OpentContentActionListener.class),
			@EventConfig(listeners = UIForumListSearch.CloseActionListener.class),
			@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
		}
)
public class UIForumListSearch extends UIForumKeepStickPageIterator {
	private List<ForumSearch> listEvent = null ;
	private JCRPageList pageList ;
	private List<ForumSearch> list = null;
	private boolean isShowIter = true;
	public UIForumListSearch() throws Exception {}
	
	public void setListSearchEvent(List<ForumSearch> listEvent) {
		this.listEvent = listEvent ;
		pageList = new ForumPageList(10, listEvent.size());
		pageList.setPageSize(10);
		this.updatePageList(pageList);
		isShowIter = true;
		try {
			if(this.getInfoPage().get(3) <= 1) isShowIter = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean getIsShowIter() {
	  return isShowIter ;
  }
	
	@SuppressWarnings("unchecked")
	public List<ForumSearch> getListEvent() {
		list = new ArrayList<ForumSearch>();
		try {
			list.addAll(this.pageList.getPageSearch(pageSelect, this.listEvent)) ;
			if(list.isEmpty()){
				while(list.isEmpty() && pageSelect > 1) {
					list.addAll(this.pageList.getPageSearch(--pageSelect, this.listEvent)) ;
				}
			}
		} catch (Exception e) {
		}
		return list ;
	}
	
	private ForumSearch getForumSearch(String id) {
		for (ForumSearch forumSearch : this.listEvent) {
			if(forumSearch.getId().equals(id)) return forumSearch ;
		}
		return null;
	}
	
	private boolean canView(Category category, Forum forum, Topic topic, Post post, UserProfile userProfile) throws Exception{
		boolean canView = true;
		boolean isModerator = false;
		if(category == null) return false;
		String[] listUsers = category.getUserPrivate();
		//check category is private:
		if(listUsers.length > 0 && listUsers[0].trim().length() > 0 && !ForumServiceUtils.hasPermission(listUsers, userProfile.getUserId())) 
			return false;
		else
			canView = true;
		
		// check forum
		if(forum != null){
			listUsers = forum.getModerators();
			if(userProfile.getUserRole() == 0 || (listUsers.length > 0 && listUsers[0].trim().length() > 0 && 
					ForumServiceUtils.hasPermission(listUsers, userProfile.getUserId()))) {
				isModerator = true;
				canView = true;
			} else if(forum.getIsClosed() || forum.getIsLock()) return false;
			else canView = true;
			
			// ckeck Topic:
			if(topic != null){
				if(isModerator) canView = true;
				else if(!topic.getIsClosed() && !topic.getIsLock() && topic.getIsActive() && topic.getIsActiveByForum() && topic.getIsApproved() && 
								!topic.getIsWaiting() &&((topic.getCanPost().length == 1 && topic.getCanPost()[0].trim().length() < 1) ||
									ForumServiceUtils.hasPermission(topic.getCanPost(), userProfile.getUserId()) || 
									(topic.getCanView().length == 1 && topic.getCanView()[0].trim().length() < 1) ||
									ForumServiceUtils.hasPermission(topic.getCanView(), userProfile.getUserId()))) canView = true;
				else canView = false;
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
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			
			String []id = path.split("/") ;
			Category category = null;
			Forum forum = null;
			Topic topic = null;
			Post post = null;
			
			try{
				String cateId =  id[3];
				category = forumService.getCategory(ForumSessionUtils.getSystemProvider(), cateId) ;
				String forumId = id[4];
				forum = forumService.getForum(ForumSessionUtils.getSystemProvider(),cateId , forumId ) ;
				String topicId = id[5];
				topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), cateId, forumId, topicId, userProfile.getUserId());
				String postId = id[6];
				post = forumService.getPost(ForumSessionUtils.getSystemProvider(), cateId , forumId, topicId, postId) ;
			} catch (Exception e) { }
			
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
				int length = id.length ;
				if(forum != null) {
					if(isRead) {
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						uiForumContainer.setIsRenderChild(true) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
						uiTopicContainer.setUpdateForum(id[length-2], forum) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-2]+"/"+id[length-1]));
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					}
				} else isErro = true ;
			} else if(type.equals(Utils.TOPIC)){
				int length = id.length ;
				if(topic != null) {
					if(isRead){
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
						uiForumContainer.setIsRenderChild(false) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
						uiTopicDetail.setTopicFromCate(id[length-3], id[length-2] , topic, true) ;
						uiTopicDetail.setUpdateForum(forum) ;
						uiTopicDetail.setIdPostView("top") ;
						uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(id[length-3], id[length-2] , topic) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-3] + "/" + id[length-2] + " "));
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
						event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					}
				} else isErro = true ;
			}
			if(isErro) {
				Object[] args = { };
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", args, ApplicationMessage.WARNING)) ;
				/*UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);*/
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
