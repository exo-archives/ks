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
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumNodeTypes;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.forum.webui.UIForumKeepStickPageIterator;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.ks.bbcode.core.ExtendedBBCodeProvider;
import org.exoplatform.ks.common.TransformHTML;
import org.exoplatform.ks.common.webui.UIPopupAction;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 05-03-2008  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIPageListTopicUnApprove.gtmpl",
    events = {
        @EventConfig(listeners = UIPageListTopicUnApprove.OpenTopicActionListener.class ),
        @EventConfig(listeners = UIPageListTopicUnApprove.ApproveTopicActionListener.class ),
        @EventConfig(listeners = UIForumKeepStickPageIterator.GoPageActionListener.class),
        @EventConfig(listeners = UIPageListTopicUnApprove.CancelActionListener.class,phase = Phase.DECODE )
    }
)
public class UIPageListTopicUnApprove extends UIForumKeepStickPageIterator implements UIPopupComponent {
  private String      categoryId, forumId;

  private int         typeApprove = Utils.APPROVE;

  private List<Topic> topics;

  public UIPageListTopicUnApprove() throws Exception {
    this.setActions(new String[] { "ApproveTopic", "Cancel" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public int getTypeApprove() {
    return typeApprove;
  }

  public void setTypeApprove(int typeApprove) {
    this.typeApprove = typeApprove;
  }

  public void setUpdateContainer(String categoryId, String forumId) {
    this.categoryId = categoryId;
    this.forumId = forumId;
  }

  protected String getTitleInHTMLCode(String s) {
    return TransformHTML.getTitleInHTMLCode(s, new ArrayList<String>((new ExtendedBBCodeProvider()).getSupportedBBCodes()));
  }

  @SuppressWarnings("unchecked")
  protected List<Topic> getTopicsUnApprove() throws Exception {
    String type = (typeApprove == Utils.WAITING) ? ForumNodeTypes.EXO_IS_WAITING : (typeApprove == Utils.APPROVE) ? ForumNodeTypes.EXO_IS_APPROVED : ForumNodeTypes.EXO_IS_ACTIVE;
    pageList = getForumService().getPageTopic(this.categoryId, this.forumId, "@" + type + "='" + ((typeApprove == Utils.WAITING) ? "true" : "false") + "'", ForumUtils.EMPTY_STR);
    pageList.setPageSize(6);
    maxPage = pageList.getAvailablePage();
    topics = pageList.getPage(pageSelect);
    pageSelect = pageList.getCurrentPage();
    if (topics == null)
      topics = new ArrayList<Topic>();
    for (Topic topic : topics) {
      if (getUICheckBoxInput(topic.getId()) != null) {
        getUICheckBoxInput(topic.getId()).setChecked(false);
      } else {
        addUIFormInput(new UICheckBoxInput(topic.getId(), topic.getId(), false));
      }
    }
    return topics;
  }

  private Topic getTopic(String topicId) throws Exception {
    for (Topic topic : topics) {
      if (topic.getId().equals(topicId))
        return topic;
    }
    return (Topic) getForumService().getObjectNameById(topicId, Utils.TOPIC);
  }

  static public class OpenTopicActionListener extends EventListener<UIPageListTopicUnApprove> {
    public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
      UIPageListTopicUnApprove uiForm = event.getSource();
      String topicId = event.getRequestContext().getRequestParameter(OBJECTID);
      Topic topic = uiForm.getTopic(topicId);
      topic = uiForm.getForumService().getTopicUpdate(topic, false);
      if (topic == null) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIShowBookMarkForm.msg.link-not-found",
                                                                                       null,
                                                                                       ApplicationMessage.WARNING));
        return;
      }
      UIPopupContainer popupContainer = uiForm.getChild(UIPopupContainer.class);
      if (popupContainer == null)
        popupContainer = uiForm.addChild(UIPopupContainer.class, null, "PoupContainerTopic");
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class);
      UIViewTopic viewTopic = popupAction.activate(UIViewTopic.class, 700);
      viewTopic.setTopic(topic);
      viewTopic.setActionForm(new String[] { "Close" });
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction);
    }
  }

  static public class ApproveTopicActionListener extends EventListener<UIPageListTopicUnApprove> {
    public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
      UIPageListTopicUnApprove uiConponent = event.getSource();
      List<Topic> listTopic = new ArrayList<Topic>();
      for (String topicId : uiConponent.getIdSelected()) {
        Topic topic = uiConponent.getTopic(topicId);
        if (topic != null) {
          if (uiConponent.typeApprove == Utils.WAITING)
            topic.setIsWaiting(false);
          else if (uiConponent.typeApprove == Utils.APPROVE)
            topic.setIsApproved(true);
          else
            topic.setIsActive(true);
          listTopic.add(topic);
        }
      }
      if (!listTopic.isEmpty()) {
        uiConponent.getForumService().modifyTopic(listTopic, uiConponent.typeApprove);
      } else {
        uiConponent.warning(uiConponent.getId() + ".sms.notCheck");
        return;
      }
      if (listTopic.size() == uiConponent.topics.size()) {
        UIForumPortlet forumPortlet = uiConponent.getAncestorOfType(UIForumPortlet.class);
        forumPortlet.cancelAction();
        UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
      } else {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiConponent.getParent());
      }
    }
  }

  static public class CancelActionListener extends EventListener<UIPageListTopicUnApprove> {
    public void execute(Event<UIPageListTopicUnApprove> event) throws Exception {
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
      UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
      event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
    }
  }
}
