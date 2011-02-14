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
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.webui.control.UIPageToolBar;
import org.exoplatform.wiki.webui.core.UIWikiContainer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = Lifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageControlArea.gtmpl"
)
public class UIWikiPageControlArea extends UIWikiContainer {
  
  public static final String TITLE_CONTROL   = "UIWikiPageTitleControlForm_PageControlArea";
  
  public static final String TOOLBAR_NAME= "UIWikiPageControlArea_PageToolBar";
  
  public UIWikiPageControlArea() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.VIEW, WikiMode.VIEWREVISION, WikiMode.PAGEINFO });
    addChild(UIWikiPageTitleControlArea.class, null, TITLE_CONTROL);
    addChild(UIPageToolBar.class, null, TOOLBAR_NAME);    
  }
  
}
