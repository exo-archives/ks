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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 11-03-2008, 09:13:50
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UISplitTopicForm.gtmpl",
		events = {
			@EventConfig(listeners = UISplitTopicForm.SaveActionListener.class), 
			@EventConfig(listeners = UISplitTopicForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UISplitTopicForm extends UIForm implements UIPopupComponent {
	private List<Post> posts = new ArrayList<Post>() ;
	private Topic topic = new Topic() ;
	private UserProfile userProfile = null;
	public static final String FIELD_SPLITTHREAD_INPUT = "SplitThread" ;
	public UISplitTopicForm() {
		addUIFormInput(new UIFormStringInput(FIELD_SPLITTHREAD_INPUT,FIELD_SPLITTHREAD_INPUT, null));
  }
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	@SuppressWarnings({ "unused", "unchecked" })
  private List<Post> getListPost() {
		for (Post post : this.posts) {
			if(getUIFormCheckBoxInput(post.getId()) != null) {
				getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
			}
    }
		return this.posts ; 
	}
	public void setListPost(List<Post> posts) { this.posts = posts ;}
	@SuppressWarnings("unused")
  private Topic getTopic() {return this.topic ;}
	public void setTopic(Topic topic) { this.topic = topic; }
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() {return this.userProfile ;}
	public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
	
	static	public class SaveActionListener extends EventListener<UISplitTopicForm> {
    public void execute(Event<UISplitTopicForm> event) throws Exception {
    	UISplitTopicForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	static	public class CancelActionListener extends EventListener<UISplitTopicForm> {
		public void execute(Event<UISplitTopicForm> event) throws Exception {
			UISplitTopicForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
