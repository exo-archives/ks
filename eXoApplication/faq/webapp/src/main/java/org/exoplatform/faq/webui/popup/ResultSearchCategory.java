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
package org.exoplatform.faq.webui.popup;

import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.faq.webui.UIBreadcumbs;
import org.exoplatform.faq.webui.UIFAQContainer;
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
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 5, 2008, 2:26:57 PM
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/ResultSearchCategory.gtmpl",
		events = {
			@EventConfig(listeners = ResultSearchCategory.LinkActionListener.class),
			@EventConfig(listeners = ResultSearchCategory.CloseActionListener.class)
		}
)
public class ResultSearchCategory extends UIForm implements UIPopupComponent{
	private List<Category> listCategory = null ;
	public ResultSearchCategory() throws Exception {}
	
  @SuppressWarnings("unused")
  private List<Category> getListCategory(){
    return this.listCategory ;
  }
  
  public void setListCategory(List<Category> listCategory) {
    this.listCategory = listCategory ;
  }
  
  public void activate() throws Exception {}
	public void deActivate() throws Exception {}
  
	static	public class LinkActionListener extends EventListener<ResultSearchCategory> {
		public void execute(Event<ResultSearchCategory> event) throws Exception {
			ResultSearchCategory resultSearch = event.getSource() ;
			UIFAQPortlet faqPortlet = resultSearch.getAncestorOfType(UIFAQPortlet.class) ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      FAQService faqService = FAQUtils.getFAQService() ;
			UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			uiQuestions.setCategories(categoryId) ;
			uiQuestions.setListQuestion() ;
      UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
      breadcumbs.setUpdataPath(null) ;
      String oldPath = "" ;
      List<String> listPath = faqService.getCategoryPath(FAQUtils.getSystemProvider(), categoryId) ;
      for(int i = listPath.size() -1 ; i >= 0; i --) {
      	oldPath = oldPath + "/" + listPath.get(i);
      }
      String newPath ="FAQService"+oldPath ;
      uiQuestions.setPath(newPath) ;
      breadcumbs.setUpdataPath(newPath);
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
      UIFAQContainer fAQContainer = uiQuestions.getAncestorOfType(UIFAQContainer.class) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(fAQContainer) ;
      faqPortlet.cancelAction() ;
		}
	}
	
	static	public class CloseActionListener extends EventListener<ResultSearchCategory> {
		public void execute(Event<ResultSearchCategory> event) throws Exception {
			UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
			faqPortlet.cancelAction() ;
		}
	}
}

