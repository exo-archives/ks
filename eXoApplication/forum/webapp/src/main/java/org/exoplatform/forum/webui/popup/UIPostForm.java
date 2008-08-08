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

import javax.jcr.PathNotFoundException;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.popup.UIForumInputWithActions.ActionData;
import org.exoplatform.services.portletcontainer.plugins.pc.portletAPIImp.PortletRequestImp;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.exception.MessageException;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

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
public class UIPostForm extends UIForm implements UIPopupComponent {
	private ForumService forumService =	(ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	public static final String FIELD_POSTTITLE_INPUT = "PostTitle" ;
	public static final String FIELD_EDITREASON_INPUT = "editReason" ;
	//public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
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
	private String postId ;
	private Topic topic ;
	private Post post_ = new Post();
	private boolean isQuote = false ;
	private boolean isMP = false ;
	public UIPostForm() throws Exception {
    UIFormStringInput postTitle = new UIFormStringInput(FIELD_POSTTITLE_INPUT, FIELD_POSTTITLE_INPUT, null);
    postTitle.addValidator(MandatoryValidator.class);
    UIFormStringInput editReason = new UIFormStringInput(FIELD_EDITREASON_INPUT, FIELD_EDITREASON_INPUT, null);
    editReason.setRendered(false);
    UIForumInputWithActions threadContent = new UIForumInputWithActions(FIELD_THREADCONTEN_TAB) ;
    threadContent.addChild(postTitle) ;
    threadContent.addChild(editReason) ;
    threadContent.addChild(new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true)) ;
    threadContent.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
    threadContent.setActionField(FIELD_THREADCONTEN_TAB, getUploadFileList()) ;
    UIFormInputIconSelector inputIconSelector = new UIFormInputIconSelector(FIELD_THREADICON_TAB, FIELD_THREADICON_TAB) ;
    inputIconSelector.setSelectedIcon("IconsView") ;
    
    addUIFormInput(threadContent) ;
    addUIFormInput(inputIconSelector) ;
    this.setActions(new String[] {"PreviewPost", "SubmitPost", "Attachment", "Cancel"}) ;
	}
	
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
			removeAction.setActionType(ActionData.TYPE_LINK) ;
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
	 
