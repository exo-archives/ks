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
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumEventQuery;
import org.exoplatform.forum.service.ForumSeach;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
		events = {
			@EventConfig(listeners = UISearchForm.SearchActionListener.class),	
			@EventConfig(listeners = UISearchForm.OnchangeActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.ResetFieldActionListener.class, phase = Phase.DECODE),	
			@EventConfig(listeners = UISearchForm.CancelActionListener.class, phase = Phase.DECODE)			
		}
)
public class UISearchForm extends UIForm {
	final static	private String FIELD_SEARCHVALUE_INPUT = "SearchValue" ;
	final static	private String FIELD_SEARCHVALUEIN_SELECTBOX = "SearchValueIn" ;
	final static	private String FIELD_SEARCHUSER_INPUT = "SearchUser" ;
	final static	private String FIELD_SEARCHTYPE_SELECTBOX = "SearchType" ;
	
	final static	private String FIELD_TOPICCOUNTMIN_INPUT = "TopicCountMin" ;
	final static	private String FIELD_TOPICCOUNTMAX_INPUT = "TopicCountMax" ;
	final static	private String FIELD_POSTCOUNTMIN_INPUT = "PostCountMin" ;
	final static	private String FIELD_POSTCOUNTMAX_INPUT = "PostCountMax" ;
	final static	private String FIELD_VIEWCOUNTMAX_INPUT = "ViewCountMin" ;
	final static	private String FIELD_VIEWCOUNTMIN_INPUT = "ViewCountMax" ;
	final static	private String FIELD_ISLOCK_SELECTBOX = "IsLock" ;
	final static	private String FIELD_ISCLOSED_SELECTBOX = "IsClosed" ;
	final static	private String FIELD_MODERATOR_INPUT = "Moderator" ;
	
	final static	private String FROMDATECREATED = "FromDateCreated" ;
	final static	private String TODATECREATED = "ToDateCreated" ;
	
	final static	private String FROMDATECREATEDLASTPOST = "FromDateCreatedLastPost" ;
	final static	private String TODATECREATEDLASTPOST = "ToDateCreatedLastPost" ;
	
	
//	final static	private String FIELD_LASTPOSTFROMTO_INPUT = "LastPostFromTo" ;
//  final static  private String FROM = "From" ;
//  final static  private String TO = "To" ;
//	final static	private String FIELD_SEARCHIN_SELECTBOX = "seachIn" ;
	
	private UserProfile userProfile = null;
	
