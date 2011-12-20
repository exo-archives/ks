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

import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 29, 2010 - 4:51:01 AM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UITopicTypeManagerForm.gtmpl",
    events = {
      @EventConfig(listeners = UITopicTypeManagerForm.AddTopicTypeActionListener.class),
      @EventConfig(listeners = UITopicTypeManagerForm.EditTopicTypeActionListener.class),
      @EventConfig(listeners = UITopicTypeManagerForm.DeleteTopicTypeActionListener.class),
      @EventConfig(listeners = UITopicTypeManagerForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UITopicTypeManagerForm extends BaseForumForm implements UIPopupComponent {
  private List<TopicType> listTT = new ArrayList<TopicType>();

  public UITopicTypeManagerForm() {
    setActions(new String[] { "AddTopicType", "Close" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected List<TopicType> getTopicTypes() throws Exception {
    listTT = new ArrayList<TopicType>();
    listTT.addAll(getForumService().getTopicTypes());
    return listTT;
  }

  private TopicType getTopicType(String topicTId) throws Exception {
    for (TopicType topicT : listTT) {
      if (topicT.getId().equals(topicTId))
        return topicT;
    }
    return new TopicType();
  }

  static public class AddTopicTypeActionListener extends BaseEventListener<UITopicTypeManagerForm> {
    public void onEvent(Event<UITopicTypeManagerForm> event, UITopicTypeManagerForm uiForm, final String objectId) throws Exception {
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      uiForm.openPopup(popupContainer, UIAddTopicTypeForm.class, 700, 0);
    }
  }

  static public class EditTopicTypeActionListener extends BaseEventListener<UITopicTypeManagerForm> {
    public void onEvent(Event<UITopicTypeManagerForm> event, UITopicTypeManagerForm uiForm, final String topicTId) throws Exception {
      TopicType topicType = uiForm.getTopicType(topicTId);
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIAddTopicTypeForm topicTypeForm = uiForm.openPopup(popupContainer, UIAddTopicTypeForm.class, "EditTopicTypeForm", 700, 0);
      topicTypeForm.setTopicType(topicType);
    }
  }

  static public class DeleteTopicTypeActionListener extends BaseEventListener<UITopicTypeManagerForm> {
    public void onEvent(Event<UITopicTypeManagerForm> event, UITopicTypeManagerForm uiForm, final String topicTypeId) throws Exception {
      uiForm.getForumService().removeTopicType(topicTypeId);
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
      topicContainer.setTopicType(topicTypeId);
      if (forumPortlet.getChild(UIForumContainer.class).isRendered() && !forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class).isRendered()) {
        event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
      }
      refresh();
    }
  }

  static public class CloseActionListener extends BaseEventListener<UITopicTypeManagerForm> {
    public void onEvent(Event<UITopicTypeManagerForm> event, UITopicTypeManagerForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
