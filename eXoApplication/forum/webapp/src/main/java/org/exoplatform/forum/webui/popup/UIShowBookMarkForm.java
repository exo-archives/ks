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

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
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
public class UIShowBookMarkForm extends UIForm implements UIPopupComponent{
	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private UserProfile userProfile ;
	private boolean isOpen = true;
	
	private String []bookMark = new String[]{}; 
	public UIShowBookMarkForm() {
	}
	
	public void activate() throws Exception {	}
	public void deActivate() throws Exception {	}
	
	@SuppressWarnings("unused")
	private String[] getBookMark() throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		bookMark = this.userProfile.getBookmark() ;
		return bookMark ;
	} 
	
	private String getBookMarkId(String id) throws Exception {
		for (String str : this.bookMark) {
			if(str.indexOf(id) > 0) return str ;
		}
		return "";
	}
	static	public class OpenLinkActionListener extends EventListener<UIShowBookMarkForm> {
		public void execute(Event<UIShowBookMarkForm> event) throws Exception {
			UIShowBookMarkForm bookMark = event.getSource() ;
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIForumPortlet forumPortlet = bookMark.getAncestorOfType(UIForumPortlet.class) ;
			UIApplication uiApp = bookMark.getAncestorOfType(UIApplication.class) ;
			String []id = path.split("/") ;
			int length = id.length ;
			String userName = bookMark.userProfile.getUserId();
			boolean isRead = true;
			Category category = bookMark.forumService.getCategory(ForumSessionUtils.getSystemProvider(), id[0]);
			if(category == null) {
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
				path = bookMark.getBookMarkId(path) ;
				if(!ForumUtils.isEmpty(path)) {
					bookMark.forumService.saveUserBookmark(ForumSessionUtils.getSystemProvider(), bookMark.userProfile.getUserId(), path, false) ;
					forumPortlet.setUserProfile() ;
				}
				return ;
			}
			String[] privateUser = category.getUserPrivate() ;
			if(bookMark.userProfile.getUserRole() > 0 && privateUser.length > 0 && !privateUser[0].equals(" ")) {
				isRead = ForumUtils.isStringInStrings(privateUser, userName);
			}
			if(!isRead){
				length = 0;
			}
			if(length == 3) {
				String path_ = "" ;
				Forum forum = bookMark.forumService.getForum(ForumSessionUtils.getSystemProvider(),id[0] , id[1] ) ;
				if(forum != null)path_ = forum.getPath()+"/"+id[2] ;
				Topic topic = bookMark.forumService.getTopicByPath(ForumSessionUtils.getSystemProvider(), path_, false) ;
				if(forum == null || topic == null) {
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
					path = bookMark.getBookMarkId(path) ;
					if(!ForumUtils.isEmpty(path)) {
						bookMark.forumService.saveUserBookmark(ForumSessionUtils.getSystemProvider(), bookMark.userProfile.getUserId(), path, false) ;
						forumPortlet.setUserProfile() ;
					}
					return ;
				}
				if(bookMark.userProfile.getUserRole() > 0){
					if(isRead &&topic.getCanView() != null && topic.getCanView()[0].equals(" ")){
						isRead = ForumUtils.isStringInStrings(topic.getCanView(), userName);
					}
					if(!isRead && forum.getModerators() != null && forum.getModerators().length > 0) {
						isRead = ForumUtils.isStringInStrings(forum.getModerators(), userName);
					}
				} else isRead = true; 
				if(isRead){
					forumPortlet.updateIsRendered(ForumUtils.FORUM);
					UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
					UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
					uiForumContainer.setIsRenderChild(false) ;
					UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
					uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
					uiTopicDetail.setTopicFromCate(id[0], id[1] , topic, true) ;
					uiTopicDetail.setUpdateForum(forum) ;
					uiTopicDetail.setIdPostView("false") ;
					uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(id[0], id[1] , topic) ;
					forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + "/" + id[1] + " "));
				}
			} else if(length == 2){
				Forum forum = bookMark.forumService.getForum(ForumSessionUtils.getSystemProvider(),id[0] , id[1] ) ;
				if(forum == null) {
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
					path = bookMark.getBookMarkId(path) ;
					if(!ForumUtils.isEmpty(path)) {
						bookMark.forumService.saveUserBookmark(ForumSessionUtils.getSystemProvider(), bookMark.userProfile.getUserId(), path, false) ;
						forumPortlet.setUserProfile() ;
					}
					return ;
				}
				if(forum.getIsClosed()) {
					if(bookMark.userProfile.getUserRole() > 0){
						if(forum.getModerators() != null && forum.getModerators().length > 0) {
							isRead = ForumUtils.isStringInStrings(forum.getModerators(), userName);
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
					uiTopicContainer.setUpdateForum(id[0], forum) ;
					forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0]+"/"+id[1]));
				}
			} else if(length == 1){
				if(!isRead && bookMark.userProfile.getUserRole() == 0) isRead = true;
				if(isRead){
					List<Forum> list = bookMark.forumService.getForums(ForumSessionUtils.getSystemProvider(), path, "");
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.getChild(UICategory.class).update(category, list);
					categoryContainer.updateIsRender(false) ;
					forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(path);
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				}
			}
			if(!isRead) {
				String[] s = new String[]{};
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
				return;
			}
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class DeleteLinkActionListener extends EventListener<UIShowBookMarkForm> {
		public void execute(Event<UIShowBookMarkForm> event) throws Exception {
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIShowBookMarkForm bookMark = event.getSource() ;
			bookMark.forumService.saveUserBookmark(ForumSessionUtils.getSystemProvider(), bookMark.userProfile.getUserId(), path, false) ;
			UIForumPortlet forumPortlet = bookMark.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.setUserProfile() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(bookMark.getParent()) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UIShowBookMarkForm> {
		public void execute(Event<UIShowBookMarkForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	public boolean getIsOpen() {
  	return isOpen;
  }

	public void setIsOpen(boolean isOpen) {
  	this.isOpen = isOpen;
  }
}
