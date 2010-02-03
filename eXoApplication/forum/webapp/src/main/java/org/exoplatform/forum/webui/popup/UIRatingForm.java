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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

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
public class UIRatingForm extends BaseUIForm implements UIPopupComponent {
	private Topic topic ;
	private String categoryId ;
	private String forumId ;
	
	public UIRatingForm() throws Exception {}
	
	public void updateRating(Topic topic,	String categoryId, String forumId) {
		this.topic = topic ;
		this.categoryId = categoryId ;
		this.forumId = forumId ;
	}
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class VoteTopicActionListener extends  BaseEventListener<UIRatingForm> {
    public void onEvent(Event<UIRatingForm> event, UIRatingForm uiForm, final String vote) throws Exception {
			Topic topic = uiForm.topic ;
			String userName = UserHelper.getCurrentUser() ;
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
			ForumService forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			try {
				forumService.saveTopic(uiForm.categoryId, uiForm.forumId, topic, false, true, ForumUtils.getDefaultMail()) ;
			} catch (PathNotFoundException e) {
				warning("UIRatingForm.msg.forum-deleted") ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} 
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
