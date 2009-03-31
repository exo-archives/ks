/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIFormWYSIWYGInput;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * May 9, 2008 - 8:19:24 AM	
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIPrivateMessegeForm.gtmpl",
		events = {
			@EventConfig(listeners = UIPrivateMessageForm.CloseActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIPrivateMessageForm.SendPrivateMessageActionListener.class),
			@EventConfig(listeners = UIPrivateMessageForm.AddValuesUserActionListener.class, phase=Phase.DECODE),
			@EventConfig(listeners = UIPrivateMessageForm.SelectTabActionListener.class, phase=Phase.DECODE)
		}
)
public class UIPrivateMessageForm extends UIForm implements UIPopupComponent, UISelector {
	private ForumService forumService ;
	private UserProfile userProfile ;
	private String userName ;
	private int id = 0;
	private boolean fullMessage = true;
	public static final String FIELD_SENDTO_TEXTAREA = "SendTo" ;
	public static final String FIELD_MAILTITLE_INPUT = "MailTitle" ;
	public static final String FIELD_MAILMESSAGE_INPUT = "MailMessage" ;
	public static final String FIELD_SENDMESSAGE_TAB = "MessageTab" ;
	public static final String FIELD_REPLY_LABEL = "Reply" ;
	public static final String FIELD_FORWARD_LABEL = "Forward" ;
	public UIPrivateMessageForm() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		UIFormTextAreaInput SendTo = new UIFormTextAreaInput(FIELD_SENDTO_TEXTAREA, FIELD_SENDTO_TEXTAREA, null);
		SendTo.addValidator(MandatoryValidator.class);
		UIFormStringInput MailTitle = new UIFormStringInput(FIELD_MAILTITLE_INPUT, FIELD_MAILTITLE_INPUT, null);
		MailTitle.addValidator(MandatoryValidator.class);
		UIFormWYSIWYGInput formWYSIWYGInput = new UIFormWYSIWYGInput(FIELD_MAILMESSAGE_INPUT, null, null, true);
		UIFormInputWithActions sendMessageTab = new UIFormInputWithActions(FIELD_SENDMESSAGE_TAB);
		sendMessageTab.addUIFormInput(SendTo);
		sendMessageTab.addUIFormInput(MailTitle);
		sendMessageTab.addUIFormInput(formWYSIWYGInput);
		
