/*
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
 */
package org.exoplatform.faq.webui;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.FAQSetting;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template = "app:/templates/faq/webui/UIFAQContainer.gtmpl"
)
public class UIFAQContainer extends UIContainer  {
	private FAQSetting faqSetting_ = null;
	private String currentUser_;
  public UIFAQContainer() throws Exception {
  	FAQService faqService_ = (FAQService)PortalContainer.getInstance().getComponentInstanceOfType(FAQService.class) ;
  	currentUser_ = FAQUtils.getCurrentUser() ;
  	faqSetting_ = new FAQSetting();
		FAQUtils.getPorletPreference(faqSetting_);
		if(currentUser_ != null && currentUser_.trim().length() > 0){
			if(faqSetting_.getIsAdmin() == null || faqSetting_.getIsAdmin().trim().length() < 1){
				if(faqService_.isAdminRole(currentUser_)) faqSetting_.setIsAdmin("TRUE");
				else faqSetting_.setIsAdmin("FALSE");
			}
			faqService_.getUserSetting(currentUser_, faqSetting_);
		} else {
			faqSetting_.setIsAdmin("FALSE");
		}
  	
		UIBreadcumbs uiBreadcumbs = addChild(UIBreadcumbs.class, null, null).setRendered(true) ;
		uiBreadcumbs.setUpdataPath(Utils.CATEGORY_HOME) ;
    UIQuestions uiQuestions = addChild(UIQuestions.class, null, null).setRendered(true) ;    
    uiQuestions.setFAQService(faqService_);
    uiQuestions.setFAQSetting(faqSetting_);
    uiQuestions.viewAuthorInfor = faqService_.isViewAuthorInfo(null);
    if(uiQuestions.getCategoryId() == null) uiQuestions.setCategories(Utils.CATEGORY_HOME) ;
    
    UICategories uiCategories = addChild(UICategories.class, null, null).setRendered(true);
    uiCategories.setFAQSetting(faqSetting_);
    uiCategories.setFAQService(faqService_);
    if(uiCategories.getCategoryPath() == null) uiCategories.setPathCategory(Utils.CATEGORY_HOME) ;
  } 
  
  public FAQSetting getFAQSetting(){return faqSetting_;}
  
  public void updateIsRender(boolean isRender) throws Exception {
  	getChild(UICategories.class).setRendered(isRender) ;
		getChild(UIBreadcumbs.class).setRendered(isRender) ;
		getChild(UIQuestions.class).setRendered(isRender) ;	
	}
}
