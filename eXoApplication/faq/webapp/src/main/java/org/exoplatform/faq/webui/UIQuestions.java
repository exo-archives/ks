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
package org.exoplatform.faq.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		template =	"app:/templates/faq/webui/UIQuestions.gtmpl" ,
		events = {
				@EventConfig(listeners = UIQuestions.AddCatelogyActionListener.class),
	      @EventConfig(listeners = UIQuestions.AddQuestionActionListener.class)
		}
)
public class UIQuestions extends UIContainer {
	
	public UIQuestions()throws Exception {
		
	}
	static  public class AddCatelogyActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
//    	UIQuestions uiActionBar = event.getSource() ; 
//      UIFAQPortlet uiPortlet = uiActionBar.getAncestorOfType(UIFAQPortlet.class);
//      UIPopupAction uiPopupAction = uiPortlet.findFirstComponentOfType(UIPopupAction.class);
//      UIPopupActionContainer uiPopupContainer = uiPopupAction.createUIComponent(UIPopupActionContainer.class, null, "UIPopupActionAddressContainer");
//      uiPopupAction.activate(uiPopupContainer, 800, 0, true) ;
//      UICatelogyForm uiAddressBookForm = uiPopupContainer.createUIComponent(UIAddressBookForm.class, null, null);
//      uiPopupContainer.addChild(uiAddressBookForm) ;
//      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    	}
    }
	static  public class AddQuestionActionListener extends EventListener<UIQuestions> {
    public void execute(Event<UIQuestions> event) throws Exception {
    	}
    }
}