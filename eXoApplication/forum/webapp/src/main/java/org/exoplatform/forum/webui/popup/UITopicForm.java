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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.popup.UIForumInputWithActions.ActionData;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;
import org.exoplatform.webui.organization.account.UIUserSelector;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Aug 22, 2007
 */

@ComponentConfigs ( {
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
							@EventConfig(listeners = UITopicForm.AddTypeTopicActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UITopicForm.AddValuesUserActionListener.class, phase=Phase.DECODE),
							@EventConfig(listeners = UITopicForm.AddUserActionListener.class, phase=Phase.DECODE)
						}
				)
			,
		    @ComponentConfig(
             id = "UITopicUserPopupWindow",
             type = UIPopupWindow.class,
             template =  "system:/groovy/webui/core/UIPopupWindow.gtmpl",
             events = {
               @EventConfig(listeners = UIPopupWindow.CloseActionListener.class, name = "ClosePopup")  ,
               @EventConfig(listeners = UITopicForm.AddActionListener.class, name = "Add", phase = Phase.DECODE),
               @EventConfig(listeners = UITopicForm.CloseActionListener.class, name = "Close", phase = Phase.DECODE)
             }
		    )
		}
)

public class UITopicForm extends BaseForumForm implements UISelector {
	
	public static final String FIELD_THREADCONTEN_TAB = "ThreadContent" ;
	public static final String FIELD_THREADICON_TAB = "ThreadIcon" ;
	public static final String FIELD_THREADOPTION_TAB = "ThreadOption" ;
	public static final String FIELD_THREADPERMISSION_TAB = "ThreadPermission" ;
	
	
	public static final String FIELD_TOPICTITLE_INPUT = "ThreadTitle" ;
	public static final String FIELD_EDITREASON_INPUT = "editReason" ;
	public static final String FIELD_MESSAGE_TEXTAREA = "Message" ;
	final static public String FIELD_MESSAGECONTENT = "messageContent" ;
	public static final String FIELD_TOPICSTATUS_SELECTBOX = "TopicStatus" ;
	public static final String FIELD_TOPICTYPE_SELECTBOX = "TopicType" ;
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
	private boolean isDetail = false;
	private int id = 0;
	private Topic topic = new Topic() ;
	private List<TopicType> listTT = new ArrayList<TopicType>();
	private boolean isDoubleClickSubmit = false; 
	@SuppressWarnings("unchecked")
	public UITopicForm() throws Exception {
		if(getId()== null)setId("UITopicForm");
		isDoubleClickSubmit = false;
		UIFormStringInput topicTitle = new UIFormStringInput(FIELD_TOPICTITLE_INPUT, FIELD_TOPICTITLE_INPUT, null);
		topicTitle.addValidator(MandatoryValidator.class);
		UIFormStringInput editReason = new UIFormStringInput(FIELD_EDITREASON_INPUT, FIELD_EDITREASON_INPUT, null);
		editReason.setRendered(false) ;
//		UIFormTextAreaInput message = new UIFormTextAreaInput(FIELD_MESSAGE_TEXTAREA, FIELD_MESSAGE_TEXTAREA, null);
		
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>(getLabel("Open"), "open")) ;
		ls.add(new SelectItemOption<String>(getLabel("Closed"), "closed")) ;
		UIFormSelectBox topicState = new UIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX, FIELD_TOPICSTATE_SELECTBOX, ls) ;
		topicState.setDefaultValue("open");
		List<SelectItemOption<String>> ls1 = new ArrayList<SelectItemOption<String>>() ;
		ls1.add(new SelectItemOption<String>(getLabel("UnLock"), "unlock")) ;
		ls1.add(new SelectItemOption<String>(getLabel("Locked"), "locked")) ;
		UIFormSelectBox topicStatus = new UIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX, FIELD_TOPICSTATUS_SELECTBOX, ls1) ;
		topicStatus.setDefaultValue("unlock");
		
		setTopicType();
		ls = new ArrayList<SelectItemOption<String>>();
		ls.add(new SelectItemOption<String>("none", getLabel("None")));
		for (TopicType topicType : listTT) {
			ls.add(new SelectItemOption<String>(topicType.getName(), topicType.getId()));
    }
		UIFormSelectBox topicType = new UIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX, FIELD_TOPICTYPE_SELECTBOX, ls) ;
		topicType.setDefaultValue(TopicType.DEFAULT_ID);
		
		UIFormCheckBoxInput moderatePost = new UIFormCheckBoxInput<Boolean>(FIELD_MODERATEPOST_CHECKBOX, FIELD_MODERATEPOST_CHECKBOX, false);
		UIFormCheckBoxInput checkWhenAddPost = new UIFormCheckBoxInput<Boolean>(FIELD_NOTIFYWHENADDPOST_CHECKBOX, FIELD_NOTIFYWHENADDPOST_CHECKBOX, false);
		UIFormCheckBoxInput sticky = new UIFormCheckBoxInput<Boolean>(FIELD_STICKY_CHECKBOX, FIELD_STICKY_CHECKBOX, false);
		UIFormTextAreaInput canView = new UIFormTextAreaInput(FIELD_CANVIEW_INPUT, FIELD_CANVIEW_INPUT, null);
		UIFormTextAreaInput canPost = new UIFormTextAreaInput(FIELD_CANPOST_INPUT, FIELD_CANPOST_INPUT, null);
		UIFormWYSIWYGInput formWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MESSAGECONTENT, FIELD_MESSAGECONTENT, "");
		formWYSIWYGInput.addValidator(MandatoryValidator.class);
		formWYSIWYGInput.setToolBarName("Basic");
		UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector(FIELD_THREADICON_TAB, FIELD_THREADICON_TAB) ;
		uiIconSelector.setSelectedIcon("IconsView");
		
		
		UIForumInputWithActions threadContent = new UIForumInputWithActions(FIELD_THREADCONTEN_TAB);
		threadContent.addUIFormInput(topicTitle);
		threadContent.addUIFormInput(editReason);
		threadContent.addUIFormInput(formWYSIWYGInput);
		threadContent.addUIFormInput(new UIFormInputInfo(FIELD_ATTACHMENTS, FIELD_ATTACHMENTS, null)) ;
		threadContent.setActionField(FIELD_THREADCONTEN_TAB, getUploadFileList()) ;
		threadContent.setActionIdAddItem(FIELD_ATTACHMENTS);
		threadContent.setActionAddItem("Attachment");
		threadContent.setLabelActionAddItem(getLabel("Attachment"));
		
		UIForumInputWithActions threadOption = new UIForumInputWithActions(FIELD_THREADOPTION_TAB);
		threadOption.addUIFormInput(topicType);
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
				if(i==0) ad.setActionListener("AddUser") ;
	      else ad.setActionListener("AddValuesUser") ;
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
		this.setActions(new String[]{"PreviewThread","SubmitThread","Cancel"}) ;
	}
	
	private void addActionAddTopicType() throws Exception {
		List<ActionData> actions = new ArrayList<ActionData>() ;
		ActionData ad = new ActionData() ;
		ad.setActionListener("AddTypeTopic") ;
		ad.setCssIconClass("AddIcon16x16") ;
		ad.setActionName("AddTypeTopic");
		actions.add(ad) ;
		UIForumInputWithActions threadOption = this.getChildById(FIELD_THREADOPTION_TAB);
		threadOption.setActionField(FIELD_TOPICTYPE_SELECTBOX, actions);
	}
	
	public String getLink() {return link;}
	public void setLink(String link) {this.link = link;}
	public void setIsDetail(boolean isDetail) {this.isDetail = isDetail;}
	public void setTopicIds(String categoryId, String forumId, Forum forum, long userRole) throws Exception {
		this.categoryId = categoryId ;
		this.forumId = forumId ;
		this.topic = new Topic();
		this.forum = forum ;
		UIForumInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB);
		threadContent.getUIStringInput(FIELD_EDITREASON_INPUT).setRendered(false) ;
		if(userRole == 0) {
			addActionAddTopicType();
		}
		setShowInfo();
	}
	
	private void setShowInfo() throws Exception {
		String info = getLabel("CanViewInfo");
		String []canV = forum.getViewer();
		UIForumInputWithActions threadPermission = this.getChildById(FIELD_THREADPERMISSION_TAB);
		if(!ForumUtils.isArrayEmpty(canV)){
			info = getLabel("CanViewParentInfo");
		} else {
			canV = getForumService().getPermissionTopicByCategory(categoryId, "viewer");
			if(!ForumUtils.isArrayEmpty(canV)){
				info = getLabel("CanViewParentInfo");
			}
		}
		threadPermission.setMapLabelInfo(FIELD_CANVIEW_INPUT, info);
		info = getLabel("CanPostInfo");
		canV = forum.getPoster();
		if(!ForumUtils.isArrayEmpty(canV)){
			info = getLabel("CanPostParentInfo");
		} else {
			canV = getForumService().getPermissionTopicByCategory(categoryId, "poster");
			if(!ForumUtils.isArrayEmpty(canV)){
				info = getLabel("CanPostParentInfo");
			}
		}
		threadPermission.setMapLabelInfo(FIELD_CANPOST_INPUT, info);
	}
	
	private void setTopicType() throws Exception {
		listTT.clear();
		listTT.addAll(getForumService().getTopicTypes());
	}
	
	public void addNewTopicType() throws Exception {
		setTopicType();
		List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
		ls.add(new SelectItemOption<String>("none", getLabel("None")));
		for (TopicType topicType : listTT) {
			ls.add(new SelectItemOption<String>(topicType.getName(), topicType.getId()));
    }
		UIForumInputWithActions threadOption = this.getChildById(FIELD_THREADOPTION_TAB);
		threadOption.getUIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX).setOptions(ls);
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
			String fileName = ForumUtils.getSizeFile(attachdata.getSize());
			fileName = attachdata.getName() + "("+ fileName +")" ;
			fileUpload.setActionName(fileName) ;
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
			this.topicId = topic.getId() ;
			this.topic =	getForumService().getTopic(categoryId, forumId, topicId, "") ;
			UIForumInputWithActions threadContent = this.getChildById(FIELD_THREADCONTEN_TAB);
			threadContent.getUIStringInput(FIELD_EDITREASON_INPUT).setRendered(true) ;
			threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT).setValue(ForumTransformHTML.unCodeHTML(this.topic.getTopicName()));
			threadContent.getChild(UIFormWYSIWYGInput.class).setValue(this.topic.getDescription());
			
			UIForumInputWithActions threadOption = this.getChildById(FIELD_THREADOPTION_TAB);
			String stat = "open";
			if(this.topic.getIsClosed()) stat = "closed";
			threadOption.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).setValue(stat);
			if(this.topic.getIsLock()) stat = "locked";
			else stat = "unlock";
			threadOption.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).setValue(stat);
			threadOption.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).setChecked(this.topic.getIsModeratePost());
			if(this.topic.getIsNotifyWhenAddPost() != null && this.topic.getIsNotifyWhenAddPost().trim().length() > 0){
				threadOption.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).setChecked(true);
			}
			
			threadOption.getUIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX).setValue(this.topic.getTopicType());
			threadOption.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).setChecked(this.topic.getIsSticky());
			
			UIForumInputWithActions threadPermission = this.getChildById(FIELD_THREADPERMISSION_TAB);
			threadPermission.getUIFormTextAreaInput(FIELD_CANVIEW_INPUT).setValue(ForumUtils.unSplitForForum(this.topic.getCanView()));
			threadPermission.getUIFormTextAreaInput(FIELD_CANPOST_INPUT).setValue(ForumUtils.unSplitForForum(this.topic.getCanPost()));
			String postId = topicId.replaceFirst(Utils.TOPIC, Utils.POST) ;
			Post post = getForumService().getPost(this.categoryId, this.forumId, this.topicId, postId);
			if(post != null && post.getAttachments() != null && post.getAttachments().size() > 0) {
				this.attachments_ = post.getAttachments();
				this.refreshUploadFileList();
			}
			getChild(UIFormInputIconSelector.class).setSelectedIcon(topic.getIcon());
		}
	}
	
	static	public class PreviewThreadActionListener extends BaseEventListener<UITopicForm> {
		public void onEvent(Event<UITopicForm> event, UITopicForm uiForm, final String objectId) throws Exception {
			int t = 0, k = 1 ;
			UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB);
			UIFormStringInput stringInputTitle = threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
			String topicTitle = "	" + stringInputTitle.getValue();
			topicTitle = topicTitle.trim() ;
			String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
			String checksms = ForumTransformHTML.cleanHtmlCode(message, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes())) ;
			checksms = checksms.replaceAll("&nbsp;", " ") ;
			t = checksms.trim().length() ;
			if(topicTitle.length() < 1 && topicTitle.equals("null")) {k = 0;}
			topicTitle = ForumTransformHTML.enCodeHTML(topicTitle);
			if(t > 0 && k != 0 && !checksms.equals("null")) {
				String userName = UserHelper.getCurrentUser() ;
				Post postNew = new Post();
				postNew.setOwner(userName);
				postNew.setName(topicTitle);
				if(ForumUtils.isEmpty(uiForm.topicId)){
					postNew.setCreatedDate(ForumUtils.getInstanceTempCalendar().getTime());
					postNew.setModifiedDate(ForumUtils.getInstanceTempCalendar().getTime());
				}else {
					postNew.setCreatedDate(uiForm.topic.getCreatedDate());
					postNew.setModifiedDate(uiForm.topic.getModifiedDate());
				}
				postNew.setModifiedBy(userName);
				postNew.setMessage(message);
				postNew.setAttachments(uiForm.attachments_) ;
				UIFormInputIconSelector uiIconSelector = uiForm.getChild(UIFormInputIconSelector.class);
				postNew.setIcon(uiIconSelector.getSelectedIcon());
				
				UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
				UIViewPost viewPost = openPopup(popupContainer, UIViewPost.class, "ViewTopic", 670, 0) ;
				viewPost.setPostView(postNew) ;
				viewPost.setActionForm(new String[] {"Close"});
			}else {
				String[] args = {""} ;
    		if(k == 0) {
    			args = new String[] {uiForm.getLabel(FIELD_TOPICTITLE_INPUT)} ;
    			if(t == 0) args = new String[] { uiForm.getLabel(FIELD_TOPICTITLE_INPUT) + ", " + uiForm.getLabel(FIELD_MESSAGECONTENT)} ;
    			warning("NameValidator.msg.ShortText", args);
    		} else if(t == 0) {
    			args = new String[] {uiForm.getLabel(FIELD_MESSAGECONTENT) } ;
    			warning("NameValidator.msg.ShortMessage", args);
    		}
			}
		}
	}
	
	private boolean checkForumHasAddTopic(UserProfile userProfile) throws Exception {
		try {
			this.forum = (Forum)getForumService().getObjectNameById(forum.getId(), Utils.FORUM);
			if(this.forum.getIsClosed() || this.forum.getIsLock()) return false;
			if(userProfile.getUserRole() > 1 || (userProfile.getUserRole() == 1 && !ForumServiceUtils.hasPermission(forum.getModerators(), userProfile.getUserId()))) {
				String[] canCreadTopic = forum.getCreateTopicRole();
				if(canCreadTopic != null && canCreadTopic.length > 0 && !canCreadTopic[0].equals(" ")){
					return ForumServiceUtils.hasPermission(canCreadTopic, userProfile.getUserId());
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
	
	static	public class SubmitThreadActionListener extends BaseEventListener<UITopicForm> {
		public void onEvent(Event<UITopicForm> event, UITopicForm uiForm, final String objectId) throws Exception {
			if(uiForm.isDoubleClickSubmit) return;
			uiForm.isDoubleClickSubmit = true;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UserProfile userProfile = forumPortlet.getUserProfile();
			try {
				if(uiForm.checkForumHasAddTopic(userProfile)) {
					int t = 0, k = 1 ;
					UIForumInputWithActions threadContent = uiForm.getChildById(FIELD_THREADCONTEN_TAB);
					UIFormStringInput stringInputTitle = threadContent.getUIStringInput(FIELD_TOPICTITLE_INPUT) ; 
					String topicTitle = "	" + stringInputTitle.getValue();
					topicTitle = topicTitle.trim() ;
					int maxText = ForumUtils.MAXTITLE ;
					if(topicTitle.length() > maxText) {
						String[] args = { uiForm.getLabel(FIELD_TOPICTITLE_INPUT), String.valueOf(maxText) };
						warning("NameValidator.msg.warning-long-text", args) ;
						uiForm.isDoubleClickSubmit = false;
						return ;
					}
					String editReason = threadContent.getUIStringInput(FIELD_EDITREASON_INPUT).getValue() ;
					if(!ForumUtils.isEmpty(editReason) && editReason.length() > maxText) {
						String[] args = { uiForm.getLabel(FIELD_EDITREASON_INPUT), String.valueOf(maxText) };
						warning("NameValidator.msg.warning-long-text", args) ;
						uiForm.isDoubleClickSubmit = false;
						return ;
					}
					String message = threadContent.getChild(UIFormWYSIWYGInput.class).getValue();
					message = message.replaceAll("<script", "&lt;script").replaceAll("<link", "&lt;link").replaceAll("</script>", "&lt;/script>");
					String checksms = ForumTransformHTML.cleanHtmlCode(message, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes())) ;
					message = StringUtils.replace(message, "'", "&apos;");
					checksms = checksms.replaceAll("&nbsp;", " ") ;
					t = checksms.trim().length() ;
					if(topicTitle.length() <= 0 && topicTitle.equals("null")) {k = 0;}
					topicTitle = ForumTransformHTML.enCodeHTML(topicTitle);
					editReason = ForumTransformHTML.enCodeHTML(editReason);
					if(t > 0 && k != 0 && !checksms.equals("null")) {
						boolean isOffend = false ; 
						boolean hasForumMod = false ;
						if(!uiForm.isMod()) {
							String []censoredKeyword = uiForm.getCensoredKeyword() ;
							checksms = checksms.toLowerCase();
							for (String string : censoredKeyword) {
								if(checksms.indexOf(string.trim()) >= 0) {isOffend = true ;break;}
								if(topicTitle.toLowerCase().indexOf(string.trim()) >= 0){isOffend = true ;break;}
							}
							if(uiForm.forum != null) hasForumMod = uiForm.forum.getIsModerateTopic() ;
						}
						UIForumInputWithActions threadOption = uiForm.getChildById(FIELD_THREADOPTION_TAB);
						String topicType = threadOption.getUIFormSelectBox(FIELD_TOPICTYPE_SELECTBOX).getValue();
						if(topicType.equals("none")) topicType = " ";
						String topicState = threadOption.getUIFormSelectBox(FIELD_TOPICSTATE_SELECTBOX).getValue();
						String topicStatus = threadOption.getUIFormSelectBox(FIELD_TOPICSTATUS_SELECTBOX).getValue();
						Boolean moderatePost = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_MODERATEPOST_CHECKBOX).getValue();
						Boolean whenNewPost = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_NOTIFYWHENADDPOST_CHECKBOX).getValue();
						Boolean sticky = (Boolean)threadOption.getUIFormCheckBoxInput(FIELD_STICKY_CHECKBOX).getValue();
						UIForumInputWithActions threadPermission = uiForm.getChildById(FIELD_THREADPERMISSION_TAB);
						String canPost = threadPermission.getUIFormTextAreaInput(FIELD_CANPOST_INPUT).getValue();
						String canView = threadPermission.getUIFormTextAreaInput(FIELD_CANVIEW_INPUT).getValue();
						canPost = ForumUtils.removeSpaceInString(canPost);
						canPost = ForumUtils.removeStringResemble(canPost);
						canView = ForumUtils.removeSpaceInString(canView);
						canView = ForumUtils.removeStringResemble(canView);
						String erroUser = UserHelper.checkValueUser(canPost) ;
						if(!ForumUtils.isEmpty(erroUser)) {
							String[] args = {uiForm.getLabel(FIELD_CANPOST_INPUT), erroUser};
							warning("NameValidator.msg.erroUser-input", args);
							uiForm.isDoubleClickSubmit = false;
							return ;
						}
						erroUser = UserHelper.checkValueUser(canView) ;
						if(!ForumUtils.isEmpty(erroUser)) {
							String[] args = { uiForm.getLabel(FIELD_CANVIEW_INPUT), erroUser };
							warning("NameValidator.msg.erroUser-input", args);
							uiForm.isDoubleClickSubmit = false;
							return ;
						}
						// set link
						Topic topicNew = uiForm.topic;
						String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, topicNew.getId()).replaceFirst("private", "public");	
						//
						String userName = userProfile.getUserId() ;
						topicNew.setOwner(userName);
						topicNew.setTopicName(topicTitle);
						topicNew.setCreatedDate(new Date());
						topicNew.setModifiedBy(userName);
						topicNew.setModifiedDate(new Date());
						topicNew.setLastPostBy(userName);
						topicNew.setLastPostDate(new Date());
						topicNew.setDescription(message);
						topicNew.setTopicType(topicType);
						topicNew.setLink(link);
						if(whenNewPost){
							String email = userProfile.getEmail();
							if(ForumUtils.isEmpty(email)){
								try {
									email = UserHelper.getUserByUserId(userName).getEmail();
                } catch (Exception e) {}
								if(ForumUtils.isEmpty(email)){
									email = "true";
								}
							}
							topicNew.setIsNotifyWhenAddPost(email);
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
						String[]canPosts = ForumUtils.splitForForum(canPost) ;
						String[]canViews = ForumUtils.splitForForum(canView) ;
						
						topicNew.setCanView(canViews);
						topicNew.setCanPost(canPosts);
						topicNew.setIsApproved(!hasForumMod) ;
						if(!ForumUtils.isEmpty(uiForm.topicId)) {
							topicNew.setId(uiForm.topicId);
							topicNew.setEditReason(editReason) ;
							try {
								uiForm.getForumService().saveTopic(uiForm.categoryId, uiForm.forumId, topicNew, false, false, ForumUtils.getDefaultMail());
								if(uiForm.isDetail){
									forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath((uiForm.categoryId + "/" + uiForm.forumId + "/" + uiForm.topicId)) ;
									UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class) ;
									topicDetail.setIsEditTopic(true) ;
									uiForm.isDetail = false;
								}
							} catch (PathNotFoundException e) {
								forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
								UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
								categoryContainer.updateIsRender(true) ;
								categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ; 
								forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
								forumPortlet.cancelAction() ;
								warning("UITopicForm.msg.forum-deleted") ;
								event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
								uiForm.isDoubleClickSubmit = false;
								return ;						
							}					
						} else {
							topicNew.setVoteRating(0.0) ;
							topicNew.setUserVoteRating(new String[] {}) ;
							try {
								String remoteAddr = "";
				      	if(forumPortlet.isEnableIPLogging()) {
				      		remoteAddr = ForumUtils.getIPRemoter();
				      	}
								topicNew.setRemoteAddr(remoteAddr);
								uiForm.getForumService().saveTopic(uiForm.categoryId, uiForm.forumId, topicNew, true, false, ForumUtils.getDefaultMail());
								if(userProfile.getIsAutoWatchMyTopics()) {
									List<String> values = new ArrayList<String>();
									values.add(userProfile.getEmail());
									String path = uiForm.categoryId + "/" + uiForm.forumId + "/" + topicNew.getId();
									uiForm.getForumService().addWatch(1, path, values, userName) ;
								}
							} catch (PathNotFoundException e) {
								forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
								UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
								categoryContainer.updateIsRender(true) ;
								categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ; 
								forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
								forumPortlet.cancelAction() ;
								warning("UITopicForm.msg.forum-deleted");
								event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
								uiForm.isDoubleClickSubmit = false;
								return;
							}					
						}
						uiForm.topic = new Topic();
						forumPortlet.cancelAction() ;
						if(isOffend || hasForumMod) {
							if(isOffend) warning("MessagePost.msg.isOffend") ;
							else {
								String[] args = new String[]{"forum", "thread"};
								warning("MessageThread.msg.isModerate", args);
							}
						}
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					} else {
						if(k == 0) {
							String[] args = new String[] {uiForm.getLabel(FIELD_TOPICTITLE_INPUT)} ;
							if(t <= 0) args = new String[] { uiForm.getLabel(FIELD_TOPICTITLE_INPUT) + " and " + uiForm.getLabel(FIELD_MESSAGECONTENT)} ;
							warning("NameValidator.msg.ShortText", args) ;
							uiForm.isDoubleClickSubmit = false;
						} else if(t <= 0) {
							warning("NameValidator.msg.ShortMessage", new String[]{"Message"}) ;
							uiForm.isDoubleClickSubmit = false;
						}
					}
				} else {
					forumPortlet.cancelAction() ;
					UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
					topicContainer.setUpdateForum(uiForm.categoryId, uiForm.forum, 0);
					warning("UITopicForm.msg.no-permission") ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
					uiForm.isDoubleClickSubmit = false;
					return;
				}
			} catch (Exception e) {
				forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
				UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
				categoryContainer.updateIsRender(true) ;
				categoryContainer.getChild(UICategories.class).setIsRenderChild(false) ; 
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(Utils.FORUM_SERVICE);
				forumPortlet.cancelAction() ;
				warning("UITopicForm.msg.forum-deleted") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      }
		}
	}
	
	static public class AttachmentActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIAttachFileForm attachFileForm = uiForm.openPopup(popupContainer, UIAttachFileForm.class, 500, 0) ;
			attachFileForm.updateIsTopicForm(true) ;
			attachFileForm.setMaxField(5);
		}
	}
	
	static public class RemoveAttachmentActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm uiTopicForm = event.getSource() ;
			String attFileId = event.getRequestContext().getRequestParameter(OBJECTID);
			for (ForumAttachment att : uiTopicForm.attachments_) {
				if (att.getId().equals(attFileId)) {
					uiTopicForm.removeFromUploadFileList(att);
					uiTopicForm.attachments_.remove(att) ;
					break ;
				}
			}
			uiTopicForm.refreshUploadFileList() ;
		}
	}
	
	static	public class AddTypeTopicActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm topicForm = event.getSource();
			UIPopupContainer popupContainer = topicForm.getAncestorOfType(UIPopupContainer.class) ;
			topicForm.openPopup(popupContainer, UIAddTopicTypeForm.class, "AddTopicTypeForm", 700, 0);
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
	
	static	public class AddValuesUserActionListener extends BaseEventListener<UITopicForm> {
		public void onEvent(Event<UITopicForm> event, UITopicForm uiTopicForm, String objctId) throws Exception {
			String[]array = objctId.split("/") ;
			String childId = array[0] ;
			if(!ForumUtils.isEmpty(childId)) {
				UIForumPortlet forumPortlet = uiTopicForm.getAncestorOfType(UIForumPortlet.class);
				UIPopupAction popupAction1 = forumPortlet.getChild(UIPopupAction.class);
				org.exoplatform.webui.core.UIPopupContainer popupContainer1 = popupAction1.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
				if(popupContainer1 != null){
					UIPopupWindow popupWindow = popupContainer1.findFirstComponentOfType(UIPopupWindow.class);
					popupWindow.setShow(false);
					popupWindow.setUIComponent(null);
					popupAction1.removeChild(org.exoplatform.webui.core.UIPopupContainer.class);
					event.getRequestContext().addUIComponentToUpdateByAjax(popupAction1) ;
				}
				UIPopupContainer popupContainer = uiTopicForm.getAncestorOfType(UIPopupContainer.class) ;
				UIGroupSelector uiGroupSelector = null ;
				if(array[1].equals("1")){
					uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "UIMemberShipSelector", 600, 0) ;
				}	else if(array[1].equals("2")) {
					uiGroupSelector = openPopup(popupContainer, UIGroupSelector.class, "GroupSelector", 600, 0) ;
				}
				uiGroupSelector.setType(array[1]) ;
				uiGroupSelector.setSelectedGroups(null) ;
				uiGroupSelector.setComponent(uiTopicForm, new String[]{childId}) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}

	static  public class CloseActionListener extends EventListener<UIUserSelector> {
    public void execute(Event<UIUserSelector> event) throws Exception {
      UIUserSelector uiUserSelector = event.getSource() ;
      UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent() ;
      uiPoupPopupWindow.setUIComponent(null);
			uiPoupPopupWindow.setShow(false);
			UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class) ;
  		UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class) ;
  		popupAction.removeChild(org.exoplatform.webui.core.UIPopupContainer.class);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
	private void setValueField(UIForumInputWithActions withActions, String field, String values) throws Exception {
		try {
			UIFormTextAreaInput textArea = withActions.getUIFormTextAreaInput(field);
			String vls = textArea.getValue();
			if(!ForumUtils.isEmpty(vls)) {
				values = values + "," + vls;
				values = ForumUtils.removeStringResemble(values.replaceAll(",,", ","));
			}
			textArea.setValue(values);
    } catch (Exception e) {e.printStackTrace();}
	}
	
  static  public class AddActionListener extends EventListener<UIUserSelector> {
  	public void execute(Event<UIUserSelector> event) throws Exception {
  		UIUserSelector uiUserSelector = event.getSource() ;
  		String values = uiUserSelector.getSelectedUsers();
  		UIForumPortlet forumPortlet = uiUserSelector.getAncestorOfType(UIForumPortlet.class) ;
  		UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class);
  		UITopicForm topicForm = popupAction.findFirstComponentOfType(UITopicForm.class);
  		UIPopupWindow uiPoupPopupWindow = uiUserSelector.getParent();
  		org.exoplatform.webui.core.UIPopupContainer uiContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
  		String id = uiContainer.getId();
  		if(topicForm != null){
	  		UIForumInputWithActions catDetail = topicForm.getChildById(FIELD_THREADPERMISSION_TAB);
				if(id.indexOf(FIELD_CANVIEW_INPUT) > 0){
					topicForm.setValueField(catDetail, FIELD_CANVIEW_INPUT, values);
	  		}else if(id.indexOf(FIELD_CANPOST_INPUT) > 0){
					topicForm.setValueField(catDetail, FIELD_CANPOST_INPUT, values);
				} 
  		}
  		uiPoupPopupWindow.setUIComponent(null);
			uiPoupPopupWindow.setShow(false);
			popupAction.removeChildById(id);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
  	}
  }
  
	static	public class AddUserActionListener extends EventListener<UITopicForm> {
		public void execute(Event<UITopicForm> event) throws Exception {
			UITopicForm topicForm = event.getSource() ;
			String id = "PopupContainer"+event.getRequestContext().getRequestParameter(OBJECTID).replace("/0", "")	;
			UIForumPortlet forumPortlet = topicForm.getAncestorOfType(UIForumPortlet.class) ;
			UIPopupAction popupAction = forumPortlet.getChild(UIPopupAction.class).setRendered(true) ;
			org.exoplatform.webui.core.UIPopupContainer uiPopupContainer = popupAction.getChild(org.exoplatform.webui.core.UIPopupContainer.class);
			if(uiPopupContainer == null)uiPopupContainer = popupAction.addChild(org.exoplatform.webui.core.UIPopupContainer.class, null, null);
			uiPopupContainer.setId(id);
			UIPopupWindow uiPopupWindow = uiPopupContainer.getChildById("UITopicUserPopupWindow");
			if(uiPopupWindow == null)uiPopupWindow = uiPopupContainer.addChild(UIPopupWindow.class, "UITopicUserPopupWindow", "UITopicUserPopupWindow") ;
			UIUserSelector uiUserSelector = uiPopupContainer.createUIComponent(UIUserSelector.class, null, null);
			uiUserSelector.setShowSearch(true);
			uiUserSelector.setShowSearchUser(true);
			uiUserSelector.setShowSearchGroup(false);
			uiPopupWindow.setUIComponent(uiUserSelector);
			uiPopupWindow.setShow(true);
			uiPopupWindow.setWindowSize(740, 400);
			uiPopupContainer.setRendered(true);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
			event.getRequestContext().addUIComponentToUpdateByAjax(topicForm);
		}
	}

	public boolean isMod() {
		return isMod;
	}

	public void setMod(boolean isMod) {
		this.isMod = isMod;
	}
}