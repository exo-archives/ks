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

import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.webui.BaseEventListener;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormTextAreaInput;

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
      @EventConfig(listeners = UICensorKeywordForm.SaveActionListener.class),
      @EventConfig(listeners = UICensorKeywordForm.CloseActionListener.class, phase = Phase.DECODE)
    }
)
public class UICensorKeywordForm extends BaseForumForm implements UIPopupComponent {
  public static final String  FIELD_CENSOREDKEYWORD_TEXTAREA = "censorKeyword";

  private ForumAdministration administration;

  public UICensorKeywordForm() {
  }

  public void setInitForm() throws Exception {
    administration = getForumService().getForumAdministration();
    UIFormTextAreaInput censorKeyword = new UIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA, FIELD_CENSOREDKEYWORD_TEXTAREA, null);
    censorKeyword.setValue(administration.getCensoredKeyword());
    addUIFormInput(censorKeyword);
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class SaveActionListener extends BaseEventListener<UICensorKeywordForm> {
    public void onEvent(Event<UICensorKeywordForm> event, UICensorKeywordForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      String censoredKeyword = uiForm.getUIFormTextAreaInput(FIELD_CENSOREDKEYWORD_TEXTAREA).getValue();
      censoredKeyword = ForumUtils.removeSpaceInString(censoredKeyword);
      if (!ForumUtils.isEmpty(censoredKeyword)) {
        censoredKeyword = censoredKeyword.toLowerCase();
      }
      uiForm.administration.setCensoredKeyword(censoredKeyword);
      try {
        uiForm.getForumService().saveForumAdministration(uiForm.administration);
      } catch (Exception e) {
        uiForm.log.error("failed to save forum administration", e);
      }
      forumPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet);
    }
  }

  static public class CloseActionListener extends BaseEventListener<UICensorKeywordForm> {
    public void onEvent(Event<UICensorKeywordForm> event, UICensorKeywordForm uiForm, String objId) throws Exception {
      UIForumPortlet forumPortlet = uiForm.getAncestorOfType(UIForumPortlet.class);
      forumPortlet.cancelAction();
    }
  }
}
