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
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.ItemExistsException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
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
	private ForumService forumService ;
	private String topicId ;
	private List<Post> posts ;
	private UserProfile userProfile ;
	private List<Category> categories;
	private String link;
	private String pathPost = "";
	
	public UIMovePostForm() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	
	@SuppressWarnings("unused")
	private String getTitleInHTMLCode(String s) {
		return ForumTransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
	}
	
	public void updatePost(String topicId, List<Post> posts) throws Exception {
		this.topicId = topicId ;
		this.posts = posts ;
		try {
			this.pathPost = posts.get(0).getPath();
    } catch (Exception e) {}
		setCategories() ;
	}
	
	public UserProfile getUserProfile() throws Exception {
	  return this.userProfile ;
  }
	public void setUserProfile(UserProfile userProfile) throws Exception {
	  this.userProfile = userProfile ;
	  if(this.userProfile == null) {
	  	this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
	  }
  }
	
	private void setCategories() throws Exception {
		this.categories = new ArrayList<Category>();
		for (Category category : this.forumService.getCategories()) {
			if(this.userProfile.getUserRole() == 1) {
				String []list = category.getUserPrivate();
				if(list !=  null && list.length > 0 && !list[0].equals(" ")){
					if(!ForumUtils.isStringInStrings(list, this.userProfile.getUserId())) {
						continue ;
					}
				}
			}
			categories.add(category) ;
		}
	}
	@SuppressWarnings("unused")
	private List<Category> getCategories() throws Exception {
		return  this.categories;
	}
	
	@SuppressWarnings("unused")
	private boolean getSelectForum(String forumId) throws Exception {
		if(this.posts.get(0).getPath().contains(forumId)) return true ;
		else return false ;
	}
	
	@SuppressWarnings("unused")
	private List<Forum> getForums(String categoryId) throws Exception {
		List<Forum> forums = new ArrayList<Forum>() ;
		for(Forum forum : this.forumService.getForums(categoryId, "")) {
			if(this.userProfile.getUserRole() == 1){
				String []moderators = forum.getModerators();
				if(!ForumServiceUtils.hasPermission(moderators, this.userProfile.getUserId())){
					continue;
				}
			}
			forums.add(forum) ;
		}
		return forums ;
	}

	@SuppressWarnings("unused")
	private List<Topic> getTopics(String categoryId, String forumId, boolean isMode) throws Exception {
		List<Topic> topics = new ArrayList<Topic>() ;
		List<Topic> topics_ = this.forumService.getTopics(categoryId, forumId);
		for(Topic topic : topics_) {
			if(topic.getId().equalsIgnoreCase(this.topicId)){
				if(pathPost.indexOf(categoryId) >= 0 && pathPost.indexOf(forumId) > 0)
					continue ;
			}
			if(this.userProfile.getUserRole() == 1){
				if(!isMode) {
					if(!topic.getIsActive() || !topic.getIsActiveByForum() || !topic.getIsApproved() || topic.getIsClosed() || topic.getIsLock() || topic.getIsWaiting()) continue ;
					if(topic.getCanPost().length > 0 && !ForumUtils.isStringInStrings(topic.getCanPost(), this.userProfile.getUserId())) continue ;
				}
			}
			topics.add(topic) ;
		}
		return topics ;
	}
	
	static	public class SaveActionListener extends EventListener<UIMovePostForm> {
    public void execute(Event<UIMovePostForm> event) throws Exception {
			UIMovePostForm uiForm = event.getSource() ;
			String topicPath = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(!ForumUtils.isEmpty(topicPath)) {
				try {
					String[] temp = topicPath.split("/") ;
				// set link
//					String link = (ForumSessionUtils.getBreadcumbUrl(uiForm.getLink(), uiForm.getId(), "Cancel", "pathId")).replaceFirst("private", "public");
					String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, "pathId").replaceFirst("private", "public");
					//
					WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
					ResourceBundle res = context.getApplicationResourceBundle() ;
					Collections.sort(uiForm.posts, new ForumUtils.DatetimeComparatorDESC()) ;
					String []postPath = new String[uiForm.posts.size()];
					int i = 0;
					for (Post post : uiForm.posts) {
						postPath[i] = post.getPath(); ++i;
          }
					uiForm.forumService.movePost(postPath, topicPath, false, res.getString("UIForumAdministrationForm.label.EmailToAuthorMoved"), link) ;
					UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.cancelAction() ;
					UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class) ;
					UITopicDetailContainer topicDetailContainer = forumContainer.getChild(UITopicDetailContainer.class) ;
					topicDetailContainer.getChild(UITopicDetail.class).setUpdateTopic(temp[temp.length - 3], temp[temp.length - 2], temp[temp.length - 1]) ;
					topicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(temp[temp.length - 3], temp[temp.length - 2], temp[temp.length - 1]) ;
					UIForumDescription forumDescription = forumContainer.getChild(UIForumDescription.class);
					forumDescription.setForumIds(temp[temp.length - 3], temp[temp.length - 2]);
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
        } catch (ItemExistsException e) {
        	UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        	uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.ObjectIsExist", null, ApplicationMessage.WARNING)) ;
        	return;
        } catch (Exception e) {
        	UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        	uiApp.addMessage(new ApplicationMessage("UIMovePostForm.msg.parent-deleted", null, ApplicationMessage.WARNING)) ;
        	return ;
        }
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