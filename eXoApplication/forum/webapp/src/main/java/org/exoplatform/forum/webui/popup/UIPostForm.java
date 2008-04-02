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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.BufferAttachment;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.EmptyNameValidator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

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
			@EventConfig(listeners = UIPostForm.AttachmentActionListener.class), 
			@EventConfig(listeners = UIPostForm.RemoveAttachmentActionListener.class), 
			@EventConfig(listeners = UIPostForm.CancelActionListener.class,phase = Phase.DECODE)
		}
)
public class UIPostForm extends UIForm implements UIPopupComponent {
	private ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	public static final String FIELD_POSTTITLE_INPUT = "PostTitle" ;
	//public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	public static final String FIELD_LABEL_QUOTE = "ReUser" ;
	
	final static public String ACT_REMOVE = "remove" ;
	final static public String FIELD_ATTACHMENTS = "attachments" ;
	final static public String FIELD_FROM_INPUT = "fromInput" ;
	final static public String FIELD_MESSAGECONTENT = "messageContent" ;
	final static public String FIELD_ORIGINALLY = "Originally" ;
	
	private List<ForumAttachment> attachments_ = new ArrayList<ForumAttachment>() ;
	private String categoryId; 
	private String forumId ;
	private String topicId ;
	private String postId ;
	private boolean isQuote = false ;
	public UIPostForm() throws Exception {
		UIFormStringInput postTitle = new UIFormStringInput(FIELD_POSTTITLE_INPUT, FIELD_POSTTITLE_INPUT, null);
		postTitle.addValidator(EmptyNameValidator.class) ;
		addUIFormInput(postTitle);
		//UIFormTextAreaInput message = new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA, null);
		//message.addValidator(EmptyNameValidator.class) ;
		//addUIFormInput(message);
		
		UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector("Icon", "Icon") ;
		uiIconSelector.setSelectedIcon("IconsView");
		addUIFormInput(uiIconSelector) ;
		
		addUIFormInput(new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true));
		
