/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.WebUIUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;
import org.exoplatform.webui.form.input.UICheckBoxInput;
import org.exoplatform.webui.form.wysiwyg.UIFormWYSIWYGInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 29, 2010 - 4:51:01 AM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UINotificationForm.gtmpl",
    events = {
      @EventConfig(listeners = UINotificationForm.GetDefaultMailActionListener.class),
      @EventConfig(listeners = UINotificationForm.SelectTabActionListener.class),
      @EventConfig(listeners = UINotificationForm.SaveActionListener.class),
      @EventConfig(listeners = UINotificationForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UINotificationForm extends BaseForumForm implements UIPopupComponent {

  public static final String  FIELD_ENABLEHEADERSUBJECT_CHECKBOX = "enableHeaderSubject";

  public static final String  FIELD_NOTIFYEMAIL_MOVE_TAB         = "notifyEmailMoveTab";

  public static final String  FIELD_NOTIFYEMAIL_ADDNEW_TAB       = "notifyEmailAddNewTab";

  public static final String  FIELD_HEADERSUBJECT_INPUT          = "headerSubject";

  public static final String  FIELD_NOTIFYEMAIL_TEXTAREA         = "notifyEmail";

  public static final String  FIELD_NOTIFYEMAILMOVED_TEXTAREA    = "notifyEmailMoved";

  private ForumAdministration administration;

  private int                 tabId                              = 0;

  public UINotificationForm() {
    setActions(new String[] { "Save", "Close" });
  }

  public void setInitForm() throws Exception {
    administration = getForumService().getForumAdministration();
    UIFormInputWithActions notifyEmailAddNewTab = new UIFormInputWithActions(FIELD_NOTIFYEMAIL_ADDNEW_TAB);
    UIFormInputWithActions notifyEmailMoveTab = new UIFormInputWithActions(FIELD_NOTIFYEMAIL_MOVE_TAB);
    UICheckBoxInput enableHeaderSubject = initEnableHeaderField();
    UIFormStringInput headerSubject = initEnableHeaderSubjectField();
    UIFormWYSIWYGInput notifyEmail = initNotifyEmailField();
    UIFormWYSIWYGInput notifyEmailMoved = initNotifyMoveField();

    notifyEmailAddNewTab.addUIFormInput(enableHeaderSubject);
    notifyEmailAddNewTab.addUIFormInput(headerSubject);
    notifyEmailAddNewTab.addUIFormInput(notifyEmail);
    notifyEmailMoveTab.addUIFormInput(notifyEmailMoved);

    initEmailField(notifyEmailAddNewTab, FIELD_NOTIFYEMAIL_TEXTAREA);
    initEmailField(notifyEmailMoveTab, FIELD_NOTIFYEMAILMOVED_TEXTAREA);

    addUIFormInput(notifyEmailAddNewTab);
    addUIFormInput(notifyEmailMoveTab);
  }

  private void initEmailField(UIFormInputWithActions notifyEmailTab, String param) throws Exception {
    List<ActionData> actions = new ArrayList<ActionData>();
    ActionData ad = new ActionData();
    ad.setActionListener("GetDefaultMail");
    ad.setActionParameter(param);
    ad.setCssIconClass("Refresh");
    ad.setActionName("TitleResetMail");
    actions.add(ad);
    notifyEmailTab.setActionField(param, actions);
  }

  private UIFormWYSIWYGInput initNotifyMoveField() {
    String value = administration.getNotifyEmailMoved();
    if (ForumUtils.isEmpty(value))
      value = this.getLabel("EmailToAuthorMoved");
    UIFormWYSIWYGInput notifyEmailMoved = new UIFormWYSIWYGInput(FIELD_NOTIFYEMAILMOVED_TEXTAREA, FIELD_NOTIFYEMAILMOVED_TEXTAREA, ForumUtils.EMPTY_STR);
    notifyEmailMoved.setToolBarName("Basic");
    notifyEmailMoved.setFCKConfig(WebUIUtils.getFCKConfig());
    notifyEmailMoved.setValue(value);
    return notifyEmailMoved;
  }

  private UIFormWYSIWYGInput initNotifyEmailField() {
    String value = administration.getNotifyEmailContent();
    if (ForumUtils.isEmpty(value))
      value = this.getLabel("notifyEmailContentDefault");
    UIFormWYSIWYGInput notifyEmail = new UIFormWYSIWYGInput(FIELD_NOTIFYEMAIL_TEXTAREA, FIELD_NOTIFYEMAIL_TEXTAREA, ForumUtils.EMPTY_STR);
    notifyEmail.setToolBarName("Basic");
    notifyEmail.setFCKConfig(WebUIUtils.getFCKConfig());
    notifyEmail.setValue(value);
    return notifyEmail;
  }

  private UIFormStringInput initEnableHeaderSubjectField() {
    UIFormStringInput headerSubject = new UIFormStringInput(FIELD_HEADERSUBJECT_INPUT, FIELD_HEADERSUBJECT_INPUT, null);
    String headerSubject_ = administration.getHeaderSubject();
    if (ForumUtils.isEmpty(headerSubject_))
      headerSubject_ = this.getLabel("notifyEmailHeaderSubjectDefault");
    headerSubject.setValue(headerSubject_);
    return headerSubject;
  }

  private UICheckBoxInput initEnableHeaderField() {
    UICheckBoxInput enableHeaderSubject = new UICheckBoxInput(FIELD_ENABLEHEADERSUBJECT_CHECKBOX, FIELD_ENABLEHEADERSUBJECT_CHECKBOX, false);
    enableHeaderSubject.setChecked(administration.getEnableHeaderSubject());
    return enableHeaderSubject;
  }

  protected boolean tabIsSelected(int tabId) {
    if (this.tabId == tabId)
      return true;
    else
      return false;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class SaveActionListener extends BaseEventListener<UINotificationForm> {
    public void onEvent(Event<UINotificationForm> event, UINotificationForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      UIFormInputWithActions notifyEmailAddNewTab = getChildById(FIELD_NOTIFYEMAIL_ADDNEW_TAB);
      UIFormInputWithActions notifyEmailMoveTab = getChildById(FIELD_NOTIFYEMAIL_MOVE_TAB);
      boolean enableHeaderSubject = (Boolean) notifyEmailAddNewTab.getUICheckBoxInput(FIELD_ENABLEHEADERSUBJECT_CHECKBOX).getValue();
      String headerSubject = notifyEmailAddNewTab.getUIStringInput(FIELD_HEADERSUBJECT_INPUT).getValue();
      String notifyEmail = notifyEmailAddNewTab.getChild(UIFormWYSIWYGInput.class).getValue();

      String notifyEmailMoved = notifyEmailMoveTab.getChild(UIFormWYSIWYGInput.class).getValue();
      if (notifyEmail == null || notifyEmail.replaceAll("<p>", ForumUtils.EMPTY_STR).replaceAll("</p>", ForumUtils.EMPTY_STR).replaceAll("&nbsp;", ForumUtils.EMPTY_STR).trim().length() < 1) {
        warning("UINotificationForm.msg.mailContentInvalid", getLabel(FIELD_NOTIFYEMAIL_TEXTAREA));
        return;
      }
      if (notifyEmailMoved == null || notifyEmailMoved.replaceAll("<p>", ForumUtils.EMPTY_STR).replaceAll("</p>", ForumUtils.EMPTY_STR).replaceAll("&nbsp;", ForumUtils.EMPTY_STR).trim().length() < 1) {
        warning("UINotificationForm.msg.mailContentInvalid", getLabel(FIELD_NOTIFYEMAILMOVED_TEXTAREA));
        return;
      }
      uiForm.administration.setEnableHeaderSubject(enableHeaderSubject);
      uiForm.administration.setHeaderSubject(headerSubject);
      uiForm.administration.setNotifyEmailContent(notifyEmail);
      uiForm.administration.setNotifyEmailMoved(notifyEmailMoved);
      try {
        uiForm.getForumService().saveForumAdministration(uiForm.administration);
      } catch (Exception e) {
        uiForm.log.error("failed to save forum administration", e);
      }
      forumPortlet.cancelAction();
    }

  }

  static public class SelectTabActionListener extends BaseEventListener<UINotificationForm> {
    public void onEvent(Event<UINotificationForm> event, UINotificationForm uiForm, String id) throws Exception {
      uiForm.tabId = Integer.parseInt(id);
      refresh();
    }
  }

  static public class GetDefaultMailActionListener extends BaseEventListener<UINotificationForm> {
    public void onEvent(Event<UINotificationForm> event, UINotificationForm uiForm, String objectId) throws Exception {
      if (objectId.equals(FIELD_NOTIFYEMAIL_TEXTAREA)) {
        ((UIFormInputWithActions) getChildById(FIELD_NOTIFYEMAIL_ADDNEW_TAB)).getChild(UIFormWYSIWYGInput.class).setValue(getLabel("notifyEmailContentDefault"));
      } else {
        ((UIFormInputWithActions) getChildById(FIELD_NOTIFYEMAIL_MOVE_TAB)).getChild(UIFormWYSIWYGInput.class).setValue(getLabel("EmailToAuthorMoved"));
      }
      refresh();
    }
  }

  static public class CloseActionListener extends BaseEventListener<UINotificationForm> {
    public void onEvent(Event<UINotificationForm> event, UINotificationForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
