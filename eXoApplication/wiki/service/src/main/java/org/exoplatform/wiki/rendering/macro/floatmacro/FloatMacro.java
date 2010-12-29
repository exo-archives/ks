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
package org.exoplatform.wiki.rendering.macro.floatmacro;

import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.macro.descriptor.DefaultContentDescriptor;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.ParserUtils;
import org.xwiki.rendering.parser.ParseException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Dec 29, 2010  
 */

@Component("float")
public class FloatMacro extends AbstractMacro<FloatMacroParameters> {
  
  private static final String LEFT = "left";
  private static final String RIGHT = "right";
  
  /**
   * The description of the macro
   */
  private static final String DESCRIPTION = "Allow content to 'float' to the left or right";
  
  private static final String CONTENT_DESCRIPTION = "The content to float";
  
  /**
   * Used to get the current syntax parser.
   */
  @Requirement
  private ComponentManager componentManager;
  
  public FloatMacro() {
    super("Float", DESCRIPTION, new DefaultContentDescriptor(CONTENT_DESCRIPTION), FloatMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_FORMATTING);
  }

  @Override
  public List<Block> execute(FloatMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {
    if (content != null) {
      String cssClass = parameters.getCssClass();
      String side = parameters.getSide();
      String width = parameters.getWidth();
      String background = parameters.getBackground();
      String border = parameters.getBorder();
      String margin = parameters.getMargin();
      String padding = parameters.getPadding();
      
      // default value for side is 'right'
      if (StringUtils.isEmpty(side) || (!side.equals(LEFT) && !side.equals(RIGHT))) side = RIGHT;
      if (StringUtils.isEmpty(margin)) margin="3px";
      if (StringUtils.isEmpty(padding)) padding="3px";
      String styles = "float: " + side + ";width:" + width + ";background:" + background + ";border:" + border + 
      ";margin:" + margin + ";padding:" + padding + ";";
      
      Map<String, String> params = new HashMap<String, String>();
      if (!StringUtils.isEmpty(cssClass)) params.put("class", cssClass);
      params.put("style", styles);
      List<Block> contentBlocks = parseSourceSyntax(content, context);
      Block floatBlock = new GroupBlock(contentBlocks, params);
      return Collections.singletonList(floatBlock); 
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  public boolean supportsInlineMode() {
    // TODO Auto-generated method stub
    return true;
  }
  
  /**
   * @return the component manager.
   */
  public ComponentManager getComponentManager() {
    return this.componentManager;
  }
  
  /**
   * Parse provided content with the parser of the current wiki syntax.
   * 
   * @param content the content to parse.
   * @param context the context of the macro transformation.
   * @return an XDOM containing the parser content.
   * @throws MacroExecutionException failed to parse content
   */
  protected List<Block> parseSourceSyntax(String content, MacroTransformationContext context) throws MacroExecutionException {
    Parser parser = getSyntaxParser(context);

    try {
      List<Block> blocks = parser.parse(new StringReader(content)).getChildren();
      
      if (context.isInline()) {
        ParserUtils parseUtils = new ParserUtils();
        parseUtils.removeTopLevelParagraph(blocks);
      }
      
      if (blocks.size() == 1 && blocks.get(0) instanceof ParagraphBlock) {
        List<Block> children = blocks.get(0).getChildren();
        if (children.size() > 0) {
          blocks = children;
        }
      }

      return blocks;
    } catch (ParseException e) {
      throw new MacroExecutionException("Failed to parse content [" + content
          + "] with Syntax parser [" + parser.getSyntax() + "]", e);
    }
  }
  
  /**
   * Get the parser for the current syntax.
   * 
   * @param context the context of the macro transformation (from which to get the current syntax)
   * @return the parser for the current syntax
   * @throws MacroExecutionException Failed to find source parser.
   */
  protected Parser getSyntaxParser(MacroTransformationContext context) throws MacroExecutionException {
    try {
      return getComponentManager().lookup(Parser.class, context.getSyntax().toIdString());
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find source parser for syntax ["
          + context.getSyntax() + "]", e);
    }
  }
}
