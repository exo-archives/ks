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
package org.exoplatform.faq.service.conf;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Feb 15, 2011  
 */

public class FAQUserListener extends UserEventListener {

  private static Log log = ExoLogger.getLogger(FAQUserListener.class);

  FAQService         faqService;

  public FAQUserListener() throws Exception {
    faqService = (FAQService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FAQService.class);
  }

  @Override
  public void postSave(User user, boolean isNew) throws Exception {
  }

  @Override
  public void postDelete(User user) throws Exception {
    try {
      log.info("\n\n Run listener delete user, user kill: " + user.getUserName());
      faqService.calculateDeletedUser(user.getUserName());
    } catch (Exception e) {
      log.warn("failed to remove member : ", e);
    }
  }
}
