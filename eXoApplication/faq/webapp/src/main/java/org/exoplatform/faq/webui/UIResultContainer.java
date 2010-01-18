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

import org.exoplatform.faq.webui.popup.ResultQuickSearch;
import org.exoplatform.faq.webui.popup.ResultSearchCategory;
import org.exoplatform.faq.webui.popup.ResultSearchQuestion;
import org.exoplatform.faq.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 7, 2008, 9:55:07 AM
 */
@ComponentConfig(
		lifecycle = UIContainerLifecycle.class
)
public class UIResultContainer extends UIContainer implements UIPopupComponent {
	public UIResultContainer() throws Exception {
		addChild(UIAdvancedSearchForm.class, null, null).setRendered(false) ;
		addChild(ResultQuickSearch.class, null, null).setRendered(true) ;
		addChild(ResultSearchCategory.class, null, null).setRendered(false) ;
		addChild(ResultSearchQuestion.class, null, null).setRendered(false) ;
		UIPopupAction childPopup =	addChild(UIPopupAction.class, null, null) ;
		childPopup.setId("FAQChildPoupupAction") ;
		childPopup.getChild(UIPopupWindow.class).setId("FAQChildPopupWindow") ;
	}

	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	public void processRender(WebuiRequestContext context) throws Exception {
		context.getWriter().append("<span class=\"").append(getId()).append("\" id=\"").append(getId()).append("\">");
		renderChildren(context) ;
		context.getWriter().append("</span>");
	}
	
	public void setIsRenderedContainer(int index) {
		boolean isAdvanSearch = false, isQuickSearch = false, isSearchCate = false, isSearchQuesion = false ;
		if(index == 1){	
			isAdvanSearch = true ;
		} else if(index == 2){
			isAdvanSearch = true ;
			isQuickSearch = true ;
		} else if(index == 3){
			isAdvanSearch = true ;
			isSearchQuesion = true;
		} else {
			isAdvanSearch = true ;
			 isSearchCate = true ;
		}
		getChild(UIAdvancedSearchForm.class).setRendered(isAdvanSearch) ;
		getChild(ResultQuickSearch.class).setRendered(isQuickSearch) ;
		getChild(ResultSearchCategory.class).setRendered(isSearchCate) ;
		getChild(ResultSearchQuestion.class).setRendered(isSearchQuesion) ;
	}
}

