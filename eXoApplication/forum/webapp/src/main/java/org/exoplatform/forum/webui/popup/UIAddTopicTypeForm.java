/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.TopicType;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.forum.webui.UITopicContainer;
import org.exoplatform.forum.webui.UITopicDetailContainer;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputIconSelector;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 1, 2009 - 10:56:38 AM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
    events = {
      @EventConfig(listeners = UIAddTopicTypeForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddTopicTypeForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIAddTopicTypeForm extends BaseForumForm implements UIPopupComponent {
  public static final String FIELD_TOPICTYPENAME_INPUT = "topicTypeName";

  public static final String FIELD_TOPICTYPEICON_TAB   = "topicTypeIcon";

  private TopicType          topicType;

  private boolean            isEdit                    = false;

  public UIAddTopicTypeForm() throws Exception {
    UIFormStringInput topicTypeName = new UIFormStringInput(FIELD_TOPICTYPENAME_INPUT, FIELD_TOPICTYPENAME_INPUT, null);
    topicTypeName.addValidator(MandatoryValidator.class);
    UIFormInputIconSelector uiIconSelector = new UIFormInputIconSelector(FIELD_TOPICTYPEICON_TAB, FIELD_TOPICTYPEICON_TAB);
    uiIconSelector.setSelectedIcon("IconsView");
    addUIFormInput(topicTypeName);
    addUIFormInput(uiIconSelector);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void setTopicType(TopicType topicType) {
    this.topicType = topicType;
    this.isEdit = true;
    getUIStringInput(FIELD_TOPICTYPENAME_INPUT).setValue(CommonUtils.decodeSpecialCharToHTMLnumber(topicType.getName()));
    ((UIFormInputIconSelector) getChild(UIFormInputIconSelector.class)).setSelectedIcon(topicType.getIcon());
  }

  private boolean checkIsSameName(ForumService forumService, String name) throws Exception {
    List<TopicType> topicTs = forumService.getTopicTypes();
    for (TopicType topicT : topicTs) {
      if (topicT.getName().equalsIgnoreCase(name))
        return true;
    }
    return false;
  }

  static public class CancelActionListener extends EventListener<UIAddTopicTypeForm> {
    public void execute(Event<UIAddTopicTypeForm> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }

  static public class SaveActionListener extends EventListener<UIAddTopicTypeForm> {
    public void execute(Event<UIAddTopicTypeForm> event) throws Exception {
      UIAddTopicTypeForm topicTypeForm = event.getSource();
      String typeName = topicTypeForm.getUIStringInput(FIELD_TOPICTYPENAME_INPUT).getValue();
      UIFormInputIconSelector uiIconSelector = topicTypeForm.getChild(UIFormInputIconSelector.class);
      String typeIcon = uiIconSelector.getSelectedIcon();
      TopicType topicType = new TopicType();
      if (topicTypeForm.isEdit) {
        topicType = topicTypeForm.topicType;
      }
      while (typeName.indexOf("  ") >= 0) {
        typeName = StringUtils.replace(typeName, "  ", " ");
      }
      typeName = CommonUtils.encodeSpecialCharInTitle(typeName);
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      if (!typeName.equalsIgnoreCase(topicType.getName()) && topicTypeForm.checkIsSameName(forumService, typeName)) {
        topicTypeForm.warning("UIAddTopicTypeForm.smg.SameNameType");
        return;
      }
      topicType.setName(typeName.trim());
      topicType.setIcon(typeIcon);
      forumService.saveTopicType(topicType);
      UIForumPortlet forumPortlet = topicTypeForm.getAncestorOfType(UIForumPortlet.class);
      if (topicTypeForm.isEdit) {
        if (forumPortlet.getChild(UIForumContainer.class).isRendered() && !forumPortlet.findFirstComponentOfType(UITopicDetailContainer.class).isRendered()) {
          UITopicContainer topicContainer = forumPortlet.findFirstComponentOfType(UITopicContainer.class);
          topicContainer.setTopicType(topicType.getId());
          event.getRequestContext().addUIComponentToUpdateByAjax(topicContainer);
        }
      }
      topicTypeForm.isEdit = false;
      try {
        UITopicForm topicForm = forumPortlet.findFirstComponentOfType(UITopicForm.class);
        topicForm.addNewTopicType();
        event.getRequestContext().addUIComponentToUpdateByAjax(topicForm);
      } catch (Exception e) {
        UITopicTypeManagerForm typeManagerForm = topicTypeForm.getAncestorOfType(UIForumPortlet.class).findFirstComponentOfType(UITopicTypeManagerForm.class);
        event.getRequestContext().addUIComponentToUpdateByAjax(typeManagerForm);
      }
      topicTypeForm.cancelChildPopupAction();
    }
  }
}
