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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 29-12-2008 - 04:43:19  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIModerationForum.gtmpl",
		events = {
			@EventConfig(listeners = UIModerationForum.OpenActionListener.class),
			@EventConfig(listeners = UIModerationForum.CloseActionListener.class, phase=Phase.DECODE)
		}
)

public class UIModerationForum extends UIForm implements UIPopupComponent {
	private UserProfile userProfile ;
	private ForumService forumService;
	private String[] path = new String[]{};
	List<ForumSearch> list_;
	private boolean isShowIter = true;
	public final String SEARCH_ITERATOR = "moderationIterator";
	private JCRPageList pageList ;
	private UIForumPageIterator pageIterator ;
	public UIModerationForum() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		pageIterator = addChild(UIForumPageIterator.class, null, SEARCH_ITERATOR);
		setActions(new String[]{"Close"});
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setUserProfile(UserProfile userProfile) throws Exception {
	  this.userProfile = userProfile;
	  if(this.userProfile == null) {
	  	this.userProfile = getAncestorOfType(UIForumPortlet.class).getUserProfile();
	  }
  }
	
	public String[] getPath() {
		if(userProfile.getUserRole() <= 1) {
			if(userProfile.getUserRole() == 1){
				path = this.userProfile.getModerateForums() ;
			} else path = new String[]{};
		}
		return path;
	}
	
	public void setPath(String[] path) {
		this.path = path;
	}

	public boolean getIsShowIter() {
	  return isShowIter ;
  }
	
	@SuppressWarnings({ "unused", "unchecked" })
  private List<ForumSearch> getListObject() throws Exception {
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			list_ = forumService.getJobWattingForModerator(sProvider, getPath()) ;
		} catch (Exception e) {
			list_ = new ArrayList<ForumSearch>();
			e.printStackTrace();
		} finally {
			sProvider.close();
		}
		pageList = new ForumPageList(10, list_.size());
		pageList.setPageSize(10);
		pageIterator.updatePageList(pageList);
		isShowIter = true;
		if(pageList.getAvailablePage() <= 1) isShowIter = false;
		long pageSelect = pageIterator.getPageSelected();
		List<ForumSearch>list = new ArrayList<ForumSearch>();
		try {
			list.addAll(pageList.getPageSearch(pageSelect, list_)) ;
		} catch (Exception e) {
		}
		pageSelect = pageList.getCurrentPage();
		return list ;
	}
	
	private ForumSearch getObject(String id) throws Exception {
		for (ForumSearch obj : list_) {
	    if(obj.getId().equals(id)) return obj;
    }
		return null;
	}
	
	static	public class OpenActionListener extends EventListener<UIModerationForum> {
		public void execute(Event<UIModerationForum> event) throws Exception {
			String objectId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIModerationForum moderationForum  = event.getSource();
			ForumSearch forumSearch = moderationForum.getObject(objectId); 
			UIPopupContainer popupContainer = moderationForum.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			if(forumSearch.getType().equals(Utils.TOPIC)) {
				try {
					Topic topic = moderationForum.forumService.getTopicByPath(sProvider, forumSearch.getPath(), false);
					UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 700) ;
					viewTopic.setTopic(topic) ;
					viewTopic.setActionForm(new String[] {"Close", "Approve", "DeleteTopic"});
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					sProvider.close();
				}
			} else {
				try {
	        Post post = moderationForum.forumService.getPost(sProvider, "", "", "", forumSearch.getPath());
					UIViewPost viewPost = popupAction.activate(UIViewPost.class, 700) ;
					viewPost.setPostView(post) ;
					viewPost.setViewUserInfo(false) ;
					viewPost.setActionForm(new String[] {"Approve", "DeletePost", "Close"});
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					sProvider.close();
				}
			}
		}
	}

	static	public class CloseActionListener extends EventListener<UIModerationForum> {
		public void execute(Event<UIModerationForum> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
