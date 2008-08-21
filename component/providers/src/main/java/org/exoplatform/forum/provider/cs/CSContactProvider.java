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
package org.exoplatform.forum.provider.cs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.forum.service.user.ContactProvider;
import org.exoplatform.forum.service.user.ForumContact;
import org.exoplatform.services.log.ExoLogger;

/**
 * ContactProvider implementation that retrieves information from eXo CS
 * {@link ContactService} contacts.<br>
 * <br>
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com 
 * Aug 21, 2008
 */
public class CSContactProvider implements ContactProvider {

  private ContactService  contactService  = null;

  private DownloadService downloadService = null;

  private static Log      log             = ExoLogger
                                              .getLogger(CSContactProvider.class);

  public CSContactProvider(ContactService contactService,
      DownloadService downloadService) {
    this.contactService = contactService;
    this.downloadService = downloadService;
  }

  /**
   * Use the personal contact information for a contact with id userId.
   * Populates workphone1, homeCountry and homeCity. The avatar url is the
   * contact portrait
   */
  public ForumContact getForumContact(String userId) {
    ForumContact contact = new ForumContact();
    try {
      Contact profile = contactService.getPersonalContact(userId);

      contact.setEmailAddress(profile.getEmailAddress());

      contact.setAvatarUrl(loadAvatarUrl(profile));
      contact.setBirthday((profile.getBirthday() == null) ? null : "");
      contact.setCity(profile.getHomeCity());
      contact.setCountry(profile.getHomeCountry());
      contact.setGender(profile.getGender());
      contact.setJob(profile.getJobTitle());
      contact.setMobile(profile.getMobilePhone());
      contact.setPhone(profile.getWorkPhone1());
      contact.setWebSite(profile.getWebPage());

    } catch (Exception e) {
      log.warn("Could not retrieve forum user profile for " + userId + ": " + e.getMessage());
    }
    return contact;
  }

  private String loadAvatarUrl(Contact contact) {

    try {
      ContactAttachment attachment = contact.getAttachment();
      InputStream input = attachment.getInputStream();
      String fileName = attachment.getFileName();
      return getFileSource(input, fileName, downloadService);
    } catch (Exception e) {
      return "/forum/skin/DefaultSkin/webui/background/Avatar1.gif";
    }
  }

  public String getFileSource(InputStream input, String fileName, DownloadService dservice)
      throws Exception {
    byte[] imageBytes = null;
    if (input != null) {
      imageBytes = new byte[input.available()];
      input.read(imageBytes);
      ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes);
      InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image");
      dresource.setDownloadName(fileName);
      return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
    }
    return null;
  }

}
