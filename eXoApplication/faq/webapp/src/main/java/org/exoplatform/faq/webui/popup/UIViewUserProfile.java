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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.webui.popup;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.CommonContact;
import org.exoplatform.ks.common.user.ContactProvider;
import org.exoplatform.services.organization.User;
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
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/faq/webui/popup/UIViewUserProfile.gtmpl",
		events = {
			@EventConfig(listeners = UIViewUserProfile.CloseActionListener.class)
		}
)
public class UIViewUserProfile extends UIForm implements UIPopupComponent {
	private CommonContact contact = null;
	public User user_  ;

	public UIViewUserProfile() throws Exception { this.setActions(new String[]{"Close"}) ; }
	
	public CommonContact getContact(String userId) throws Exception {
		if(contact == null) {
			contact = getPersonalContact(userId) ;
		}
		return contact;
	}

	public void setContact(CommonContact contact) {
		this.contact = contact;
	}
	
	private CommonContact getPersonalContact(String userId) throws Exception {
		CommonContact contact = getPersonalContact1(userId) ;
		if(contact == null) {
			contact = new CommonContact() ;
		}
		return contact ;
	}
	
	@SuppressWarnings("unused")
	private String getAvatarUrl(CommonContact contact) throws Exception {
//	DownloadService dservice = getApplicationComponent(DownloadService.class) ;
//	try {
//		ContactAttachment attachment = contact.getAttachment() ; 
//		InputStream input = attachment.getInputStream() ;
//		String fileName = attachment.getFileName() ;
//		return ForumSessionUtils.getFileSource(input, fileName, dservice);
//	} catch (NullPointerException e) {
//		return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
//	}
	if (contact.getAvatarUrl() == null ) {
		return "/faq/skin/DefaultSkin/webui/background/Avatar1.gif";
	} else {
		return contact.getAvatarUrl();
	}
	}
	
	public void setUser(User userName) {
		this.user_ = userName ;
	}
	public User getUser() throws Exception {
		return user_;
	}
	
	@SuppressWarnings("unused")
	private String[] getLabelProfile() {
		return new String[]{"userName", "firstName", "lastName", "birthDay", "gender", 
				"email", "jobTitle", "location", "workPhone", "mobilePhone" , "website"};
	}
	
	public  CommonContact getPersonalContact1(String userId){
    try {
    	ContactProvider provider = (ContactProvider) PortalContainer.getComponent(ContactProvider.class) ;
    	return provider.getCommonContact(userId);
    } catch (Exception e) {
	    return new CommonContact();
    }
  }
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class CloseActionListener extends EventListener<UIViewUserProfile> {
		public void execute(Event<UIViewUserProfile> event) throws Exception {
			UIViewUserProfile uiViewUserProfile = event.getSource() ;
			UIPopupAction uiPopupAction = uiViewUserProfile.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
		}
	}
	
}
