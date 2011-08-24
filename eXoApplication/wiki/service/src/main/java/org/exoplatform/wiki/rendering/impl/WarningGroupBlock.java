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
package org.exoplatform.wiki.rendering.impl;

import java.util.List;

import org.exoplatform.wiki.rendering.macro.MacroUtils;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.GroupBlock;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.transformation.MacroTransformationContext;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Aug 16, 2011  
 */
public class WarningGroupBlock extends GroupBlock {
  public static final String WARNING_GROUP_CSS_CLASS = "box WikiWarningMessage";
  
  public WarningGroupBlock(String warningMessage, ComponentManager componentManager, MacroTransformationContext context)
      throws MacroExecutionException {
    List<Block> blocks = MacroUtils.parseSourceSyntax(componentManager, warningMessage, context);
    addChildren(blocks);
    
    setParameter("class", WARNING_GROUP_CSS_CLASS);
  }
}
