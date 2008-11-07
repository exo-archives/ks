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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.popup.UIForumInputWithActions.ActionData;
import org.exoplatform.ks.common.CommonContact;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormWYSIWYGInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Aug 22, 2007	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UITopicForm.gtmpl",
		events = {
			@EventConfig(listeners = UITopicForm.PreviewThreadActionListener.class), 
			@EventConfig(listeners = UITopicForm.SubmitThreadActionListener.class), 
			@EventConfig(listeners = UITopicForm.AttachmentActionListener.class,phase = Phase.DECODE), 
			@EventConfig(listeners = UITopicForm.RemoveAttachmentActionListener.class,phase = Phase.DECODE), 
			@EventConfig(listeners = UITopicForm.CancelActionListener.class,phase = Phase.DECODE),
			@EventConfig(listeners = UITopicForm.SelectTabActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UITopicForm.SelectIconActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UITopicForm.AddValuesUserActionListener.class, phase=Phase.DECODE)
		}
)
public class UITopicForm extends UIForm implements UIPopupComponent, UISelector {
	
	public static final String FIELD_THREADCONTEN_TAB = "ThreadContent" ;
	public static final String FIELD_THREADICON_TAB = "ThreadIcon" ;
	public static final String FIELD_THREADOPTION_TAB = "ThreadOption" ;
	public static final String FIELD_THREADPERMISSION_TAB = "ThreadPermission" ;
	
	
	public static final String FIELD_TOPICTITLE_INPUT = "ThreadTitle" ;
	public static final String FIELD_EDITREASON_INPUT = "editReason" ;
	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	final static public String FIELD_MESSAGECONTENT = "messageContent" ;
	public static final String FIELD_TOPICSTATUS_SELECTBOX = "TopicStatus" ;
	public static final String FIELD_TOPICSTATE_SELECTBOX = "TopicState" ;
	
	public static final String FIELD_APPROVED_CHECKBOX = "Approved" ;
	public static final String FIELD_MODERATEPOST_CHECKBOX = "ModeratePost" ;
	public static final String FIELD_NOTIFYWHENADDPOST_CHECKBOX = "NotifyWhenAddPost" ;
	public static final String FIELD_STICKY_CHECKBOX = "Sticky" ;
	
