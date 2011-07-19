/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.faq.webui.popup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.ListAccess;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.ks.common.webui.UISelectComponent;
import org.exoplatform.ks.common.webui.UISelector;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIBreadcumbs;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UITree;
import org.exoplatform.webui.core.UIBreadcumbs.LocalPath;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          phamtuanchip@yahoo.de
 * Aug 29, 2007 11:57:56 AM 
 */

@ComponentConfigs({ 
  @ComponentConfig(
      template = "app:/templates/faq/webui/popup/UIGroupSelector.gtmpl",
      events = {
          @EventConfig(listeners = UIGroupSelector.ChangeNodeActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectMembershipActionListener.class),
          @EventConfig(listeners = UIGroupSelector.SelectPathActionListener.class)  
      }  
  ),
  @ComponentConfig(
      type = UITree.class, id = "UIKSTreeGroupSelector",
      template = "system:/groovy/webui/core/UITree.gtmpl",
      events = @EventConfig(listeners = UITree.ChangeNodeActionListener.class)
  ),
  @ComponentConfig(
      type = UIBreadcumbs.class, id = "KSBreadcumbGroupSelector",
      template = "system:/groovy/webui/core/UIBreadcumbs.gtmpl",
      events = @EventConfig(listeners = UIBreadcumbs.SelectPathActionListener.class)
  )
})
@SuppressWarnings("unchecked")
public class UIGroupSelector extends UIContainer implements UIPopupComponent, UISelectComponent {

  private UIComponent uiComponent;

  private String      type_           = null;

  private List        selectedGroup_;

  private String      returnFieldName = null;

  private Group        selectGroup_;

  private List<String> listMemberhip;

  public UIGroupSelector() throws Exception {
    UIBreadcumbs uiBreadcumbs = addChild(UIBreadcumbs.class, "KSBreadcumbGroupSelector", "KSBreadcumbGroupSelector");
    UITree tree = addChild(UITree.class, "UIKSTreeGroupSelector", "KSTreeGroupSelector");
    tree.setIcon("GroupAdminIcon");
    tree.setSelectedIcon("PortalIcon");
    tree.setBeanIdField("id");
    tree.setBeanLabelField("label");
    uiBreadcumbs.setBreadcumbsStyle("UIExplorerHistoryPath");
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    UITree tree = getChild(UITree.class);
    if (tree != null && tree.getSibbling() == null) {
      Collection<?> sibblingsGroup = UserHelper.getOrganizationService().getGroupHandler().findGroups(null);
      tree.setSibbling((List) sibblingsGroup);
    }

    Collection<?> collection = UserHelper.getOrganizationService().getMembershipTypeHandler().findMembershipTypes();
    listMemberhip = new ArrayList<String>(5);
    for (Object obj : collection) {
      listMemberhip.add(((MembershipType) obj).getName());
    }
    if (!listMemberhip.contains("*")) {
      listMemberhip.add("*");
    }

    super.processRender(context);
  }

  public UIComponent getReturnComponent() {
    return uiComponent;
  }

  public String getReturnField() {
    return returnFieldName;
  }

  public void setComponent(UIComponent uicomponent, String[] initParams) {
    uiComponent = uicomponent;
    if (initParams == null || initParams.length <= 0)
      return;
    for (int i = 0; i < initParams.length; i++) {
      if (initParams[i].indexOf("returnField") > -1) {
        String[] array = initParams[i].split("=");
        returnFieldName = array[1];
        break;
      }
      returnFieldName = initParams[0];
    }
  }

  public List getChildGroup() throws Exception {
    List children = UserHelper.findGroups(getCurrentGroup());
    return children;
  }

  public boolean isSelectGroup() {
    return TYPE_GROUP.equals(type_);
  }

  public boolean isSelectUser() {
    return TYPE_USER.equals(type_);
  }

  public boolean isSelectMemberShip() {
    return TYPE_MEMBERSHIP.equals(type_);
  }

  public Group getCurrentGroup() {
    return selectGroup_;
  }
  
  public List<String> getList() throws Exception {
    List<String> children = new ArrayList<String>();
    if (TYPE_USER.equals(type_)) {
      ListAccess<User> userPageList = UserHelper.getOrganizationService().getUserHandler().findUsersByGroupId(this.getCurrentGroup().getId());
      User[] user = userPageList.load(0, userPageList.getSize()-1);
      for (int i = 1; i <= userPageList.getSize(); i++) {
        children.add(user[i].getUserName()); 
      }
    } else if (TYPE_MEMBERSHIP.equals(type_)) {
      for (String child : getListMemberhip()) {
        children.add(child);
      }
    } else if (TYPE_GROUP.equals(type_)) {
      Collection groups = UserHelper.findGroups(getCurrentGroup());
      for (Object child : groups) {
        children.add(((Group) child).getGroupName());
      }
    }
    return children;
  }

