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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * Apr 14, 2008, 2:56:30 PM
 */
public class FAQUtils {
	
	static public FAQService getFAQService() throws Exception {
    return (FAQService)PortalContainer.getComponent(FAQService.class) ;
  }
	
	public static User getUserByUserId(String userId) throws Exception {
  	OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  	return organizationService.getUserHandler().findUserByName(userId) ;
  }
	
	public static String[] splitForFAQ (String str) throws Exception {
		if(str != null && str.length() > 0) {
			if(str.contains(",")) return str.trim().split(",") ;
			else return str.trim().split(";") ;
		} else return new String[] {} ;
	}
	
	 public static SessionProvider getSystemProvider() {
	    return SessionProvider.createSystemProvider();
	  }
	  
	  public static SessionProvider getSessionProvider() {
	    SessionProviderService service = (SessionProviderService) PortalContainer
	        .getComponent(SessionProviderService.class);
	    return service.getSessionProvider(null);
	  }
	
  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }
  
  
}
