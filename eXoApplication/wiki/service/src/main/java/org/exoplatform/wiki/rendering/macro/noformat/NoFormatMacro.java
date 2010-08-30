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
package org.exoplatform.wiki.rendering.macro.noformat;

import java.util.Collections;
import java.util.List;

import org.xwiki.component.annotation.Component;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.VerbatimBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 27, 2010  
 */
@Component("noformat")
public class NoFormatMacro extends AbstractMacro<Object> {

  /**
   * The description of the macro.
   */
  private static final String DESCRIPTION = "Makes a pre-formatted block of text with no syntax highlighting";

  /**
   * The description of the macro content.
   */
  private static final String CONTENT_DESCRIPTION = "block of text";
  
  /**
   * Create and initialize the descriptor of the macro.
   */
  public NoFormatMacro() {
    super("NoFormat", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION), Object.class);
    setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
  }
  
  @Override
  public List<Block> execute(Object parameters, String content, MacroTransformationContext context) throws MacroExecutionException {
    if (content != null) {
      Block verbatimBlock = new VerbatimBlock(content, context.isInline());
      return Collections.singletonList(verbatimBlock);
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean supportsInlineMode() {
    return true;
  }

}