	public static final String FIELD_CANVIEW_INPUT = "CanView" ;
	public static final String FIELD_CANPOST_INPUT = "CanPost" ;
	final static public String ACT_REMOVE = "remove" ;
	final static public String FIELD_ATTACHMENTS = "attachments" ;
	
	
	private List<ForumAttachment> attachments_ = new ArrayList<ForumAttachment>() ;
	private String categoryId; 
	private String forumId ;
	private String topicId ;
	private String link = "";
	private Forum forum ;
	private boolean isMod = false;
	private int id = 0;
	private Topic topic = new Topic() ;
	@SuppressWarnings("unchecked")
	public UITopicForm() throws Exception {
		UIFormStringInput topicTitle = new UIFormStringInput(FIELD_TOPICTITLE_INPUT, FIELD_TOPICTITLE_INPUT, null);
		topicTitle.addValidator(MandatoryValidator.class);
		UIFormStringInput editReason = new UIFormStringInput(FIELD_EDITREASON_INPUT, FIELD_EDITREASON_INPUT, null);
		editReason.setRendered(false) ;
//		UIFormTextAreaInput message = new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA, null);
		
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>("Open", "open")) ;
		ls.add(new SelectItemOption<String>("Closed", "closed")) ;
		UIFormSelectBox topicState = new UIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX, FIELD_TOPICSTATE_SELECTBOX, ls) ;
		topicState.setDefaultValue("open");
		List<SelectItemOption<String>> ls1 = new ArrayList<SelectItemOption<String>>() ;
		ls1.add(new SelectItemOption<String>("UnLock", "unlock")) ;
		ls1.add(new SelectItemOption<String>("Locked", "locked")) ;
		UIFormSelectBox topicStatus = new UIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX, FIELD_TOPICSTATUS_SELECTBOX, ls1) ;
		topicStatus.setDefaultValue("unlock");
		
		UIFormCheckBoxInput moderatePost = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEPOST_CHECKBOX, FIELD_MODERATEPOST_CHECKBOX, false);
		UIFormCheckBoxInput checkWhenAddPost = new UIFormCheckBoxInput<Boolean>(FIELD_NOTIFYWHENADDPOST_CHECKBOX, FIELD_NOTIFYWHENADDPOST_CHECKBOX, false);
		UIFormCheckBoxInput sticky = new UIFormCheckBoxInput<Boolean>(FIELD_STICKY_CHECKBOX, FIELD_STICKY_CHECKBOX, false);
		UIFormTextAreaInput canView = new UIFormTextAreaInput(FIELD_CANVIEW_INPUT, FIELD_CANVIEW_INPUT, null);
		UIFormTextAreaInput canPost = new UIFormTextAreaInput(FIELD_CANPOST_INPUT, FIELD_CANPOST_INPUT, null);
		UIFormWYSIWYGInput formWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, null, null, true);
		formWYSIWYGInput.addValidator(MandatoryValidator.class);
		UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector(FIELD_THREADICON_TAB, FIELD_THREADICON_TAB) ;
		uiIconSelector.setSelectedIcon("IconsView");
		
		
		UIForumInputWithActions threadContent = new UIForumInputWithActions(FIELD_THREADCONTEN_TAB);
		threadContent.addUIFormInput(topicTitle);
		threadContent.addUIFormInput(editReason);
		threadContent.addUIFormInput(formWYSIWYGInput);
		threadContent.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
		threadContent.setActionField(FIELD_THREADCONTEN_TAB, getUploadFileList()) ;

		UIForumInputWithActions threadOption = new UIForumInputWithActions(FIELD_THREADOPTION_TAB);
		threadOption.addUIFormInput(topicState);
		threadOption.addUIFormInput(topicStatus);
		threadOption.addUIFormInput(moderatePost);
		threadOption.addUIFormInput(checkWhenAddPost);
		threadOption.addUIFormInput(sticky);
		
		UIForumInputWithActions threadPermission = new UIForumInputWithActions(FIELD_THREADPERMISSION_TAB);
		threadPermission.addUIFormInput(canPost);
		threadPermission.addUIFormInput(canView);
		
		String[]fieldPermissions = new String[]{FIELD_CANVIEW_INPUT, FIELD_CANPOST_INPUT} ; 
		String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		List<ActionData> actions ;
		ActionData ad ;int i ;
		for (String fieldPermission : fieldPermissions) {
			actions = new ArrayList<ActionData>() ;
			i = 0;
			for(String string : strings) {
				ad = new ActionData() ;
				ad.setActionListener("AddValuesUser") ;
				ad.setActionParameter(fieldPermission + "/" + String.valueOf(i)) ;
				ad.setCssIconClass(string + "Icon") ;
				ad.setActionName(string);
				actions.add(ad) ;
				++i;
			}
			threadPermission.setActionField(fieldPermission, actions);
		}
		
		addUIFormInput(threadContent) ;
		addUIFormInput(uiIconSelector) ;
		addUIFormInput(threadOption) ;
		addUIFormInput(threadPermission) ;
		this.setActions(new String[]{"PreviewThread","SubmitThread","Attachment","Cancel"}) ;
	}
	
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	public void setTopicIds(String categoryId, String forumId, Forum forum) {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topic = new Topic();
		this.forum = forum ;
		UIForumInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB);
		threadContent.getUIStringInput(FIELD_EDITREASON_INPUT).setRendered(false) ;
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	@SuppressWarnings("unused")
	private boolean getIsSelected(int id) {
		if(this.id == id) return true ;
		return false ;
	}
	
	public List<ActionData> getUploadFileList() { 
		List<ActionData> uploadedFiles = new ArrayList<ActionData>() ;
		for(ForumAttachment attachdata : attachments_) {
			ActionData fileUpload = new ActionData() ;
			fileUpload.setActionListener("") ;
			fileUpload.setActionType(ActionData.TYPE_ATT) ;
			fileUpload.setCssIconClass("AttachmentIcon ZipFileIcon") ;
			String fileName = ForumUtils.getSizeFile((double)attachdata.getSize());
			fileName = attachdata.getName() + "("+ fileName +")" ;
			fileUpload.setActionName(fileName) ;
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
		UIForumInputWithActions inputSet = getChildById(FIELD_THREADCONTEN_TAB) ;
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

	public void setUpdateTopic(Topic topic, boolean isUpdate) throws Exception {
		if(isUpdate) {
			this.topic =	topic ;
			this.topicId = topic.getId() ;
			UIForumInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB);
			threadContent.getUIStringInput(FIELD_EDITREASON_INPUT).setRendered(true) ;
			threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT).setValue(ForumTransformHTML.unCodeHTML(topic.getTopicName()));
			threadContent.getChild(UIFormWYSIWYGInput.class).setValue(ForumTransformHTML.enCodeHTML(topic.getDescription()));
			
			UIForumInputWithActions threadOption = this.getChildById(FIELD_THREADOPTION_TAB);
			String stat = "open";
			if(topic.getIsClosed()) stat = "closed";
			threadOption.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).setValue(stat);
			if(topic.getIsLock()) stat = "locked";
			else stat = "unlock";
			threadOption.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).setValue(stat);
			threadOption.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).setChecked(topic.getIsModeratePost());
			if(topic.getIsNotifyWhenAddPost() != null && topic.getIsNotifyWhenAddPost().trim().length() > 0){
				threadOption.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).setChecked(true);
			}
			threadOption.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).setChecked(topic.getIsSticky());
			
			UIForumInputWithActions threadPermission = this.getChildById(FIELD_THREADPERMISSION_TAB);
			threadPermission.getUIStringInput(FIELD_CANVIEW_INPUT).setValue(ForumUtils.unSplitForForum(topic.getCanView()));
			threadPermission.getUIStringInput(FIELD_CANPOST_INPUT).setValue(ForumUtils.unSplitForForum(topic.getCanPost()));
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			String postId = topicId.replaceFirst(Utils.TOPIC, Utils.POST) ;
			Post post = forumService.getPost(ForumSessionUtils.getSystemProvider(), this.categoryId, this.forumId, this.topicId, postId);
			if(post.getAttachments() != null && post.getAttachments().size() > 0) {
				this.attachments_ = post.getAttachments();
				this.refreshUploadFileList();
			}
			getChild(UIFormInputIconSelector.class).setSelectedIcon(topic.getIcon());
		}
	}
	
	static	public class PreviewThreadActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiForm = event.getSource() ;
			int t = 0, k = 1 ;
			UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB);
			UIFormStringInput stringInputTitle = threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
			String topicTitle = "	" + stringInputTitle.getValue();
			topicTitle = topicTitle.trim() ;
			String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			String checksms = ForumTransformHTML.cleanHtmlCode(message) ;
			checksms = checksms.replaceAll("&nbsp;", " ") ;
			t = checksms.trim().length() ;
			if(topicTitle.length() <= 3 && topicTitle.equals("null")) {k = 0;}
			topicTitle = ForumTransformHTML.enCodeHTML(topicTitle);
			if(t >= 3 && k != 0 && !checksms.equals("null")) {
				String userName = ForumSessionUtils.getCurrentUser() ;
				Post postNew = new Post();
				postNew.setOwner(userName);
				postNew.setName(topicTitle);
				postNew.setCreatedDate(new Date());
				postNew.setModifiedBy(userName);
				postNew.setModifiedDate(new Date());
				postNew.setMessage(message);
				postNew.setAttachments(uiForm.attachments_) ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				postNew.setIcon(uiIconSelector.getSelectedIcon());
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIViewPost viewPost = popupAction.activate(UIViewPost.class, 670) ;
				viewPost.setId("UIViewTopic") ;
				viewPost.setPostView(postNew) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}else {
				String sms = "" ;
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				if(k == 0) {
					sms = "Thread Title" ;
					if(t < 3) sms = "Thread Title and Message";
					Object[] args = { sms };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortText", args, ApplicationMessage.WARNING)) ;
				} else if(t < 3) {
					Object[] args = { "Message" };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortMessage", args, ApplicationMessage.WARNING)) ;
				}
			}
		}
	}
	
	static	public class SubmitThreadActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			int t = 0, k = 1 ;
			UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB);
			UIFormStringInput stringInputTitle = threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
			String topicTitle = "	" + stringInputTitle.getValue();
			topicTitle = topicTitle.trim() ;
			int maxText = ForumUtils.MAXTITLE ;
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			if(topicTitle.length() > maxText) {
				Object[] args = { uiForm.getLabel(FIELD_TOPICTITLE_INPUT), String.valueOf(maxText) };
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
			String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			message = message.replaceAll("<script", "&lt;script").replaceAll("<link", "&lt;link").replaceAll("</script>", "&lt;/script>");
			String checksms = ForumTransformHTML.cleanHtmlCode(message) ;
			checksms = checksms.replaceAll("&nbsp;", " ") ;
			t = checksms.trim().length() ;
			if(topicTitle.length() <= 3 && topicTitle.equals("null")) {k = 0;}
			topicTitle = ForumTransformHTML.enCodeHTML(topicTitle);
			editReason = ForumTransformHTML.enCodeHTML(editReason);
			if(t >= 3 && k != 0 && !checksms.equals("null")) {
				ForumAdministration forumAdministration = forumService.getForumAdministration(ForumSessionUtils.getSystemProvider()) ;
				boolean isOffend = false ; 
				boolean hasForumMod = false ;
				if(!uiForm.isMod()) {
					String stringKey = forumAdministration.getCensoredKeyword() ;
					if(!ForumUtils.isEmpty(stringKey)) {
						stringKey = stringKey.toLowerCase() ;
						String []censoredKeyword = ForumUtils.splitForForum(stringKey) ;
						checksms = checksms.toLowerCase();
						for (String string : censoredKeyword) {
							if(checksms.indexOf(string.trim()) >= 0) {isOffend = true ;break;}
							if(topicTitle.toLowerCase().indexOf(string.trim()) >= 0){isOffend = true ;break;}
						}
					}
					if(uiForm.forum != null) hasForumMod = uiForm.forum.getIsModerateTopic() ;
				}
				UIForumInputWithActions threadOption = uiForm.getChildById(FIELD_THREADOPTION_TAB);
				// uiForm.getUIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA).getValue() ;
				String topicState = threadOption.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).getValue();
				String topicStatus = threadOption.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).getValue();
				Boolean moderatePost = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).getValue();
				Boolean whenNewPost = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).getValue();
				Boolean sticky = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).getValue();
				UIForumInputWithActions threadPermission = uiForm.getChildById(FIELD_THREADPERMISSION_TAB);
				String canPost = threadPermission.getUIStringInput(FIELD_CANPOST_INPUT).getValue() ;
				String canView = threadPermission.getUIStringInput(FIELD_CANVIEW_INPUT).getValue() ;
				String erroUser = ForumSessionUtils.checkValueUser(canPost) ;
				if(!ForumUtils.isEmpty(erroUser)) {
					Object[] args = { uiForm.getLabel(FIELD_CANPOST_INPUT), erroUser };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return ;
				}
				erroUser = ForumSessionUtils.checkValueUser(canView) ;
				if(!ForumUtils.isEmpty(erroUser)) {
					Object[] args = { uiForm.getLabel(FIELD_CANVIEW_INPUT), erroUser };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
					return ;
				}
				// set link
				PortalRequestContext portalContext = Util.getPortalRequestContext();
				String url = portalContext.getRequest().getRequestURL().toString();
				url = url.replaceFirst("http://", "") ;
				url = url.substring(0, url.indexOf("/")) ;
				url = "http://" + url;
				String link = uiForm.getLink();
				link = ForumSessionUtils.getBreadcumbUrl(link, uiForm.getId(), "PreviewThread");	
				link = link.replaceFirst("pathId", (uiForm.categoryId+"/"+uiForm.forumId+"/"+uiForm.topic.getId())) ;
				link = url + link;
				//
				String userName = ForumSessionUtils.getCurrentUser() ;
				Topic topicNew = uiForm.topic;
				topicNew.setOwner(userName);
				topicNew.setTopicName(topicTitle);
				topicNew.setCreatedDate(new Date());
				topicNew.setModifiedBy(userName);
				topicNew.setModifiedDate(new Date());
				topicNew.setLastPostBy(userName);
				topicNew.setLastPostDate(new Date());
				topicNew.setDescription(message);
				topicNew.setLink(link);
				if(whenNewPost){
					CommonContact contact = ForumSessionUtils.getPersonalContact(userName);
					topicNew.setIsNotifyWhenAddPost(contact.getEmailAddress());
				} else {
					topicNew.setIsNotifyWhenAddPost("");
				}
				topicNew.setIsModeratePost(moderatePost);
				topicNew.setIsWaiting(isOffend) ;
				topicNew.setAttachments(uiForm.attachments_) ;
				if(topicState.equals("closed")) {
					topicNew.setIsClosed(true);
				}else{
					topicNew.setIsClosed(false);
				}
				if(topicStatus.equals("locked")) {
					topicNew.setIsLock(true) ;
				}else {
					topicNew.setIsLock(false) ;
				}
				topicNew.setIsSticky(sticky);
				
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				topicNew.setIcon(uiIconSelector.getSelectedIcon());
				//topicNew.setAttachmentFirstPost(0) ;
				canPost = ForumUtils.removeSpaceInString(canPost) ;
				String temp = "" ;
				if(!ForumUtils.isEmpty(canPost)) {
					temp = ForumUtils.unSplitForForum(uiForm.forum.getPoster());
				}
				if(ForumUtils.isEmpty(temp)) temp = canPost;
				String[]canPosts = ForumUtils.addStringToString(canPost, temp);
				if(!ForumUtils.isEmpty(canView)) {
					temp = ForumUtils.unSplitForForum(uiForm.forum.getViewer());
					canView = canView + "," + temp;
				}
				canView = ForumUtils.removeSpaceInString(canView) ;
				String[]canViews = ForumUtils.addStringToString(canPost, canView);
								
				topicNew.setCanView(canViews);
				topicNew.setCanPost(canPosts);
				topicNew.setIsApproved(!hasForumMod) ;
				if(!ForumUtils.isEmpty(uiForm.topicId)) {
					topicNew.setId(uiForm.topicId);
					topicNew.setEditReason(editReason) ;
					try {
						forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, topicNew, false, false, ForumUtils.getDefaultMail());
						forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((uiForm.categoryId + "/" + uiForm.forumId + "/" + uiForm.topicId)) ;
						UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class) ;
						topicDetail.setIsEditTopic(true) ;
					} catch (PathNotFoundException e) {
						// hung.hoang add
						uiApp.addMessage(new ApplicationMessage("UITopicForm.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
						return ;						
					}					
				} else {
					topicNew.setVoteRating(0.0) ;
					topicNew.setUserVoteRating(new String[] {}) ;
					try {
						forumService.saveTopic(ForumSessionUtils.getSystemProvider(), uiForm.categoryId, uiForm.forumId, topicNew, true, false, ForumUtils.getDefaultMail());
					} catch (PathNotFoundException e) {
						e.printStackTrace();
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.updateIsRender(true) ;
						categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ; 
						forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
						forumPortlet.cancelAction() ;
						uiApp.addMessage(new ApplicationMessage("UITopicForm.msg.forum-deleted", null, ApplicationMessage.WARNING)) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
						return;
					}					
				}
				uiForm.topic = new Topic();
				forumPortlet.cancelAction() ;
				if(isOffend || hasForumMod) {
					Object[] args = { "" };
					if(isOffend)uiApp.addMessage(new ApplicationMessage("MessagePost.msg.isOffend", args, ApplicationMessage.WARNING)) ;
					else {
						args = new Object[]{ "forum", "thread" };
						uiApp.addMessage(new ApplicationMessage("MessageThread.msg.isModerate", args, ApplicationMessage.WARNING)) ;
					}
					event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				}
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
			} else {
				String sms = "" ;
				if(k == 0) {
					sms = "Thread Title" ;
					if(t <= 4) sms = "Thread Title and Message";
					Object[] args = { sms };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortText", args, ApplicationMessage.WARNING)) ;
				} else if(t <= 4) {
					Object[] args = { "Message" };
					uiApp.addMessage(new ApplicationMessage("NameValidator.msg.ShortMessage", args, ApplicationMessage.WARNING)) ;
				}
			}
		}
	}
	
	static public class AttachmentActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIAttachFileForm attachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
			attachFileForm.updateIsTopicForm(true) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static public class RemoveAttachmentActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiTopicForm = event.getSource() ;
			String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
			//BufferAttachment attachfile = new BufferAttachment();
			for (ForumAttachment att : uiTopicForm.attachments_) {
				if (att.getId().equals(attFileId)) {
					//attachfile = (BufferAttachment) att;
					uiTopicForm.removeFromUploadFileList(att);
					uiTopicForm.attachments_.remove(att) ;
					break ;
				}
			}
			uiTopicForm.refreshUploadFileList() ;
		}
	}
	
	static	public class CancelActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	static	public class SelectTabActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			String id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UITopicForm topicForm = event.getSource();
			topicForm.id = Integer.parseInt(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(topicForm.getParent()) ;
		}
	}
	
	static public class SelectIconActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			String iconName = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UITopicForm topicForm = event.getSource();
			UIFormInputIconSelector iconSelector = topicForm.getChild(UIFormInputIconSelector.class);
			if(!iconSelector.getValue().equals(iconName)) {
				iconSelector.setSelectedIcon(iconName) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(topicForm.getParent()) ;
			}
		}
	}
	
	public void updateSelect(String selectField, String value ) throws Exception {
		UIFormTextAreaInput fieldInput = getUIFormTextAreaInput(selectField) ;
		String values = fieldInput.getValue() ;
		if(!ForumUtils.isEmpty(values)) {
			values = ForumUtils.removeSpaceInString(values);
			if(!ForumUtils.isStringInStrings(values.split(","), value)){
				if(values.lastIndexOf(",") != (values.length() - 1)) values = values + ",";
				values = values + value ;
			} 
		} else values = value ;
		fieldInput.setValue(values) ;
	}
	
	static	public class AddValuesUserActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiTopicForm = event.getSource() ;
			String objctId = event.getRequestContext().getRequestParameter(OBJECTID)	;
			String[]array = objctId.split("/") ;
			String childId = array[0] ;
			if(!ForumUtils.isEmpty(childId)) {
				UIPopupContainer popupContainer = uiTopicForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
				UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 600) ;
				if(array[1].equals("0")) uiGroupSelector.setId("UIUserSelector");
				else if(array[1].equals("1")) uiGroupSelector.setId("UIMemberShipSelector");
				uiGroupSelector.setType(array[1]) ;
				uiGroupSelector.setSelectedGroups(null) ;
				uiGroupSelector.setComponent(uiTopicForm, new String[]{childId}) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}

	public boolean isMod() {
		return isMod;
	}

	public void setMod(boolean isMod) {
		this.isMod = isMod;
	}
}