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
package org.exoplatform.wiki.webui.control.filter;

import java.util.Map;

import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilterType;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * This filter is used to deny component if it is installed on Wiki home page. <br>
 * Author : Le Thanh Quang
 *          quang.le@exoplatform.com
 * Dec 9, 2010  
 */
public class DeniedOnWikiHomePageFilter implements UIExtensionFilter {

  @Override
  public boolean accept(Map<String, Object> context) throws Exception {
    Page page = Utils.getCurrentWikiPage();
    if (page != null && page instanceof PageImpl) {
      PageImpl pageImpl = (PageImpl) page;
      // a page is wiki home page if its parent page is null.
      return pageImpl.getParentPage() != null; 
    }
    return false;
  }

  @Override
  public UIExtensionFilterType getType() {
    return UIExtensionFilterType.MANDATORY;
  }

  @Override
  public void onDeny(Map<String, Object> context) throws Exception {
    
  }

}
