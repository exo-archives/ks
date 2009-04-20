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
package org.exoplatform.forum.webui.popup;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.info.UIForumQuickReplyPortlet;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * October 2, 2007	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIViewPost.gtmpl",
		events = {
			@EventConfig(listeners = UIViewPost.CloseActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIViewPost.ApproveActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIViewPost.DeletePostActionListener.class, phase = Phase.DECODE),
			@EventConfig(listeners = UIViewPost.DownloadAttachActionListener.class, phase = Phase.DECODE)
		}
)
public class UIViewPost extends UIForm implements UIPopupComponent {
	private Post post;
	private boolean isViewUserInfo = true ;
	public UIViewPost() {
	}
	
	public void setActionForm(String[] actions) {
	  this.setActions(actions);
  }
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		UserProfile userProfile = new UserProfile();
		try {
			userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
		} catch (Exception e) {
			String userName = ForumSessionUtils.getCurrentUser();
			if (userName != null) {
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider();
				try {
					ForumService forumService = (ForumService) PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class);
					userProfile = forumService.getQuickProfile(sProvider, userName);
				} catch (Exception ex) {
				} finally {
					sProvider.close();
				}
			}
		}
		return userProfile;
	}
	
	public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
	@SuppressWarnings("unused")
	private String getFileSource(ForumAttachment attachment) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			InputStream input = attachment.getInputStream() ;
			String fileName = attachment.getName() ;
			return ForumSessionUtils.getFileSource(input, fileName, dservice);
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	public void setPostView(Post post) throws Exception {
		this.post = post ;
	}
	
	@SuppressWarnings("unused")
	private Post getPostView() throws Exception {
		return post ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setViewUserInfo(boolean isView){ this.isViewUserInfo = isView ;}
	public boolean getIsViewUserInfo(){ return this.isViewUserInfo ;}
	
	static public class DownloadAttachActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost viewPost = event.getSource() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(viewPost) ;
		}
	}
	
	static	public class ApproveActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost uiForm = event.getSource() ;
			Post post = uiForm.post;
			post.setIsApproved(true);
			post.setIsHidden(false);
			List<Post> posts = new ArrayList<Post>();
			posts.add(post);
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try{
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				forumService.modifyPost(sProvider, posts, 1);
				forumService.modifyPost(sProvider, posts, 2);
			}catch(Exception e) {
				e.printStackTrace() ;
			}finally {
				sProvider.close() ;
			}
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer != null) {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				UIModerationForum moderationForum = popupContainer.getChild(UIModerationForum.class);
				if(moderationForum != null)
					event.getRequestContext().addUIComponentToUpdateByAjax(moderationForum) ;
			} else {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
			}
		}
	}
	
	static	public class DeletePostActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost uiForm = event.getSource() ;
			Post post = uiForm.post;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try{
				String []path = post.getPath().split("/");
				int l = path.length ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				forumService.removePost(sProvider, path[l-4], path[l-3], path[l-2], post.getId());
			}catch(Exception e) {
				e.printStackTrace() ;
			}finally {
				sProvider.close() ;
			}
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer != null) {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
			}
		}
	}

	static	public class CloseActionListener extends EventListener<UIViewPost> {
		public void execute(Event<UIViewPost> event) throws Exception {
			UIViewPost uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer != null) {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				popupAction.deActivate();
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				try {
					UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.cancelAction() ;
        } catch (Exception e) {
        	UIForumQuickReplyPortlet forumPortlet = uiForm.getAncestorOfType(UIForumQuickReplyPortlet.class) ;
        	forumPortlet.cancelAction() ;
        }
			}
		}
	}

}
