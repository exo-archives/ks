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
package org.exoplatform.forum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.utils.PageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.user.ContactProvider;
import org.exoplatform.forum.service.user.ForumContact;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.impl.GroupImpl;

public class ForumSessionUtils {
  
  static public String getCurrentUser() throws Exception {
    return Util.getPortalRequestContext().getRemoteUser();
  }
  
  public static List<String> getAllGroupAndMembershipOfUser(String userId) throws Exception{
  	List<String> listOfUser = new ArrayList<String>();
		listOfUser.add(userId);
		String value = "";
		String id = "";
		Membership membership = null;
		OrganizationService organizationService_ = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
		for(Object object : organizationService_.getMembershipHandler().findMembershipsByUser(userId).toArray()){
			id = object.toString();
			id = id.replace("Membership[", "").replace("]", "");
			membership = organizationService_.getMembershipHandler().findMembership(id);
			value = membership.getGroupId();
			listOfUser.add(value);
			value = membership.getMembershipType() + ":" + value;
			listOfUser.add(value);
		}
		return listOfUser;
  }
  
  public static boolean isAnonim() throws Exception {
    String userId = getCurrentUser();
    if (userId == null)
      return true;
    return false;
  }
  
  public static SessionProvider getSystemProvider() {
    return SessionProviderFactory.createSystemProvider();
  }
  
  public static String getUserAvatarURL(String userName, ForumService forumService, SessionProvider sessionProvider, 
  																			DownloadService dservice){
  	try{
	  	ForumAttachment attachment = forumService.getUserAvatar(userName, sessionProvider);
	  	return ForumSessionUtils.getFileSource(attachment.getInputStream(), attachment.getName(), dservice);
  	} catch (Exception e){
  		return null;
  	}
  }
  
  public static String getFileSource(InputStream input, String fileName, DownloadService dservice) throws Exception {
		byte[] imageBytes = null;
		if (input != null) {
			imageBytes = new byte[input.available()];
			input.read(imageBytes);
			ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
			InputStreamDownloadResource dresource = new InputStreamDownloadResource(
					byteImage, "image");
			dresource.setDownloadName(fileName);
			return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
		}
  	return null;
  }
  
  public static String[] getUserGroups() throws Exception {
    OrganizationService organizationService = (OrganizationService) PortalContainer
        .getComponent(OrganizationService.class);
    Object[] objGroupIds = organizationService.getGroupHandler()
        .findGroupsOfUser(getCurrentUser()).toArray();
    String[] groupIds = new String[objGroupIds.length];
    for (int i = 0; i < groupIds.length; i++) {
      groupIds[i] = ((GroupImpl) objGroupIds[i]).getId();
    }
    return groupIds;
  }
  
  @SuppressWarnings("unchecked")
  public static PageList getPageListUser() throws Exception {
    OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
    return organizationService.getUserHandler().getUserPageList(0);
  }

  @SuppressWarnings("unchecked")
  public static List<User> getAllUser() throws Exception {
  	OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  	PageList pageList = organizationService.getUserHandler().getUserPageList(0) ;
  	List<User>list = pageList.getAll() ;
  	return list;
  }
  
  public static User getUserByUserId(String userId) throws Exception {
  	OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  	return organizationService.getUserHandler().findUserByName(userId) ;
  }

  @SuppressWarnings("unchecked")
  public static List<User> getUserByGroupId(String groupId) throws Exception {
  	OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  	return organizationService.getUserHandler().findUsersByGroup(groupId).getAll() ;
  }

  @SuppressWarnings("unchecked")
  public static boolean hasUserInGroup(String groupId, String userId) throws Exception {
  	OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  	List<User> users = organizationService.getUserHandler().findUsersByGroup(groupId).getAll() ;
  	for (User user : users) {
	    if(user.getUserName().equals(userId)) return true ;
    }
  	return false ;
  }

  @SuppressWarnings("unchecked")
  public static boolean hasGroupIdAndMembershipId(String str, OrganizationService organizationService) throws Exception {
	  if(str.indexOf(":") >= 0) { //membership
  		String[] array = str.split(":") ;
  		try {
  			organizationService.getGroupHandler().findGroupById(array[1]).getId() ;
  		} catch (Exception e) {
  			return false ;
  		}
  		if(array[0].length() == 1 && array[0].charAt(0) == '*') {
  			return true ;
  		} else if(array[0].length() > 0){
  			if(organizationService.getMembershipTypeHandler().findMembershipType(array[0])== null) return false ;
  		} else return false ;
		} else { //group
			try {
				organizationService.getGroupHandler().findGroupById(str).getId() ;
			} catch (Exception e) {
				return false ;
			}
		}
    return true ;
  }
  
  public static String checkValueUser(String values) throws Exception {
  	String erroUser = null;
  	if(values != null && values.trim().length() > 0) {
  		OrganizationService organizationService = (OrganizationService) PortalContainer.getComponent(OrganizationService.class);
  		String[] userIds = values.split(",");
  		boolean isUser = false ;
  		List<User> users = ForumSessionUtils.getAllUser() ;
  		for (String str : userIds) {
  			str = str.trim() ;
  			if(str.indexOf("/") >= 0) {
					if(!hasGroupIdAndMembershipId(str, organizationService)){
						if(erroUser == null) erroUser = str ;
						else erroUser = erroUser + ", " + str;
  				}
  			} else {//user
  				isUser = false ;
  				for (User user : users) {
	          if(user.getUserName().equals(str)) {
	          	isUser = true ;
	          	break;
	          }
          }
  				if(!isUser) {
  					if(erroUser == null) erroUser = str ;
  					else erroUser = erroUser + ", " + str;
  				}
  			}
      }
  	}
  	return erroUser;
  }
  
  public static ForumContact getPersonalContact(String userId) throws Exception {
    try {
    	ContactProvider provider = (ContactProvider) PortalContainer.getComponent(ContactProvider.class) ;
    	return provider.getForumContact(userId);
    } catch (Exception e) {
	    return new ForumContact();
    }
  }
  
  public static String getBreadcumbUrl(String link, String componentName, String actionName) throws Exception {
  	String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
		String portalName = "/" + Util.getUIPortal().getName() ;
		link = link.replaceFirst(componentName,"UIBreadcumbs").replaceFirst(actionName,"ChangePath").replaceAll("&amp;", "&");							
		if(link.indexOf(portalName) > 0) {
	    if(link.indexOf(portalName + "/" + selectedNode) < 0){
	      link = link.replaceFirst(portalName, portalName + "/" + selectedNode) ;
	    }									
		}	
  	return link;
  }
  
}
