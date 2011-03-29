/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.control.action.core;

import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.commons.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 24 Mar 2011  
 */
public abstract class AbstractFormActionComponent extends AbstractActionComponent {

  @Override
  public String getActionLink() throws Exception {
    String action = getActionName();
    UIForm form = this.getAncestorOfType(UIForm.class);
    if (form != null) {
      if (isSubmit()) {
        return Utils.createFormActionLink(this, action, action);
      } else {
        return form.event(action, this.getId(), action);
      }
    }
    return null;
  }
  
  public abstract boolean isSubmit();

}
