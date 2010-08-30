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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.BreadcumbData;
import org.exoplatform.wiki.service.WikiPageParams;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  lifecycle = UIApplicationLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiBreadCrumb.gtmpl"
)
public class UIWikiBreadCrumb extends UIContainer {

  private List<BreadcumbData> breadCumbs = new ArrayList<BreadcumbData>();

  public List<BreadcumbData> getBreadCumbs() {
    return breadCumbs;
  }

  public void setBreadCumbs(List<BreadcumbData> breadCumbs) {
    this.breadCumbs = breadCumbs;
  }
  
  public String getParentURL() throws Exception {
    if(breadCumbs.size() > 1) {
      return createActionLink(breadCumbs.get(breadCumbs.size() - 2)) ;
    }else {
      return createActionLink(breadCumbs.get(0)) ;
    }     
  }
  
  public String createActionLink(BreadcumbData breadCumbData) throws Exception {
    WikiPageParams currentPageParams = Utils.getCurrentWikiPageParams();
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    StringBuilder sb = new StringBuilder(portalRequestContext.getPortalURI());
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    sb.append(pageNodeSelected);
    sb.append("/");
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(currentPageParams.getType())) {
      sb.append(currentPageParams.getType());
      sb.append("/");
      sb.append(currentPageParams.getOwner());
      sb.append("/");
    }
    sb.append(breadCumbData.getId());
    return sb.toString();
  }
}
