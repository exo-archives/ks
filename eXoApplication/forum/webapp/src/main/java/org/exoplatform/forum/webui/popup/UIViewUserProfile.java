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
package org.exoplatform.forum.webui.popup;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.webui.BaseUIForm;
import org.exoplatform.ks.common.webui.UIPopupContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template = "app:/templates/forum/webui/popup/UIViewMemberProfile.gtmpl",
		events = {
			@EventConfig(listeners = UIViewUserProfile.CloseActionListener.class,phase = Phase.DECODE)
		}
)
@SuppressWarnings("unused")
public class UIViewUserProfile extends BaseUIForm implements UIPopupComponent {
	
	private UserProfile userProfile ;
	private UserProfile userProfileLogin ;
	private CommonContact contact = null;
	private ForumService forumService ;
	private boolean isGetContact = true;
	public CommonContact getContact(String userId) throws Exception {
		if(contact == null || isGetContact) {
			contact = getPersonalContact(userId) ;
		}
		return contact;
	}

	public void setContact(CommonContact contact) {
		isGetContact = false;
		this.contact = contact;
	}

	public UIViewUserProfile() {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	}

  private boolean isAdmin(String userId) throws Exception {
  	return forumService.isAdminRole(userId);
  }
	
	
	private boolean isOnline(String userId) throws Exception {
		return forumService.isOnline(userId) ;
	}
	
  private String getScreenName(String userName) throws Exception {
		return forumService.getScreenName(userName);
	}
	
	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile ;
	}
	
	public UserProfile getUserProfile() {
		return this.userProfile ;
	}

	public void setUserProfileLogin(UserProfile userProfile) {
		this.userProfileLogin = userProfile ;
	}
	
	public UserProfile getUserProfileLogin() {
		return this.userProfileLogin ;
	}
	
	private CommonContact getPersonalContact(String userId) throws Exception {
	  CommonContact contact = ForumSessionUtils.getPersonalContact(userId) ;
		if(contact == null) {
			contact = new CommonContact() ;
		}
		return contact ;
	}
	
	private String getAvatarUrl(CommonContact contact) throws Exception {
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		String url = ForumSessionUtils.getUserAvatarURL(getUserProfile().getUserId(), this.forumService, dservice);
		return url;
	}
	
	private String[] getLabelProfile() {
		return new String[]{"userName", "firstName", "lastName", "birthDay", "gender", 
				"email", "jobTitle", "location", "workPhone", "mobilePhone" , "website"};
	}
	
	public void activate() throws Exception {}
	public void deActivate() throws Exception {}
	
	static	public class CloseActionListener extends EventListener<UIViewUserProfile> {
		public void execute(Event<UIViewUserProfile> event) throws Exception {
			UIViewUserProfile uiForm = event.getSource() ;
			UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
			if(popupContainer == null) {
				UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
				forumPortlet.cancelAction() ;
			} else {
				UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
				if(popupAction.findFirstComponentOfType(UIViewUserProfile.class) != null){
					popupAction.deActivate();
					event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
				} else {
					UIPopupAction popup = popupContainer.getAncestorOfType(UIPopupAction.class);
					popup.deActivate();
					event.getRequestContext().addUIComponentToUpdateByAjax(popup) ;
				}
			}
		}
	}
}
