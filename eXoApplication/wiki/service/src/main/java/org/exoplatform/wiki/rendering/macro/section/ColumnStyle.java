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

import org.apache.commons.lang.StringUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 15 Mar 2011  
 */
public class ColumnStyle {
  private static final String FLOAT_RULE = "float:left;";

  private String              width;

  private String              paddingRight;

  public String getStyleAsString() {
    String style = FLOAT_RULE;
    if (!StringUtils.isBlank(this.width)) {
      style = style + "width:" + this.width + ";";
    }
    if (!StringUtils.isBlank(this.paddingRight)) {
      style = style + "padding-right:" + this.paddingRight + ";";
    }
    return style;
  }

  public String getWidth() {
    return this.width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public String getPaddingRight() {
    return this.paddingRight;
  }

  public void setPaddingRight(String paddingRight) {
    this.paddingRight = paddingRight;
  }
}
