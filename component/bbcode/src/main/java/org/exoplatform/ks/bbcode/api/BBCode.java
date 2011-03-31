/***************************************************************************
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.ks.bbcode.api;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Oct 7, 2009 - 6:57:52 AM  
 */
public class BBCode {

  public static final String BBCODE = "bbcode";

  private String             id;

  private String             tagName;

  private String             replacement;

  private String             description;

  private String             example;

  private boolean            isActive;

  private boolean            isOption;

  public BBCode() {
    isOption = false;
    isActive = true;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String name) {
    this.tagName = name;
  }

  public String getReplacement() {
    return replacement;
  }

  public void setReplacement(String replacement) {
    this.replacement = replacement;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getExample() {
    return example;
  }

  public void setExample(String example) {
    this.example = example;
  }

  public boolean isActive() {
    return isActive;
  }

  public void setActive(boolean isActive) {
    this.isActive = isActive;
  }

  public boolean isOption() {
    return isOption;
  }

  public void setOption(boolean isOption) {
    this.isOption = isOption;
  }

  public String toString() {
    return getTagName() + (isOption ? "(option)" : "");
  }

  public int hashCode() {
    return super.hashCode();
  }

}
