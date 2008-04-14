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
package org.exoplatform.faq.webui.popup;

import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UICategoryForm.gtmpl",
		events = {
				@EventConfig(listeners = UICategoryForm.SaveActionListener.class),
				@EventConfig(listeners = UICategoryForm.CancelActionListener.class)
		}
)
public class UICategoryForm extends UIForm	{
	final private static String EVENT_CATEGORY_NAME = "eventCategoryName" ; 
  final private static String DESCRIPTION = "description" ;
  final private static String MODERATOR = "moderator" ;
	public UICategoryForm() throws Exception {
		addUIFormInput(new UIFormStringInput(EVENT_CATEGORY_NAME, EVENT_CATEGORY_NAME, null)) ;
    addUIFormInput(new UIFormTextAreaInput(DESCRIPTION, DESCRIPTION, null)) ;
    addUIFormInput(new UIFormStringInput(MODERATOR, MODERATOR, null)) ;
  }
  protected String getCategoryName() {return getUIStringInput(EVENT_CATEGORY_NAME).getValue() ;}
  protected void setCategoryName(String value) {getUIStringInput(EVENT_CATEGORY_NAME).setValue(value) ;}

  protected String getCategoryDescription() {return getUIStringInput(DESCRIPTION).getValue() ;}
  protected void setCategoryDescription(String value) {getUIFormTextAreaInput(DESCRIPTION).setValue(value) ;}
	
  protected String getModerator() {return getUIStringInput(MODERATOR).getValue() ;}
  protected void setModerator(String value) {getUIStringInput(MODERATOR).setValue(value) ;}
  
	static public class SaveActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;			
			System.out.println("========> Save") ;
			UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
      UIFAQPortlet uiPortlet = uiCategory.getAncestorOfType(UIFAQPortlet.class) ;
      String name = uiCategory.getUIStringInput(UICategoryForm.EVENT_CATEGORY_NAME).getValue() ;
//      if(Utils.isEmptyField(name)) {
//        uiApp.addMessage(new ApplicationMessage("UIEventCategoryForm.msg.name-required", null)) ;
//        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
//        return ;
//      }
		}
	}

	static public class CancelActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
			UICategoryForm uiCategory = event.getSource() ;			
      UIPopupAction uiPopupAction = uiCategory.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
	
	
	
}