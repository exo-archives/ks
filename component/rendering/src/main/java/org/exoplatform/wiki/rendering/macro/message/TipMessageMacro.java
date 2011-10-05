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
package org.exoplatform.wiki.rendering.macro.message;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.macro.message.AbstractMessageMacro;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 23, 2010  
 */
@Component("tip")
public class TipMessageMacro extends AbstractMessageMacro {

  /**
   * Create and initialize the descriptor of the macro.
   */
  public TipMessageMacro()
  {
      super("Tip Message", "Displays a tip message.");
      setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
  }
  
}
