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
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
		template =	"app:/templates/forum/webui/UICategoryInfo.gtmpl",
		events = {
				@EventConfig(listeners = UICategoryInfo.CreatedLinkActionListener.class )
		}
)
@SuppressWarnings("unused")
public class UICategoryInfo extends UIContainer	{
	private	ForumService forumService ;
	private UserProfile userProfile;
	//private long mostUserOnline_ = 0;
	
	public UICategoryInfo() throws Exception {
		forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	} 

	private List<String> getUserOnline() throws Exception {
		return forumService.getOnlineUsers();
	}

  private String getScreenName(String userName) throws Exception {
		return forumService.getScreenName(userName);
	}
  
  private UserProfile getUserProfile() throws Exception {
  	try {
     	UIForumPortlet forumPortlet = getAncestorOfType(UIForumPortlet.class);
     	userProfile = forumPortlet.getUserProfile();
     } catch (Exception e) {
    	 String userId = UserHelper.getCurrentUser();
     	userProfile = forumService.getDefaultUserProfile(userId, "");
     }
    return userProfile;
  }
  
  private String getMostUsersOnline(String s, String at) throws Exception {
		if(ForumUtils.isEmpty(s)) return "";
		try {
	    String []strs = s.split(",");
	    long l = Long.parseLong(strs[1].replace("at", "").trim());
	    Calendar calendar = GregorianCalendar.getInstance();
      double timeZone = userProfile.getTimeZone();
      if(userProfile.getUserId().equals(UserProfile.USER_GUEST))timeZone = 0;
      long zone = (long)(timeZone*3600000) ;
      calendar.setTimeInMillis(l - zone);
      StringBuilder builder = new StringBuilder();
      
      if(ForumUtils.isEmpty(at)) at = "at";
      builder.append(strs[0]).append(", ").append(at).append(" ");
      builder.append(ForumUtils.getFormatDate((userProfile.getLongDateFormat() + ", " + userProfile.getTimeFormat()), calendar.getTime()));
      if(userProfile.getUserId().equals(UserProfile.GUEST)) {
      	if(timeZone >= 0)
      		builder.append(" GMT+").append(String.valueOf(timeZone).replace(".0", ""));
      	else builder.append(" GMT").append(String.valueOf(timeZone).replace(".0", ""));
      }
      s = builder.toString();
    } catch (Exception e) {
    	s = s.replace("at", at);
    }
		return s;
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
	
	static public class CreatedLinkActionListener extends EventListener<UICategoryInfo> {
		public void execute(Event<UICategoryInfo> event) throws Exception {
		}
	}
}
