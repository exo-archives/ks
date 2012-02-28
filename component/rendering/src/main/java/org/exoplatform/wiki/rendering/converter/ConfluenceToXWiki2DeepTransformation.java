/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.rendering.converter;

import java.util.ArrayList;
import java.util.List;

import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.transformation.TransformationContext;
import org.xwiki.rendering.transformation.TransformationException;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Feb 27, 2012
 */
public class ConfluenceToXWiki2DeepTransformation {
  private static ConfluenceToXWiki2DeepTransformation instance;
  
  private ComponentManager componentManager;
  
  public static ConfluenceToXWiki2DeepTransformation getInstance() {
    if (instance == null) {
      instance = new ConfluenceToXWiki2DeepTransformation();
    }
    return instance;
  }
  
  private ConfluenceToXWiki2DeepTransformation() {
  }
  
  public void setComponentManager(ComponentManager componentManager) {
    this.componentManager = componentManager;
  }

  public void transform(Block block, TransformationContext transformationContext) throws TransformationException {
    // Find all Word blocks and for each of them check if they're a wiki word or not
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
    if (macro.equals("noformat") || macro.equals("code") || macro.equals("csv") || macro.equals("style")) {
      return content;
    }
    return ConfluenceToXWiki2Transformer.transformContent(content, componentManager);
  }
}
