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
package org.exoplatform.forum;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.service.ForumAttachment;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.user.ContactProvider;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 */

public class ForumSessionUtils {
	
	public static String getUserAvatarURL(String userName, ForumService forumService, DownloadService dservice){
		String url = "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
		try{
			ForumAttachment attachment = forumService.getUserAvatar(userName);
			url = ForumSessionUtils.getFileSource(attachment.getInputStream(), attachment.getName(), dservice);
			if(url == null || url.trim().length() < 1){
				CommonContact contact = getPersonalContact(userName) ;
				if(contact.getAvatarUrl() != null && contact.getAvatarUrl().trim().length() > 0){
					url = contact.getAvatarUrl();
				}
			}
		}catch (Exception e){
		}
		return url;
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
	
	public static CommonContact getPersonalContact(String userId) throws Exception {
		try {
			ContactProvider provider = (ContactProvider) PortalContainer.getComponent(ContactProvider.class) ;
			return provider.getCommonContact(userId);
		}catch (Exception e) {
			return new CommonContact();
		}
	}
	
	public static String getBreadcumbUrl(String link, String componentName, String actionName, String objectId) throws Exception {
		PortalRequestContext portalContext = Util.getPortalRequestContext();
		String url = portalContext.getRequest().getRequestURL().toString();
		url = url.substring(0, url.indexOf("/", 8)) ;
		link = link.replaceFirst(componentName,"UIBreadcumbs").replaceFirst(actionName,"ChangePath")
			.replace("pathId", objectId).replaceAll("&amp;", "&");
		return (url+link);
	}
}
