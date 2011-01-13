/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.macro;

import java.io.StringReader;
import java.net.URLDecoder;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.macro.excerpt.ExcerptMacro;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.ParagraphBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.parser.ParseException;
import org.xwiki.rendering.parser.Parser;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.util.ParserUtils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 12 Jan 2011  
 */
public class MacroUtils {

  /**
   * Parse provided content with the parser of the current wiki syntax.
   * 
   * @param content the content to parse.
   * @param context the context of the macro transformation.
   * @return an XDOM containing the parser content.
   * @throws MacroExecutionException failed to parse content
   */
  public static List<Block> parseSourceSyntax(ComponentManager componentManager,
                                              String content,
                                              MacroTransformationContext context) throws MacroExecutionException {
    Parser parser = getSyntaxParser(componentManager, context);
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
  
  public static String getExcerpts(WikiPageParams params) throws Exception {
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    PageImpl page = (PageImpl) wikiService.getPageById(params.getType(),
                                                       params.getOwner(),
                                                       params.getPageId());

    return getExcerpts(page.getContent().getText(), page.getContent().getSyntax());
  }

  private static String getExcerpts(String markup, String sourceSyntax) throws Exception {
    RenderingService renderingService = (RenderingService) ExoContainerContext.getCurrentContainer()
                                                                              .getComponentInstanceOfType(RenderingService.class);
    StringBuilder sb = new StringBuilder();
    if (markup != null) {
      XDOM xdom = renderingService.parse(markup, sourceSyntax);
      List<MacroBlock> mBlocks = xdom.getChildrenByType(MacroBlock.class, true);
      for (MacroBlock block : mBlocks) {

        if (block.getId().equals(ExcerptMacro.MACRO_ID)) {
          sb.append(renderingService.render(block.getContent(),
                                            sourceSyntax,
                                            Syntax.XHTML_1_0.toIdString(),
                                            false));
        }
      }
    }
    return sb.toString();
  }

  private static Parser getSyntaxParser(ComponentManager componentManager, MacroTransformationContext context) throws MacroExecutionException {
    try {
      return componentManager.lookup(Parser.class, context.getSyntax().toIdString());
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find source parser for syntax ["
          + context.getSyntax() + "]", e);
    }
  }
}
