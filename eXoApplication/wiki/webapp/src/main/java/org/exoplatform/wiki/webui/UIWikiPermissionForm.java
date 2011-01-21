/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.organization.UIGroupMembershipSelector;
import org.exoplatform.webui.organization.account.UIGroupSelector;
import org.exoplatform.webui.organization.account.UIUserSelector;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.IDType;
import org.exoplatform.wiki.service.Permission;
import org.exoplatform.wiki.service.PermissionEntry;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.core.UIWikiForm;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Jan 5, 2011  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPermissionForm.gtmpl",
  events = {
    @EventConfig(listeners = UIWikiPermissionForm.AddOwnerActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.SelectUserActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.SelectGroupActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.SelectMembershipActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.DeleteOwnerActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.SaveActionListener.class),
    @EventConfig(listeners = UIWikiPermissionForm.CloseActionListener.class)
  }
)
public class UIWikiPermissionForm extends UIWikiForm implements UIPopupComponent {

  private List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>();
  
  private Scope scope;
  
  public static String DELETE_ACTION = "DeleteOwner";

  public static enum Scope {
    WIKI, PAGE
  }
  
  public UIWikiPermissionForm() throws Exception{
    
    UIPermissionGrid permissionGrid = addChild(UIPermissionGrid.class, null, null);
    permissionGrid.setPermissionEntries(this.permissionEntries);
    
    UIFormInputWithActions owner = new UIFormInputWithActions("UIWikiPermissionOwner");
    owner.addUIFormInput(new UIFormStringInput("owner", "owner", null));
    List<ActionData> actions = new ArrayList<ActionData>();
    ActionData addOwner = new ActionData();
    addOwner.setActionListener("AddOwner");
    addOwner.setActionType(ActionData.TYPE_ICON);
    addOwner.setActionName("AddOwner");
    addOwner.setCssIconClass("Add");
    actions.add(addOwner);
    /*ActionData selectUser = new ActionData();
    selectUser.setActionListener("SelectUser");
    selectUser.setActionType(ActionData.TYPE_ICON);
    selectUser.setActionName("SelectUser");
    selectUser.setCssIconClass("SearchIcon");
    actions.add(selectUser);
    ActionData selectGroup = new ActionData();
    selectGroup.setActionListener("SelectGroup");
    selectGroup.setActionType(ActionData.TYPE_ICON);
    selectGroup.setActionName("SelectGroup");
    selectGroup.setCssIconClass("SearchIcon");
    actions.add(selectGroup);
    ActionData selectMembership = new ActionData();
    selectMembership.setActionListener("SelectMembership");
    selectMembership.setActionType(ActionData.TYPE_ICON);
    selectMembership.setActionName("SelectMembership");
    selectMembership.setCssIconClass("SearchIcon");
    actions.add(selectMembership);*/
    owner.setActionField("owner", actions);
    
    addChild(owner);
    
    setActions(new String[]{"Save", "Close"});
  }

  public Scope getScope() {
    return scope;
  }

