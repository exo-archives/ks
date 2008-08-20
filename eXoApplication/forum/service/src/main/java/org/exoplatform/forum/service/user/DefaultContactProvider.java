package org.exoplatform.forum.service.user;

import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;

public class DefaultContactProvider implements ContactProvider {
  
  private OrganizationService orgService;
  
  public DefaultContactProvider(OrganizationService orgService) {
    this.orgService = orgService;
  }
  
  public ForumContact getForumContact(String userId) {
    ForumContact contact = new ForumContact();
      try {
        User user = orgService.getUserHandler().findUserByName(userId);
        UserProfile profile = orgService.getUserProfileHandler().findUserProfileByName(userId);

        
        contact.setAvatarUrl(profile.getAttribute("user.other-info.avatar.url"));
        contact.setBirthday(profile.getAttribute("user.bdate"));
        contact.setCity(profile.getAttribute("user.home-info.postal.city"));
        contact.setCountry(profile.getAttribute("user.home-info.postal.country"));
        contact.setEmailAddress(user.getEmail());
        contact.setGender(profile.getAttribute("user.gender"));
        contact.setJob(profile.getAttribute("user.jobtitle"));
        contact.setMobile(profile.getAttribute("user.home-info.telecom.mobile.number"));
        contact.setPhone(profile.getAttribute("user.business-info.telecom.telephone.number"));
        contact.setWebSite(profile.getAttribute("user.home-info.online.uri"));
        
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      return contact;
      
  }

}
