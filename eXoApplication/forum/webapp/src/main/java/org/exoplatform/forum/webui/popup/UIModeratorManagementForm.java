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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
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
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

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
	private String[] permissionUser = null;
  private JCRPageList pageList ;
	private boolean isEdit = false ;
	private UserProfile userProfile = new UserProfile();
	private List<ForumLinkData> forumLinks = null;
  private List<String> listModerate = new ArrayList<String>();
	private List<User> listUser = null;
	public static final String FIELD_USERPROFILE_FORM = "ForumUserProfile" ;
	public static final String FIELD_USEROPTION_FORM = "ForumUserOption" ;
	public static final String FIELD_USERBAN_FORM = "ForumUserBan" ;
	
	public static final String FIELD_USERID_INPUT = "ForumUserName" ;
	public static final String FIELD_USERTITLE_INPUT = "ForumUserTitle" ;
	public static final String FIELD_USERROLE_CHECKBOX = "isAdmin" ;
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
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
		permissionUser = new String[]{res.getString("UIForumPortlet.label.PermissionAdmin"), 
																	res.getString("UIForumPortlet.label.PermissionModerator"),
																	res.getString("UIForumPortlet.label.PermissionUser")};
  }
	
  @SuppressWarnings("unused")
  public JCRPageList setPageListUserProfile() throws Exception {
  	if(listUser == null) {
  		listUser = ForumSessionUtils.getAllUser() ;
  	}
    for (User user : listUser) {
      UserProfile userProfile = this.forumService.getUserProfile(ForumSessionUtils.getSystemProvider(), user.getUserName(), true, true, false) ;
    }
  	this.pageList = this.forumService.getPageListUserProfile(ForumSessionUtils.getSystemProvider()) ;
  	this.pageList.setPageSize(10);
  	this.getChild(UIForumPageIterator.class).updatePageList(this.pageList) ;
  	return this.pageList;
  }
  
  @SuppressWarnings("unused")
  private List<UserProfile> getListProFileUser() throws Exception {
  	setListProFileUser() ;
  	return this.userProfiles ;
  }
  
  @SuppressWarnings("unchecked")
  private void setListProFileUser() throws Exception {
  	long page = this.getChild(UIForumPageIterator.class).getPageSelected() ;
  	long maxPage = this.pageList.getAvailablePage() ;
  	if(page <= 0) page = 1;
  	if(page > maxPage) page = maxPage ;
  	List<UserProfile> listUserProfile = this.pageList.getPage(page) ;
  	this.userProfiles = new ArrayList<UserProfile>();
  	int i =0, j = 0;
  	for (UserProfile userProfile : listUserProfile) {
  		if(userProfile.getUser() == null) {
	  		for (User user : listUser) {
		      if(user.getUserName().equals(userProfile.getUserId())) {
			  		userProfile.setUser(user);
			  		userProfile.setLastLoginDate(user.getLastLoginTime());
			  		this.userProfiles.add(userProfile);
			  		++j;
			  		break ;
		      }
	      }
  		} else {
  			++j;
  			this.userProfiles.add(userProfile);
  		}
  		++ i;
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
  
	private String stringProcess(List<String> values) {
	  String outPut = "" ;
	  if(!values.isEmpty()) {
	    for(String value : values) {
	    	if(!ForumUtils.isEmpty(value)) {
	      	if(value.indexOf('(') > 0){
	      		outPut += value.substring(0, value.indexOf('(')) + "\n" ;
	      	}
	      }
	    }
	  }
	  return outPut ;
	}
	
	private List<String> getModerateList(List<String> forumsModerate) {
		List<String> list = new ArrayList<String>() ;
		for (String string : forumsModerate) {
			if(string.indexOf('(') > 0) {
				string = string.substring((string.indexOf('(')+1)) ;
				list.add(string);
			}
    }
		return list; 
	}
	@SuppressWarnings("unused")
  private boolean getIsEdit() {
		return this.isEdit ;
	}
	
	public void setValues(List<String> values) {
    this.listModerate = values ;
    UIFormInputWithActions inputSetProfile = this.getChildById(FIELD_USERPROFILE_FORM) ;
    String value = stringProcess(values) ;
    inputSetProfile.getUIFormTextAreaInput(FIELD_MODERATEFORUMS_MULTIVALUE).setValue(value) ;
//    UIFormStringInput stringInput = inputSetProfile.getUIStringInput(FIELD_USERTITLE_INPUT) ;
//    String userTitle = stringInput.getValue() ;
//    boolean isChangeTitle = false;
//    if(!values.isEmpty()) {
//    	if(userTitle == null || userTitle.equals("User") || userTitle.equals("Guest")) {
//    		userTitle = "Moderator" ;
//    		isChangeTitle = true;
//    	}
//    } else {
//    	if(userTitle.equals("Moderator")) {
//    		userTitle = "User" ;
//    		isChangeTitle = true;
//    	}
//    }
//    if(isChangeTitle)stringInput.setValue(userTitle) ;
  }
	
	@SuppressWarnings({ "unchecked", "deprecation" })
  private void initUserProfileForm() throws Exception {
		this.setForumLinks();
		List<SelectItemOption<String>> list ;
		UIFormStringInput userId = new UIFormStringInput(FIELD_USERID_INPUT, FIELD_USERID_INPUT, null);
		userId.setValue(this.userProfile.getUserId());
		userId.setEditable(false) ;
		userId.setEnable(false) ;
		UIFormStringInput userTitle = new UIFormStringInput(FIELD_USERTITLE_INPUT, FIELD_USERTITLE_INPUT, null);
		userTitle.setValue(this.userProfile.getUserTitle());
		
		boolean isAdmin = false ;
		if(this.userProfile.getUserRole() == 0) isAdmin = true;
		UIFormCheckBoxInput userRole = new UIFormCheckBoxInput<Boolean>(FIELD_USERROLE_CHECKBOX, FIELD_USERROLE_CHECKBOX, false) ;
		userRole.setValue(isAdmin);
		
		UIFormTextAreaInput signature = new UIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA, FIELD_SIGNATURE_TEXTAREA, null);
		signature.setValue(this.userProfile.getSignature());
		UIFormCheckBoxInput isDisplaySignature = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYSIGNATURE_CHECKBOX, FIELD_ISDISPLAYSIGNATURE_CHECKBOX, false);
		isDisplaySignature.setChecked(this.userProfile.getIsDisplaySignature()) ;
		UIFormTextAreaInput moderateForums = new UIFormTextAreaInput(FIELD_MODERATEFORUMS_MULTIVALUE, FIELD_MODERATEFORUMS_MULTIVALUE, null);
		List<String> values = Arrays.asList(userProfile.getModerateForums()) ;
		this.listModerate = values ;
		moderateForums.setValue(stringProcess(values)) ;
    
    moderateForums.setEditable(false);
		UIFormCheckBoxInput isDisplayAvatar = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYAVATAR_CHECKBOX, FIELD_ISDISPLAYAVATAR_CHECKBOX, false);
		isDisplayAvatar.setChecked(this.userProfile.getIsDisplayAvatar()) ;
		//Option
		String []timeZone1 = getLabel(FIELD_TIMEZONE).split("/") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		for(String string : timeZone1) {
			list.add(new SelectItemOption<String>(string, ForumUtils.getTimeZoneNumberInString(string))) ;
		}
		UIFormSelectBox timeZone = new UIFormSelectBox(FIELD_TIMEZONE_SELECTBOX, FIELD_TIMEZONE_SELECTBOX, list) ;
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
			list.add(new SelectItemOption<String>((frm.toLowerCase() +" ("  + ForumUtils.getFormatDate(frm, date)+")"), frm)) ;
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
			list.add(new SelectItemOption<String>("Banned until: " + ForumUtils.getFormatDate(userProfile.getShortDateFormat()+ " hh:mm a", date) + " GMT+0", ("Until_" + until))) ;
		}
		date = getInstanceTempCalendar();
		until = date.getTime() + oneDate;
		date.setTime(until);
		list.add(new SelectItemOption<String>("1 Day ("+ForumUtils.getFormatDate(userProfile.getShortDateFormat()+ " hh:mm a", date)+" GMT+0)", "Until_" + until)) ;
    while(true) {
			if(i == 8 && dv.equals("Days")) i = 10;
			if(i == 11) {i = 2; dv = "Weeks";}
			if(i == 4 && dv.equals("Weeks")) {i = 1; dv = "Month" ;}
			if(i == 2 && dv.equals("Month")){dv = "Months" ;}
			if(i == 7 && dv.equals("Months")){i = 1; dv = "Year" ;}
			if(i == 2 && dv.equals("Year")){dv = "Years" ;}
			if(i == 3 && dv.equals("Years")){break;}
			if(dv.equals("Days")){ date = getInstanceTempCalendar(); until = date.getTime() + i*oneDate ; date.setTime(until);}
			if(dv.equals("Weeks")){ date = getInstanceTempCalendar();until = date.getTime() + i*oneDate*7; date.setTime(until);}
			if(dv.equals("Month")||dv.equals("Months")){ date = getInstanceTempCalendar(); date.setMonth(date.getMonth() + i) ; until = date.getTime();}
			if(dv.equals("Years")||dv.equals("Year")){ date = getInstanceTempCalendar(); date.setYear(date.getYear() + i) ; until = date.getTime();}
			list.add(new SelectItemOption<String>(i+" "+dv+" ("+ForumUtils.getFormatDate(userProfile.getShortDateFormat()+ " hh:mm a", date)+" GMT+0)", ("Until_" + until))) ;
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
		banReasonSummary.setValue(ForumUtils.unSplitForForum(userProfile.getBanReasonSummary()));
		UIFormStringInput createdDateBan = new UIFormStringInput(FIELD_CREATEDDATEBAN_INPUT, FIELD_CREATEDDATEBAN_INPUT, null) ;
		if(isBan) {
			banReason.setValue(userProfile.getBanReason());
			createdDateBan.setValue(ForumUtils.getFormatDate("MM/dd/yyyy, hh:mm a",userProfile.getCreatedDateBan()));
		} else {
			banReason.setEnable(true);
		}
		UIFormInputWithActions inputSetProfile = new UIFormInputWithActions(FIELD_USERPROFILE_FORM); 
		inputSetProfile.addUIFormInput(userId);
		inputSetProfile.addUIFormInput(userTitle);
		inputSetProfile.addUIFormInput(userRole);
		inputSetProfile.addUIFormInput(moderateForums);
		inputSetProfile.addUIFormInput(signature);
		inputSetProfile.addUIFormInput(isDisplaySignature);
		inputSetProfile.addUIFormInput(isDisplayAvatar);
		String string = FIELD_MODERATEFORUMS_MULTIVALUE ;
		List<ActionData> actions = new ArrayList<ActionData>() ;
		ActionData ad = new ActionData() ;
		ad.setActionListener("AddValuesArea") ;
		ad.setActionParameter(string) ;
		ad.setCssIconClass("AddIcon16x16") ;
		ad.setActionName(string);
		actions.add(ad) ;
		inputSetProfile.setActionField(string, actions);
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
		UIPageListTopicByUser pageListTopicByUser = addChild(UIPageListTopicByUser.class, null, null) ;
		pageListTopicByUser.setUserName(this.userProfile.getUserId()) ;
		UIPageListPostByUser listPostByUser = addChild(UIPageListPostByUser.class, null, null) ;
		listPostByUser.setUserName(this.userProfile.getUserId()) ;
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
	
	private Date getInstanceTempCalendar() {
		return ForumUtils.getInstanceTempCalendar().getTime() ;
	}
	
	private void setForumLinks() throws Exception {
		UIForumLinks uiForumLinks = this.getAncestorOfType(UIForumPortlet.class).getChild(UIForumLinks.class) ;
		boolean hasGetService = false;
		if(uiForumLinks == null) hasGetService = true;
		else this.forumLinks = uiForumLinks.getForumLinks() ;
		if(this.forumLinks == null || forumLinks.size() <= 0) hasGetService = true;
		if(hasGetService) {
			this.forumService.getAllLink(ForumSessionUtils.getSystemProvider());
		}
	}
	
	@SuppressWarnings("unused")
  private List<ForumLinkData> getForumLinks() throws Exception {
	  return this.forumLinks ;
  }
	
  static  public class ViewProfileActionListener extends EventListener<UIModeratorManagementForm> {
    public void execute(Event<UIModeratorManagementForm> event) throws Exception {
    	UIModeratorManagementForm uiForm = event.getSource() ;
  		String userId = event.getRequestContext().getRequestParameter(OBJECTID);
  		UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIViewUserProfile viewUserProfile = popupAction.activate(UIViewUserProfile.class, 670) ;
			viewUserProfile.setUserProfile(uiForm.getUserProfile(userId)) ;
			String userLogin = ForumSessionUtils.getCurrentUser() ;
			viewUserProfile.setUserProfileLogin(uiForm.getUserProfile(userLogin));
			viewUserProfile.setContact(null) ;
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
	    uiForm.removeChild(UIPageListPostByUser.class) ;
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
    	UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
    	UIFormInputWithActions inputSetProfile = uiForm.getChildById(FIELD_USERPROFILE_FORM) ;
    	String userTitle = inputSetProfile.getUIStringInput(FIELD_USERTITLE_INPUT).getValue() ;
    	long userRole = 3;
    	boolean isAdmin = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_USERROLE_CHECKBOX).getValue() ;
    	if(isAdmin) userRole = 0;
    	String moderateForum = inputSetProfile.getUIFormTextAreaInput(FIELD_MODERATEFORUMS_MULTIVALUE).getValue() ;
      List<String> moderateForums = new ArrayList<String>() ;
    	if(!ForumUtils.isEmpty(moderateForum)) {
        moderateForums = uiForm.listModerate ;
    	} 
    	List<String> NewModerates = new ArrayList<String> ();
    	NewModerates = uiForm.getModerateList(moderateForums);
    	List<String> OldModerateForums = uiForm.getModerateList(Arrays.asList(userProfile.getModerateForums())) ;
    	List<String> DeleteModerateForums = new ArrayList<String> ();
    	boolean isSetGetNewListForum = false ;
    	if(NewModerates.isEmpty()){
    		DeleteModerateForums = OldModerateForums;
    	} else {
    		for (String string : OldModerateForums) {
    			if(NewModerates.contains(string)) {
    				NewModerates.remove(string);
    			} else {
    				DeleteModerateForums.add(string) ;
    			}
    		}
    		uiForm.forumService.saveModerateOfForums(ForumSessionUtils.getSystemProvider(), NewModerates, userProfile.getUserId(), false);
    		isSetGetNewListForum = true ;
    	}
    	if(DeleteModerateForums.size() > 0) {
    		uiForm.forumService.saveModerateOfForums(ForumSessionUtils.getSystemProvider(), DeleteModerateForums, userProfile.getUserId(), true);
    		isSetGetNewListForum = true ;
    	}
    	if(isSetGetNewListForum)forumPortlet.findFirstComponentOfType(UICategories.class).setIsgetForumList(true);
    	
    	if(!moderateForums.isEmpty()) {
    		if(userRole >= 2) userRole = 1;
    	} else {
    		if(userRole >= 1) userRole = 2;
    	}
			if(userTitle == null || userTitle.trim().length() < 1 || Arrays.asList(uiForm.permissionUser).contains(userTitle)) {
				userTitle = uiForm.permissionUser[(int)userRole];
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
      boolean wasBanned = userProfile.getIsBanned() ;
    	boolean isBanned = (Boolean)inputSetBan.getUIFormCheckBoxInput(FIELD_ISBANNED_CHECKBOX).getValue() ;
    	String until = inputSetBan.getUIFormSelectBox(FIELD_BANUNTIL_SELECTBOX).getValue() ;
    	long banUntil = 0;
    	if(!ForumUtils.isEmpty(until)) {
    		banUntil = Long.parseLong(until.substring(6));
    	}
    	String banReason = inputSetBan.getUIFormTextAreaInput(FIELD_BANREASON_TEXTAREA).getValue() ;
      
    	String []banReasonSummaries =  userProfile.getBanReasonSummary();
    	Date date = uiForm.getInstanceTempCalendar();
    	int banCounter = userProfile.getBanCounter();
    	date.setTime(banUntil) ;
      StringBuffer stringBuffer = new StringBuffer();
      stringBuffer.append("Ban Reason: ").append(banReason).append(" From Date: ") 
          .append(ForumUtils.getFormatDate("MM-dd-yyyy hh:mm a", uiForm.getInstanceTempCalendar())) 
          .append(" GMT+0 To Date: ").append(ForumUtils.getFormatDate("MM-dd-yyyy hh:mm a", date)).append(" GMT+0") ;
      if(isBanned) {
      	if(banReasonSummaries != null && banReasonSummaries.length > 0){
          if(wasBanned){
            banReasonSummaries[0] = stringBuffer.toString() ;
          } else {
            String []temp = new String [banReasonSummaries.length + 1] ;
            int i = 1;
            for (String string : banReasonSummaries) {
              temp[i++] = string ;
            }
            temp[0] = stringBuffer.toString() ;
            banReasonSummaries = temp ;
      			banCounter = banCounter + 1;
          }
      	} else {
            banReasonSummaries = new String[] {stringBuffer.toString()} ;
      			banCounter = 1;
      	}
      }
    	userProfile.setUserTitle(userTitle);
    	userProfile.setUserRole(userRole) ;
    	userProfile.setSignature(signature);
    	userProfile.setIsDisplaySignature(isDisplaySignature);
    	userProfile.setModerateForums(moderateForums.toArray(new String[]{}));
    	userProfile.setIsDisplayAvatar(isDisplayAvatar);
    	
    	userProfile.setTimeZone(-timeZone);
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
    	userProfile.setBanReasonSummary(banReasonSummaries);
    	try {
    		uiForm.forumService.saveUserProfile(ForumSessionUtils.getSystemProvider(), userProfile, true, true) ;
      } catch (Exception e) {
      	e.printStackTrace() ;
      }
      if(userProfile.getUserId().equals(ForumSessionUtils.getCurrentUser())) {
      	forumPortlet.setUserProfile() ;
      }
      uiForm.isEdit = false ;
      uiForm.setPageListUserProfile();
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
    }
  }
  
  static  public class AddValuesAreaActionListener extends EventListener<UIModeratorManagementForm> {
  	public void execute(Event<UIModeratorManagementForm> event) throws Exception {
  		UIModeratorManagementForm uiForm = event.getSource() ;
  		UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UISelectItemForum selectItemForum = popupAction.activate(UISelectItemForum.class, 400) ;
			selectItemForum.setForumLinks() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
  	}
  }

  static  public class CloseActionListener extends EventListener<UIModeratorManagementForm> {
  	public void execute(Event<UIModeratorManagementForm> event) throws Exception {
  		UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
  		forumPortlet.cancelAction() ;
  		event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
  	}
  }
}
