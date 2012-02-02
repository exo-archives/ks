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

import java.util.HashMap;
import java.util.Map;

/**
 * Created by The eXo Platform SAS
 * Author : Dimitri BAELI
 * Feb 02, 2012  
 */
public class WikiSpace {
  public String spaceId;
  public String spaceName;
  public String spaceKey;

  Map<String, Page> pages = new HashMap<String, Page>();
  Map<String, Integer> macroMap = new HashMap<String, Integer>();

  public WikiSpace(String spaceId, String spaceKey, String spaceName) {
    this.spaceId = spaceId;
    this.spaceName = spaceName;
    this.spaceKey = spaceKey;
  }

  public void registerPage(Page page) {
    pages.put(page.pageId, page);
  }
}