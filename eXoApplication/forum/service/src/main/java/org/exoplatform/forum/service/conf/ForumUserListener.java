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
package org.exoplatform.forum.service.conf;

import java.util.TimeZone;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.UserProfile;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SARL Author : Hung Nguyen Quang
 * hung.nguyen@exoplatform.com Nov 23, 2007 3:09:21 PM
 */
public class ForumUserListener extends UserEventListener {

  private static Log  log = ExoLogger.getLogger(ForumUserListener.class);

  private UserProfile profileTemplate;

  public ForumUserListener(InitParams params) throws Exception {
    if (params == null)
      return;
    PropertiesParam propsParams = params.getPropertiesParam("user.profile.setting");
    ExoProperties props = propsParams.getProperties();
    profileTemplate = new UserProfile();

    String timeZoneNumber = props.getProperty("timeZone") != null ? props.getProperty("timeZone") : "GMT";
    double timeZone = 0.0;
    timeZone = -TimeZone.getTimeZone(timeZoneNumber).getRawOffset() * 1.0 / 3600000;
    profileTemplate.setTimeZone(timeZone);

    String shortDateFormat = props.getProperty("shortDateFormat") != null ? props.getProperty("shortDateFormat") : "MM/dd/yyyy";
    profileTemplate.setShortDateFormat(shortDateFormat);

    String longDateFormat = props.getProperty("longDateFormat") != null ? props.getProperty("longDateFormat") : "DDD,MMM dd,yyyy";

    profileTemplate.setLongDateFormat(longDateFormat);
    String timeFormat = (props.getProperty("timeFormat") != null) ? props.getProperty("timeFormat") : "hh:mm a";

    profileTemplate.setTimeFormat(timeFormat);

    String strMaxTopic = props.getProperty("maxTopic");
    int maxTopic = 10;
    try {
      maxTopic = Integer.parseInt(strMaxTopic);
    } catch (NumberFormatException nfe) {
      log.warn("maxTopic is not in format", nfe);
    }
    profileTemplate.setMaxTopicInPage(maxTopic);

    String strMaxPost = props.getProperty("maxPost");
    int maxPost = 10;
    try {
      maxPost = Integer.parseInt(strMaxPost);
    } catch (NumberFormatException nfe) {
      log.warn("maxPost is not in format", nfe);
    }
    profileTemplate.setMaxPostInPage(maxPost);

    String strShowForumJump = props.getProperty("isShowForumJump");
    boolean isShowForumJump = true;
    isShowForumJump = Boolean.parseBoolean(strShowForumJump);
    profileTemplate.setIsShowForumJump(isShowForumJump);
  }

  public void postSave(User user, boolean isNew) throws Exception {
    if (isNew) {
      try {
        getForumService().addMember(user, profileTemplate);

      } catch (Exception e) {
        log.warn("Error while adding new forum member: ", e);
      }

    } else {

      try {
        getForumService().updateUserProfile(user);
      } catch (Exception e) {
        log.warn("Error while updating forum profile: ", e);
      }
    }
  }

  private ForumService getForumService() {
    return (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
  }

  @Override
  public void postDelete(User user) throws Exception {
    try {
      getForumService().removeMember(user);
    } catch (Exception e) {
      log.warn("failed to remove member : ", e);
    }

  }
}
