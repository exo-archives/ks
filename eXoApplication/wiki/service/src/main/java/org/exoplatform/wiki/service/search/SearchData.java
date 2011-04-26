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
package org.exoplatform.wiki.service.search;

import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.utils.Utils;


/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 28 Jan 2011  
 */
public class SearchData {
  public String text;

  public String title;

  public String content;

  public String wikiType;

  public String wikiOwner;

  public String pageId;

  public String jcrQueryPath;
  
  public static String ALL_PATH    = "%/";

  public static String PORTAL_PATH = "/exo:applications/"
                                       + WikiNodeType.Definition.WIKI_APPLICATION + "/"
                                       + WikiNodeType.Definition.WIKIS + "/%/";

  public static String GROUP_PATH  = "/Groups/%/ApplicationData/"
                                       + WikiNodeType.Definition.WIKI_APPLICATION + "/";

  public static String USER_PATH   = "/Users/%/ApplicationData/"
                                       + WikiNodeType.Definition.WIKI_APPLICATION + "/";

  public SearchData(String text, String title, String content, String wikiType, String wikiOwner, String pageId) {
    this.text = text;
    this.title = title;
    this.content = content;
    this.wikiType = wikiType;
    this.wikiOwner = Utils.validateWikiOwner(wikiType, wikiOwner);
    this.pageId = pageId;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public String getWikiType() {
    return wikiType;
  }

  public void setWikiType(String wikiType) {
    this.wikiType = wikiType;
  }

  public String getWikiOwner() {
    return wikiOwner;
  }

  public void setWikiOwner(String wikiOwner) {
    this.wikiOwner = wikiOwner;
  }

  public String getPageId() {
    return pageId;
  }

  public void setPageId(String pageId) {
    this.pageId = pageId;
  }

  public String getJcrQueryPath() {
    return jcrQueryPath;
  }

  public void setJcrQueryPath(String jcrQueryPath) {
    this.jcrQueryPath = jcrQueryPath;
  }
  
  public String getStatement() {
    return null;
  }
}
