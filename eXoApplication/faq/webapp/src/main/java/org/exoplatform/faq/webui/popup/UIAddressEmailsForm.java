/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
 */
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.SelectItem;
import org.exoplatform.faq.webui.SelectOption;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIFormSelectBoxWithGroups;
import org.exoplatform.faq.webui.UISendEmailsContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormStringInput;
/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Oct 2, 2008, 10:47:27 AM
 */
@ComponentConfig(lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIAddressEmailsForm.gtmpl", 
    events = {
  @EventConfig(listeners = UIAddressEmailsForm.SearchUserActionListener.class),
  @EventConfig(listeners = UIAddressEmailsForm.AddActionListener.class),
  @EventConfig(listeners = UIAddressEmailsForm.ChangeGroupActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIAddressEmailsForm.ShowPageActionListener.class, phase = Phase.DECODE),
  @EventConfig(listeners = UIAddressEmailsForm.CancelActionListener.class, phase = Phase.DECODE)
})
public class UIAddressEmailsForm extends UIForm implements UIPopupComponent {
	public static final String USER_SEARCH = "user-search".intern();
  public static final String USER_GROUP = "user-group".intern();

	public Map<String, User> checkedList_ = new HashMap<String, User>() ;
  public Map<String, User> newCheckedList_ = new HashMap<String, User>() ;
  
  private static final String FILED_ALL_GROUP = "all-group" ;
  private String selectedAddressId_ = "" ;
  private String recipientsType_ = "";
  private UIPageIterator uiPageList_ ;
  public void setRecipientsType(String type) {
    recipientsType_ = type;
  }

  public String getRecipientType() {
    return recipientsType_;
  }

  public UIAddressEmailsForm() throws Exception {
  	addUIFormInput(new UIFormStringInput(USER_SEARCH, USER_SEARCH, null)) ;
    UIFormSelectBoxWithGroups uiSelect = new UIFormSelectBoxWithGroups(USER_GROUP, USER_GROUP, getOptions()) ;
    uiSelect.setOnChange("ChangeGroup") ;
    addUIFormInput(uiSelect) ;
    uiPageList_ = new UIPageIterator() ;
    setUserList(FAQUtils.getAllUser()) ;
  }
  
