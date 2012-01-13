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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
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

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.commons.utils.PageList;
import org.exoplatform.faq.webui.SelectItem;
import org.exoplatform.faq.webui.SelectOption;
import org.exoplatform.faq.webui.UIAnswersPortlet;
import org.exoplatform.faq.webui.UIFormSelectBoxWithGroups;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.Query;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen 
 *          truong.nguyen@exoplatform.com 
 * Oct 2, 2008, 10:47:27 AM
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/faq/webui/popup/UIAddressEmailsForm.gtmpl", 
    events = { 
        @EventConfig(listeners = UIAddressEmailsForm.AddActionListener.class), 
        @EventConfig(listeners = UIAddressEmailsForm.SearchActionListener.class), 
        @EventConfig(listeners = UIAddressEmailsForm.ChangeGroupActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIAddressEmailsForm.ShowPageActionListener.class, phase = Phase.DECODE), 
        @EventConfig(listeners = UIAddressEmailsForm.CancelActionListener.class, phase = Phase.DECODE) 
    }
)
public class UIAddressEmailsForm extends BaseUIForm implements UIPopupComponent {
  public static final String  USER_SEARCH        = "user-search".intern();

  public static final String  USER_GROUP         = "user-group".intern();

  public Map<String, User>    checkedList_       = new HashMap<String, User>();

  public Map<String, User>    newCheckedList_    = new HashMap<String, User>();

  private static final String FILED_ALL_GROUP    = "all-group";

  private String              selectedAddressId_ = "";

  private String              recipientsType_    = "";

  private UIPageIterator      uiPageList_;

  public void setRecipientsType(String type) {
    recipientsType_ = type;
  }

  public String getRecipientType() {
    return recipientsType_;
  }

  public UIAddressEmailsForm() throws Exception {
    addUIFormInput(new UIFormStringInput(USER_SEARCH, USER_SEARCH, null));
    UIFormSelectBoxWithGroups uiSelect = new UIFormSelectBoxWithGroups(USER_GROUP, USER_GROUP, getOptions());
    uiSelect.setOnChange("ChangeGroup");
    addUIFormInput(uiSelect);
    uiPageList_ = new UIPageIterator();
    try {
      setUserList(UserHelper.getPageListUser());
    } catch (Exception e) {
      log.error("Can not set users list, exception: " + e.getMessage());
    }
  }

  public List<SelectItem> getOptions() throws Exception {
    List<SelectItem> options = new ArrayList<SelectItem>();
    options.add(new SelectOption(FILED_ALL_GROUP, FILED_ALL_GROUP));
    try {
      List<String> groupIds = UserHelper.getAllGroupId();
      if (!groupIds.isEmpty()) {
        for (String publicCg : groupIds) {
          options.add(new SelectOption(publicCg, publicCg));
        }
      }
    } catch (Exception e) {
      log.error("Can not get all groups user , exception: ", e);
    }
    return options;
  }

  private void searchUserProfileByKey(String keyWord) throws Exception {
    try {
      Map<String, Object> mapObject = new HashMap<String, Object>();
      OrganizationService service = this.getApplicationComponent(OrganizationService.class);
      keyWord = "*" + keyWord + "*";
      Query q;
      // search by user name
      q = new Query();
      q.setUserName(keyWord);
      ListAccess<User> listAcess = service.getUserHandler().findUsersByQuery(q);
      for (User user : listAcess.load(0, listAcess.getSize())) {
        mapObject.put(user.getUserName(), user);
      }
      
      // search by last name
      listAcess = service.getUserHandler().findUsersByQuery(q);
      for (User user : listAcess.load(0, listAcess.getSize())) {
        mapObject.put(user.getUserName(), user);
      }
      
      // search by firstname
      q = new Query();
      q.setFirstName(keyWord);
      listAcess = service.getUserHandler().findUsersByQuery(q);
      for (User user : listAcess.load(0, listAcess.getSize())) {
        mapObject.put(user.getUserName(), user);
      }
      
      // search by email
      q = new Query();
      q.setEmail(keyWord);
      listAcess = service.getUserHandler().findUsersByQuery(q);
      for (User user : listAcess.load(0, listAcess.getSize())) {
        mapObject.put(user.getUserName(), user);
      }

      ObjectPageList<User> objPageList = new ObjectPageList(Arrays.asList(mapObject.values().toArray()), 10);
      uiPageList_.setPageList(objPageList);
    } catch (Exception e) {
      log.error("Can not search user by key, exception: " + e.getMessage());
    }
  }

