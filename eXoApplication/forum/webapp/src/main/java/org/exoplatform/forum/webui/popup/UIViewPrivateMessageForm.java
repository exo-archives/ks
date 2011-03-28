/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.webui.popup;

import org.exoplatform.forum.rendering.RenderHelper;
import org.exoplatform.forum.rendering.RenderingException;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 10, 2008 - 9:16:19 AM  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIViewPrivateMessageForm.gtmpl",
    events = {
      @EventConfig(listeners = UIViewPrivateMessageForm.CloseActionListener.class,phase = Phase.DECODE)
    }
)
public class UIViewPrivateMessageForm extends UIForm implements UIPopupComponent {
  private ForumPrivateMessage privateMessage;

  private UserProfile         userProfile;

  RenderHelper                renderHelper = new RenderHelper();

  public UIViewPrivateMessageForm() {
  }

  public ForumPrivateMessage getPrivateMessage() {
    return privateMessage;
  }

  public void setPrivateMessage(ForumPrivateMessage privateMessage) {
    this.privateMessage = privateMessage;
  }

  public String renderMessage(String str) throws RenderingException {
    Post post = new Post();
    post.setMessage(str);
    return renderHelper.renderPost(post);
  }

  public UserProfile getUserProfile() {
    return userProfile;
  }

  public void setUserProfile(UserProfile userProfile) {
    this.userProfile = userProfile;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class CloseActionListener extends EventListener<UIViewPrivateMessageForm> {
    public void execute(Event<UIViewPrivateMessageForm> event) throws Exception {
      UIViewPrivateMessageForm uiForm = event.getSource();
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      if (popupContainer == null) {
        UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
      } else {
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
        popupAction.deActivate();
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
      }
    }
  }
}
