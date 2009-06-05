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

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/UICategoryInfo.gtmpl"
)
public class UICategoryInfo extends UIContainer	{
	private	ForumService forumService ;
	//private long mostUserOnline_ = 0;
	
	public UICategoryInfo() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	} 

	@SuppressWarnings("unused")
	private List<String> getUserOnline() throws Exception {
		List<String> list = this.forumService.getOnlineUsers() ;
		//this.mostUserOnline_ = list.size() ;
		return	list;
	}

	public Calendar getInstanceTempCalendar() { 
		Calendar	calendar = GregorianCalendar.getInstance() ;
		calendar.setLenient(false) ;
		int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
		calendar.setTimeInMillis(System.currentTimeMillis() + gmtoffset) ; 
		return	calendar;
	}
	
	public ForumStatistic getForumStatistic() throws Exception {
		return  forumService.getForumStatistic() ;				
	}
}