	public void setUserProfile(UserProfile userProfile) {
		try {
			this.userProfile = userProfile ;
    } catch (Exception e) {
    	this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
    }
  }
	private boolean getIsAdmin() {
		if(this.userProfile != null) {
			if(this.userProfile.getUserRole() == 0) return true ;
		}
		return false ;
	}
	public UISearchForm() throws Exception {
		UIFormStringInput searchValue = new UIFormStringInput(FIELD_SEARCHVALUE_INPUT, FIELD_SEARCHVALUE_INPUT, null) ;
		UIFormStringInput searchUser = new UIFormStringInput(FIELD_SEARCHUSER_INPUT, FIELD_SEARCHUSER_INPUT, null) ;
		List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Forum", "forum")) ;
		list.add(new SelectItemOption<String>("Topic", "topic")) ;
		list.add(new SelectItemOption<String>("Post", "post")) ;
		UIFormSelectBox searchType = new UIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX, FIELD_SEARCHTYPE_SELECTBOX, list) ;
		searchType.setOnChange("Onchange") ;
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("Search Entire", "entire")) ;
		list.add(new SelectItemOption<String>("Search Titles Only", "title")) ;
		UIFormSelectBox textIn = new UIFormSelectBox(FIELD_SEARCHVALUEIN_SELECTBOX, FIELD_SEARCHVALUEIN_SELECTBOX, list) ;
		textIn.setValue("entire");
		
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("All", "all")) ;
		list.add(new SelectItemOption<String>("UnLock", "false")) ;
		list.add(new SelectItemOption<String>("Locked", "true")) ;
		UIFormSelectBox isLock = new UIFormSelectBox(FIELD_ISLOCK_SELECTBOX, FIELD_ISLOCK_SELECTBOX, list) ;
		isLock.setValue("all");
		list = new ArrayList<SelectItemOption<String>>() ;
		list.add(new SelectItemOption<String>("All", "all")) ;
		list.add(new SelectItemOption<String>("UnClose", "false")) ;
		list.add(new SelectItemOption<String>("Closed", "true")) ;
		UIFormSelectBox isClosed = new UIFormSelectBox(FIELD_ISCLOSED_SELECTBOX, FIELD_ISCLOSED_SELECTBOX, list) ;
		isLock.setValue("all");
		
		UIFormDateTimeInput FromDateCreated = new UIFormDateTimeInput(FROMDATECREATED, FROMDATECREATED, null, false) ;
		UIFormDateTimeInput ToDateCreated = new UIFormDateTimeInput(TODATECREATED, TODATECREATED, null, false) ;
		UIFormDateTimeInput FromDateCreatedLastPost = new UIFormDateTimeInput(FROMDATECREATEDLASTPOST, FROMDATECREATEDLASTPOST, null, false) ;
		UIFormDateTimeInput ToDateCreatedLastPost = new UIFormDateTimeInput(TODATECREATEDLASTPOST, TODATECREATEDLASTPOST, null, false) ;

		UIFormStringInput topicCountMin = new UIFormStringInput(FIELD_TOPICCOUNTMIN_INPUT, FIELD_TOPICCOUNTMIN_INPUT, null) ;
		topicCountMin.addValidator(PositiveNumberFormatValidator.class) ;
		UIFormStringInput topicCountMax = new UIFormStringInput(FIELD_TOPICCOUNTMAX_INPUT, FIELD_TOPICCOUNTMAX_INPUT, null) ;
		topicCountMax.addValidator(PositiveNumberFormatValidator.class) ;

		UIFormStringInput postCountMin = new UIFormStringInput(FIELD_POSTCOUNTMIN_INPUT, FIELD_POSTCOUNTMIN_INPUT, null) ;
		postCountMin.addValidator(PositiveNumberFormatValidator.class) ;
		UIFormStringInput postCountMax = new UIFormStringInput(FIELD_POSTCOUNTMAX_INPUT, FIELD_POSTCOUNTMAX_INPUT, null) ;
		postCountMin.addValidator(PositiveNumberFormatValidator.class) ;
		
		UIFormStringInput viewCountMin = new UIFormStringInput(FIELD_VIEWCOUNTMIN_INPUT, FIELD_VIEWCOUNTMIN_INPUT, null) ;
		viewCountMin.addValidator(PositiveNumberFormatValidator.class) ;viewCountMin.setRendered(false) ;
		UIFormStringInput viewCountMax = new UIFormStringInput(FIELD_VIEWCOUNTMAX_INPUT, FIELD_VIEWCOUNTMAX_INPUT, null) ;
		viewCountMax.addValidator(PositiveNumberFormatValidator.class) ;viewCountMax.setRendered(false) ;
		
		UIFormStringInput moderator = new UIFormStringInput(FIELD_MODERATOR_INPUT, FIELD_MODERATOR_INPUT, null) ;
		
		addUIFormInput(searchType) ;
		addUIFormInput(searchValue) ;
		addUIFormInput(textIn) ;
		addUIFormInput(searchUser) ;
		addUIFormInput(isLock) ;
		addUIFormInput(isClosed) ;
		addUIFormInput(FromDateCreated) ;
		addUIFormInput(ToDateCreated) ;
		
		addUIFormInput(topicCountMin) ;
		addUIFormInput(topicCountMax) ;
		addUIFormInput(postCountMin) ;
		addUIFormInput(postCountMax) ;
		addUIFormInput(FromDateCreatedLastPost) ;
		addUIFormInput(ToDateCreatedLastPost) ;
		addUIFormInput(viewCountMin) ;
		addUIFormInput(viewCountMax) ;
		addUIFormInput(moderator) ;