		UIFormInputWithActions inputSet = new UIFormInputWithActions(FIELD_FROM_INPUT); 
		inputSet.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
		inputSet.setActionField(FIELD_FROM_INPUT, getUploadFileList()) ;
		addUIFormInput(inputSet) ;
	}
	
	public void setPostIds(String categoryId, String forumId, String topicId) {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topicId = topicId ;
	}
	
	public List<ActionData> getUploadFileList() { 
		List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
		for(ForumAttachment attachdata : attachments_) {
			ActionData fileUpload = new ActionData() ;
			fileUpload.setActionListener("") ;
			fileUpload.setActionType(ActionData.TYPE_ICON) ;
			fileUpload.setCssIconClass("AttachmentIcon") ;
			fileUpload.setActionName(attachdata.getName() + " ("+attachdata.getSize()/1024 +" Kb)" ) ;
			fileUpload.setShowLabel(true) ;
			uploadedFiles.add(fileUpload) ;
			ActionData removeAction = new ActionData() ;
			removeAction.setActionListener("RemoveAttachment") ;
			removeAction.setActionName(ACT_REMOVE);
			removeAction.setActionParameter(attachdata.getId());
			removeAction.setActionType(ActionData.TYPE_LINK) ;
			removeAction.setBreakLine(true) ;
			uploadedFiles.add(removeAction) ;
		}
		return uploadedFiles ;
	}
	public void refreshUploadFileList() throws Exception {
		UIFormInputWithActions inputSet = getChildById(FIELD_FROM_INPUT) ;
		inputSet.setActionField(FIELD_ATTACHMENTS, getUploadFileList()) ;
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
	 
	public void updatePost(String postId, boolean isQuote) throws Exception {
		this.postId = postId ;
		this.isQuote = isQuote ;
		if(this.postId != null && this.postId.length() > 0) {
			Post post = this.forumService.getPost(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, this.topicId, postId) ;
			String message = post.getMessage() ;
			if(isQuote) {//quote
				String title = "" ;
				if(post.getSubject().indexOf(": ") > 0) title = post.getSubject() ;
				else title = getLabel(FIELD_LABEL_QUOTE) + ": " + post.getSubject() ;
				getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(title) ;
				String value = "[QUOTE=" + post.getOwner() + "]" + ForumFormatUtils.clearQuote(message) + "[/QUOTE]<br/>";
				getChild(UIFormWYSIWYGInput.class).setValue(value);
				//getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA).setDefaultValue(value) ;
				getChild(UIFormInputIconSelector.class).setSelectedIcon(post.getIcon());
			} else {//edit
				getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(post.getSubject()) ;
//				this.attachments_ = post.getAttachments();
//				this.refreshUploadFileList();
				getChild(UIFormWYSIWYGInput.class).setValue(message);
				getChild(UIFormInputIconSelector.class).setSelectedIcon(post.getIcon());
			}
		} else {
			if(!isQuote) {//reply
				Topic topic = this.forumService.getTopic(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, this.topicId, "guest") ;
				String title = topic.getTopicName() ;
				getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(getLabel(FIELD_LABEL_QUOTE) + ": " + title) ;
				getChild(UIFormInputIconSelector.class).setSelectedIcon(topic.getIcon());
			}
		}
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public String[] getActionsTopic() throws Exception {
		return (new String [] {"PreviewPost", "SubmitPost", "Cancel"});
	}
		
	static	public class PreviewPostActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			int t = 0, k = 1 ;
			String postTitle = uiForm.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue().trim();
			String message = uiForm.getChild(UIFormWYSIWYGInput.class).getValue();
			String userName = ForumSessionUtils.getCurrentUser() ;
			if(message != null && message.length() > 0) message = message.trim() ;
			t = message.length() ;
			if(postTitle.length() <= 3) {k = 0;}
			if(t >= 20 && k != 0) {	
				Post post = new Post() ;
				post.setSubject(postTitle.trim()) ;
				post.setMessage(message) ;
				post.setOwner(userName) ;
				post.setCreatedDate(new Date()) ;
				post.setModifiedBy(userName) ;
				post.setModifiedDate(new Date()) ;
				post.setRemoteAddr("") ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				post.setIcon(uiIconSelector.getSelectedIcon());
				post.setIsApproved(false) ;
				post.setAttachments(uiForm.attachments_) ;
				
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true)	;
				UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 670) ;
				viewTopic.setPostView(post) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}else {
				String[] args = { ""} ;
				if(k == 0) {
					args = new String[] { "Thread Title" } ;
					if(t < 20) args = new String[] { "Thread Title and Message" } ;
					throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortText", args)) ;
				} else if(t < 20) {
					args = new String[] { "Message" } ;
					throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortMessage", args)) ;
				}
			}
		}
	}
	
	static	public class SubmitPostActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			int t = 0, k = 1 ;
			String postTitle = uiForm.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue().trim();
			String message = uiForm.getChild(UIFormWYSIWYGInput.class).getValue();
				//uiForm.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
			String userName = ForumSessionUtils.getCurrentUser() ;
			if(message != null && message.length() > 0) message = message.trim() ;
			//message = message.replaceAll("[QUOTE][/QUOTE]", "").replaceAll('['+"CODE][/CODE"+']', "") ;
			t = ForumFormatUtils.clearQuote(message).length() ;
			if(postTitle.length() <= 3) {k = 0;}
			if(t >= 20 && k != 0) {	
				Post post = new Post() ;
				post.setSubject(postTitle.trim()) ;
				post.setMessage(message) ;
				post.setOwner(userName) ;
				post.setCreatedDate(new Date()) ;
				post.setModifiedBy(userName) ;
				post.setModifiedDate(new Date()) ;
				post.setRemoteAddr("") ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				post.setIcon(uiIconSelector.getSelectedIcon());
				post.setIsApproved(false) ;
				post.setAttachments(uiForm.attachments_) ;
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
				if(uiForm.postId != null && uiForm.postId.length() > 0) {
					if(uiForm.isQuote) {
						uiForm.forumService.savePost(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true) ;
						topicDetailContainer.getChild(UITopicDetail.class).setIdPostView("true");
					} else {
						post.setId(uiForm.postId) ;
						uiForm.forumService.savePost(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, false) ;
						topicDetailContainer.getChild(UITopicDetail.class).setIdPostView(uiForm.postId);
					}
				} else {
					uiForm.forumService.savePost(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true) ;
					topicDetailContainer.getChild(UITopicDetail.class).setIdPostView("true");
				}
				forumPortlet.cancelAction() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
			}else {
				String[] args = { ""} ;
				if(k == 0) {
					args = new String[] { "Thread Title" } ;
					if(t < 20) args = new String[] { "Thread Title and Message" } ;
					throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortText", args)) ;
				} else if(t < 20) {
					args = new String[] { "Message" } ;
					throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortMessage", args)) ;
				}
			}
		}
	}
	
	static public class AttachmentActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIAttachFileForm attachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
			attachFileForm.updateIsTopicForm(false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static public class RemoveAttachmentActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiPostForm = event.getSource() ;
			String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
			BufferAttachment attachfile = new BufferAttachment();
			for (ForumAttachment att : uiPostForm.attachments_) {
				if (att.getId().equals(attFileId)) {
					attachfile = (BufferAttachment) att;
				}
			}
			uiPostForm.removeFromUploadFileList(attachfile);
			uiPostForm.refreshUploadFileList() ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
			UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
		}
	}	
}