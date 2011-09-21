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
package org.exoplatform.wiki.rendering.macro.children;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.wiki.rendering.macro.MacroUtils;
import org.xwiki.properties.annotation.PropertyDescription;
import org.xwiki.rendering.macro.MacroExecutionException;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 06, 2011  
 */
public class ChildrenMacroParameters {

//  public enum HEADINGSTYLE {
//
//    h1, h2, h3, h4, h5, h6,
//
//  };
//
//  public enum SORTBY {
//
//    Author, Title, Modified
//
//  };

  /**
   * Show descendant or not
   */
  private boolean      descendant   = false;

  /**
   * Title of parent Page
   */
  private String       parent   = StringUtils.EMPTY;

  /**
   * Number of children
   */
  private String     childrenNum = StringUtils.EMPTY;

  /**
   * Depth of tree page
   */
  private String     depth       = StringUtils.EMPTY;

  /**
   * Show excerpt or not
   */
  private boolean excerpt = false;

//  /**
//   * Style of heading
//   */
//  private HEADINGSTYLE heading = HEADINGSTYLE.h4;

//  /**
//   * Sort children by author, title or modified date,
//   */
//  private SORTBY       sortBy       = SORTBY.Title;
//
//  /**
//   * Sort order
//   */
//  private boolean      desc         = true;

  /**
   * @return the value allow show descendant or not
   */
  public boolean isDescendant() {
    return descendant;
  }

  /**
   * @param descendant the value show descendant to set
   */
  @PropertyDescription("The value allow show descendant or not")
  public void setDescendant(boolean descendant) {
    this.descendant = descendant;
  }

  /**
   * @return parent page of children
   */
  public String getParent() {
    return parent;
  }

  /**
   * @param parentPage parent of children
   */
  @PropertyDescription("Parent page of children. If not specified, the current page is used")
  public void setParent(String parentPage) {
    this.parent = parentPage;
  }

  /**
   * @return the number of children
   */
  public String getChildrenNum() {
    return childrenNum;
  }

  /**
   * @param childrenNum number of children to set
   * @throws MacroExecutionException if parameter is not empty and not a number
   */
  @PropertyDescription("The number of children. If not specified, no limit is applied")
  public void setChildrenNum(String childrenNum) throws MacroExecutionException {
    MacroUtils.validateNumberParam(childrenNum);
    this.childrenNum = childrenNum;
  }

  /**
   * @return the depth of children
   */
  public String getDepth() {
    return depth;
  }

  /**
   * @param depth depth of children to set   
   * @throws MacroExecutionException if parameter is not empty and not a number
   */
  @PropertyDescription("Depth of children. If not specified, no limit is applied")
  public void setDepth(String depth) throws MacroExecutionException {
    MacroUtils.validateNumberParam(depth);
    this.depth = depth;
  }  

  /**
   * @return the value allow hide excerpt or not
   */
  public boolean isExcerpt() {
    return excerpt;
  }

  /**
   * @param hidden the value show excerpt to set
   */
  @PropertyDescription("Include Excerpts")
  public void setExcerpt(boolean excerpt) {
    this.excerpt = excerpt;
  }
//
//  /**
//   * @return style of children
//   */
//  public HEADINGSTYLE getHeading() {
//    return heading;
//  }
//
//  /**
//   * @param headingStyle style of children to set
//   */
//  @PropertyDescription("Style of children")
//  public void setHeading(HEADINGSTYLE headingStyle) {
//    this.heading = headingStyle;
//  }
//
//  /**
//   * @return sort's type
//   */
//  public SORTBY getSortBy() {
//    return sortBy;
//  }
//
//  /**
//   * @param sortBy sort's type to set
//   */
//  @PropertyDescription("Sort's type")
//  public void setSortBy(SORTBY sortBy) {
//    this.sortBy = sortBy;
//  }
//
//  /**
//   * @return order of sort type
//   */
//  public boolean isDesc() {
//    return desc;
//  }
//
//  /**
//   * @param desc order of sort type to set
//   */
//  @PropertyDescription("Order of sort type")
//  public void setDesc(boolean desc) {
//    this.desc = desc;
//  }

}
