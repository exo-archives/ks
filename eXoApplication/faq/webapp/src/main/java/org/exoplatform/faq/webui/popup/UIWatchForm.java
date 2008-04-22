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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIWatchForm.gtmpl",
		events = {
				@EventConfig(listeners = UIWatchForm.SaveActionListener.class),
				@EventConfig(listeners = UIWatchForm.CancelActionListener.class)
		}
)
public class UIWatchForm extends UIForm	{
	public static final String USER_NAME = "userName" ; 
	public static final String EMAIL_ADDRESS = "emailAddress" ;
  
	public UIWatchForm() throws Exception {
		UIFormStringInput userName = new UIFormStringInput(USER_NAME, USER_NAME, null);
		UIFormStringInput emailAddress = new UIFormStringInput(EMAIL_ADDRESS, EMAIL_ADDRESS, null);
		addUIFormInput(userName);
		addUIFormInput(emailAddress);
	}

	
	public String[] getActions() { return new String[] {"Save","Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
	static public class SaveActionListener extends EventListener<UIWatchForm> {
    public void execute(Event<UIWatchForm> event) throws Exception {
			UIWatchForm uiCategory = event.getSource() ;
			UIApplication uiApp = uiCategory.getAncestorOfType(UIApplication.class) ;
      String name = uiCategory.getUIStringInput(UIWatchForm.USER_NAME).getValue() ;
      String email = uiCategory.getUIStringInput(UIWatchForm.EMAIL_ADDRESS).getValue() ;
      if (email == null || email.trim().length() <= 0) {
        uiApp.addMessage(new ApplicationMessage("UIWatchForm.msg.email-required", null,
          ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      FAQService faqService =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;

		}
	}

	static public class CancelActionListener extends EventListener<UIWatchForm> {
    public void execute(Event<UIWatchForm> event) throws Exception {
			UIWatchForm uiCategory = event.getSource() ;						
      UIPopupAction uiPopupAction = uiCategory.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ; ;
		}
	}
	
	
	
}