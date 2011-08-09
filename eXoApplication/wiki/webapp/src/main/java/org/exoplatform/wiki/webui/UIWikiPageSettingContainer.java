/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
import org.exoplatform.wiki.webui.core.UIWikiContainer;
import org.exoplatform.wiki.webui.popup.UIWikiSettingContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 01 Aug 2011  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIWikiPageSettingContainer extends UIWikiContainer {
  public static final String SETTING_CONTAINER = "UIWikiSettingContainer";
  
  public UIWikiPageSettingContainer() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.SPACESETTING });
    addChild(UIWikiSettingContainer.class, null, SETTING_CONTAINER);
  }
}
