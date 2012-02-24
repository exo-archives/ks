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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
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
  @Inject
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
      List<Block> contentBlocks = MacroUtils.parseSourceSyntax(getComponentManager(),
                                                               content,
                                                               context);
      Block floatBlock = new GroupBlock(contentBlocks, params);
      return Collections.singletonList(floatBlock); 
    } else {
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
