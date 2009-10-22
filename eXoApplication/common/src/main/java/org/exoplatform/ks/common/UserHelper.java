/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 */
package org.exoplatform.ks.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.GroupHandler;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;
import org.exoplatform.services.organization.impl.GroupImpl;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class UserHelper {
  
  private static OrganizationService getOrganizationService() {
    OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
    return organizationService;
  }
  
  private static UserHandler getUserHandler() {
    return getOrganizationService().getUserHandler();
  }
  
  private static GroupHandler getGroupHandler() {
    return getOrganizationService().getGroupHandler();
  }

  public static List<Group> getAllGroup() throws Exception {
    PageList pageList = (PageList) getGroupHandler().getAllGroups() ;
    List<Group> list = pageList.getAll() ;
    return list;
  }
  

  public static String checkValueUser(String values) throws Exception {
  	String erroUser = null;
  	if(values != null && values.trim().length() > 0) {
  		String[] userIds = values.split(",");
  		for (String str : userIds) {
  			str = str.trim() ;
  			if(str.indexOf("/") >= 0) {
  				if(!UserHelper.hasGroupIdAndMembershipId(str)){
  					if(erroUser == null) erroUser = str ;
  					else erroUser = erroUser + ", " + str;
  				}
  			}else {//user
  				if((getUserHandler().findUserByName(str) == null)) {
  					if(erroUser == null) erroUser = str ;
  					else erroUser = erroUser + ", " + str;
  				}
  			}
  		}
  	}
  	return erroUser;
  }

  public static boolean hasGroupIdAndMembershipId(String str) throws Exception {
  	if(str.indexOf(":") >= 0) { //membership
  		String[] array = str.split(":") ;
  		try {
  		  getGroupHandler().findGroupById(array[1]).getId() ;
  		}catch (Exception e) {
  			return false ;
  		}
  		if(array[0].length() == 1 && array[0].charAt(0) == '*') {
  			return true ;
  		}else if(array[0].length() > 0){
  			if(getOrganizationService().getMembershipTypeHandler().findMembershipType(array[0])== null) return false ;
  		}else return false ;
  	}else { //group
  		try {
  		  getGroupHandler().findGroupById(str).getId() ;
  		}catch (Exception e) {
  			return false ;
  		}
  	}
  	return true ;
  }



  @SuppressWarnings("unchecked")
  public static boolean hasUserInGroup(String groupId, String userId) throws Exception {
  	List<User> users = getUserHandler().findUsersByGroup(groupId).getAll() ;
  	for (User user : users) {
  		if(user.getUserName().equals(userId)) return true ;
  	}
  	return false ;
  }

  @SuppressWarnings("unchecked")
  public static List<User> getUserByGroupId(String groupId) throws Exception {
  	return getUserHandler().findUsersByGroup(groupId).getAll() ;
  }

  public static User getUserByUserId(String userId) throws Exception {
  	return getUserHandler().findUserByName(userId) ;
  }

  @SuppressWarnings("unchecked")
  /**
   * @deprecated this method is danngerous and may not work with all OrganizationService impl
   */
  public static List<User> getAllUser() throws Exception {
  	PageList pageList = getUserHandler().getUserPageList(0) ;
  	List<User>list = pageList.getAll() ;
  	return list;
  }

  
  public static String[] getUserGroups() throws Exception {
  	Object[] objGroupIds = getGroupHandler().findGroupsOfUser(UserHelper.getCurrentUser()).toArray();
  	String[] groupIds = new String[objGroupIds.length];
  	for (int i = 0; i < groupIds.length; i++) {
  		groupIds[i] = ((GroupImpl) objGroupIds[i]).getId();
  	}
  	return groupIds;
  }

  public static PageList getPageListUser() throws Exception {
  	return getUserHandler().getUserPageList(0);
  }

  public static boolean isAnonim() throws Exception {
  	String userId = UserHelper.getCurrentUser();
  	if (userId == null)
  		return true;
  	return false;
  }

  @SuppressWarnings("unchecked")
  public static List<String> getAllGroupAndMembershipOfUser(String userId) throws Exception{
    List<String> listOfUser = new ArrayList<String>();
    if (userId == null) {
      return listOfUser; // should we throw an IllegalArgumentException instead ?
    }

  	listOfUser.add(userId); //himself
  	String value = "";
  	Collection<Membership> memberships = getOrganizationService().getMembershipHandler().findMembershipsByUser(userId);
  	for (Membership membership : memberships) {
       value = membership.getGroupId();
        listOfUser.add(value); // its groups
        value = membership.getMembershipType() + ":" + value;
        listOfUser.add(value);  // its memberships
    }
  	
  	return listOfUser;
  }

  static public String getEmailUser(String userName) throws Exception {
  	User user = getUserHandler().findUserByName(userName) ;
  	String email = user.getEmail() ;
  	return email;
  }

  static public String getCurrentUser() throws Exception {
  	return Util.getPortalRequestContext().getRemoteUser();
  }

}
