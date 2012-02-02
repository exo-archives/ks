/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.common.user;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;

/**
 * Default implementation for {@link ContactProvider}. 
 * Uses the {@link OrganizationService} and get information from {@link UserProfile}.<br><br>
 * 
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 21, 2008  
 *
 */
public class DefaultContactProvider implements ContactProvider {
  
  private static Log log = ExoLogger.getLogger(DefaultContactProvider.class);
  
  private OrganizationService orgService;
  
  
  public DefaultContactProvider(OrganizationService orgService) {
    this.orgService = orgService;
  }
  
  /**
   * Retrieve the forum contact information from user and user profile.<br>
   * 
   * <ul>
   * <li>email address is taken from the user {@link User#getEmail()} </li>
   * <li>mobile, city and country from the {@link UserProfile#HOME_INFO_KEYS} </li>
   * <li>phone is taken from the {@link UserProfile#BUSINESE_INFO_KEYS} </li>
   * </ul>
   */
  public CommonContact getCommonContact(String userId) {
    CommonContact contact = new CommonContact();
    contact.setFullName(userId);
    try {
      User user = orgService.getUserHandler().findUserByName(userId);
      UserProfile profile = orgService.getUserProfileHandler().findUserProfileByName(userId);
      contact.setEmailAddress(user.getEmail());
      contact.setFirstName(user.getFirstName());
      contact.setLastName(user.getLastName());
      contact.setFullName(user.getFullName());
      if (profile.getUserInfoMap() != null) {
        contact.setAvatarUrl(profile.getAttribute("user.other-info.avatar.url"));
        contact.setBirthday(profile.getAttribute("user.bdate"));
        contact.setCity(profile.getAttribute("user.home-info.postal.city"));
        contact.setCountry(profile.getAttribute("user.home-info.postal.country"));
        contact.setGender(profile.getAttribute("user.gender"));
        contact.setJob(profile.getAttribute("user.jobtitle"));
        contact.setHomePhone(profile.getAttribute("user.home-info.telecom.telephone.number"));
        contact.setWorkPhone(profile.getAttribute("user.business-info.telecom.telephone.number"));
        contact.setWebSite(profile.getAttribute("user.home-info.online.uri"));
      }
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Could not retrieve forum user profile for " + userId + " by DefaultContactProvider.\nCaused by:", e);
      }
    }
    return contact;
  }

}
