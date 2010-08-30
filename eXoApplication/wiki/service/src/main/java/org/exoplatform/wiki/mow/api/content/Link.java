/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.mow.api.content;

import org.exoplatform.wiki.mow.api.Page;

/**
 * Represents a link to another wiki page.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface Link extends ContentItem {
  /**
   * Get the alias for the link (name to be displayed)
   * 
   * @return
   */
  String getAlias();

  /**
   * get the UID of the target page
   * 
   * @return
   */
  String getTarget();

  /**
   * Get the page referenced by target
   * 
   * @return
   */
  Page getTargetPage();
}
