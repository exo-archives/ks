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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIViewMemberProfile.gtmpl",
    events = {
      @EventConfig(listeners = UIViewUserProfile.CloseActionListener.class,phase = Phase.DECODE)
    }
)
public class UIViewUserProfile extends BaseForumForm implements UIPopupComponent {
  private UserProfile   userProfileViewer;

  public CommonContact getContact(String userId) throws Exception {
    return ForumSessionUtils.getPersonalContact(userId);
  }

  public UIViewUserProfile() {
  }

  protected boolean isAdmin(String userId) throws Exception {
    return getForumService().isAdminRole(userId);
  }

  protected boolean isOnline(String userId) throws Exception {
    return getForumService().isOnline(userId);
  }

  public void setUserProfileViewer(UserProfile userProfileViewer) {
    this.userProfileViewer = userProfileViewer;
  }

  public UserProfile getUserProfileViewer() {
    return this.userProfileViewer;
  }

  protected String getAvatarUrl() throws Exception {
    return ForumSessionUtils.getUserAvatarURL(userProfileViewer.getUserId(), getForumService());
  }

  protected String[] getLabelProfile() {
    return new String[] { getLabel("userName"), getLabel("firstName"), getLabel("lastName"), getLabel("birthDay"), getLabel("gender"), 
        getLabel("email"), getLabel("jobTitle"), getLabel("location"), getLabel("homePhone"), getLabel("workPhone"), getLabel("website")};
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class CloseActionListener extends EventListener<UIViewUserProfile> {
    public void execute(Event<UIViewUserProfile> event) throws Exception {
      UIViewUserProfile uiForm = event.getSource();
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      if (popupContainer == null) {
        UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
      } else {
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        if (popupAction.findFirstComponentOfType(UIViewUserProfile.class) != null) {
          popupAction.deActivate();
          event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
        } else {
          UIPopupAction popup = popupContainer.getAncestorOfType(UIPopupAction.class);
          popup.deActivate();
          event.getRequestContext().addUIComponentToUpdateByAjax(popup);
        }
      }
    }
  }
}
