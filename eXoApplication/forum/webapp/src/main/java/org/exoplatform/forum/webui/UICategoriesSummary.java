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
    template = "app:/templates/forum/webui/UICategoriesSummary.gtmpl"
)
public class UICategoriesSummary extends UIContainer {
  public UICategoriesSummary() throws Exception {
    addChild(UICategoryInfo.class, null, null);
    addChild(UIForumIconState.class, null, null).updateInfor(true);
  }

  protected void setRenderChild() {
    UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class);
    UICategoryInfo categoryInfo = getChild(UICategoryInfo.class);
    if (categoryInfo.isRendered())
      categoryInfo.setRendered(forumPortlet.isShowStatistics());
    getChild(UIForumIconState.class).setRendered(forumPortlet.isShowIconsLegend());
  }
}
