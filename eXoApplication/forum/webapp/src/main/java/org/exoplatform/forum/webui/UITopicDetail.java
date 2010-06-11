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
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.info.ForumParameter;
import org.exoplatform.forum.rendering.RenderHelper;
import org.exoplatform.forum.rendering.RenderingException;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumSearch;
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
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.rss.RSS;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;


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
public class UITopicDetail extends  UIForumKeepStickPageIterator {

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
	private List<Watch> listWatches = new ArrayList<Watch>();
	private Map<String, Integer> pagePostRemember = new HashMap<String, Integer>();
	private Map<String, UserProfile> mapUserProfile = new HashMap<String, UserProfile>();
	private Map<String, CommonContact> mapContact = new HashMap<String, CommonContact>();

	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	public static final String FIELD_ADD_TAG = "AddTag" ;
	
	RenderHelper renderHelper = new RenderHelper();
	
	public UITopicDetail() throws Exception {
		isDoubleClickQuickReply = false;
		
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_T, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_B, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		addUIFormInput( new UIFormStringInput(FIELD_ADD_TAG, null)) ;
		addUIFormInput( new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA,null)) ;
		addChild(UIPostRules.class, null, null);
		this.setActions(new String[]{"PreviewReply","QuickReply"} );
		this.isLink = true;
	}
	
  public boolean isShowQuickReply() {
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
	
	private String getRestPath() throws Exception {
		try {
			ExoContainerContext exoContext = (ExoContainerContext)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ExoContainerContext.class);
	    return "/"+exoContext.getPortalContainerName()+"/"+exoContext.getRestContextName();
    } catch (Exception e) {
	    log.error("Can not get portal name or rest context name, exception: ",e);
    }
		return "";
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
	
  public boolean isIPBaned(String ip){
  	List<String> ipBaneds = forum.getBanIP();
		if(ipBaneds != null && ipBaneds.size() > 0 && ipBaneds.contains(ip)) return true;
		return false;
	}
	
  public boolean isOnline(String userId) throws Exception {
		return getForumService().isOnline(userId) ;
	}
	
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	
	private int getPagePostRemember(String topicId) {
  	if(pagePostRemember.containsKey(topicId)) return pagePostRemember.get(topicId);
  	return 1;
  }
	
	public boolean isNotLogin() throws Exception {
	  
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
		this.topic = getForumService().getTopic(categoryId, forumId, topicId, userName) ;
		getForumService().setViewCountTopic((categoryId + "/" + forumId + "/" + topicId), userName);
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
		/**
		 * Set permission for current user login.  	
		*/  
  	isMod = (userProfile.getUserRole() == UserProfile.ADMIN)
        		||(ForumServiceUtils.hasPermission(forum.getModerators(), userName));
  	if(topic != null){
	    canCreateTopic = getCanCreateTopic();
	    isCanPost = isCanPostReply();
  	}
    try {
    	PortletRequestContext pcontext = (PortletRequestContext) WebuiRequestContext.getCurrentInstance();
    	ActionResponse actionRes = (ActionResponse) pcontext.getResponse();
    	sendForumPollEvent(actionRes);
    	sendQuickReplyEvent(actionRes);
    	sendRuleEvent(actionRes);
    } catch (Exception e) {
    	log.error("Can not cast class PortletResponse to ActionResponse");
    }
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
    list.set(1, String.valueOf(canCreateTopic));
    list.set(2, String.valueOf(isCanPost));
    param.setInfoRules(list);
    param.setRenderRule(true);
    actionRes.setEvent(new QName("ForumRuleEvent"), param) ;
  }

  private void sendQuickReplyEvent(ActionResponse actionRes) {
    ForumParameter param = new ForumParameter() ;
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

	private boolean getCanCreateTopic() throws Exception {
		/**
		 * set permission for create new thread
		 */
		boolean canCreateTopic = true;
		boolean isCheck = true;
		List<String> ipBaneds = forum.getBanIP();
		if(ipBaneds != null && ipBaneds.contains(getRemoteIP()) || userProfile.getIsBanned()) {
			canCreateTopic = false;
			isCheck = false;
		}
		if(!this.isMod && isCheck){
			String[] strings = this.forum.getCreateTopicRole() ;
			boolean isEmpty = false;
			if(!ForumUtils.isArrayEmpty(strings)){
				canCreateTopic = ForumServiceUtils.hasPermission(strings, userName) ;
			}
			if(isEmpty || !canCreateTopic){
				strings = getForumService().getPermissionTopicByCategory(categoryId, "createTopicRole");
				if(!ForumUtils.isArrayEmpty(strings)){
					canCreateTopic = ForumServiceUtils.hasPermission(strings, userName) ;
				}
			}
		}
		return canCreateTopic;
	}
	
	public boolean getCanPost() throws Exception {
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
		if(isIPBaned(getRemoteIP())) return false;
		if(!topic.getIsActive() || !topic.getIsActiveByForum() || topic.getIsWaiting()) return false;
		try {
			List<String> listUser = new ArrayList<String>() ;
			listUser = ForumUtils.addArrayToList(listUser, topic.getCanPost());
			listUser = ForumUtils.addArrayToList(listUser, forum.getPoster());
			listUser = ForumUtils.addArrayToList(listUser, getForumService().getCategory(categoryId).getPoster());
			if(!listUser.isEmpty()) {
				listUser.add(topic.getOwner());
				return ForumServiceUtils.hasPermission(listUser.toArray(new String[listUser.size()]), userName);
			}
    } catch (Exception e) {
	    log.error("Check can reply is fall, exception: ", e);
    }
		return true ;
	}
	
	private String getRemoteIP() throws Exception {
		if(enableIPLogging) {
			return org.exoplatform.ks.common.Utils.getRemoteIP();
		}
		return "";
	}
	
	public Forum getForum() throws Exception {
		return this.forum ;
	}
	
	public String getIdPostView() {
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
	
	private boolean isModerator() {
		return isMod;
	}
	
	private String getScreenName(String userName, String screenName) throws Exception {
		return (userName.contains(Utils.DELETED)) ? "<s>"
				+ ((screenName.contains(Utils.DELETED)) ? 
						screenName.substring(0, screenName.indexOf(Utils.DELETED)) : screenName) + "</s>" : screenName;
	}
	 
	private Topic getTopic() throws Exception {
		try {
			if(this.isEditTopic || this.topic == null) {
				this.topic = getForumService().getTopic(categoryId, forumId, topicId, UserProfile.USER_GUEST) ;
				this.isEditTopic = false ;
			}
			return this.topic ;
		} catch (Exception e) {
		  log.warn("Failed to load topic: "+ e.getMessage(), e);
		}
		return null ;
	}
	
	public boolean userCanView() throws Exception {
		if(isMod) return true;
		else {
			if(forum.getIsClosed() || topic.getIsClosed() || !topic.getIsActive() || !topic.getIsActiveByForum() || topic.getIsWaiting()) return false;
		}
		if(getCanPost()) return true;
		List<String> listUser = new ArrayList<String>() ;
		
		listUser = ForumUtils.addArrayToList(listUser, topic.getCanView());
		listUser = ForumUtils.addArrayToList(listUser, forum.getViewer());
		listUser = ForumUtils.addArrayToList(listUser, getForumService().getPermissionTopicByCategory(categoryId, "viewer"));
		if(listUser.size() > 0) {
			listUser.add(topic.getOwner());
			return ForumServiceUtils.hasPermission(listUser.toArray(new String[listUser.size()]), userName);
		}
		return true;
	}
	
  public String getImageUrl(String imagePath) throws Exception {
  	String url = "";
  	try {
  		url = org.exoplatform.ks.common.Utils.getImageUrl(imagePath);
    } catch (Exception e) {
    	e.printStackTrace();
    }
    return url ;
  }

  public String getFileSource(ForumAttachment attachment) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		try {
			InputStream input = attachment.getInputStream() ;
			String fileName = attachment.getName() ;
			return ForumSessionUtils.getFileSource(input, fileName, dservice);
		} catch (PathNotFoundException e) {
		  log.warn("Failed get file source: "+ e.getMessage(), e);
			return null;
		}
	}

	public CommonContact getPersonalContact(String userId) throws Exception {
	  CommonContact contact ;
		if(mapContact.containsKey(userId) && listContactsGotten.contains(userId)){
			contact = mapContact.get(userId) ;
		} else {
			contact = ForumSessionUtils.getPersonalContact(userId) ;
			mapContact.put(userId, contact) ;
			listContactsGotten.add(userId);
		}
		return contact ;
	}
	
	public String getAvatarUrl(CommonContact contact, String userId) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		return ForumSessionUtils.getUserAvatarURL(userId, getForumService(), dservice);
	}


  public void initPage() throws Exception {
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
		  log.warn("Failed to init topic page: "+ e.getMessage(), e);
		}
	}
	
	private boolean getIsModeratePost(){return this.isModeratePost; }
	
	@SuppressWarnings("unchecked")
  public List<Post> getPostPageList() throws Exception {
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
      } catch (Exception e) {
        log.warn("Failed to find last read index for topic: "+ e.getMessage(), e);
      }
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
				  log.warn("Failed to load qui profiles: "+ e.getMessage(), e);
				}
			}
		} catch (Exception e) {
		  log.warn("Failed to load posts page: "+ e.getMessage(), e);
    }
		return posts ;
	}
	
	public List<Tag> getTagsByTopic() throws Exception {
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
      log.warn("Failed to load user tags in topic: "+ e.getMessage(), e);
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
	
	public UserProfile getUserInfo(String userName) throws Exception {
		if(!mapUserProfile.containsKey(userName)) {
			try{
				mapUserProfile.put(userName, getForumService().getQuickProfile(userName)) ;			
			}catch(Exception e){
			  log.warn("Failed load user info: "+ e.getMessage(), e);
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
	
	private void renderPoll() throws Exception {
		UITopicDetailContainer container = this.getParent();
		container.setRederPoll(false);
		((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(container) ;      
	}

  private void refreshPortlet() throws Exception {
    UIForumPortlet forumPortlet = getAncestorOfType(UIForumPortlet.class) ;
    UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
    categoryContainer.updateIsRender(true) ;
    forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
    forumPortlet.getChild(UIForumLinks.class).setUpdateForumLinks() ;
     ((WebuiRequestContext) WebuiRequestContext.getCurrentInstance()).addUIComponentToUpdateByAjax(forumPortlet) ;      
  }
  
	static public class AddPostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			try { 
				UIPostForm postForm = topicDetail.openPopup(UIPostForm.class, "UIAddPostContainer", 900, 460);
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost("", false, false, null) ;
				postForm.setMod(topicDetail.isMod) ;				
			} catch (Exception e) {
			  warning("UIForumPortlet.msg.topicEmpty");
			  topicDetail.refreshPortlet();
			}
		}
	}

	static public class RatingTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			try{
				String userName = UserHelper.getCurrentUser() ;
				String[] userVoteRating = topicDetail.topic.getUserVoteRating() ;
				boolean erro = false ;
				for (String string : userVoteRating) {
					if(string.equalsIgnoreCase(userName)) erro = true ; 
				}
				if(!erro) {				  
					UIRatingForm ratingForm = topicDetail.openPopup(UIRatingForm.class, 300, 145);
					ratingForm.updateRating(topicDetail.topic, topicDetail.categoryId, topicDetail.forumId) ;
					topicDetail.isEditTopic = true ;
				} else {
				  warning("UITopicDetail.sms.VotedRating", userName);
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}
	
	static public class AddTagTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			try {
				UIFormStringInput stringInput = topicDetail.getUIStringInput(FIELD_ADD_TAG);
				String tagIds = stringInput.getValue();
				if(!ForumUtils.isEmpty(tagIds)) {
					String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=*':}{\"";
					for (int i = 0; i < special.length(); i++) {
						char c = special.charAt(i);
						if(tagIds.indexOf(c) >= 0) {
						  warning("UITopicDetail.msg.failure");
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
		        topicDetail.log.error("Failed to add tag : ", e);
	        }
				} else {
				  warning("UITopicDetail.msg.empty-field");
					return ;
				}
				stringInput.setValue("");
				topicDetail.isEditTopic = true;
				refresh();
			} catch (Exception e) {
			  warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class UnTagTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String tagId) throws Exception {
			topicDetail.getForumService().unTag(tagId, topicDetail.userName, topicDetail.topic.getPath());
			topicDetail.isEditTopic = true;
			refresh();
		}
	}
	
	static public class OpenTopicsTagActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String tagId) throws Exception {
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.TAG) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption("") ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(tagId) ;
			forumPortlet.getChild(UITopicsTag.class).setIdTag(tagId) ;	
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	
	static public class SearchFormActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			String path = topicDetail.topic.getPath() ;
			UIFormStringInput formStringInput = topicDetail.getUIStringInput(ForumUtils.SEARCHFORM_ID) ;
			String text = formStringInput.getValue() ;
			if(!ForumUtils.isEmpty(text) && !ForumUtils.isEmpty(path)) {
				String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=|:\"'";
				for (int i = 0; i < special.length(); i++) {
					char c = special.charAt(i);
					if(text.indexOf(c) >= 0) {
					  warning("UIQuickSearchForm.msg.failure");
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
				topicDetail.refreshPortlet();
			} else {
			  throwWarning("UIQuickSearchForm.msg.checkEmpty");
			}
		}
	}

	static public class PrintActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
//			
		}
	}

	static public class GoNumberPageActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			int idbt = Integer.parseInt(objectId) ;
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
					  throwWarning("NameValidator.msg.Invalid-number", "go page");
					} else {
						if(page == 0) {
							page = 1;
						} else if(page > topicDetail.pageList.getAvailablePage()){
							page = topicDetail.pageList.getAvailablePage() ;
						}
						topicDetail.pageSelect = page ;
						refresh();
					}
				} catch (NumberFormatException e) {
				  throwWarning("NameValidator.msg.Invalid-number", "go page");
				}
			}
		}
	}

	static public class EditActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String postId) throws Exception {
			Post post = topicDetail.getPost(postId);
			if(post !=  null) {
				UIPostForm postForm = topicDetail.openPopup(UIPostForm.class, "UIEditPostContainer", 900, 460);
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost(postId, false, false, post) ;
			} else {
			  throwWarning("UIPostForm.msg.canNotEdit");
			}
		}
	}
	
	static public class DeleteActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String postId) throws Exception {
			try {
				topicDetail.getForumService().removePost(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, postId) ;
				topicDetail.IdPostView = "top";
			}catch (Exception e) {
			  topicDetail.log.warn("Failed to delete topic: " + e.getMessage(), e);
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
		}
	}
	
	static public class QuoteActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String postId) throws Exception {
			Post post = topicDetail.getPost(postId);
			if(post !=  null) {
				UIPostForm postForm = topicDetail.openPopup(UIPostForm.class, "UIQuoteContainer", 900, 500);			
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost(postId, true, false, post) ;
				postForm.setMod(topicDetail.isMod);
			} else {
			  throwWarning("UIPostForm.msg.isParentDelete");
			}
		}
	}

	static public class PrivatePostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String postId) throws Exception {
			Post post = topicDetail.getPost(postId);
			if(post !=  null) {
				UIPostForm postForm = topicDetail.openPopup(UIPostForm.class, "UIPrivatePostContainer", 900, 460);
				postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
				postForm.updatePost(postId, false, true, post) ;
				postForm.setMod(topicDetail.isMod) ;
			} else {
			  throwWarning("UIPostForm.msg.isParentDelete");
			}
		}
	}
