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

import java.util.List;

/**
 * Represents a bit of the content of a page such as a paragraph, a text, a
 * link...
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public interface ContentItem {

  /**
   * Get the unique identifier for the content
   * 
   * @return
   */
  String getId();

  /**
   * Get the child items
   * 
   * @return
   */
  List<?> getChildren();

  /**
   * Get the text representation of the content item
   * 
   * @return
   */
  String getText();
  
  void setText(String text);
}
