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

import java.util.List;
import java.util.ResourceBundle;

import javax.jcr.ItemExistsException;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.webui.BaseDataForm;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumDescription;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetail;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.event.EventListener;
/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aus 15, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIMoveTopicForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMoveTopicForm.SaveActionListener.class), 
      @EventConfig(listeners = UIMoveTopicForm.CancelActionListener.class,phase = Phase.DECODE)
    }
)
public class UIMoveTopicForm extends BaseDataForm implements UIPopupComponent {
  private List<Topic> topics;

  private boolean     isFormTopic = false;

  private boolean     isAdmin     = false;

  public boolean isAdmin() {
    return isAdmin;
  }

  public void setAdmin(boolean isAdmin) {
    this.isAdmin = isAdmin;
  }

  public UIMoveTopicForm() throws Exception {
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void updateTopic(String forumId, List<Topic> topics, boolean isFormTopic) throws Exception {
    this.forumId = forumId;
    this.topics = topics;
    this.isMoveTopic = true;
    this.pathTopic = topics.get(0).getPath();
    this.topics.get(0).setEditReason(getUserProfile().getUserId());
    this.isFormTopic = isFormTopic;
  }

  protected boolean getSelectCate(String cateId) throws Exception {
    if (this.topics.get(0).getPath().contains(cateId))
      return true;
    else
      return false;
  }

  static public class SaveActionListener extends BaseEventListener<UIMoveTopicForm> {
    public void onEvent(Event<UIMoveTopicForm> event, UIMoveTopicForm uiForm, final String forumPath) throws Exception {
      if (!ForumUtils.isEmpty(forumPath)) {
        try {
          WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
          ResourceBundle res = context.getApplicationResourceBundle();
          // set link
          String link = ForumUtils.createdForumLink(ForumUtils.TOPIC, "pathId", false);
          //
          uiForm.getForumService().moveTopic(uiForm.topics, forumPath, res.getString("UINotificationForm.label.EmailToAuthorMoved"), link);
          UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
          forumPortlet.removeCacheUserProfile();
          forumPortlet.cancelAction();
          if (uiForm.isFormTopic) {
            UIForumContainer forumContainer = forumPortlet.findFirstComponentOfType(UIForumContainer.class);
            UITopicDetailContainer topicDetailContainer = forumContainer.getChild(UITopicDetailContainer.class);
            forumContainer.setIsRenderChild(false);
            String[] strs = forumPath.split(ForumUtils.SLASH);
            String catgoryId = strs[strs.length - 2], forumId=strs[strs.length - 1];
            UIForumDescription forumDescription = forumContainer.getChild(UIForumDescription.class);
            forumDescription.setForumIds(catgoryId, forumId);
            UITopicDetail topicDetail = topicDetailContainer.getChild(UITopicDetail.class);
            topicDetail.setUpdateForum(uiForm.getForumService().getForum(catgoryId, forumId));
            topicDetail.setUpdateTopic(catgoryId, forumId, uiForm.topics.get(0).getId());
            event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
          } else {
            UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
            event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
          }
        } catch (ItemExistsException e) {
          warning("UIImportForm.msg.ObjectIsExist");
          return;
        } catch (Exception e) {
          warning("UIMoveTopicForm.msg.parent-deleted");
          return;
        }
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIMoveTopicForm> {
    public void execute(Event<UIMoveTopicForm> event) throws Exception {
      UIMoveTopicForm uiForm = event.getSource();
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
