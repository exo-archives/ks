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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.portlet.ActionResponse;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.rendering.MarkupRenderer;
import org.exoplatform.forum.rendering.RenderingException;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.webui.popup.UIMovePostForm;
import org.exoplatform.forum.webui.popup.UIMoveTopicForm;
import org.exoplatform.forum.webui.popup.UIPageListPostHidden;
import org.exoplatform.forum.webui.popup.UIPageListPostUnApprove;
import org.exoplatform.forum.webui.popup.UIPollForm;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.forum.webui.popup.UIPopupContainer;
import org.exoplatform.forum.webui.popup.UIPostForm;
import org.exoplatform.forum.webui.popup.UIPrivateMessageForm;
import org.exoplatform.forum.webui.popup.UIRatingForm;
import org.exoplatform.forum.webui.popup.UISplitTopicForm;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.forum.webui.popup.UIViewPost;
import org.exoplatform.forum.webui.popup.UIViewPostedByUser;
import org.exoplatform.forum.webui.popup.UIViewTopicCreatedByUser;
import org.exoplatform.forum.webui.popup.UIViewUserProfile;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/UITopicDetail.gtmpl", 
		events = {
			@EventConfig(listeners = UITopicDetail.AddPostActionListener.class ),
			@EventConfig(listeners = UITopicDetail.RatingTopicActionListener.class ),
			@EventConfig(listeners = UITopicDetail.AddTagTopicActionListener.class ),
			@EventConfig(listeners = UITopicDetail.UnTagTopicActionListener.class ),
			@EventConfig(listeners = UITopicDetail.OpenTopicsTagActionListener.class ),
			@EventConfig(listeners = UITopicDetail.GoNumberPageActionListener.class ),
			@EventConfig(listeners = UITopicDetail.SearchFormActionListener.class ),
			
			@EventConfig(listeners = UITopicDetail.PrintActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.EditActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.DeleteActionListener.class,confirm="UITopicDetail.confirm.DeleteThisPost" ),	
			@EventConfig(listeners = UITopicDetail.PrivatePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.QuoteActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.EditTopicActionListener.class ),	//Topic Menu
			@EventConfig(listeners = UITopicDetail.PrintPageActionListener.class ),
			@EventConfig(listeners = UITopicDetail.AddPollActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetOpenTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetCloseTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetLockedTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnLockTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetMoveTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetStickTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnStickTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SplitTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetApproveTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnApproveTopicActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetDeleteTopicActionListener.class,confirm="UITopicDetail.confirm.DeleteThisTopic" ),	
			@EventConfig(listeners = UITopicDetail.MergePostActionListener.class ), //Post Menu 
			@EventConfig(listeners = UITopicDetail.MovePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetApprovePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetHiddenPostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnHiddenPostActionListener.class ),	
//			@EventConfig(listeners = UITopicDetail.SetUnApproveAttachmentActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.DeletePostActionListener.class),
			
			@EventConfig(listeners = UITopicDetail.QuickReplyActionListener.class),
			@EventConfig(listeners = UITopicDetail.PreviewReplyActionListener.class),
			
			@EventConfig(listeners = UITopicDetail.ViewPostedByUserActionListener.class ), 
			@EventConfig(listeners = UITopicDetail.ViewPublicUserInfoActionListener.class ) ,
			@EventConfig(listeners = UITopicDetail.ViewThreadByUserActionListener.class ),
			@EventConfig(listeners = UITopicDetail.WatchOptionActionListener.class ),
			@EventConfig(listeners = UITopicDetail.PrivateMessageActionListener.class ),
			@EventConfig(listeners = UITopicDetail.DownloadAttachActionListener.class ),
			@EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class),
			@EventConfig(listeners = UITopicDetail.AdvancedSearchActionListener.class),
			@EventConfig(listeners = UITopicDetail.BanIPAllForumActionListener.class),
			@EventConfig(listeners = UITopicDetail.BanIPThisForumActionListener.class),
			@EventConfig(listeners = UITopicDetail.AddBookMarkActionListener.class),
			@EventConfig(listeners = UITopicDetail.RSSActionListener.class),
			@EventConfig(listeners = UITopicDetail.UnWatchActionListener.class),
			@EventConfig(listeners = UITopicDetail.AddWatchingActionListener.class)
		}
)
@SuppressWarnings("unused")
public class UITopicDetail extends UIForumKeepStickPageIterator implements MarkupRenderer {

