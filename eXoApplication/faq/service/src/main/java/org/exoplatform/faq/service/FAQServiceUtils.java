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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.UserHelper;
import org.exoplatform.ks.common.jcr.JCRSessionManager;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.SessionManager;
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
		if(userGroupMembership == null || userGroupMembership.length <= 0 || 
				(userGroupMembership.length == 1 && userGroupMembership[0].equals(" "))) return users ; 
		OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
		for(String str : userGroupMembership) {
			str = str.trim();
			if(str.indexOf("/") >= 0) {
				if(str.indexOf(":") >= 0) { //membership
					String[] array = str.split(":") ;
					PageList userPageList = organizationService.getUserHandler().findUsersByGroup(array[1]) ;					
					//List<User> userList = organizationService.getUserHandler().findUsersByGroup(array[1]).getAll() ;
					if(array[0].length() > 1){
					  List<User> userList = new ArrayList<User>() ;
					  for(int i = 1; i < userPageList.getAvailablePage(); i++) {
					    userList.clear() ;
					    userList.addAll(userPageList.getPage(i)) ;
				      for (User user : userList) {
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
					  }
						/*for(User user: userList) {
							if(!users.contains(user.getUserName())){
								Collection<Membership> memberships = organizationService.getMembershipHandler().findMembershipsByUser(user.getUserName()) ;
								for(Membership member : memberships){
									if(member.getMembershipType().equals(array[0])) {
										users.add(user.getUserName()) ;
										break ;
									}
								}						
							}
						}*/
					}else {
						if(array[0].charAt(0)== 42) {
						  List<User> userList = new ArrayList<User>() ;
						  for(int i = 1; i < userPageList.getAvailablePage(); i++) {
						    userList.clear() ;
	              userList.addAll(userPageList.getPage(i)) ;
	              for (User user : userList) {
	                if(!users.contains(user.getUserName())){
	                  users.add(user.getUserName()) ;
	                }
	              }
	            }
							/*for(User user: userList) {
								if(!users.contains(user.getUserName())){
									users.add(user.getUserName()) ;
								}
							}*/
						}
					}
				}else { //group
					//List<User> userList = organizationService.getUserHandler().findUsersByGroup(str).getAll() ;
				  PageList userPageList = organizationService.getUserHandler().findUsersByGroup(str) ;
				  List<User> userList = new ArrayList<User>() ;
          for(int i = 1; i < userPageList.getAvailablePage(); i++) {
            userList.clear() ;
            userList.addAll(userPageList.getPage(i)) ;
            for (User user : userList) {
              if(!users.contains(user.getUserName())){
                users.add(user.getUserName()) ;
              }
            }
          }
					/*for(User user: userList) {
						if(!users.contains(user.getUserName())){
							users.add(user.getUserName()) ;
						}
					}*/
				}
			}else {//user
				if(!users.contains(str)){
					users.add(str) ;
				}
			}
		}
		return users ;
  }
  
  /**
   * @deprecated use {@link UserHelper#getAllGroupAndMembershipOfUser(String)}
   */
  @Deprecated
  public static List<String> getAllGroupAndMembershipOfUser(String userId) throws Exception{
    return UserHelper.getAllGroupAndMembershipOfUser(userId);
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

  public static SessionManager getSessionManager() {
    KSDataLocation location =  (KSDataLocation) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(KSDataLocation.class);
    return location.getSessionManager();
  }
}

