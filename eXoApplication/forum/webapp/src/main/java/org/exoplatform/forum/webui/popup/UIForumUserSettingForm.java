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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIFormSelectBoxForum;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
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
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
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
		template = "app:/templates/forum/webui/popup/UIFormInputWithActions.gtmpl",
		events = {
			@EventConfig(listeners = UIForumUserSettingForm.SaveActionListener.class), 
			@EventConfig(listeners = UIForumUserSettingForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIForumUserSettingForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_USERPROFILE_FORM = "ForumUserProfile" ;
	public static final String FIELD_USEROPTION_FORM = "ForumUserOption" ;
	
	public static final String FIELD_TIMEZONE_SELECTBOX = "TimeZone" ;
	public static final String FIELD_SHORTDATEFORMAT_SELECTBOX = "ShortDateformat" ;
	public static final String FIELD_LONGDATEFORMAT_SELECTBOX = "LongDateformat" ;
	public static final String FIELD_TIMEFORMAT_SELECTBOX = "Timeformat" ;
	public static final String FIELD_MAXTOPICS_SELECTBOX = "MaximumThreads" ;
	public static final String FIELD_MAXPOSTS_SELECTBOX = "MaximumPosts" ;
	public static final String FIELD_FORUMJUMP_CHECKBOX = "ShowForumJump" ;
	public static final String FIELD_TIMEZONE = "timeZone" ;
	
	public static final String FIELD_USERID_INPUT = "ForumUserName" ;
	public static final String FIELD_USERTITLE_INPUT = "ForumUserTitle" ;
	public static final String FIELD_SIGNATURE_TEXTAREA = "Signature" ;
	public static final String FIELD_ISDISPLAYSIGNATURE_CHECKBOX = "IsDisplaySignature" ;
	public static final String FIELD_ISDISPLAYAVATAR_CHECKBOX = "IsDisplayAvatar" ;
	
	
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private UserProfile userProfile = null ;
	private String[] permissionUser = null;
	
	public UIForumUserSettingForm() throws Exception {
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
		ResourceBundle res = context.getApplicationResourceBundle() ;
		permissionUser = new String[]{res.getString("UIForumPortlet.label.PermissionAdmin").toLowerCase(), 
																	res.getString("UIForumPortlet.label.PermissionModerator").toLowerCase(),
																	res.getString("UIForumPortlet.label.PermissionGuest").toLowerCase(),
																	res.getString("UIForumPortlet.label.PermissionUser").toLowerCase()};
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private void initForumOption() throws Exception {
		try {
			this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		} catch (Exception e) {
			String userName = ForumSessionUtils.getCurrentUser() ;
			this.userProfile = forumService.getUserProfile(ForumSessionUtils.getSystemProvider(), userName, true, false, false) ;
		}
		
		List<SelectItemOption<String>> list ;
		String []timeZone1 = getLabel(FIELD_TIMEZONE).split("/") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		for(String string : timeZone1) {
			list.add(new SelectItemOption<String>(string + "/timeZone", ForumUtils.getTimeZoneNumberInString(string))) ;
		}
		UIFormSelectBoxForum timeZone = new UIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list) ;
		double timeZoneOld = -userProfile.getTimeZone() ;
		Date date = getNewDate(timeZoneOld) ;
		String mark = "-";
		if(timeZoneOld < 0) {
			timeZoneOld = -timeZoneOld ;
		} else if(timeZoneOld > 0){
			mark = "+" ;
		} else {
			timeZoneOld = 0.0 ;
			mark = "";
		}
		timeZone.setValue(mark + timeZoneOld + "0");
		list = new ArrayList<SelectItemOption<String>>() ;
		String []format = new String[] {"M-d-yyyy", "M-d-yy", "MM-dd-yy", "MM-dd-yyyy","yyyy-MM-dd", "yy-MM-dd", "dd-MM-yyyy", "dd-MM-yy",
				"M/d/yyyy", "M/d/yy", "MM/dd/yy", "MM/dd/yyyy","yyyy/MM/dd", "yy/MM/dd", "dd/MM/yyyy", "dd/MM/yy"} ;
		for (String frm : format) {
			list.add(new SelectItemOption<String>((frm.toLowerCase() +" ("	+ ForumUtils.getFormatDate(frm, date)+")"), frm)) ;
		}

		UIFormSelectBox shortdateFormat = new UIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX, FIELD_SHORTDATEFORMAT_SELECTBOX, list) ;
		shortdateFormat.setValue(userProfile.getShortDateFormat());
		list = new ArrayList<SelectItemOption<String>>() ;
		format = new String[] {"DDD,MMMM dd,yyyy", "DDDD,MMMM dd,yyyy", "DDDD,dd MMMM,yyyy", "DDD,MMM dd,yyyy", "DDDD,MMM dd,yyyy", "DDDD,dd MMM,yyyy",
				 								"MMMM dd,yyyy", "dd MMMM,yyyy","MMM dd,yyyy", "dd MMM,yyyy"} ;
		for (String idFrm : format) {
			list.add(new SelectItemOption<String>((idFrm.toLowerCase() +" (" + ForumUtils.getFormatDate(idFrm, date)+")"), idFrm.replaceFirst(" ", "="))) ;
		}
	
		UIFormSelectBox longDateFormat = new UIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX, FIELD_LONGDATEFORMAT_SELECTBOX, list) ;
		longDateFormat.setValue(userProfile.getLongDateFormat().replaceFirst(" ", "="));
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("12-hour","hh:mm=a")) ;
		list.add(new SelectItemOption<String>("24-hour","HH:mm")) ;
		UIFormSelectBox timeFormat = new UIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX, FIELD_TIMEFORMAT_SELECTBOX, list) ;
		timeFormat.setValue(userProfile.getTimeFormat().replace(' ', '='));
		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 45; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i),("id" + i))) ;
		}
		UIFormSelectBox maximumThreads = new UIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX, FIELD_MAXTOPICS_SELECTBOX, list) ;
		maximumThreads.setValue("id" + userProfile.getMaxTopicInPage());
		list = new ArrayList<SelectItemOption<String>>() ;
		for(int i=5; i <= 35; i = i + 5) {
			list.add(new SelectItemOption<String>(String.valueOf(i), ("id" + i))) ;
		}
	
		UIFormSelectBox maximumPosts = new UIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX, FIELD_MAXPOSTS_SELECTBOX, list) ;
		maximumPosts.setValue("id" + userProfile.getMaxPostInPage());
		boolean isJump = userProfile.getIsShowForumJump() ;
		UIFormCheckBoxInput isShowForumJump = new UIFormCheckBoxInput<Boolean>(FIELD_FORUMJUMP_CHECKBOX, FIELD_FORUMJUMP_CHECKBOX, isJump);
		isShowForumJump.setChecked(isJump) ;

		UIFormStringInput userId = new UIFormStringInput(FIELD_USERID_INPUT, FIELD_USERID_INPUT, null);
		userId.setValue(this.userProfile.getUserId());
		userId.setEditable(false) ;
		userId.setEnable(false);
		UIFormStringInput userTitle = new UIFormStringInput(FIELD_USERTITLE_INPUT, FIELD_USERTITLE_INPUT, null);
		userTitle.setValue(this.userProfile.getUserTitle());
		if(this.userProfile.getUserRole() > 0) {
			userTitle.setEditable(false) ;
			userTitle.setEnable(false);
		}
		UIFormTextAreaInput signature = new UIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA, FIELD_SIGNATURE_TEXTAREA, null);
		signature.setValue(this.userProfile.getSignature());
		UIFormCheckBoxInput isDisplaySignature = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYSIGNATURE_CHECKBOX, FIELD_ISDISPLAYSIGNATURE_CHECKBOX, false);
		isDisplaySignature.setChecked(this.userProfile.getIsDisplaySignature()) ;
		UIFormCheckBoxInput isDisplayAvatar = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYAVATAR_CHECKBOX, FIELD_ISDISPLAYAVATAR_CHECKBOX, false);
		isDisplayAvatar.setChecked(this.userProfile.getIsDisplayAvatar()) ;
		
		UIFormInputWithActions inputSetProfile = new UIFormInputWithActions(FIELD_USERPROFILE_FORM);
		inputSetProfile.addUIFormInput(userId) ;
		inputSetProfile.addUIFormInput(userTitle) ;
		inputSetProfile.addUIFormInput(signature) ;
		inputSetProfile.addUIFormInput(isDisplaySignature) ;
		inputSetProfile.addUIFormInput(isDisplayAvatar) ;
		
		UIFormInputWithActions inputSetOption = new UIFormInputWithActions(FIELD_USEROPTION_FORM); 
		inputSetOption.addUIFormInput(timeZone) ;
		inputSetOption.addUIFormInput(shortdateFormat) ;
		inputSetOption.addUIFormInput(longDateFormat) ;
		inputSetOption.addUIFormInput(timeFormat) ;
		inputSetOption.addUIFormInput(maximumThreads) ;
		inputSetOption.addUIFormInput(maximumPosts) ;
		inputSetOption.addUIFormInput(isShowForumJump) ;

		addUIFormInput(inputSetProfile);
		addUIFormInput(inputSetOption);
	}
	
	@SuppressWarnings("deprecation")
	private Date getNewDate(double timeZoneOld) {
		Calendar	calendar = GregorianCalendar.getInstance() ;
		calendar.setLenient(false) ;
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset + (long)(timeZoneOld*3600000)) ; 
		return calendar.getTime() ;
	}
	
	public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
		return	findComponentById(name) ;
	}
	
	public void activate() throws Exception {
		initForumOption() ;
	}
	public void deActivate() throws Exception {
	}
	
	static	public class SaveActionListener extends EventListener<UIForumUserSettingForm> {
		public void execute(Event<UIForumUserSettingForm> event) throws Exception {
			UIForumUserSettingForm uiForm = event.getSource() ;
			UIFormInputWithActions inputSetProfile = uiForm.getChildById(FIELD_USERPROFILE_FORM) ;
			String userTitle = inputSetProfile.getUIStringInput(FIELD_USERTITLE_INPUT).getValue() ;
			UserProfile userProfile = uiForm.userProfile ;
			if(ForumUtils.isEmpty(userTitle) || 
					(!userTitle.equals(userProfile.getUserTitle()) && Arrays.asList(uiForm.permissionUser).contains(userTitle.toLowerCase()))) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIForumUserSettingForm.msg.UserTitleInvalid", new Object[]{}, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return;
			}
			int maxText = ForumUtils.MAXSIGNATURE ;
			String signature = inputSetProfile.getUIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA).getValue() ;
			if(!ForumUtils.isEmpty(signature) && signature.length() > maxText) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				Object[] args = { uiForm.getLabel(FIELD_SIGNATURE_TEXTAREA), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("UIForumUserSettingForm.msg.UserTitleInvalid", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			signature = ForumTransformHTML.enCodeHTML(signature);
			boolean isDisplaySignature = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_ISDISPLAYSIGNATURE_CHECKBOX).getValue() ;
			Boolean isDisplayAvatar = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_ISDISPLAYAVATAR_CHECKBOX).getValue() ;
			
			UIFormInputWithActions inputSetOption = uiForm.getChildById(FIELD_USEROPTION_FORM) ;
			long maxTopic = Long.parseLong(inputSetOption.getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).getValue().substring(2)) ;
			long maxPost = Long.parseLong(inputSetOption.getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).getValue().substring(2)) ;
			double timeZone = Double.parseDouble(uiForm.getUIFormSelectBoxForum(FIELD_TIMEZONE_SELECTBOX).getValue());
			String shortDateFormat = inputSetOption.getUIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX).getValue();
			String longDateFormat = inputSetOption.getUIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX).getValue();
			String timeFormat = inputSetOption.getUIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX).getValue();
			boolean isJump = (Boolean)inputSetOption.getUIFormCheckBoxInput(FIELD_FORUMJUMP_CHECKBOX).getValue() ;
			
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			userProfile.setUserTitle(userTitle);
			userProfile.setSignature(signature);
			userProfile.setIsDisplaySignature(isDisplaySignature);
			userProfile.setIsDisplayAvatar(isDisplayAvatar);
			userProfile.setTimeZone(-timeZone) ;
			userProfile.setTimeFormat(timeFormat.replace('=', ' '));
			userProfile.setShortDateFormat(shortDateFormat);
			userProfile.setLongDateFormat(longDateFormat.replace('=', ' '));
			userProfile.setMaxPostInPage(maxPost);
			userProfile.setMaxTopicInPage(maxTopic);
			userProfile.setIsShowForumJump(isJump);
			uiForm.forumService.saveUserProfile(ForumSessionUtils.getSystemProvider(), userProfile, true, false);
			forumPortlet.setUserProfile() ;
			forumPortlet.cancelAction() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
		}
	}
	
	static	public class CancelActionListener extends EventListener<UIForumUserSettingForm> {
		public void execute(Event<UIForumUserSettingForm> event) throws Exception {
			UIForumUserSettingForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.cancelAction() ;
		}
	}
}
