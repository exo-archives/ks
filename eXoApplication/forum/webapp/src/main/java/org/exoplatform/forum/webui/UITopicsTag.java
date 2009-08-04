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
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIRSSForm;
import org.exoplatform.ks.rss.RSS;
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
				@EventConfig(listeners = UITopicsTag.RemoveTopicActionListener.class),
				@EventConfig(listeners = UITopicsTag.AddWatchingActionListener.class),
				@EventConfig(listeners = UITopicsTag.AddBookMarkActionListener.class),
				@EventConfig(listeners = UITopicsTag.RSSActionListener.class),
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
	private String userIdAndtagId ;
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
		if(!userProfile.getUserId().equals(UserProfile.USER_GUEST)){
			this.userIdAndtagId = userProfile.getUserId() + ":" + tagId;
		} else this.userIdAndtagId = tagId;
	}
	
	public void setTag(Tag tag, String userIdAndtagId) throws Exception {
	  this.tag = tag;
	  this.tagId = tag.getId();
	  this.isUpdateTag = false;
	  this.mapNumberPagePost.clear();
	  this.userIdAndtagId = userIdAndtagId;
	  this.userProfile	= this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
  }
	
	private UserProfile getUserProfile() {
		return userProfile ;
	}
	public void setUserProfile(UserProfile userProfile) throws Exception {
		this.userProfile	= userProfile ;
  }

	private String getScreenName(String userName)throws Exception {
		return forumService.getScreenName(userName);
	}
	public String getRSSLink(String cateId){
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return RSS.getRSSLink("forum", pcontainer.getPortalContainerInfo().getContainerName(), cateId);
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
			Forum forum = this.forumService.getForum(Ids[(Ids.length - 3)], Ids[(Ids.length - 2)]);
			if(role == 1) {
				if(!ForumServiceUtils.hasPermission(forum.getModerators(), userLogin)){
					isHidden = "false" ;
				}
			}
			if(forum.getIsModeratePost() || topic.getIsModeratePost()) {
				if(isHidden.equals("false") && !(topic.getOwner().equals(userLogin))) isApprove = "true" ;
			}
			long availablePost = this.forumService.getAvailablePost(Ids[(Ids.length - 3)], Ids[(Ids.length - 2)], Ids[(Ids.length - 1)], isApprove, isHidden, userLogin)	; 
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
		this.pageList = forumService.getTopicByMyTag(userIdAndtagId, strOrderBy);
		int maxTopic = this.userProfile.getMaxTopicInPage().intValue() ;
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
			try{
				this.tag = forumService.getTag(this.tagId) ;
			}catch (Exception e) {
				throw e;
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

//	@SuppressWarnings("unused")
//	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
//		List<Tag> tags = new ArrayList<Tag>();
//		SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
//		try {
//			tags = this.forumService.getTagsByTopic(sProvider, tagIds);
//    } catch (Exception e) {
//    	throw e;
//    }finally {
//    	sProvider.close();
//    }
//    return tags;
//	}
	
	@SuppressWarnings("unchecked")
  private Topic getTopic(String topicId) throws Exception {
		List<Topic> listTopic = this.pageList.getAll();
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
			String[]ids = topic.getPath().split("/");
			String cateId="", forumId="", topicId="", postId="";
			for (int i = 0; i < ids.length; i++) {
	      if(ids[i].indexOf(Utils.CATEGORY) >= 0) cateId = ids[i];
	      if(ids[i].indexOf(Utils.FORUM) >= 0) forumId = ids[i];
	      if(ids[i].indexOf(Utils.TOPIC) >= 0) topicId = ids[i];
	      if(ids[i].indexOf(Utils.POST) >= 0) postId = ids[i];
      }
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
			Forum forum = uiTopicsTag.getForum(cateId, forumId) ;
			UIForumPortlet forumPortlet = uiTopicsTag.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FORUM);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiTopicDetail.setUpdateForum(forum);
			uiTopicDetail.setUpdateContainer(cateId, forumId, topic, Integer.parseInt(id[1])) ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(cateId, forumId, topic.getId()) ;
			if(id[2].equals("true")) {
				uiTopicDetail.setIdPostView("lastpost") ;
			} else {
				uiTopicDetail.setIdPostView("top") ;
			}
			forumPortlet.getChild(UIForumLinks.class).setValueOption(cateId + "/" + forumId + " ");
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
	
	static public class RemoveTopicActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag topicsTag = event.getSource();
			UIForumPortlet forumPortlet = topicsTag.getParent();
			boolean hasCheck = false;
			String topicPath = "";
			try {
				String userId = topicsTag.getUserProfile().getUserId();
				for (String topicId : (List<String>)topicsTag.getIdSelected()) {
					topicPath = topicsTag.getTopic(topicId).getPath();
					try {
						topicsTag.forumService.unTag(topicsTag.tagId, userId, topicPath);
					} catch (Exception e) {
					}
					hasCheck = true;
				}
			}catch (Exception e) {
			}
			if (!hasCheck) {
				Object[] args = {};
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.notCheckMove", args, ApplicationMessage.WARNING));
			} else {
				topicsTag.isUpdateTag = true;
				Tag tag = topicsTag.getTagById();
				if(tag == null || tag.getUserTag() == null || tag.getUserTag().length == 0) {
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
		  		categoryContainer.updateIsRender(true) ;
					UIBreadcumbs uiBreadcumbs = forumPortlet.findFirstComponentOfType(UIBreadcumbs.class);
					uiBreadcumbs.setUpdataPath(Utils.FORUM_SERVICE);
				}
				topicsTag.isUpdateTag = false;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
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
	
	static public class RSSActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag uiForm = event.getSource();
			String forumId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String currentUser = ForumSessionUtils.getCurrentUser();
			if(currentUser != null){
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider();
				uiForm.forumService.addWatch(sProvider, -1, forumId, null, currentUser);
				sProvider.close();
			}
			String rssLink = uiForm.getRSSLink(forumId);
			UIForumPortlet portlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("ForumRSSForm") ;
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
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
					SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
					List<String> values = new ArrayList<String>();
					String userName = topicTag.userProfile.getUserId();
					try {
						values.add(ForumSessionUtils.getUserByUserId(userName).getEmail());
						topicTag.forumService.addWatch(sProvider, 1, path, values, ForumSessionUtils.getCurrentUser()) ;
						Object[] args = { };
						UIApplication uiApp = topicTag.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.successfully", args, ApplicationMessage.INFO)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					} catch (Exception e) {
						e.printStackTrace();
						Object[] args = { };
						UIApplication uiApp = topicTag.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.fall", args, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					}finally {
						sProvider.close();
					}
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