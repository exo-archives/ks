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
package org.exoplatform.forum.service.user;

import org.apache.commons.logging.Log;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.UserProfile;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 21, 2008  
 */
public class PersonalProfileContactProvider implements ContactProvider {

  private static Log log = ExoLogger.getLogger(PersonalProfileContactProvider.class);
  
  private OrganizationService orgService;
  
  
  public PersonalProfileContactProvider(OrganizationService orgService) {
    this.orgService = orgService;
  }
  
  /**
   * Retrieve the forum contact information from and user personal profile.<br>
   * Email, city, country, mobile, phone and website are taken from {@link UserProfile#HOME_INFO_KEYS}.
   */
  public ForumContact getForumContact(String userId) {
    ForumContact contact = new ForumContact();
      try {
        UserProfile profile = orgService.getUserProfileHandler().findUserProfileByName(userId);
       
        contact.setAvatarUrl(profile.getAttribute("user.other-info.avatar.url"));
        contact.setBirthday(profile.getAttribute("user.bdate"));
        contact.setGender(profile.getAttribute("user.gender"));
        contact.setJob(profile.getAttribute("user.jobtitle"));
        
        contact.setEmailAddress(profile.getAttribute("user.home-info.online.email"));
        contact.setCity(profile.getAttribute("user.home-info.postal.city"));
        contact.setCountry(profile.getAttribute("user.home-info.postal.country"));        
        contact.setMobile(profile.getAttribute("user.home-info.telecom.mobile.number"));
        contact.setPhone(profile.getAttribute("user.home-info.telecom.telephone.number"));
        contact.setWebSite(profile.getAttribute("user.home-info.online.uri"));
        
      } catch (Exception e) {
        log.warn("Could not retrieve forum user profile for " + userId + ": " + e.getMessage());
      }
      return contact;
  }
}