	private String categoryId ;
	private String forumId ; 
	private String topicId = "";
	private String link = "";
	private Forum forum;
	private Topic topic = new Topic();
	private boolean isEditTopic = false ;
	private String IdPostView = "false" ;
	private String IdLastPost = "false" ;
	private UserProfile userProfile = null;
	private String userName = " " ;
	private boolean isModeratePost = false ;
	private boolean isMod = false ;
	private boolean enableIPLogging = true;
	private boolean isCanPost = false;
	private boolean canCreateTopic;
	private boolean isGetSv = true;
	private boolean isShowQuickReply = true;
  private boolean isShowRule = true;
  private boolean isDoubleClickQuickReply = false;
	private String lastPoistIdSave = "";
	private String lastPostId = "", isApprove="", isHidden="";
	private List<String> listContactsGotten = new ArrayList<String>();
	List<BBCode> listBBCode = new ArrayList<BBCode>();
	private List<Watch> listWatches = new ArrayList<Watch>();
	private Map<String, Integer> pagePostRemember = new HashMap<String, Integer>();
	private Map<String, UserProfile> mapUserProfile = new HashMap<String, UserProfile>();
	private Map<String, CommonContact> mapContact = new HashMap<String, CommonContact>();
	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	public static final String FIELD_ADD_TAG = "AddTag" ;
	public UITopicDetail() throws Exception {
		isDoubleClickQuickReply = false;
		
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_T, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_B, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		addUIFormInput( new UIFormStringInput(FIELD_ADD_TAG, null)) ;
		addUIFormInput( new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA,null)) ;
		addChild(UIPostRules.class, null, null);
		this.setSubmitAction("GoNumberPage") ;
		this.setActions(new String[]{"PreviewReply","QuickReply"} );
		this.isLink = true;
	}
	
  private boolean isShowQuickReply() {
		return isShowQuickReply;
	}
	
	public String getLastPostId() {
  	return lastPostId;
  }

	public void setLastPostId(String lastPost) {
  	this.lastPostId = lastPost;
  }

	public String getRSSLink(String cateId){
		PortalContainer pcontainer =  PortalContainer.getInstance() ;
		return RSS.getRSSLink("forum", pcontainer.getPortalContainerInfo().getContainerName(), cateId);
	}
	
	public UserProfile getUserProfile() {
		return userProfile ;
	}
	
	public void setUserProfile(UserProfile userProfile) throws Exception {
		this.userProfile	= userProfile ;
  }
	
	public boolean getHasEnableIPLogging() {
	  return enableIPLogging;
  }
	
  private boolean isIPBaned(String ip){
  	List<String> ipBaneds = forum.getBanIP();
		if(ipBaneds != null && ipBaneds.size() > 0 && ipBaneds.contains(ip)) return true;
		return false;
	}
	
  private boolean isOnline(String userId) throws Exception {
		return getForumService().isOnline(userId) ;
	}
	
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	
	private int getPagePostRemember(String topicId) {
  	if(pagePostRemember.containsKey(topicId)) return pagePostRemember.get(topicId);
  	return 1;
  }
	
	private boolean isNotLogin() throws Exception {
	  
		if(UserHelper.isAnonim() && !forum.getIsLock() && !topic.getIsLock()) return true;
		return false;
	}
	public void setUpdateTopic(String categoryId, String forumId, String topicId) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topicId ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		isShowQuickReply = forumPortlet.isShowQuickReply();
		isShowRule = forumPortlet.isShowRules();
		enableIPLogging = forumPortlet.isEnableIPLogging();
		forumPortlet.updateAccessTopic(topicId);
		userProfile = forumPortlet.getUserProfile() ;
		userName = userProfile.getUserId() ;
		cleanCheckedList();
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		this.isUseAjax = forumPortlet.isUseAjax();
		listWatches = forumPortlet.getWatchinhByCurrentUser();
		
		// TODO : replace these 2 statements by ForumService.viewTopic(topicId, userName)
		this.topic = forumService.getTopic(categoryId, forumId, topicId, userName) ;
		forumService.setViewCountTopic((categoryId + "/" + forumId + "/" + topicId), userName);
		setRenderInfoPorlet();
	}
	
	public void setTopicFromCate(String categoryId, String forumId, Topic topic, int page) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		if(page > 0) pageSelect = page;
		else pageSelect = getPagePostRemember(topicId);
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		isShowQuickReply = forumPortlet.isShowQuickReply();
		isShowRule = forumPortlet.isShowRules();
		enableIPLogging = forumPortlet.isEnableIPLogging();
		cleanCheckedList();
		if(ForumUtils.isEmpty(topic.getDescription())) {
			this.topic = getForumService().getTopic(categoryId, forumId, topic.getId(), userName) ;
		} else this.topic = topic;
		getForumService().setViewCountTopic((categoryId + "/" + forumId + "/" + topicId), userName);
		forumPortlet.updateAccessTopic(topicId);
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		this.isUseAjax = forumPortlet.isUseAjax();
		userProfile = forumPortlet.getUserProfile() ;
		listWatches = forumPortlet.getWatchinhByCurrentUser();
		userName = userProfile.getUserId() ;
		setRenderInfoPorlet();
	}
	
	public void hasPoll(boolean hasPoll) throws Exception {
		this.topic.setIsPoll(hasPoll);
		if(hasPoll) setRenderInfoPorlet();
	}
	
	public void setUpdateContainer(String categoryId, String forumId, Topic topic, int numberPage) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		this.pageSelect = numberPage ;
		this.isEditTopic = false ;
		if(pageSelect == 0) pageSelect = getPagePostRemember(topicId);
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		isShowQuickReply = forumPortlet.isShowQuickReply();
		isShowRule = forumPortlet.isShowRules();
		enableIPLogging = forumPortlet.isEnableIPLogging();
		cleanCheckedList();
		if(ForumUtils.isEmpty(topic.getDescription())) {
			this.topic = getForumService().getTopic(categoryId, forumId, topic.getId(), userName) ;
		} else this.topic = topic;
		getForumService().setViewCountTopic((categoryId + "/" + forumId + "/" + topicId), userName);
		forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		this.isUseAjax = forumPortlet.isUseAjax();
		userProfile = forumPortlet.getUserProfile() ;
		listWatches = forumPortlet.getWatchinhByCurrentUser();
		userName = userProfile.getUserId() ;
		setRenderInfoPorlet();
	}
	
  public void setRenderInfoPorlet() throws Exception {

    isMod = (userProfile.getUserRole() == UserProfile.ADMIN)
        || (ForumServiceUtils.hasPermission(forum.getModerators(), userName));

    PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    ActionResponse actionRes = (ActionResponse) pcontext.getResponse();

    sendForumPollEvent(actionRes);
    sendQuickReplyEvent(actionRes);
    sendRuleEvent(actionRes);
  }

  private void sendRuleEvent(ActionResponse actionRes) throws Exception {
    ForumParameter param = new ForumParameter() ;
    List<String> list = param.getInfoRules();
    if(forum.getIsClosed() || forum.getIsLock()) {
    	list.set(0, "true");
    }
    else {
      list.set(0, "");
    }
    list.set(1, String.valueOf(getCanCreateTopic()));
    list.set(2, String.valueOf(isCanPost));
    param.setInfoRules(list);
    param.setRenderRule(true);
    actionRes.setEvent(new QName("ForumRuleEvent"), param) ;
  }

  private void sendQuickReplyEvent(ActionResponse actionRes) {
    ForumParameter param = new ForumParameter() ;
    try {
    	isCanPost = isCanPostReply();
    } catch (Exception e) {
    	isCanPost = false;
    }
    param.setRenderQuickReply(isCanPost);
    param.setModerator(isMod);
    param.setCategoryId(categoryId) ; 
    param.setForumId(forumId); 
    param.setTopicId(topicId);
    actionRes.setEvent(new QName("QuickReplyEvent"), param) ;
  }

  private void sendForumPollEvent(ActionResponse actionRes) {
    ForumParameter param = new ForumParameter() ;
    param.setCategoryId(categoryId) ; 
    param.setForumId(forumId); 
    param.setTopicId(topicId);
    param.setRenderPoll(topic.getIsPoll());
    actionRes.setEvent(new QName("ForumPollEvent"), param) ;
  }
	
	public void setIsGetSv(boolean isGetSv) {
		this.isGetSv = isGetSv;
  }
	
  public String getReplaceByBBCode(String s) throws Exception {
		syncBBCodeCache();
    if(!listBBCode.isEmpty()){
	    try {
	    	s = Utils.getReplacementByBBcode(s, listBBCode, forumService);
	    } catch (Exception e) {}
    }
    return s;
	}

  public void syncBBCodeCache() {
    List<String> activeOnserver = new ArrayList<String>();
    
		if(isGetSv) {
			List<BBCode> bbcs = new ArrayList<BBCode>();
			try {
				activeOnserver = getForumService().getActiveBBCode();
				isGetSv = false;
		    boolean isAdd = true;
		    BBCode bbCode;
		    for (String srvBBCode : activeOnserver) {
		    	isAdd = true;
		    	for (BBCode localBBCode : listBBCode) {
		    		if(localBBCode.getTagName().equals(srvBBCode) || (localBBCode.getTagName().equals(srvBBCode.replaceFirst("=", "")) && localBBCode.isOption())){
		    			bbcs.add(localBBCode);
		    			isAdd = false;
		    			break;
		    		}
		    	}
		    	if(isAdd) {
		    		bbCode = new BBCode();
		    		if(srvBBCode.indexOf("=") >= 0){
		    			bbCode.setOption(true);
		    			srvBBCode = srvBBCode.replaceFirst("=", "");
		    			bbCode.setId(srvBBCode+"_option");
		    		}else {
		    			bbCode.setId(srvBBCode);
		    		}
		    		bbCode.setTagName(srvBBCode);
		    		bbcs.add(bbCode);
		    	}
		    }
		    listBBCode.clear();
		    listBBCode.addAll(bbcs);
			} catch (Exception e) {}
		}
  }
	
	private boolean getCanCreateTopic() throws Exception {
		/**
		 * set permission for create new thread
		 */
		String[] strings = this.forum.getCreateTopicRole();
		canCreateTopic = this.isMod;
		if(!canCreateTopic){ 
			if(isIPBaned(getIPRemoter())) canCreateTopic = false;
			else {
				if(strings == null || strings.length == 0 || (strings.length == 1 && strings[0].equals(" "))) canCreateTopic = true;
				else canCreateTopic = ForumServiceUtils.hasPermission(strings, userName);
			}
		}
		return canCreateTopic;
	}
	
	public boolean getCanPost() throws Exception {
	  if(isEditTopic) {
	  	isCanPost = isCanPostReply();
	  }
	  return isCanPost;
  }
	
	public void setUpdateForum(Forum forum) throws Exception {
		this.forum = forum ;
	}
	
	private boolean isCanPostReply() throws Exception {
		if(userProfile.getUserRole() == 3) return false;
		if(forum.getIsClosed() || forum.getIsLock() || topic.getIsClosed() || topic.getIsLock()) return false;
		if(userProfile.getIsBanned()) return false;
		if(isMod) return true;
		if(isIPBaned(getIPRemoter())) return false;
		if(!topic.getIsActive() || !topic.getIsActiveByForum() || topic.getIsWaiting()) return false;
		List<String> listUser = new ArrayList<String>() ;
		
		listUser.add(topic.getOwner());
		listUser = ForumUtils.addArrayToList(listUser, topic.getCanPost());
		listUser = ForumUtils.addArrayToList(listUser, forum.getPoster());
		listUser = ForumUtils.addArrayToList(listUser, forumService.getCategory(categoryId).getPoster());
		System.out.println("\n\n can post: " + listUser.toString());
		if(!listUser.isEmpty()) {
			return ForumServiceUtils.hasPermission(listUser.toArray(new String[]{}), userName);
		}
		return true ;
	}
	
	private String getIPRemoter() throws Exception {
		if(enableIPLogging) {
			try {
				WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
				HttpServletRequest request = context.getRequest();
				return request.getRemoteAddr();
	    } catch (Exception e) {}
		}
		return "";
	}
	
	private Forum getForum() throws Exception {
		return this.forum ;
	}
	
	private String getIdPostView() {
		if(this.IdPostView.equals("lastpost")){
			this.IdPostView = "normal" ;
			return this.IdLastPost ;
		}
		if(this.IdPostView.equals("top")){
			this.IdPostView = "normal" ;
			return "top" ;
		}
		String temp = this.IdPostView ;
		this.IdPostView = "normal" ;
		return temp ;
	}
	
	public void setIdPostView(String IdPostView) {
		this.IdPostView = IdPostView ;
	}
	
	public void setIsEditTopic( boolean isEditTopic) {
		this.isEditTopic = isEditTopic ;
	}

	private Topic getTopic() throws Exception {
		try {
			if(this.isEditTopic || this.topic == null) {
				this.topic = getForumService().getTopic(categoryId, forumId, topicId, UserProfile.USER_GUEST) ;
				this.isEditTopic = false ;
			}
			return this.topic ;
		} catch (Exception e) {
		}
		return null ;
	}
	
	private boolean userCanView() throws Exception {
		if(isMod) return true;
		else {
			if(forum.getIsClosed() || topic.getIsClosed() || !topic.getIsActive() || !topic.getIsActiveByForum() || topic.getIsWaiting()) return false;
		}
		if(getCanPost()) return true;
		List<String> listUser = new ArrayList<String>() ;
		
		listUser.add(topic.getOwner());
		listUser = ForumUtils.addArrayToList(listUser, topic.getCanView());
		listUser = ForumUtils.addArrayToList(listUser, forum.getViewer());
		listUser = ForumUtils.addArrayToList(listUser, forumService.getPermissionTopicByCategory(categoryId, "viewer"));
		if(listUser.size() > 0) {
			return ForumServiceUtils.hasPermission(listUser.toArray(new String[]{}), userName);
		}
		return true;
	}
	
	public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }

  private String getFileSource(ForumAttachment attachment) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			InputStream input = attachment.getInputStream() ;
			String fileName = attachment.getName() ;
			return ForumSessionUtils.getFileSource(input, fileName, dservice);
		} catch (PathNotFoundException e) {
			return null;
		}
	}

	private CommonContact getPersonalContact(String userId) throws Exception {
	  CommonContact contact ;
		if(mapContact.containsKey(userId) && listContactsGotten.contains(userId)){
			contact = mapContact.get(userId) ;
		} else {
			contact = ForumSessionUtils.getPersonalContact(userId) ;
			mapContact.put(userId, contact) ;
			listContactsGotten.add(userId);
		}
		if(contact == null) {
			contact = new CommonContact() ;
		}
		return contact ;
	}
	
	private String getAvatarUrl(CommonContact contact, String userId) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		return ForumSessionUtils.getUserAvatarURL(userId, this.forumService, dservice);
	}

	private void initPage() throws Exception {
		objectId = topicId;
		isDoubleClickQuickReply = false;
		isGetSv = true;
		listContactsGotten =  new ArrayList<String>();
		try {
				isApprove = "";
				isHidden = "";
				if(!isMod) isHidden = "false"; 
				if (this.forum.getIsModeratePost() || this.topic.getIsModeratePost()) {
					isModeratePost = true;
					if (!isMod && !(this.topic.getOwner().equals(userName)))
						isApprove = "true";
				}
				pageList = getForumService().getPosts(this.categoryId, this.forumId, topicId, isApprove, isHidden, "", userName);
			int maxPost = this.userProfile.getMaxPostInPage().intValue();
			if (maxPost <= 0) maxPost = 10;
			pageList.setPageSize(maxPost);
			maxPage = pageList.getAvailablePage();
			if (IdPostView.equals("lastpost") || this.pageSelect > maxPage) {
				this.pageSelect = maxPage;
			}
		} catch (Exception e) {
		}
	}
	
	private boolean getIsModeratePost(){return this.isModeratePost; }
	
	@SuppressWarnings("unchecked")
  private List<Post> getPostPageList() throws Exception {
		List<Post> posts = new ArrayList<Post>();  
		if(this.pageList == null) return posts ;
		try {
			try {
				if(!ForumUtils.isEmpty(lastPostId)){
					int maxPost = this.userProfile.getMaxPostInPage().intValue();
					Long index = getForumService().getLastReadIndex((categoryId+"/"+forumId+"/"+topicId+"/"+lastPostId), isApprove, isHidden, userName);
					if(index.intValue() <= maxPost) pageSelect = 1;
					else {
						pageSelect =  (int) (index/maxPost);
						if(maxPost*pageSelect < index) pageSelect = pageSelect + 1;
					}
					lastPostId = "";
				}
      } catch (Exception e) {}
			posts = pageList.getPage(pageSelect) ;
			pageSelect = pageList.getCurrentPage();
			pagePostRemember.put(topicId, pageSelect);
			if(posts == null) posts = new ArrayList<Post>(); 
			List<String> userNames = new ArrayList<String>() ;
			mapUserProfile.clear() ;
			for (Post post : posts) {
				if(!userNames.contains(post.getOwner())) userNames.add(post.getOwner());			
				if(getUIFormCheckBoxInput(post.getId()) != null) {
					getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
				}else {
					addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
				}
				this.IdLastPost = post.getId() ;
			}
			if(!lastPoistIdSave.equals(IdLastPost)) {
				lastPoistIdSave = IdLastPost;
				userProfile.addLastPostIdReadOfForum(forumId, topicId+"/"+IdLastPost);
				userProfile.addLastPostIdReadOfTopic(topicId, IdLastPost);
				UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.getUserProfile().addLastPostIdReadOfForum(forumId, topicId+"/"+IdLastPost);
				forumPortlet.getUserProfile().addLastPostIdReadOfTopic(topicId, IdLastPost+","+ForumUtils.getInstanceTempCalendar().getTimeInMillis());
				getForumService().saveLastPostIdRead(userName, userProfile.getLastReadPostOfForum(), userProfile.getLastReadPostOfTopic());
			}
			//updateUserProfiles
			if(userNames.size() > 0) {
				try{
					List<UserProfile> profiles = getForumService().getQuickProfiles(userNames) ;
					for(UserProfile profile : profiles) {
						mapUserProfile.put(profile.getUserId(), profile) ;
					}
				}catch(Exception e) {
					e.printStackTrace() ;
				}
			}
		} catch (Exception e) {
    }
		return posts ;
	}
	
	private List<Tag> getTagsByTopic() throws Exception {
		List<Tag> list = new ArrayList<Tag>();
		List<String> listTagId = new ArrayList<String>();
		String[] tagIds = topic.getTagId();
		String[]temp;
		for (int i = 0; i < tagIds.length; i++) {
			temp = tagIds[i].split(":");
	    if(temp[0].equals(userName)) {
	    	listTagId.add(temp[1]);
	    }
    }
		try {
			list = getForumService().getMyTagInTopic(listTagId.toArray(new String[listTagId.size()]));
    } catch (Exception e) {
    }
		return list;	
	}
	
	private Post getPost(String postId) throws Exception {
		return getForumService().getPost(categoryId, forumId, topicId, postId);
	}
	
	public void setPostRules(boolean isNull) throws Exception {
		UIPostRules postRules = getChild(UIPostRules.class); 
		postRules.setUserProfile(this.userProfile) ;
		if(!isNull) {
			if(this.forum.getIsClosed() || this.forum.getIsLock()){
				postRules.setLock(true);
			} else {
				postRules.setCanCreateNewThread(canCreateTopic);
				/**
				 * set permission for post reply
				 */
				if(this.topic != null && !this.topic.getIsClosed() && !this.topic.getIsLock()){
					postRules.setCanAddPost(getCanPost());
				} else {
					postRules.setCanAddPost(false);
				}
			}
		} else {
			postRules.setCanCreateNewThread(!isNull);
			postRules.setCanAddPost(!isNull);
		}
	}
	
	private UserProfile getUserInfo(String userName) throws Exception {
		if(!mapUserProfile.containsKey(userName)) {
			try{
				mapUserProfile.put(userName, getForumService().getQuickProfile(userName)) ;			
			}catch(Exception e){
				e.printStackTrace() ;
			}
		}
		return mapUserProfile.get(userName) ;
	}
	
	public void setListWatches(List<Watch> listWatches) {
	  this.listWatches = listWatches;
  }
	
	private boolean isWatching(String path) throws Exception {
		for (Watch watch : listWatches) {
			if(path.equals(watch.getNodePath())) return true;
    }
		return false;
	}

	private String getEmailWatching(String path) throws Exception {
		for (Watch watch : listWatches) {
			try {
				if(watch.getNodePath().endsWith(path)) return watch.getEmail();
      } catch (Exception e) {}
		}
		return "";
	}

	static public class AddPostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			try {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost("", false, false, null) ;
				postForm.setMod(topicDetail.isMod) ;
				
				popupContainer.setId("UIAddPostContainer") ;
				popupAction.activate(popupContainer, 900, 460) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class RatingTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			try{
				String userName = UserHelper.getCurrentUser() ;
				String[] userVoteRating = topicDetail.topic.getUserVoteRating() ;
				boolean erro = false ;
				for (String string : userVoteRating) {
					if(string.equalsIgnoreCase(userName)) erro = true ; 
				}
				if(!erro) {
					UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
					UIRatingForm ratingForm = popupAction.createUIComponent(UIRatingForm.class, null, null) ;
					ratingForm.updateRating(topicDetail.topic, topicDetail.categoryId, topicDetail.forumId) ;
					popupAction.activate(ratingForm, 300, 145) ;
					
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
					topicDetail.isEditTopic = true ;
				} else {
					Object[] args = { userName };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicDetail.sms.VotedRating", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	
	static public class AddTagTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
			try {
				UIFormStringInput stringInput = topicDetail.getUIStringInput(FIELD_ADD_TAG);
				String tagIds = stringInput.getValue();
				if(!ForumUtils.isEmpty(tagIds)) {
					String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=*':}{\"";
					for (int i = 0; i < special.length(); i++) {
						char c = special.charAt(i);
						if(tagIds.indexOf(c) >= 0) {
							UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
							uiApp.addMessage(new ApplicationMessage("UITopicDetail.msg.failure", null, ApplicationMessage.WARNING)) ;
							return ;
						}
					}
					while (tagIds.indexOf("  ") > 0) {
		        tagIds = StringUtils.replace(tagIds, "  ", " ");
	        }
					List<String> listTags = new ArrayList<String>();
					for (String string : Arrays.asList(tagIds.split(" "))) {
						if(!listTags.contains(string) && !ForumUtils.isEmpty(string)) {
							listTags.add(string);
						}
	        }
					List<Tag> tags = new ArrayList<Tag>();
					Tag tag;
					for (String string : listTags) {
		        tag = new Tag();
		        tag.setName(string);
		        tag.setId(Utils.TAG + string);
		        tag.setUserTag(new String[]{topicDetail.userName});
		        tags.add(tag);
	        }
					try {
						topicDetail.getForumService().addTag(tags, topicDetail.userName, topicDetail.topic.getPath());
	        } catch (Exception e) {
		        e.printStackTrace();
	        }
				} else {
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicDetail.msg.empty-field", null, ApplicationMessage.WARNING)) ;
					return ;
				}
				stringInput.setValue("");
				topicDetail.isEditTopic = true;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class UnTagTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			topicDetail.getForumService().unTag(tagId, topicDetail.userName, topicDetail.topic.getPath());
			topicDetail.isEditTopic = true;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}
	
	static public class OpenTopicsTagActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.TAG) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption("") ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(tagId) ;
			forumPortlet.getChild(UITopicsTag.class).setIdTag(tagId) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class SearchFormActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String path = topicDetail.topic.getPath() ;
			UIFormStringInput formStringInput = topicDetail.getUIStringInput(ForumUtils.SEARCHFORM_ID) ;
			String text = formStringInput.getValue() ;
			if(!ForumUtils.isEmpty(text) && !ForumUtils.isEmpty(path)) {
				String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=|:\"'";
				for (int i = 0; i < special.length(); i++) {
					char c = special.charAt(i);
					if(text.indexOf(c) >= 0) {
						UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
						uiApp.addMessage(new ApplicationMessage("UIQuickSearchForm.msg.failure", null, ApplicationMessage.WARNING)) ;
						return ;
					}
				}
				StringBuffer type = new StringBuffer();
				if(topicDetail.isMod){ 
					type.append("true,").append(Utils.POST);
				} else {
					type.append("false,").append(Utils.POST);
				}
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES) ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				UICategories categories = categoryContainer.getChild(UICategories.class);
				categories.setIsRenderChild(true) ;
				List<ForumSearch> list = 
					topicDetail.getForumService().getQuickSearch(text, type.toString(), path, 
					topicDetail.getUserProfile().getUserId(), forumPortlet.getInvisibleCategories(), forumPortlet.getInvisibleForums(), null);
				
				UIForumListSearch listSearchEvent = categories.getChild(UIForumListSearch.class) ;
				listSearchEvent.setListSearchEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
				formStringInput.setValue("") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIQuickSearchForm.msg.checkEmpty", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class PrintActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
//			UITopicDetail topicDetail = event.getSource() ;
		}
	}

	static public class GoNumberPageActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			int idbt = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
			UIFormStringInput stringInput1 = topicDetail.getUIStringInput(ForumUtils.GOPAGE_ID_T) ;
			UIFormStringInput stringInput2 = topicDetail.getUIStringInput(ForumUtils.GOPAGE_ID_B) ;
			String numberPage = "" ;
			if(idbt == 1) {
				numberPage = stringInput1.getValue() ;
			} else {
				numberPage = stringInput2.getValue() ;
			}
			numberPage = ForumUtils.removeZeroFirstNumber(numberPage) ;
			stringInput1.setValue("") ; stringInput2.setValue("") ;
			if(!ForumUtils.isEmpty(numberPage)) {
				try {
					int page = Integer.parseInt(numberPage.trim()) ;
					if(page < 0) {
						Object[] args = { "go page" };
						throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
					} else {
						if(page == 0) {
							page = 1;
						} else if(page > topicDetail.pageList.getAvailablePage()){
							page = topicDetail.pageList.getAvailablePage() ;
						}
						topicDetail.pageSelect = page ;
						event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
					}
				} catch (NumberFormatException e) {
					Object[] args = { "go page" };
					throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
				}
			}
		}
	}

	static public class EditActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Post post = topicDetail.getPost(postId);
			if(post !=  null) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost(postId, false, false, post) ;
				postForm.setMod(topicDetail.isMod) ;
				popupContainer.setId("UIEditPostContainer") ;
				popupAction.activate(popupContainer, 900, 460) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIPostForm.msg.canNotEdit", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
	
	static public class DeleteActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			try {
				topicDetail.getForumService().removePost(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, postId) ;
				topicDetail.IdPostView = "top";
			}catch (Exception e) {
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
		}
	}
	
	static public class QuoteActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Post post = topicDetail.getPost(postId);
			if(post !=  null) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost(postId, true, false, post) ;
				postForm.setMod(topicDetail.isMod);
				popupContainer.setId("UIQuoteContainer") ;
				popupAction.activate(popupContainer, 900, 500) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class PrivatePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Post post = topicDetail.getPost(postId);
			if(post !=  null) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost(postId, false, true, post) ;
				postForm.setMod(topicDetail.isMod) ;
				popupContainer.setId("UIPrivatePostContainer") ;
				popupAction.activate(popupContainer, 900, 460) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
