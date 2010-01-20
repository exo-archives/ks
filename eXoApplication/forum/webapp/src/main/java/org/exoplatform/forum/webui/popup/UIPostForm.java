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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.popup.UIForumInputWithActions.ActionData;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIPostForm.gtmpl",
		events = {
			@EventConfig(listeners = UIPostForm.PreviewPostActionListener.class), 
			@EventConfig(listeners = UIPostForm.SubmitPostActionListener.class), 
			@EventConfig(listeners = UIPostForm.AttachmentActionListener.class, phase = Phase.DECODE), 
			@EventConfig(listeners = UIPostForm.RemoveAttachmentActionListener.class, phase = Phase.DECODE), 
			@EventConfig(listeners = UIPostForm.SelectTabActionListener.class, phase = Phase.DECODE), 
			@EventConfig(listeners = UIPostForm.SelectIconActionListener.class, phase = Phase.DECODE), 
			@EventConfig(listeners = UIPostForm.CancelActionListener.class, phase = Phase.DECODE)
		}
)
public class UIPostForm extends BaseForumForm implements UIPopupComponent {
	public static final String FIELD_POSTTITLE_INPUT = "PostTitle" ;
	public static final String FIELD_EDITREASON_INPUT = "editReason" ;
	public static final String FIELD_LABEL_QUOTE = "ReUser" ;
	
	final static public String ACT_REMOVE = "remove" ;
	final static public String FIELD_ATTACHMENTS = "Attachments" ;
	final static public String FIELD_FROM_INPUT = "FromInput" ;
	final static public String FIELD_MESSAGECONTENT = "MessageContent" ;
	final static public String FIELD_ORIGINALLY = "Originally" ;
	
	public static final String FIELD_THREADCONTEN_TAB = "ThreadContent" ;
	public static final String FIELD_THREADICON_TAB = "IconAndSmiley" ;
	private int tabId = 0 ;
	
	private List<ForumAttachment> attachments_ = new ArrayList<ForumAttachment>() ;
	private String categoryId; 
	private String forumId ;
	private String topicId ;
	private String postId = "";
	private boolean isMod = false ;
	private Topic topic ;
	private Post post_ = new Post();
	private boolean isQuote = false ;
	private boolean isMP = false ;
	private String link = "";
	private boolean isDoubleClickSubmit = false;
	public UIPostForm() throws Exception {
		if(ForumUtils.isEmpty(getId())) setId("UIPostForm");
		isDoubleClickSubmit = false;
		UIFormStringInput postTitle = new UIFormStringInput(FIELD_POSTTITLE_INPUT, FIELD_POSTTITLE_INPUT, null);
		postTitle.addValidator(MandatoryValidator.class);
		UIFormStringInput editReason = new UIFormStringInput(FIELD_EDITREASON_INPUT, FIELD_EDITREASON_INPUT, null);
		editReason.setRendered(false);
		UIForumInputWithActions threadContent = new UIForumInputWithActions(FIELD_THREADCONTEN_TAB) ;
		UIFormWYSIWYGInput formWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, FIELD_MESSAGECONTENT, "") ;
		formWYSIWYGInput.addValidator(MandatoryValidator.class);
		formWYSIWYGInput.setToolBarName("Basic");
		threadContent.addChild(postTitle) ;
		threadContent.addChild(editReason) ;
		threadContent.addChild(formWYSIWYGInput) ;
		threadContent.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
		threadContent.setActionField(FIELD_THREADCONTEN_TAB, getUploadFileList()) ;
		threadContent.setActionIdAddItem(FIELD_ATTACHMENTS);
		threadContent.setActionAddItem("Attachment");
		threadContent.setLabelActionAddItem(getLabel("Attachment"));
		
		UIFormInputIconSelector inputIconSelector = new UIFormInputIconSelector(FIELD_THREADICON_TAB, FIELD_THREADICON_TAB) ;
		inputIconSelector.setSelectedIcon("IconsView") ;
		