//		addUIFormInput(new UIFormStringInput(FIELD_SEARCHIN_SELECTBOX, FIELD_SEARCHIN_SELECTBOX, null)) ;
	}
	
	public void setSelectedForum(){
		getUIFormDateTimeInput(FROMDATECREATEDLASTPOST).setRendered(false) ;
		getUIFormDateTimeInput(TODATECREATEDLASTPOST).setRendered(false) ;
		getUIFormSelectBox(FIELD_ISLOCK_SELECTBOX).setRendered(true);
		getUIFormSelectBox(FIELD_ISCLOSED_SELECTBOX).setRendered(getIsAdmin());
		getUIStringInput(FIELD_TOPICCOUNTMIN_INPUT).setRendered(true);
		getUIStringInput(FIELD_TOPICCOUNTMAX_INPUT).setRendered(true);
		getUIStringInput(FIELD_POSTCOUNTMAX_INPUT).setRendered(true);
		getUIStringInput(FIELD_POSTCOUNTMIN_INPUT).setRendered(true);
		getUIStringInput(FIELD_VIEWCOUNTMAX_INPUT).setRendered(false);
		getUIStringInput(FIELD_VIEWCOUNTMIN_INPUT).setRendered(false);
		getUIStringInput(FIELD_MODERATOR_INPUT).setRendered(true);
	}

	private void setSelectedTopic(){
		getUIFormDateTimeInput(FROMDATECREATEDLASTPOST).setRendered(true) ;
		getUIFormDateTimeInput(TODATECREATEDLASTPOST).setRendered(true) ;
		getUIFormSelectBox(FIELD_ISLOCK_SELECTBOX).setRendered(true);
		getUIFormSelectBox(FIELD_ISCLOSED_SELECTBOX).setRendered(getIsAdmin());
		getUIStringInput(FIELD_TOPICCOUNTMIN_INPUT).setRendered(false);
		getUIStringInput(FIELD_TOPICCOUNTMAX_INPUT).setRendered(false);
		getUIStringInput(FIELD_POSTCOUNTMAX_INPUT).setRendered(true);
		getUIStringInput(FIELD_POSTCOUNTMIN_INPUT).setRendered(true);
		getUIStringInput(FIELD_VIEWCOUNTMAX_INPUT).setRendered(true);
		getUIStringInput(FIELD_VIEWCOUNTMIN_INPUT).setRendered(true);
		getUIStringInput(FIELD_MODERATOR_INPUT).setRendered(false);
	}
	private void setSelectedPost(){
		getUIFormDateTimeInput(FROMDATECREATEDLASTPOST).setRendered(false) ;
		getUIFormDateTimeInput(TODATECREATEDLASTPOST).setRendered(false) ;
		getUIFormSelectBox(FIELD_ISLOCK_SELECTBOX).setRendered(false);
		getUIFormSelectBox(FIELD_ISCLOSED_SELECTBOX).setRendered(false);
		getUIStringInput(FIELD_TOPICCOUNTMIN_INPUT).setRendered(false);
		getUIStringInput(FIELD_TOPICCOUNTMAX_INPUT).setRendered(false);
		getUIStringInput(FIELD_POSTCOUNTMAX_INPUT).setRendered(false);
		getUIStringInput(FIELD_POSTCOUNTMIN_INPUT).setRendered(false);
		getUIStringInput(FIELD_VIEWCOUNTMAX_INPUT).setRendered(false);
		getUIStringInput(FIELD_VIEWCOUNTMIN_INPUT).setRendered(false);
		getUIStringInput(FIELD_MODERATOR_INPUT).setRendered(false);
	}
	
	public String getLabel(ResourceBundle res, String id) throws Exception {
    String label = getId() + ".label." + id;    
    try {
    	return res.getString(label);
    } catch (Exception e) {
			return id ;
		}
  }
  
  public String[] getActions() {
    return new String[]{"Search","ResetField", "Cancel"} ;
  }
	static	public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue() ;
			String keyValue = uiForm.getUIStringInput(FIELD_SEARCHVALUE_INPUT).getValue() ;
			String valueIn = uiForm.getUIFormSelectBox(FIELD_SEARCHVALUEIN_SELECTBOX).getValue() ;
			String path = "" ;
			String byUser = uiForm.getUIStringInput(FIELD_SEARCHUSER_INPUT).getValue() ;
			String isLock = uiForm.getUIFormSelectBox(FIELD_ISLOCK_SELECTBOX).getValue();
			String isClosed = uiForm.getUIFormSelectBox(FIELD_ISCLOSED_SELECTBOX).getValue();
			String topicCountMin = uiForm.getUIStringInput(FIELD_TOPICCOUNTMIN_INPUT).getValue();
			String topicCountMax = uiForm.getUIStringInput(FIELD_TOPICCOUNTMAX_INPUT).getValue();
			String postCountMin = uiForm.getUIStringInput(FIELD_POSTCOUNTMAX_INPUT).getValue();
			String postCountMax = uiForm.getUIStringInput(FIELD_POSTCOUNTMIN_INPUT).getValue();
			String viewCountMin = uiForm.getUIStringInput(FIELD_VIEWCOUNTMIN_INPUT).getValue();
			String viewCountMax = uiForm.getUIStringInput(FIELD_VIEWCOUNTMAX_INPUT).getValue();
			String moderator = uiForm.getUIStringInput(FIELD_MODERATOR_INPUT).getValue();
			Calendar fromDateCreated = uiForm.getUIFormDateTimeInput(FROMDATECREATED).getCalendar() ;
			Calendar toDateCreated= uiForm.getUIFormDateTimeInput(TODATECREATED).getCalendar() ;
			Calendar fromDateCreatedLastPost = uiForm.getUIFormDateTimeInput(FROMDATECREATEDLASTPOST).getCalendar() ;
			Calendar toDateCreatedLastPost = uiForm.getUIFormDateTimeInput(TODATECREATEDLASTPOST).getCalendar() ;
