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
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIFormTextAreaMultilInput;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIModeratorsManagementForm.gtmpl",
    events = {
      @EventConfig(listeners = UIModeratorManagementForm.ViewProfileActionListener.class), 
      @EventConfig(listeners = UIModeratorManagementForm.EditProfileActionListener.class), 
      @EventConfig(listeners = UIModeratorManagementForm.SaveActionListener.class), 
      @EventConfig(listeners = UIModeratorManagementForm.AddValuesAreaActionListener.class), 
      @EventConfig(listeners = UIModeratorManagementForm.CloseActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIModeratorManagementForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIModeratorManagementForm extends UIForm implements UIPopupComponent {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private List<UserProfile> userProfiles = new ArrayList<UserProfile>();
  private JCRPageList pageList ;
	private boolean isEdit = false ;
	private UserProfile userProfile = new UserProfile();
	private List<ForumLinkData> forumLinks = null;
	
	public static final String FIELD_USERPROFILE_FORM = "ForumUserProfile" ;
	public static final String FIELD_USEROPTION_FORM = "ForumUserOption" ;
	public static final String FIELD_USERBAN_FORM = "ForumUserBan" ;
	
	public static final String FIELD_USERID_INPUT = "ForumUserName" ;
	public static final String FIELD_USERTITLE_INPUT = "ForumUserTitle" ;
	public static final String FIELD_USERROLE_SELECTBOX = "UserRole" ;
	public static final String FIELD_SIGNATURE_TEXTAREA = "Signature" ;
	public static final String FIELD_ISDISPLAYSIGNATURE_CHECKBOX = "IsDisplaySignature" ;
	public static final String FIELD_MODERATEFORUMS_MULTIVALUE = "ModForums" ;
	public static final String FIELD_MODERATETOPICS_MULTIVALUE = "MosTopics" ;
	public static final String FIELD_ISDISPLAYAVATAR_CHECKBOX = "IsDisplayAvatar" ;
	
	public static final String FIELD_TIMEZONE_SELECTBOX = "TimeZone" ;
	public static final String FIELD_SHORTDATEFORMAT_SELECTBOX = "ShortDateformat" ;
	public static final String FIELD_LONGDATEFORMAT_SELECTBOX = "LongDateformat" ;
	public static final String FIELD_TIMEFORMAT_SELECTBOX = "Timeformat" ;
	public static final String FIELD_MAXTOPICS_SELECTBOX = "MaximumThreads" ;
	public static final String FIELD_MAXPOSTS_SELECTBOX = "MaximumPosts" ;
	public static final String FIELD_FORUMJUMP_CHECKBOX = "ShowForumJump" ;
	public static final String FIELD_TIMEZONE = "timeZone" ;
	
	public static final String FIELD_ISBANNED_CHECKBOX = "IsBanned" ;
	public static final String FIELD_BANUNTIL_SELECTBOX = "BanUntil" ;
	public static final String FIELD_BANREASON_TEXTAREA = "BanReason" ;
	public static final String FIELD_BANCOUNTER_INPUT = "BanCounter" ;
	public static final String FIELD_BANREASONSUMMARY_MULTIVALUE = "BanReasonSummary" ;
	public static final String FIELD_CREATEDDATEBAN_INPUT = "CreatedDateBan" ;

	public UIModeratorManagementForm() throws Exception {
		addChild(UIForumPageIterator.class, null, "ForumUserPageIterator") ;
  }
	
  @SuppressWarnings("unused")
  public JCRPageList setPageListUserProfile() throws Exception {
    List<User> listUser = ForumSessionUtils.getAllUser() ;
    for (User user : listUser) {
      UserProfile userProfile = this.forumService.getUserProfile(ForumSessionUtils.getSystemProvider(), user.getUserName(), true, true) ;
    }
  	this.pageList = this.forumService.getPageListUserProfile(ForumSessionUtils.getSystemProvider()) ;
  	this.pageList.setPageSize(10);
  	this.getChild(UIForumPageIterator.class).updatePageList(this.pageList) ;
  	this.setListProFileUser() ;
  	return this.pageList;
  }
  
  @SuppressWarnings("unused")
  private List<UserProfile> getListProFileUser() throws Exception {
  	return this.userProfiles ;
  }
  
  @SuppressWarnings("unchecked")
  private void setListProFileUser() throws Exception {
  	List<User> listUser = ForumSessionUtils.getAllUser() ;
  	List<UserProfile> listUserProfile = new ArrayList<UserProfile>() ;
  	this.userProfiles = new ArrayList<UserProfile>();
  	long page = this.getChild(UIForumPageIterator.class).getPageSelected() ;
  	listUserProfile = this.pageList.getPage(page) ;
  	for (User user : listUser) {
  		for (UserProfile userProfile : listUserProfile) {
	      if(user.getUserName().equals(userProfile.getUserId())) {
		  		userProfile.setUser(user);
		  		userProfile.setLastLoginDate(user.getLastLoginTime());
		  		this.userProfiles.add(userProfile);
		  		break ;
	      }
      }
    }
  }
  
  private UserProfile getUserProfile(String userId) throws Exception {
  	for (UserProfile userProfile : this.userProfiles) {
	    if(userProfile.getUserId().equals(userId)) return userProfile ;
    }
  	UserProfile userProfile = new UserProfile() ;
  	userProfile.setUserId(userId) ;
  	return userProfile ;
  }
  
  public UserProfile getProfile() {
	  return this.userProfile ;
  }
  public void activate() throws Exception {}
	public void deActivate() throws Exception {}
  
	@SuppressWarnings("unused")
  private boolean getIsEdit() {
		return this.isEdit ;
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
  private void initUserProfileForm() throws Exception {
		this.setForumLinks();
		List<SelectItemOption<String>> list ;
		UIFormStringInput userId = new UIFormStringInput(FIELD_USERID_INPUT, FIELD_USERID_INPUT, null);
		userId.setValue(this.userProfile.getUserId());
		userId.setEditable(false) ;
		UIFormStringInput userTitle = new UIFormStringInput(FIELD_USERTITLE_INPUT, FIELD_USERTITLE_INPUT, null);
		userTitle.setValue(this.userProfile.getUserTitle());
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Admin", "id0")) ;
		list.add(new SelectItemOption<String>("Moderator", "id1")) ;
		list.add(new SelectItemOption<String>("User", "id2")) ;
		UIFormSelectBox userRole = new UIFormSelectBox(FIELD_USERROLE_SELECTBOX, FIELD_USERROLE_SELECTBOX, list) ;
		userRole.setValue("id" + this.userProfile.getUserRole());
		UIFormTextAreaInput signature = new UIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA, FIELD_SIGNATURE_TEXTAREA, null);
		signature.setValue(this.userProfile.getSignature());
		UIFormCheckBoxInput isDisplaySignature = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYSIGNATURE_CHECKBOX, FIELD_ISDISPLAYSIGNATURE_CHECKBOX, false);
		isDisplaySignature.setChecked(this.userProfile.getIsDisplaySignature()) ;
		UIFormTextAreaMultilInput moderateForums = new UIFormTextAreaMultilInput(FIELD_MODERATEFORUMS_MULTIVALUE, FIELD_MODERATEFORUMS_MULTIVALUE, null);
		moderateForums.setValue(ForumFormatUtils.unSplitForForum(userProfile.getModerateForums()));
//		UIFormTextAreaMultilInput moderateTopics = new UIFormTextAreaMultilInput(FIELD_MODERATETOPICS_MULTIVALUE, FIELD_MODERATETOPICS_MULTIVALUE, null);
//		moderateTopics.setValue(ForumFormatUtils.unSplitForForum(userProfile.getModerateTopics()));
		UIFormCheckBoxInput isDisplayAvatar = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYAVATAR_CHECKBOX, FIELD_ISDISPLAYAVATAR_CHECKBOX, false);
		isDisplayAvatar.setChecked(this.userProfile.getIsDisplayAvatar()) ;
		//Option
		String []timeZone1 = getLabel(FIELD_TIMEZONE).split("/") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		for(String string : timeZone1) {
			list.add(new SelectItemOption<String>(string, ForumFormatUtils.getTimeZoneNumberInString(string))) ;
		}
		UIFormSelectBox timeZone = new UIFormSelectBox(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list) ;
		double timeZoneOld = userProfile.getTimeZone() ;
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
			list.add(new SelectItemOption<String>((frm.toLowerCase() +" ("  + ForumFormatUtils.getFormatDate(frm, date)+")"), frm)) ;
    }
		UIFormSelectBox shortdateFormat = new UIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX, FIELD_SHORTDATEFORMAT_SELECTBOX, list) ;
		shortdateFormat.setValue(userProfile.getShortDateFormat());
		list = new ArrayList<SelectItemOption<String>>() ;
		format = new String[] {"DDD,MMMM dd,yyyy", "DDDD,MMMM dd,yyyy", "DDDD,dd MMMM,yyyy", "DDD,MMM dd,yyyy", "DDDD,MMM dd,yyyy", "DDDD,dd MMM,yyyy",
				 								"MMMM dd,yyyy", "dd MMMM,yyyy","MMM dd,yyyy", "dd MMM,yyyy"} ;
		for (String idFrm : format) {
			list.add(new SelectItemOption<String>((idFrm.toLowerCase() +" (" + ForumFormatUtils.getFormatDate(idFrm, date)+")"), idFrm.replaceFirst(" ", "="))) ;
		}
		UIFormSelectBox longDateFormat = new UIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX, FIELD_LONGDATEFORMAT_SELECTBOX, list) ;
		longDateFormat.setValue(userProfile.getLongDateFormat());
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("12-hour ("+ForumFormatUtils.getFormatDate("h:mm a", date)+")", "h:mm=a")) ;
		list.add(new SelectItemOption<String>("12-hour ("+ForumFormatUtils.getFormatDate("hh:mm a", date)+")", "hh:mm=a")) ;
		list.add(new SelectItemOption<String>("24-hour ("+ForumFormatUtils.getFormatDate("H:mm", date)+")", "H:mm")) ;
		list.add(new SelectItemOption<String>("24-hour ("+ForumFormatUtils.getFormatDate("HH:mm", date)+")", "HH:mm")) ;
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
		UIFormCheckBoxInput isShowForumJump = new UIFormCheckBoxInput<Boolean>(FIELD_FORUMJUMP_CHECKBOX, FIELD_FORUMJUMP_CHECKBOX, false);
		isShowForumJump.setChecked(isJump);
		//Ban
		UIFormCheckBoxInput isBanned = new UIFormCheckBoxInput<Boolean>(FIELD_ISBANNED_CHECKBOX, FIELD_ISBANNED_CHECKBOX, false);
		boolean isBan = userProfile.getIsBanned() ;
		isBanned.setChecked(isBan) ;
		list = new ArrayList<SelectItemOption<String>>() ;
		String dv = "Days";
		int i = 2;
		long oneDate = 86400000, until ;
		if(isBan){
			until = userProfile.getBanUntil() ;
			date.setTime(until) ;
			list.add(new SelectItemOption<String>("Banned until: " + ForumFormatUtils.getFormatDate(userProfile.getShortDateFormat()+ " hh:mm a", date), ("Until_" + until))) ;
		}
		date = getNewDate(timeZoneOld);
		until = date.getTime() + oneDate;
		date.setTime(until);
		list.add(new SelectItemOption<String>("1 Day ("+ForumFormatUtils.getFormatDate(userProfile.getShortDateFormat()+ " hh:mm a", date)+")", "Until_" + until)) ;
		while(true) {
			if(i == 8 && dv.equals("Days")) i = 10;
			if(i == 11) {i = 2; dv = "Weeks";}
			if(i == 4 && dv.equals("Weeks")) {i = 1; dv = "Month" ;}
			if(i == 2 && dv.equals("Month")){dv = "Months" ;}
			if(i == 7 && dv.equals("Months")){i = 1; dv = "Year" ;}
			if(i == 2 && dv.equals("Year")){dv = "Years" ;}
			if(i == 3 && dv.equals("Years")){break;}
			if(dv.equals("Days")){ date = getNewDate(timeZoneOld); until = date.getTime() + i*oneDate ; date.setTime(until);}
			if(dv.equals("Weeks")){ date = getNewDate(timeZoneOld);until = date.getTime() + i*oneDate*7; date.setTime(until);}
			if(dv.equals("Month")||dv.equals("Months")){ date = getNewDate(timeZoneOld); date.setMonth(date.getMonth() + i) ; until = date.getTime();}
			if(dv.equals("Years")||dv.equals("Year")){ date = getNewDate(timeZoneOld); date.setYear(date.getYear() + i) ; until = date.getTime();}
			list.add(new SelectItemOption<String>(i+" "+dv+" ("+ForumFormatUtils.getFormatDate(userProfile.getShortDateFormat()+ " hh:mm a", date)+")", ("Until_" + until))) ;
			++i;
		}
		UIFormSelectBox banUntil = new UIFormSelectBox(FIELD_BANUNTIL_SELECTBOX,FIELD_BANUNTIL_SELECTBOX, list) ;
		if(isBan) {
			banUntil.setValue("Until_" + userProfile.getBanUntil()) ;
		}
		
		UIFormTextAreaInput banReason = new UIFormTextAreaInput(FIELD_BANREASON_TEXTAREA, FIELD_BANREASON_TEXTAREA, null);
		UIFormStringInput banCounter = new UIFormStringInput(FIELD_BANCOUNTER_INPUT, FIELD_BANCOUNTER_INPUT, null) ;
		banCounter.setValue(userProfile.getBanCounter() + "");
		UIFormTextAreaInput banReasonSummary = new UIFormTextAreaInput(FIELD_BANREASONSUMMARY_MULTIVALUE, FIELD_BANREASONSUMMARY_MULTIVALUE, null);
		banReasonSummary.setValue(ForumFormatUtils.unSplitForForum(userProfile.getBanReasonSummary()));
		UIFormStringInput createdDateBan = new UIFormStringInput(FIELD_CREATEDDATEBAN_INPUT, FIELD_CREATEDDATEBAN_INPUT, null) ;
		if(isBan) {
			banReason.setValue(userProfile.getBanReason());
			createdDateBan.setValue(ForumFormatUtils.getFormatDate("MM/dd/yyyy, hh:mm a",userProfile.getCreatedDateBan()));
		} else {
			banReason.setEnable(true);
		}
		
		UIFormInputWithActions inputSetProfile = new UIFormInputWithActions(FIELD_USERPROFILE_FORM); 
		inputSetProfile.addUIFormInput(userId);
		inputSetProfile.addUIFormInput(userTitle);
		inputSetProfile.addUIFormInput(userRole);
		inputSetProfile.addUIFormInput(signature);
		inputSetProfile.addUIFormInput(isDisplaySignature);
		inputSetProfile.addUIFormInput(moderateForums);