		addUIFormInput(threadContent) ;
		addUIFormInput(inputIconSelector) ;
		this.setActions(new String[] {"PreviewPost", "SubmitPost", "Cancel"}) ;
	}
	
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	@SuppressWarnings("unused")
	private boolean tabIsSelected(int tabId) {
		if(this.tabId == tabId) return true ;
		else return false ;
	}
	
	@SuppressWarnings("unused")
	private String[] getTabName(){
		String[] tab = {"UIPostForm.tittle.threadContent", "UIPostForm.tittle.iconAndSmiley"};
		return tab ;
	}
	
	public void setPostIds(String categoryId, String forumId, String topicId, Topic topic) {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topicId ;
		this.topic = topic ;
	}
	
	public List<ActionData> getUploadFileList() { 
		List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
		for(ForumAttachment attachdata : attachments_) {
			ActionData fileUpload = new ActionData() ;
			fileUpload.setActionListener("") ;
			fileUpload.setActionType(ActionData.TYPE_ATT) ;
			fileUpload.setCssIconClass("AttachmentIcon") ;
			String size = ForumUtils.getSizeFile((double)attachdata.getSize()) ;
			fileUpload.setActionName(attachdata.getName() + "("+size+")") ;
			fileUpload.setShowLabel(true) ;
			uploadedFiles.add(fileUpload) ;
			ActionData removeAction = new ActionData() ;
			removeAction.setActionListener("RemoveAttachment") ;
			removeAction.setActionName(ACT_REMOVE);
			removeAction.setActionParameter(attachdata.getId());
			removeAction.setActionType(ActionData.TYPE_ICON) ;
			removeAction.setCssIconClass("DustBin");
			removeAction.setBreakLine(true) ;
			uploadedFiles.add(removeAction) ;
		}
		return uploadedFiles ;
	}
	public void refreshUploadFileList() throws Exception {
		UIForumInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB) ;
		threadContent.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
	}
	public void addToUploadFileList(ForumAttachment attachfile) {
		attachments_.add(attachfile) ;
	}
	public void removeFromUploadFileList(ForumAttachment attachfile) {
		attachments_.remove(attachfile);
	}	
	public void removeUploadFileList() {
		attachments_.clear() ;
	}
	public List<ForumAttachment> getAttachFileList() {
		return attachments_ ;
	}
	 
	public void updatePost(String postId, boolean isQuote, boolean isPP, Post post) throws Exception {
		if(post != null)this.post_ = post;
		this.postId = postId ;
		this.isQuote = isQuote ;
		this.isMP = isPP ;
		UIForumInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB) ;
		UIFormStringInput editReason = threadContent.getUIStringInput(FIELD_EDITREASON_INPUT);
		editReason.setRendered(false) ;
		if(!ForumUtils.isEmpty(this.postId) && post != null) {
			String message = post.getMessage();
			if(isQuote) {//quote
				String title = "" ;
				if(post.getName().indexOf(": ") > 0) title = post.getName() ;
				else title = getLabel(FIELD_LABEL_QUOTE) + ": " + post.getName() ;
				threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(ForumTransformHTML.unCodeHTML(title)) ;
				String value = "[QUOTE=" + post.getOwner() + "]" + message + "[/QUOTE]";
				threadContent.getChild(UIFormWYSIWYGInput.class).setValue(value);
				getChild(UIFormInputIconSelector.class).setSelectedIcon(this.topic.getIcon());
			} else if(isPP){
				String title = this.topic.getTopicName() ;
				threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(getLabel(FIELD_LABEL_QUOTE) + ": " + ForumTransformHTML.unCodeHTML(title)) ;
				getChild(UIFormInputIconSelector.class).setSelectedIcon(this.topic.getIcon());
			} else{//edit
				editReason.setRendered(true);
				threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(ForumTransformHTML.unCodeHTML(post.getName())) ;
				if(post.getAttachments() != null && post.getAttachments().size() > 0) {
					this.attachments_ = post.getAttachments();
					this.refreshUploadFileList();
				}
				threadContent.getChild(UIFormWYSIWYGInput.class).setValue(message);
				getChild(UIFormInputIconSelector.class).setSelectedIcon(post.getIcon());
			}
		} else {
			if(!isQuote) {//reply
				String title = this.topic.getTopicName() ;
				threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(getLabel(FIELD_LABEL_QUOTE) + ": " + ForumTransformHTML.unCodeHTML(title)) ;
				getChild(UIFormInputIconSelector.class).setSelectedIcon(this.topic.getIcon());
			}
		}
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class PreviewPostActionListener extends BaseEventListener<UIPostForm> {
		public void onEvent(Event<UIPostForm> event, UIPostForm uiForm, String id) throws Exception {
			UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB) ;
			int t = 0, k = 1 ;
			String postTitle = threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue();
			String userName = UserHelper.getCurrentUser() ;
			String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			String checksms = ForumTransformHTML.cleanHtmlCode(message, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes())) ;
			checksms = checksms.replaceAll("&nbsp;", " ") ;
			t = checksms.trim().length() ;
			if(postTitle != null && postTitle.length() <= 3) {k = 0;}
			postTitle = ForumTransformHTML.enCodeHTML(postTitle).trim() ;
			if(t > 0 && k != 0 && !checksms.equals("null")) {	
				Post post = uiForm.post_;
				post.setName(postTitle) ;
				post.setMessage(message) ;
				post.setOwner(userName) ;
				if(ForumUtils.isEmpty(uiForm.postId)){
					post.setCreatedDate(ForumUtils.getInstanceTempCalendar().getTime()) ;
					post.setModifiedDate(ForumUtils.getInstanceTempCalendar().getTime()) ;
				}
				post.setModifiedBy(userName) ;
				post.setRemoteAddr("") ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				post.setIcon(uiIconSelector.getSelectedIcon());
				post.setIsApproved(false) ;
				post.setAttachments(uiForm.getAttachFileList()) ;
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIViewPost viewPost = uiForm.openPopup(popupContainer, UIViewPost.class, 670, 0) ;
				viewPost.setId("viewPost") ;
				viewPost.setPostView(post) ;
				viewPost.setActionForm(new String[] {"Close"});
			}else {
				String[] args = { ""} ;
				if(k == 0) {
					args = new String[] {uiForm.getLabel(FIELD_POSTTITLE_INPUT)} ;
					if(t <= 0) args = new String[] { uiForm.getLabel(FIELD_POSTTITLE_INPUT) + ", " + uiForm.getLabel(FIELD_MESSAGECONTENT)} ;
					uiForm.warning("NameValidator.msg.ShortText", args) ;
				} else if(t <= 0) {
					args = new String[] {uiForm.getLabel(FIELD_MESSAGECONTENT) } ;
					uiForm.warning("NameValidator.msg.ShortMessage", args) ;
				}
			}
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
	
	static	public class SubmitPostActionListener extends BaseEventListener<UIPostForm> {
		public void onEvent(Event<UIPostForm> event, UIPostForm uiForm, String id) throws Exception {
			if(uiForm.isDoubleClickSubmit) return;
			uiForm.isDoubleClickSubmit = true;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UserProfile userProfile = forumPortlet.getUserProfile();
			try {
	      if(uiForm.checkForumHasAddTopic(userProfile)) {
	      	UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB) ;
	      	int t = 0, k = 1 ;
	      	String postTitle = " " + threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue();
	      	int maxText = ForumUtils.MAXTITLE ;
	      	if(postTitle.length() > maxText) {
	      		warning("NameValidator.msg.warning-long-text", new String[]{ uiForm.getLabel(FIELD_POSTTITLE_INPUT), String.valueOf(maxText) }) ;
	      		uiForm.isDoubleClickSubmit = false;
	      		return ;
	      	}
	      	String editReason = threadContent.getUIStringInput(FIELD_EDITREASON_INPUT).getValue() ;
	      	if(!ForumUtils.isEmpty(editReason) && editReason.length() > maxText) {
	      		warning("NameValidator.msg.warning-long-text", new String[]{ uiForm.getLabel(FIELD_EDITREASON_INPUT), String.valueOf(maxText) }) ;
	      		uiForm.isDoubleClickSubmit = false;
	      		return ;
	      	}
	      	
	      	editReason = ForumTransformHTML.enCodeHTML(editReason).trim() ;
	      	String userName = userProfile.getUserId() ;
	      	String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
	      	String checksms = ForumTransformHTML.cleanHtmlCode(message, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes())) ;
	      	message = message.replaceAll("<script", "&lt;script").replaceAll("<link", "&lt;link").replaceAll("</script>", "&lt;/script>");
	      	message = StringUtils.replace(message, "'", "&apos;");
	      	message = ForumTransformHTML.fixAddBBcodeAction(message);
	      	String remoteAddr = "";
	      	if(forumPortlet.isEnableIPLogging()) {
	      		try {
	      			HttpServletRequest request = event.getRequestContext().getRequest();
	      			remoteAddr = request.getRemoteAddr();
            } catch (Exception e) {}
	      	}
	      	checksms = checksms.replaceAll("&nbsp;", " ") ;
	      	t = checksms.length() ;
	      	postTitle = postTitle.trim();
	      	if(postTitle.trim().length() <= 0) {k = 0;}
	      	postTitle = ForumTransformHTML.enCodeHTML(postTitle) ;
	      	Post post = uiForm.post_;
	      	boolean isPP = false;
	      	if(t > 0 && k != 0 && !checksms.equals("null")) {	
	      		boolean isOffend = false ; 
	      		boolean hasTopicMod = false ;
	      		if(!uiForm.isMod()) {
      				String []censoredKeyword = uiForm.getCensoredKeyword() ;
      				checksms = checksms.toLowerCase().trim();
      				for (String string : censoredKeyword) {
      					if(checksms.indexOf(string.trim()) >= 0) {isOffend = true ;break;}
      					if(postTitle.toLowerCase().indexOf(string.trim()) >= 0){isOffend = true ;break;}
      				}
	      			if(post.getUserPrivate() != null && post.getUserPrivate().length == 2)isPP = true;
	      			if((!uiForm.isMP || !isPP) && uiForm.topic != null) hasTopicMod = uiForm.topic.getIsModeratePost() ;
	      		}
	      		if(isOffend && (uiForm.isMP || isPP)) {
	      			uiForm.warning("UIPostForm.msg.PrivateCensor") ;
	      			uiForm.isDoubleClickSubmit = false;
	      			return ;
	      		}
	      		// set link
	      		String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, uiForm.topicId).replaceFirst("private", "public");	
	      		//
	      		if(uiForm.isQuote || uiForm.isMP) post = new Post();
	      		post.setName(postTitle) ;
	      		post.setMessage(message) ;
	      		post.setOwner(userName) ;
	      		post.setCreatedDate(new Date()) ;
	      		UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
	      		post.setIcon(uiIconSelector.getSelectedIcon());
	      		post.setAttachments(uiForm.getAttachFileList()) ;
	      		post.setIsHidden(isOffend) ;
	      		post.setLink(link);
	      		String[]userPrivate = new String[]{"exoUserPri"};
	      		if(uiForm.isMP){
	      			userPrivate = new String[]{userName, uiForm.post_.getOwner()};
	      			hasTopicMod = false;
	      		}
	      		post.setUserPrivate(userPrivate);
	      		post.setIsApproved(!hasTopicMod) ;
	      		
	      		UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
	      		UITopicDetail topicDetail = topicDetailContainer.getChild(UITopicDetail.class) ;
	      		boolean isParentDelete = false;
	      		boolean isNew = false;
	      		try {
	      			if(!ForumUtils.isEmpty(uiForm.postId)) {
	      				if(uiForm.isQuote || uiForm.isMP) {
	      					post.setRemoteAddr(remoteAddr) ;
	      					try {
	      						uiForm.getForumService().savePost(uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true, ForumUtils.getDefaultMail()) ;
	      						isNew = true;
	      					} catch (PathNotFoundException e) {
	      						isParentDelete = true;
	      					} catch (Exception ex) {}
	      					topicDetail.setIdPostView("lastpost");
	      				} else{
	      					//post.setId(uiForm.postId) ;
	      					post.setModifiedBy(userName) ;
	      					post.setModifiedDate(new Date()) ;
	      					post.setEditReason(editReason) ;
	      					try {
	      						uiForm.getForumService().savePost(uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, false, ForumUtils.getDefaultMail()) ;
	      					} catch (PathNotFoundException e) {
	      						isParentDelete = true;
	      					} catch (Exception ex) {}
	      					topicDetail.setIdPostView(uiForm.postId);
	      				}
	      			} else {
	      				post.setRemoteAddr(remoteAddr) ;
	      				try {
	      					uiForm.getForumService().savePost(uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true, ForumUtils.getDefaultMail()) ;
	      					isNew = true;
	      				} catch (PathNotFoundException e) {
	      					isParentDelete = true;
	      				} catch (Exception ex) {}
	      				topicDetail.setIdPostView("lastpost");
	      			}
	      			if(isNew) {
	      				if(userProfile.getIsAutoWatchTopicIPost()) {
	      					List<String> values = new ArrayList<String>();
	      					values.add(userProfile.getEmail());
	      					String path = uiForm.categoryId + "/" + uiForm.forumId + "/" + uiForm.topicId;
	      					uiForm.getForumService().addWatch(1, path, values, userProfile.getUserId()) ;
	      				}
	      			}
	      			uiForm.getForumService().updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  uiForm.topicId) ;
	      			forumPortlet.getUserProfile().setLastTimeAccessTopic(uiForm.topicId, ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
	      		} catch (Exception e) {
	      		}
	      		uiForm.isMP = uiForm.isQuote = false;
	      		if(isParentDelete){
	      			uiForm.warning("UIPostForm.msg.isParentDelete") ;
	      			forumPortlet.cancelAction() ;
	      			return ;
	      		}
	      		forumPortlet.cancelAction() ;
	      		if(isOffend || hasTopicMod) {
	      			topicDetail.setIdPostView("normal");
	      			if(isOffend)uiForm.warning("MessagePost.msg.isOffend") ;
	      			else {
	      				uiForm.warning("MessagePost.msg.isModerate") ;
	      			}
	      		}
	      		event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
	      	}else {
	      		String[] args = { ""} ;
	      		if(k == 0) {
	      			args = new String[] {uiForm.getLabel(FIELD_POSTTITLE_INPUT)} ;
	      			if(t == 0) args = new String[] { uiForm.getLabel(FIELD_POSTTITLE_INPUT) + ", " + uiForm.getLabel(FIELD_MESSAGECONTENT)} ;
	      			uiForm.isDoubleClickSubmit = false;
	      			uiForm.warning("NameValidator.msg.ShortText", args) ;
	      		} else if(t == 0) {
	      			args = new String[] {uiForm.getLabel(FIELD_MESSAGECONTENT) } ;
	      			uiForm.isDoubleClickSubmit = false;
	      			uiForm.warning("NameValidator.msg.ShortMessage", args) ;
	      		}
	      	}
	      } else {
	      	forumPortlet.cancelAction() ;
					UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
					topicDetail.setUpdateContainer(uiForm.categoryId, uiForm.forumId, uiForm.topic, 0);
					uiForm.warning("UIPostForm.msg.no-permission") ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
	      }
      } catch (Exception e) {
      	uiForm.log.error("Can not save post into this topic, exception: " + e.getMessage(), e);
      	uiForm.warning("UIPostForm.msg.isParentDelete") ;
  			forumPortlet.cancelAction() ;
      }
		}
	}
	
	static public class AttachmentActionListener extends EventListener<UIPostForm> {
		public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIAttachFileForm attachFileForm = uiForm.openPopup(popupContainer, UIAttachFileForm.class, 500, 0) ;
			attachFileForm.updateIsTopicForm(false) ;
			attachFileForm.setMaxField(5);
		}
	}
	
	static public class SelectTabActionListener extends BaseEventListener<UIPostForm> {
		public void onEvent(Event<UIPostForm> event, UIPostForm postForm, String id) throws Exception {
			postForm.tabId = Integer.parseInt(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(postForm.getParent()) ;
		}
	}
	
	static public class RemoveAttachmentActionListener extends BaseEventListener<UIPostForm> {
		public void onEvent(Event<UIPostForm> event, UIPostForm uiPostForm, String attFileId) throws Exception {
			for (ForumAttachment att : uiPostForm.attachments_) {
				if (att.getId().equals(attFileId)) {
					uiPostForm.removeFromUploadFileList(att);
					break;
				}
			}
			UIForumInputWithActions threadContent = uiPostForm.getChildById(FIELD_THREADCONTEN_TAB) ;
			threadContent.setActionField(FIELD_ATTACHMENTS, uiPostForm.getUploadFileList()) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiPostForm) ;
		}
	}
	
	static public class SelectIconActionListener extends BaseEventListener<UIPostForm> {
		public void onEvent(Event<UIPostForm> event, UIPostForm postForm, String iconName) throws Exception {
			UIFormInputIconSelector iconSelector = postForm.getChild(UIFormInputIconSelector.class);
			if(!iconSelector.getValue().equals(iconName)) {
				iconSelector.setSelectedIcon(iconName) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(postForm.getParent()) ;
			}
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIPostForm> {
		public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	public boolean isMod() {
		return isMod;
	}

	public void setMod(boolean isMod) {
		this.isMod = isMod;
	}	
}