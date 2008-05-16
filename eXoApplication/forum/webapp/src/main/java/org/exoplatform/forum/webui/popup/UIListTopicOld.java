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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 16, 2008 - 5:02:24 AM  
 */
@ComponentConfig(
		template =	"app:/templates/forum/webui/popup/UIListTopicOldForm.gtmpl" ,
		events = {
				@EventConfig(listeners = UIListTopicOld.ActiveTopicActionListener.class),
				@EventConfig(listeners = UIListTopicOld.DeleteTopicActionListener.class),
				@EventConfig(listeners = UIListTopicOld.OpenTopicActionListener.class)
		}
)
public class UIListTopicOld extends UIContainer {
	private ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private UserProfile userProfile = null;
	private List<Topic> topics = new ArrayList<Topic>() ;
	private long date = 0 ;
	public UIListTopicOld() throws Exception {
		addChild(UIForumPageIterator.class, null, "PageListTopicTopicOld") ;
	}
	
	public long getDate() { return date;}
	public void setDate(long date) {this.date = date;}
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() throws Exception {
		if(userProfile == null) {
			this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
		}
		return userProfile ;
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
  private List<Topic> getTopicsOld() throws Exception {
		JCRPageList pageList = forumService.getPageTopicOld(ForumSessionUtils.getSystemProvider(), date);
		UIForumPageIterator pageIterator = this.getChild(UIForumPageIterator.class) ;
		pageIterator.updatePageList(pageList) ;
		long page = pageIterator.getPageSelected() ;
		List<Topic> topics = new ArrayList<Topic>();
		if(pageList != null)topics = pageList.getPage(page) ;
		this.topics = topics ;
		return topics ;
	}
	
	public Topic getTopicById(String topicId) {
    for(Topic topic : this.topics) {
      if(topic.getId().equals(topicId)) return topic ;
    }
    return null ;
  }
	
	static	public class ActiveTopicActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
		}
	}
	
	static	public class DeleteTopicActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
		}
	}

	static	public class OpenTopicActionListener extends EventListener<UIForumAdministrationForm> {
		public void execute(Event<UIForumAdministrationForm> event) throws Exception {
		}
	}
	
}
