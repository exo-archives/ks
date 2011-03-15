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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.macro.section;

import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 15 Mar 2011  
 */
public class SectionMacroParameters {
  
  /**
   * Position of section
   */
  private boolean justify;

  /**
   * @return position of section
   */
  public boolean isJustify() {
    return this.justify;
  }

  /**
   * @param justify position of section
   */
  @PropertyDescription("Postition of section. f not specified, left is default")
  public void setJustify(boolean justify) {
    this.justify = justify;
  }
}
