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


import org.exoplatform.forum.service.PruneSetting;
import org.exoplatform.forum.webui.BaseForumForm;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * May 24, 2009 - 11:16:35 PM  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/forum/webui/popup/UIRunPruneForm.gtmpl",
    events = {
      @EventConfig(listeners = UIRunPruneForm.RunActionListener.class),
      @EventConfig(listeners = UIRunPruneForm.CloseActionListener.class, phase=Phase.DECODE)
    }
)
public class UIRunPruneForm extends BaseForumForm implements UIPopupComponent {
  private PruneSetting pruneSetting;

  private long         topicOld = 0;

  public UIRunPruneForm() {

  }

  public void setPruneSetting(PruneSetting pruneSetting) throws Exception {
    this.pruneSetting = pruneSetting;
    this.topicOld = getForumService().checkPrune(pruneSetting);
  }

  public long getTopicOld() {
    return topicOld;
  }

  public void setTopicOld(long topicOld) {
    this.topicOld = topicOld;
  }

  public void activate() throws Exception {
  }

  public void deActivate() throws Exception {
  }

  static public class CloseActionListener extends EventListener<UIRunPruneForm> {
    public void execute(Event<UIRunPruneForm> event) throws Exception {
      event.getSource().cancelChildPopupAction();
    }
  }

  static public class RunActionListener extends EventListener<UIRunPruneForm> {
    public void execute(Event<UIRunPruneForm> event) throws Exception {
      UIRunPruneForm uiform = event.getSource();
      uiform.getForumService().runPrune(uiform.pruneSetting);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiform.getAncestorOfType(UIForumPortlet.class));
      uiform.cancelChildPopupAction();
    }
  }
}
