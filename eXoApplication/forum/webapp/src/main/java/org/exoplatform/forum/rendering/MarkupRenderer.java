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
package org.exoplatform.forum.rendering;

/**
 * A MarkupRenderer is capable of rendering markup in special syntax such as bbcode wiki or others.
 * 
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface MarkupRenderer {

  /**
   * Process some input markup. Note that this tells nothing on the syntax of the markup.
   * This is left to the implementation to decide which syntax to use.
   * @param markup
   * @return processed markup
   * @throws RenderingException if the markup processing failed for any reason.
   */
  public String processMarkup(String markup) throws RenderingException;
  
  
  
}
