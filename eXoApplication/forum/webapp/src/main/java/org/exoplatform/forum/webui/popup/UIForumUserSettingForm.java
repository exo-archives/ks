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
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumTransformHTML;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Watch;
import org.exoplatform.forum.webui.UIBreadcumbs;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIFormSelectBoxForum;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumLinks;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.forum.webui.UITopicPoll;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
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
		template = "app:/templates/forum/webui/popup/UIForumUserSettingForm.gtmpl",
		events = {
			@EventConfig(listeners = UIForumUserSettingForm.AttachmentActionListener.class), 
			@EventConfig(listeners = UIForumUserSettingForm.SaveActionListener.class), 
			@EventConfig(listeners = UIForumUserSettingForm.OpenTabActionListener.class), 
			@EventConfig(listeners = UIForumUserSettingForm.OpentContentActionListener.class), 
			@EventConfig(listeners = UIForumUserSettingForm.DeleteEmailWatchActionListener.class), 
			@EventConfig(listeners = UIForumUserSettingForm.CancelActionListener.class, phase=Phase.DECODE)
		}
)
public class UIForumUserSettingForm extends UIForm implements UIPopupComponent {
	public static final String FIELD_USERPROFILE_FORM = "ForumUserProfile" ;
	public static final String FIELD_USEROPTION_FORM = "ForumUserOption" ;
	public static final String FIELD_USERWATCHMANGER_FORM = "ForumUserWatches" ;
	
	public static final String FIELD_TIMEZONE_SELECTBOX = "TimeZone" ;
	public static final String FIELD_SHORTDATEFORMAT_SELECTBOX = "ShortDateformat" ;
	public static final String FIELD_LONGDATEFORMAT_SELECTBOX = "LongDateformat" ;
	public static final String FIELD_TIMEFORMAT_SELECTBOX = "Timeformat" ;
	public static final String FIELD_MAXTOPICS_SELECTBOX = "MaximumThreads" ;
	public static final String FIELD_MAXPOSTS_SELECTBOX = "MaximumPosts" ;
	public static final String FIELD_FORUMJUMP_CHECKBOX = "ShowForumJump" ;
	public static final String FIELD_AUTOWATCHMYTOPICS_CHECKBOX = "AutoWatchMyTopics" ;
	public static final String FIELD_AUTOWATCHTOPICIPOST_CHECKBOX = "AutoWatchTopicIPost" ;
	public static final String FIELD_TIMEZONE = "timeZone" ;
	
	public static final String FIELD_USERID_INPUT = "ForumUserName" ;
	public static final String FIELD_USERTITLE_INPUT = "ForumUserTitle" ;
	public static final String FIELD_SIGNATURE_TEXTAREA = "Signature" ;
	public static final String FIELD_ISDISPLAYSIGNATURE_CHECKBOX = "IsDisplaySignature" ;
	public static final String FIELD_ISDISPLAYAVATAR_CHECKBOX = "IsDisplayAvatar" ;
	
	public final String WATCHES_ITERATOR = "WatchChesPageIterator";
	
	@SuppressWarnings("unused")
  private String tabId = "ForumUserProfile";
	private ForumService forumService ;
	private UserProfile userProfile = null ;
	private String[] permissionUser = null;
	private List<Watch> listWatches = new ArrayList<Watch>();
	private JCRPageList pageList ;
	UIForumPageIterator pageIterator ;
	
