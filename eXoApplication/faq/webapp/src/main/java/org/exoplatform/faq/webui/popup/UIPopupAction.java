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
package org.exoplatform.faq.webui.popup;

import org.exoplatform.ks.common.webui.AbstractPopupAction;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SARL 
 * Author : hung.nguyen@exoplatform.com
 * Aug 02, 2007 9:43:23 AM
 */
@ComponentConfig(lifecycle = Lifecycle.class)
public class UIPopupAction extends AbstractPopupAction {

  @Override
  protected void afterProcessRender(WebuiRequestContext context) {
    // TODO : use the mask layer script to avoid clicking behind the popup
  }

  @Override
  protected String getAncestorName() {

    return "Answers";
  }
}