  @SuppressWarnings("unchecked")
  public List<User> getUsers() throws Exception {
    List<User> users = new ArrayList<User>(uiPageList_.getCurrentPageData());
    String userName;
    for (User c : users) {
      userName = c.getUserName();
      UICheckBoxInput uiInput = getUICheckBoxInput(userName);
      if (uiInput == null){
        uiInput = new UICheckBoxInput(userName, userName, null);
        addUIFormInput(uiInput);
      }
      if(checkedList_.containsKey(userName)) {
        uiInput.setChecked(true);
      }
      uiInput.setHTMLAttribute("title", c.getFullName());
    }
    return users;
  }

  public void setUserList(PageList<User> userList) throws Exception {
    uiPageList_.setPageList(userList);
  }

  public void setAlreadyCheckedUser(List<User> alreadyCheckedUser) throws Exception {
    for (User ct : alreadyCheckedUser) {
      checkedList_.put(ct.getUserName(), ct);
    }
  }

  public String[] getActions() {
    return new String[] { "Save", "Cancel" };
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public UIPageIterator getUIPageIterator() {
    return uiPageList_;
  }

  public long getAvailablePage() {
    return uiPageList_.getAvailablePage();
  }

  public long getCurrentPage() {
    return uiPageList_.getCurrentPage();
  }

  protected void updateCurrentPage(int page) throws Exception {
    uiPageList_.setCurrentPage(page);
  }

  @SuppressWarnings("unchecked")
  public List<User> getCheckedUser() throws Exception {
    List<User> userList = new ArrayList<User>();
    for (User user : new ArrayList<User>(uiPageList_.getCurrentPageData())) {
      UICheckBoxInput uiCheckbox = getChildById(user.getUserName());
      if (uiCheckbox != null && uiCheckbox.isChecked()) {
        userList.add(user);
      }
    }
    return userList;
  }

  static public class AddActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource();
      List<User> checkedUser = uiAddressForm.getCheckedUser();
      if (checkedUser.size() <= 0) {
        uiAddressForm.warning("UIAddressEmailsForm.msg.user-email-required");
        return;
      }
      UIAnswersPortlet uiPortlet = uiAddressForm.getAncestorOfType(UIAnswersPortlet.class);
      StringBuffer sb = new StringBuffer();
      StringBuffer toAddress = new StringBuffer();
      for (User ct : checkedUser) {
        uiAddressForm.newCheckedList_.put(ct.getUserName(), ct);
      }
      for (User user : uiAddressForm.newCheckedList_.values()) {
        if (user.getEmail() != null)
          toAddress.append(user.getFullName()).append("<").append(user.getEmail()).append("> ,");
      }
      List<String> listMail = Arrays.asList(sb.toString().split(","));
      String email = null;
      for (User c : checkedUser) {
        email = c.getEmail();
        if (!listMail.contains(email)) {
          if (sb != null && sb.length() > 0)
            sb.append(",");
          if (email != null)
            sb.append(email);
        }
      }
      UISendMailForm uiSendMailForm = uiPortlet.findFirstComponentOfType(UISendMailForm.class);
      if (uiAddressForm.getRecipientType().equals("To")) {
        uiSendMailForm.setFieldToValue(toAddress.toString());
        uiSendMailForm.setToUsers(new ArrayList<User>(uiAddressForm.newCheckedList_.values()));
      } else if (uiAddressForm.getRecipientType().equals("AddCc")) {
        uiSendMailForm.setFieldCCValue(toAddress.toString());
        uiSendMailForm.setAddCCUsers(new ArrayList<User>(uiAddressForm.newCheckedList_.values()));
      } else if (uiAddressForm.getRecipientType().equals("AddBcc")) {
        uiSendMailForm.setFieldBCCValue(toAddress.toString());
        uiSendMailForm.setAddBCCUsers(new ArrayList<User>(uiAddressForm.newCheckedList_.values()));
      }
      uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSendMailForm);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm.getParent());
    }
  }

  static public class ReplaceActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource();
      List<User> checkedUser = uiAddressForm.getCheckedUser();
      if (checkedUser.isEmpty()) {
        uiAddressForm.warning("UIAddressEmailsForm.msg.user-email-required");
        return;
      }
      UIPopupContainer uiPopupContainer = uiAddressForm.getAncestorOfType(UIPopupContainer.class);
      uiAddressForm.checkedList_.clear();
      uiAddressForm.newCheckedList_.clear();
      StringBuffer sb = new StringBuffer();
      StringBuffer toAddress = new StringBuffer();
      for (User ct : checkedUser) {
        uiAddressForm.newCheckedList_.put(ct.getUserName(), ct);
      }
      for (User user : uiAddressForm.newCheckedList_.values()) {
        if (user.getEmail() != null) {
          toAddress.append(user.getFullName()).append("<").append(user.getEmail()).append("> ,");
          if (sb.length() > 0)
            sb.append(",");
          sb.append(user.getEmail());
        }
      }
      UISendMailForm uiSendMailForm = uiPopupContainer.getChild(UISendMailForm.class);
      if (uiAddressForm.getRecipientType().equals("to")) {
        uiSendMailForm.setFieldToValue(toAddress.toString());
        uiSendMailForm.setToUsers(checkedUser);
      }
      if (uiAddressForm.getRecipientType().equals("AddCc")) {
        uiSendMailForm.setFieldCCValue(toAddress.toString());
        uiSendMailForm.setAddCCUsers(checkedUser);
      }
      if (uiAddressForm.getRecipientType().equals("AddBcc")) {
        uiSendMailForm.setFieldBCCValue(toAddress.toString());
        uiSendMailForm.setAddBCCUsers(checkedUser);
      }
      uiAddressForm.checkedList_ = uiAddressForm.newCheckedList_;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiSendMailForm);
      uiAddressForm.cancelChildPopupAction();
    }
  }

  static public class ChangeGroupActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource();
      String group = ((UIFormSelectBoxWithGroups) uiAddressForm.getChildById(UIAddressEmailsForm.USER_GROUP)).getValue();
      if (group.equals("all-group"))
        uiAddressForm.setUserList(UserHelper.getPageListUser());
      else
        uiAddressForm.setUserList(UserHelper.getUserPageListByGroupId(group));
      uiAddressForm.selectedAddressId_ = group;
      uiAddressForm.getUIStringInput(UIAddressEmailsForm.USER_GROUP).setValue(null);
      ((UIFormSelectBoxWithGroups) uiAddressForm.getChildById(UIAddressEmailsForm.USER_GROUP)).setValue(uiAddressForm.selectedAddressId_);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);
    }
  }

  static public class CancelActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }

  static public class ShowPageActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource();
      int page = Integer.parseInt(event.getRequestContext().getRequestParameter(OBJECTID));
      for (User user : uiAddressForm.getCheckedUser()) {
        uiAddressForm.newCheckedList_.put(user.getUserName(), user);
        uiAddressForm.checkedList_.put(user.getUserName(), user);
      }
      uiAddressForm.updateCurrentPage(page);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);
    }
  }

  static public class SearchActionListener extends EventListener<UIAddressEmailsForm> {
    public void execute(Event<UIAddressEmailsForm> event) throws Exception {
      UIAddressEmailsForm uiAddressForm = event.getSource();
      String searchValue = ((UIFormStringInput) uiAddressForm.getChildById(UIAddressEmailsForm.USER_SEARCH)).getValue();
      if (searchValue == null || searchValue.trim().length() < 1)
        uiAddressForm.setUserList(UserHelper.getPageListUser());
      else
        uiAddressForm.searchUserProfileByKey(searchValue);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiAddressForm);
    }
  }
}
