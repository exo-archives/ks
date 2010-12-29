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
package org.exoplatform.wiki.rendering.macro.floatmacro;

import org.apache.commons.lang.StringUtils;
import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 29, 2010  
 */
public class FloatMacroParameters {
  
  /**
   * the CSS class of the DIV element
   */
  private String cssClass = StringUtils.EMPTY;
  
  /**
   * side to float content: left or right, it will be value of 'float' property of the DIV element
   */
  private String side = StringUtils.EMPTY;
  
  /**
   * CSS value of 'width' property of the DIV element
   */
  private String width = StringUtils.EMPTY;
  
  /**
   * CSS value of 'background' property of the DIV element, i.e: red, #3C78B5, ... 
   */
  private String background = StringUtils.EMPTY;
  
  /**
   * CSS value of 'border' property of the DIV element, i.e: 2px solid
   */
  private String border = StringUtils.EMPTY;
  
  /**
   * CSS value of 'margin' property of the DIV element
   */
  private String margin = StringUtils.EMPTY;
  
  /**
   * CSS value of 'padding' property of the DIV element
   */
  private String padding = StringUtils.EMPTY;
  
  
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
  
  /**
   * @return the side
   */
  public String getSide() {
    return side;
  }
  
  /**
   * @param side the side to set (left/right)
   */
  @PropertyDescription("side to float content: left or right")
  public void setSide(String side) {
    this.side = side;
  }
  
  /**
   * @return the width
   */
  public String getWidth() {
    return width;
  }
  
  /**
   * @param width the width to set
   */
  @PropertyDescription("CSS width property")
  public void setWidth(String width) {
    this.width = width;
  }
  
  /**
   * @return the background
   */
  public String getBackground() {
    return background;
  }
  
  /**
   * @param background the background to set
   */
  @PropertyDescription("CSS background color")
  public void setBackground(String background) {
    this.background = background;
  }
  
  /**
   * @return the border
   */
  public String getBorder() {
    return border;
  }
  
  /**
   * @param border the border to set
   */
  @PropertyDescription("CSS border property")
  public void setBorder(String border) {
    this.border = border;
  }
  
  /**
   * @return margin
   */
  public String getMargin() {
    return margin;
  }
  
  /**
   * @param margin the margin to set
   */
  @PropertyDescription("CSS margin property")
  public void setMargin(String margin) {
    this.margin = margin;
  }
  
  /**
   * @return the padding
   */
  public String getPadding() {
    return padding;
  }
  
  /**
   * @param padding the padding to set
   */
  @PropertyDescription("CSS padding property")
  public void setPadding(String padding) {
    this.padding = padding;
  }
  
}
