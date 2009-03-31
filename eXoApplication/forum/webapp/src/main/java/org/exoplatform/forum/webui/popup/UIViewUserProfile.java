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
import org.exoplatform.forum.service.user.ForumContact;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;

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
public class UIViewUserProfile extends UIForm implements UIPopupComponent {
	
	private UserProfile userProfile ;
	private UserProfile userProfileLogin ;
	private ForumContact contact = null;
	private ForumService forumService ;
	
	public ForumContact getContact(String userId) throws Exception {
		if(contact == null) {
			contact = getPersonalContact(userId) ;
		}
		return contact;
	}

	public void setContact(ForumContact contact) {
		this.contact = contact;
	}

	public UIViewUserProfile() {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	}

	@SuppressWarnings("unused")
  private boolean isAdmin(String userId) throws Exception {
  	return forumService.isAdminRole(userId);
  }
	
	@SuppressWarnings("unused")
	private boolean isOnline(String userId) throws Exception {
		return forumService.isOnline(userId) ;
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
	
	private ForumContact getPersonalContact(String userId) throws Exception {
		ForumContact contact = ForumSessionUtils.getPersonalContact(userId) ;
		if(contact == null) {
			contact = new ForumContact() ;
		}
		return contact ;
	}
	
	@SuppressWarnings("unused")
	private String getAvatarUrl(ForumContact contact) throws Exception {
//	DownloadService dservice = getApplicationComponent(DownloadService.class) ;
//	try {
//		ContactAttachment attachment = contact.getAttachment() ; 
//		InputStream input = attachment.getInputStream() ;
//		String fileName = attachment.getFileName() ;
//		return ForumSessionUtils.getFileSource(input, fileName, dservice);
//	} catch (NullPointerException e) {
//		return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
//	}
		DownloadService dservice = getApplicationComponent(DownloadService.class) ;
		String url = ForumSessionUtils.getUserAvatarURL(getUserProfile().getUserId(), this.forumService, dservice);
		return url;
	}
	
	@SuppressWarnings("unused")
	private String[] getLabelProfile() {
		return new String[]{"userName", "firstName", "lastName", "birthDay", "gender", 
				"email", "jobTitle", "location", "workPhone", "mobilePhone" , "website"};
	}
	/*@SuppressWarnings("unused")
	private User getUser() {
		User user = this.userProfile.getUser() ;
		return user;
	}*/
	
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
				popupContainer.getChild(UIPopupAction.class).deActivate() ;
				event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer) ;
			}
		}
	}
}
