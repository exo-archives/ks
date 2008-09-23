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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.faq.service.Category;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
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
		template =	"app:/templates/faq/webui/UIBreadcumbs.gtmpl" ,
		events = {
				@EventConfig(listeners = UIBreadcumbs.ChangePathActionListener.class)
		}
)
public class UIBreadcumbs extends UIContainer {
	private List<String> breadcumbs_ = new ArrayList<String>();
	public List<String> paths_ = new ArrayList<String>();
	private String path_ = "FAQService" ;
	public static final String FIELD_FAQHOME_BREADCUMBS = "faqHome" ;
	public UIBreadcumbs()throws Exception {
		breadcumbs_.add("eXo FAQ") ;
		paths_.add("FAQService") ;
	}
	
	public void setUpdataPath(String path) throws Exception {
		if(path != null && path.length() > 0 ) {
			String temp[] = path.split("/") ;
			this.path_ = path ;
			paths_.clear() ;
			breadcumbs_.clear() ;
			paths_.add("FAQService") ;
			String oldPath = "FAQService" ;
			breadcumbs_.add("eXo FAQ") ;
				for (String string : temp) {
					if(string.equals("FAQService")) continue ;
					oldPath = oldPath + "/" + string;
					Category category = FAQUtils.getFAQService().getCategoryById(string, FAQUtils.getSystemProvider()) ;
					String categoryName = category.getName() ;
					breadcumbs_.add(categoryName) ;
					paths_.add(oldPath) ;
				}
		} else {
			paths_.clear() ;
			breadcumbs_.clear() ;
			paths_.add("FAQService") ;
			breadcumbs_.add("eXo FAQ") ;
		}
	}
	
	@SuppressWarnings("unused")
	private String getPath(int index) {
		return this.paths_.get(index) ;
	}
	
	public String getPaths() {
	  return this.path_;
  }
	@SuppressWarnings("unused")
	private int getMaxPath() {
		return breadcumbs_.size() ;
	}
	
	@SuppressWarnings("unused")
	private List<String> getBreadcumbs() throws Exception {
		return breadcumbs_ ;
	}
	
	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
    public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs uiBreadcums = event.getSource() ;			
			String paths = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet faqPortlet = uiBreadcums.getAncestorOfType(UIFAQPortlet.class) ;
			if(paths.equals("FAQService")){
				UIFAQContainer uiContainer = faqPortlet.findFirstComponentOfType(UIFAQContainer.class) ;
				UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
				String categoryId = null;
				uiContainer.updateIsRender(true) ;
				uiQuestions.setCategories(categoryId) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			} else {
				UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
				uiQuestions.setPath(paths) ;
				String cate = paths.substring(paths.lastIndexOf("/")+1, paths.length()) ;
				try {
					uiQuestions.setCategories(cate) ;
				} catch (Exception e) {
					UIApplication uiApplication = uiBreadcums.getAncestorOfType(UIApplication.class) ;
          uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
          UIFAQContainer uiContainer = faqPortlet.findFirstComponentOfType(UIFAQContainer.class) ;
  				uiContainer.updateIsRender(true) ;
  				uiQuestions.setCategories(null) ;
  				uiBreadcums.setUpdataPath("FAQService");
          event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
          return ;
				}
				uiQuestions.setCategories(cate) ;
				uiQuestions.backPath_ = "" ;
				event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
			}
			uiBreadcums.setUpdataPath(paths);
			event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
		}
	}


}