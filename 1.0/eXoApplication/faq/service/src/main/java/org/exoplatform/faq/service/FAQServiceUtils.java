/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;

/**
 * Created by The eXo Platform SARL
 * Author : Truong Nguyen
 *					truong.nguyen@exoplatform.com
 * May 10, 2008, 4:26:37 PM
 */
public class FAQServiceUtils {
  private static OrganizationService organizationService_ = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  
  /**
   * Get moderator in user,group,membership become list user
   * 
   * @param userGroupMembership is string user input to interface
   * @return list users
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public static List<String> getUserPermission(String[] userGroupMembership) throws Exception {
  	List<String> users = new ArrayList<String> () ;
  	if(userGroupMembership == null || userGroupMembership.length <= 0) return users ; 
  	for(String str : userGroupMembership) {
			if(str.indexOf("/") >= 0) {
				if(str.indexOf(":") >= 0) { //membership
					String[] array = str.split(":") ;
					List<User> userList = organizationService_.getUserHandler().findUsersByGroup(array[1]).getAll() ;
					if(array[0].equals("*")) {
						for(User user: userList) {
							if(!users.contains(user.getUserName())){
								users.add(user.getUserName()) ;
							}
						}
					} else {
						for(User user: userList) {
							Collection<Membership> memberships = organizationService_.getMembershipHandler().findMembershipsByUser(user.getUserName()) ;
							for(Membership member : memberships){
								if(member.getMembershipType().equals(array[0])) {
									if(!users.contains(user.getUserName())){
										users.add(user.getUserName()) ;
									}
									break ;
								}
							}  					
						}
					}
				} else { //group
					List<User> userList = organizationService_.getUserHandler().findUsersByGroup(str).getAll() ;
					for(User user: userList) {
						if(!users.contains(user.getUserName())){
							users.add(user.getUserName()) ;
						}
					}
				}
			} else {
				if(!users.contains(str)){
					users.add(str) ;
				}
			}
		}
  	return users ;
  }
  
  /**
   * Repare permission of node
   * @param node	Node which is repared permission
   * @param owner	permission will be added for this node
   * @throws Exception
   */
  public static void reparePermissions(Node node, String owner) throws Exception {
		ExtendedNode extNode = (ExtendedNode)node ;
    if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
    extNode.setPermission(owner, arrayPers) ;
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;    
    for(AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
    } 
  }
}

