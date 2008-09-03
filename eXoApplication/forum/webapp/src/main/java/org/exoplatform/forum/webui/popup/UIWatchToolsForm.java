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
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
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
 * 27-08-2008 - 04:36:33  
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIWatchToolsForm.gtmpl",
		events = {
			@EventConfig(listeners = UIWatchToolsForm.DeleteEmailActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIWatchToolsForm.EditEmailActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIWatchToolsForm.CloseActionListener.class, phase=Phase.DECODE)
		}
)
public class UIWatchToolsForm extends UIForm implements UIPopupComponent {
	private String path = "";
	private String[] emails = new String[]{};
	private boolean isTopic = true;
	public UIWatchToolsForm() {
	}
	public String getPath() {return path;}
	public void setPath(String path) {this.path = path;}

	public boolean getIsTopic() {return isTopic;}
	public void setIsTopic(boolean isTopic) {this.isTopic = isTopic;}
	
	public String[] getEmails() {return emails;}
	public void setEmails(String[] emails) {this.emails = emails;}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class DeleteEmailActionListener extends EventListener<UIWatchToolsForm> {
		public void execute(Event<UIWatchToolsForm> event) throws Exception {
			String email = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIWatchToolsForm uiForm = event.getSource();
			List<String>emails = new ArrayList<String>();
			emails.add(email) ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			forumService.removeWatch(ForumSessionUtils.getSystemProvider(), 1, uiForm.getPath(), emails) ;
			if(uiForm.getIsTopic()){
				UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
				topicDetail.setIsEditTopic(true) ;
			} else {
				UITopicContainer topicContainer= forumPortlet.findFirstComponentOfType(UITopicContainer.class);
				topicContainer.setIdUpdate(true);
			}
			String []strings = new String[(uiForm.emails.length-1)];
			int j = 0;
			for (int i = 0; i < uiForm.emails.length; i++) {
				if(uiForm.emails[i].equals(email)) continue ;
	      strings[j] = uiForm.emails[i]; ++j;
      }
			uiForm.emails = strings ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static	public class EditEmailActionListener extends EventListener<UIWatchToolsForm> {
		public void execute(Event<UIWatchToolsForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
	
	static	public class CloseActionListener extends EventListener<UIWatchToolsForm> {
		public void execute(Event<UIWatchToolsForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
