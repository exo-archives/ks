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

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * November 01 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIRatingForm.gtmpl",
		events = {
			@EventConfig(listeners = UIRatingForm.VoteTopicActionListener.class), 
			@EventConfig(listeners = UIRatingForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIRatingForm extends UIForm implements UIPopupComponent {
	private Topic topic ;
	private String categoryId ;
	private String forumId ;
	
	public UIRatingForm() throws Exception {
		
	}
	
	public void updateRating(Topic topic,	String categoryId, String forumId) {
		this.topic = topic ;
		this.categoryId = categoryId ;
		this.forumId = forumId ;
	}
	public void activate() throws Exception {
		// TODO Auto-generated method stub
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
	
	static	public class VoteTopicActionListener extends EventListener<UIRatingForm> {
		public void execute(Event<UIRatingForm> event) throws Exception {
			UIRatingForm uiForm = event.getSource() ;
			String vote = event.getRequestContext().getRequestParameter(OBJECTID)	;
			Topic topic = uiForm.topic ;
			
			String userName = ForumSessionUtils.getCurrentUser() ;
			String[] Vote = topic.getUserVoteRating() ;
			int k = Vote.length ;
			Double voteRating = topic.getVoteRating() ;
			voteRating = (voteRating*k + Integer.parseInt(vote))/(k+1) ;
			String[] temp = new String[k + 1] ;
			for (int i = 0; i < k; i++) {
				temp[i] = Vote[i] ;
			}
			temp[k] = userName ;
			topic.setVoteRating(voteRating) ;
			topic.setUserVoteRating(temp) ;
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				forumService.saveTopic(sProvider, uiForm.categoryId, uiForm.forumId, topic, false, true, ForumUtils.getDefaultMail()) ;
			} catch (PathNotFoundException e) {
				sProvider.close();
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIRatingForm.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			} finally {
				sProvider.close();
			}
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIRatingForm> {
		public void execute(Event<UIRatingForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
