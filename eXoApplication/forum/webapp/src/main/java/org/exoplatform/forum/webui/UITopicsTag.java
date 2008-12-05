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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.popup.UIAddTagForm;
import org.exoplatform.forum.webui.popup.UIAddWatchingForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
				@EventConfig(listeners = UITopicsTag.SetOrderByActionListener.class),
				@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class)
		}
)

public class UITopicsTag extends UIForumKeepStickPageIterator {
	private ForumService forumService ;
	private String tagId = "" ;
	private Tag tag ;
	private boolean isUpdateTag = true ;
	private String strOrderBy = "";
	private UserProfile userProfile = null;
	private Map<String, Long> mapNumberPagePost = new HashMap<String, Long>();
	public UITopicsTag() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	}
	
	public void setIdTag(String tagId) throws Exception {
		this.tagId = tagId ;
		this.isUpdateTag = true ;
		this.mapNumberPagePost.clear();
		this.userProfile	= this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
	}
	
	public void setTag(Tag tag) throws Exception {
	  this.tag = tag;
	  this.tagId = tag.getId();
	  this.isUpdateTag = false;
	  this.mapNumberPagePost.clear();
	  this.userProfile	= this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
  }
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() {
		return userProfile ;
	}
	
	@SuppressWarnings("unused")
  private long getSizePost(String Id) throws Exception {
		if(mapNumberPagePost.containsKey(Id)) return mapNumberPagePost.get(Id);
		String Ids[] = Id.split("/") ;
		Topic topic = getTopic(Ids[(Ids.length - 1)]) ;
		long maxPost = getUserProfile().getMaxPostInPage() ;
		if(maxPost <= 0) maxPost = 10;
		if(topic !=null && topic.getPostCount() > maxPost) {
			String isApprove = "" ;
			String isHidden = "" ;
			String userLogin = this.userProfile.getUserId();
			long role = this.userProfile.getUserRole() ;
			if(role >=2){ isHidden = "false" ;}
			Forum forum = this.forumService.getForum(ForumSessionUtils.getSystemProvider(), Ids[(Ids.length - 3)], Ids[(Ids.length - 2)]);
			if(role == 1) {
				if(!ForumServiceUtils.hasPermission(forum.getModerators(), userLogin)){
					isHidden = "false" ;
				}
			}
			if(forum.getIsModeratePost() || topic.getIsModeratePost()) {
				if(isHidden.equals("false") && !(topic.getOwner().equals(userLogin))) isApprove = "true" ;
			}
			long availablePost = this.forumService.getAvailablePost(ForumSessionUtils.getSystemProvider(), Ids[(Ids.length - 3)], Ids[(Ids.length - 2)], Ids[(Ids.length - 1)], isApprove, isHidden, userLogin)	; 
			long value = availablePost/maxPost;
			if(value*maxPost < availablePost) value = value + 1;
			mapNumberPagePost.put(Id, value);
			return value;
		} else {
			mapNumberPagePost.put(Id, (long)1);
			return 1;
		}
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Topic> getTopicsTag() throws Exception {
		this.pageList = forumService.getTopicsByTag(ForumSessionUtils.getSystemProvider(), this.tagId, strOrderBy) ;
		long maxTopic = this.userProfile.getMaxTopicInPage() ;
		if(maxTopic <= 0) maxTopic = 10;
		this.pageList.setPageSize(maxTopic) ;
		this.maxPage = this.pageList.getAvailablePage();
		List<Topic> topics = pageList.getPage(pageSelect) ;
		pageSelect = pageList.getCurrentPage();
		if(topics == null) topics = new ArrayList<Topic>(); 
		for(Topic topic : topics) {
			if(getUIFormCheckBoxInput(topic.getId()) != null) {
				getUIFormCheckBoxInput(topic.getId()).setChecked(false) ;
			}else {
				addUIFormInput(new UIFormCheckBoxInput(topic.getId(), topic.getId(), false) );
			}
		}
		return topics ;
	}
	
	@SuppressWarnings("unused")
	private Tag getTagById() throws Exception {
		if(this.isUpdateTag) {
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try{
				this.tag = forumService.getTag(sProvider, this.tagId) ;
			}catch (Exception e) {
				throw e;
			}finally {
				sProvider.close();
			}
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
		List<Tag> tags = new ArrayList<Tag>();
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			tags = this.forumService.getTagsByTopic(sProvider, tagIds);
    } catch (Exception e) {
    	throw e;
    }finally {
    	sProvider.close();
    }
    return tags;
	}
	
	@SuppressWarnings("unchecked")
  private Topic getTopic(String topicId) throws Exception {
		List<Topic> listTopic = this.pageList.getPage((long)0) ;
		for (Topic topic : listTopic) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	private Forum getForum(String categoryId, String forumId) throws Exception {
		Forum forum = null;
		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
		try {
			forum = this.forumService.getForum(sProvider, categoryId, forumId);
		}finally {
    	sProvider.close();
    }
		return forum;
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
			UITopicsTag topicsTag = event.getSource();
			boolean hasCheck = false;
			String topicPath = "";
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				for (String topicId : topicsTag.getIdSelected()) {
					topicPath = topicsTag.getTopic(topicId).getPath();
					try {
						topicsTag.forumService.removeTopicInTag(sProvider, topicsTag.tagId, topicPath);
					} catch (Exception e) {
					}
					hasCheck = true;
				}
			} finally {
				sProvider.close();
			}
			if (!hasCheck) {
				Object[] args = {};
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheckMove", args, ApplicationMessage.WARNING));
			} else {
				topicsTag.isUpdateTag = true;
			}
			UIForumPortlet forumPortlet = topicsTag.getParent();
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
		}
	}
	
	static public class RemoveTagActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource() ;
			UIForumPortlet forumPortlet = topicsTag.getParent() ;
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try {
				topicsTag.forumService.removeTag(sProvider, topicsTag.tagId) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} finally {
				sProvider.close();
			}
		}
	}
	
	static public class AddBookMarkActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicTag = event.getSource();
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(topicId)) {
				SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
				try{
					Topic topic = topicTag.getTopic(topicId);
					String path = topic.getPath();
					path = path.substring(path.indexOf(Utils.CATEGORY));
					StringBuffer buffer = new StringBuffer();
					buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//").append(path) ;
					String userName = topicTag.userProfile.getUserId() ;
					topicTag.forumService.saveUserBookmark(sProvider, userName, buffer.toString(), true) ;
					UIForumPortlet forumPortlet = topicTag.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.updateUserProfileInfo() ;
				} catch (Exception e) {
				} finally {
					sProvider.close();
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
	
	static public class SetOrderByActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag uiContainer = event.getSource();
			String path = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(uiContainer.strOrderBy)) {
				if(uiContainer.strOrderBy.indexOf(path) >= 0) {
					if(uiContainer.strOrderBy.indexOf("descending") > 0) {
						uiContainer.strOrderBy = path + " ascending";
					} else {
						uiContainer.strOrderBy = path + " descending";
					}
				} else {
					uiContainer.strOrderBy = path + " ascending";
				}
			} else {
				uiContainer.strOrderBy = path + " ascending";
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiContainer) ;
		}
	}
}