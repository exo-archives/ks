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
package org.exoplatform.wiki.webui.control;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;
import org.exoplatform.wiki.webui.UIWikiAttachmentUploadListForm;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Aug 31, 2011  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIAttachmentContainer extends UIWikiExtensionContainer {
  public static final String EXTENSION_TYPE = "org.exoplatform.wiki.webui.control.UIAttachmentContainer";
  
  public UIAttachmentContainer() throws Exception {
    addChild(UIWikiAttachmentUploadListForm.class, null, null);
  }
  
  @Override
  public String getExtensionType() {
    return EXTENSION_TYPE;
  }
}
