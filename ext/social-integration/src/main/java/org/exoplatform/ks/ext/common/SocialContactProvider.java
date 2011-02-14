package org.exoplatform.ks.ext.common;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.ks.common.user.CommonContact;
import org.exoplatform.ks.common.user.ContactProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.manager.IdentityManager;

/**
 * ContactProvider based on eXo Social profile.
 * @author exo
 */
public class SocialContactProvider implements ContactProvider {

  static private final Log LOG = ExoLogger.getLogger(SocialContactProvider.class);
  
  @Override
  public CommonContact getCommonContact(String userId) {
    CommonContact contact = new CommonContact();
    try {
      IdentityManager identityM = (IdentityManager) PortalContainer.getInstance().getComponentInstanceOfType(IdentityManager.class);
      Identity userIdentity = identityM.getIdentity(OrganizationIdentityProvider.NAME, userId, true);
      Profile profile = userIdentity.getProfile();
      if (profile.contains(Profile.EMAIL)) {
        contact.setEmailAddress(profile.getProperty(Profile.EMAIL).toString());
      }
      if (profile.contains(Profile.FIRST_NAME)) {
        contact.setFirstName(profile.getProperty(Profile.FIRST_NAME).toString());
      }
      if (profile.contains(Profile.LAST_NAME)) {
        contact.setLastName(profile.getProperty(Profile.LAST_NAME).toString());
      }
      contact.setAvatarUrl(profile.getAvatarUrl());
      if (profile.contains(Profile.GENDER)) {
        contact.setGender(profile.getProperty(Profile.GENDER).toString());
      }

      if (profile.contains(Profile.CONTACT_PHONES)) {
        contact.setPhone(profile.getProperty(Profile.CONTACT_PHONES).toString());
      }
      if (profile.contains(Profile.URL)) {
        contact.setWebSite(profile.getProperty(Profile.URL).toString());
      }
    } catch (Exception e) {
      if (LOG.isErrorEnabled()) LOG.error(String.format("can not load contact from eXo Social Profile with user [%s]", userId), e);
      
    }
    return contact;
  }

}
