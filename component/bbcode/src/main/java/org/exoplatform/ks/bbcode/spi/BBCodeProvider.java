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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bbcode.spi;

import java.util.Collection;

import org.exoplatform.ks.bbcode.api.BBCode;

/**
 * Responsible to provide a list of BBCode definitions 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public interface BBCodeProvider {

  /**
   * Get the list of BBCodes tag names that this provider can provide
   * @return
   */
  Collection<String> getSupportedBBCodes();

  /**
   * Get a specific BBCode definition
   * @param tagName
   * @return
   */
  BBCode getBBCode(String tagName);

}
