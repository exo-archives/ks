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
package org.exoplatform.forum.webui;

import java.util.Date;

import javax.portlet.PortletPreferences;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.forum.webui.popup.UIPopupAction;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.RequestContext;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupMessages;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
/**
 * Author : Nguyen Quang Hung
 *					hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
	 lifecycle = UIApplicationLifecycle.class, 
	 template = "app:/templates/forum/webui/UIForumPortlet.gtmpl"
)
public class UIForumPortlet extends UIPortletApplication {
	private boolean isCategoryRendered = true;
	private boolean isForumRendered = false;
	private boolean isTagRendered = false;
	private boolean isSearchRendered = false;
	private boolean isJumpRendered = false;
	private UserProfile userProfile = null;
	private boolean enableIPLogging = false;
	public UIForumPortlet() throws Exception {
		addChild(UIBreadcumbs.class, null, null) ;
		addChild(UICategoryContainer.class, null, null).setRendered(isCategoryRendered) ;
		addChild(UIForumContainer.class, null, null).setRendered(isForumRendered) ;
		addChild(UITopicsTag.class, null, null).setRendered(isTagRendered) ;
		addChild(UISearchForm.class, null, null).setRendered(isSearchRendered) ;
		addChild(UIForumLinks.class, null, null).setRendered(isJumpRendered) ;
		addChild(UIPopupAction.class, null, "UIForumPopupAction") ;
		loadPreferences();
	}
//
//	@Override
//	public void processDecode(WebuiRequestContext context) throws Exception {
//		String objId = context.getRequestParameter(OBJECTID) ; 
////		System.out.println("\n\n"+objId);
//		super.processDecode(context);
//	}
	
	public void updateIsRendered(String selected) throws Exception {
		if(selected == ForumUtils.CATEGORIES) {
			isCategoryRendered = true ;
			isForumRendered = false ;
			isTagRendered = false ;
			isSearchRendered = false ;
		} else if(selected == ForumUtils.FORUM) {
			isForumRendered = true ;
			isCategoryRendered = false ;
			isTagRendered = false ;
			isSearchRendered = false ;
		} else if(selected == ForumUtils.TAG) {
			isTagRendered = true ;
			isForumRendered = false ;
			isCategoryRendered = false ;
			isSearchRendered = false ;
		} else {
			isTagRendered = false ;
			isForumRendered = false ;
			isCategoryRendered = false ;
			isSearchRendered = true ;
		}
		if(userProfile == null) updateUserProfileInfo();
		isJumpRendered = this.userProfile.getIsShowForumJump() ;
		UICategoryContainer categoryContainer = getChild(UICategoryContainer.class).setRendered(isCategoryRendered) ;
		if(isCategoryRendered) {
			categoryContainer.setIsRenderJump(isJumpRendered);
		}else {
			getChild(UIForumLinks.class).setRendered(isJumpRendered) ;
		}
		getChild(UIForumContainer.class).setRendered(isForumRendered) ;
		getChild(UITopicsTag.class).setRendered(isTagRendered) ;
		getChild(UISearchForm.class).setRendered(isSearchRendered) ;
	}
	
	private void loadPreferences() throws Exception {
		PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
		PortletPreferences portletPref = pcontext.getRequest().getPreferences() ;
		try {
			enableIPLogging = Boolean.parseBoolean(portletPref.getValue("enableIPLogging", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public boolean isEnableIPLogging() {
  	return enableIPLogging;
  }

	public void renderPopupMessages() throws Exception {
		UIPopupMessages popupMess = getUIPopupMessages();
		if(popupMess == null)	return ;
		WebuiRequestContext	context =	RequestContext.getCurrentInstance() ;
		popupMess.processRender(context);
	}

	public void cancelAction() throws Exception {
		WebuiRequestContext context = RequestContext.getCurrentInstance() ;
		UIPopupAction popupAction = getChild(UIPopupAction.class) ;
		popupAction.deActivate() ;
		context.addUIComponentToUpdateByAjax(popupAction) ;
	}
	
	public UserProfile getUserProfile() throws Exception {
		if(this.userProfile == null) updateUserProfileInfo() ;
		return this.userProfile ;
	}
	
	@SuppressWarnings("deprecation")
	public void updateUserProfileInfo() throws Exception {
		String userId = "" ;
		try {
			userId = ForumSessionUtils.getCurrentUser() ;
		} catch (Exception e) {
			e.printStackTrace() ;
		}
		Date date = null ;
		if(this.userProfile != null) {
			date = this.userProfile.getLastLoginDate() ;
		}
		SessionProvider sProvider = ForumSessionUtils.getSystemProvider() ;
		try{
			ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			this.userProfile = forumService.getDefaultUserProfile(sProvider, userId) ;
		}finally {
			sProvider.close();
		}
		this.userProfile.setIsOnline(true) ;
		if(date != null)
			this.userProfile.setLastLoginDate(date) ;
	}
}