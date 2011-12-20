/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.webui;

import java.util.Arrays;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.core.UIWikiComponent;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(lifecycle = Lifecycle.class, template = "app:/templates/wiki/webui/UIWikiPageNotFound.gtmpl")
public class UIWikiPageNotFound extends UIWikiComponent {
  private Log         log = ExoLogger.getLogger(this.getClass());

  private WikiService wservice;

  public UIWikiPageNotFound() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.PAGE_NOT_FOUND });
    wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
  }

  protected Page getRelatedPage() {
    try {
      WikiPageParams params = Utils.getCurrentWikiPageParams();
      return wservice.getRelatedPage(params.getType(), params.getOwner(), params.getPageId());
    } catch (Exception e) {
     log.debug("Failed to get related page", e);
    }
    return null;
  }

  protected String getHomeURL(WikiPageParams param) throws Exception {
    param.setPageId(WikiNodeType.Definition.WIKI_HOME_NAME);
    return Utils.getURLFromParams(param);
  }
}
