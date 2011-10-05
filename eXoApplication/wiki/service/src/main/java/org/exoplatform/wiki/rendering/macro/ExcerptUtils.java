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

import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.macro.excerpt.ExcerptMacro;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.xwiki.rendering.block.MacroBlock;
import org.xwiki.rendering.block.XDOM;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 12 Jan 2011  
 */
public class ExcerptUtils {
  
  public static String getExcerpts(WikiPageParams params) throws Exception {
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    PageImpl page = (PageImpl) wikiService.getPageById(params.getType(),
                                                       params.getOwner(),
                                                       params.getPageId());

    return getExcerpts(page.getContent().getText(), page.getSyntax());
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
          sb.append("<span class=\"Excerpt\">");
          sb.append(renderingService.render(" (" + block.getContent() + ")",
                                            sourceSyntax,
                                            Syntax.XHTML_1_0.toIdString(),
                                            false));
          sb.append("</span>");
        }
      }
    }
    return sb.toString();
  }

}
