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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JobWattingForModerator;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumKeepStickPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

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
			@EventConfig(listeners = UIModerationForum.ApplyAllActionListener.class),
			@EventConfig(listeners = UIModerationForum.SelectTabActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIModerationForum.CloseActionListener.class, phase=Phase.DECODE)
		}
)

public class UIModerationForum extends UIForumKeepStickPageIterator implements UIPopupComponent {
	private UserProfile userProfile ;
	private ForumService forumService;
	private JobWattingForModerator wattingForModerator;
	private String[] path = new String[]{};
	private int id = 0 ;
	private Long pages[] = new Long[]{(long)1,(long)1,(long)1,(long)1};  
	public UIModerationForum() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
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
			} 
		}
		return path;
	}
	
	public void setPath(String[] path) {
		this.path = path;
	}
	
	@SuppressWarnings("unused")
  private String getTabId(int i) throws Exception {
		String[]array = new String[]{"TopicUnApproved","TopicWaiting","PostUnApproved","PostsHidden"};
		return array[i];
	}
	
	@SuppressWarnings("unused")
	private void setJobWattingForModerator() throws Exception {
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			wattingForModerator = forumService.getJobWattingForModerator(sProvider, this.getPath()) ;
    } catch (Exception e) {
    	wattingForModerator = new JobWattingForModerator();
    	e.printStackTrace();
    } finally {
    	sProvider.close();
    }
    wattingForModerator.getTopicUnApproved();
	}
	
	private List<Topic> getListTopic(int type) throws Exception {
		List<Topic> list = new ArrayList<Topic>();
		if(type == 0) {
			pageList = wattingForModerator.getTopicUnApproved();
		} else {
			pageList = wattingForModerator.getTopicWaiting();
		}
		long page = getPageSelect();
		list = pageList.getPage(page);
		pages[type] = pageList.getCurrentPage();
		return list;
	}

	private List<Topic> getListPost(int type) throws Exception {
		List<Topic> list = new ArrayList<Topic>();
		if(type == 2) {
			pageList = wattingForModerator.getPostsUnApproved();
		} else {
			pageList = wattingForModerator.getPostsHidden();
		}
		long page = getPageSelect();
		list = pageList.getPage(page);
		pages[type] = pageList.getCurrentPage();
		return list;
	}

	@SuppressWarnings("unused")
	private boolean getIsSelected(int id) {
		if(this.id == id) return true ;
		return false ;
	}
	
	static	public class OpenActionListener extends EventListener<UIModerationForum> {
		public void execute(Event<UIModerationForum> event) throws Exception {
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String type = event.getRequestContext().getRequestParameter("type") ;
		}
	}

	static	public class SelectTabActionListener extends EventListener<UIModerationForum> {
		public void execute(Event<UIModerationForum> event) throws Exception {
			String tabType = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIModerationForum uiform = event.getSource();
			uiform.id = Integer.parseInt(tabType);
			uiform.setPageSelect(uiform.pages[uiform.id]);
			event.getRequestContext().addUIComponentToUpdateByAjax(uiform) ;
		}
	}

	static	public class ApplyAllActionListener extends EventListener<UIModerationForum> {
		public void execute(Event<UIModerationForum> event) throws Exception {
			String objectType = event.getRequestContext().getRequestParameter(OBJECTID) ;
		}
	}
	
	static	public class CloseActionListener extends EventListener<UIModerationForum> {
		public void execute(Event<UIModerationForum> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
