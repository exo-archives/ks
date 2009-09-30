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
import java.util.Arrays;
import java.util.List;

import javax.portlet.ActionResponse;
import javax.portlet.PortletMode;
import javax.portlet.PortletPreferences;
import javax.xml.namespace.QName;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.user.ForumContact;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPrivateMessageForm;
import org.exoplatform.forum.webui.popup.UISettingEditModeForm;
import org.exoplatform.forum.webui.popup.UIViewPostedByUser;
import org.exoplatform.forum.webui.popup.UIViewTopicCreatedByUser;
import org.exoplatform.forum.webui.popup.UIViewUserProfile;
import org.exoplatform.services.portletcontainer.plugins.pc.portletAPIImp.PortletRequestImp;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletApplication;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
/**
 * Author : Nguyen Quang Hung
 *					hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
	 lifecycle = UIApplicationLifecycle.class, 
	 template = "app:/templates/forum/webui/UIForumPortlet.gtmpl",
	 events = {
	  	@EventConfig(listeners = UIForumPortlet.ReLoadPortletEventActionListener.class),
	  	@EventConfig(listeners = UIForumPortlet.ViewPublicUserInfoActionListener.class ) ,
			@EventConfig(listeners = UIForumPortlet.ViewPostedByUserActionListener.class ), 
			@EventConfig(listeners = UIForumPortlet.PrivateMessageActionListener.class ),
			@EventConfig(listeners = UIForumPortlet.ViewThreadByUserActionListener.class ),
	  	@EventConfig(listeners = UIForumPortlet.OpenLinkActionListener.class)
	 }
)
public class UIForumPortlet extends UIPortletApplication {
	private ForumService forumService;
	private boolean isCategoryRendered = true;
	private boolean isForumRendered = false;
	private boolean isTagRendered = false;
	private boolean isSearchRendered = false;
	private boolean isJumpRendered = false;
	private boolean isShowForumJump = false;
	private boolean isShowPoll = false;
	private boolean isShowModerators = false;
	private boolean isShowRules = false;
	private boolean isShowIconsLegend = false;
	private boolean isShowStatistics = false;
	private boolean isShowQuickReply = false;
	private UserProfile userProfile = null;
	private boolean enableIPLogging = false;
	private boolean isShowForumActionBar = false;
	private boolean enableBanIP = false;
	private boolean useAjax = true;
	private int dayForumNewPost = 0;
	private List<String>invisibleForums = new ArrayList<String>();
	private List<String>invisibleCategories = new ArrayList<String>();
	public UIForumPortlet() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addChild(UIBreadcumbs.class, null, null) ;
		boolean isRenderBar = !ForumSessionUtils.isAnonim() ;
		addChild(UIForumActionBar.class, null, null).setRendered(isRenderBar);
		addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
		addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
		addChild(UITopicsTag.class, null, null).setRendered(isTagRendered) ;
		addChild(UISearchForm.class, null, null).setRendered(isSearchRendered) ;
		addChild(UIForumLinks.class, null, null).setRendered(isJumpRendered) ;
		addChild(UIPopupAction.class, null, "UIForumPopupAction") ;
		loadPreferences();
	}
	
	 public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
		 PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
		 if(portletReqContext.getApplicationMode() == PortletMode.VIEW) {
	    	if(getChild(UIBreadcumbs.class) ==  null) {
	    		if(getChild(UISettingEditModeForm.class) != null)
	    			removeChild(UISettingEditModeForm.class);
		    	addChild(UIBreadcumbs.class, null, null) ;
		  		addChild(UIForumActionBar.class, null, null).setRendered(!ForumSessionUtils.isAnonim());
		  		UICategoryContainer categoryContainer = addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
		  		addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
		  		addChild(UITopicsTag.class, null, null).setRendered(isTagRendered) ;
		  		addChild(UISearchForm.class, null, null).setRendered(isSearchRendered) ;
		  		addChild(UIForumLinks.class, null, null).setRendered(isJumpRendered) ;
		  		updateIsRendered(ForumUtils.CATEGORIES);
		  		categoryContainer.updateIsRender(true) ;
	    	}
	    }else if(portletReqContext.getApplicationMode() == PortletMode.EDIT) {
	    	if(getChild(UISettingEditModeForm.class) == null) {
	    		UISettingEditModeForm editModeForm = addChild(UISettingEditModeForm.class, null, null);
	    		editModeForm.setUserProfile(getUserProfile());
	    		if(getChild(UIBreadcumbs.class) != null) {
		    		removeChild(UIBreadcumbs.class) ;
		    		removeChild(UIForumActionBar.class) ;
		    		removeChild(UICategoryContainer.class) ;
		    		removeChild(UIForumContainer.class) ;
		    		removeChild(UITopicsTag.class) ;
		    		removeChild(UISearchForm.class) ;
		    		removeChild(UIForumLinks.class);
	    		}
	    	}
	    }
	    super.processRender(app, context) ;
	 }
	
	public void updateIsRendered(String selected) throws Exception {
		if(selected.equals(ForumUtils.CATEGORIES)) {
			isCategoryRendered = true ;
			isForumRendered = false ;
			isTagRendered = false ;
			isSearchRendered = false ;
		} else if(selected.equals(ForumUtils.FORUM)) {
			isForumRendered = true ;
			isCategoryRendered = false ;
			isTagRendered = false ;
			isSearchRendered = false ;
		} else if(selected.equals(ForumUtils.TAG)) {
			isTagRendered = true ;
			isForumRendered = false ;
			isCategoryRendered = false ;
			isSearchRendered = false ;
		} else {
			isTagRendered = false ;
			isForumRendered = false ;
			isCategoryRendered = false ;
			isSearchRendered = true ;
		}
		if(!isShowForumActionBar){
			if(!isCategoryRendered || isSearchRendered){
				getChild(UIForumActionBar.class).setRendered(false) ;
			}
		}
		setRenderForumLink();
		getChild(UIForumContainer.class).setRendered(isForumRendered) ;
		getChild(UITopicsTag.class).setRendered(isTagRendered) ;
		getChild(UISearchForm.class).setRendered(isSearchRendered) ;
		if(!isForumRendered) {
			try {
	      this.setRenderQuickReply();
      } catch (Exception e) {
      }
		}
	}
	
	public void setRenderForumLink() throws Exception {
		if(userProfile == null) updateUserProfileInfo();
		if(isShowForumJump){
			isJumpRendered = this.userProfile.getIsShowForumJump() ;
		} else {
			isJumpRendered = false;
		}
		UICategoryContainer categoryContainer = getChild(UICategoryContainer.class).setRendered(isCategoryRendered) ;
		categoryContainer.setIsRenderJump(isJumpRendered);
		if(!isCategoryRendered) {
			getChild(UIForumLinks.class).setRendered(isJumpRendered) ;
		}
  }
	
	public void setRenderQuickReply() {
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		ActionResponse actionRes = (ActionResponse)pcontext.getResponse();
		ForumParameter param = new ForumParameter() ;
		param.setRenderQuickReply(false);
		param.setRenderPoll(false);
		param.setRenderModerator(false);
		param.setRenderRule(false);
		actionRes.setEvent(new QName("QuickReplyEvent"), param) ;
		actionRes.setEvent(new QName("ForumPollEvent"), param) ;
		actionRes.setEvent(new QName("ForumModerateEvent"), param) ;
		actionRes.setEvent(new QName("ForumRuleEvent"), param) ;
  }
	
	public void loadPreferences() throws Exception {
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		invisibleCategories.clear();
		invisibleForums.clear();
		try {
			isShowForumActionBar = Boolean.parseBoolean(portletPref.getValue("showForumActionBar", ""));
			dayForumNewPost = Integer.parseInt(portletPref.getValue("forumNewPost", ""));
			useAjax = Boolean.parseBoolean(portletPref.getValue("useAjax", ""));
			enableIPLogging = Boolean.parseBoolean(portletPref.getValue("enableIPLogging", ""));
			enableBanIP = Boolean.parseBoolean(portletPref.getValue("enableIPFiltering", ""));
			isShowForumJump = Boolean.parseBoolean(portletPref.getValue("isShowForumJump", ""));
			isShowPoll = Boolean.parseBoolean(portletPref.getValue("isShowPoll", ""));
			isShowModerators = Boolean.parseBoolean(portletPref.getValue("isShowModerators", ""));
			isShowRules = Boolean.parseBoolean(portletPref.getValue("isShowRules", ""));
			isShowQuickReply = Boolean.parseBoolean(portletPref.getValue("isShowQuickReply", ""));
			isShowStatistics = Boolean.parseBoolean(portletPref.getValue("isShowStatistics", ""));
			isShowIconsLegend = Boolean.parseBoolean(portletPref.getValue("isShowIconsLegend", ""));
			invisibleCategories.addAll(getListInValus(portletPref.getValue("invisibleCategories", ""))) ;
			invisibleForums.addAll(getListInValus(portletPref.getValue("invisibleForums", ""))) ;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(invisibleCategories.size() == 1 && invisibleCategories.get(0).equals(" ")) invisibleCategories.clear();
	}
	
	private List<String> getListInValus(String value) throws Exception {
		List<String>list = new ArrayList<String>();
		if(!ForumUtils.isEmpty(value)) {
			list.addAll(Arrays.asList(ForumUtils.addStringToString(value, value)));
		}
		return list;
	}
		
	public List<String> getInvisibleForums() {
  	return invisibleForums;
  }
	
	public List<String> getInvisibleCategories() {
  	return invisibleCategories;
  }

	public boolean isEnableIPLogging() {
		return enableIPLogging;
	}
	
	public boolean isEnableBanIp() {
		return enableBanIP;
	}
	
	public boolean isShowForumActionBar() {
	  return isShowForumActionBar;
  }

	public boolean isShowPoll() {
		return isShowPoll;
	}
	public boolean isShowModerators() {
		return isShowModerators;
	}

	public boolean isShowRules() {
		return isShowRules;
	}
	
	public boolean isShowIconsLegend() {
		return isShowIconsLegend;
	}

	public boolean isShowQuickReply() {
		return isShowQuickReply;
	}
	
	public boolean isShowStatistics() {
		return isShowStatistics;
	}

	public boolean isUseAjax(){
		return useAjax;
	}
	
	public int getDayForumNewPost(){
		return dayForumNewPost;
	}

	public void renderPopupMessages() throws Exception {
		UIPopupMessages popupMess = getUIPopupMessages();
		if(popupMess == null)	return ;
		WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
		popupMess.processRender(context);
	}

	public void cancelAction() throws Exception {
		WebuiRequestContext context = RequestContext.getCurrentInstance() ;
		UIPopupAction popupAction = getChild(UIPopupAction.class) ;
		popupAction.deActivate() ;
		context.addUIComponentToUpdateByAjax(popupAction) ;
	}
	
	public UserProfile getUserProfile() throws Exception {
		if(this.userProfile == null) updateUserProfileInfo() ;
		return this.userProfile ;
	}
	
	public void updateAccessTopic(String topicId) throws Exception {
		String userId = ForumSessionUtils.getCurrentUser() ;
		if(userId != null && userId.length() > 0) {
			try{
				forumService.updateTopicAccess(userId, topicId);
			} catch (Exception e) {}
		}
		userProfile.setLastTimeAccessTopic(topicId, ForumUtils.getInstanceTempCalendar().getTimeInMillis());
  }

	public void updateAccessForum(String forumId) throws Exception {
		String userId = ForumSessionUtils.getCurrentUser() ;
		if(userId != null && userId.length() > 0) {
			try{
				forumService.updateForumAccess(userId, forumId);
			} catch (Exception e) {}
		}
		userProfile.setLastTimeAccessForum(forumId, ForumUtils.getInstanceTempCalendar().getTimeInMillis());
	}
	
	public void updateUserProfileInfo() throws Exception {
		String userId = "" ;
		try {
			userId = ForumSessionUtils.getCurrentUser() ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		try{
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			if(enableBanIP) {
				WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
				PortletRequestImp request = context.getRequest() ;
				userProfile = forumService.getDefaultUserProfile(userId, request.getRemoteAddr()) ;
			}else {
				userProfile = forumService.getDefaultUserProfile(userId, null) ;
			}
			if(!ForumUtils.isEmpty(userId))
				userProfile.setEmail(ForumSessionUtils.getUserByUserId(userId).getEmail());
		}catch (Exception e) {}			
	}
	
	static public class ReLoadPortletEventActionListener extends EventListener<UIForumPortlet> {
		public void execute(Event<UIForumPortlet> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource() ;
			ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
			if(params.getTopicId() != null){
				forumPortlet.userProfile.setLastTimeAccessTopic(params.getTopicId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
				UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
				topicDetail.setIdPostView("lastpost");
			}
			if(params.isRenderPoll()) {
				UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class);
				topicDetailContainer.getChild(UITopicDetail.class).setIsEditTopic(true);;
				topicDetailContainer.getChild(UITopicPoll.class).setEditPoll(true);
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class OpenLinkActionListener extends EventListener<UIForumPortlet> {
		public void execute(Event<UIForumPortlet> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource() ;
			String path = event.getRequestContext().getRequestParameter(OBJECTID) ;
			if(ForumUtils.isEmpty(path)){
				ForumParameter params = (ForumParameter) event.getRequestContext().getAttribute(PortletApplication.PORTLET_EVENT_VALUE);
				path = params.getPath();
			}
			if(ForumUtils.isEmpty(path)) return;
			UIApplication uiApp = forumPortlet.getAncestorOfType(UIApplication.class) ;
			if(path.indexOf(ForumUtils.FIELD_EXOFORUM_LABEL) >= 0) {
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
			}else if(path.equals(Utils.FORUM_SERVICE)){
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
			}else	if(path.lastIndexOf(Utils.TOPIC) >= 0) {
				String []id = path.split("/") ;
				try{
					Topic topic ;
					if(id.length > 1) {
						topic = forumPortlet.forumService.getTopicByPath(path, false) ;
					} else {
						topic = (Topic)forumPortlet.forumService.getObjectNameById(path, Utils.TOPIC);
						path = topic.getPath();
						path = path.substring(path.indexOf(Utils.CATEGORY));
						id = path.split("/") ;
					}
					if(topic != null) {
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						Forum forum = forumPortlet.forumService.getForum(id[0], id[1]) ;
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
						uiForumContainer.setIsRenderChild(false) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
						uiTopicDetail.setUpdateForum(forum) ;
						uiTopicDetail.setTopicFromCate(id[0], id[1] , topic, 0) ;
						uiTopicDetail.setIdPostView("top") ;
						uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[0], id[1] , topic.getId()) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((id[0] + "/" + id[1] + " "));
						if(!forumPortlet.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)) {
							forumPortlet.forumService.updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  topic.getId()) ;
							forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
						}
					}						
				}catch(Exception e) {
					uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
					forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
					UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
					categoryContainer.updateIsRender(true) ;
					categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
					path = Utils.FORUM_SERVICE;
				}
			}else	if((path.lastIndexOf(Utils.FORUM) == 0 && path.lastIndexOf(Utils.CATEGORY) < 0) || (path.lastIndexOf(Utils.FORUM) > 0)) {
				String id[] = path.split("/");
				forumPortlet.updateIsRendered(ForumUtils.FORUM);
				UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
				forumContainer.setIsRenderChild(true) ;
				if(id.length > 1) {
					forumContainer.getChild(UIForumDescription.class).setForumIds(id[0], id[1]);
					forumContainer.getChild(UITopicContainer.class).updateByBreadcumbs(id[0], id[1], true, 1) ;
				} else {
					try {
						Forum forum = (Forum)forumPortlet.forumService.getObjectNameById(path, Utils.FORUM);
						path = forum.getPath();
						path = path.substring(path.indexOf(Utils.CATEGORY));
						id = path.split("/");
						forumContainer.getChild(UIForumDescription.class).setForum(forum) ;
						forumContainer.getChild(UITopicContainer.class).setUpdateForum(id[0], forum, 1) ;
					}catch(Exception e) {
						uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", null, ApplicationMessage.WARNING)) ;
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.updateIsRender(true) ;
						categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ;
						path = Utils.FORUM_SERVICE;
					}
				}
			}else {
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.getChild(UICategory.class).updateByBreadcumbs(path) ;
				categoryContainer.updateIsRender(false) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
			}
			UIBreadcumbs uiBreadcumbs = forumPortlet.findFirstComponentOfType(UIBreadcumbs.class);
			uiBreadcumbs.setUpdataPath(path);
			forumPortlet.getChild(UIForumLinks.class).setValueOption(path);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	private ForumContact getPersonalContact(String userId) throws Exception {
		ForumContact contact  = ForumSessionUtils.getPersonalContact(userId) ;
		if(contact == null) {
			contact = new ForumContact() ;
		}
	return contact ;
}
	
	static public class ViewPublicUserInfoActionListener extends EventListener<UIForumPortlet> {
		public void execute(Event<UIForumPortlet> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource() ;
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIViewUserProfile viewUserProfile = popupAction.createUIComponent(UIViewUserProfile.class, null, null) ;
			try{
				UserProfile selectProfile = forumPortlet.forumService.getUserInformations(forumPortlet.forumService.getQuickProfile(userId.trim())) ;
				viewUserProfile.setUserProfile(selectProfile) ;
			}catch(Exception e) {
				e.printStackTrace() ;
			}
			viewUserProfile.setUserProfileLogin(forumPortlet.userProfile) ;
			ForumContact contact = forumPortlet.getPersonalContact(userId.trim());
			viewUserProfile.setContact(contact) ;
			popupAction.activate(viewUserProfile, 670, 400, true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class PrivateMessageActionListener extends EventListener<UIForumPortlet> {
		public void execute(Event<UIForumPortlet> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource() ;;
			if(forumPortlet.userProfile.getIsBanned()){
				String[] args = new String[] { } ;
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.userIsBannedCanNotSendMail", args, ApplicationMessage.WARNING)) ;
			}
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPrivateMessageForm messageForm = popupContainer.addChild(UIPrivateMessageForm.class, null, null) ;
			messageForm.setFullMessage(false);
			messageForm.setUserProfile(forumPortlet.userProfile);
			messageForm.setSendtoField(userId) ;
			popupContainer.setId("PrivateMessageForm") ;
			popupAction.activate(popupContainer, 650, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class ViewPostedByUserActionListener extends EventListener<UIForumPortlet> {
		public void execute(Event<UIForumPortlet> event) throws Exception {
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = event.getSource() ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIViewPostedByUser viewPostedByUser = popupContainer.addChild(UIViewPostedByUser.class, null, null) ;
			viewPostedByUser.setUserProfile(userId) ;
			popupContainer.setId("ViewPostedByUser") ;
			popupAction.activate(popupContainer, 760, 370) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class ViewThreadByUserActionListener extends EventListener<UIForumPortlet> {
		public void execute(Event<UIForumPortlet> event) throws Exception {
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = event.getSource() ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIViewTopicCreatedByUser topicCreatedByUser = popupContainer.addChild(UIViewTopicCreatedByUser.class, null, null) ;
			topicCreatedByUser.setUserId(userId) ;
			popupContainer.setId("ViewTopicCreatedByUser") ;
			popupAction.activate(popupContainer, 760, 450) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
}