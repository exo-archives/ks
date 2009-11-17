/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.forum.rendering;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.ks.common.bbcode.BBCodeRenderer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ExtendedBBCodeRenderer extends BBCodeRenderer {

  //private List<BBCode> listBBCode = new ArrayList<BBCode>();
  private static Log log = ExoLogger.getLogger(ExtendedBBCodeRenderer.class);
  protected ForumService forumService ;
  
  /**
   * Get a reference to the forum service
   * @return
   */
  protected ForumService getForumService() {
    if (forumService == null) {
      forumService = (ForumService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class) ;
    }
    return forumService;
  }
  
  
  /**
   * Set forum service (used by unit tests)
   * @param forumService
   */
  protected void setForumService(ForumService forumService) {
    this.forumService = forumService;
  }

  public void syncBBCodeCache() {
      List<BBCode> bbcs = new ArrayList<BBCode>();
      try {
        List<String> activeOnserver = getForumService().getActiveBBCode();
        for (String srvBBCode : activeOnserver) {          
          BBCode code = getBBCode(srvBBCode);
          bbcs.add(code);
        }
        bbcodes.clear();
        bbcodes.addAll(bbcs);
      } catch (Exception e) {
        log.warn("Failed to sync BBCodes cache: "+ e.getMessage());
      }
    
  }


  private BBCode getBBCode(String srvBBCode) throws Exception {
    BBCode bbCode = getForumService().getBBcode(srvBBCode);
    if (bbCode == null) {
      log.warn("BBCode " + srvBBCode + " does not exist.");
    }
    
    if(srvBBCode.indexOf("=") >= 0){
      bbCode.setOption(true);
      String tagName = srvBBCode.replaceFirst("=", "");
      bbCode.setTagName(tagName);
      bbCode.setId(tagName+"_option");
    }
    return bbCode;
  }
  
}
