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

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.input.UICheckBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 29, 2010 - 4:51:01 AM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIAutoPruneForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAutoPruneForm.PruneSettingActionListener.class),
      @EventConfig(listeners = UIAutoPruneForm.RunPruneActionListener.class),
      @EventConfig(listeners = UIAutoPruneForm.ActivatePruneActionListener.class),
      @EventConfig(listeners = UIAutoPruneForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UIAutoPruneForm extends BaseForumForm implements UIPopupComponent {
  private List<PruneSetting> listPruneSetting = new ArrayList<PruneSetting>();

  public UIAutoPruneForm() {
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  protected List<PruneSetting> getPruneSettings() throws Exception {
    listPruneSetting = new ArrayList<PruneSetting>();
    try {
      UICheckBoxInput autoPruneCheckBox;
      for (PruneSetting pruneSetting : getForumService().getAllPruneSetting()) {
        listPruneSetting.add(pruneSetting);
        autoPruneCheckBox = getUICheckBoxInput(getForumIdOfPrune(pruneSetting));
        if (autoPruneCheckBox == null) {
          autoPruneCheckBox = new UICheckBoxInput(getForumIdOfPrune(pruneSetting), getForumIdOfPrune(pruneSetting), false);
          addUIFormInput(autoPruneCheckBox);
        }
        String title = (pruneSetting.isActive()) ? getLabel("InActive") : getLabel("Active");
        autoPruneCheckBox.setHTMLAttribute("title", title);
        autoPruneCheckBox.setChecked(pruneSetting.isActive());
      }
    } catch (Exception e) {
      log.error("failed to get prune settings", e);
    }
    return listPruneSetting;
  }

  private String getForumIdOfPrune(PruneSetting pruneSetting) {
    String id = pruneSetting.getForumPath();
    return (ForumUtils.isEmpty(id)) ? ForumUtils.EMPTY_STR : id.substring(id.lastIndexOf(ForumUtils.SLASH));
  }

  private PruneSetting getPruneSetting(String forumId) throws Exception {
    for (PruneSetting prune : listPruneSetting) {
      if (prune.getForumPath().indexOf(forumId) > 0)
        return prune;
    }
    return new PruneSetting();
  }

  static public class RunPruneActionListener extends BaseEventListener<UIAutoPruneForm> {
    public void onEvent(Event<UIAutoPruneForm> event, UIAutoPruneForm uiForm, final String pruneId) throws Exception {
      PruneSetting pruneSetting = uiForm.getPruneSetting(pruneId);
      if (pruneSetting.getInActiveDay() == 0) {
        warning("UIAutoPruneForm.sms.not-set-activeDay");
        return;
      } else {
        UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
        UIRunPruneForm pruneForm = uiForm.openPopup(popupContainer, UIRunPruneForm.class, 300, 0);
        pruneForm.setPruneSetting(pruneSetting);
      }
    }
  }

  static public class ActivatePruneActionListener extends BaseEventListener<UIAutoPruneForm> {
    public void onEvent(Event<UIAutoPruneForm> event, UIAutoPruneForm uiForm, final String pruneId) throws Exception {
      PruneSetting pruneSetting = uiForm.getPruneSetting(pruneId);
      boolean isActive = uiForm.getUICheckBoxInput(pruneId).isChecked();
      if (pruneSetting.getInActiveDay() == 0) {
        UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
        UIAutoPruneSettingForm pruneSettingForm = uiForm.openPopup(popupContainer, UIAutoPruneSettingForm.class, 525, 0);
        pruneSettingForm.setPruneSetting(pruneSetting);
        pruneSettingForm.setActivate(isActive);
      } else {
        pruneSetting.setActive(isActive);
        uiForm.getForumService().savePruneSetting(pruneSetting);
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIForumPortlet.class));
    }
  }

  static public class PruneSettingActionListener extends BaseEventListener<UIAutoPruneForm> {
    public void onEvent(Event<UIAutoPruneForm> event, UIAutoPruneForm uiForm, final String pruneId) throws Exception {
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      UIAutoPruneSettingForm pruneSettingForm = uiForm.openPopup(popupContainer, UIAutoPruneSettingForm.class, 525, 0);
      PruneSetting pruneSetting = uiForm.getPruneSetting(pruneId);
      pruneSettingForm.setPruneSetting(pruneSetting);
    }
  }

  static public class CloseActionListener extends BaseEventListener<UIAutoPruneForm> {
    public void onEvent(Event<UIAutoPruneForm> event, UIAutoPruneForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
