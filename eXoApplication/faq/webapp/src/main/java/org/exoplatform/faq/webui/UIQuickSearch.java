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



import java.util.List;

import org.exoplatform.faq.service.FAQFormSearch;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.popup.ResultQuickSearch;
import org.exoplatform.faq.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 24, 2008, 1:38:00 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/UIQuickSearch.gtmpl",
		events = {
			@EventConfig(listeners = UIQuickSearch.SearchActionListener.class),			
			@EventConfig(listeners = UIQuickSearch.AdvancedSearchActionListener.class)			
		}
)
public class UIQuickSearch  extends UIForm {
	final static	private String FIELD_SEARCHVALUE = "inputValue" ;
	
	public UIQuickSearch() throws Exception {
		addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
		this.setSubmitAction(this.event("Search")) ;
	}
	
	static public class SearchActionListener extends EventListener<UIQuickSearch> {
		public void execute(Event<UIQuickSearch> event) throws Exception {
			UIQuickSearch uiForm = event.getSource() ;
			UIFormStringInput formStringInput = uiForm.getUIStringInput(FIELD_SEARCHVALUE) ;
			UIFAQPortlet uiPortlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			String text = formStringInput.getValue() ;
			if(text != null && text.trim().length() > 0) {
				FAQService faqService = FAQUtils.getFAQService() ;
				List<FAQFormSearch> list = null ;
				try {
					list = faqService.getAdvancedEmpty(FAQUtils.getSystemProvider(), text, null, null);
				} catch (Exception e) {
					uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
					uiApp.addMessage(new ApplicationMessage("UIQuickSearch.msg.failure", null, ApplicationMessage.WARNING)) ;
					return ;
				}
				UIResultContainer resultcontainer = popupAction.activate(UIResultContainer.class, 700) ;
				ResultQuickSearch result = resultcontainer.getChild(ResultQuickSearch.class) ;
				popupContainer.setId("ResultQuickSearch") ;
				result.setFormSearchs(list);
//				formStringInput.setValue("") ;
			} else {
				uiApp.addMessage(new ApplicationMessage("UIQuickSeach.msg.no-text-to-search", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
  }
	
	static public class AdvancedSearchActionListener extends EventListener<UIQuickSearch> {
		public void execute(Event<UIQuickSearch> event) throws Exception {
			UIQuickSearch uiForm = event.getSource() ;
			UIFAQPortlet uiPortlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIResultContainer resultContainer = popupAction.activate(UIResultContainer.class, 500) ;
			resultContainer.setIsRenderedContainer(1) ;
			UIAdvancedSearchForm uiAdvancedSearchForm = resultContainer.getChild(UIAdvancedSearchForm.class) ;
			resultContainer.setId("AdvanceSearchForm") ;
			uiAdvancedSearchForm.setValue(false, false, false, false, false, false, false, false) ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
  }
}

