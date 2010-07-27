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
 *					tu.duy@exoplatform.com
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
	private ForumService forumService ;
	private UserProfile userProfile = null;
	private List<Topic> topics = new ArrayList<Topic>() ;
	private long date = 0 ;
	private boolean isUpdate = false ;
	public UIListTopicOld() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addChild(UIForumPageIterator.class, null, "PageListTopicTopicOld") ;
	}
	
	public long getDate() { return date;}
	public void setDate(long date) {
		if(this.date != 0){
			UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class);
			forumPageIterator.setSelectPage(1);
		}
		this.date = date;
	}
	
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		if(userProfile == null) {
			this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
		}
		return userProfile ;
	}
	
	public void setIsUpdate(boolean isUpdate){
		this.isUpdate = isUpdate;
	}
	
	@SuppressWarnings({ "unused", "unchecked" })
	private List<Topic> getTopicsOld() throws Exception {
		if(topics == null || topics.size() == 0 || isUpdate) {
			JCRPageList pageList = forumService.getPageTopicOld(date, "");
			if(pageList != null) {
				pageList.setPageSize(10) ;
				UIForumPageIterator pageIterator = this.getChild(UIForumPageIterator.class) ;
				pageIterator.updatePageList(pageList) ;
				int page = pageIterator.getPageSelected() ;
				List<Topic> topics = pageList.getPage(page) ;
				pageIterator.setSelectPage(pageList.getCurrentPage());
				if(topics == null)topics = new ArrayList<Topic>();
				this.topics = topics ;
			} else {
				this.topics.clear();
			}
			isUpdate = false ;
		}
		return this.topics ;
	}
	
	public Topic getTopicById(String topicId) {
		for(Topic topic : this.topics) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	static	public class ActiveTopicActionListener extends EventListener<UIListTopicOld> {
		public void execute(Event<UIListTopicOld> event) throws Exception {
			UIListTopicOld administration = event.getSource() ;
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			Topic topic = administration.getTopicById(topicId) ;
			boolean isActive = topic.getIsActive() ;
			topic.setIsActive(!isActive) ;
			List<Topic> topics = new ArrayList<Topic>();
			topics.add(topic);
			try {
				administration.forumService.modifyTopic(topics, 6) ;
				administration.isUpdate = true ;
			} catch (Exception e) {}
			event.getRequestContext().addUIComponentToUpdateByAjax(administration);
		}
	}
	
	static	public class DeleteTopicActionListener extends EventListener<UIListTopicOld> {
		public void execute(Event<UIListTopicOld> event) throws Exception {
			UIListTopicOld listTopicOld = event.getSource() ;
			String ids = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String []id = ids.split("/") ;
			int l = id.length ;
			try {
				listTopicOld.forumService.removeTopic(id[l-3], id[l-2],id[l-1]) ;
			} catch (Exception e) {}
			listTopicOld.isUpdate = true ;
			event.getRequestContext().addUIComponentToUpdateByAjax(listTopicOld.getAncestorOfType(UIForumPortlet.class));
		}
	}

	static	public class OpenTopicActionListener extends EventListener<UIListTopicOld> {
		public void execute(Event<UIListTopicOld> event) throws Exception {
			UIListTopicOld listTopicOld = event.getSource() ;
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			Topic topic = listTopicOld.getTopicById(topicId) ;
			UIForumAdministrationForm administrationForm = listTopicOld.getParent();
			UIPopupContainer popupContainer = administrationForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
			UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 700) ;
			viewTopic.setTopic(topic) ;
			viewTopic.setActionForm(new String[] {"Close"});
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
}
