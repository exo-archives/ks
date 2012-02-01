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
package org.exoplatform.wiki.rendering.macro.excerpt;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.exoplatform.wiki.rendering.macro.MacroUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 12, 2011  
 */

@Component("excerpt")
public class ExcerptMacro extends AbstractMacro<ExcerptMacroParameters> {
  
  /**
   * The description of the macro
   */
  private static final String DESCRIPTION = "Marks part of the page's content for use by other macros";
  
  private static final String CONTENT_DESCRIPTION = "Text or macro are allowed";
  
  public static final String EXCERPT_CLASS = "ExcerptClass";
  
  public static final String MACRO_ID = "excerpt";
  
  /**
   * Used to get the current syntax parser.
   */
  @Inject
  private ComponentManager    componentManager;
  
  public ExcerptMacro() {
    super("Excerpt",
          DESCRIPTION,
          new DefaultContentDescriptor(CONTENT_DESCRIPTION),
          ExcerptMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_CONTENT);
  }

  @Override
  public List<Block> execute(ExcerptMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {

    try {
      boolean isHidden = parameters.isHidden();
      String styleProps = "display: " + ((isHidden) ? "none" : "block");
      Map<String, String> blockParams = new HashMap<String, String>();
      blockParams.put("class", EXCERPT_CLASS);
      blockParams.put("style", styleProps);
      List<Block> contentBlocks = MacroUtils.parseSourceSyntax(componentManager, content, context);
      Block result = new GroupBlock(contentBlocks, blockParams);
      return Collections.singletonList(result);
    } catch (Exception e) {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean supportsInlineMode() {

    return true;
  }
  
  /**
   * @return the component manager.
   */
  public ComponentManager getComponentManager() {
    return this.componentManager;
  }
}
