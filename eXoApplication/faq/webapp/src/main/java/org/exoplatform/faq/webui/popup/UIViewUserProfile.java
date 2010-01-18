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

import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.faq.webui.FAQUtils;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.user.ContactProvider;
import org.exoplatform.services.organization.User;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
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
	private FAQService faqService_ = null;
	String[] lableProfile = null;
	public User user_  ;

	public UIViewUserProfile() throws Exception {
		WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;
		lableProfile = new String[]{res.getString("UIViewUserProfile.label.userName"), res.getString("UIViewUserProfile.label.firstName"),
																res.getString("UIViewUserProfile.label.lastName"), res.getString("UIViewUserProfile.label.birthDay"),
																res.getString("UIViewUserProfile.label.gender"), res.getString("UIViewUserProfile.label.email"),
																res.getString("UIViewUserProfile.label.jobTitle"), res.getString("UIViewUserProfile.label.location"),
																res.getString("UIViewUserProfile.label.workPhone"),res.getString("UIViewUserProfile.label.mobilePhone"),
																res.getString("UIViewUserProfile.label.website")
																};
		this.setActions(new String[]{"Close"}) ; 
	}
	
	public CommonContact getContact(String userId) {
		if(contact == null) {
			contact = new CommonContact() ;
			try {
				FAQUtils.setCommonContactInfor(userId, contact, faqService_, getApplicationComponent(DownloadService.class));
			} catch (Exception e) {}
		}
		return contact;
	}

	@SuppressWarnings("unused")
	private String getAvatarUrl(String userId) throws Exception {
		try{
			String url = FAQUtils.getFileSource(faqService_.getUserAvatar(userId), null);
			if(FAQUtils.isFieldEmpty(url)) url = Utils.DEFAULT_AVATAR_URL;
			return url;
		} catch (Exception e){
		}
		return Utils.DEFAULT_AVATAR_URL;
	}
	
	public void setUser(User userName, FAQService faqService) {
		this.user_ = userName ;
		this.faqService_ = faqService;
	}
	public User getUser() throws Exception {
		return user_;
	}
	
	@SuppressWarnings("unused")
	private String[] getLabelProfile() {
		return this.lableProfile;
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