	public void updatePost(String postId, boolean isQuote, boolean isMP, Post post) throws Exception {
		this.post_ = post;
		this.postId = postId ;
		this.isQuote = isQuote ;
		this.isMP = isMP ;
		UIForumInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB) ;
		UIFormStringInput editReason = threadContent.getUIStringInput(FIELD_EDITREASON_INPUT);
		editReason.setRendered(false) ;
		if(!ForumUtils.isEmpty(this.postId)) {
			String message = post.getMessage() ;
			if(isQuote) {//quote
				String title = "" ;
				if(post.getName().indexOf(": ") > 0) title = post.getName() ;
				else title = getLabel(FIELD_LABEL_QUOTE) + ": " + post.getName() ;
        threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(title) ;
				String value = "[QUOTE=" + post.getOwner() + "]" + ForumTransformHTML.clearQuote(message) + "[/QUOTE]";
        threadContent.getChild(UIFormWYSIWYGInput.class).setValue(value);
				//getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA).setDefaultValue(value) ;
				getChild(UIFormInputIconSelector.class).setSelectedIcon(this.topic.getIcon());
			} else if(isMP){
				String title = this.topic.getTopicName() ;
				threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(getLabel(FIELD_LABEL_QUOTE) + ": " + title) ;
				getChild(UIFormInputIconSelector.class).setSelectedIcon(this.topic.getIcon());
			} else{//edit
				editReason.setRendered(true);
        threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(post.getName()) ;
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
        threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).setValue(getLabel(FIELD_LABEL_QUOTE) + ": " + title) ;
				getChild(UIFormInputIconSelector.class).setSelectedIcon(this.topic.getIcon());
			}
		}
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class PreviewPostActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB) ;
			int t = 0, k = 1 ;
			String postTitle = threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue();
			String userName = ForumSessionUtils.getCurrentUser() ;
			String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			String checksms = ForumTransformHTML.cleanHtmlCode(message) ;
			checksms = checksms.replaceAll("&nbsp;", " ") ;
			t = checksms.trim().length() ;
			if(postTitle != null && postTitle.length() <= 3) {k = 0;}
			if(t >= 3 && k != 0 && !checksms.equals("null")) {	
				Post post = new Post();
				post.setName(postTitle) ;
				post.setMessage(message) ;
				post.setOwner(userName) ;
				post.setCreatedDate(new Date()) ;
				post.setModifiedBy(userName) ;
				post.setModifiedDate(new Date()) ;
				post.setRemoteAddr("") ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				post.setIcon(uiIconSelector.getSelectedIcon());
				post.setIsApproved(false) ;
				post.setAttachments(uiForm.getAttachFileList()) ;
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true)	;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670) ;
				viewPost.setId("viewPost") ;
				viewPost.setPostView(post) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
			}else {
				String[] args = { ""} ;
				if(k == 0) {
					args = new String[] {uiForm.getLabel(FIELD_POSTTITLE_INPUT)} ;
					if(t < 4) args = new String[] { uiForm.getLabel(FIELD_POSTTITLE_INPUT) + ", " + uiForm.getLabel(FIELD_MESSAGECONTENT)} ;
					throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortText", args)) ;
				} else if(t < 4) {
					args = new String[] {uiForm.getLabel(FIELD_MESSAGECONTENT) } ;
					throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortMessage", args)) ;
				}
			}
		}
	}
	
	static	public class SubmitPostActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiForm = event.getSource() ;
			UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB) ;
			int t = 0, k = 1 ;
			String postTitle = " " + threadContent.getUIStringInput(FIELD_POSTTITLE_INPUT).getValue();
				//uiForm.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
			int maxText = ForumUtils.MAXTITLE ;
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			if(postTitle.length() > maxText) {
				Object[] args = { uiForm.getLabel(FIELD_POSTTITLE_INPUT), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			String editReason = threadContent.getUIStringInput(FIELD_EDITREASON_INPUT).getValue() ;
			if(!ForumUtils.isEmpty(editReason) && editReason.length() > maxText) {
				Object[] args = { uiForm.getLabel(FIELD_EDITREASON_INPUT), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			String userName = ForumSessionUtils.getCurrentUser() ;
			String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			String checksms = ForumTransformHTML.cleanHtmlCode(message) ;
			PortletRequestImp request = event.getRequestContext().getRequest();
			String remoteAddr = request.getRemoteAddr();
			ForumAdministration forumAdministration = uiForm.forumService.getForumAdministration(ForumSessionUtils.getSystemProvider()) ;
			boolean isOffend = false ; 
			checksms = checksms.replaceAll("&nbsp;", " ") ;
			String stringKey = forumAdministration.getCensoredKeyword();
			if(!ForumUtils.isEmpty(stringKey)) {
				stringKey = stringKey.toLowerCase() ;
				String []censoredKeyword = ForumUtils.splitForForum(stringKey) ;
				checksms = checksms.toLowerCase().trim();
				for (String string : censoredKeyword) {
		      if(checksms.indexOf(string.trim()) >= 0) {isOffend = true ;break;}
		      if(postTitle.toLowerCase().indexOf(string.trim()) >= 0){isOffend = true ;break;}
	      }
			}
			t = checksms.length() ;
			if(postTitle.trim().length() <= 3) {k = 0;}
			if(t >= 3 && k != 0 && !checksms.equals("null")) {	
        Post post = new Post();
				post.setName(postTitle.trim()) ;
				post.setMessage(message) ;
				post.setOwner(userName) ;
				post.setCreatedDate(new Date()) ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				post.setIcon(uiIconSelector.getSelectedIcon());
				post.setAttachments(uiForm.getAttachFileList()) ;
				post.setIsHidden(isOffend) ;
				String[]userPrivate = new String[]{"exoUserPri"};
				if(uiForm.isMP){
					userPrivate = new String[]{userName, uiForm.post_.getOwner()};
				}
				post.setUserPrivate(userPrivate);
				boolean hasTopicMod = false ;
				if(uiForm.topic != null) hasTopicMod = uiForm.topic.getIsModeratePost() ;
				post.setIsApproved(!hasTopicMod) ;
				
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
				UITopicDetail topicDetail = topicDetailContainer.getChild(UITopicDetail.class) ;
				boolean isParentDelete = false;
				if(!ForumUtils.isEmpty(uiForm.postId)) {
					if(uiForm.isQuote || uiForm.isMP) {
						post.setRemoteAddr(remoteAddr) ;
						try {
							uiForm.forumService.savePost(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true) ;
            } catch (PathNotFoundException e) {
	            isParentDelete = true;
            }
						topicDetail.setIdPostView("true");
						topicDetail.setUpdatePostPageList(true);
					} else{
						post.setId(uiForm.postId) ;
						post.setModifiedBy(userName) ;
						post.setModifiedDate(new Date()) ;
						post.setEditReason(editReason) ;
						try {
							uiForm.forumService.savePost(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, false) ;
						} catch (PathNotFoundException e) {
							isParentDelete = true;
						}
						topicDetail.setIdPostView(uiForm.postId);
					}
				} else {
					post.setRemoteAddr(remoteAddr) ;
					try {
						uiForm.forumService.savePost(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, uiForm.topicId, post, true) ;
					} catch (PathNotFoundException e) {
						isParentDelete = true;
					}
          topicDetail.setIdPostView("true");
          topicDetail.setUpdatePostPageList(true);
				}
				uiForm.isMP = uiForm.isQuote = false;
				if(isParentDelete){
					Object[] args = { "" };
					uiApp.addMessage(new ApplicationMessage("UIPostForm.msg.isParentDelete", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					forumPortlet.cancelAction() ;
					return ;
				}
				forumPortlet.cancelAction() ;
				if(isOffend || hasTopicMod) {
          topicDetail.setIdPostView("normal");
					Object[] args = { "" };
					if(isOffend)uiApp.addMessage(new ApplicationMessage("MessagePost.msg.isOffend", args, ApplicationMessage.WARNING)) ;
					else {
						args = new Object[]{ "thread", "post" };
						uiApp.addMessage(new ApplicationMessage("MessagePost.msg.isModerate", args, ApplicationMessage.WARNING)) ;
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
			}else {
				String[] args = { ""} ;
				if(k == 0) {
					args = new String[] {uiForm.getLabel(FIELD_POSTTITLE_INPUT)} ;
					if(t < 4) args = new String[] { uiForm.getLabel(FIELD_POSTTITLE_INPUT) + ", " + uiForm.getLabel(FIELD_MESSAGECONTENT)} ;
					throw new MessageException(new ApplicationMessage("NameValidator.msg.ShortText", args)) ;
				} else if(t < 4) {
					args = new String[] {uiForm.getLabel(FIELD_MESSAGECONTENT) } ;
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
	static public class SelectTabActionListener extends EventListener<UIPostForm> {
	  public void execute(Event<UIPostForm> event) throws Exception {
      String id = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIPostForm postForm = event.getSource();
      postForm.tabId = Integer.parseInt(id);
      event.getRequestContext().addUIComponentToUpdateByAjax(postForm.getParent()) ;
	  }
	}
	
	static public class RemoveAttachmentActionListener extends EventListener<UIPostForm> {
    public void execute(Event<UIPostForm> event) throws Exception {
			UIPostForm uiPostForm = event.getSource() ;
			String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
			for (ForumAttachment att : uiPostForm.attachments_) {
				if (att.getId().equals(attFileId)) {
					uiPostForm.removeFromUploadFileList(att);
          uiPostForm.attachments_.remove(att) ;
					break;
				}
			}
			uiPostForm.refreshUploadFileList() ;
		}
	}
	
	static public class SelectIconActionListener extends EventListener<UIPostForm> {
		public void execute(Event<UIPostForm> event) throws Exception {
			String iconName = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIPostForm postForm = event.getSource();
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
			UITopicDetailContainer topicDetailContainer = forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(topicDetailContainer);
		}
	}	
}