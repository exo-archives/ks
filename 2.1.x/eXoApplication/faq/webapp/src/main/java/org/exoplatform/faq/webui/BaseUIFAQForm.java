/***************************************************************************
 * Copyright (C) 2003-2010 eXo Platform SAS.
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

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.webui.viewer.UIFAQPortlet;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 18, 2010 - 4:59:43 AM  
 */
public class BaseUIFAQForm extends BaseUIForm{
	 private FAQService faqService ;
	  
	  /**
	   * Get a reference to the faq service
	   * @return
	   */
	  protected FAQService getFAQService() {
	    if (faqService == null) {
	    	faqService = (FAQService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FAQService.class) ;
	    }
	    return faqService;
	  }
	  /**
	   * Set faq service (used by unit tests)
	   * @param faqService
	   */
	  protected void setFAQService(FAQService faqService) {
	    this.faqService = faqService;
	  }
	  
	  protected <T extends UIComponent> T  openPopup(Class<T> componentType,  String popupId, int width, int height) throws Exception {
	    UIFAQPortlet faqPortlet = getAncestorOfType(UIFAQPortlet.class) ;   
	    return openPopup(faqPortlet, componentType, popupId, width, height);
	  }
	  
	  protected <T extends UIComponent> T openPopup(Class<T> componentType, int width, int height) throws Exception {
	  	UIFAQPortlet faqPortlet = getAncestorOfType(UIFAQPortlet.class);
	    return openPopup(faqPortlet, componentType, width, height);
	  }
	  
	  protected <T extends UIComponent> T openPopup(Class<T> componentType, int width) throws Exception {
	    return openPopup(componentType, width, 0);
	  }

	  protected <T extends UIComponent> T openPopup(Class<T> componentType, String popupId, int width) throws Exception {
	    return openPopup(componentType, popupId, width, 0);
	  }

}
