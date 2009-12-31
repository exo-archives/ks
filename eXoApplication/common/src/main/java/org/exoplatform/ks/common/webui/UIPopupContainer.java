/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.ks.common.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Octo 26, 2007 9:48:18 AM 
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIPopupContainer extends UIContainer implements UIPopupComponent {
	public UIPopupContainer()  {
	}
	
	public <T extends UIPopupAction>void initChildPopupAction(Class<T> popupType, String name) throws Exception {
	   UIPopupAction uiPopupAction = addChild(popupType, null, name + "ChildPopupAction").setRendered(true) ;
	    uiPopupAction.getChild(UIPopupWindow.class).setId(name + "ChildPopupWindow") ;
	}
	
	public void activate() throws Exception {
		// TODO Auto-generated method stub
	}

	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
}