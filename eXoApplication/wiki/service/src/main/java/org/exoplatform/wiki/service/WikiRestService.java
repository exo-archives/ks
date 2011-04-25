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
package org.exoplatform.wiki.service;

import javax.ws.rs.core.Response;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 20, 2010  
 */
public interface WikiRestService {

  /**
   * 
   * @param sessionKey key is used to retrieve the editor input value from the session.
   * @param isMarkup if <em>true</em> then <em>markup content</em> is returned else <em>html content</em> is returned
   * @return the instance of javax.ws.rs.core.Response
   */
  Response getWikiPageContent(String sessionKey,
                              String wikiContextKey,
                              boolean isMarkup,
                              String data);
  
}
