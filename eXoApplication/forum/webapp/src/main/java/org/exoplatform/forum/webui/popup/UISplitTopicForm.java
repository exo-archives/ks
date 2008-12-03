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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumKeepStickPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * 11-03-2008, 09:13:50
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UISplitTopicForm.gtmpl",
		events = {
			@EventConfig(listeners = UISplitTopicForm.SaveActionListener.class), 
			@EventConfig(listeners = UISplitTopicForm.CancelActionListener.class,phase = Phase.DECODE),
			@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
		}
)
public class UISplitTopicForm extends UIForumKeepStickPageIterator implements UIPopupComponent {
	private List<Post> posts = new ArrayList<Post>() ;
	private Topic topic = new Topic() ;
	private UserProfile userProfile = null;
	private List<String> listPostId = new ArrayList<String>();
	private boolean isRender = true;
	public static final String FIELD_SPLITTHREAD_INPUT = "SplitThread" ;
	public UISplitTopicForm() throws Exception {
		addUIFormInput(new UIFormStringInput(FIELD_SPLITTHREAD_INPUT,FIELD_SPLITTHREAD_INPUT, null));
		this.setActions(new String []{"Save", "Cancel"});
	}
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	private Post getPostById(String postId) throws Exception {
		for (Post post : this.posts) {
			if(post.getId().equals(postId)) return post ;
		}
		return new Post() ;
	}
	public boolean getIdRender() {
	  return this.isRender;
  }
	@SuppressWarnings({ "unchecked", "unused" })
  private List<String> getListPost() throws Exception {
		String postId = this.topic.getId().replaceFirst(Utils.TOPIC, Utils.POST) ;
		this.posts.remove(this.getPostById(postId));
		listPostId.clear();
		for (Post post : this.posts) {
			listPostId.add(post.getId());
			if(getUIFormCheckBoxInput(post.getId()) != null) {
				getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
			}
		}
		pageList = new ForumPageList(6, listPostId.size());
		pageList.setPageSize(6);
//		this.updatePageList(pageList);
		List<String>list = new ArrayList<String>();
		try {
			list.addAll(pageList.getPageList(pageSelect, this.listPostId)) ;
		} catch (Exception e) {
		}
		pageSelect = pageList.getCurrentPage();
		if(maxPage <= 1) isRender =  false ;
		return list ; 
	}
	
	public void setListPost(List<Post> posts) {this.posts = posts ;}
	@SuppressWarnings("unused")
	private Topic getTopic() {return this.topic ;}
	public void setTopic(Topic topic) { this.topic = topic; }
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() {return this.userProfile ;}
	public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
	
	static	public class SaveActionListener extends EventListener<UISplitTopicForm> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UISplitTopicForm> event) throws Exception {
			UISplitTopicForm uiForm = event.getSource() ;
			String newTopicTitle = uiForm.getUIStringInput(FIELD_SPLITTHREAD_INPUT).getValue() ;
			if(!ForumUtils.isEmpty(newTopicTitle)) {
				newTopicTitle = ForumTransformHTML.enCodeHTML(newTopicTitle);
				List<Post> posts = new ArrayList<Post>() ;
				for(String child : uiForm.getIdSelected()) {
					Post post = uiForm.getPostById(child);
					if(post !=  null) {
							posts.add(post);
					}
				}
				if(posts.size() > 0) {
					Topic topic = new Topic() ;
					Post post = posts.get(0) ;
					String owner = ForumSessionUtils.getCurrentUser() ;
					String topicId = post.getId().replaceFirst(Utils.POST, Utils.TOPIC);
					topic.setId(topicId) ;
					topic.setTopicName(newTopicTitle) ;
					topic.setOwner(owner) ;
					topic.setModifiedBy(owner) ;
					topic.setDescription(post.getMessage());
					topic.setIcon(post.getIcon());
					topic.setAttachments(post.getAttachments());
					topic.setLastPostBy(posts.get(posts.size() - 1).getOwner()) ;
					String path = uiForm.topic.getPath() ;
					String []string = path.split("/") ;
					String categoryId = string[string.length - 3] ;
					String forumId = string[string.length - 2] ;
					ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
					SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
					try {
						forumService.saveTopic(sProvider, categoryId, forumId, topic, true, true, ForumUtils.getDefaultMail()) ;
						String destTopicPath = path.substring(0, path.lastIndexOf("/"))+ "/" + topicId ;
						forumService.movePost(sProvider, posts, destTopicPath, true);
					} catch (Exception e) {
						sProvider.close();
						UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
						UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
						Object[] args = { };
						throw new MessageException(new ApplicationMessage("UISplitTopicForm.msg.forum-deleted", args, ApplicationMessage.WARNING)) ;
					} finally {
						sProvider.close();
					}			
				}else {
					Object[] args = { };
					throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheckPost", args, ApplicationMessage.WARNING)) ;
				}
			} else {
				Object[] args = {uiForm.getLabel(FIELD_SPLITTHREAD_INPUT) };
				throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortText", args, ApplicationMessage.WARNING)) ;
			}
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
			UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
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
