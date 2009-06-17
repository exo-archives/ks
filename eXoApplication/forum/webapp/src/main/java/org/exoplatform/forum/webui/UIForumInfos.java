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

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/UIForumInfos.gtmpl"
)
public class UIForumInfos extends UIContainer	{
	private UserProfile userProfile ;
	public UIForumInfos() throws Exception { 
		addChild(UIPostRules.class, null, null);
		addChild(UIForumModerator.class, null, null);
	}
	
	public void setForum(Forum forum)throws Exception {
		UIForumPortlet forumPortlet = this.getAncestorOfType(UIForumPortlet.class); 
		this.userProfile = forumPortlet.getUserProfile() ;
		List<String> moderators = ForumServiceUtils.getUserPermission(forum.getModerators()) ;
		UIPostRules postRules = getChild(UIPostRules.class); 
		boolean isShowRule = forumPortlet.isShowRules();
		postRules.setRendered(isShowRule);
		if(isShowRule) {
			boolean isLock = forum.getIsClosed() ;
			if(!isLock) isLock = forum.getIsLock() ;
			if(!isLock && userProfile.getUserRole()!=0) {
				if(!moderators.contains(userProfile.getUserId())) {
					String []listUser = forum.getCreateTopicRole() ;
					if(listUser != null && listUser.length > 0)
						isLock = !ForumServiceUtils.hasPermission(listUser, userProfile.getUserId()) ;
					if(isLock || listUser == null || listUser.length == 0 || listUser[0].equals(" ")){
						ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
						listUser = forumService.getPermissionTopicByCategory(forum.getCategoryId(), "createTopic");
						if(listUser != null && listUser.length > 0 && !listUser[0].equals(" ")){
							isLock = !ForumServiceUtils.hasPermission(listUser, userProfile.getUserId()) ;
						}
					}
				}
			}
			postRules.setLock(isLock) ;
			postRules.setUserProfile(this.userProfile) ;
		}
		UIForumModerator forumModerator = getChild(UIForumModerator.class);
		forumModerator.setModeratorsForum(moderators);
		forumModerator.setRendered(forumPortlet.isShowModerators());
	}
}
