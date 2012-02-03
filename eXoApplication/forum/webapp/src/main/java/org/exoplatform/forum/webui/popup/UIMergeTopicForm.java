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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
    events = {
      @EventConfig(listeners = UIMergeTopicForm.SaveActionListener.class), 
      @EventConfig(listeners = UIMergeTopicForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UIMergeTopicForm extends BaseUIForm implements UIPopupComponent {
  private static final String TITLE       = "title";

  private static final String DESTINATION = "destination";

  private List<Topic>         listTopic;

  public UIMergeTopicForm() throws Exception {
  }

  private void intAddChild() throws Exception {
    List<SelectItemOption<String>> list = new ArrayList<SelectItemOption<String>>();
    if (this.listTopic != null && this.listTopic.size() > 0) {
      for (Topic topic : this.listTopic) {
        list.add(new SelectItemOption<String>(topic.getTopicName(), topic.getId()));
      }
    }
    UIFormSelectBox destination = new UIFormSelectBox(DESTINATION, DESTINATION, list);
    UIFormStringInput titleThread = new UIFormStringInput(TITLE, TITLE, null);
    if (this.listTopic != null && this.listTopic.size() > 0) {
      destination.setValue(this.listTopic.get(0).getId());
      titleThread.setValue(this.listTopic.get(0).getTopicName());
    }
    addUIFormInput(destination);
    addUIFormInput(titleThread);
  }

  public void updateTopics(List<Topic> topics) {
    this.listTopic = topics;
  }

  public void activate() throws Exception {
    intAddChild();
  }

  public void deActivate() throws Exception {
  }

  static public class SaveActionListener extends EventListener<UIMergeTopicForm> {
    public void execute(Event<UIMergeTopicForm> event) throws Exception {
      UIMergeTopicForm uiForm = event.getSource();
      String topicMergeId = uiForm.getUIFormSelectBox(DESTINATION).getValue();
      String topicMergeTitle = uiForm.getUIStringInput(TITLE).getValue();
      if (!ForumUtils.isEmpty(topicMergeTitle)) {
        topicMergeTitle = CommonUtils.encodeSpecialCharInTitle(topicMergeTitle);
        Topic topicMerge = new Topic();
        for (Topic topic : uiForm.listTopic) {
          if (topicMergeId.equals(topic.getId())) {
            topicMerge = topic;
            break;
          }
        }
        String destTopicPath = topicMerge.getPath();
        boolean isMerge = true;
        if (!ForumUtils.isEmpty(destTopicPath)) {
          String temp[] = destTopicPath.split(ForumUtils.SLASH);
          String categoryId = temp[temp.length - 3];
          String forumId = temp[temp.length - 2];
          ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);          
          WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
          ResourceBundle res = context.getApplicationResourceBundle();
          String emailContent = res.getString("UINotificationForm.label.EmailToAuthorMoved");
          for (Topic topic : uiForm.listTopic) {
            if (topicMergeId.equals(topic.getId())) {
              continue;
            }
            try {
              // set link
              String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, "pathId", false);
              forumService.mergeTopic(categoryId + ForumUtils.SLASH + forumId + ForumUtils.SLASH + topic.getId(),
                                      destTopicPath,
                                      emailContent,
                                      link);
            } catch (Exception e) {
              isMerge = false;
              break;
            }
          }
          if (isMerge) {
            topicMerge.setTopicName(topicMergeTitle);
            try {
              List<Topic> list = new ArrayList<Topic>();
              list.add(topicMerge);
              forumService.modifyTopic(list, Utils.CHANGE_NAME);
            } catch (Exception e) {
              uiForm.log.error("Merge topic is fall ", e);
              isMerge = false;
            }
          }
        } else {
          isMerge = false;
        }
        if (!isMerge) {
          uiForm.warning("UIMergeTopicForm.msg.forum-deleted");
        }
      } else {
        uiForm.warning("UIMergeTopicForm.msg.checkEmptyTitle");
        return;
      }
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class CancelActionListener extends EventListener<UIMergeTopicForm> {
    public void execute(Event<UIMergeTopicForm> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }
}
