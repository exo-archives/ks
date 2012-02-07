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
package org.exoplatform.wiki.analyze;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Author : Dimitri BAELI
 *          dbaeli@exoplatform.com
 * Feb 02, 2012  
 */
public class Page {
  public String pageId;
  public String pageName;
  public String pageParentId;
  public WikiSpace space;
  public Page pageParent;

  public HashSet<String> comments = new HashSet<String>();
  public HashSet<String> attachments = new HashSet<String>();
  public HashSet<Page> childPages = new HashSet<Page>();
  public Map<String, Integer> macrosMap;
  // Body size in ko
  public long bodySize;

  public Page(WikiSpace space, String pageId, String pageName, String pageParentId) {
    this.space = space;
    this.pageId = pageId;
    this.pageName = pageName;
    this.pageParentId = pageParentId;
  }

  public void addComment(String commentId) {
    comments.add(commentId);
  }

  public void addAttachement(String attachmentId) {
    attachments.add(attachmentId);
  }

  public void setPageParent(Page pageParent) {
    this.pageParent = pageParent;
  }

  public void registerChild(Page page) {
    childPages.add(page);
  }

  public String getPath() {
    String path = pageName;
    if (pageParent != null) {
      path = pageParent.getPath() + "/" + path;
    }

    // TODO Store once computed to avoid multiple calls
    return path;
  }
}