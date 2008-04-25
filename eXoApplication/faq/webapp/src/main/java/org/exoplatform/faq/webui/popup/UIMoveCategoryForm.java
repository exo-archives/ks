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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIFAQPortlet;
import org.exoplatform.faq.webui.UIQuestions;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@SuppressWarnings({ "unused", "unchecked" })
@ComponentConfig(
		lifecycle = UIFormLifecycle.class ,
		template =	"app:/templates/faq/webui/popup/UIMoveCategoryForm.gtmpl",
		events = {
				@EventConfig(listeners = UIMoveCategoryForm.SaveActionListener.class),
				@EventConfig(listeners = UIMoveCategoryForm.CancelActionListener.class)
		}
)
public class UIMoveCategoryForm extends UIForm	implements UIPopupComponent{
	private String categoryId_ ;
	public UIMoveCategoryForm() throws Exception {}
	
	public void init() {}
	public String getCategoryID() { return categoryId_; }
  public void setCategoryID(String s) { categoryId_ = s ; }
  
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
	@SuppressWarnings("unused")
  private List<Category> getCategories() throws Exception {
		FAQService faqService =	(FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
		List<Category> categorys =	new ArrayList<Category>();
		for (Category category :faqService.getAllCategories(FAQUtils.getSystemProvider())) {
			if( !category.getId().equals(categoryId_) ) {
				categorys.add(category) ;
			}
		}
		return categorys ;
	}
	
	static public class SaveActionListener extends EventListener<UIMoveCategoryForm> {
    public void execute(Event<UIMoveCategoryForm> event) throws Exception {
    	UIMoveCategoryForm moveCategory = event.getSource() ;	
    	UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
    	String destCategoryId = event.getRequestContext().getRequestParameter(OBJECTID);
    	FAQService faqService  = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
    	if (destCategoryId != null && moveCategory.getCategoryID() != null) {
    		faqService.moveCategory(moveCategory.getCategoryID(), destCategoryId, FAQUtils.getSystemProvider()) ;
    	}
    	UIQuestions questions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			questions.setCategories() ;
			event.getRequestContext().addUIComponentToUpdateByAjax(questions) ;
			faqPortlet.cancelAction() ;
		return ;
		}
	}

	static public class CancelActionListener extends EventListener<UIMoveCategoryForm> {
    public void execute(Event<UIMoveCategoryForm> event) throws Exception {
			UIMoveCategoryForm uiCategory = event.getSource() ;			
			UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
			faqPortlet.cancelAction() ;
		}
	}
	
	
	
}