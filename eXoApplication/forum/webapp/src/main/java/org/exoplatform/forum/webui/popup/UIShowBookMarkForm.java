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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Apr 30, 2008 - 8:19:21 AM	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIShowBookMarkForm.gtmpl",
		events = {
			@EventConfig(listeners = UIShowBookMarkForm.OpenLinkActionListener.class, phase=Phase.DECODE), 
			@EventConfig(listeners = UIShowBookMarkForm.DeleteLinkActionListener.class), 
			@EventConfig(listeners = UIShowBookMarkForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIShowBookMarkForm extends BaseUIForm implements UIPopupComponent{
	ForumService forumService ;
	public final String BOOKMARK_ITERATOR = "BookmarkPageIterator";
	private JCRPageList pageList ;
	UIForumPageIterator pageIterator ;
	private List<String> bookMarks = new ArrayList<String>();
	public UIShowBookMarkForm() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		pageIterator = addChild(UIForumPageIterator.class, null, BOOKMARK_ITERATOR);
	}
	
	public void activate() throws Exception {	}
	public void deActivate() throws Exception {	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private List<String> getBookMark() throws Exception {
		try{
			bookMarks = forumService.getBookmarks( this.getAncestorOfType(UIForumPortlet.class).getUserProfile().getUserId());
		}catch(Exception e) {
			e.printStackTrace() ;
		}
		pageList = new ForumPageList(6, bookMarks.size());
		pageList.setPageSize(6);
		pageIterator = this.getChild(UIForumPageIterator.class);
		pageIterator.updatePageList(pageList);
		List<String>list = new ArrayList<String>();
		list.addAll(this.pageList.getPageList(pageIterator.getPageSelected(), this.bookMarks)) ;
		pageIterator.setSelectPage(pageList.getCurrentPage());
		try {
			if(pageList.getAvailablePage() <= 1) pageIterator.setRendered(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list ;
	} 
	
	private String getBookMarkId(String id) throws Exception {
		for (String str : this.bookMarks) {
			if(str.indexOf(id) >= 0) return str ;
		}
		return "";
	}
	
	@SuppressWarnings("unused")
  private String getType(String id) {
		return (id.indexOf(Utils.FORUM_SERVICE) >= 0)? Utils.FORUM_SERVICE:(
					 (id.indexOf(Utils.CATEGORY) >= 0)? ForumUtils.CATEGORY :( 
					 (id.indexOf(Utils.FORUM) >= 0)? ForumUtils.FORUM :(
					 (id.indexOf(Utils.TOPIC) >= 0)? ForumUtils.TOPIC :(""))));
	}
	
	static	public class OpenLinkActionListener extends BaseEventListener<UIShowBookMarkForm> {
    public void onEvent(Event<UIShowBookMarkForm> event, UIShowBookMarkForm bookmarkForm, String id) throws Exception {
			UIForumPortlet forumPortlet = bookmarkForm.getAncestorOfType(UIForumPortlet.class) ;
			UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class);
			String userName = forumPortlet.getUserProfile().getUserId();
			long role = forumPortlet.getUserProfile().getUserRole();
			boolean isRead = true;
			try {
				if(id.indexOf(Utils.TOPIC) == 0) {
					Topic topic = (Topic)bookmarkForm.forumService.getObjectNameById(id, Utils.TOPIC);
					String ids[] = topic.getPath().split("/");
					int leng = ids.length;
					String categoryId = ids[leng - 3];
					String forumId = ids[leng - 2];
					Forum forum = bookmarkForm.forumService.getForum(categoryId, forumId ) ;
					if(forum == null || topic == null) {
						breadcumbs.setOpen(false) ;
						warning("UIShowBookMarkForm.msg.link-not-found") ;
						id = bookmarkForm.getBookMarkId(id) ;
						if(!ForumUtils.isEmpty(id)) {
							bookmarkForm.forumService.saveUserBookmark(forumPortlet.getUserProfile().getUserId(), id, false) ;
							forumPortlet.updateUserProfileInfo() ;
						}
						return ;
					}
					Category category = bookmarkForm.forumService.getCategory(categoryId);
					String[] privateUser = category.getUserPrivate() ;
					if(role > 0 && privateUser.length > 0 && !privateUser[0].equals(" ")) {
						isRead = ForumServiceUtils.hasPermission(privateUser, userName);
					}
					if(forum != null && forum.getIsClosed()) isRead = false;
					if(role > 0){
						boolean isMode = false;
						if(!isRead && forum.getModerators() != null && forum.getModerators().length > 0 && !forum.getModerators()[0].equals(" ")) {
							isMode = ForumServiceUtils.hasPermission(forum.getModerators(), userName);
						}  
						if(!isMode){
							if(isRead && topic.getCanView() != null && topic.getCanView().length > 0 && !topic.getCanView()[0].equals(" ")){
								isRead = ForumServiceUtils.hasPermission(topic.getCanView(), userName);
								if(!isRead)isRead = ForumServiceUtils.hasPermission(forum.getPoster(), userName);
								if(!isRead)isRead = ForumServiceUtils.hasPermission(forum.getViewer(), userName);
							}
						} else isRead = true;
					} else isRead = true; 
					if(isRead){
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
						uiForumContainer.setIsRenderChild(false) ;
						UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						uiTopicDetail.setUpdateForum(forum) ;
						uiTopicDetail.setTopicFromCate(categoryId, forumId , topic, 0) ;
						uiTopicDetail.setIdPostView("top") ;
						uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(categoryId, forumId , topic.getId()) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((categoryId + "/" + forumId + " "));
					}
				} else if(id.indexOf(Utils.FORUM) == 0 && id.indexOf(Utils.CATEGORY) < 0){
					Forum forum = (Forum)bookmarkForm.forumService.getObjectNameById(id, Utils.FORUM ) ;
					if(forum == null) {
						breadcumbs.setOpen(false) ;
						warning("UIShowBookMarkForm.msg.link-not-found") ;
						id = bookmarkForm.getBookMarkId(id) ;
						if(!ForumUtils.isEmpty(id)) {
							bookmarkForm.forumService.saveUserBookmark(forumPortlet.getUserProfile().getUserId(), id, false) ;
							forumPortlet.updateUserProfileInfo() ;
						}
						return ;
					}
					String ids[] = forum.getPath().split("/");
					int leng = ids.length;
					String categoryId = ids[leng - 2];
					Category category = bookmarkForm.forumService.getCategory(categoryId);
					String[] privateUser = category.getUserPrivate() ;
					if(role > 0 && privateUser.length > 0 && !privateUser[0].equals(" ")) {
						isRead = ForumServiceUtils.hasPermission(privateUser, userName);
					}
					if(forum.getIsClosed()) {
						if(role > 0){
							if(forum.getModerators() != null && forum.getModerators().length > 0) {
								isRead = ForumServiceUtils.hasPermission(forum.getModerators(), userName);
							}else {
								isRead = false;
							}
						}
					}
					if(isRead) {
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						uiForumContainer.setIsRenderChild(true) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
						uiTopicContainer.setUpdateForum(categoryId, forum, 0) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((categoryId+"/"+id));
					}
				} else if(id.indexOf(Utils.CATEGORY) >= 0){
					Category category = (Category)bookmarkForm.forumService.getObjectNameById(id, Utils.CATEGORY ) ;
					String[] privateUser = category.getUserPrivate() ;
					if(role > 0 && privateUser.length > 0 && !privateUser[0].equals(" ")) {
						isRead = ForumServiceUtils.hasPermission(privateUser, userName);
					}
					if(!isRead && role == 0) isRead = true;
					if(isRead){
						List<Forum> list = bookmarkForm.forumService.getForums(id, "");
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.getChild(UICategory.class).update(category, list);
						categoryContainer.updateIsRender(false) ;
						forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(id);
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					}
				}
			} catch (Exception e) {
				breadcumbs.setOpen(false) ;
				warning("UIShowBookMarkForm.msg.link-not-found") ;
				id = bookmarkForm.getBookMarkId(id) ;
				if(!ForumUtils.isEmpty(id)) {
					bookmarkForm.forumService.saveUserBookmark(forumPortlet.getUserProfile().getUserId(), id, false) ;
					forumPortlet.updateUserProfileInfo() ;
				}
				return ;
			}
			if(!isRead) {
				breadcumbs.setOpen(false) ;
				warning("UIForumPortlet.msg.do-not-permission") ;
				return;
			}
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class DeleteLinkActionListener extends BaseEventListener<UIShowBookMarkForm> {
    public void onEvent(Event<UIShowBookMarkForm> event, UIShowBookMarkForm bookmarkForm, String path) throws Exception {
			UIShowBookMarkForm bookMark = event.getSource() ;
			UIForumPortlet forumPortlet = bookMark.getAncestorOfType(UIForumPortlet.class) ;
			bookMark.forumService.saveUserBookmark(forumPortlet.getUserProfile().getUserId(), path, false) ;
			forumPortlet.updateUserProfileInfo() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(bookMark.getParent()) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UIShowBookMarkForm> {
		public void execute(Event<UIShowBookMarkForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
