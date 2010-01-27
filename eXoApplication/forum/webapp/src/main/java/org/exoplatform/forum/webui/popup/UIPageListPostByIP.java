/*
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
 */
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * 06-03-2008, 04:41:47
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/forum/webui/popup/UIPageListPostByIP.gtmpl",
		events = {
			@EventConfig(listeners = UIPageListPostByIP.OpenPostLinkActionListener.class),
			@EventConfig(listeners = UIPageListPostByIP.SetOrderByActionListener.class),
			@EventConfig(listeners = UIPageListPostByIP.CancelActionListener.class),
			@EventConfig(listeners = UIPageListPostByIP.DeletePostLinkActionListener.class)
		}
)
public class UIPageListPostByIP  extends BaseUIForm implements UIPopupComponent  {
	private ForumService forumService ;
	private UserProfile userProfile = null ;
	private String userName = "";
	private String ip_ = null;
	private String strOrderBy = "createdDate descending";
	private boolean hasEnableIPLogging = true;
	private List<Post> posts = new ArrayList<Post>() ;
	public UIPageListPostByIP() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		this.userName = null ;
		addChild(UIForumPageIterator.class, null, "PageListPostByUser") ;
		this.setActions(new String[]{"Cancel"});
	}

	public boolean getHasEnableIPLogging() {
		return hasEnableIPLogging;
	}
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		if(this.userProfile == null) {
			UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
			this.userProfile = forumPortlet.getUserProfile() ;
			hasEnableIPLogging = forumPortlet.isEnableIPLogging();
		}
		return this.userProfile ;
	}

	public String getUserName() { return userName;}
	public void setUserName(String userId) { this.userName = userId ;}

	@SuppressWarnings("unused")
	private String getTitleInHTMLCode(String s) {
		return ForumTransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
	}
	
	public void setIp(String ip){
		this.ip_ = ip;
		strOrderBy = "createdDate descending";
	}

	@SuppressWarnings({ "unchecked", "unused" })
	private List<Post> getPostsByUser() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		List<Post> posts = null;
		try {
			boolean isMod = false;
			if(this.userProfile.getUserRole() < 2) isMod = true;
			JCRPageList pageList	= forumService.getListPostsByIP(ip_, strOrderBy);
			forumPageIterator.updatePageList(pageList) ;
			if(pageList != null) pageList.setPageSize(6) ;
			posts = pageList.getPage(forumPageIterator.getPageSelected());
			forumPageIterator.setSelectPage(pageList.getCurrentPage());
		}catch (Exception e) {}
		if(posts == null) posts = new ArrayList<Post>();
		this.posts = posts ;
		return posts ;
	}

	private Post getPostById(String postId) {
		for(Post post : this.posts) {
			if(post.getId().equals(postId)) return post ;
		}
		return null ;
	}

	static	public class OpenPostLinkActionListener extends BaseEventListener<UIPageListPostByIP> {
    public void onEvent(Event<UIPageListPostByIP> event, UIPageListPostByIP uiForm, final String postId) throws Exception {
			Post post = uiForm.getPostById(postId) ;
			if(post == null){
				warning("UIShowBookMarkForm.msg.link-not-found") ;
				return ;
			}
			boolean isRead = true;
			if(uiForm.userProfile.getUserRole() > 0) {
				String ids[] = post.getPath().split("/");
				int leng = ids.length;
				String categoryId = ids[leng - 4];
				String forumId = ids[leng - 3];
				String topicId = ids[leng - 2];
				try {
					Category category = uiForm.forumService.getCategory(categoryId);
					if(category == null) {
						warning("UIShowBookMarkForm.msg.link-not-found") ;
						return ;
					}
					String[] privateUser = category.getUserPrivate();
					if(privateUser != null && privateUser.length > 0) {
						if(privateUser.length ==1 && privateUser[0].equals(" ")){
							isRead = true;
						} else {
							isRead = ForumServiceUtils.hasPermission(privateUser, uiForm.userProfile.getUserId());
						}
					}
					if(isRead) {
						String path_ = "" ;
						Forum forum = uiForm.forumService.getForum(categoryId , forumId ) ;
						if(forum != null ) path_ = forum.getPath()+"/"+topicId ;
						Topic topic = uiForm.forumService.getTopicByPath(path_, false) ;
						if(forum == null || topic == null) {
							warning("UIForumPortlet.msg.do-not-permission") ;
							return;
						}
						if(uiForm.userProfile.getUserRole() == 1 && (forum.getModerators() != null && forum.getModerators().length > 0 && 
								ForumServiceUtils.hasPermission(forum.getModerators(), uiForm.userProfile.getUserId()))) isRead = true;
						else isRead = false;

						if(!isRead && !forum.getIsClosed()){
							List<String> listUserPermission = new ArrayList<String>();
							if (forum.getCreateTopicRole() != null && forum.getCreateTopicRole().length > 0) 
								listUserPermission.addAll(Arrays.asList(forum.getCreateTopicRole()));

							if(forum.getViewer() != null && forum.getViewer().length > 0 )
								listUserPermission.addAll(Arrays.asList(forum.getViewer()));

							if(ForumServiceUtils.hasPermission(listUserPermission.toArray(new String[]{}), uiForm.userProfile.getUserId())) isRead = true;

							// check for topic:
							if(!isRead && post.getIsActiveByTopic() && post.getIsApproved() && !post.getIsHidden() && topic.getIsActive() &&
									topic.getIsActiveByForum() && topic.getIsApproved() && !topic.getIsClosed() && !topic.getIsWaiting()){
								if((topic.getCanPost().length == 1 && topic.getCanPost()[0].equals(" ")) || 
										ForumServiceUtils.hasPermission(topic.getCanPost(),uiForm.userProfile.getUserId()) ||
										(topic.getCanView().length == 1 && topic.getCanView()[0].equals(" ")) ||
										ForumServiceUtils.hasPermission(topic.getCanView(),uiForm.userProfile.getUserId())) isRead = true;
								else isRead = false;
							} else {
								isRead = false;
							}
						}
					}
				} catch (Exception e) {
					warning("UIShowBookMarkForm.msg.link-not-found") ;
				}
			}
			if(isRead){
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700) ;
				viewPost.setPostView(post) ;
				viewPost.setViewUserInfo(false) ;
				viewPost.setActionForm(new String[] {"Close", "OpenTopicLink"});
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				warning("UIForumPortlet.msg.do-not-permission") ;
				return;
			}
		}
	}

	static	public class DeletePostLinkActionListener extends EventListener<UIPageListPostByIP> {
		public void execute(Event<UIPageListPostByIP> event) throws Exception {
			UIPageListPostByIP uiForm = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Post post = uiForm.getPostById(postId);
			String[] path = post.getPath().split("/");
			int length = path.length;
			String topicId = path[length - 2];
			String forumId = path[length - 3];
			String categoryId = path[length - 4];
			if(topicId.replaceFirst(Utils.TOPIC, Utils.POST).equals(postId)){
				try {
					uiForm.forumService.removeTopic(categoryId, forumId, topicId);
				}catch (Exception e) {}
			} else {
				try {
					uiForm.forumService.removePost(categoryId, forumId, topicId, postId);
				}catch (Exception e) {}
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	static public class SetOrderByActionListener extends EventListener<UIPageListPostByIP> {
		public void execute(Event<UIPageListPostByIP> event) throws Exception {
			UIPageListPostByIP uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(uiContainer.strOrderBy)) {
				if(uiContainer.strOrderBy.indexOf(path) >= 0) {
					if(uiContainer.strOrderBy.indexOf("descending") > 0) {
						uiContainer.strOrderBy = path + " ascending";
					} else {
						uiContainer.strOrderBy = path + " descending";
					}
				} else {
					uiContainer.strOrderBy = path + " ascending";
				}
			} else {
				uiContainer.strOrderBy = path + " ascending";
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UIPageListPostByIP> {
		public void execute(Event<UIPageListPostByIP> event) throws Exception {
			UIPageListPostByIP pageListPostByIP = event.getSource();
			UIPopupContainer popupContainer = pageListPostByIP.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
			popupAction.deActivate() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	public void activate() throws Exception {	}
	public void deActivate() throws Exception {	}

}
