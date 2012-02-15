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
package org.exoplatform.wiki.rendering.macro.jira;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.properties.annotation.PropertyDescription;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * 14 Jan 2012
 */
public class JiraIssueMacroParameters {

  /**
   * Default columns if none defined.
   */
  private static final List<String> DEFAULT_COLUMNS = new ArrayList<String>();

  // Static init...
  static {
    DEFAULT_COLUMNS.add("Type");
    DEFAULT_COLUMNS.add("Key");
    DEFAULT_COLUMNS.add("Summary");
  }

  /**
   * JIRA RSS Feed URL.
   */
  private String query = null;

  /**
   * Columns specified by the user.
   */
  private List<String> columns;

  /**
   * Feed title to show.
   */
  private String title = "JIRA Issues";

  /**
   * @return the columns names.
   */
  public List<String> getColumns() {
    if (columns == null) {
      columns = new ArrayList<String>(DEFAULT_COLUMNS);
    }
    return this.columns;
  }
  
  @PropertyDescription("JIRA Field Columns to Display. Sample: type,key,summary")
  public void setColumns(List<String> columns) {
    this.columns = columns;
  }

  /**
   * @return the feed
   */
  public String getUrl() {
    return this.query;
  }

  /**
   * @param querry
   *          the feed to set
   */
  @PropertyDescription("Url refer to Jira data. Sample: https://jira.side.org/jira.issueviews:path/data.xml?jqlQuery=query")
  public void setUrl(final String querry) {
    this.query = querry;
  }

  /**
   * @return the title
   */
  public String getTitle() {
    return this.title;
  }

  /**
   * @param title
   *          the title to set
   */
  public void setTitle(final String title) {
    this.title = title;
  }
}
