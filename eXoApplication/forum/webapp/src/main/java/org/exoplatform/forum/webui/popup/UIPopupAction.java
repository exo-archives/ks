/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.webui.popup;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SARL Author : hung.nguyen@exoplatform.com Aug 02,
 * 2007 9:43:23 AM
 */
@ComponentConfig(lifecycle = Lifecycle.class)
public class UIPopupAction extends org.exoplatform.ks.common.webui.UIPopupAction {

  protected void afterProcessRender(WebuiRequestContext context) {
    String parentId = ((UIComponent) this.getParent()).getId();
    context.getJavascriptManager()
           .addOnLoadJavascript("function(){eXo.forum.UIForumPortlet.setMaskLayer('" + parentId
               + "');}");
  }
  


  @Override
  protected String getAncestorName() {
    return "UIForum";
  }
}
