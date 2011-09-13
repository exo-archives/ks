/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.webui.control;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;


/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Sep 6, 2011  
 */
@ComponentConfig(
    lifecycle = Lifecycle.class,
    template = "app:/templates/wiki/webui/control/UIEditorTabs.gtmpl"
)
public class UIEditorTabs extends UIWikiExtensionContainer {
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.control.UIEditorTabs";

  @Override
  public String getExtensionType() {
    return EXTENSION_TYPE;
  }
}
