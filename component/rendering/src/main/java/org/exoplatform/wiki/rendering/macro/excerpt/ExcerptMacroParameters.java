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
package org.exoplatform.wiki.rendering.macro.excerpt;

import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 12, 2011  
 */
public class ExcerptMacroParameters {

  /**
   * Show excerpt or not
   */
  private boolean hidden = false;

  /**
   * @return the value allow hide excerpt or not
   */
  public boolean isHidden() {
    return hidden;
  }

  /**
   * @param hidden the value show excerpt to set
   */
  @PropertyDescription("Hide Excerpted Content")
  public void setHidden(boolean hidden) {
    this.hidden = hidden;
  }

}