//		inputSetProfile.addUIFormInput(moderateTopics);
		inputSetProfile.addUIFormInput(isDisplayAvatar);
		addUIFormInput(inputSetProfile);
		
		UIFormInputWithActions inputSetOption = new UIFormInputWithActions(FIELD_USEROPTION_FORM); 
		inputSetOption.addUIFormInput(timeZone) ;
		inputSetOption.addUIFormInput(shortdateFormat) ;
		inputSetOption.addUIFormInput(longDateFormat) ;
		inputSetOption.addUIFormInput(timeFormat) ;
		inputSetOption.addUIFormInput(maximumThreads) ;
		inputSetOption.addUIFormInput(maximumPosts) ;
		inputSetOption.addUIFormInput(isShowForumJump) ;
		addUIFormInput(inputSetOption);
		
		UIFormInputWithActions inputSetBan = new UIFormInputWithActions(FIELD_USERBAN_FORM); 
		inputSetBan.addUIFormInput(isBanned);
		inputSetBan.addUIFormInput(banUntil);
		inputSetBan.addUIFormInput(banReason);
		inputSetBan.addUIFormInput(banCounter);
		inputSetBan.addUIFormInput(banReasonSummary);
		inputSetBan.addUIFormInput(createdDateBan);
		addUIFormInput(inputSetBan);
		addChild(UIPageListTopicByUser.class, null, null) ;
		addChild(UIPageListPostByUser.class, null, null) ;
	}
	
	@SuppressWarnings("deprecation")
  private Date getNewDate(double timeZoneOld) {
		Date date = new Date() ;
		long timeZoneMyHost = (long)date.getTimezoneOffset() ;
		if(timeZoneMyHost == 0) {
			date.setTime(date.getTime() + (long)(timeZoneOld*3600000));
		}
		return date ;
	}
	
	private void setForumLinks() throws Exception {
		this.forumLinks = this.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class).getForumLinks() ;
		if(forumLinks.size() <= 0) {
			this.forumService.getAllLink(ForumSessionUtils.getSystemProvider());
		}
	}
	
	@SuppressWarnings("unused")
  private List<ForumLinkData> getForumLinks() throws Exception {
	  return this.forumLinks ;
  }
	
	public void setValuesTextArea(String values) {
		UIFormInputWithActions inputSetProfile = this.getChildById(FIELD_USERPROFILE_FORM) ;
		inputSetProfile.getUIFormTextAreaInput(FIELD_MODERATEFORUMS_MULTIVALUE).setValue(values) ;
  }
	
  static  public class ViewProfileActionListener extends EventListener<UIModeratorManagementForm> {
    public void execute(Event<UIModeratorManagementForm> event) throws Exception {
    	UIModeratorManagementForm uiForm = event.getSource() ;
  		String userId = event.getRequestContext().getRequestParameter(OBJECTID);
  		UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIViewUserProfile viewUserProfile = popupAction.activate(UIViewUserProfile.class, 670) ;
			viewUserProfile.setUserProfile(uiForm.getUserProfile(userId)) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
    }
  }

  static  public class EditProfileActionListener extends EventListener<UIModeratorManagementForm> {
  	public void execute(Event<UIModeratorManagementForm> event) throws Exception {
  		UIModeratorManagementForm uiForm = event.getSource() ;
  		String userId = event.getRequestContext().getRequestParameter(OBJECTID);
  		uiForm.userProfile = uiForm.getUserProfile(userId) ;
	    uiForm.removeChildById("ForumUserProfile") ;
	    uiForm.removeChildById("ForumUserOption") ;
	    uiForm.removeChildById("ForumUserBan") ;
	    uiForm.removeChild(UIPageListTopicByUser.class) ;
			uiForm.initUserProfileForm();
			uiForm.isEdit = true ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
  	}
  }
  
  static  public class CancelActionListener extends EventListener<UIModeratorManagementForm> {
    public void execute(Event<UIModeratorManagementForm> event) throws Exception {
    	UIModeratorManagementForm uiForm = event.getSource();
      uiForm.isEdit = false ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  
  static  public class SaveActionListener extends EventListener<UIModeratorManagementForm> {
    public void execute(Event<UIModeratorManagementForm> event) throws Exception {
    	UIModeratorManagementForm uiForm = event.getSource() ;
    	UserProfile userProfile = uiForm.userProfile ;
    	
    	UIFormInputWithActions inputSetProfile = uiForm.getChildById(FIELD_USERPROFILE_FORM) ;
    	String userTitle = inputSetProfile.getUIStringInput(FIELD_USERTITLE_INPUT).getValue() ;
    	long userRole = Long.parseLong(inputSetProfile.getUIFormSelectBox(FIELD_USERROLE_SELECTBOX).getValue().substring(2));
    	String moderateForum = inputSetProfile.getUIFormTextAreaInput(FIELD_MODERATEFORUMS_MULTIVALUE).getValue() ;
    	String []moderateForums ;
    	if(moderateForum != null && moderateForum.length() > 0) {
    		moderateForums = ForumFormatUtils.splitForForum(moderateForum) ;
    	} else {
    		moderateForums = userProfile.getModerateForums() ;
    	}
//    	String moderateTopic = inputSetProfile.getUIFormTextAreaInput(FIELD_MODERATETOPICS_MULTIVALUE).getValue() ;
//    	String []moderateTopics ;
//    	if(moderateTopic != null && moderateTopic.length() > 0) {
//    		moderateTopics = ForumFormatUtils.splitForForum(moderateTopic) ;
//    	} else {
//    		moderateTopics = userProfile.getModerateTopics() ;
//    	}
    	if(moderateForums.length > 0 || moderateForums.length > 0) {
    		if(userRole >= 2) userRole = 1;
    	}
    	if(userTitle.indexOf("Admin") >= 0 || userTitle.equals("Moderator") || userTitle.equals("User") || userTitle.equals("Guest")) {
    		if(userRole == 0) userTitle = "Administrator" ;
    		if(userRole == 1) userTitle = "Moderator" ;
    		if(userRole == 2) userTitle = "User" ;
    	}
    	String signature = inputSetProfile.getUIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA).getValue() ;
      boolean isDisplaySignature = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_ISDISPLAYSIGNATURE_CHECKBOX).getValue() ;
    	Boolean isDisplayAvatar = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_ISDISPLAYAVATAR_CHECKBOX).getValue() ;
    	
    	UIFormInputWithActions inputSetOption = uiForm.getChildById(FIELD_USEROPTION_FORM) ;
    	double timeZone = Double.parseDouble(inputSetOption.getUIFormSelectBox(FIELD_TIMEZONE_SELECTBOX).getValue());
    	String shortDateFormat = inputSetOption.getUIFormSelectBox(FIELD_SHORTDATEFORMAT_SELECTBOX).getValue();
    	String longDateFormat = inputSetOption.getUIFormSelectBox(FIELD_LONGDATEFORMAT_SELECTBOX).getValue();
    	String timeFormat = inputSetOption.getUIFormSelectBox(FIELD_TIMEFORMAT_SELECTBOX).getValue();
    	long maxTopic = Long.parseLong(inputSetOption.getUIFormSelectBox(FIELD_MAXTOPICS_SELECTBOX).getValue().substring(2)) ;
			long maxPost = Long.parseLong(inputSetOption.getUIFormSelectBox(FIELD_MAXPOSTS_SELECTBOX).getValue().substring(2)) ;
			boolean isShowForumJump = (Boolean)inputSetOption.getUIFormCheckBoxInput(FIELD_FORUMJUMP_CHECKBOX).getValue() ;
			
    	UIFormInputWithActions inputSetBan = uiForm.getChildById(FIELD_USERBAN_FORM) ;
    	boolean isBanned = (Boolean)inputSetBan.getUIFormCheckBoxInput(FIELD_ISBANNED_CHECKBOX).getValue() ;
    	String until = inputSetBan.getUIFormSelectBox(FIELD_BANUNTIL_SELECTBOX).getValue() ;
    	long banUntil = 0;
    	if(until != null && until.length() > 0) {
    		banUntil = Long.parseLong(until.substring(6));
    	}
    	String banReason = inputSetBan.getUIFormTextAreaInput(FIELD_BANREASON_TEXTAREA).getValue() ;
    	String banReasonSummarys =  ForumFormatUtils.unSplitForForum(userProfile.getBanReasonSummary());
    	double timeZoneOld = userProfile.getTimeZone() ;
    	Date date = uiForm.getNewDate(timeZoneOld);
    	int banCounter = 0;
    	date.setTime(banUntil) ;
    	if(banReasonSummarys != null && banReasonSummarys.length() > 0){
    		if(isBanned) {
    			banReasonSummarys = banReasonSummarys + 
    			"Ban Reason: " + banReason + " From Date: " + (ForumFormatUtils.getFormatDate("MM-dd-yyyy hh:mm a", uiForm.getNewDate(timeZoneOld))) + 
    			" To Date: " + ForumFormatUtils.getFormatDate("MM-dd-yyyy hh:mm a", date) + ";";
    			banCounter = userProfile.getBanCounter() + 1;
    		}
    	} else {
    		if(isBanned) {
    			banReasonSummarys = "Ban Reason: " + banReason + " From Date: " + (ForumFormatUtils.getFormatDate("MM-dd-yyyy hh:mm a", uiForm.getNewDate(timeZoneOld))) + 
    			" To Date: " + ForumFormatUtils.getFormatDate("MM-dd-yyyy hh:mm a", date) + ";";
    			banCounter = 1;
    		}
    	}
    	String []banReasonSummary = ForumFormatUtils.splitForForum(banReasonSummarys);
    	
    	userProfile.setUserTitle(userTitle);
    	userProfile.setUserRole(userRole) ;
    	userProfile.setSignature(signature);
    	userProfile.setIsDisplaySignature(isDisplaySignature);
    	userProfile.setModerateForums(moderateForums);
//    	userProfile.setModerateTopics(moderateTopics);
    	userProfile.setIsDisplayAvatar(isDisplayAvatar);
    	
    	userProfile.setTimeZone(timeZone);
    	userProfile.setShortDateFormat(shortDateFormat);
    	userProfile.setLongDateFormat(longDateFormat.replace('=', ' ')) ;
    	userProfile.setTimeFormat(timeFormat.replace('=', ' '));
    	userProfile.setMaxPostInPage(maxPost);
    	userProfile.setMaxTopicInPage(maxTopic);
    	userProfile.setIsShowForumJump(isShowForumJump) ;
    	
    	userProfile.setIsBanned(isBanned) ;
    	userProfile.setBanUntil(banUntil) ;
    	userProfile.setBanReason(banReason);
    	userProfile.setBanCounter(banCounter);
    	userProfile.setBanReasonSummary(banReasonSummary);
    	try {
    		uiForm.forumService.saveUserProfile(ForumSessionUtils.getSystemProvider(), userProfile, true, true) ;
      } catch (Exception e) {
      	e.printStackTrace() ;
      }
      if(userProfile.getUserId().equals(ForumSessionUtils.getCurrentUser())) {
      	uiForm.getAncestorOfType(UIForumPortlet.class).setUserProfile() ;
      }
      uiForm.isEdit = false ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  
  static  public class AddValuesAreaActionListener extends EventListener<UIModeratorManagementForm> {
  	public void execute(Event<UIModeratorManagementForm> event) throws Exception {
  		UIModeratorManagementForm uiForm = event.getSource() ;
  		String idChild = event.getRequestContext().getRequestParameter(OBJECTID);
  		boolean isTopic = false ;
  		if(idChild.equals("MosTopics")) isTopic = true ;
  		UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UISelectItemForum selectItemForum = popupAction.activate(UISelectItemForum.class, 400) ;
			selectItemForum.setForumLinks() ;
			selectItemForum.setIsTopic(isTopic, idChild) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
  		
  	}
  }

  static  public class CloseActionListener extends EventListener<UIModeratorManagementForm> {
  	public void execute(Event<UIModeratorManagementForm> event) throws Exception {
  		UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
  		forumPortlet.cancelAction() ;
  	}
  }
}