//--------------------------------	 Topic Menu		-------------------------------------------//
	static public class EditTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			try{
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
				topicForm.setTopicIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.forum, topicDetail.userProfile.getUserRole()) ;
				topicForm.setUpdateTopic(topicDetail.getTopic(), true) ;
				topicForm.setMod(topicDetail.isMod) ;
				topicForm.setIsDetail(true);
				popupContainer.setId("UIEditTopicContainer") ;
				popupAction.activate(popupContainer, 900, 460) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				topicDetail.isEditTopic = true ;
			} catch (Exception e) {
				e.printStackTrace();
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class PrintPageActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
//			UITopicDetail topicDetail = event.getSource() ;
		}
	}

	static public class AddPollActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try {
				Topic topic = topicDetail.topic ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPollForm	pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
				pollForm.setTopicPath(topic.getPath()) ;
				popupAction.activate(pollForm, 655, 455) ;
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetOpenTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic ;
				if(topic.getIsClosed()) {
					topic.setIsClosed(false) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 1) ;
					topicDetail.isEditTopic = true ;
					topicDetail.setRenderInfoPorlet();
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
				} else {
					Object[] args = { topic.getTopicName() };
					throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Open", args, ApplicationMessage.WARNING)) ;
				}
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetCloseTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic ;
				if(!topic.getIsClosed()) {
					topic.setIsClosed(true) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 1) ;
					topicDetail.isEditTopic = true ;
					topicDetail.setRenderInfoPorlet();
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
				} else {
					Object[] args = { topic.getTopicName() };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicContainer.sms.Close", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetLockedTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic ;
				if(!topic.getIsLock()) {
					topic.setIsLock(true) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 2) ;
					topicDetail.isEditTopic = true ;
					topicDetail.setRenderInfoPorlet();
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
				} else {
					Object[] args = { topic.getTopicName() };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicContainer.sms.Locked", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetUnLockTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic ;
				if(topic.getIsLock()) {
					topic.setIsLock(false) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 2) ;
					topicDetail.isEditTopic = true ;
					topicDetail.setRenderInfoPorlet();
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
				} else {
					Object[] args = { topic.getTopicName() };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicContainer.sms.UnLock", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetMoveTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			try{
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMoveTopicForm moveTopicForm = popupAction.createUIComponent(UIMoveTopicForm.class, null, null) ;
				moveTopicForm.setUserProfile(topicDetail.userProfile) ;
				List <Topic> topics = new ArrayList<Topic>();
				topics.add(topicDetail.topic) ;
				topicDetail.isEditTopic = true ;
				moveTopicForm.updateTopic(topicDetail.forumId, topics, true);
				popupAction.activate(moveTopicForm, 400, 420) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	
	static public class SetStickTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic ;
				if(!topic.getIsSticky()) {
					topic.setIsSticky(true) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 4) ;
					topicDetail.isEditTopic = true ;
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
				} else {
					Object[] args = { topic.getTopicName() };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicContainer.sms.Stick", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetUnStickTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic ;
				if(topic.getIsSticky()) {
					topic.setIsSticky(false) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 4) ;
					topicDetail.isEditTopic = true ;
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
				} else {
					Object[] args = { topic.getTopicName() };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicContainer.sms.UnStick", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SplitTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				JCRPageList pageList = topicDetail.getForumService().getPostForSplitTopic(topicDetail.categoryId+"/"+topicDetail.forumId +"/"+topicDetail.topicId);
				if(pageList.getAvailable() > 0) {
					UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
					UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
					UISplitTopicForm splitTopicForm = popupAction.createUIComponent(UISplitTopicForm.class, null, null) ;
					splitTopicForm.setPageListPost(pageList) ;
					splitTopicForm.setTopic(topicDetail.topic) ;
					splitTopicForm.setUserProfile(topicDetail.userProfile) ;
					popupAction.activate(splitTopicForm, 700, 400) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				} else {
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UITopicContainer.sms.NotSplit", null, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} catch (Exception e) {
				e.printStackTrace();
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetApproveTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic;
				topic.setIsApproved(true) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				topicDetail.getForumService().modifyTopic(topics, 3) ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	
	static public class SetUnApproveTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try{
				Topic topic = topicDetail.topic;
				topic.setIsApproved(false) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				topicDetail.getForumService().modifyTopic(topics, 3) ;
				topicDetail.isEditTopic = true ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class SetDeleteTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			try {
				Topic topic = topicDetail.topic ;
				topicDetail.getForumService().removeTopic(topicDetail.categoryId, topicDetail.forumId, topic.getId()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				uiForumContainer.setIsRenderChild(true) ;
				UITopicContainer topicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
				topicContainer.setUpdateForum(topicDetail.categoryId, topicDetail.forum, 0) ;
				UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiForumContainer) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
			} catch (Exception e) {
			  
			  
			  topicDetail.warning("UIForumPortlet.msg.topicEmpty");

				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	//---------------------------------	Post Menu	 --------------------------------------//
	static public class MergePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
		}
	}

	static public class DownloadAttachActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}

	static public class MovePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				posts.add(topicDetail.getPost(postId));
      }
			if(posts.size() > 0) {
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIMovePostForm movePostForm = popupAction.createUIComponent(UIMovePostForm.class, null, null) ;
				movePostForm.setUserProfile(topicDetail.userProfile) ;
				movePostForm.updatePost(topicDetail.topicId, posts);
				popupAction.activate(movePostForm, 400, 430) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheckPost", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetApprovePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				posts.add(topicDetail.getPost(postId));
      }
			if(posts.isEmpty()){
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIPageListPostUnApprove postUnApprove = popupContainer.addChild(UIPageListPostUnApprove.class, null, null) ;
				postUnApprove.setUpdateContainer(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
				popupContainer.setId("PageListPostUnApprove") ;
				popupAction.activate(popupContainer, 500, 360) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				int count = 0;
				while(count < posts.size()){
					if(!posts.get(count).getIsApproved()){
						posts.get(count).setIsApproved(true);
						count ++;
					} else {
						posts.remove(count);
					}
				}
				if(posts.size() > 0){
					try {
						topicDetail.getForumService().modifyPost(posts, 1) ;
					} catch (Exception e) {
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
				}
			}
		}
	}
	
	static public class SetHiddenPostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Post post = new Post() ;
			List<String> postIds = topicDetail.getIdSelected(); 
			if(postIds == null || postIds.isEmpty()){
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheckPost", args, ApplicationMessage.WARNING)) ;
			}
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				post = topicDetail.getPost(postId);
				if(post != null && !post.getIsHidden()){
					post.setIsHidden(true) ;
					posts.add(post) ;
				}
      }
			try {
				topicDetail.getForumService().modifyPost(posts, 2) ;
			} catch (Exception e) {
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}

	static public class SetUnHiddenPostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				Post post = topicDetail.getPost(postId);
				if(post != null) {
					posts.add(post);
				}
      }
			if(posts.isEmpty()){
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
				UIPageListPostHidden listPostHidden = popupContainer.addChild(UIPageListPostHidden.class, null, null) ;
				listPostHidden.setUpdateContainer(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
				popupContainer.setId("PageListPostHidden") ;
				popupAction.activate(popupContainer, 500, 360) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				int count = 0;
				while(count < posts.size()){
					if(posts.get(count).getIsHidden()){
						posts.get(count).setIsHidden(false);
						count ++;
					} else {
						posts.remove(count);
					}
				}
				if(posts.size() > 0){
					try {
						topicDetail.getForumService().modifyPost(posts, 2) ;
					} catch (Exception e) {
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
				}
			}
		}
	}
	
	static public class SetUnApproveAttachmentActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
		}
	}
	
	static public class DeletePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				posts.add(topicDetail.getPost(postId));
      }
			for(Post post : posts) {
				try {
					topicDetail.getForumService().removePost(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post.getId()) ;
				} catch(Exception e){
					e.printStackTrace();
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			}
		}
	}

	static public class ViewPublicUserInfoActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIViewUserProfile viewUserProfile = popupAction.createUIComponent(UIViewUserProfile.class, null, null) ;
			try{
				UserProfile selectProfile = topicDetail.getForumService().getUserInformations(topicDetail.mapUserProfile.get(userId)) ;
				viewUserProfile.setUserProfile(selectProfile) ;
			}catch(Exception e) {
				e.printStackTrace() ;
			}
			viewUserProfile.setUserProfileLogin(topicDetail.userProfile) ;
			CommonContact contact = null ;
			if(topicDetail.mapContact.containsKey(userId)) {
				contact = topicDetail.mapContact.get(userId) ;
			}
			viewUserProfile.setContact(contact) ;
			popupAction.activate(viewUserProfile, 670, 400, true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class PrivateMessageActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			if(topicDetail.userProfile.getIsBanned()){
				String[] args = new String[] { } ;
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.userIsBannedCanNotSendMail", args, ApplicationMessage.WARNING)) ;
			}
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPrivateMessageForm messageForm = popupContainer.addChild(UIPrivateMessageForm.class, null, null) ;
			messageForm.setFullMessage(false);
			messageForm.setUserProfile(topicDetail.userProfile);
			messageForm.setSendtoField(userId) ;
			popupContainer.setId("PrivateMessageForm") ;
			popupAction.activate(popupContainer, 650, 480) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class ViewPostedByUserActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIViewPostedByUser viewPostedByUser = popupContainer.addChild(UIViewPostedByUser.class, null, null) ;
			viewPostedByUser.setUserProfile(userId) ;
			popupContainer.setId("ViewPostedByUser") ;
			popupAction.activate(popupContainer, 760, 370) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class ViewThreadByUserActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIViewTopicCreatedByUser topicCreatedByUser = popupContainer.addChild(UIViewTopicCreatedByUser.class, null, null) ;
			topicCreatedByUser.setUserId(userId) ;
			popupContainer.setId("ViewTopicCreatedByUser") ;
			popupAction.activate(popupContainer, 760, 450) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	private boolean checkForumHasAddTopic(UserProfile userProfile) throws Exception {
		try {
			this.topic = (Topic)getForumService().getObjectNameById(this.topicId, Utils.TOPIC);
			if(topic.getIsClosed() || topic.getIsLock()) return false;
			Forum forum = (Forum)getForumService().getObjectNameById(this.forumId, Utils.FORUM);
			if(forum.getIsClosed() || forum.getIsLock()) return false;
			if(userProfile.getUserRole() > 1 || (userProfile.getUserRole() == 1 && !ForumServiceUtils.hasPermission(forum.getModerators(), userProfile.getUserId()))) {
				if(!topic.getIsActive() || !topic.getIsActiveByForum()) return false;
				String[] canCreadPost = topic.getCanPost();
				if(canCreadPost != null && canCreadPost.length > 0 && !canCreadPost[0].equals(" ")){
					return ForumServiceUtils.hasPermission(canCreadPost, userProfile.getUserId());
				}
			}
    } catch (Exception e) {
    	throw e;
    }
    return true;
	}
	


  private String[] getCensoredKeyword() throws Exception {
		ForumAdministration forumAdministration = getForumService().getForumAdministration() ;
		String stringKey = forumAdministration.getCensoredKeyword();
		if(stringKey != null && stringKey.length() > 0) {
			stringKey = stringKey.toLowerCase().replaceAll(", ", ",").replaceAll(" ,", ",") ;
			if(stringKey.contains(",")){ 
				stringKey.replaceAll(";", ",") ;
				return stringKey.trim().split(",") ;
			} else { 
				return stringKey.trim().split(";") ;
			}
		}
		return new String[]{};
	}
	
	static public class QuickReplyActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			if(topicDetail.isDoubleClickQuickReply) return;
			topicDetail.isDoubleClickQuickReply = true;
			try {
  			UIFormTextAreaInput textAreaInput = topicDetail.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA) ;
  			String message = "";
  			try {
  				message = textAreaInput.getValue();
        } catch (Exception e) {}
  			String checksms = message ;
  			if(message != null && message.trim().length() > 0) {
  				if(topicDetail.checkForumHasAddTopic(topicDetail.userProfile)){
	  				boolean isOffend = false ;
	  				boolean hasTopicMod = false ;
	  				if(!topicDetail.isMod) {
  						String []censoredKeyword = topicDetail.getCensoredKeyword() ;
  						checksms = checksms.toLowerCase().trim();
  						for (String string : censoredKeyword) {
  							if(checksms.indexOf(string.trim().toLowerCase()) >= 0) {isOffend = true ;break;}
  						}
	  					if(topicDetail.topic != null) hasTopicMod = topicDetail.topic.getIsModeratePost() ;
	  				}
	  				StringBuffer buffer = new StringBuffer();
	  				for (int j = 0; j < message.length(); j++) {
	  					char c = message.charAt(j); 
	  					if((int)c == 9){
	  						buffer.append("&nbsp; &nbsp; ") ;
	  					} else if((int)c == 10){
	  						buffer.append("<br/>") ;
	  					}	else if((int)c == 60){
	  						buffer.append("&lt;") ;
	  					} else if((int)c == 62){
	  						buffer.append("&gt;") ;
	  					} else if(c == '\''){
	  						buffer.append("&apos;") ;
	  					} else if(c == '&' || (int)c == 38){
	  						buffer.append("&#x26;");
	  					} else{
	  						buffer.append(c) ;
	  					}
	  				} 
	  				
	  				// set link
	  				String link = ForumSessionUtils.getBreadcumbUrl(topicDetail.getLink(), topicDetail.getId(), "ViewThreadByUser", topicDetail.topicId).replaceFirst("private", "public");				
	  				//
	  				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
	  				String userName = topicDetail.userProfile.getUserId() ;
	  				String remoteAddr = topicDetail.getIPRemoter();
	  				Topic topic = topicDetail.topic ;
	  				Post post = new Post() ;
	  				post.setName("Re: " + topic.getTopicName()) ;
	  				post.setMessage(buffer.toString()) ;
	  				post.setOwner(userName) ;
	  				post.setRemoteAddr(remoteAddr) ;
	  				post.setIcon(topic.getIcon());
	  				post.setIsHidden(isOffend) ;
	  				post.setIsApproved(!hasTopicMod) ;
	  				post.setLink(link);
	  				try {
	  					topicDetail.getForumService().savePost(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post, true, ForumUtils.getDefaultMail()) ;
	  					long postCount = topicDetail.getUserInfo(userName).getTotalPost() + 1 ;
	  					topicDetail.getUserInfo(userName).setTotalPost(postCount);
	  					topicDetail.getUserInfo(userName).setLastPostDate(ForumUtils.getInstanceTempCalendar().getTime()) ;
	  					topicDetail.getForumService().updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  topic.getId()) ;
	  					forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
	  					if(topicDetail.userProfile.getIsAutoWatchTopicIPost()) {
	  						List<String> values = new ArrayList<String>();
	  						values.add(topicDetail.userProfile.getEmail());
	  						String path = topicDetail.categoryId + "/" + topicDetail.forumId + "/" + topicDetail.topicId;
	  						topicDetail.getForumService().addWatch(1, path, values, topicDetail.userProfile.getUserId()) ;
	  					}
	  				} catch (PathNotFoundException e) {
	  					String[] args = new String[] { } ;
	  					throw new MessageException(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
	  				} catch (Exception e) {}
	  				textAreaInput.setValue("") ;
	  				if(isOffend || hasTopicMod) {
	  					Object[] args = { "" };
	  					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
	  					if(isOffend)uiApp.addMessage(new ApplicationMessage("MessagePost.msg.isOffend", args, ApplicationMessage.WARNING)) ;
	  					else {
	  						args = new Object[]{ };
	  						uiApp.addMessage(new ApplicationMessage("MessagePost.msg.isModerate", args, ApplicationMessage.WARNING)) ;
	  					}
	  					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
	  					topicDetail.IdPostView = "normal";
	  				} else {
	  					topicDetail.IdPostView = "lastpost";
	  				}
	  				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
	  			}else {
	  				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
	  				uiApp.addMessage(new ApplicationMessage("UIPostForm.msg.no-permission", null, ApplicationMessage.WARNING)) ;
	  				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
	  				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
	  			}
  			}else {
  				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
  				String[] args = new String[] { topicDetail.getLabel(FIELD_MESSAGE_TEXTAREA) } ;
  				uiApp.addMessage(new ApplicationMessage("MessagePost.msg.message-empty", args, ApplicationMessage.WARNING)) ;
  				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
  				topicDetail.isDoubleClickQuickReply = false;
  			}
      } catch (Exception e) {
      	UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
      	Object[] args = {""};
  			uiApp.addMessage(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
  			event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
  			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
      }
		}
	}
	
	static public class PreviewReplyActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;	
			String message = topicDetail.getUIStringInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
			String checksms = (message) ;
			if(checksms != null && message.trim().length() > 0) {
				StringBuffer buffer = new StringBuffer();
				for (int j = 0; j < message.length(); j++) {
					char c = message.charAt(j); 
					if((int)c == 9){
						buffer.append("&nbsp; &nbsp; ") ;
					} else if((int)c == 10){
						buffer.append("<br/>") ;
					}	else if((int)c == 60){
						buffer.append("&lt;") ;
					} else if((int)c == 62){
						buffer.append("&gt;") ;
					} else if(c == '&' || (int)c == 38){
						buffer.append("&#x26;");
					} else {
						buffer.append(c) ;
					}
				} 
				String userName = topicDetail.userProfile.getUserId() ;
				Topic topic = topicDetail.topic ;
				Post post = new Post() ;
				post.setName("Re: " + topic.getTopicName()) ;
				post.setMessage(buffer.toString()) ;
				post.setOwner(userName) ;
				post.setRemoteAddr("") ;
				post.setIcon(topic.getIcon());
				post.setIsApproved(false) ;
				post.setCreatedDate(new Date()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true)	;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670) ;
				viewPost.setPostView(post) ;
				viewPost.setViewUserInfo(false) ;
				viewPost.setActionForm(new String[] {"Close"});
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}else {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				String[] args = new String[] { topicDetail.getLabel(FIELD_MESSAGE_TEXTAREA) } ;
				uiApp.addMessage(new ApplicationMessage("MessagePost.msg.message-empty", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			}
		}
	}
	
	static public class WatchOptionActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource();
			Topic topic = topicDetail.topic ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIWatchToolsForm watchToolsForm = popupAction.createUIComponent(UIWatchToolsForm.class, null, null) ;
			watchToolsForm.setPath(topic.getPath());
			watchToolsForm.setEmails(topic.getEmailNotification()) ;
			watchToolsForm.setIsTopic(true);
			popupAction.activate(watchToolsForm, 500, 365) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}	
	
	static	public class AdvancedSearchActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			UISearchForm searchForm = forumPortlet.getChild(UISearchForm.class) ;
			searchForm.setUserProfile(forumPortlet.getUserProfile()) ;
			searchForm.setSelectType(Utils.POST) ;
			searchForm.setPath(uiForm.topic.getPath());
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class BanIPAllForumActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			String ip = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UITopicDetail uiForm = event.getSource() ;
			if(!uiForm.getForumService().addBanIP(ip)){
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumAdministrationForm.sms.ipBanFalse", new Object[]{ip}, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}

	static	public class BanIPThisForumActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			String ip = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UITopicDetail topicDetail = event.getSource() ;
			List<String> listIp = topicDetail.forum.getBanIP();
			if(listIp == null || listIp.size()  == 0) listIp = new ArrayList<String>();
			listIp.add(ip);topicDetail.forum.setBanIP(listIp);
			if(!topicDetail.getForumService().addBanIPForum(ip, (topicDetail.categoryId + "/" + topicDetail.forumId))){
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIBanIPForumManagerForm.sms.ipBanFalse", new Object[]{ip}, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
		}
	}
	
	static public class AddBookMarkActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource();
			try{
				Topic topic = topicDetail.getTopic();
				StringBuffer buffer = new StringBuffer();
				buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//").append(topic.getId()) ;
				String userName = topicDetail.userProfile.getUserId() ;
				topicDetail.getForumService().saveUserBookmark(userName, buffer.toString(), true) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateUserProfileInfo() ;
			} catch (Exception e) {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}

	static public class AddWatchingActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource();
			if(topicDetail.getTopic() != null) {
				topicDetail.isEditTopic = true;
				StringBuffer buffer = new StringBuffer();
				buffer.append(topicDetail.categoryId).append("/").append(topicDetail.forumId).append("/").append(topicDetail.topicId) ;
				List<String> values = new ArrayList<String>();
				try {
					values.add(topicDetail.userProfile.getEmail());
					topicDetail.getForumService().addWatch(1, buffer.toString(), values, topicDetail.userProfile.getUserId()) ;
					UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.updateWatchinh();
					topicDetail.listWatches = forumPortlet.getWatchinhByCurrentUser();
					Object[] args = { };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.successfully", args, ApplicationMessage.INFO)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
				} catch (Exception e) {
					e.printStackTrace();
					Object[] args = { };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.fall", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
			} else {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	

	static public class UnWatchActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource();
			if(topicDetail.getTopic() != null) {
				topicDetail.isEditTopic = true;
				String path =  topicDetail.categoryId+"/"+topicDetail.forumId+"/"+topicDetail.topicId;
				try {
					topicDetail.getForumService().removeWatch(1, path, topicDetail.userProfile.getUserId()+"/"+topicDetail.getEmailWatching(path)) ;
					UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.updateWatchinh();
					topicDetail.listWatches = forumPortlet.getWatchinhByCurrentUser();
					Object[] args = { };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.UnWatchSuccessfully", args, ApplicationMessage.INFO)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				} catch (Exception e) {
					e.printStackTrace();
					Object[] args = { };
					UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIAddWatchingForm.msg.UnWatchfall", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} else {
				UIApplication uiApp = topicDetail.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.topicEmpty", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE) ;
				forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			}
		}
	}
	

	static public class RSSActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail uiForm = event.getSource();
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!uiForm.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)){
				uiForm.getForumService().addWatch(-1, topicId, null, uiForm.userName);
			}
			/*String rssLink = uiForm.getRSSLink(topicId);
			UIForumPortlet portlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = portlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			popupContainer.setId("ForumRSSForm") ;
			UIRSSForm exportForm = popupContainer.addChild(UIRSSForm.class, null, null) ;
			popupAction.activate(popupContainer, 560, 170) ;
			exportForm.setRSSLink(rssLink);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;*/
		}
	}

  public String processMarkup(String markup) throws RenderingException {
    try {

        return getReplaceByBBCode(markup);

    } catch (Exception e) {
      throw new RenderingException(e);
    }
  }
}
