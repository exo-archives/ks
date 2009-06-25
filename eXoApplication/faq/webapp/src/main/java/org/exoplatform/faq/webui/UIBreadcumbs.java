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
import org.exoplatform.faq.service.Utils;
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
	public List<String> pathList_ = new ArrayList<String>();
	private String currentPath_ = Utils.CATEGORY_HOME ;
	public static final String FIELD_FAQHOME_BREADCUMBS = "faqHome" ;
	private static final String QUICK_SEARCH = "QuickSearch";
	
	public UIBreadcumbs()throws Exception {
		addChild(UIQuickSearch.class, null, QUICK_SEARCH) ;
	}

	public void setUpdataPath(String path) throws Exception {
		if(path != null && path.trim().length() > 0  && !path.equals(Utils.CATEGORY_HOME) ) {
			String temp[] = path.split("/") ;
			pathList_.clear() ;
			breadcumbs_.clear() ;
			String subPath = "" ;
			for (String string : temp) {
				if(subPath.length() > 0) subPath = subPath + "/" + string ;
				else subPath = string ;
				breadcumbs_.add(FAQUtils.getFAQService().getCategoryNameOf(subPath)) ;
				pathList_.add(subPath) ;
			}
		} else {
			pathList_.clear() ;
			breadcumbs_.clear() ;
			pathList_.add(Utils.CATEGORY_HOME) ;
			breadcumbs_.add(Utils.CATEGORY_HOME) ;
		}
		currentPath_ = path ;
	}

	@SuppressWarnings("unused")
	public String getPath(int index) {
		return this.pathList_.get(index) ;
	}

	public String getPaths() {
		return this.currentPath_;
	}
	@SuppressWarnings("unused")
	private int getMaxPath() {
		return breadcumbs_.size() ;
	}

	@SuppressWarnings("unused")
	public List<String> getBreadcumbs() throws Exception {
		return breadcumbs_ ;
	}

	static public class ChangePathActionListener extends EventListener<UIBreadcumbs> {
		@SuppressWarnings("static-access")
    public void execute(Event<UIBreadcumbs> event) throws Exception {
			UIBreadcumbs uiBreadcums = event.getSource() ;			
			String paths = event.getRequestContext().getRequestParameter(OBJECTID) ;
			UIFAQPortlet faqPortlet = uiBreadcums.getAncestorOfType(UIFAQPortlet.class) ;
			UIQuestions uiQuestions = faqPortlet.findFirstComponentOfType(UIQuestions.class) ;
			UICategories categories = faqPortlet.findFirstComponentOfType(UICategories.class);
			String categoryId = null;
			try{
				if(!paths.equals("FAQService")){
					uiQuestions.setPath(paths) ;
					categoryId = paths.substring(paths.lastIndexOf("/")+1, paths.length()) ;
					uiQuestions.backPath_ = "" ;
					uiQuestions.language_ = "";
				}
				uiQuestions.viewAuthorInfor = FAQUtils.getFAQService().isViewAuthorInfo(categoryId);
				uiBreadcums.setUpdataPath(paths);
				categories.setPathCategory(paths);
				uiQuestions.setCategories(categoryId) ;
			} catch(Exception e){
				FAQUtils.findCateExist(FAQUtils.getFAQService(), uiQuestions.getAncestorOfType(UIFAQContainer.class));
				UIApplication uiApplication = uiBreadcums.getAncestorOfType(UIApplication.class) ;
				uiApplication.addMessage(new ApplicationMessage("UIQuestions.msg.category-id-deleted", null, ApplicationMessage.WARNING)) ;
				event.getRequestContext().addUIComponentToUpdateByAjax(uiApplication.getUIPopupMessages()) ;
			}
			event.getRequestContext().addUIComponentToUpdateByAjax(faqPortlet) ;
		}
	}


}