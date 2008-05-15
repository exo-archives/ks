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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
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
		template =	"app:/templates/forum/webui/popup/UIPageListTopicByUser.gtmpl",
		events = {
				@EventConfig(listeners = UIPageListTopicByUser.OpenTopicActionListener.class ),
				@EventConfig(listeners = UIPageListTopicByUser.OpenTopicsTagActionListener.class )
		}
)
public class UIPageListTopicByUser extends UIContainer{
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
  private List<Topic> topics = new ArrayList<Topic>() ;
  private UserProfile userProfile ;
  private String userName = new String() ;
	public UIPageListTopicByUser() throws Exception {
		addChild(UIForumPageIterator.class, null, "PageListTopicByUser") ;
  }
	
	@SuppressWarnings("unused")
  private UserProfile getUserProfile() throws Exception {
		return this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
  public void setUserName(String userName) {
    this.userName = userName ;
  }
  
	@SuppressWarnings({ "unchecked", "unused" })
  private List<Topic> getTopicsByUser() throws Exception {
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		JCRPageList pageList  = forumService.getPageTopicByUser(ForumSessionUtils.getSystemProvider(), this.userName) ;
		forumPageIterator.updatePageList(pageList) ;
		pageList.setPageSize(6) ;
		long page = forumPageIterator.getPageSelected() ;
		List<Topic> topics = pageList.getPage(page) ;
    this.topics = topics ;
		return topics ;
	}
  
  public Topic getTopicById(String topicId) {
    for(Topic topic : this.topics) {
      if(topic.getId().equals(topicId)) return topic ;
    }
    return null ;
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
  private JCRPageList getPageListPost(String topicPath) throws Exception {
		String []id = topicPath.split("/") ;
		int i = id.length ;
		JCRPageList pageListPost = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(), id[i-3], id[i-2], id[i-1], "", "", "", "")	; 
		long maxPost = this.userProfile.getMaxTopicInPage() ;
		if(maxPost > 0)	pageListPost.setPageSize(maxPost) ;
		return pageListPost;
	}
	static	public class OpenTopicActionListener extends EventListener<UIPageListTopicByUser> {
    public void execute(Event<UIPageListTopicByUser> event) throws Exception {
			UIPageListTopicByUser uiForm = event.getSource() ;
      String topicId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      Topic topic = uiForm.getTopicById(topicId) ;
      
      String []id = topic.getPath().split("/") ;
      int i = id.length ;
      String categoryId = id[i-3];
      String forumId = id[i-2] ;
      //id[i-1] ; 
      Forum forum = uiForm.forumService.getForum(ForumSessionUtils.getSystemProvider(), id[i-3], id[i-2]) ;
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.updateIsRendered(2);
      UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
      UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
      uiForumContainer.setIsRenderChild(false) ;
      uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
      UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
      
      uiTopicDetail.setUpdateContainer(categoryId, forumId, topic, 1) ;
      
      uiTopicDetail.setUpdatePageList(uiForm.getPageListPost(topic.getPath())) ;
      uiTopicDetail.setUpdateForum(forum) ;
      uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(categoryId, forumId, topic ) ;
      forumPortlet.getChild(UIForumLinks.class).setValueOption((categoryId+"/"+ forumId + " "));
      uiTopicDetail.setIdPostView("false") ;
      forumPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	static	public class OpenTopicsTagActionListener extends EventListener<UIPageListTopicByUser> {
		public void execute(Event<UIPageListTopicByUser> event) throws Exception {
			//UIPageListPostByUser uiForm = event.getSource() ;
		}
	}
	
}
