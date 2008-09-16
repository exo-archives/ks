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
import org.exoplatform.forum.service.JCRPageList;
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
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.portletcontainer.plugins.pc.portletAPIImp.PortletRequestImp;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
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
			@EventConfig(listeners = UITopicDetail.DeleteActionListener.class,confirm="UITopicDetail.confirm.DeletePost" ),	
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
			@EventConfig(listeners = UITopicDetail.SetDeleteTopicActionListener.class,confirm="UITopicDetail.confirm.DeleteTopic" ),	
			@EventConfig(listeners = UITopicDetail.MergePostActionListener.class ), //Post Menu 
			@EventConfig(listeners = UITopicDetail.MovePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetApprovePostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetHiddenPostActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.SetUnHiddenPostActionListener.class ),	
//			@EventConfig(listeners = UITopicDetail.SetUnApproveAttachmentActionListener.class ),	
			@EventConfig(listeners = UITopicDetail.DeletePostActionListener.class ),
			
			@EventConfig(listeners = UITopicDetail.QuickReplyActionListener.class),
			@EventConfig(listeners = UITopicDetail.PreviewReplyActionListener.class),
			
			@EventConfig(listeners = UITopicDetail.ViewPostedByUserActionListener.class ), 
			@EventConfig(listeners = UITopicDetail.ViewPublicUserInfoActionListener.class ) ,
			@EventConfig(listeners = UITopicDetail.ViewThreadByUserActionListener.class ),
			@EventConfig(listeners = UITopicDetail.WatchOptionActionListener.class ),
			@EventConfig(listeners = UITopicDetail.PrivateMessageActionListener.class )
		}
)
public class UITopicDetail extends UIForm {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String categoryId ;
	private String forumId ; 
	private String topicId = "";
	private String link = "";
	private boolean viewTopic = true ;
	private Forum forum;
	private Topic topic = new Topic();
	private JCRPageList pageList ;
	private long pageSelect	= 1 ;
	private boolean isGopage = false ;
	private boolean isEditTopic = false ;
	private boolean isUpdatePageList = false ;
	private boolean isGetTopic = false ;
	private String IdPostView = "false" ;
	private String IdLastPost = "false" ;
	private List<Post>	posts ;
	private boolean isEdit = false ;
	private long maxPost = 10 ;
	private UserProfile userProfile = null;
	private String userName = " " ;
	private boolean isModeratePost = false ;
	private boolean isMod = false ;
	private Map<String, UserProfile> mapUserProfile = new HashMap<String, UserProfile>();
	private Map<String, ForumContact> mapContact = new HashMap<String, ForumContact>();
//replace when portal fix bug show image
	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	public UITopicDetail() throws Exception {
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_T, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.GOPAGE_ID_B, null)) ;
		addUIFormInput( new UIFormStringInput(ForumUtils.SEARCHFORM_ID, null)) ;
		addUIFormInput( new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA,null)) ;
		addChild(UIForumPageIterator.class, null, "TopicPageIterator") ;
		addChild(UIPostRules.class, null, null);
		this.setSubmitAction("GoNumberPage") ;
		this.setActions(new String[]{"PreviewReply","QuickReply"} );
	}
	
	public UserProfile getUserProfile() {
		return userProfile ;
	}
	
	@SuppressWarnings("unused")
	private boolean isOnline(String userId) throws Exception {
		return this.forumService.isOnline(userId) ;
	}
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	
	public void setUpdateTopic(String categoryId, String forumId, String topicId, boolean viewTopic) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topicId ;
		this.viewTopic = viewTopic ;
		this.isUpdatePageList = true ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		this.userProfile = forumPortlet.getUserProfile() ;
		String userName = this.userProfile.getUserId() ;
		if(!this.viewTopic) userName = "guest" ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
		this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId, userName) ;
		if(!userName.equals("guest"))	forumPortlet.setUserProfile() ;
		this.userName = userName ;
	}
	
	public void setTopicFromCate(String categoryId, String forumId, Topic topic, boolean viewTopic) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		this.viewTopic = viewTopic ;
		this.isUpdatePageList = true ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		this.userProfile = forumPortlet.getUserProfile() ;
		if(this.userProfile == null) this.userProfile = new UserProfile();
		String userName = this.userProfile.getUserId() ;
		if(!this.viewTopic) userName = "guest" ;
		boolean isGetService = false ;
		if(!userName.equals(this.userName)) {
			String []topicsRead = this.userProfile.getReadTopic() ;
			if(topicsRead != null && topicsRead.length > 0) {
				if(!ForumUtils.isStringInStrings(topicsRead, this.topicId)) {
					isGetService = true ;
				} 
			} else isGetService = true ;
		} 
		this.userName = userName ;
		if(isGetService) {
			this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId(), userName) ;
			forumPortlet.setUserProfile() ;
		} else {
			this.topic = topic ;
		}
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
	}
	
	public void setUpdateContainer(String categoryId, String forumId, Topic topic, long numberPage) throws Exception {
		if(this.topicId == null || !this.topicId.equals(topic.getId())) this.userName = "" ;
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topic.getId() ;
		this.viewTopic = false ;
		this.pageSelect = numberPage ;
		this.isGopage = true ;
		this.isEditTopic = false ;
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class) ;
		this.userProfile = forumPortlet.getUserProfile() ;
		String userName = this.userProfile.getUserId() ;
		boolean isGetService = false ;
		if(!userName.equals(this.userName)) {
			String []topicsRead = this.userProfile.getReadTopic() ;
			if(topicsRead != null && topicsRead.length > 0) {
				if(!ForumUtils.isStringInStrings(topicsRead, this.topicId)) {
					isGetService = true ;
				} 
			} else isGetService = true ;
		} 
		this.userName = userName ;
		if(isGetService || this.isGetTopic) {
			this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topic.getId(), userName) ;
			forumPortlet.setUserProfile() ;
			this.isGetTopic = false ;
		} else {
			this.topic = topic ;
		}
		this.getChild(UIForumPageIterator.class).setSelectPage(numberPage) ;
		forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((categoryId + "/" + forumId + "/" + topicId)) ;
	}
	
	public void setUpdateForum(Forum forum) throws Exception {
		this.forum = forum ;
	}
	
	@SuppressWarnings("unused")
	private boolean isCanPostReply() throws Exception {
		if(!isMod) {
			List<String> listUser = new ArrayList<String>() ;
			listUser.addAll(ForumServiceUtils.getUserPermission(this.topic.getCanPost())) ;
			if(!listUser.isEmpty() && listUser.size() > 0 && !listUser.get(0).equals(" ")) {
				listUser.addAll(ForumServiceUtils.getUserPermission(this.forum.getPoster())) ;
				if(!listUser.contains(userName)) {
					return false ;
				}
			}
		}
		return true ;
	}
	
	@SuppressWarnings("unused")
	private Forum getForum() throws Exception {
		return this.forum ;
	}
	
	@SuppressWarnings("unused")
	private String getIdPostView() {
		if(this.IdPostView.equals("true")){
			this.IdPostView = "normal" ;
			return this.IdLastPost ;
		}
		String temp = this.IdPostView ;
		this.IdPostView = "normal" ;
		return temp ;
	}
	
	public void setIdPostView(String IdPostView) {
		this.IdPostView = IdPostView ;
	}
	
	public void setGetTopic(boolean isGetTopic) {
		this.isGetTopic = isGetTopic ;
	}
	
	public void setIsEditTopic( boolean isEditTopic) {
		this.isEditTopic = isEditTopic ;
	}

	public void setUpdatePageList(JCRPageList pageList) throws Exception {
		this.pageList = pageList ;
		this.isUpdatePageList = false ;
	}
	
	@SuppressWarnings("unused")
	private Topic getTopic() throws Exception {
		try {
			if(this.isEditTopic || this.topic == null) {
				this.topic = forumService.getTopic(ForumSessionUtils.getSystemProvider(), categoryId, forumId, topicId, Utils.GUEST) ;
				this.isEditTopic = false ;
			}
			return this.topic ;
		} catch (Exception e) {
			e.printStackTrace();
			return null ;
		}
	}
	
	@SuppressWarnings("unused")
	private boolean userCanView() {
		try {
			List<String> listUser = new ArrayList<String>() ;
			Topic topic = this.getTopic() ;
			listUser.addAll(ForumServiceUtils.getUserPermission(topic.getCanView())) ;
			if(!listUser.isEmpty() && listUser.size() > 0 && !listUser.get(0).equals(" ")){
				Forum forum = this.getForum() ;
				listUser.addAll(ForumServiceUtils.getUserPermission(forum.getViewer())) ;
			}
			if(listUser.isEmpty()|| listUser.size() == 0 || listUser.get(0).equals(" ") || listUser.contains(this.getUserProfile().getUserId())) return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false ;
		}
		return false ;
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
	private String getAvatarUrl(ForumContact contact) throws Exception {
//		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
//		try {
//			ContactAttachment attachment = contact.getAttachment() ; 
//			InputStream input = attachment.getInputStream() ;
//			String fileName = attachment.getFileName() ;
//			return ForumSessionUtils.getFileSource(input, fileName, dservice);
//		} catch (NullPointerException e) {
//			return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
//		}
		if (contact.getAvatarUrl() == null ) {
			return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
		} else {
			return contact.getAvatarUrl();
		}
	}
	@SuppressWarnings("unused")
	private void initPage() throws Exception {
		String userLogin = this.userProfile.getUserId();
		isMod = false;
		if(this.userProfile.getUserRole() == 0) isMod = true;
		if(!isMod) isMod = ForumServiceUtils.hasPermission(forum.getModerators(), userLogin) ;
		try {
			if (this.isUpdatePageList) {
				String isApprove = "";
				String isHidden = "";
				Topic topic = this.topic;
				if(!isMod) isHidden = "false"; 
				
				if (this.forum.getIsModeratePost() || topic.getIsModeratePost()) {
					if (!isMod && !(this.topic.getOwner().equals(userLogin)))
						isApprove = "true";
				}
				this.pageList = this.forumService.getPosts(ForumSessionUtils.getSystemProvider(),
						this.categoryId, this.forumId, topicId, isApprove, isHidden, "", userLogin);
				this.isUpdatePageList = false;
			}
			long maxPost = this.userProfile.getMaxPostInPage();
			if (maxPost > 0)
				this.maxPost = maxPost;
			pageList.setPageSize(this.maxPost);
			UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class);
			forumPageIterator.updatePageList(this.pageList);
			if (IdPostView.equals("true")) {
				forumPageIterator.setSelectPage(pageList.getAvailablePage());
			}
			this.isModeratePost = this.topic.getIsModeratePost();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unused")
	private boolean getIsModeratePost(){return this.isModeratePost; }
	public void setUpdatePostPageList(boolean isUpdatePageList) {
		this.isUpdatePageList = isUpdatePageList;
	}
	@SuppressWarnings("unused")
	private long getPageSelect() {return this.pageSelect ;}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private List<Post> getPostPageList() throws Exception {
		if(this.pageList == null) return null ;
		UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class) ;
		if(!this.isGopage) {
			this.pageSelect = forumPageIterator.getPageSelected() ;
			long availablePage = this.pageList.getAvailablePage() ;
			if(this.pageSelect > availablePage) {
				this.pageSelect = availablePage ;
				forumPageIterator.setSelectPage(availablePage);
			}
		}
		if(this.pageSelect < 1) return null ;
		try {
			this.posts = this.pageList.getPage(this.pageSelect) ;
		} catch (Exception e) {
			long availablePage = this.pageList.getAvailablePage() ;
			this.pageSelect = availablePage ;
			forumPageIterator.setSelectPage(availablePage);
			this.posts = this.pageList.getPage(availablePage) ;
		}
		if(this.posts.size() > 0 && this.posts != null) {
			for (Post post : this.posts) {
				if(getUIFormCheckBoxInput(post.getId()) != null) {
					getUIFormCheckBoxInput(post.getId()).setChecked(false) ;
				}else {
					addUIFormInput(new UIFormCheckBoxInput(post.getId(), post.getId(), false) );
				}
				this.IdLastPost = post.getId() ;
			}
		}
		this.isGopage = false ;
		return this.posts ;
	}
	
	@SuppressWarnings("unchecked")
	public List<Post> getAllPost() throws Exception {return this.pageList.getPage(0) ;}
	
	private Post getPost(String postId) throws Exception {
		for(Post post : this.posts) {
			if(post.getId().equals(postId)) return post;
		}
		return null ;
	}
	
	@SuppressWarnings("unused")
	private void setIsEdit( boolean isEdit) {
		this.isEdit = isEdit ;
	}
	@SuppressWarnings("unused")
	public void setPostRules(boolean isNull) throws Exception {
		UIPostRules postRules = getChild(UIPostRules.class); 
		postRules.setUserProfile(this.userProfile) ;
		if(!isNull) {
			this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
			if(this.forum.getIsClosed() || this.forum.getIsLock()){
				postRules.setCanCreateNewThread(false);
				postRules.setCanAddPost(false);
			} else {
				/**
				 * set permission for create new thread
				 */
				String userLogin = this.userProfile.getUserId() ;
				String[] strings = this.forum.getCreateTopicRole();
				boolean canCreateThread = false;
				if(this.isEdit || (strings != null && strings.length > 0 && ForumServiceUtils.hasPermission(strings, userLogin))) {
					canCreateThread = true;
					postRules.setCanCreateNewThread(true);
				} else {
					postRules.setCanCreateNewThread(false);
				}
				/**
				 * set permission for post reply
				 */
				if(this.topic != null && !this.topic.getIsClosed() && !this.topic.getIsLock()){
					strings = this.topic.getCanPost() ;
					if(canCreateThread || strings == null || strings.length < 1 ||(strings.length == 1 && strings[0].trim().length() < 1)) {
						postRules.setCanAddPost(true);
					} else {
						postRules.setCanAddPost(ForumServiceUtils.hasPermission(strings, userLogin));
					}
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
		UserProfile userProfile = this.forumService.getUserInfo(ForumSessionUtils.getSystemProvider(), userName);
		if (userProfile.getUser() == null) {
			User user = ForumSessionUtils.getUserByUserId(userName) ; 
			userProfile.setUser(user) ;
			mapUserProfile.put(userName, userProfile) ;
		}
		return userProfile ;
	}
	
	@SuppressWarnings("unchecked")
	private List<Post> getPostsSelected() throws Exception{
		List<Post> posts = new ArrayList<Post>();
		List<UIComponent> children = this.getChildren() ;
		for(UIComponent child : children) {
			if(child instanceof UIFormCheckBoxInput) {
				if(((UIFormCheckBoxInput)child).isChecked()) {
					posts.add(getPost(((UIFormCheckBoxInput)child).getName())) ;
				}
			}
		}
		return posts;
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
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIAddPostContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
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
				topicDetail.viewTopic = false ;
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
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSearch> list = forumService.getQuickSearch(ForumSessionUtils.getSystemProvider(), text, type.toString(), path, ForumSessionUtils.getAllGroupAndMembershipOfUser(topicDetail.getUserProfile().getUserId()));
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
						topicDetail.isGopage = true ;
						topicDetail.pageSelect = page ;
						topicDetail.getChild(UIForumPageIterator.class).setSelectPage(page) ;
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
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost(postId, false, false, topicDetail.getPost(postId)) ;
			postForm.setMod(topicDetail.isMod) ;
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIEditPostContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}
	
	static public class DeleteActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			topicDetail.forumService.removePost(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, postId) ;
			topicDetail.isUpdatePageList = true ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail.getParent()) ;
		}
	}
	
	static public class QuoteActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost(postId, true, false, topicDetail.getPost(postId)) ;
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIQuoteContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
	}

	static public class PrivatePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			String postId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			UIPostForm postForm = popupContainer.addChild(UIPostForm.class, null, null) ;
			postForm.setPostIds(topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, topicDetail.topic) ;
			postForm.updatePost(postId, false, true, topicDetail.getPost(postId)) ;
			postForm.setMod(topicDetail.isMod) ;
			topicDetail.viewTopic = false ;
			popupContainer.setId("UIPrivatePostContainer") ;
			popupAction.activate(popupContainer, 700, 460) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
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
			popupAction.activate(popupContainer, 700, 460) ;
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
			topicDetail.isEditTopic = true ;
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
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 1) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
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
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 1) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
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
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 2) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
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
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 2) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
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
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 4) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
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
				topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 4) ;
				topicDetail.viewTopic = false ;
				topicDetail.isEditTopic = true ;
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
				popupAction.activate(splitTopicForm, 700, 550) ;
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
			topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 3) ;
			topicDetail.viewTopic = false ;
			topicDetail.isEditTopic = true ;
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
			topicDetail.forumService.modifyTopic(ForumSessionUtils.getSystemProvider(), topics, 3) ;
			topicDetail.viewTopic = false ;
			topicDetail.isEditTopic = true ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}

	static public class SetDeleteTopicActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			Topic topic = topicDetail.topic ;
			topicDetail.forumService.removeTopic(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topic.getId()) ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			uiForumContainer.setIsRenderChild(true) ;
			UITopicContainer topicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
			topicContainer.setUpdateForum(topicDetail.categoryId, topicDetail.forum) ;
			UIBreadcumbs breadcumbs = forumPortlet.getChild(UIBreadcumbs.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForumContainer) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
		}
	}

	//---------------------------------	Post Menu	 --------------------------------------//
	static public class MergePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
		}
	}

	static public class MovePostActionListener extends EventListener<UITopicDetail> {
		@SuppressWarnings("unchecked")
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			UIForumPortlet forumPortlet = topicDetail.getAncestorOfType(UIForumPortlet.class) ;
			List<Post> posts = new ArrayList<Post>();
			List<UIComponent> children = topicDetail.getChildren() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						posts.add(topicDetail.getPost(((UIFormCheckBoxInput)child).getName()));
					}
				}
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
				throw new MessageException(new ApplicationMessage("UITopicDetail.msg.notCheck", args, ApplicationMessage.WARNING)) ;
			}
		}
	}

	static public class SetApprovePostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<Post> posts = topicDetail.getPostsSelected();
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
					topicDetail.forumService.modifyPost(ForumSessionUtils.getSystemProvider(), posts, 1) ;
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
			List<Post> posts = new ArrayList<Post>();
			List<UIComponent> children = topicDetail.getChildren() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						post = topicDetail.getPost(((UIFormCheckBoxInput)child).getName());
						post.setIsHidden(true) ;
						posts.add(post) ;
					}
				}
			}
			topicDetail.forumService.modifyPost(ForumSessionUtils.getSystemProvider(), posts, 2) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail) ;
		}
	}

	static public class SetUnHiddenPostActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			List<Post> posts = topicDetail.getPostsSelected();
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
					topicDetail.forumService.modifyPost(ForumSessionUtils.getSystemProvider(), posts, 2) ;
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
			List<UIComponent> children = topicDetail.getChildren() ;
			List<Post> posts = new ArrayList<Post>() ;
			for(UIComponent child : children) {
				if(child instanceof UIFormCheckBoxInput) {
					if(((UIFormCheckBoxInput)child).isChecked()) {
						posts.add(topicDetail.getPost(((UIFormCheckBoxInput)child).getName()));
					}
				}
			}
			for(Post post : posts) {
				topicDetail.forumService.removePost(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post.getId()) ;
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
			UserProfile userProfile ;
			if(topicDetail.mapUserProfile.containsKey(userId)){
				userProfile = topicDetail.mapUserProfile.get(userId) ;
			} else {
				userProfile = topicDetail.forumService.getUserInfo(ForumSessionUtils.getSystemProvider(), userId) ;
				User user = ForumSessionUtils.getUserByUserId(userId) ; 
				userProfile.setUser(user) ;
			}
			viewUserProfile.setUserProfile(userProfile) ;
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
	
	static public class QuickReplyActionListener extends EventListener<UITopicDetail> {
		public void execute(Event<UITopicDetail> event) throws Exception {
			UITopicDetail topicDetail = event.getSource() ;
			ForumAdministration forumAdministration = topicDetail.forumService.getForumAdministration(ForumSessionUtils.getSystemProvider()) ;
			UIFormTextAreaInput textAreaInput = topicDetail.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA) ;
			String message = textAreaInput.getValue() ;
			String checksms = message ;
			if(checksms != null && checksms.trim().length() > 3) {
				boolean isOffend = false ;
				boolean hasTopicMod = false ;
				if(!topicDetail.isMod) {
					String stringKey = forumAdministration.getCensoredKeyword();
					if(stringKey != null && stringKey.length() > 0) {
						stringKey = stringKey.toLowerCase() ;
						String []censoredKeyword = ForumUtils.splitForForum(stringKey) ;
						checksms = checksms.toLowerCase().trim();
						for (String string : censoredKeyword) {
							if(checksms.indexOf(string.trim().toLowerCase()) >= 0) {isOffend = true ;break;}
						}
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
						buffer.append("&#39") ;
					} else {
						buffer.append(c) ;
					}
				}
				
			// set link
				PortalRequestContext portalContext = Util.getPortalRequestContext();
				String url = portalContext.getRequest().getRequestURL().toString();
				url = url.replaceFirst("http://", "") ;
				url = url.substring(0, url.indexOf("/")) ;
				url = "http://" + url;
				String link = topicDetail.getLink();
				link = ForumSessionUtils.getBreadcumbUrl(link, topicDetail.getId(), "ViewThreadByUser");				
				link = link.replaceFirst("pathId", (topicDetail.categoryId+"/"+topicDetail.forumId+"/"+topicDetail.topicId)) ;
				link = url + link;
				//
				String userName = topicDetail.userProfile.getUserId() ;
				PortletRequestImp request = event.getRequestContext().getRequest();
				String remoteAddr = request.getRemoteAddr();
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
					topicDetail.forumService.savePost(ForumSessionUtils.getSystemProvider(), topicDetail.categoryId, topicDetail.forumId, topicDetail.topicId, post, true) ;
				} catch (PathNotFoundException e) {
					String[] args = new String[] { } ;
					throw new MessageException(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
				}
				topicDetail.setUpdatePostPageList(true);
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
					topicDetail.IdPostView = "false";
				} else {
					topicDetail.IdPostView = "true";
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
	
}
