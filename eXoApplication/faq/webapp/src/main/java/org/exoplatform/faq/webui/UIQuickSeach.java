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



import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.popup.ResultQuickSearch;
import org.exoplatform.faq.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.faq.webui.popup.UIPopupAction;
import org.exoplatform.faq.webui.popup.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
			@EventConfig(listeners = UIQuickSeach.SearchActionListener.class),			
			@EventConfig(listeners = UIQuickSeach.AdvancedSearchActionListener.class)			
		}
)
public class UIQuickSeach  extends UIForm {
	final static	private String FIELD_SEARCHVALUE = "inputValue" ;
	private List<Category> categoryList_ = new ArrayList<Category>() ;
	
	public UIQuickSeach() throws Exception {
		addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
		this.setSubmitAction("Search") ;
	}
	
	public List<Category> getListCategories(String text) {
		FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		List<Category> list = new ArrayList<Category>() ;
		try {
	    this.categoryList_ = faqService.getAllCategories(FAQUtils.getSystemProvider());
	    for(Category cate : this.categoryList_) {
	    	if(cate.getName().equals(text) || cate.getDescription().equals(text) || cate.getModerators().equals(text)) {
	    		list.add(cate) ;
	    	}
	    }
    } catch (Exception e) {
	    e.printStackTrace();
    }
		return list;
	}
	
	static public class SearchActionListener extends EventListener<UIQuickSeach> {
		public void execute(Event<UIQuickSeach> event) throws Exception {
			UIQuickSeach uiForm = event.getSource() ;
			UIFormStringInput formStringInput = uiForm.getUIStringInput(FIELD_SEARCHVALUE) ;
			UIFAQPortlet uiPortlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIPopupContainer popupContainer = popupAction.createUIComponent(UIPopupContainer.class, null, null) ;
			String text = formStringInput.getValue() ;
			if(text != null && text.trim().length() > 0) {
				UIResultContainer resultcontainer = popupAction.activate(UIResultContainer.class, 800) ;
				ResultQuickSearch result = resultcontainer.getChild(ResultQuickSearch.class) ;
				popupContainer.setId("ResultQuickSearch") ;
				FAQService faqService = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		  	result.setFormSearchs(faqService.getQuickSeach(FAQUtils.getSystemProvider(), text+",,all"));
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
  }
	
	static public class AdvancedSearchActionListener extends EventListener<UIQuickSeach> {
		public void execute(Event<UIQuickSeach> event) throws Exception {
			UIQuickSeach uiForm = event.getSource() ;
			UIFAQPortlet uiPortlet = uiForm.getAncestorOfType(UIFAQPortlet.class);
			UIPopupAction popupAction = uiPortlet.getChild(UIPopupAction.class);
			UIResultContainer resultContainer = popupAction.activate(UIResultContainer.class, 600) ;
			resultContainer.setIsRenderedContainer(1) ;
			resultContainer.getChild(UIAdvancedSearchForm.class) ;
			resultContainer.setId("AdvanceSearchForm") ;
			event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
		}
  }
}

