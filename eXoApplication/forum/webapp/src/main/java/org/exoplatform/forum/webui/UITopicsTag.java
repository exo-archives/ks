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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIAddTagForm;
import org.exoplatform.forum.webui.popup.UIAddWatchingForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/UITopicsTag.gtmpl",
		events = {
				@EventConfig(listeners = UITopicsTag.OpenTopicActionListener.class),
				@EventConfig(listeners = UITopicsTag.EditTagActionListener.class),
				@EventConfig(listeners = UITopicsTag.OpenTopicsTagActionListener.class),
				@EventConfig(listeners = UITopicsTag.RemoveTopicActionListener.class),
				@EventConfig(listeners = UITopicsTag.RemoveTagActionListener.class),
				@EventConfig(listeners = UITopicsTag.AddWatchingActionListener.class),
				@EventConfig(listeners = UITopicsTag.AddBookMarkActionListener.class),
				@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
		}
)

public class UITopicsTag extends UIForumKeepStickPageIterator {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String tagId = "" ;
	private JCRPageList listTopic ;
	private List<Topic> topics ;
	private Tag tag ;
	private long maxPost = 10 ;
	private long maxTopic = 10 ;
	private long maxPage = 1 ;
	private boolean isUpdateTag = true ;
	private boolean isUpdateTopicTag = true ;
	private UserProfile userProfile = null;
	public UITopicsTag() throws Exception {
	}
	
	public void setIdTag(String tagId) throws Exception {
		this.tagId = tagId ;
		this.isUpdateTag = true ;
		this.isUpdateTopicTag = true ;
		this.userProfile	= this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void setTag(Tag tag) throws Exception {
	  this.tag = tag;
	  this.tagId = tag.getId();
	  this.isUpdateTag = false;
	  this.isUpdateTopicTag = true ;
	  this.userProfile	= this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
  }
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() {
		return userProfile ;
	}
	
	@SuppressWarnings("unused")
	private void getListTopicTag() throws Exception {
		this.listTopic = forumService.getTopicsByTag(ForumSessionUtils.getSystemProvider(), this.tagId) ;
		long maxTopic = this.userProfile.getMaxTopicInPage() ;
		if(maxTopic > 0) this.maxTopic = maxTopic;
		this.listTopic.setPageSize(this.maxTopic) ;
		this.updatePageList(this.listTopic) ;
		if(this.isUpdateTopicTag) { 
			this.pageSelect = 1;
			this.isUpdateTopicTag = false ;
		}
	}
	
	private TreeMap<String, JCRPageList> mapPostPage = new TreeMap<String, JCRPageList>();
	@SuppressWarnings("unused")
	private long getMaxPagePost(String Id) throws Exception {
		String Ids[] = Id.split("/") ;
		String isApprove = "" ;
		String isHidden = "" ;
		String userLogin = this.userProfile.getUserId();
		Forum forum = this.forumService.getForum(ForumSessionUtils.getSystemProvider(), Ids[(Ids.length - 3)], Ids[(Ids.length - 2)]);
		Topic topic = getTopic(Ids[(Ids.length - 1)]) ;
		long role = this.userProfile.getUserRole() ;
		if(role >=2){ isHidden = "flase" ;}
		if(role == 1) {
			if(!ForumServiceUtils.hasPermission(forum.getModerators(), userLogin)){
				isHidden = "flase" ;
			}
		}
		if(forum.getIsModeratePost() || topic.getIsModeratePost()) {
			if(isHidden.equals("false") && !(topic.getOwner().equals(userLogin))) isApprove = "true" ;
		}
		JCRPageList pageListPost = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(), Ids[(Ids.length - 3)], Ids[(Ids.length - 2)], Ids[(Ids.length - 1)], isApprove, isHidden, "", userLogin)	; 
		long maxPost = this.userProfile.getMaxTopicInPage() ;
		if(maxPost > 0) this.maxPost = maxPost;
		pageListPost.setPageSize(this.maxPost) ;
		this.mapPostPage.put(Ids[(Ids.length - 1)], pageListPost) ; 
		return pageListPost.getAvailablePage();
	}

	@SuppressWarnings("unused")
	private JCRPageList getPagePost(String topicId) {
		return this.mapPostPage.get(topicId) ;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Topic> getTopicsTag() throws Exception {
		getListTopicTag() ;
		this.maxPage = this.listTopic.getAvailablePage();
		this.topics = null;
		while(topics == null && pageSelect >= 1){
			try {
				topics = listTopic.getPage(pageSelect) ;
      } catch (Exception e) {
      	topics = null; 
      	--pageSelect;
      }
		}
		if(topics == null) topics = new ArrayList<Topic>(); 
		for(Topic topic : this.topics) {
			if(getUIFormCheckBoxInput(topic.getId()) != null) {
				getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(topic.getId(), topic.getId(), false) );
			}
		}
		return this.topics ;
	}
	
	@SuppressWarnings("unused")
	private Tag getTagById() throws Exception {
		if(this.isUpdateTag) {
			this.tag = forumService.getTag(ForumSessionUtils.getSystemProvider(), this.tagId) ;
			this.isUpdateTag = false ;
		}
		return this.tag ;
	}

	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		return ForumUtils.getStarNumber(voteRating) ;
	}

	@SuppressWarnings("unused")
	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
