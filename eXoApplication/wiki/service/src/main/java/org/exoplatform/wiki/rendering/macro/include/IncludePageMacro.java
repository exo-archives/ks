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
package org.exoplatform.wiki.rendering.macro.include;

import java.util.Collections;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.DefaultWikiModel;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 06, 2011  
 */

@Component("includepage")
public class IncludePageMacro extends AbstractMacro<IncludePageMacroParameters> {
  
  /**
   * The description of the macro
   */
  private static final String DESCRIPTION = "Includes the contents of a page within current";
  
  /**
   * Used to get the current syntax parser.
   */
  @Requirement
  private ComponentManager    componentManager;

  @Requirement
  private Execution           execution;

  private DefaultWikiModel    model;

  private RenderingService    renderingservice;

  private WikiService         wservice;
  
  public IncludePageMacro() {
    super("Include Page", DESCRIPTION, IncludePageMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_CONTENT);
  }

  @Override
  public List<Block> execute(IncludePageMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {

    String documentName = parameters.getPage();
    model = (DefaultWikiModel) getWikiModel(context);
    renderingservice = getRenderingService();
    wservice = getWikiService();
    WikiPageParams params = model.getWikiMarkupContext(documentName,ResourceType.DOCUMENT);
    WikiContext currentContext = null;
    WikiContext pageContext = null;
    StringBuilder includeContent = new StringBuilder();
    ExecutionContext ec = execution.getContext();
    try {
      if (ec != null) {
        currentContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
        pageContext = currentContext.clone();
        pageContext.setOwner(params.getOwner());
        pageContext.setType(params.getType());
        pageContext.setPageId(params.getPageId());
        // Set page context as current context
        ec.setProperty(WikiContext.WIKICONTEXT, pageContext);
      }
      includeContent.append("<div class=\"IncludePage \" >");
      PageImpl page = (PageImpl) wservice.getPageById(params.getType(),
                                                      params.getOwner(),
                                                      params.getPageId());
      if (page != null) {
        includeContent.append(renderingservice.render(page.getContent().getText(),
                                                      page.getSyntax(),
                                                      Syntax.XHTML_1_0.toIdString(),
                                                      false));
      }
      includeContent.append("</div>");
      Block result = new RawBlock(includeContent.toString(), Syntax.XHTML_1_0);
      return Collections.singletonList(result);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return Collections.emptyList();
    } finally {
      // Restore current context
      ec.setProperty(WikiContext.WIKICONTEXT, currentContext);
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

  protected WikiModel getWikiModel(MacroTransformationContext context) throws MacroExecutionException {
    try {
      return getComponentManager().lookup(WikiModel.class);
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find wiki model", e);
    }
  }
  
  protected RenderingService getRenderingService() {
    return (RenderingService) ExoContainerContext.getCurrentContainer()
                                                 .getComponentInstanceOfType(RenderingService.class);
  }
  
  protected WikiService getWikiService() {
    return (WikiService) ExoContainerContext.getCurrentContainer()
                                                 .getComponentInstanceOfType(WikiService.class);
  }
  
}
