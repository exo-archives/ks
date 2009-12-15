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
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumKeepStickPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
	private Topic topic = new Topic() ;
	private String link;
	private UserProfile userProfile = null;
	private boolean isRender = true;
	private boolean isSetPage = true;
	private ForumService forumService = null;
	public static final String FIELD_SPLITTHREAD_INPUT = "SplitThread" ;
	public UISplitTopicForm() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addUIFormInput(new UIFormStringInput(FIELD_SPLITTHREAD_INPUT,FIELD_SPLITTHREAD_INPUT, null));
		this.setActions(new String []{"Save", "Cancel"});
	}
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	
	public boolean getIdRender() {
	  return this.isRender;
  }
	@SuppressWarnings({ "unchecked", "unused" })
  private List<Post> getListPost() throws Exception {
		List<Post> posts = new ArrayList<Post>() ;
		String path = this.topic.getPath();
		path = path.substring(path.indexOf(Utils.CATEGORY));
		if(isSetPage){
			pageList = forumService.getPostForSplitTopic(path);
		}
		pageList.setPageSize(6);
		maxPage = pageList.getAvailablePage() ;
		posts =  pageList.getPage(pageSelect);
		pageSelect = pageList.getCurrentPage();
		if(maxPage <= 1) isRender =  false ;
		String checkBoxId;
		for (Post post : posts) {
			checkBoxId = post.getCreatedDate().getTime()+ "/"+post.getId();
			if(getUIFormCheckBoxInput(checkBoxId) != null) {
				getUIFormCheckBoxInput(checkBoxId).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(checkBoxId, checkBoxId, false) );
			}
		}
		isSetPage = true;
		return posts ; 
	}
	
	public void setPageListPost(JCRPageList pageList) {
		this.pageList = pageList ;
		isSetPage = false;
	}
	
	@SuppressWarnings("unused")
	private Topic getTopic() {return this.topic ;}
	public void setTopic(Topic topic) { this.topic = topic; }
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() {return userProfile ;}
	public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
	
	static	public class SaveActionListener extends EventListener<UISplitTopicForm> {
		public void execute(Event<UISplitTopicForm> event) throws Exception {
			UISplitTopicForm uiForm = event.getSource() ;
			String newTopicTitle = uiForm.getUIStringInput(FIELD_SPLITTHREAD_INPUT).getValue() ;
			if(!ForumUtils.isEmpty(newTopicTitle)) {
				newTopicTitle = ForumTransformHTML.enCodeHTML(newTopicTitle);
				// postIds number/id
				List<String> postIds = uiForm.getIdSelected() ;
				if(postIds.size() > 0) {
				  Collections.sort(postIds);
				  List<String> postPaths = new ArrayList<String>();
				  String path = uiForm.topic.getPath() ;
				  for (String str : postIds) {
				  	postPaths.add(path + str.substring(str.indexOf("/")));
          }
					Topic topic = new Topic() ;
					Post post = uiForm.forumService.getPost("", "", "", postPaths.get(0));
					String owner = uiForm.userProfile.getUserId();
					String topicId = post.getId().replaceFirst(Utils.POST, Utils.TOPIC);
					topic.setId(topicId) ;
					topic.setTopicName(newTopicTitle) ;
					topic.setOwner(owner) ;
					topic.setModifiedBy(owner) ;
					topic.setDescription(post.getMessage());
					topic.setIcon(post.getIcon());
					topic.setAttachments(post.getAttachments());
					Post lastPost = uiForm.forumService.getPost("", "", "", postPaths.get(postPaths.size()-1));
					topic.setLastPostBy(lastPost.getOwner()) ;
					if(postPaths.size() > 1){
						topic.setLastPostDate(lastPost.getCreatedDate()) ;
					}
					String []string = path.split("/") ;
					String categoryId = string[string.length - 3] ;
					String forumId = string[string.length - 2] ;
					try {
						// set link
//						String link = ForumSessionUtils.getBreadcumbUrl(uiForm.getLink(), uiForm.getId(), "Cancel", "pathId").replaceFirst("private", "public");	
						String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, "pathId").replaceFirst("private", "public");	
						//
						WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
						ResourceBundle res = context.getApplicationResourceBundle() ;
						
						uiForm.forumService.saveTopic(categoryId, forumId, topic, true, true, ForumUtils.getDefaultMail()) ;
						String destTopicPath = path.substring(0, path.lastIndexOf("/"))+ "/" + topicId ;
						uiForm.forumService.movePost(postPaths.toArray(new String[]{}), destTopicPath, true, res.getString("UIForumAdministrationForm.label.EmailToAuthorMoved"), link);
					} catch (Exception e) {
						e.printStackTrace();
						UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
						UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
						Object[] args = { };
						throw new MessageException(new ApplicationMessage("UISplitTopicForm.msg.forum-deleted", args, ApplicationMessage.WARNING)) ;
					}		
				}else {
					Object[] args = { };
					throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheckPost", args, ApplicationMessage.WARNING)) ;
				}
			} else {
				uiForm.getIdSelected();
				Object[] args = { uiForm.getLabel(FIELD_SPLITTHREAD_INPUT) };
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortText", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return;
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
