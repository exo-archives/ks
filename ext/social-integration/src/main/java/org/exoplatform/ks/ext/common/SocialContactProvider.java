/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.ks.ext.common;

import java.util.List;
import java.util.Map;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.user.ContactProvider;
import org.exoplatform.ks.common.user.DefaultContactProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;
import org.exoplatform.social.core.service.LinkProvider;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Sep 28, 2011  
 */
public class SocialContactProvider implements ContactProvider {
  @SuppressWarnings("unchecked")
  @Override
  public CommonContact getCommonContact(String userId) {
    CommonContact contact = new CommonContact();
    try {
      IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
      Identity userIdentity = identityM.getOrCreateIdentity(OrganizationIdentityProvider.NAME, userId, true);
      Profile profile = userIdentity.getProfile();

      contact.setEmailAddress(profile.getEmail());
      contact.setFullName(profile.getFullName());
      contact.setAvatarUrl(profile.getAvatarUrl());
      if (profile.contains(Profile.FIRST_NAME)) {
        contact.setFirstName(profile.getProperty(Profile.FIRST_NAME).toString());
      }
      if (profile.contains(Profile.LAST_NAME)) {
        contact.setLastName(profile.getProperty(Profile.LAST_NAME).toString());
      }
      if (profile.contains(Profile.GENDER)) {
        contact.setGender(profile.getProperty(Profile.GENDER).toString());
      }
      if (profile.contains(Profile.CONTACT_PHONES)) {
        List<Map<String, String>> profiles = (List<Map<String, String>>) profile.getProperty(Profile.CONTACT_PHONES);
        for (Map<String, String> mapInfo : profiles) {
          try {
            String str = mapInfo.get("key");
            if (str != null && str.length() > 0) {
              if (str.equals("Work")) {
                str = mapInfo.get("value");
                if (contact.getWorkPhone().length() > 0) {
                  str = contact.getWorkPhone() + ", " + str;
                }
                contact.setWorkPhone(str);
              } else if (str.equals("Home")) {
                str = mapInfo.get("value");
                if (contact.getHomePhone().length() > 0) {
                  str = contact.getHomePhone() + ", " + str;
                }
                contact.setHomePhone(str);
              }
            }
          } catch (Exception e) {
          }
        }
      }
      if (profile.contains(Profile.CONTACT_URLS)) {
        List<Map<String, String>> profiles = (List<Map<String, String>>) profile.getProperty(Profile.CONTACT_URLS);
        for (Map<String, String> mapInfo : profiles) {
          try {
            String str = mapInfo.get("key");
            if (str != null && str.length() > 0) {
              if (str.equals("url")) {
                str = mapInfo.get("value");
                if (contact.getWebSite().length() > 0) {
                  str = contact.getWebSite() + "," + str;
                }
                contact.setWebSite(str);
              }
            }
          } catch (Exception e) {
          }
        }
      } else {
        contact.setWebSite(LinkProvider.getProfileUri(userId));
      }
    } catch (Exception e) {
      OrganizationService orgService = (OrganizationService) PortalContainer.getInstance().getComponentInstanceOfType(OrganizationService.class);
      DefaultContactProvider provider = new DefaultContactProvider(orgService);
      contact = provider.getCommonContact(userId);
    }
    return contact;
  }
}
