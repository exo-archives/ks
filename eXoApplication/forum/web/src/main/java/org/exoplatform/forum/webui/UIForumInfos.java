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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.service.Forum;
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
	private List<String> moderators = new ArrayList<String>();
  private UserProfile userProfile ;
	public UIForumInfos() throws Exception { 
		addChild(UIPostRules.class, null, null);
	}

	@SuppressWarnings("unused")
  private List<String> getModeratorsForum() throws Exception {
		return moderators ;
	}
	
	public void setForum(Forum forum)throws Exception {
		this.userProfile = this.getAncestorOfType(UIForumPortlet.class).getUserProfile() ;
		this.moderators = ForumServiceUtils.getUserPermission(forum.getModerators()) ;
		UIPostRules postRules = getChild(UIPostRules.class); 
		boolean isLock = forum.getIsClosed() ;
		if(!isLock) isLock = forum.getIsLock() ;
    if(!isLock && userProfile.getUserRole()!=0) {
      if(!this.moderators.contains(userProfile.getUserId())) {
        String []listUser = forum.getCreateTopicRole() ;
        if(listUser != null && listUser.length > 0)
          isLock = !ForumServiceUtils.hasPermission(listUser, userProfile.getUserId()) ;
      }
    }
		postRules.setLock(isLock) ;
		postRules.setUserProfile(this.userProfile) ;
	}
  
}
