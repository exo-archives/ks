/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.webui;

import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UISettingForm;
import org.exoplatform.faq.webui.popup.UIWatchManager;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 21, 2008, 10:35:14 PM
 */
@ComponentConfig(
		lifecycle = UIContainerLifecycle.class
)
public class UIWatchContainer  extends UIContainer implements UIPopupComponent{
	public UIWatchContainer() throws Exception {
		addChild(UIWatchManager.class, null, null).setRendered(true) ;
		addChild(UISettingForm.class, null, null).setRendered(false) ;
		UIPopupAction childPopup =	addChild(UIPopupAction.class, null, null) ;
		childPopup.setId("FAQChildWatchPoupupAction") ;
		childPopup.getChild(UIPopupWindow.class).setId("FAQChildWatchPopupWindow") ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void setIsRenderedContainer(int index) {
		boolean isWatchManager = false, isSettingForm = false ;
		if(index == 1){	
			isWatchManager = false ;
			isSettingForm = true ;
		}  else {
			isWatchManager = true ;
			isSettingForm = false ;
		}
		getChild(UIWatchManager.class).setRendered(isWatchManager) ;
		getChild(UISettingForm.class).setRendered(isSettingForm) ;
	}
}