	public UIForumUserSettingForm() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
		ResourceBundle res = context.getApplicationResourceBundle() ;
		permissionUser = new String[]{res.getString("UIForumPortlet.label.PermissionAdmin").toLowerCase(), 
																	res.getString("UIForumPortlet.label.PermissionModerator").toLowerCase(),
																	res.getString("UIForumPortlet.label.PermissionGuest").toLowerCase(),
																	res.getString("UIForumPortlet.label.PermissionUser").toLowerCase()};
		setActions(new String[]{"Save", "Cancel"});
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	private void initForumOption() throws Exception {
		SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ; 
		try {
			//String userId = this.getAncestorOfType(UIForumPortlet.class).getUserProfile().getUserId() ;
			String userId = ForumSessionUtils.getCurrentUser();
			this.userProfile = forumService.getUserSettingProfile(sProvider, userId) ;
		} catch (Exception e) {			
			e.printStackTrace() ;
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

		UIFormCheckBoxInput isAutoWatchMyTopics = new UIFormCheckBoxInput<Boolean>(FIELD_AUTOWATCHMYTOPICS_CHECKBOX, FIELD_AUTOWATCHMYTOPICS_CHECKBOX, false);
		isAutoWatchMyTopics.setChecked(userProfile.getIsAutoWatchMyTopics()) ;
		UIFormCheckBoxInput isAutoWatchTopicIPost = new UIFormCheckBoxInput<Boolean>(FIELD_AUTOWATCHTOPICIPOST_CHECKBOX, FIELD_AUTOWATCHTOPICIPOST_CHECKBOX, false);
		isAutoWatchTopicIPost.setChecked(userProfile.getIsAutoWatchTopicIPost()) ;
		
		UIFormCheckBoxInput isDisplayAvatar = new UIFormCheckBoxInput<Boolean>(FIELD_ISDISPLAYAVATAR_CHECKBOX, FIELD_ISDISPLAYAVATAR_CHECKBOX, false);
		isDisplayAvatar.setChecked(this.userProfile.getIsDisplayAvatar()) ;
		
		UIFormInputWithActions inputSetProfile = new UIFormInputWithActions(FIELD_USERPROFILE_FORM);
		inputSetProfile.addUIFormInput(userId) ;
		inputSetProfile.addUIFormInput(userTitle) ;
		inputSetProfile.addUIFormInput(signature) ;
		inputSetProfile.addUIFormInput(isDisplaySignature) ;
		inputSetProfile.addUIFormInput(isDisplayAvatar) ;
		inputSetProfile.addUIFormInput(isAutoWatchMyTopics) ;
		inputSetProfile.addUIFormInput(isAutoWatchTopicIPost) ;
		
		UIFormInputWithActions inputSetOption = new UIFormInputWithActions(FIELD_USEROPTION_FORM); 
		inputSetOption.addUIFormInput(timeZone) ;
		inputSetOption.addUIFormInput(shortdateFormat) ;
		inputSetOption.addUIFormInput(longDateFormat) ;
		inputSetOption.addUIFormInput(timeFormat) ;
		inputSetOption.addUIFormInput(maximumThreads) ;
		inputSetOption.addUIFormInput(maximumPosts) ;
		inputSetOption.addUIFormInput(isShowForumJump) ;
		
		UIFormInputWithActions inputUserWatchManger = new UIFormInputWithActions(FIELD_USERWATCHMANGER_FORM);

		addUIFormInput(inputSetProfile);
		addUIFormInput(inputSetOption);
		addUIFormInput(inputUserWatchManger);
		
		listWatches = forumService.getWatchByUser(this.userProfile.getUserId(), sProvider);
		sProvider.close();
		
		pageIterator = addChild(UIForumPageIterator.class, null, WATCHES_ITERATOR);
		pageList = new ForumPageList(7, listWatches.size());
		pageIterator.updatePageList(pageList);
		try {
			if(pageIterator.getInfoPage().get(3) <= 1) pageIterator.setRendered(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
  public List<Watch> getListWatch() throws Exception {
		long pageSelect = pageIterator.getPageSelected() ;
		List<Watch>list = new ArrayList<Watch>();
		try {
			list.addAll(this.pageList.getPageWatch(pageSelect, this.listWatches)) ;
			if(list.isEmpty()){
				while(list.isEmpty() && pageSelect > 1) {
					list.addAll(this.pageList.getPageWatch(--pageSelect, this.listWatches)) ;
					pageIterator.setSelectPage(pageSelect) ;
				}
			}
		} catch (Exception e) {
		}
		return list ;
	}
	
	@SuppressWarnings("deprecation")
	private Date getNewDate(double timeZoneOld) {
		Calendar	calendar = GregorianCalendar.getInstance() ;
		calendar.setLenient(false) ;
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() - gmtoffset + (long)(timeZoneOld*3600000)) ; 
		return calendar.getTime() ;
	}
	
	@SuppressWarnings("unused")
  private String getAvatarUrl(){
		String url = "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
		SessionProvider sessionProvider = ForumSessionUtils.getSystemProvider();
		try {
			DownloadService dservice = getApplicationComponent(DownloadService.class) ;
			url = ForumSessionUtils.getUserAvatarURL(ForumSessionUtils.getCurrentUser(), this.forumService, sessionProvider, dservice);
		} catch (Exception e) {
			url = "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
		}
		if(url == null || url.trim().length() < 1) url = "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
		sessionProvider.close();
		return url;
	}
	
	public UIFormSelectBoxForum getUIFormSelectBoxForum(String name) {
		return	findComponentById(name) ;
	}
	
	public void activate() throws Exception {
		initForumOption() ;
	}
	public void deActivate() throws Exception {
	}
	
	private boolean canView(Category category, Forum forum, Topic topic, Post post, UserProfile userProfile) throws Exception{
		if(userProfile.getUserRole() == 0) return true;
		boolean canView = true;
		boolean isModerator = false;
		if(category == null) return false;
		String[] listUsers = category.getUserPrivate();
		//check category is private:
		if(listUsers.length > 0 && listUsers[0].trim().length() > 0 && !ForumServiceUtils.hasPermission(listUsers, userProfile.getUserId())) 
			return false;
		else
			canView = true;
		
		// check forum
		if(forum != null){
			listUsers = forum.getModerators();
			if(userProfile.getUserRole() == 1 && (listUsers.length > 0 && listUsers[0].trim().length() > 0 && 
					ForumServiceUtils.hasPermission(listUsers, userProfile.getUserId()))) {
				isModerator = true;
				canView = true;
			} else if(forum.getIsClosed()) return false;
			else canView = true;
			
			// ckeck Topic:
			if(topic != null){
				if(isModerator) canView = true;
				else if(!topic.getIsClosed() && topic.getIsActive() && topic.getIsActiveByForum() && topic.getIsApproved() && 
								!topic.getIsWaiting() &&((topic.getCanView().length == 1 && topic.getCanView()[0].trim().length() < 1) ||
								ForumServiceUtils.hasPermission(topic.getCanView(), userProfile.getUserId()) ||
								ForumServiceUtils.hasPermission(forum.getViewer(), userProfile.getUserId()) ||
								ForumServiceUtils.hasPermission(forum.getPoster(), userProfile.getUserId()) )) canView = true;
				else canView = false;
			}
		}
		
		return canView;
	}
	
	static	public class SaveActionListener extends EventListener<UIForumUserSettingForm> {
		public void execute(Event<UIForumUserSettingForm> event) throws Exception {
			UIForumUserSettingForm uiForm = event.getSource() ;
			UIFormInputWithActions inputSetProfile = uiForm.getChildById(FIELD_USERPROFILE_FORM) ;
			String userTitle = inputSetProfile.getUIStringInput(FIELD_USERTITLE_INPUT).getValue() ;
			UserProfile userProfile = uiForm.userProfile ;
			if(userTitle == null || userTitle.trim().length() < 1){
    		userTitle = userProfile.getUserTitle();
    	} else {
    		int newPos = Arrays.asList(uiForm.permissionUser).indexOf(userTitle.toLowerCase());
    		if(newPos >= 0 && newPos < userProfile.getUserRole()){
    			userTitle = userProfile.getUserTitle();
    		}
    	}
			int maxText = ForumUtils.MAXSIGNATURE ;
			String signature = inputSetProfile.getUIFormTextAreaInput(FIELD_SIGNATURE_TEXTAREA).getValue() ;
			if(!ForumUtils.isEmpty(signature) && signature.length() > maxText) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				Object[] args = { uiForm.getLabel(FIELD_SIGNATURE_TEXTAREA), String.valueOf(maxText) };
				uiApp.addMessage(new ApplicationMessage("NameValidator.msg.warning-long-text", args, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
				return ;
			}
			signature = ForumTransformHTML.enCodeHTML(signature);
			boolean isDisplaySignature = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_ISDISPLAYSIGNATURE_CHECKBOX).getValue() ;
			Boolean isDisplayAvatar = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_ISDISPLAYAVATAR_CHECKBOX).getValue() ;
			boolean isAutoWatchMyTopics = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_AUTOWATCHMYTOPICS_CHECKBOX).getValue() ;
			boolean isAutoWatchTopicIPost = (Boolean)inputSetProfile.getUIFormCheckBoxInput(FIELD_AUTOWATCHTOPICIPOST_CHECKBOX).getValue() ;
			
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
			userProfile.setIsAutoWatchMyTopics(isAutoWatchMyTopics);
			userProfile.setIsAutoWatchTopicIPost(isAutoWatchTopicIPost);
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				uiForm.forumService.saveUserSettingProfile(sProvider, userProfile);
			} finally {
				sProvider.close();
			}
			forumPortlet.updateUserProfileInfo() ;
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
	
	static	public class OpenTabActionListener extends EventListener<UIForumUserSettingForm> {
		public void execute(Event<UIForumUserSettingForm> event) throws Exception {
			UIForumUserSettingForm uiForm = event.getSource() ;
			uiForm.tabId = event.getRequestContext().getRequestParameter(OBJECTID);
//			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
		}
	}
	

	static public class AttachmentActionListener extends EventListener<UIForumUserSettingForm> {
		public void execute(Event<UIForumUserSettingForm> event) throws Exception {
			UIForumUserSettingForm uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			UIPopupAction uiChildPopup = popupContainer.getChild(UIPopupAction.class).setRendered(true) ;
			UIAttachFileForm attachFileForm = uiChildPopup.activate(UIAttachFileForm.class, 500) ;
			attachFileForm.updateIsTopicForm(false) ;
			attachFileForm.setIsChangeAvatar(true);
			attachFileForm.setMaxField(1);
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static public class DeleteEmailWatchActionListener extends EventListener<UIForumUserSettingForm> {
		public void execute(Event<UIForumUserSettingForm> event) throws Exception {
			UIForumUserSettingForm uiForm = event.getSource() ;
			String input =  event.getRequestContext().getRequestParameter(OBJECTID) ;
			String email = input.substring(input.lastIndexOf("/") + 1) ;
			String path = input.substring(0, input.lastIndexOf("/"));
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			List<String>emails = new ArrayList<String>();
			emails.add(email) ;
			SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
			try {
				uiForm.forumService.removeWatch(sProvider, 1, path, emails) ;
				for(int i = 0; i < uiForm.listWatches.size(); i ++){
					if(uiForm.listWatches.get(i).getNodePath().equals(path) && uiForm.listWatches.get(i).getEmail().equals(email)){
						uiForm.listWatches.remove(i);
						break;
					}
				}
				uiForm.pageList = new ForumPageList(7, uiForm.listWatches.size());
				uiForm.pageIterator.updatePageList(uiForm.pageList);
			} finally {
				sProvider.close();
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
		}
	}
	
	static	public class OpentContentActionListener extends EventListener<UIForumUserSettingForm> {
		public void execute(Event<UIForumUserSettingForm> event) throws Exception {
			UIForumUserSettingForm uiForm = event.getSource() ;
			String path =  event.getRequestContext().getRequestParameter(OBJECTID) ;
			boolean isErro = false ;
			UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
			UserProfile userProfile = forumPortlet.getUserProfile();
			boolean isRead = true;
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			
			String []id = path.split("/") ;
			Category category = null;
			Forum forum = null;
			Topic topic = null;
			SessionProvider sProvider = SessionProviderFactory.createSystemProvider();
			try{
				String cateId =  id[3];
				category = forumService.getCategory(sProvider, cateId) ;
				String forumId = id[4];
				forum = forumService.getForum(sProvider,cateId , forumId ) ;
				String topicId = id[5];
				topic = forumService.getTopic(sProvider, cateId, forumId, topicId, userProfile.getUserId());
			} catch (Exception e) { 
			}finally {
				sProvider.close();
			}
			
			isRead = uiForm.canView(category, forum, topic, null, userProfile);
			
			if(id.length == 4) {
				if(category != null) {
					if(isRead){
						UICategoryContainer categoryContainer = forumPortlet.getChild(UICategoryContainer.class) ;
						categoryContainer.getChild(UICategory.class).update(category, null);
						categoryContainer.updateIsRender(false) ;
						forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath(id[3]);
						forumPortlet.updateIsRendered(ForumUtils.CATEGORIES);
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					}
				} else isErro = true ;
			} else if(id.length == 5) {
				int length = id.length ;
				if(forum != null) {
					if(isRead) {
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						uiForumContainer.setIsRenderChild(true) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicContainer uiTopicContainer = uiForumContainer.getChild(UITopicContainer.class) ;
						uiTopicContainer.setUpdateForum(id[length-2], forum) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-2]+"/"+id[length-1]));
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					}
				} else isErro = true ;
			} else if(id.length == 6){
				int length = id.length ;
				if(topic != null) {
					if(isRead){
						forumPortlet.updateIsRendered(ForumUtils.FORUM);
						UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
						UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
						uiForumContainer.setIsRenderChild(false) ;
						uiForumContainer.getChild(UIForumDescription.class).setForum(forum);
						UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
						uiTopicDetail.setTopicFromCate(id[length-3], id[length-2], topic) ;
						uiTopicDetail.setUpdateForum(forum) ;
						uiTopicDetail.setIdPostView("top") ;
						uiTopicDetailContainer.getChild(UITopicPoll.class).updateFormPoll(id[length-3], id[length-2] , topic.getId()) ;
						forumService.updateTopicAccess(forumPortlet.getUserProfile().getUserId(),  topic.getId()) ;
						forumPortlet.getUserProfile().setLastTimeAccessTopic(topic.getId(), ForumUtils.getInstanceTempCalendar().getTimeInMillis()) ;
						forumPortlet.getChild(UIForumLinks.class).setValueOption((id[length-3] + "/" + id[length-2] + " "));
						event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
					}
				} else isErro = true ;
			}
			if(isErro) {
				Object[] args = { };
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				uiApp.addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found", args, ApplicationMessage.WARNING)) ;
				return;
			}
			if(!isRead) {
				UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
				String[] s = new String[]{};
				uiApp.addMessage(new ApplicationMessage("UIForumPortlet.msg.do-not-permission", s, ApplicationMessage.WARNING)) ;
				return;
			}
			forumPortlet.cancelAction() ;
		}
	}
	
}
