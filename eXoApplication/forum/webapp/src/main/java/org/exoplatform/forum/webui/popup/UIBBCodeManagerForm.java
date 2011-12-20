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

import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
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
    template = "app:/templates/forum/webui/popup/UIBBCodeManagerForm.gtmpl",
    events = {
      @EventConfig(listeners = UIBBCodeManagerForm.AddNewBBCodeActionListener.class), 
      @EventConfig(listeners = UIBBCodeManagerForm.EditBBCodeActionListener.class), 
      @EventConfig(listeners = UIBBCodeManagerForm.DeleteBBCodeActionListener.class), 
      @EventConfig(listeners = UIBBCodeManagerForm.SaveActionListener.class),
      @EventConfig(listeners = UIBBCodeManagerForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UIBBCodeManagerForm extends BaseForumForm implements UIPopupComponent {
  private BBCodeService bbCodeService;

  private List<BBCode>  listBBCode = new ArrayList<BBCode>();

  public UIBBCodeManagerForm() {
    bbCodeService = getApplicationComponent(BBCodeService.class);
    setActions(new String[] { "AddNewBBCode", "Save", "Close" });
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  public void loadBBCodes() throws Exception {
    listBBCode = new ArrayList<BBCode>();
    try {
      listBBCode.addAll(bbCodeService.getAll());
    } catch (Exception e) {
      log.error("failed to set BBCode List", e);
    }
    initCheckBoxActiveBBCode();
  }

  private String getIdCheckBox(String id) {
    return id.contains("=") ? id.replaceFirst("=", "opt") : id;
  }

  public void initCheckBoxActiveBBCode() throws Exception {
    for (BBCode bbc : listBBCode) {
      String id = getIdCheckBox(bbc.getId());
      UICheckBoxInput isActiveBBcode = getUICheckBoxInput(id);
      if (isActiveBBcode == null) {
        isActiveBBcode = new UICheckBoxInput(id, id, false);
        addUIFormInput(isActiveBBcode);
      }
      isActiveBBcode.setChecked(bbc.isActive());
    }
  }

  protected List<BBCode> getListBBcode() throws Exception {
    return listBBCode;
  }

  private BBCode getBBCode(String bbcId) {
    for (BBCode bbCode : listBBCode) {
      if (bbCode.getId().equals(bbcId))
        return bbCode;
    }
    return new BBCode();
  }

  static public class AddNewBBCodeActionListener extends BaseEventListener<UIBBCodeManagerForm> {
    public void onEvent(Event<UIBBCodeManagerForm> event, UIBBCodeManagerForm uiForm, final String objectId) throws Exception {
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      uiForm.openPopup(popupContainer, UIAddBBCodeForm.class, 670, 0);
    }
  }

  static public class EditBBCodeActionListener extends BaseEventListener<UIBBCodeManagerForm> {
    public void onEvent(Event<UIBBCodeManagerForm> event, UIBBCodeManagerForm uiForm, final String bbcodeId) throws Exception {
      UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class);
      BBCode bbCode = uiForm.getBBCode(bbcodeId);
      UIAddBBCodeForm bbcForm = uiForm.openPopup(popupContainer, UIAddBBCodeForm.class, "EditBBCodeForm", 670, 0);
      bbcForm.setEditBBcode(bbCode);
    }
  }

  static public class DeleteBBCodeActionListener extends BaseEventListener<UIBBCodeManagerForm> {
    public void onEvent(Event<UIBBCodeManagerForm> event, UIBBCodeManagerForm uiForm, final String objectId) throws Exception {
      uiForm.bbCodeService.delete(objectId);
      uiForm.loadBBCodes();
      refresh();
    }
  }

  static public class SaveActionListener extends BaseEventListener<UIBBCodeManagerForm> {
    public void onEvent(Event<UIBBCodeManagerForm> event, UIBBCodeManagerForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      List<BBCode> bbCodes = new ArrayList<BBCode>();
      boolean inactiveAll = true;
      try {
        for (BBCode bbc : uiForm.listBBCode) {
          boolean isActive = uiForm.getUICheckBoxInput(uiForm.getIdCheckBox(bbc.getId())).isChecked();
          if (bbc.isActive() != isActive) {
            bbc.setActive(isActive);
            bbCodes.add(bbc);
          }
          if (isActive)
            inactiveAll = false;
        }
        if (uiForm.listBBCode.size() > 0 && inactiveAll) {
          warning("UIBBCodeManagerForm.msg.inactiveAllBBCode");
          return;
        }
        if (!bbCodes.isEmpty()) {
          uiForm.bbCodeService.save(bbCodes);
        }
      } catch (Exception e) {
        uiForm.log.error("failed to save active bbcodes ", e);
      }
      forumPortlet.cancelAction();
    }
  }

  static public class CloseActionListener extends BaseEventListener<UIBBCodeManagerForm> {
    public void onEvent(Event<UIBBCodeManagerForm> event, UIBBCodeManagerForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