//		String []ids = new String[tagIds.length-1] ; 
//		int t = 0;
//		for (String string : tagIds) {
//			if(!string.equals(this.tagId)){
//				ids[t] = string ;
//				++t;
//			}
//		}
		return this.forumService.getTagsByTopic(ForumSessionUtils.getSystemProvider(), tagIds);
	}
	
	@SuppressWarnings("unchecked")
  private Topic getTopic(String topicId) throws Exception {
		List<Topic> listTopic = this.listTopic.getPage((long)0) ;
		for (Topic topic : listTopic) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	private Forum getForum(String categoryId, String forumId) throws Exception {
		return this.forumService.getForum(ForumSessionUtils.getSystemProvider(), categoryId, forumId);
	}
	
	@SuppressWarnings("unchecked")
  private List<String> getIdSelected() throws Exception{
		List<UIComponent> children = this.getChildren() ;
		List<String> ids = new ArrayList<String>() ;
		for (int i = 0; i <= this.maxPage; i++) {
			if(this.getListChecked(i) != null)ids.addAll(this.getListChecked(i));
		}
		for(UIComponent child : children) {
			if(child instanceof UIFormCheckBoxInput) {
				if(((UIFormCheckBoxInput)child).isChecked()) {
					if(!ids.contains(child.getName()))ids.add(child.getName());
				}
			}
		}
		this.cleanCheckedList();
		return ids;
	}
	
	static public class OpenTopicActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag uiTopicsTag = event.getSource();
			String idAndNumber = event.getRequestContext().getRequestParameter(OBJECTID) ;
			String []id = idAndNumber.split(",") ;
			Topic topic = uiTopicsTag.getTopic(id[0]);
			String cateId = topic.getPath().split("/")[3];
			SessionProvider sProvider = ForumServiceUtils.getSessionProvider();
			Category category = ((ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class)).getCategory(sProvider, cateId);
			String[] privateUsers = category.getUserPrivate();
			if(privateUsers.length > 0 && privateUsers[0].trim().length() > 0 && 
					!ForumServiceUtils.hasPermission(privateUsers, uiTopicsTag.userProfile.getUserId())){
				UIApplication uiApp = uiTopicsTag.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", null, ApplicationMessage.WARNING)) ;
				return ;
			}
			sProvider.close();
			String []temp = topic.getPath().split("/") ;
			Forum forum = uiTopicsTag.getForum(temp[temp.length-3], temp[temp.length-2]) ;
			UIForumPortlet forumPortlet = uiTopicsTag.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FORUM);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiTopicDetail.setUpdateContainer(temp[temp.length-3], temp[temp.length-2], topic, Long.parseLong(id[1])) ;
			uiTopicDetail.setUpdatePageList(uiTopicsTag.getPagePost(id[0]));
			uiTopicDetail.setUpdateForum(forum);
			uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(temp[temp.length-3], temp[temp.length-2], topic) ;
			if(id[2].equals("true")) {
				uiTopicDetail.setIdPostView("lastpost") ;
			} else {
				uiTopicDetail.setIdPostView("top") ;
			}
			forumPortlet.getChild(UIForumLinks.class).setValueOption(temp[temp.length-3] + "/" + temp[temp.length-2] + "");
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenTopicsTagActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(tagId) ;
			topicsTag.setIdTag(tagId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class EditTagActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIAddTagForm addTagForm = popupAction.createUIComponent(UIAddTagForm.class, null, null) ;
			addTagForm.setUpdateTag(topicsTag.tag);
			addTagForm.setIsTopicTag(true) ;
			popupAction.activate(addTagForm, 410, 263) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class RemoveTopicActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			boolean hasCheck = false ;
			String topicPath = "" ;
			for(String  topicId : topicsTag.getIdSelected()) {
				topicPath = topicsTag.getTopic(topicId).getPath() ;
				try {
					topicsTag.forumService.removeTopicInTag(ForumSessionUtils.getSystemProvider(), topicsTag.tagId,topicPath ) ;
				} catch (Exception e) {
				}
				hasCheck = true ;
			}
			if(!hasCheck) {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheckMove", args, ApplicationMessage.WARNING)) ;
			}else {
				topicsTag.isUpdateTag = true ;
			}
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class RemoveTagActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			topicsTag.forumService.removeTag(ForumSessionUtils.getSystemProvider(), topicsTag.tagId) ;
			forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
			UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
			categoryContainer.updateIsRender(true) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class AddBookMarkActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicTag = event.getSource();
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(topicId)) {
				try{
					Topic topic = topicTag.getTopic(topicId);
					String path = topic.getPath();
					path = path.substring(path.indexOf(Utils.CATEGORY));
					StringBuffer buffer = new StringBuffer();
					buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//").append(path) ;
					String userName = topicTag.userProfile.getUserId() ;
					topicTag.forumService.saveUserBookmark(ForumSessionUtils.getSystemProvider(), userName, buffer.toString(), true) ;
					UIForumPortlet forumPortlet = topicTag.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.setUserProfile() ;
				} catch (Exception e) {
				}
			}
		}
	}
	
	static public class AddWatchingActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicTag = event.getSource();
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(topicId)) {
				try{
					Topic topic = topicTag.getTopic(topicId);
					String path = topic.getPath();
					path = path.substring(path.indexOf(Utils.CATEGORY));
					UIForumPortlet forumPortlet = topicTag.getAncestorOfType(UIForumPortlet.class) ;
					UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
					UIAddWatchingForm addWatchingForm = popupAction.createUIComponent(UIAddWatchingForm.class, null, null) ;
					addWatchingForm.initForm() ;
					addWatchingForm.setPathNode(path);
					popupAction.activate(addWatchingForm, 425, 250) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} catch (Exception e) {
				}
			}
		}
	}
}