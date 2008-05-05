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
	private String type_ = null ;
	public static String newPath_ = "" ;
	private List<Category> listCategory = null ;
	public ResultSearchCategory() throws Exception {}
	public void init() throws Exception {System.out.println("====>>>>:::" + type_ );}
	
  @SuppressWarnings("unused")
  private List<Category> getListCategory(){
    return this.listCategory ;
  }
  
  public void setListCategory(List<Category> listCategory) {
    this.listCategory = listCategory ;
  }
  
  public String[] getActions() { return new String[] {"Close"} ; }
  public void activate() throws Exception {
	  // TODO Auto-generated method stub
  }
	public void deActivate() throws Exception {
	  // TODO Auto-generated method stub
  }
  
	static	public class LinkActionListener extends EventListener<ResultSearchCategory> {
		public void execute(Event<ResultSearchCategory> event) throws Exception {
			ResultSearchCategory resultSearch = event.getSource() ;
			UIFAQPortlet faqPortlet = event.getSource().getAncestorOfType(UIFAQPortlet.class) ;
			String categoryId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIQuestions questions = resultSearch.getAncestorOfType(UIQuestions.class) ;
			System.out.print("===>>>>:::" + categoryId) ;
      questions.setCategories() ;
      questions.setListQuestion() ;
      UIBreadcumbs breadcumbs = faqPortlet.findFirstComponentOfType(UIBreadcumbs.class) ;
      String oldPath = breadcumbs.getPaths() ;
      if(oldPath != null && oldPath.trim().length() > 0) {
      	if(!oldPath.contains(categoryId)) {
      		newPath_ = oldPath + "/" +categoryId ;
      		breadcumbs.setUpdataPath(oldPath + "/" +categoryId);
      	}
      } else breadcumbs.setUpdataPath(categoryId);
			event.getRequestContext().addUIComponentToUpdateByAjax(breadcumbs) ;
      UIFAQContainer fAQContainer = questions.getAncestorOfType(UIFAQContainer.class) ;
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

