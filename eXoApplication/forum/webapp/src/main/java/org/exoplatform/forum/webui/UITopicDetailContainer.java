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
package org.exoplatform.forum.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template = "app:/templates/forum/webui/UITopicDetailContainer.gtmpl" 
)
public class UITopicDetailContainer extends UIContainer {
  private boolean isRender = true;

  public UITopicDetailContainer() throws Exception {
    addChild(UITopicPoll.class, null, null).setRendered(isRender);
    addChild(UITopicDetail.class, null, null);
  }

  public void setRederPoll(boolean isRender) throws Exception {
    this.isRender = isRender;
  }

  protected boolean getRenderPoll(boolean isShow) {
    boolean isRdByDeff = this.getAncestorOfType(UIForumPortlet.class).isShowPoll();
    if (isShow && isRender && isRdByDeff)
      return true;
    else
      return false;
  }
}
