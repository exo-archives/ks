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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumSearch;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.service.user.ForumContact;
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
import org.exoplatform.forum.webui.popup.UITagForm;
import org.exoplatform.forum.webui.popup.UITopicForm;
import org.exoplatform.forum.webui.popup.UIViewPost;
import org.exoplatform.forum.webui.popup.UIViewPostedByUser;
import org.exoplatform.forum.webui.popup.UIViewTopicCreatedByUser;
import org.exoplatform.forum.webui.popup.UIViewUserProfile;
import org.exoplatform.forum.webui.popup.UIWatchToolsForm;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.portletcontainer.plugins.pc.portletAPIImp.PortletRequestImp;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
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
			@EventConfig(listeners = UITopicDetail.AddWatchingActionListener.class)
		}
)
public class UITopicDetail extends UIForumKeepStickPageIterator {
	private ForumService forumService ;
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
	private Map<String, UserProfile> mapUserProfile = new HashMap<String, UserProfile>();
	private Map<String, ForumContact> mapContact = new HashMap<String, ForumContact>();
	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	public UITopicDetail() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_T, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_B, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		addUIFormInput( new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA,null)) ;
		addChild(UIPostRules.class, null, null);
		this.setSubmitAction("GoNumberPage") ;
		this.setActions(new String[]{"PreviewReply","QuickReply"} );
	}
	
	public UserProfile getUserProfile() {
		return userProfile ;
	}
	
	public boolean getHasEnableIPLogging() {
	  return enableIPLogging;
  }
	
  private boolean isIPBaned(String ip){
  	List<String> ipBaneds = forum.getBanIP();
		if(ipBaneds != null && ipBaneds.size() > 0 && ipBaneds.contains(ip)) return true;
		return false;
	}
	@SuppressWarnings("unused")
	private boolean isOnline(String userId) throws Exception {
		return this.forumService.isOnline(userId) ;
	}
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	
	public void setUpdateTopic(String categoryId, String forumId, String topicId) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topicId ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		enableIPLogging = forumPortlet.isEnableIPLogging();
		forumPortlet.updateAccessTopic(topicId);
		userProfile = forumPortlet.getUserProfile() ;
		userName = userProfile.getUserId() ;
		cleanCheckedList();
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId, userName) ;
	}
	
	public void setTopicFromCate(String categoryId, String forumId, Topic topic) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		enableIPLogging = forumPortlet.isEnableIPLogging();
		cleanCheckedList();
		this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId(), userName) ;
		forumPortlet.updateAccessTopic(topicId);
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		userProfile = forumPortlet.getUserProfile() ;
		userName = userProfile.getUserId() ;
	}
	
	public void hasPoll(boolean hasPoll) {
		this.topic.setIsPoll(hasPoll);
	}
	
	public void setUpdateContainer(String categoryId, String forumId, Topic topic, long numberPage) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		this.pageSelect = numberPage ;
		this.isEditTopic = false ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		enableIPLogging = forumPortlet.isEnableIPLogging();
		cleanCheckedList();
		this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId(), userName) ;
		forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		userProfile = forumPortlet.getUserProfile() ;
		userName = userProfile.getUserId() ;
	}
	
	public void setUpdateForum(Forum forum) throws Exception {
		this.forum = forum ;
	}
	
	@SuppressWarnings("unused")
	private boolean isCanPostReply() throws Exception {
		if(userProfile.getUserRole() == 3) return false;
		if(forum.getIsClosed() || forum.getIsLock() || topic.getIsClosed() || topic.getIsLock()) return false;
		if(userProfile.getIsBanned()) return false;
		if(isMod) return true;
		if(isIPBaned(getIPRemoter())) return false;
		if(!topic.getIsActive() || !topic.getIsActiveByForum() || topic.getIsWaiting()) return false;
		List<String> listUser = new ArrayList<String>() ;
		String [] canPost = this.topic.getCanPost();
		if(canPost != null && canPost.length > 0 && !canPost[0].equals(" ")){
			listUser.addAll(Arrays.asList(canPost));
			canPost = this.forum.getPoster();
			if(canPost != null && canPost.length > 0 && !canPost[0].equals(" ")){
				listUser.addAll(Arrays.asList(canPost));
			}
		}
		if(listUser.size() > 0) {
			return ForumServiceUtils.hasPermission(listUser.toArray(new String[]{}), userName);
		}
		return true ;
	}
	
	private String getIPRemoter() throws Exception {
		if(enableIPLogging) {
			WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
			PortletRequestImp request = context.getRequest() ;
			return request.getRemoteAddr();
		}
		return "";
	}
	
	@SuppressWarnings("unused")
	private Forum getForum() throws Exception {
		return this.forum ;
	}
	
	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	private Topic getTopic() throws Exception {
		SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
		try {
			if(this.isEditTopic || this.topic == null) {
				this.topic = forumService.getTopic(sProvider, categoryId, forumId, topicId, UserProfile.USER_GUEST) ;
				this.isEditTopic = false ;
			}
			return this.topic ;
		} catch (Exception e) {
			sProvider.close();
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
	private boolean userCanView() throws Exception {
		if(isMod) return true;
		else {
			if(forum.getIsClosed() || topic.getIsClosed() || !topic.getIsActive() || !topic.getIsActiveByForum() || topic.getIsWaiting()) return false;
		}
		if(isCanPostReply()) return true;
		List<String> listUser = new ArrayList<String>() ;
		String [] canPost = topic.getCanView();
		if(canPost != null && canPost.length > 0 && !canPost[0].equals(" ")){
			listUser.addAll(Arrays.asList(canPost));
			canPost = this.forum.getViewer();
			if(canPost != null && canPost.length > 0 && !canPost[0].equals(" ")){
				listUser.addAll(Arrays.asList(canPost));
			}
		}
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
	@SuppressWarnings("unused")
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

		@SuppressWarnings("unused")
	private ForumContact getPersonalContact(String userId) throws Exception {
			ForumContact contact ;
		if(mapContact.containsKey(userId)){
			contact = mapContact.get(userId) ;
		} else {
			contact = ForumSessionUtils.getPersonalContact(userId) ;
			mapContact.put(userId, contact) ;
		}
		if(contact == null) {
			contact = new ForumContact() ;
		}
		return contact ;
	}
	
	@SuppressWarnings("unused")
	private String getAvatarUrl(ForumContact contact, String userId, SessionProvider sessionProvider) throws Exception {
//		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
//		try {
//			ContactAttachment attachment = contact.getAttachment() ; 
//			InputStream input = attachment.getInputStream() ;
//			String fileName = attachment.getFileName() ;
//			return ForumSessionUtils.getFileSource(input, fileName, dservice);
//		} catch (NullPointerException e) {
//			return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
//		}
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		return ForumSessionUtils.getUserAvatarURL(userId, this.forumService, dservice);
	}

	@SuppressWarnings("unused")
	private void initPage() throws Exception {
		isMod = false;
		if(this.userProfile.getUserRole() == 0) isMod = true;
		if(!isMod) isMod = ForumServiceUtils.hasPermission(forum.getModerators(), userName) ;
		SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
		try {
				String isApprove = "";
				String isHidden = "";
				if(!isMod) isHidden = "false"; 
				if (this.forum.getIsModeratePost() || this.topic.getIsModeratePost()) {
					isModeratePost = true;
					if (!isMod && !(this.topic.getOwner().equals(userName)))
						isApprove = "true";
				}
				pageList = this.forumService.getPosts(sProvider, this.categoryId, this.forumId, topicId, isApprove, isHidden, "", userName);
			long maxPost = this.userProfile.getMaxPostInPage();
			if (maxPost <= 0) maxPost = 10;
			pageList.setPageSize(maxPost);
			maxPage = pageList.getAvailablePage();
			if (IdPostView.equals("lastpost")) {
				this.pageSelect = maxPage;
			}
		} catch (Exception e) {
			sProvider.close();
		}
	}
	
	@SuppressWarnings("unused")
	private boolean getIsModeratePost(){return this.isModeratePost; }
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Post> getPostPageList() throws Exception {
		List<Post> posts = new ArrayList<Post>();  
		if(this.pageList == null) return posts ;
		posts = this.pageList.getPage(this.pageSelect) ;
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
		//updateUserProfiles
		if(userNames.size() > 0) {
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try{
				List<UserProfile> profiles = forumService.getQuickProfiles(sProvider, userNames) ;
				for(UserProfile profile : profiles) {
					mapUserProfile.put(profile.getUserId(), profile) ;
				}
			}catch(Exception e) {
				e.printStackTrace() ;
			}finally {
				sProvider.close() ;
			}
		}
		return posts ;
	}
	
	@SuppressWarnings("unchecked")
	public List<Post> getAllPost() throws Exception {return this.pageList.getPage(0) ;}
	
	private Post getPost(String postId) throws Exception {
		for(Post post : getAllPost()) {
			if(post.getId().equals(postId)) return post;
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
	public void setPostRules(boolean isNull) throws Exception {
		UIPostRules postRules = getChild(UIPostRules.class); 
		postRules.setUserProfile(this.userProfile) ;
		if(!isNull) {
			if(this.forum.getIsClosed() || this.forum.getIsLock()){
				postRules.setCanCreateNewThread(false);
				postRules.setCanAddPost(false);
			} else {
				/**
				 * set permission for create new thread
				 */
				String[] strings = this.forum.getCreateTopicRole();
				boolean canCreateThread = this.isMod;
				if(!canCreateThread){ 
					if(isIPBaned(getIPRemoter())) canCreateThread = false;
					else {
						if(strings == null || strings.length == 0 || (strings.length == 1 && strings[0].equals(" "))) canCreateThread = true;
						else canCreateThread = ForumServiceUtils.hasPermission(strings, userName);
					}
				}
				postRules.setCanCreateNewThread(canCreateThread);
				/**
				 * set permission for post reply
				 */
				if(this.topic != null && !this.topic.getIsClosed() && !this.topic.getIsLock()){
					postRules.setCanAddPost(isCanPostReply());
				} else {
					postRules.setCanAddPost(false);
				}
			}
		} else {
			postRules.setCanCreateNewThread(!isNull);
			postRules.setCanAddPost(!isNull);
		}
	}
	
	@SuppressWarnings("unused")
	private UserProfile getUserInfo(String userName) throws Exception {
		if(!mapUserProfile.containsKey(userName)) {
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try{
				mapUserProfile.put(userName, forumService.getQuickProfile(sProvider, userName)) ;			
			}catch(Exception e){
				e.printStackTrace() ;
			}finally{
				sProvider.close() ;
			}
		}
		return mapUserProfile.get(userName) ;
	}
	
	static public class AddPostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost("", false, false, null) ;
			postForm.setMod(topicDetail.isMod) ;
			
			popupContainer.setId("UIAddPostContainer") ;
			popupAction.activate(popupContainer, 900, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class RatingTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String userName = ForumSessionUtils.getCurrentUser() ;
			String[] userVoteRating = topicDetail.topic.getUserVoteRating() ;
			boolean erro = false ;
			for (String string : userVoteRating) {
				if(string.equalsIgnoreCase(userName)) erro = true ; 
			}
			if(!erro) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UIRatingForm ratingForm = popupAction.createUIComponent(UIRatingForm.class, null, null) ;
				ratingForm.updateRating(topicDetail.topic, topicDetail.categoryId, topicDetail.forumId) ;
				popupAction.activate(ratingForm, 300, 145) ;
				
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
				topicDetail.isEditTopic = true ;
			} else {
				Object[] args = { userName };
				throw new MessageException(new ApplicationMessage("UITopicDetail.sms.VotedRating", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
	
	static public class AddTagTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, "UITagFormContainer") ;
			UITagForm tagForm = popupContainer.addChild(UITagForm.class, null, null) ;
			tagForm.setTopicPathAndTagId(topicDetail.topic.getPath(), topicDetail.topic.getTagId()) ;
			popupAction.activate(popupContainer, 240, 280) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class SearchFormActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String path = topicDetail.topic.getPath() ;
			UIFormStringInput formStringInput = topicDetail.getUIStringInput(ForumUtils.SEARCHFORM_ID) ;
			String text = formStringInput.getValue() ;
			if(!ForumUtils.isEmpty(text) && !ForumUtils.isEmpty(path)) {
				String special = "\\,.?!`~/][)(;#@$%^&*<>-_+=";
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
					topicDetail.forumService.getQuickSearch(ForumSessionUtils.getSystemProvider(), text, type.toString(), path, 
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
					long page = Long.parseLong(numberPage.trim()) ;
					if(page < 0) {
						Object[] args = { "go page" };
						throw new MessageException(new ApplicationMessage("NameValidator.msg.Invalid-number", args, ApplicationMessage.WARNING)) ;
					} else {
						if(page == 0) {
							page = (long)1;
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
				throw new MessageException(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
			}
		}
	}
	
	static public class DeleteActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				topicDetail.forumService.removePost(sProvider, topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, postId) ;
				topicDetail.IdPostView = "top";
			} finally {
				sProvider.close();
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
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UITopicForm topicForm = popupContainer.addChild(UITopicForm.class, null, null) ;
			topicForm.setTopicIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.forum) ;
			topicForm.setUpdateTopic(topicDetail.topic, true) ;
			topicForm.setMod(topicDetail.isMod) ;
			popupContainer.setId("UIEditTopicContainer") ;
			popupAction.activate(popupContainer, 900, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			topicDetail.isEditTopic = true ;
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
			Topic topic = topicDetail.topic ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPollForm	pollForm = popupAction.createUIComponent(UIPollForm.class, null, null) ;
			pollForm.setTopicPath(topic.getPath()) ;
			popupAction.activate(pollForm, 655, 455) ;
		}
	}

	static public class SetOpenTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(topic.getIsClosed()) {
				topic.setIsClosed(false) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					topicDetail.forumService.modifyTopic(sProvider, topics, 1) ;
					topicDetail.isEditTopic = true ;
				} finally {
					sProvider.close();
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Open", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetCloseTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(!topic.getIsClosed()) {
				topic.setIsClosed(true) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					topicDetail.forumService.modifyTopic(sProvider, topics, 1) ;
					topicDetail.isEditTopic = true ;
				} finally {
					sProvider.close();
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Close", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetLockedTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(!topic.getIsLock()) {
				topic.setIsLock(true) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					topicDetail.forumService.modifyTopic(sProvider, topics, 2) ;
					topicDetail.isEditTopic = true ;
				} finally {
					sProvider.close();
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Locked", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetUnLockTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(topic.getIsLock()) {
				topic.setIsLock(false) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					topicDetail.forumService.modifyTopic(sProvider, topics, 2) ;
					topicDetail.isEditTopic = true ;
				} finally {
					sProvider.close();
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnLock", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetMoveTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIMoveTopicForm moveTopicForm = popupAction.createUIComponent(UIMoveTopicForm.class, null, null) ;
			moveTopicForm.setUserProfile(topicDetail.userProfile) ;
			List <Topic> topics = new ArrayList<Topic>();
			topics.add(topicDetail.topic) ;
			topicDetail.isEditTopic = true ;
			moveTopicForm.updateTopic(topicDetail.forumId, topics, true);
			popupAction.activate(moveTopicForm, 400, 420) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class SetStickTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(!topic.getIsSticky()) {
				topic.setIsSticky(true) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					topicDetail.forumService.modifyTopic(sProvider, topics, 4) ;
					topicDetail.isEditTopic = true ;
				} finally {
					sProvider.close();
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.Stick", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetUnStickTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			if(topic.getIsSticky()) {
				topic.setIsSticky(false) ;
				List<Topic>topics = new ArrayList<Topic>();
				topics.add(topic);
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					topicDetail.forumService.modifyTopic(sProvider, topics, 4) ;
					topicDetail.isEditTopic = true ;
				} finally {
					sProvider.close();
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
			} else {
				Object[] args = { topic.getTopicName() };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.UnStick", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SplitTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<Post>list = topicDetail.getAllPost() ;
			if(list.size() > 1) {
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
				UISplitTopicForm splitTopicForm = popupAction.createUIComponent(UISplitTopicForm.class, null, null) ;
				splitTopicForm.setListPost(list) ;
				splitTopicForm.setTopic(topicDetail.topic) ;
				splitTopicForm.setUserProfile(topicDetail.userProfile) ;
				popupAction.activate(splitTopicForm, 700, 400) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			} else {
				Object[] args = { };
				throw new MessageException(new ApplicationMessage("UITopicContainer.sms.NotSplit", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetApproveTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic;
			topic.setIsApproved(true) ;
			List<Topic>topics = new ArrayList<Topic>();
			topics.add(topic);
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				topicDetail.forumService.modifyTopic(sProvider, topics, 3) ;
				topicDetail.isEditTopic = true ;
			} finally {
				sProvider.close();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}
	
	static public class SetUnApproveTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic;
			topic.setIsApproved(false) ;
			List<Topic>topics = new ArrayList<Topic>();
			topics.add(topic);
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				topicDetail.forumService.modifyTopic(sProvider, topics, 3) ;
				topicDetail.isEditTopic = true ;
			} finally {
				sProvider.close();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}

	static public class SetDeleteTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				topicDetail.forumService.removeTopic(sProvider, topicDetail.categoryId, topicDetail.forumId, topic.getId()) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
				uiForumContainer.setIsRenderChild(true) ;
				UITopicContainer topicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
				topicContainer.setUpdateForum(topicDetail.categoryId, topicDetail.forum) ;
				UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiForumContainer) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
			} finally {
				sProvider.close();
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
		@SuppressWarnings("unchecked")
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
					SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
					try {
						topicDetail.forumService.modifyPost(sProvider, posts, 1) ;
					} finally {
						sProvider.close();
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
				}
			}
		}
	}
	
	static public class SetHiddenPostActionListener extends EventListener<UITopicDetail> {
		@SuppressWarnings("unchecked")
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
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				topicDetail.forumService.modifyPost(sProvider, posts, 2) ;
			} finally {
				sProvider.close();
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
					SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
					try {
						topicDetail.forumService.modifyPost(sProvider, posts, 2) ;
					} finally {
						sProvider.close();
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
		@SuppressWarnings("unchecked")
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<String> postIds = topicDetail.getIdSelected(); 
			List<Post> posts = new ArrayList<Post>();
			for (String postId : postIds) {
				posts.add(topicDetail.getPost(postId));
      }
			for(Post post : posts) {
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					topicDetail.forumService.removePost(sProvider, topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post.getId()) ;
				} finally {
					sProvider.close();
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
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try{
				UserProfile selectProfile = topicDetail.forumService.getUserInformations(sProvider, topicDetail.mapUserProfile.get(userId)) ;
				viewUserProfile.setUserProfile(selectProfile) ;
			}catch(Exception e) {
				e.printStackTrace() ;
			}finally{
				sProvider.close() ;
			}
			
			viewUserProfile.setUserProfileLogin(topicDetail.userProfile) ;
			ForumContact contact = null ;
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
			@SuppressWarnings("unused")
			UIViewPostedByUser viewPostedByUser = popupContainer.addChild(UIViewPostedByUser.class, null, null) ;
			viewPostedByUser.setUserProfile(userId) ;
			popupContainer.setId("ViewPostedByUser") ;
			popupAction.activate(popupContainer, 760, 350) ;
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
			@SuppressWarnings("unused")
			UIViewTopicCreatedByUser topicCreatedByUser = popupContainer.addChild(UIViewTopicCreatedByUser.class, null, null) ;
			topicCreatedByUser.setUserId(userId) ;
			popupContainer.setId("ViewTopicCreatedByUser") ;
			popupAction.activate(popupContainer, 760, 350) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	private String[] getCensoredKeyword(SessionProvider sProvider) throws Exception {
		ForumAdministration forumAdministration = forumService.getForumAdministration(sProvider) ;
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
			UIFormTextAreaInput textAreaInput = topicDetail.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA) ;
			String message = textAreaInput.getValue() ;
			String checksms = message ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			if(checksms != null && checksms.trim().length() > 0) {
				boolean isOffend = false ;
				boolean hasTopicMod = false ;
				if(!topicDetail.isMod) {
					String []censoredKeyword = topicDetail.getCensoredKeyword(sProvider);
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
					} else{
						buffer.append(c) ;
					}
				} 
				
			// set link
				PortalRequestContext portalContext = Util.getPortalRequestContext();
				String url = portalContext.getRequest().getRequestURL().toString();
				url = "http://" + portalContext.getRequest().getServerName() + ":"+ portalContext.getRequest().getServerPort();
				String link = topicDetail.getLink();
				link = ForumSessionUtils.getBreadcumbUrl(link, topicDetail.getId(), "ViewThreadByUser");				
				link = link.replaceFirst("pathId", topicDetail.topicId) ;
				link = url + link;
				link = link.replaceFirst("private", "public");
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
					topicDetail.forumService.savePost(sProvider, topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post, true, ForumUtils.getDefaultMail()) ;
					long postCount = topicDetail.getUserInfo(userName).getTotalPost() + 1 ;
					topicDetail.getUserInfo(userName).setTotalPost(postCount);
					topicDetail.getUserInfo(userName).setLastPostDate(ForumUtils.getInstanceTempCalendar().getTime()) ;
					topicDetail.forumService.updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  topic.getId()) ;
					forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
					if(topicDetail.userProfile.getIsAutoWatchTopicIPost()) {
						List<String> values = new ArrayList<String>();
						values.add(topicDetail.userProfile.getEmail());
						String path = topicDetail.categoryId + "/" + topicDetail.forumId + "/" + topicDetail.topicId;
						topicDetail.forumService.addWatch(sProvider, 1, path, values, topicDetail.userProfile.getUserId()) ;
					}
				} catch (PathNotFoundException e) {
					String[] args = new String[] { } ;
					throw new MessageException(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
				} finally {
					sProvider.close();
				}
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
				String[] args = new String[] { topicDetail.getLabel(FIELD_MESSAGE_TEXTAREA) } ;
				throw new MessageException(new ApplicationMessage("MessagePost.msg.message-empty", args)) ;
			}
		}
	}
	
	static public class PreviewReplyActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;	
			String message = topicDetail.getUIStringInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
			String checksms = (message) ;
			if(checksms != null && checksms.trim().length() > 3) {
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
				String[] args = new String[] { topicDetail.getLabel(FIELD_MESSAGE_TEXTAREA) } ;
				throw new MessageException(new ApplicationMessage("MessagePost.msg.message-empty", args)) ;
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
			searchForm.setSelectType(Utils.CATEGORY) ;
			searchForm.setIsSearchForum(false);
			searchForm.setIsSearchTopic(false);
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}

	static	public class BanIPAllForumActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			String ip = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UITopicDetail uiForm = event.getSource() ;
			if(!uiForm.forumService.addBanIP(ip)){
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
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			if(!topicDetail.forumService.addBanIPForum(sProvider, ip, (topicDetail.categoryId + "/" + topicDetail.forumId))){
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
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try{
				Topic topic = topicDetail.getTopic();
				StringBuffer buffer = new StringBuffer();
				buffer.append("ThreadNoNewPost//").append(topic.getTopicName()).append("//").append(topic.getId()) ;
				String userName = topicDetail.userProfile.getUserId() ;
				topicDetail.forumService.saveUserBookmark(sProvider, userName, buffer.toString(), true) ;
				UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateUserProfileInfo() ;
			} catch (Exception e) {
				sProvider.close();
			}
		}
	}

	static public class AddWatchingActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource();
			topicDetail.isEditTopic = true;
			StringBuffer buffer = new StringBuffer();
			buffer.append(topicDetail.categoryId).append("/").append(topicDetail.forumId).append("/").append(topicDetail.topicId) ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			List<String> values = new ArrayList<String>();
			String userName = topicDetail.userProfile.getUserId();
			try {
				values.add(ForumSessionUtils.getUserByUserId(userName).getEmail());
				topicDetail.forumService.addWatch(sProvider, 1, buffer.toString(), values, ForumSessionUtils.getCurrentUser()) ;
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
			}finally {
				sProvider.close();
			}
		}
	}
}
