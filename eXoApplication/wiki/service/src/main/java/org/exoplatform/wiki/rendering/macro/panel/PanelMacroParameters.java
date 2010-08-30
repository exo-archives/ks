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
package org.exoplatform.wiki.rendering.macro.panel;

import org.apache.commons.lang.StringUtils;
import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 26, 2010  
 */
public class PanelMacroParameters {

  /**
   * @see #getTitle()
   */
  private String title = StringUtils.EMPTY;

  /**
   * @return the title to be displayed in the panel header. Note that it can
   *         contain content in the current syntax and that text which will be
   *         parsed and rendered as any syntax content
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title refer to {@link #getTitle()}
   */
  @PropertyDescription("The title which is to be displayed in the panel header")
  public void setTitle(String title) {
    this.title = title;
  }
  
}
