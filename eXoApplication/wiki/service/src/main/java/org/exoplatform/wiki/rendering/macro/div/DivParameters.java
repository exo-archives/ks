/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.macro.div;

import org.apache.commons.lang.StringUtils;
import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Jul 13, 2011  
 */
public class DivParameters {

  /**
   * Style sheet content
   */
  private String style = StringUtils.EMPTY;
  
  /**
   * the CSS class of the DIV element
   */
  private String cssClass = StringUtils.EMPTY;

  /**
   * @return the style content
   */
  public String getStyle() {
    return style;
  }

  /**
   * @param style content of the style sheet to set
   */
  @PropertyDescription("Style sheet content (text-align: left;color: red;)")
  public void setStyle(String style) {
    this.style = style;
  }
  
  /**
   * @return the CSS class
   */
  public String getCssClass() {
    return cssClass;
  }
  
  /**
   * @param cssClass the CSS class to set
   */
  @PropertyDescription("CSS class of the DIV element")
  public void setCssClass(String cssClass) {
    this.cssClass = cssClass;
  }
  
}