  public void setSelectedGroups(List groups) {
    if (groups != null) {
      selectedGroup_ = groups;
      getChild(UITree.class).setSibbling(selectedGroup_);
    }
  }

  public void changeGroup(String groupId) throws Exception {
    GroupHandler groupHandler = UserHelper.getOrganizationService().getGroupHandler();
    UIBreadcumbs uiBreadcumb = getChild(UIBreadcumbs.class);
    uiBreadcumb.setPath(getPath(null, groupId));

    UITree tree = getChild(UITree.class);
    Collection<?> sibblingGroup;

    if (groupId == null) {
      sibblingGroup = groupHandler.findGroups(null);
      tree.setSibbling((List) sibblingGroup);
      tree.setChildren(null);
      tree.setSelected(null);
      selectGroup_ = null;
      return;
    }

    selectGroup_ = groupHandler.findGroupById(groupId);
    String parentGroupId = null;
    if (selectGroup_ != null) {
      parentGroupId = selectGroup_.getParentId();
    }
    Group parentGroup = null;
    if (parentGroupId != null) {
      parentGroup = groupHandler.findGroupById(parentGroupId);
    }

    Collection childrenGroup = groupHandler.findGroups(selectGroup_);
    sibblingGroup = groupHandler.findGroups(parentGroup);

    tree.setSibbling((List) sibblingGroup);
    tree.setChildren((List) childrenGroup);
    tree.setSelected(selectGroup_);
    tree.setParentSelected(parentGroup);

    if (selectedGroup_ != null) {
      tree.setSibbling(selectedGroup_);
      tree.setChildren(null);
    }
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setType(String type) {
    this.type_ = type;
  }

  public String getType() {
    return type_;
  }
  
  private List<LocalPath> getPath(List<LocalPath> list, String id) throws Exception {
    if (list == null) {
      list = new ArrayList<LocalPath>(5);
    }
    if (id == null) {
      return list;
    }
    Group group = UserHelper.getOrganizationService().getGroupHandler().findGroupById(id);
    if (group == null) {
      return list;
    }
    list.add(0, new LocalPath(group.getId(), group.getGroupName()));
    getPath(list, group.getParentId());
    return list;
  }

  public List<String> getListMemberhip() {
    return listMemberhip;
  }

  public String event(String name, String beanId) throws Exception {
    UIForm uiForm = getAncestorOfType(UIForm.class);
    if (uiForm != null) {
      return uiForm.event(name, getId(), beanId);
    }
    return super.event(name, beanId);
  }

  static public class SelectMembershipActionListener extends EventListener<UIGroupSelector> {
    public void execute(Event<UIGroupSelector> event) throws Exception {
      String user = event.getRequestContext().getRequestParameter(OBJECTID);
      UIGroupSelector uiGroupSelector = event.getSource();
      String returnField = uiGroupSelector.getReturnField();
      ((UISelector) uiGroupSelector.getReturnComponent()).updateSelect(returnField, user);
      try {
        UIPopupContainer popupContainer = uiGroupSelector.getAncestorOfType(UIPopupContainer.class);
        UIPopupAction popupAction;
        if (((UIComponent) uiGroupSelector.getParent()).getId().equals(popupContainer.getId())) {
          popupAction = popupContainer.getAncestorOfType(UIPopupAction.class);
        } else {
          popupAction = popupContainer.getChild(UIPopupAction.class);
        }
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector.getReturnComponent());
      } catch (NullPointerException e) {
        UIPopupAction uiPopup = uiGroupSelector.getAncestorOfType(UIPopupAction.class);
        uiPopup.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector.getReturnComponent());
      }
    }
  }

  static public class ChangeNodeActionListener extends EventListener<UITree> {
    public void execute(Event<UITree> event) throws Exception {
      UIGroupSelector uiGroupSelector = event.getSource().getAncestorOfType(UIGroupSelector.class);
      String groupId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiGroupSelector.changeGroup(groupId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector);
    }
  }

  static public class SelectPathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
      UIGroupSelector uiGroupSelector = event.getSource().getParent();
      String objectId = event.getRequestContext().getRequestParameter(OBJECTID);
      uiGroupSelector.changeGroup(objectId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiGroupSelector);
    }
  }
}
