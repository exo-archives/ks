/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.forum.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.common.jcr.JCRSessionManager;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.SessionManager;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.MembershipEntry;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 */

public class ForumServiceUtils {
	
	private static final Log                          log                          = ExoLogger.getLogger(ForumServiceUtils.class);
	
	/**
	 * 
	 * Verify if a user match user, group, membership expressions
	 * @param userGroupMembership ist that may contain usernames or group names or membership expressions in the form MEMBERSHIPTYPE:GROUP
	 * @param userId username to match against the expressions
	 * @return true if the user match at least one of the expressions
	 * @throws Exception
	 */
	public static boolean hasPermission(String[] userGroupMembership, String userId) throws Exception {
		IdentityRegistry identityRegistry = (IdentityRegistry) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(IdentityRegistry.class);
		Identity identity = identityRegistry.getIdentity(userId);
		if (identity == null) {
		  if (log.isDebugEnabled()) log.debug("Could not retrieve identity for " + userId + ". Permissions not granted.");
      return false;		  
		}
		
		if(userGroupMembership == null || userGroupMembership.length <= 0 || userGroupMembership[0].equals(" ")) return false;
		
		for (String item : userGroupMembership) {
			String expr = item.trim();
			
			if (isMembershipExpression(expr)) {
				String[] array = expr.split(":") ;
				String membershipType = array[0];
				String group = array[1];
				if (identity.isMemberOf(group, membershipType)) {
					return true;
				}
			} else if (isGroupExpression(expr)) {
				String group = expr;
				if (identity.isMemberOf(group)) {
					return true;
				}
			} else {
				String username = expr;
				if (username.equals(userId)) {
					return true;
				}
			}
			
		}
		return false; // no match found
	}
	
	/**
	 * Is the expression a group expression
	 * @param expr
	 * @return
	 */
	private static boolean isGroupExpression(String expr) {
		return ((expr.indexOf("/") >= 0)  && !(expr.indexOf(":") >= 0));
	}

	/**
	 * Is the expression a membership expression (MEMBERSHIPTYPE:GROUP)
	 * @param expr
	 * @return
	 */
	private static boolean isMembershipExpression(String expr) {
		return ((expr.indexOf("/") >= 0)  && (expr.indexOf(":") >= 0));
	}

	/**
	 * Find usernames matching user, group or membership expressions
	 * @param userGroupMembership list that may contain usernames or group names or membership expressions in the form MEMBERSHIPTYPE:GROUP
	 * @return list of users that mach at least one of the userGroupMembership
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getUserPermission(String[] userGroupMembership) throws Exception {
		List<String> users = getFromCache(userGroupMembership);
		if (users != null) {
			return users;
		}
		users = new ArrayList<String> () ;
		
		if(userGroupMembership == null || userGroupMembership.length <= 0 || 
				(userGroupMembership.length == 1 && userGroupMembership[0].equals(" "))) return users ; 
		OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
		for(String str : userGroupMembership) {
		  
			str = str.trim();
			if (isMembershipExpression(str)) {
				String[] array = str.split(":") ;
				//List<User> userList = organizationService.getUserHandler().findUsersByGroup(array[1]).getAll() ;
				PageList pageList = organizationService.getUserHandler().findUsersByGroup(array[1]) ;
				if(array[0].length() > 1){
				  List<User> userList = new ArrayList<User>() ;
			    for(int i = 1; i <= pageList.getAvailablePage(); i++) {
			      userList.clear() ;
			      userList.addAll(pageList.getPage(i)) ;
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
					  pageList = organizationService.getUserHandler().findUsersByGroup(array[1]) ;
					  List<User> userList = new ArrayList<User>() ;					  
	          for(int i = 1; i <= pageList.getAvailablePage(); i++) {
	            userList.clear() ;
	            userList.addAll(pageList.getPage(i)) ;
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

			} else if (isGroupExpression(str)) {
			  PageList pageList = organizationService.getUserHandler().findUsersByGroup(str) ;
			  List<User> userList = new ArrayList<User>() ;
			  for(int i = 1; i <= pageList.getAvailablePage(); i++) {
          userList.clear() ;
          userList.addAll(pageList.getPage(i)) ;
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
			} else {
			  if(!users.contains(str)){
					users.add(str) ;
				}
			}
			
		}
		storeInCache(userGroupMembership, users);
		return users ;
	}
	
	/**
	 * Store the list of user for the permission expressions in cache
	 * @param userGroupMembership
	 * @param users
	 * @throws Exception
	 */
	private static void storeInCache(String[] userGroupMembership,
			List<String> users) throws Exception {
		ExoCache cache = getCache();
		Serializable cacheKey = getCacheKey(userGroupMembership);
		cache.put(cacheKey, users);
	}

	/**
	 * Load a list of user for the permission expressions in cache
	 * @param userGroupMembership
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	private static List<String> getFromCache(String[] userGroupMembership) throws Exception{
		ExoCache cache = getCache();
		if (userGroupMembership == null) return null;
		Serializable cacheKey = getCacheKey(userGroupMembership);
		return  (List<String>) cache.get(cacheKey);
	}

	private static Serializable getCacheKey(String[] userGroupMembership) {
		StringBuilder sb = new StringBuilder();
		for (String item : userGroupMembership) {
			sb.append("#").append(item);
		}
		return sb.toString();
	}

	private static ExoCache getCache() throws Exception {
		CacheService cacheService = (CacheService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CacheService.class);
		return cacheService.getCacheInstance("org.exoplatform.forum.ForumPermissionsUsers");
	}

	public static List<String> getAllGroupAndMembershipOfUser(String userId) throws Exception{
    List<String> listOfUser = new ArrayList<String>();
    listOfUser.add(userId);
    String groupId = "";
    String type = "";
    Membership membership = null;

    OrganizationService organizationService_ = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
    for (Iterator iterator = organizationService_.getMembershipHandler().findMembershipsByUser(userId).iterator(); iterator.hasNext();) {
      membership = (Membership)iterator.next();
      groupId = membership.getGroupId();
      listOfUser.add(groupId);
      type = membership.getMembershipType() + ":" + type;
      listOfUser.add(type);
    }
		return listOfUser;
	}
	
	public static void reparePermissions(Node node, String owner) throws Exception {
		ExtendedNode extNode = (ExtendedNode)node ;
		if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
		String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE};
		extNode.setPermission(owner, arrayPers) ;
		List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;		
		for(AccessControlEntry accessControlEntry : permsList) {
			extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;			
		}
	}

	public static SessionProvider getSessionProvider() {
		return SessionProvider.createSystemProvider();
	}
	
	
	public static SessionManager getSessionManager() {
	  KSDataLocation location =  (KSDataLocation) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(KSDataLocation.class);
	  return location.getSessionManager();
	}
}
