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
package org.exoplatform.wiki.webui;


import javax.portlet.PortletMode;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008
 */

@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/wiki/webui/UIWikiPortlet.gtmpl"
)
public class UIWikiPortlet extends UIPortletApplication {
  public UIWikiPortlet() throws Exception {
  	
  }
  
  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {    
  	PortletRequestContext portletReqContext = (PortletRequestContext)  context ;
    if(portletReqContext.getApplicationMode() == PortletMode.VIEW) {
    	// Check and remove edit component
    	// add a component that has template is View mode HTML
	    // addChild(UIFAQContainer.class, null, null) ;
	    	
    	
    }else if(portletReqContext.getApplicationMode() == PortletMode.EDIT) {
    	
    	// remove view component
    	// Add edit component
    	// UISettingForm settingForm = addChild(UISettingForm.class, null, "FAQPortletSetting");
		  // settingForm.setRendered(true);
    	
    	//NOTE: add this property to portlet.xml file under <supports> tag 
    	// <portlet-mode>edit</portlet-mode> 
		  
    }
    super.processRender(app, context) ;
  }

} 
