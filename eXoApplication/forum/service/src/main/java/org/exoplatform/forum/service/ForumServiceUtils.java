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
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

public class ForumServiceUtils {
  
  
  public static boolean hasPermission(String[] userGroupMembership, String userId) throws Exception {
  	List<String> users = ForumServiceUtils.getUserPermission(userGroupMembership) ;
  	if(users.contains(userId)) return true ;
  	return false ;
  }

  @SuppressWarnings("unchecked")
  public static List<String> getUserPermission(String[] userGroupMembership) throws Exception {
  	List<String> users = new ArrayList<String> () ;
  	if(userGroupMembership == null || userGroupMembership.length <= 0) return users ; 
  	OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  	for(String str : userGroupMembership) {
			if(str.indexOf("/") >= 0) {
				if(str.indexOf(":") >= 0) { //membership
					String[] array = str.split(":") ;
					List<User> userList = organizationService.getUserHandler().findUsersByGroup(array[1]).getAll() ;
					if(array[0].length() > 1){
						for(User user: userList) {
							if(!users.contains(user.getUserName())){
								Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(user.getUserName()) ;
								for(Membership member : memberships){
									if(member.getMembershipType().equals(array[0])) {
											users.add(user.getUserName()) ;
										break ;
									}
								}  					
							}
						}
					} else {
						if(array[0].charAt(0)== 42) {
							for(User user: userList) {
								if(!users.contains(user.getUserName())){
									users.add(user.getUserName()) ;
								}
							}
						}
					}
				}else { //group
					List<User> userList = organizationService.getUserHandler().findUsersByGroup(str).getAll() ;
					for(User user: userList) {
						if(!users.contains(user.getUserName())){
							users.add(user.getUserName()) ;
						}
					}
				}
			}else {//user
				if(!users.contains(str)){
					users.add(str) ;
				}
			}
		}
  	return users ;
  }

}
