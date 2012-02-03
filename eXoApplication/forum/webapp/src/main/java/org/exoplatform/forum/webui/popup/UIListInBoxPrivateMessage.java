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

import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 3, 2008 - 9:48:51 AM  
 */
@ComponentConfig(
    template = "app:/templates/forum/webui/popup/UIListInBoxPrivateMessage.gtmpl",
    events = {
      @EventConfig(listeners = UIListInBoxPrivateMessage.ViewMessageActionListener.class),
      @EventConfig(listeners = UIListInBoxPrivateMessage.DeleteMessageActionListener.class,confirm="UIPrivateMessageForm.confirm.Delete-message"),
      @EventConfig(listeners = UIListInBoxPrivateMessage.ReplyMessageActionListener.class)
    }
)
public class UIListInBoxPrivateMessage extends UIContainer {
  private ForumService              forumService;

  private UserProfile               userProfile      = null;

  private List<ForumPrivateMessage> listInbox        = null;

  private String                    userName         = ForumUtils.EMPTY_STR;

  private boolean                   isRenderIterator = false;

  public UIListInBoxPrivateMessage() throws Exception {
    forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    addChild(UIForumPageIterator.class, null, "PageListInBoxMessage");
  }

  protected UserProfile getUserProfile() throws Exception {
    if (userProfile == null) {
      userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile();
    }
    this.userName = userProfile.getUserId();
    return userProfile;
  }

  protected boolean isRenderIterator() {
    return isRenderIterator;
  }

  @SuppressWarnings("unchecked")
  protected List<ForumPrivateMessage> getListInBoxPrivateMessage() throws Exception {
    JCRPageList pageList = this.forumService.getPrivateMessage(userName, Utils.RECEIVE_MESSAGE);
    UIForumPageIterator forumPageIterator = this.getChild(UIForumPageIterator.class);
    forumPageIterator.updatePageList(pageList);
    pageList.setPageSize(10);
    int page = forumPageIterator.getPageSelected();
    this.listInbox = pageList.getPage(page);
    if (pageList.getAvailable() > 10) {
      isRenderIterator = true;
    }
    return this.listInbox;
  }

  private ForumPrivateMessage getPrivateMessage(String id) throws Exception {
    List<ForumPrivateMessage> list = this.listInbox;
    for (ForumPrivateMessage forumPrivateMessage : list) {
      if (forumPrivateMessage.getId().equals(id))
        return forumPrivateMessage;
    }
    return null;
  }

  static public class ViewMessageActionListener extends EventListener<UIListInBoxPrivateMessage> {
    public void execute(Event<UIListInBoxPrivateMessage> event) throws Exception {
      UIListInBoxPrivateMessage uicontainer = event.getSource();
      String objctId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!ForumUtils.isEmpty(objctId)) {
        UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
        try {
          uicontainer.forumService.saveReadMessage(objctId, uicontainer.userName, Utils.RECEIVE_MESSAGE);
          ForumPrivateMessage privateMessage = uicontainer.getPrivateMessage(objctId);
          UIPopupContainer popupContainer = uicontainer.getAncestorOfType(UIPopupContainer.class);
          UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
          UIViewPrivateMessageForm privateMessageForm = popupAction.activate(UIViewPrivateMessageForm.class, 650);
          privateMessageForm.setPrivateMessage(privateMessage);
          event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
          forumPortlet.getUserProfile();
        } catch (Exception e) {          
          event.getRequestContext()
               .getUIApplication()
               .addMessage(new ApplicationMessage("UIListInBoxPrivateMessage.msg.fail-view", null, ApplicationMessage.WARNING));
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      }
    }
  }

  static public class DeleteMessageActionListener extends EventListener<UIListInBoxPrivateMessage> {
    public void execute(Event<UIListInBoxPrivateMessage> event) throws Exception {
      UIListInBoxPrivateMessage uicontainer = event.getSource();
      String objctId = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!ForumUtils.isEmpty(objctId)) {
        uicontainer.forumService.removePrivateMessage(objctId, uicontainer.userName, Utils.RECEIVE_MESSAGE);
        UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
        forumPortlet.getUserProfile();
        event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
      }
    }
  }

  static public class ReplyMessageActionListener extends EventListener<UIListInBoxPrivateMessage> {
    public void execute(Event<UIListInBoxPrivateMessage> event) throws Exception {
      UIListInBoxPrivateMessage uicontainer = event.getSource();
      String objctId = event.getRequestContext().getRequestParameter(OBJECTID);
      ForumPrivateMessage privateMessage = uicontainer.getPrivateMessage(objctId);
      UIPrivateMessageForm privateMessageForm = uicontainer.getParent();
      privateMessageForm.setUpdate(privateMessage, true);
      event.getRequestContext().addUIComponentToUpdateByAjax(privateMessageForm);
    }
  }
}
