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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 7 Dec 2010  
 */
@ComponentConfig(
                 template = "app:/templates/wiki/webui/UIWikiPageContainer.gtmpl"
               )
public class UIWikiPageContainer extends UIContainer {
  public UIWikiPageContainer() throws Exception {
    super();
    addChild(UIWikiPageArea.class, null, null);
    addChild(UIWikiBottomArea.class, null, null);
    addChild(UIWikiSearchSpaceArea.class, null, null);
    addChild(UIWikiHistorySpaceArea.class, null, null);
    addChild(UIWikiPageInfo.class, null, null);
  }
}
