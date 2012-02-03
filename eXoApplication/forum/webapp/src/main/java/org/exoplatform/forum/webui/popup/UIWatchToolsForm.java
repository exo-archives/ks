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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 27-08-2008 - 04:36:33  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIWatchToolsForm.gtmpl",
    events = {
      @EventConfig(listeners = UIWatchToolsForm.DeleteEmailActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIWatchToolsForm.EditEmailActionListener.class, phase=Phase.DECODE),
      @EventConfig(listeners = UIWatchToolsForm.CloseActionListener.class, phase=Phase.DECODE)
    }
)
public class UIWatchToolsForm extends UIForm implements UIPopupComponent {
  public final String  WATCHTOOLS_ITERATOR = "WatchToolsPageIterator";

  private String       path                = ForumUtils.EMPTY_STR;

  private String[]     emails              = new String[] {};

  private boolean      isTopic             = false;

  @SuppressWarnings("unchecked")
  private JCRPageList  pageList;

  UIForumPageIterator  pageIterator;

  private List<String> listEmail           = new ArrayList<String>();

  private Log          log                 = ExoLogger.getLogger(UIWatchToolsForm.class);

  public UIWatchToolsForm() throws Exception {
    pageIterator = addChild(UIForumPageIterator.class, null, WATCHTOOLS_ITERATOR);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean getIsTopic() {
    return isTopic;
  }

  public void setIsTopic(boolean isTopic) {
    this.isTopic = isTopic;
  }

  public String[] getEmails() throws Exception {
    emails = getListEmail().toArray(new String[] {});
    return emails;
  }

  @SuppressWarnings("deprecation")
  public void setEmails(String[] emails) {
    listEmail.clear();
    listEmail.addAll(Arrays.asList(emails));
    pageList = new ForumPageList(6, listEmail.size());
    pageList.setPageSize(6);
    pageIterator = this.getChild(UIForumPageIterator.class);
    pageIterator.updatePageList(pageList);
    try {
      if (pageIterator.getInfoPage().get(3) <= 1)
        pageIterator.setRendered(false);
    } catch (Exception e) {
      log.error("\nA UIComponent could not rendered: ", e);
    }
  }

  @SuppressWarnings("unchecked")
  public List<String> getListEmail() {
    int pageSelect = pageIterator.getPageSelected();
    List<String> list = new ArrayList<String>();
    list.addAll(this.pageList.getPageList(pageSelect, this.listEmail));
    if (list.isEmpty()) {
      while (list.isEmpty() && pageSelect > 1) {
        list.addAll(this.pageList.getPageList(--pageSelect, this.listEmail));
        pageIterator.setSelectPage(pageSelect);
      }
    }
    return list;
  }

  public void setUnWatchEmail(String[] emails, String unwatchEmail) {
    if (emails.length == 1) {
      setEmails(emails);
    } else if (emails.length > 1) {
      List<String> temp = new ArrayList<String>();
      for (String em : emails) {
        if (!em.equals(unwatchEmail)) {
          temp.add(em);
        }
      }
      String[] tempEmails = (String[]) temp.toArray(new String[0]);
      setEmails(tempEmails);
    }
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class DeleteEmailActionListener extends EventListener<UIWatchToolsForm> {
    public void execute(Event<UIWatchToolsForm> event) throws Exception {
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      UIWatchToolsForm uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      try {
        String path = uiForm.path;
        forumService.removeWatch(1, path, ForumUtils.SLASH + email);
        String[] strings = new String[(uiForm.listEmail.size() - 1)];
        int j = 0;
        for (String string : uiForm.listEmail) {
          if (string.equals(email))
            continue;
          strings[j] = string;
          ++j;
        }
        uiForm.setEmails(strings);
        if (uiForm.getIsTopic()) {
          UITopicDetail topicDetail = forumPortlet.findFirstComponentOfType(UITopicDetail.class);
          topicDetail.setIsEditTopic(true);
          uiForm.isTopic = false;
          event.getRequestContext().addUIComponentToUpdateByAjax(topicDetail);
        } else if (path.indexOf(Utils.CATEGORY) < path.lastIndexOf(Utils.FORUM)) {
          UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
          topicContainer.setIdUpdate(true);
          event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
        } else {
          UICategory uicategory = forumPortlet.findFirstComponentOfType(UICategory.class);
          uicategory.setIsEditCategory(true);
          event.getRequestContext().addUIComponentToUpdateByAjax(uicategory);
        }
        event.getRequestContext().addUIComponentToUpdateByAjax(uiForm);
      } catch (Exception e) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UIWatchToolsForm.msg.fail-delete-email", null, ApplicationMessage.WARNING));
        ((PortalRequestContext) event.getRequestContext().getParentAppRequestContext()).ignoreAJAXUpdateOnPortlets(true);        
      }
    }
  }

  static public class EditEmailActionListener extends EventListener<UIWatchToolsForm> {
    public void execute(Event<UIWatchToolsForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }

  static public class CloseActionListener extends EventListener<UIWatchToolsForm> {
    public void execute(Event<UIWatchToolsForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