		String[]strings = new String[] {"SelectUser", "SelectMemberShip", "SelectGroup"}; 
		ActionData ad ;int i = 0;
		List<ActionData> actions = new ArrayList<ActionData>() ;
		for(String string : strings) {
			ad = new ActionData() ;
			ad.setActionListener("AddValuesUser") ;
			ad.setActionParameter(String.valueOf(i)) ;
			ad.setCssIconClass(string + "Icon") ;
			ad.setActionName(string);
			actions.add(ad) ;
			++i;
		}
		sendMessageTab.setActionField(FIELD_SENDTO_TEXTAREA, actions);
		addUIFormInput(sendMessageTab) ;
		addChild(UIListInBoxPrivateMessage.class, null, null) ;
		addChild(UIListSentPrivateMessage.class, null, null) ;
	}
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setUserProfile(UserProfile userProfile){
		this.userProfile = userProfile ;
		this.userName = userProfile.getUserId() ;
	}
	@SuppressWarnings("unused")
	private UserProfile getUserProfile() throws Exception {
		return this.userProfile;
	}

	public void setSendtoField(String str) {
		this.getUIFormTextAreaInput(FIELD_SENDTO_TEXTAREA).setValue(str) ;
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
	
	@SuppressWarnings("unused")
	private int getIsSelected() {
		return this.id ;
	}
	
	static	public class SelectTabActionListener extends EventListener<UIPrivateMessageForm> {
		public void execute(Event<UIPrivateMessageForm> event) throws Exception {
			String id = event.getRequestContext().getRequestParameter(OBJECTID)	;
			UIPrivateMessageForm messageForm = event.getSource() ; 
			messageForm.id = Integer.parseInt(id) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(messageForm) ;
		}
	}
	
	static	public class SendPrivateMessageActionListener extends EventListener<UIPrivateMessageForm> {
		public void execute(Event<UIPrivateMessageForm> event) throws Exception {
			UIPrivateMessageForm messageForm = event.getSource() ; 
			UIFormInputWithActions MessageTab = messageForm.getChildById(FIELD_SENDMESSAGE_TAB);
			UIFormTextAreaInput areaInput = messageForm.getUIFormTextAreaInput(FIELD_SENDTO_TEXTAREA) ;
			UIApplication uiApp = messageForm.getAncestorOfType(UIApplication.class) ;
			String sendTo = areaInput.getValue() ;
			sendTo = ForumUtils.removeSpaceInString(sendTo) ;
			sendTo = ForumUtils.removeStringResemble(sendTo) ;
			String erroUser = ForumSessionUtils.checkValueUser(sendTo) ;
			if(!ForumUtils.isEmpty(erroUser)) {
				Object[] args = { messageForm.getLabel(FIELD_SENDTO_TEXTAREA), erroUser };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.erroUser-input", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			UIFormStringInput stringInput = MessageTab.getUIStringInput(FIELD_MAILTITLE_INPUT);
			String mailTitle = stringInput.getValue() ;
			int maxText = 80 ;
			if(mailTitle.length() > maxText) {
				Object[] args = { messageForm.getLabel(FIELD_MAILTITLE_INPUT), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			mailTitle = ForumTransformHTML.enCodeHTML(mailTitle);
			UIFormWYSIWYGInput formWYSIWYGInput = MessageTab.getChild(UIFormWYSIWYGInput.class) ;
			String message = formWYSIWYGInput.getValue();
			if(!ForumUtils.isEmpty(message)) {
				ForumPrivateMessage privateMessage = new ForumPrivateMessage() ;
				privateMessage.setFrom(messageForm.userName) ;
				privateMessage.setSendTo(sendTo) ;
				privateMessage.setName(mailTitle) ;
				privateMessage.setMessage(message) ;
				SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
				try {
					messageForm.forumService.savePrivateMessage(sProvider, privateMessage) ;
				} finally {
					sProvider.close();
				}
				areaInput.setValue("") ;
				stringInput.setValue("") ;
				formWYSIWYGInput.setValue("") ;
				Object[] args = { "" };
				uiApp.addMessage(new ApplicationMessage("UIPrivateMessageForm.msg.sent-successfully", args, ApplicationMessage.INFO)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				if(messageForm.fullMessage){
					messageForm.id = 1;
					event.getRequestContext().addUIComponentToUpdateByAjax(messageForm.getParent()) ;
				} else {
					UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
					forumPortlet.cancelAction() ;
				}
			} else {
				Object[] args = { messageForm.getLabel(FIELD_MAILMESSAGE_INPUT) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.empty-input", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
			}
		}
	}
	
	static	public class AddValuesUserActionListener extends EventListener<UIPrivateMessageForm> {
		public void execute(Event<UIPrivateMessageForm> event) throws Exception {
			UIPrivateMessageForm messageForm = event.getSource() ;
			String type = event.getRequestContext().getRequestParameter(OBJECTID)	;
			if(!ForumUtils.isEmpty(type)) {
				UIPopupContainer popupContainer = messageForm.getAncestorOfType(UIPopupContainer.class) ;
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
				UIGroupSelector uiGroupSelector = popupAction.activate(UIGroupSelector.class, 500) ;
				if(type.equals("0")) uiGroupSelector.setId("UIUserSelector");
				else if(type.equals("1")) uiGroupSelector.setId("UIMemberShipSelector");
				uiGroupSelector.setType(type) ;
				uiGroupSelector.setSelectedGroups(null) ;
				uiGroupSelector.setComponent(messageForm, new String[]{FIELD_SENDTO_TEXTAREA}) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}
	
	public void setUpdate(ForumPrivateMessage privateMessage, boolean isReply) throws Exception {
		UIFormInputWithActions MessageTab = this.getChildById(FIELD_SENDMESSAGE_TAB);
		UIFormStringInput stringInput = MessageTab.getUIStringInput(FIELD_MAILTITLE_INPUT);
		UIFormWYSIWYGInput message = MessageTab.getChild(UIFormWYSIWYGInput.class);
		String content = privateMessage.getMessage() ;
		String label = this.getLabel(FIELD_REPLY_LABEL) ;
		String title = privateMessage.getName() ;
		if(isReply) {
			UIFormTextAreaInput areaInput = this.getUIFormTextAreaInput(FIELD_SENDTO_TEXTAREA) ;
			areaInput.setValue(privateMessage.getFrom()) ;
			if(title.indexOf(label) < 0) {
				title = label + ": " + title ;
			} 
			stringInput.setValue(title) ;
			content = "<br/><br/><br/><div style=\"padding: 5px; border-left:solid 2px blue;\">" + content + "</div>" ;
			message.setValue(content) ;
		} else {
			label = this.getLabel(FIELD_FORWARD_LABEL) ;
			if(title.indexOf(label) < 0) {
				title = label + ": " + title ;
			} 
			stringInput.setValue(title) ;
		}
		message.setValue(content) ;
		this.id = 2;
	}
	
	static	public class CloseActionListener extends EventListener<UIPrivateMessageForm> {
		public void execute(Event<UIPrivateMessageForm> event) throws Exception {
			UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}

	public boolean isFullMessage() {
		return fullMessage;
	}
	public void setFullMessage(boolean fullMessage) {
		this.fullMessage = fullMessage;
	}
}
