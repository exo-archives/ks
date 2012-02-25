/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.wiki.transform;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.embed.EmbeddableComponentManager;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.converter.ConversionException;
import org.xwiki.rendering.converter.Converter;
import org.xwiki.rendering.renderer.printer.DefaultWikiPrinter;
import org.xwiki.rendering.renderer.printer.WikiPrinter;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.AbstractTransformation;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * Convert Macro Content to XWIKI2 Format
 */
@Component
@Named("deepconvert")
@Singleton
public class DeepConvertTransformation extends AbstractTransformation {
  
  private static Logger LOG = LoggerFactory.getLogger(DeepConvertTransformation.class.toString()); ;

  @Override
  public void transform(Block block, TransformationContext transformationContext) throws TransformationException {
    // Find all Word blocks and for each of them check if they're a wiki word or
    // not
    List<Block> children = new ArrayList<Block>();
    for (Block child : block.getChildren()) {

      if (child instanceof MacroBlock) {
        MacroBlock macroBlock = (MacroBlock) child;
        String content = macroBlock.getContent();
        // Bad parsing of content
        String macro = "{" + macroBlock.getId() + "}";
        if (content.endsWith(macro)) {
          content = content.substring(0, content.length() - macro.length());
        }
        String formattedContent = formatContent(macroBlock.getId(), content);
        child = new MacroBlock(macroBlock.getId(),
                                    macroBlock.getParameters(),
                                    formattedContent,
                                    macroBlock.isInline());
        child.setChildren(macroBlock.getChildren());
      }
      children.add(child);
      transform(child, transformationContext);
    }
    block.setChildren(children);

  }

  public String formatContent(String macro, String content) {

    if (macro.equals("noformat") || macro.equals("code") || macro.equals("csv") || macro.equals("style"))
      return content;

    EmbeddableComponentManager ecm = new EmbeddableComponentManager();
    ecm.initialize(Thread.currentThread().getContextClassLoader());
    WikiPrinter printer = new DefaultWikiPrinter();
    try {
      Converter converter = ecm.lookup(Converter.class);
      converter.convert(new StringReader(content), Syntax.CONFLUENCE_1_0, Syntax.XWIKI_2_0, printer);
      return printer.toString();
    } catch (ComponentLookupException e) {
      LOG.warn("TRANSFORMATION FAILURE: " + e.getMessage());
    } catch (ConversionException e) {
      LOG.warn("TRANSFORMATION FAILURE: " + e.getMessage());
    }
    return content;
  }
}
