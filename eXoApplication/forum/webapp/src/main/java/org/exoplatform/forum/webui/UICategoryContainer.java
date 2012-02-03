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

import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template = "app:/templates/forum/webui/UICategoryContainer.gtmpl"
)
public class UICategoryContainer extends UIContainer {
  boolean isRenderJump = true;

  boolean isRender_    = true;

  public UICategoryContainer() throws Exception {
    addChild(UICategories.class, null, null).setRendered(true);
    addChild(UICategory.class, null, null).setRendered(false);
    addChild(UICategoriesSummary.class, null, null);
  }

  public void setIsRenderJump(boolean isRenderJump) {
    this.isRenderJump = isRenderJump;
  }

  public void updateIsRender(boolean isRender) {
    isRender_ = isRender;
    getChild(UICategories.class).setRendered(isRender);
    getChild(UICategory.class).setRendered(!isRender);
    UIForumPortlet forumPortlet = getParent();
    if (isRender || forumPortlet.isShowForumActionBar()) {
      forumPortlet.getChild(UIForumActionBar.class).setRendered(!UserHelper.isAnonim());
    } else {
      forumPortlet.getChild(UIForumActionBar.class).setRendered(false);
    }
    this.findFirstComponentOfType(UICategoryInfo.class).setRendered(isRender);
    renderJump();
  }

  public void renderJump() {
    UIForumLinks forumLinks = ((UIForumPortlet) this.getParent()).getChild(UIForumLinks.class);
    if (isRenderJump) {
      forumLinks.setRendered(!isRender_);
    } else {
      forumLinks.setRendered(false);
    }
  }
}
