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

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Tag;
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
 * 05-03-2008  
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/popup/UIPageListTopicUnApprove.gtmpl",
		events = {
				@EventConfig(listeners = UIPageListTopicUnApprove.OpenTopicActionListener.class ),
				@EventConfig(listeners = UIPageListTopicUnApprove.OpenTopicsTagActionListener.class )
		}
)
public class UIPageListTopicUnApprove extends UIContainer{
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String categoryId, forumId ;
	public UIPageListTopicUnApprove() throws Exception {
		addChild(UIForumPageIterator.class, null, "PageListTopicUnApprove") ;
  }
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() throws Exception {
		return this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void setUpdateContainer(String categoryId, String forumId) {
	  this.categoryId = categoryId ; this.forumId = forumId ;
  }
	
	@SuppressWarnings({ "unchecked", "unused" })
  private List<Topic> getTopicsByUser() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		JCRPageList pageList  = forumService.getPageTopic(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, "false") ;
		forumPageIterator.updatePageList(pageList) ;
		pageList.setPageSize(6) ;
		long page = forumPageIterator.getPageSelected() ;
		List<Topic> topics = pageList.getPage(page) ;
		return topics ;
	}
	
	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumFormatUtils.getStarNumber(voteRating) ;
	}
	
	@SuppressWarnings("unused")
	private String getStringCleanHtmlCode(String sms) {
		return ForumFormatUtils.getStringCleanHtmlCode(sms);
	}

	@SuppressWarnings("unused")
	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
		return this.forumService.getTagsByTopic(ForumSessionUtils.getSystemProvider(), tagIds);	
	}
	
	@SuppressWarnings("unused")
  private JCRPageList getPageListPost(String topicPath) throws Exception {
		String []id = topicPath.split("/") ;
		int i = id.length ;
		JCRPageList pageListPost = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(), id[i-3], id[i-2], id[i-1])	; 
//		long maxPost = getUserProfile().getMaxTopicInPage() ;
//		if(maxPost > 0) this.maxPost = maxPost ;
//		pageListPost.setPageSize(this.maxPost) ;
		return pageListPost;
	}
	static	public class OpenTopicActionListener extends EventListener<UIPageListTopicUnApprove> {
    public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
			//UIPageListPostByUser uiForm = event.getSource() ;
		}
	}
	static	public class OpenTopicsTagActionListener extends EventListener<UIPageListTopicUnApprove> {
		public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
			//UIPageListPostByUser uiForm = event.getSource() ;
		}
	}
	
}
