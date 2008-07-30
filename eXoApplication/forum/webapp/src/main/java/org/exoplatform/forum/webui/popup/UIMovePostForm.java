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
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
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
 * Aus 15, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIMovePostForm.gtmpl",
		events = {
			@EventConfig(listeners = UIMovePostForm.SaveActionListener.class), 
			@EventConfig(listeners = UIMovePostForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIMovePostForm extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String topicId ;
	private List<Post> posts ;
	public UIMovePostForm() throws Exception {
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void updatePost(String topicId, List<Post> posts) {
		this.topicId = topicId ;
		this.posts = posts ;
	}
	
	@SuppressWarnings("unused")
	private List<Category> getCategories() throws Exception {
		return this.forumService.getCategories(ForumSessionUtils.getSystemProvider()) ;
	}
	
	@SuppressWarnings("unused")
	private boolean getSelectForum(String forumId) throws Exception {
		if(this.posts.get(0).getPath().contains(forumId)) return true ;
		else return false ;
	}

	@SuppressWarnings("unused")
	private List<Forum> getForums(String categoryId) throws Exception {
		return this.forumService.getForums(ForumSessionUtils.getSystemProvider(), categoryId, "") ;
	}

	@SuppressWarnings("unused")
	private List<Topic> getTopics(String categoryId, String forumId) throws Exception {
		List<Topic> topics = new ArrayList<Topic>() ;
		for(Topic topic : this.forumService.getTopics(ForumSessionUtils.getSystemProvider(), categoryId, forumId)) {
			if(topic.getId().equalsIgnoreCase(this.topicId)) continue ;
			topics.add(topic) ;
		}
		return topics ;
	}
	
	static	public class SaveActionListener extends EventListener<UIMovePostForm> {
    public void execute(Event<UIMovePostForm> event) throws Exception {
			UIMovePostForm uiForm = event.getSource() ;
			String topicPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(!ForumUtils.isEmpty(topicPath)) {
				uiForm.forumService.movePost(ForumSessionUtils.getSystemProvider(), uiForm.posts, topicPath) ;
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
				String[] temp = topicPath.split("/") ;
				UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
				topicDetailContainer.getChild(UITopicDetail.class).setUpdateTopic(temp[temp.length - 3], temp[temp.length - 2], temp[temp.length - 1], false) ;
				topicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(temp[temp.length - 3], temp[temp.length - 2], temp[temp.length - 1]) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIMovePostForm> {
    public void execute(Event<UIMovePostForm> event) throws Exception {
			UIMovePostForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}