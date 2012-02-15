/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.macro.iframe;

import org.apache.commons.lang.StringUtils;
import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : Dimitri BAELI
 *          dbaeli@exoplatform.com
 * 7 Jan 2012
 * 
 * Manage an iframe node
 */
public class IFrameMacroParameters {
  private String src = StringUtils.EMPTY;
  
  private String width = StringUtils.EMPTY;
  
  private String height = StringUtils.EMPTY;

  /**
   * @return the src to be displayed in the panel header. Note that it can
   *         contain content in the current syntax and that text which will be
   *         parsed and rendered as any syntax content
   */
  public String getSrc() {
    return src;
  }

  /**
   * @param src refer to {@link #getSrc()}
   */
  @PropertyDescription("The src to declare in the iframe")
  public void setSrc(String src) {
    this.src = src;
  }

  /**
   * @return the src to be displayed in the panel header. Note that it can
   *         contain content in the current syntax and that text which will be
   *         parsed and rendered as any syntax content
   */
  public String getWidth() {
    return width;
  }

  /**
   * @param width refer to {@link #getWidth()}
   **/
  @PropertyDescription("The width to declare in the iframe")
  public void setWidth(String width) {
    this.width = width;
  }

  /**
   * @return the height to be displayed in the panel header.
   */
  public String getHeight() {
    return height;
  }

  /**
   * @param height refer to {@link #getHeight()}
   */
  @PropertyDescription("The height to declare in the iframe")
  public void setHeight(String height) {
    this.height = height;
  }
}
