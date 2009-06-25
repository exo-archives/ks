/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 ***************************************************************************/
package org.exoplatform.faq.webui.viewer;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.CategoryInfo;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.QuestionInfo;
import org.exoplatform.faq.service.SubCategoryInfo;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 24, 2009 - 2:26:16 AM  
 */

@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/faq/webui/UIFAQViewerPortlet.gtmpl"
)

public class UIFAQViewerPortlet extends UIPortletApplication{
	public UIFAQViewerPortlet() throws Exception {
		addChild(UIViewer.class, null, null);
  }
	
}
