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
package org.exoplatform.wiki.rendering.macro.include;

import org.apache.commons.lang.StringUtils;
import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 06, 2011  
 */
public class IncludePageMacroParameters {

  /**
   * Inclusion page's name
   */
  private String       page   = StringUtils.EMPTY;

  /**
   * @return inclusion page's name
   */
  public String getPage() {
    return page;
  }

  /**
   * @param parentPage parent of children
   */
  @PropertyDescription("REQUIRED. To specify a page in a different space, use space.Page Title.")
  public void setPage(String page) {
    if (StringUtils.EMPTY.equals(page)) {
      throw new IllegalArgumentException("Inclusion page's name is empty!");
    }
    this.page = page;
  }

}