//--------------------------------	 Topic Menu		-------------------------------------------//
	static public class EditTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			try{
				UITopicForm topicForm = openPopup(forumPortlet, UITopicForm.class, "UIEditTopicContainer", 900, 460);
				topicForm.setTopicIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.forum, topicDetail.userProfile.getUserRole()) ;
				topicForm.setUpdateTopic(topicDetail.getTopic(), true) ;
				topicForm.setMod(topicDetail.isMod) ;
				topicForm.setIsDetail(true);
				topicDetail.isEditTopic = true ;
			} catch (Exception e) {
			  topicDetail.log.warn("Error while editing topic: "+ e.getMessage(), e);
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class PrintPageActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
//			
		}
	}

	static public class AddPollActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {	
			try {
				Topic topic = topicDetail.topic ;
				UIPollForm  pollForm = topicDetail.openPopup(UIPollForm.class, 655, 455) ;
				pollForm.setTopicPath(topic.getPath()) ;
			} catch (Exception e) {
			  e.printStackTrace();
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetOpenTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
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
				  throwWarning("UITopicContainer.sms.Open", topic.getTopicName());
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetCloseTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
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
					warning("UITopicContainer.sms.Close", topic.getTopicName());
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetLockedTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
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
					warning("UITopicContainer.sms.Locked", topic.getTopicName());
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");			
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetUnLockTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
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
				  warning("UITopicContainer.sms.UnLock", topic.getTopicName() );
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetMoveTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
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
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}
	
	static public class SetStickTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			try{
				Topic topic = topicDetail.topic ;
				if(!topic.getIsSticky()) {
					topic.setIsSticky(true) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 4) ;
					topicDetail.isEditTopic = true ;
					refresh();
				} else {
					warning("UITopicContainer.sms.Stick", topic.getTopicName() );
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetUnStickTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			try{
				Topic topic = topicDetail.topic ;
				if(topic.getIsSticky()) {
					topic.setIsSticky(false) ;
					List<Topic>topics = new ArrayList<Topic>();
					topics.add(topic);
					topicDetail.getForumService().modifyTopic(topics, 4) ;
					topicDetail.isEditTopic = true ;
					refresh();
				} else {
				  warning("UITopicContainer.sms.UnStick", topic.getTopicName());
				}
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SplitTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			try{
				JCRPageList pageList = topicDetail.getForumService().getPostForSplitTopic(topicDetail.categoryId+"/"+topicDetail.forumId +"/"+topicDetail.topicId);
				if(pageList.getAvailable() > 0) {
					UISplitTopicForm splitTopicForm = topicDetail.openPopup(UISplitTopicForm.class, 700, 400);
					splitTopicForm.setPageListPost(pageList) ;
					splitTopicForm.setTopic(topicDetail.topic) ;
					splitTopicForm.setUserProfile(topicDetail.userProfile) ;
				} else {
				  warning("UITopicContainer.sms.NotSplit");
				}
			} catch (Exception e) {
			  topicDetail.log.warn("Failed to split topic: "+ e.getMessage(), e);
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetApproveTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			try {
				Topic topic = topicDetail.topic;
				topic.setIsApproved(true) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				topicDetail.getForumService().modifyTopic(topics, 3) ;
				topicDetail.isEditTopic = true ;
				refresh();
			} catch (Exception e) {
			  warning("UIForumPortlet.msg.topicEmpty");
			  topicDetail.refreshPortlet();
			}
		}
	}
	
	static public class SetUnApproveTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {	
			try{
				Topic topic = topicDetail.topic;
				topic.setIsApproved(false) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				topicDetail.getForumService().modifyTopic(topics, 3) ;
				topicDetail.isEditTopic = true ;
				refresh();
			} catch (Exception e) {
			  warning("UIForumPortlet.msg.topicEmpty");
			  topicDetail.refreshPortlet();
			}
		}
	}

	static public class SetDeleteTopicActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
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
				forumPortlet.updateUserProfileInfo();
			} catch (Exception e) {
			  warning("UIForumPortlet.msg.topicEmpty");
			  topicDetail.refreshPortlet();
			}
		}
	}

	//---------------------------------	Post Menu	 --------------------------------------//
	static public class MergePostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
		}
	}

	static public class DownloadAttachActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			refresh();
		}
	}

	static public class MovePostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				posts.add(topicDetail.getPost(postId));
      }
			if(posts.size() > 0) {
				UIMovePostForm movePostForm = topicDetail.openPopup(UIMovePostForm.class, 400, 430);
				movePostForm.setUserProfile(topicDetail.userProfile) ;
				movePostForm.updatePost(topicDetail.topicId, posts);
			} else {
			  throwWarning("UITopicDetail.msg.notCheckPost");
			}
		}
	}

	static public class SetApprovePostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				posts.add(topicDetail.getPost(postId));
      }
			if(posts.isEmpty()){
				UIPageListPostUnApprove postUnApprove = topicDetail.openPopup(UIPageListPostUnApprove.class, 500, 360);
				postUnApprove.setUpdateContainer(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
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
					  topicDetail.log.warn("Failed to modify: "+ e.getMessage(), e);
					}
					refresh();
				}
			}
		}
	}
	
	static public class SetHiddenPostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			List<String> postIds = topicDetail.getIdSelected(); 
			if(postIds == null || postIds.isEmpty()){
			  throwWarning("UITopicDetail.msg.notCheckPost");
			}
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				Post post = topicDetail.getPost(postId);
				if(post != null && !post.getIsHidden()){
					post.setIsHidden(true) ;
					posts.add(post) ;
				}
      }
			try {
				topicDetail.getForumService().modifyPost(posts, 2) ;
			} catch (Exception e) {
			  topicDetail.log.warn("Failed to modify post: "+ e.getMessage(), e);
			}
			refresh();
		}
	}

	static public class SetUnHiddenPostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				Post post = topicDetail.getPost(postId);
				if(post != null) {
					posts.add(post);
				}
      }
			if(posts.isEmpty()){			  
			  UIPageListPostHidden listPostHidden = topicDetail.openPopup(UIPageListPostHidden.class, 500, 360);
        listPostHidden.setUpdateContainer(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId) ;
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
					  topicDetail.log.warn("Failed to modify post: "+ e.getMessage(), e);
					}
					refresh();
				}
			}
		}
	}
	
	static public class SetUnApproveAttachmentActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
		}
	}
	
	static public class DeletePostActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				posts.add(topicDetail.getPost(postId));
      }
			for(Post post : posts) {
				try {
					topicDetail.getForumService().removePost(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post.getId()) ;
				} catch(Exception e){
				  topicDetail.log.warn("Failed to remove post: "+ e.getMessage(), e);
				}
				refresh();
			}
		}
	}

	static public class ViewPublicUserInfoActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String userId) throws Exception {
			UIViewUserProfile viewUserProfile = topicDetail.openPopup(UIViewUserProfile.class, 670, 400);
			try {
				UserProfile selectProfile = topicDetail.getForumService().getUserInformations(topicDetail.mapUserProfile.get(userId)) ;
				viewUserProfile.setUserProfile(selectProfile) ;
			} catch(Exception e) {
			  topicDetail.log.warn("Failed to get User info: "+ e.getMessage(), e);
			}
			viewUserProfile.setUserProfileLogin(topicDetail.userProfile) ;
			CommonContact contact = null ;
			if(topicDetail.mapContact.containsKey(userId)) {
				contact = topicDetail.mapContact.get(userId) ;
			}
			viewUserProfile.setContact(contact) ;
		}
	}
	
	static public class PrivateMessageActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String userId) throws Exception {
			if(topicDetail.userProfile.getIsBanned()){
			  throwWarning("UITopicDetail.msg.userIsBannedCanNotSendMail");
			}
			int t = userId.indexOf(Utils.DELETED);
			if(t > 0) {
				String[] args = new String[] { userId.substring(0, t)} ;
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.userIsDeleted", args, ApplicationMessage.WARNING)) ;
			}
			UIPrivateMessageForm messageForm = topicDetail.openPopup(UIPrivateMessageForm.class, 650, 480);
	    messageForm.setFullMessage(false);
	    messageForm.setUserProfile(topicDetail.userProfile);
	    messageForm.setSendtoField(userId) ;
		}
	}
	
	static public class ViewPostedByUserActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			String userId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIViewPostedByUser viewPostedByUser = topicDetail.openPopup(UIViewPostedByUser.class, 760, 370);
			viewPostedByUser.setUserProfile(userId) ;
		}
	}
	
	static public class ViewThreadByUserActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String userId) throws Exception {
			UIViewTopicCreatedByUser topicCreatedByUser = topicDetail.openPopup(UIViewTopicCreatedByUser.class, 760, 450);
			topicCreatedByUser.setUserId(userId) ;			
		}
	}
	
  private boolean checkForumHasAddTopic(UserProfile userProfile) throws Exception {
    this.topic = (Topic) getForumService().getObjectNameById(this.topicId, Utils.TOPIC);
    if (topic.getIsClosed() || topic.getIsLock())
      return false;
    Forum forum = (Forum) getForumService().getObjectNameById(this.forumId, Utils.FORUM);
    if (forum.getIsClosed() || forum.getIsLock())
      return false;
    if (userProfile.getUserRole() > 1
        || (userProfile.getUserRole() == 1 && !ForumServiceUtils.hasPermission(forum.getModerators(),
                                                                               userProfile.getUserId()))) {
      if (!topic.getIsActive() || !topic.getIsActiveByForum())
        return false;
      String[] canCreadPost = topic.getCanPost();
      if (!ForumUtils.isArrayEmpty(canCreadPost)) {
        return ForumServiceUtils.hasPermission(canCreadPost, userProfile.getUserId());
      }
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
		return new String[0];
	}
	
	static public class QuickReplyActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			if(topicDetail.isDoubleClickQuickReply) return;
			topicDetail.isDoubleClickQuickReply = true;
			try {
  			UIFormTextAreaInput textAreaInput = topicDetail.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA) ;
  			String message = "";
  			try {
  				message = textAreaInput.getValue();
        } catch (Exception e) {
          topicDetail.log.warn("Failed read quick reply: "+ e.getMessage(), e);
        }
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
	  				String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, topicDetail.topicId).replaceFirst("private", "public");				
	  				//
	  				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class);
	  				String userName = topicDetail.userProfile.getUserId() ;
	  				Topic topic = topicDetail.topic ;
	  				Post post = new Post() ;
	  				post.setName("Re: " + topic.getTopicName()) ;
	  				post.setMessage(buffer.toString()) ;
	  				post.setOwner(userName) ;
	  				post.setRemoteAddr(topicDetail.getRemoteIP()) ;
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
	  				  throwWarning("UIPostForm.msg.isParentDelete");
	  				} catch (Exception e) {
	  				  topicDetail.log.warn("Failed to save post: "+ e.getMessage(), e);
	  				}
	  				textAreaInput.setValue("") ;
	  				if(isOffend || hasTopicMod) {
	  					if(isOffend) { 
	  					  warning("MessagePost.msg.isOffend", "");
	  					} else {
	  					  warning("MessagePost.msg.isModerate", "");
	  					}
	  					topicDetail.IdPostView = "normal";
	  				} else {
	  					topicDetail.IdPostView = "lastpost";
	  				}	  				
	  			} else {
	  				warning("UIPostForm.msg.no-permission");	  				
	  			}
  				refresh();
  			} else {
  				warning("MessagePost.msg.message-empty", getLabel(FIELD_MESSAGE_TEXTAREA));
  				topicDetail.isDoubleClickQuickReply = false;
  			}
      } catch (Exception e) {
  			warning("UIPostForm.msg.isParentDelete", "");
  			refresh();
      }
		}
	}
	
	static public class PreviewReplyActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
				
			String message = topicDetail.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
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
				
				UIViewPost viewPost = topicDetail.openPopup(UIViewPost.class, 670, 0);
				
				viewPost.setPostView(post) ;
				viewPost.setViewUserInfo(false) ;
				viewPost.setActionForm(new String[] {"Close"});

			} else {
			  warning("MessagePost.msg.message-empty", getLabel(FIELD_MESSAGE_TEXTAREA));
			}
		}
	}
	
	static public class WatchOptionActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
		  UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			Topic topic = topicDetail.topic ;
			UIWatchToolsForm watchToolsForm = openPopup(forumPortlet, UIWatchToolsForm.class, 500, 365);
	    watchToolsForm.setPath(topic.getPath());
	    watchToolsForm.setEmails(topic.getEmailNotification()) ;
	    watchToolsForm.setIsTopic(true);
		}
	}	
	
	static	public class AdvancedSearchActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(ForumUtils.FIELD_SEARCHFORUM_LABEL) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(ForumUtils.FIELD_EXOFORUM_LABEL) ;
			UISearchForm searchForm = forumPortlet.getChild(UISearchForm.class) ;
			searchForm.setUserProfile(forumPortlet.getUserProfile()) ;
			searchForm.setSelectType(Utils.POST) ;
			searchForm.setPath(topicDetail.topic.getPath());
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class BanIPAllForumActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String ip) throws Exception {
			if(!topicDetail.getForumService().addBanIP(ip)){
				warning("UIBanIPForumManagerForm.sms.ipBanFalse", ip);
				return;
			}
			refresh();
		}
	}

	static	public class BanIPThisForumActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String ip) throws Exception {
			List<String> listIp = topicDetail.forum.getBanIP();
			if(listIp == null || listIp.size()  == 0) listIp = new ArrayList<String>();
			listIp.add(ip);topicDetail.forum.setBanIP(listIp);
			if(!topicDetail.getForumService().addBanIPForum(ip, (topicDetail.categoryId + "/" + topicDetail.forumId))){
				warning("UIBanIPForumManagerForm.sms.ipBanFalse", ip);
				return;
			}
			refresh();
		}
	}
	
	static public class AddBookMarkActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
			try{
				Topic topic = topicDetail.getTopic();
				StringBuffer buffer = new StringBuffer();
				buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//").append(topic.getId()) ;
				String userName = topicDetail.userProfile.getUserId() ;
				topicDetail.getForumService().saveUserBookmark(userName, buffer.toString(), true) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateUserProfileInfo() ;
			} catch (Exception e) {
				warning("UIForumPortlet.msg.topicEmpty");
        topicDetail.refreshPortlet();
			}
		}
	}

	static public class AddWatchingActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			
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
					topicDetail.info("UIAddWatchingForm.msg.successfully");
					
					refresh();
				} catch (Exception e) {
				  topicDetail.log.warn("Failed to add watch: "+ e.getMessage(), e);
				  warning("UIAddWatchingForm.msg.fall");
				}
			} else {
				warning("UIForumPortlet.msg.topicEmpty");
        topicDetail.refreshPortlet();
			}
		}
	}
	

	static public class UnWatchActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String objectId) throws Exception {
			if(topicDetail.getTopic() != null) {
				topicDetail.isEditTopic = true;
				String path =  topicDetail.categoryId+"/"+topicDetail.forumId+"/"+topicDetail.topicId;
				try {
					topicDetail.getForumService().removeWatch(1, path, topicDetail.userProfile.getUserId()+"/"+topicDetail.getEmailWatching(path)) ;
					UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.updateWatchinh();
					topicDetail.listWatches = forumPortlet.getWatchinhByCurrentUser();
					topicDetail.info("UIAddWatchingForm.msg.UnWatchSuccessfully");
					
				} catch (Exception e) {
				  topicDetail.log.warn("Failed to unwatch: "+ e.getMessage(), e);
					warning("UIAddWatchingForm.msg.UnWatchfall");
				}
				refresh();
			} else {
				warning("UIForumPortlet.msg.topicEmpty");
				topicDetail.refreshPortlet();
			}
		}
	}
	

	static public class RSSActionListener extends BaseEventListener<UITopicDetail> {
		public void onEvent(Event<UITopicDetail> event, UITopicDetail topicDetail, final String topicId) throws Exception {
			if(!topicDetail.getUserProfile().getUserId().equals(UserProfile.USER_GUEST)){
			  topicDetail.getForumService().addWatch(-1, topicId, null, topicDetail.userName);
			}
		}
	}

  public String renderPost(Post post) throws RenderingException {
    return renderHelper.renderPost(post);
  }


}
