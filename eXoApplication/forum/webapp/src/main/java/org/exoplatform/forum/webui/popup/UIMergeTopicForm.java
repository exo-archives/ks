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
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UIMergeTopicForm.SaveActionListener.class), 
			@EventConfig(listeners = UIMergeTopicForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIMergeTopicForm extends UIForm implements UIPopupComponent {
	private List<Topic> listTopic ;
	public UIMergeTopicForm() throws Exception {
	}
	
	private void intAddChild() throws Exception {
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		if(this.listTopic != null && this.listTopic.size() > 0) {
			for (Topic topic : this.listTopic) {
	      list.add(new SelectItemOption<String>(topic.getTopicName(), topic.getId()));
      }
		}
		UIFormSelectBox destination = new UIFormSelectBox("destination", "destination", list) ;
		UIFormStringInput titleThread = new UIFormStringInput("title","title", null) ;
		if(this.listTopic != null && this.listTopic.size() > 0) {
			destination.setValue(this.listTopic.get(0).getId()) ;
			titleThread.setValue(this.listTopic.get(0).getTopicName());
		}
		addUIFormInput(destination) ;
		addUIFormInput(titleThread) ;
	}

	public void updateTopics(List<Topic> topics) {
		this.listTopic = topics ;
	}
	
	public void activate() throws Exception {
		intAddChild() ;
	}
	
	public void deActivate() throws Exception {}
	
	static	public class SaveActionListener extends EventListener<UIMergeTopicForm> {
    @SuppressWarnings("unchecked")
    public void execute(Event<UIMergeTopicForm> event) throws Exception {
			UIMergeTopicForm uiForm = event.getSource() ;
			String topicMergeId = uiForm.getUIFormSelectBox("destination").getValue() ;
			String topicMergeTitle = uiForm.getUIStringInput("title").getValue() ;
			Topic topicMerge = new Topic() ;
			for(Topic topic : uiForm.listTopic) {
				if(topicMergeId.equals(topic.getId())) {topicMerge = topic; break ;}
			}
			String destTopicPath = topicMerge.getPath() ;
			String temp[] = destTopicPath.split("/") ;
			String categoryId = temp[temp.length - 3] ;
			String forumId = temp[temp.length - 2] ;
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			for(Topic topic : uiForm.listTopic) {
				if(topicMergeId.equals(topic.getId())) {continue ;}
				JCRPageList pageList = forumService.getPosts(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId(), "") ;
				List<Post> posts = pageList.getPage(0) ;
				forumService.movePost(ForumSessionUtils.getSystemProvider(), posts, destTopicPath) ;
				forumService.removeTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId()) ;
			}
			topicMerge.setTopicName(topicMergeTitle) ;
			forumService.saveTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicMerge, false, false) ;
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIMergeTopicForm> {
    public void execute(Event<UIMergeTopicForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
		}
	}
}
