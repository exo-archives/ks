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
package org.exoplatform.wiki.rendering.render.confluence;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.internal.renderer.AbstractPrintRendererFactory;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 2, 2010  
 */

/**
 * Create Confluence Syntax 1.0 Renderers.
 */
@Component("confluence/1.0")
public class ConfluenceSyntaxRendererFactory extends AbstractPrintRendererFactory {
  /**
   * {@inheritDoc}
   * 
   * @see AbstractPrintRendererFactory#getSyntax()
   */
  public Syntax getSyntax() {
    return Syntax.CONFLUENCE_1_0;
  }
}