//			if(){
				ForumEventQuery eventQuery = new ForumEventQuery() ;
				eventQuery.setType(type) ;
				eventQuery.setKeyValue(keyValue) ;
				eventQuery.setValueIn(valueIn) ;
				eventQuery.setPath(path) ;
				eventQuery.setByUser(byUser);
				eventQuery.setIsLock(isLock) ;
				eventQuery.setIsClose(isClosed) ;
				eventQuery.setTopicCountMin(topicCountMin) ;
				eventQuery.setTopicCountMax(topicCountMax) ;
				eventQuery.setPostCountMin(postCountMin) ;
				eventQuery.setPostCountMax(postCountMax) ;
				eventQuery.setViewCountMin(viewCountMin) ;
				eventQuery.setViewCountMax(viewCountMax) ;
				eventQuery.setModerator(moderator) ;
				eventQuery.setFromDateCreated(fromDateCreated) ;
				eventQuery.setToDateCreated(toDateCreated) ;
				eventQuery.setFromDateCreatedLastPost(fromDateCreatedLastPost) ;
				eventQuery.setToDateCreatedLastPost(toDateCreatedLastPost) ;
			
				UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.updateIsRendered(1) ;
				UICategories categories = forumPortlet.findFirstComponentOfType(UICategories.class);
				categories.setIsRenderChild(true) ;
				ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
				List<ForumSeach> list = forumService.getAdvancedSeach(ForumSessionUtils.getSystemProvider(),eventQuery);
				UIForumListSeach listSeachEvent = categories.getChild(UIForumListSeach.class) ;
				listSeachEvent.setListSeachEvent(list) ;
				forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath("ForumSeach") ;
				event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
//			} else {
//				Object[] args = { };
//				throw new MessageException(new ApplicationMessage("UISearchForm.msg.checkEmpty", args, ApplicationMessage.WARNING)) ;
//			}
		}
	}
	static	public class OnchangeActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			String type = uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).getValue() ;
			if(type.equals("forum")) {
				uiForm.setSelectedForum() ;
			} else if(type.equals("topic")){
				uiForm.setSelectedTopic() ;
			} else {
				uiForm.setSelectedPost() ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}
	
	static	public class ResetFieldActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			uiForm.getUIFormSelectBox(FIELD_SEARCHTYPE_SELECTBOX).setValue("forum");
			uiForm.getUIFormSelectBox(FIELD_SEARCHVALUEIN_SELECTBOX).setValue("entire");
			uiForm.getUIFormSelectBox(FIELD_ISLOCK_SELECTBOX).setValue("all");
			uiForm.getUIFormSelectBox(FIELD_ISCLOSED_SELECTBOX).setValue("all");
			uiForm.setSelectedForum() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(uiForm) ;
		}
	}

	static	public class CancelActionListener extends EventListener<UISearchForm> {
		public void execute(Event<UISearchForm> event) throws Exception {
			UISearchForm uiForm = event.getSource() ;
			UIForumPortlet forumPortlet = (UIForumPortlet)uiForm.getParent() ;
			forumPortlet.updateIsRendered(1) ;
			forumPortlet.getChild(UIBreadcumbs.class).setUpdataPath("ForumService") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}










}















