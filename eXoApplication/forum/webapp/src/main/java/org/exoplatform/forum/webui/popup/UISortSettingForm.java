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

import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UICategory;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormSelectBox;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 29, 2010 - 4:51:01 AM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIFormForum.gtmpl",
    events = {
      @EventConfig(listeners = UISortSettingForm.SaveActionListener.class),
      @EventConfig(listeners = UISortSettingForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UISortSettingForm extends BaseForumForm implements UIPopupComponent {
  public static final String  FIELD_FORUMSORTBY_INPUT     = "forumSortBy";

  public static final String  FIELD_FORUMSORTBYTYPE_INPUT = "forumSortByType";

  public static final String  FIELD_TOPICSORTBY_INPUT     = "topicSortBy";

  public static final String  FIELD_TOPICSORTBYTYPE_INPUT = "topicSortByType";

  private ForumAdministration administration;

  public UISortSettingForm() {
  }

  public void setInitForm() throws Exception {
    administration = getForumService().getForumAdministration();
    UIFormSelectBox forumSortBy = initForumSortField();
    UIFormSelectBox forumSortByType = initForumSortDirectionField();
    UIFormSelectBox topicSortBy = initTopicSortField();
    UIFormSelectBox topicSortByType = initTopicSortDirectionField();

    addUIFormInput(forumSortBy);
    addUIFormInput(forumSortByType);
    addUIFormInput(topicSortBy);
    addUIFormInput(topicSortByType);
  }

  private UIFormSelectBox initTopicSortDirectionField() {
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(this.getLabel("ascending"), "ascending"));
    ls.add(new SelectItemOption<String>(this.getLabel("descending"), "descending"));
    UIFormSelectBox topicSortByType = new UIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT, FIELD_TOPICSORTBYTYPE_INPUT, ls);
    topicSortByType.setValue(administration.getTopicSortByType());
    return topicSortByType;
  }

  private UIFormSelectBox initTopicSortField() {
    String[] idLables = new String[] { "isLock", "createdDate", "modifiedDate", "lastPostDate", "postCount", "viewCount", "numberAttachments" };
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(this.getLabel("threadName"), "name"));
    for (String string : idLables) {
      ls.add(new SelectItemOption<String>(this.getLabel(string), string));
    }
    UIFormSelectBox topicSortBy = new UIFormSelectBox(FIELD_TOPICSORTBY_INPUT, FIELD_TOPICSORTBY_INPUT, ls);
    topicSortBy.setValue(administration.getTopicSortBy());
    return topicSortBy;
  }

  private UIFormSelectBox initForumSortDirectionField() {
    List<SelectItemOption<String>> ls;
    ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(this.getLabel("ascending"), "ascending"));
    ls.add(new SelectItemOption<String>(this.getLabel("descending"), "descending"));
    UIFormSelectBox forumSortByType = new UIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT, FIELD_FORUMSORTBYTYPE_INPUT, ls);
    forumSortByType.setValue(administration.getForumSortByType());
    return forumSortByType;
  }

  private UIFormSelectBox initForumSortField() {
    String[] idLables = new String[] { "forumOrder", "isLock", "createdDate", "modifiedDate", "topicCount", "postCount" };
    List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>();
    ls.add(new SelectItemOption<String>(this.getLabel("forumName"), "name"));
    for (String string : idLables) {
      ls.add(new SelectItemOption<String>(this.getLabel(string), string));
    }
    UIFormSelectBox forumSortBy = new UIFormSelectBox(FIELD_FORUMSORTBY_INPUT, FIELD_FORUMSORTBY_INPUT, ls);
    forumSortBy.setValue(administration.getForumSortBy());
    return forumSortBy;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class SaveActionListener extends BaseEventListener<UISortSettingForm> {
    public void onEvent(Event<UISortSettingForm> event, UISortSettingForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      String forumSortBy = uiForm.getUIFormSelectBox(FIELD_FORUMSORTBY_INPUT).getValue();
      String forumSortByType = uiForm.getUIFormSelectBox(FIELD_FORUMSORTBYTYPE_INPUT).getValue();
      String topicSortBy = uiForm.getUIFormSelectBox(FIELD_TOPICSORTBY_INPUT).getValue();
      String topicSortByType = uiForm.getUIFormSelectBox(FIELD_TOPICSORTBYTYPE_INPUT).getValue();

      if (!forumSortBy.equals(uiForm.administration.getForumSortBy()) || !forumSortByType.equals(uiForm.administration.getForumSortByType())) {
        forumPortlet.findFirstComponentOfType(UICategory.class).setIsEditForum(true);
      }

      ForumAdministration forumAdministration = uiForm.administration;
      forumAdministration.setForumSortBy(forumSortBy);
      forumAdministration.setForumSortByType(forumSortByType);
      forumAdministration.setTopicSortBy(topicSortBy);
      forumAdministration.setTopicSortByType(topicSortByType);
      try {
        uiForm.getForumService().saveForumAdministration(forumAdministration);
      } catch (Exception e) {
        uiForm.log.error("failed to save forum administration", e);
      }
      forumPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class CloseActionListener extends BaseEventListener<UISortSettingForm> {
    public void onEvent(Event<UISortSettingForm> event, UISortSettingForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
