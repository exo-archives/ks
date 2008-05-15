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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumFormatUtils;
import org.exoplatform.forum.ForumSessionUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.ForumStatistic;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.services.organization.User;
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
	private	ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private long mostUserOnline_ ;
	private long numberActive = 0;
	private List<UserProfile> userProfiles = new ArrayList<UserProfile>();
	
	public UICategoryInfo() throws Exception { 
	} 
	
	@SuppressWarnings("unchecked")
  public List<UserProfile> setPageListUserProfile() throws Exception {
		if(userProfiles == null) {
	    List<User> listUser = ForumSessionUtils.getAllUser() ;
	    for (User user : listUser) {
	      this.forumService.getUserProfile(ForumSessionUtils.getSystemProvider(), user.getUserName(), false, false, false) ;
	    }
	    JCRPageList pageList = this.forumService.getPageListUserProfile(ForumSessionUtils.getSystemProvider()) ;
	    userProfiles = pageList.getPage(0) ;
		}
    return userProfiles;
  }
	
	@SuppressWarnings("unused")
  private long getUserActive() throws Exception {
		if(numberActive <= 0) {
			Date date = null;
			long newTime = getInstanceTempCalendar().getTimeInMillis(), oldTime;
			for (UserProfile userProfile : userProfiles) {
				date = userProfile.getLastLoginDate();
				if(date != null){
					oldTime = date.getTime();
					if((newTime - oldTime) < 3*86400000) {// User have login at least 3 day
						date = userProfile.getLastPostDate() ;
						oldTime = date.getTime();
						if((newTime - oldTime) < 5*86400000) {// User have a post at least in five day
							++numberActive ;
						}
					}
				}
	    }
		}
		if(numberActive <= 0) numberActive = 1;
		return numberActive;
	}
	@SuppressWarnings("unused")
  private List<String> getUserOnline() throws Exception {
		List<String> list = this.forumService.getOnlineUsers() ;
		this.mostUserOnline_ = list.size() ;
		return  list;
	}

  public Calendar getInstanceTempCalendar() { 
    Calendar  calendar = GregorianCalendar.getInstance() ;
    calendar.setLenient(false) ;
    int gmtoffset = calendar.get(Calendar.DST_OFFSET) + calendar.get(Calendar.ZONE_OFFSET);
    calendar.setTimeInMillis(System.currentTimeMillis() + gmtoffset) ; 
    return  calendar;
  }
	
	public ForumStatistic getForumStatistic() throws Exception {
		ForumStatistic forumStatistic = forumService.getForumStatistic(ForumSessionUtils.getSystemProvider()) ;
		
		List<User> userList = ForumSessionUtils.getAllUser();
		long size = (long)userList.size() ;
		boolean isSave = false ;
		if(forumStatistic.getMembersCount() < size) {
			long max = userList.get(0).getCreatedDate().getTime(), temp ;
			int i = 0, j = 0;
			for (User user : userList) {
				temp = user.getCreatedDate().getTime() ;
				if(temp > max){
					max = temp; i = j ;
				}
				j++;
	    }
			forumStatistic.setMembersCount(size) ;
			forumStatistic.setNewMembers(userList.get(i).getUserName()) ;
			isSave = true ;
		}
		long mumberUserOnline = 0;
		String mostUserOnlines = forumStatistic.getMostUsersOnline();
		Date date = getInstanceTempCalendar().getTime() ;
		if(mostUserOnlines != null && mostUserOnlines.length() > 0) {
			mumberUserOnline = Long.parseLong(mostUserOnlines.split(",")[0]) ;
			if(this.mostUserOnline_ > mumberUserOnline) {
				mostUserOnlines = this.mostUserOnline_ + ", at " + ForumFormatUtils.getFormatDate("MM-dd-yyyy, hh:mm a", date);
				forumStatistic.setMostUsersOnline(mostUserOnlines) ;
				isSave = true ;
			}
		} else {
			mostUserOnlines = this.mostUserOnline_ + ", at " + ForumFormatUtils.getFormatDate("MM-dd-yyyy, hh:mm a", date);
			forumStatistic.setMostUsersOnline(mostUserOnlines) ;
			isSave = true ;
		} 
		if(isSave) {
			this.forumService.saveForumStatistic(ForumSessionUtils.getSystemProvider(), forumStatistic) ;
		}
	  return forumStatistic ;
  }
}