  public void setScope(Scope scope) {
    this.scope = scope;
    if (Scope.WIKI.equals(this.scope)) {
      this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.EDITPAGE,
          WikiMode.ADDPAGE, WikiMode.DELETECONFIRM, WikiMode.VIEWREVISION, WikiMode.SHOWHISTORY,
          WikiMode.ADVANCEDSEARCH });
    } else if (Scope.PAGE.equals(this.scope)) {
      this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW });
    }
  }

  public void setPermission(List<PermissionEntry> permissionEntries) throws Exception {
    this.permissionEntries = permissionEntries;
    UIPermissionGrid permissionGrid = getChild(UIPermissionGrid.class);
    permissionGrid.setPermissionEntries(this.permissionEntries);
  }
  
  @Override
  public void activate() throws Exception {
  }

  @Override
  public void deActivate() throws Exception {
  }
  
  private void processPostAction() throws Exception {
    UIPermissionGrid permissionGrid = getChild(UIPermissionGrid.class);
    List<UIComponent> permissionEntries = permissionGrid.getChildren();
    List<PermissionEntry> permEntries = new ArrayList<PermissionEntry>();
    for (UIComponent uiPermissionEntry : permissionEntries) {
      PermissionEntry permissionEntry = ((UIWikiPermissionEntry) uiPermissionEntry).getPermissionEntry();
      Permission[] permissions = permissionEntry.getPermissions();
      for (Permission permission : permissions) {
        UIFormCheckBoxInput<Boolean> checkboxInput = ((UIWikiPermissionEntry) uiPermissionEntry).getChildById(permission.getPermissionType()
                                                                                                                        .toString()
            + permissionEntry.getId());
        permission.setAllowed(checkboxInput.getValue());
      }
      permEntries.add(permissionEntry);
    }
    setPermission(permEntries);
  }
  
  private HashMap<String, String[]> convertToPermissionMap(List<PermissionEntry> permissionEntries) {
    HashMap<String, String[]> permissionMap = new HashMap<String, String[]>();
    for (PermissionEntry permissionEntry : permissionEntries) {
      Permission[] permissions = permissionEntry.getPermissions();
      List<String> permlist = new ArrayList<String>();
      for (int i = 0; i < permissions.length; i++) {
        Permission permission = permissions[i];
        if (permission.isAllowed()) {
          if (permission.getPermissionType().equals(PermissionType.VIEWPAGE)) {
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.READ);
          } else if (permission.getPermissionType().equals(PermissionType.EDITPAGE)) {
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.ADD_NODE);
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.REMOVE);
            permlist.add(org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY);
          }
        }
      }
      if (permlist.size() > 0) {
        permissionMap.put(permissionEntry.getId(), permlist.toArray(new String[permlist.size()]));
      }
    }
    return permissionMap;
  }
  
  static public class AddOwnerActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      Scope scope = uiWikiPermissionForm.getScope();
      PermissionEntry permissionEntry = new PermissionEntry();
      Permission[] permissions = null;
      if (Scope.WIKI.equals(scope)) {
        permissions = new Permission[4];
        permissions[0] = new Permission();
        permissions[0].setPermissionType(PermissionType.VIEWPAGE);
        permissions[1] = new Permission();
        permissions[1].setPermissionType(PermissionType.EDITPAGE);
        permissions[2] = new Permission();
        permissions[2].setPermissionType(PermissionType.ADMINPAGE);
        permissions[3] = new Permission();
        permissions[3].setPermissionType(PermissionType.ADMINSPACE);
      } else if (Scope.PAGE.equals(scope)) {
        permissions = new Permission[2];
        permissions[0] = new Permission();
        permissions[0].setPermissionType(PermissionType.VIEWPAGE);
        permissions[1] = new Permission();
        permissions[1].setPermissionType(PermissionType.EDITPAGE);
      }
      permissionEntry.setPermissions(permissions);
      UIFormInputWithActions inputWithActions = uiWikiPermissionForm.getChild(UIFormInputWithActions.class);
      UIFormStringInput uiFormStringInput = inputWithActions.getChild(UIFormStringInput.class);
      permissionEntry.setId(uiFormStringInput.getValue());
      permissionEntry.setIdType(IDType.USER);
      uiWikiPermissionForm.processPostAction();
      uiWikiPermissionForm.permissionEntries.add(permissionEntry);
      uiWikiPermissionForm.setPermission(uiWikiPermissionForm.permissionEntries);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }
  
  static public class SelectUserActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L2);
      UIUserSelector uiUserSelector = uiPopupContainer.activate(UIUserSelector.class, 800);
      uiUserSelector.setShowSearch(true);
      uiUserSelector.setShowSearchUser(true) ;
      uiUserSelector.setShowSearchGroup(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
    }
  }
  
  static public class SelectGroupActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L2);
      UIPopupWindow popup = uiPopupContainer.getChild(UIPopupWindow.class);
      UIGroupSelector uiGroupSelector = uiPopupContainer.createUIComponent(UIGroupSelector.class, null, null);
      popup.setUIComponent(uiGroupSelector);
      popup.setRendered(true);
      popup.setShow(true);
      popup.setResizable(true);
      //uiPopupContainer.activate(UIGroupSelector.class, 800);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
    }
  }
  
  static public class SelectMembershipActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPortlet uiWikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer uiPopupContainer = uiWikiPortlet.getPopupContainer(PopupLevel.L2);
      UIPopupWindow popup = uiPopupContainer.getChild(UIPopupWindow.class);
      UIGroupMembershipSelector uiGroupMembershipSelector = uiPopupContainer.createUIComponent(UIGroupMembershipSelector.class, null, null);
      popup.setUIComponent(uiGroupMembershipSelector);
      popup.setRendered(true);
      popup.setShow(true);
      popup.setResizable(true);
      //uiPopupContainer.activate(UIGroupMembershipSelector.class, 800);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupContainer) ;
    }
  }
  
  static public class DeleteOwnerActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      String ownerId = event.getRequestContext().getRequestParameter(OBJECTID);
      for (PermissionEntry permissionEntry : uiWikiPermissionForm.permissionEntries) {
        if (permissionEntry.getId().equals(ownerId)) {
          uiWikiPermissionForm.permissionEntries.remove(permissionEntry);
          break;
        }
      }
      uiWikiPermissionForm.setPermission(uiWikiPermissionForm.permissionEntries);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiWikiPermissionForm);
    }
  }
  
  static public class SaveActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPermissionForm uiWikiPermissionForm = event.getSource();
      Scope scope = uiWikiPermissionForm.getScope();
      UIWikiPortlet wikiPortlet = uiWikiPermissionForm.getAncestorOfType(UIWikiPortlet.class);
      uiWikiPermissionForm.processPostAction();
      if (Scope.WIKI.equals(scope)) {
        WikiService wikiService = uiWikiPermissionForm.getApplicationComponent(WikiService.class);
        WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
        wikiService.setWikiPermission(pageParams.getType(), pageParams.getOwner(), uiWikiPermissionForm.permissionEntries);
      } else if (Scope.PAGE.equals(scope)) {
        PageImpl page = (PageImpl) Utils.getCurrentWikiPage();
        HashMap<String, String[]> permissionMap = uiWikiPermissionForm.convertToPermissionMap(uiWikiPermissionForm.permissionEntries);
        // Include ACL for administrators
        permissionMap.putAll(org.exoplatform.wiki.utils.Utils.getACLForAdmins());
        page.setPagePermission(permissionMap);
        page.setOverridePermission(true);
      }
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();
    }
  }
  
  static public class CloseActionListener extends EventListener<UIWikiPermissionForm> {
    @Override
    public void execute(Event<UIWikiPermissionForm> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.cancelPopupAction();
    }
  }
  
}
