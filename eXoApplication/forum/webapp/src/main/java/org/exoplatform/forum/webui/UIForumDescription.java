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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/UIForumDescription.gtmpl"
)
public class UIForumDescription extends UIContainer	{
	private String forumId ;
	private String categoryId ;
  private Forum forum = null ;
  private boolean isForum = false;
	public UIForumDescription() throws Exception {		
	}
	
  public void setForum(Forum forum) {
    this.forum = forum ;
    this.isForum = false ;
  }
	public void setForumIds(String categoryId, String forumId) {
    this.isForum = true;
		this.forumId = forumId ;
		this.categoryId = categoryId ;
	}
	
	@SuppressWarnings("unused")
	private Forum getForum() throws Exception {
    if(forum == null || isForum) {
  		ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
			try {
				return forumService.getForum(categoryId, forumId);
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
    } else {
      return this.forum ;
    }
	}
}
