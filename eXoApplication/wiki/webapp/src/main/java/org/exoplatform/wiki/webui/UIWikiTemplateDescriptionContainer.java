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
package org.exoplatform.wiki.webui;

import java.util.Arrays;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 9 Feb 2011  
 */
@ComponentConfig(
                 lifecycle = UIContainerLifecycle.class,
                 template = "app:/templates/wiki/webui/UIWikiTemplateDescriptionContainer.gtmpl"
)
public class UIWikiTemplateDescriptionContainer extends UIWikiContainer {

  public static final String FIELD_DESCRIPTION = "Description";

  public UIWikiTemplateDescriptionContainer() {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.EDITTEMPLATE, WikiMode.ADDTEMPLATE });
    addChild(new UIFormStringInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null));

  }
}