  public List<SelectItem> getOptions() throws Exception {
  	List<SelectItem> options = new ArrayList<SelectItem>() ;
  	options.add(new SelectOption(FILED_ALL_GROUP, FILED_ALL_GROUP));
  	OrganizationService organizationService =(OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
	  Object[] objGroupIds = organizationService.getGroupHandler().getAllGroups().toArray() ;
	  List<String> groupIds = new ArrayList<String>() ;
	  for (Object object : objGroupIds) {
	    groupIds.add(((GroupImpl)object).getId()) ;
	  }
	  if(!groupIds.isEmpty()){
	    for(String publicCg : groupIds) {
	    	options.add(new SelectOption(publicCg, publicCg));
	    }
	  }
    return options ;
  }
  
  @SuppressWarnings("unchecked")
  public List<User> getUsers() throws Exception {
    List<User> users = new ArrayList<User>(uiPageList_.getCurrentPageData()) ;
    for(User c : users) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getUserName()) ;
      if(uiInput == null) addUIFormInput(new UIFormCheckBoxInput<Boolean>(c.getUserName(),c.getUserName(), null)) ;
    }
    for(User c : checkedList_.values()) {
      UIFormCheckBoxInput uiInput = getUIFormCheckBoxInput(c.getUserName()) ;
      if(uiInput != null) uiInput.setChecked(true) ;
    }
    return users ;
  }
  
  public List<User> getListAllUsers() throws Exception {
  	List<User> users = FAQUtils.getAllUser() ;
  	 return users ;
  }
  public void setUserList(List<User> userList) throws Exception {
  	ObjectPageList objPageList = new ObjectPageList(userList, 10) ;
    uiPageList_.setPageList(objPageList) ;
  }
  
  public void setAlreadyCheckedUser(List<User> alreadyCheckedUser) throws Exception {
    for (User ct : alreadyCheckedUser) {
      checkedList_.put(ct.getUserName(), ct) ;
    }
  }
  
  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  public UIPageIterator  getUIPageIterator() {  return uiPageList_ ; }

  public long getAvailablePage(){ return uiPageList_.getAvailablePage() ; }

  public long getCurrentPage() { return uiPageList_.getCurrentPage(); }

  protected void updateCurrentPage(int page) throws Exception{
    uiPageList_.setCurrentPage(page) ;
  }
  
  @SuppressWarnings("unchecked")
  public List<User> getCheckedUser() throws Exception {
    List<User> userList = new ArrayList<User>();
    for (User user : new ArrayList<User>(uiPageList_.getCurrentPageData())) {
      UIFormCheckBoxInput<Boolean> uiCheckbox = getChildById(user.getUserName());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
      	userList.add(user);
      }
    }
    return userList;
  }

  
  static public class AddActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource() ;
      List<User> checkedUser = uiAddressForm.getCheckedUser();
      if(checkedUser.size() <= 0) {
        UIApplication uiApp = uiAddressForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressEmailsForm.msg.user-email-required",null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UIFAQPortlet uiPortlet = uiAddressForm.getAncestorOfType(UIFAQPortlet.class) ;
      String toAddress = "";
      StringBuffer sb = new StringBuffer() ;
      for (User ct : checkedUser) {
        uiAddressForm.newCheckedList_.put(ct.getUserName(), ct) ;
      }
      for (User user : uiAddressForm.newCheckedList_.values()) {
        if(user.getEmail() != null)
          toAddress += user.getFullName() + "<" + user.getEmail() + "> ," ;
      }
      List<String> listMail = Arrays.asList( sb.toString().split(",")) ; 
      String email = null ;
      for(User c : checkedUser) {
        email = c.getEmail() ;
        if(!listMail.contains(email)) {
          if(sb != null && sb.length() > 0) sb.append(",") ;
          if(email != null) sb.append(email) ;
        }
      }
      UISendMailForm uiSendMailForm = uiPortlet.findFirstComponentOfType(UISendMailForm.class) ;
      if (uiAddressForm.getRecipientType().equals("To")) {
        uiSendMailForm.setFieldToValue(toAddress) ;
        uiSendMailForm.setToUsers(new ArrayList<User>(uiAddressForm.newCheckedList_.values())) ;
      } else if (uiAddressForm.getRecipientType().equals("AddCc")) {
      	uiSendMailForm.setFieldCCValue(toAddress) ;
      	uiSendMailForm.setAddCCUsers(new ArrayList<User>(uiAddressForm.newCheckedList_.values())) ;
      } else if (uiAddressForm.getRecipientType().equals("AddBcc")) {
      	uiSendMailForm.setFieldBCCValue(toAddress) ;
      	uiSendMailForm.setAddBCCUsers(new ArrayList<User>(uiAddressForm.newCheckedList_.values())) ;
      }
        uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_ ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiSendMailForm) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm.getParent());
    }
  }
  
  static public class ReplaceActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
    	UIAddressEmailsForm uiAddressForm = event.getSource() ;
      List<User> checkedUser = uiAddressForm.getCheckedUser();
      if(checkedUser.isEmpty()) {
        UIApplication uiApp = uiAddressForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAddressEmailsForm.msg.user-email-required",null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      UISendEmailsContainer uiPopupContainer = uiAddressForm.getAncestorOfType(UISendEmailsContainer.class) ;
      uiAddressForm.checkedList_.clear() ;
      uiAddressForm.newCheckedList_.clear() ;
      String toAddress = "";
      StringBuffer sb = new StringBuffer() ;
      for (User ct : checkedUser) {
        uiAddressForm.newCheckedList_.put(ct.getUserName(), ct) ;
      }
      for (User user : uiAddressForm.newCheckedList_.values()) {
        if(user.getEmail() != null) { 
          toAddress += user.getFullName() + "<" + user.getEmail() + "> ," ;
          if(sb.length() > 0) sb.append(",") ;
          sb.append(user.getEmail()) ;
        }
      }
      UISendMailForm uiSendMailForm = uiPopupContainer.getChild(UISendMailForm.class) ;
      if (uiAddressForm.getRecipientType().equals("to")) {
      	uiSendMailForm.setFieldToValue(toAddress) ;
      	uiSendMailForm.setToUsers(checkedUser) ;
      }
      if (uiAddressForm.getRecipientType().equals("AddCc")) {
      	uiSendMailForm.setFieldCCValue(toAddress) ;
      	uiSendMailForm.setAddCCUsers(checkedUser) ;
      }
      if (uiAddressForm.getRecipientType().equals("AddBcc")) {
      	uiSendMailForm.setFieldBCCValue(toAddress) ;
      	uiSendMailForm.setAddBCCUsers(checkedUser) ;
      }
    uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_ ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiSendMailForm) ;
    UIPopupAction uiPopupAction = uiAddressForm.getAncestorOfType(UIPopupAction.class) ;
    uiPopupAction.deActivate() ;
    event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
      }
    }

  
  static public class ChangeGroupActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
    	UIAddressEmailsForm uiAddressForm = event.getSource();  
      String group = ((UIFormSelectBoxWithGroups)uiAddressForm.getChildById(UIAddressEmailsForm.USER_GROUP)).getValue() ;
      if(group.equals("all-group")) uiAddressForm.setUserList(FAQUtils.getAllUser()) ;
      else uiAddressForm.setUserList(FAQUtils.getUserByGroupId(group)) ;
      uiAddressForm.selectedAddressId_ = group ;
      uiAddressForm.getUIStringInput(UIAddressEmailsForm.USER_GROUP).setValue(null) ;
      ((UIFormSelectBoxWithGroups)uiAddressForm.getChildById(UIAddressEmailsForm.USER_GROUP)).setValue(uiAddressForm.selectedAddressId_) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm) ;
    }
  }
  
  static public class SearchUserActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource() ;  
      String text = uiAddressForm.getUIStringInput(UIAddressEmailsForm.USER_SEARCH).getValue() ;
      String group = ((UIFormSelectBoxWithGroups)uiAddressForm.getChildById(UIAddressEmailsForm.USER_GROUP)).getValue() ;
      List<User> listUsers = new ArrayList<User>() ;
      List<User> listResult = new ArrayList<User>() ;
      if(group.equals("all-group")) listUsers = FAQUtils.getAllUser() ;
      else listUsers = FAQUtils.getUserByGroupId(group) ;
      uiAddressForm.selectedAddressId_ = group ;
      try {
      	if(!FAQUtils.isFieldEmpty(text)) {
      		for(User user: listUsers) {
      			if(user.getFullName().contains(text) || user.getEmail().contains(text) || user.getUserName().contains(text)) listResult.add(user); 
      		}
      uiAddressForm.setUserList(listResult) ;
      		((UIFormSelectBoxWithGroups)uiAddressForm.getChildById(UIAddressEmailsForm.USER_GROUP)).setValue(group) ;
      		event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm) ;
      	}
      } catch (Exception e) {
      	UIApplication uiApp = uiAddressForm.getAncestorOfType(UIApplication.class) ;
      	uiApp.addMessage(new ApplicationMessage("UIAddressEmailsForm.msg.search-error-keyword", null)) ;
      	event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      	return ;
      }
    }
  }
  
  static public class CancelActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiAddressForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static public class ShowPageActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
    	UIAddressEmailsForm uiAddressForm = event.getSource() ;
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID)) ;
      for (User user : uiAddressForm.getCheckedUser()) {
        uiAddressForm.newCheckedList_.put(user.getUserName(), user) ;
        uiAddressForm.checkedList_.put(user.getUserName(), user) ;
      }
      uiAddressForm.updateCurrentPage(page) ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);           
    }
  }
}

