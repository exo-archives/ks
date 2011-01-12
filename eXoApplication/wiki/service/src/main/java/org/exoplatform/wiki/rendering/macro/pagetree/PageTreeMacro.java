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
package org.exoplatform.wiki.rendering.macro.pagetree;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.impl.DefaultWikiModel;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.tree.JsonNodeData;
import org.exoplatform.wiki.tree.TreeNode;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.block.Block;
import org.xwiki.rendering.block.RawBlock;
import org.xwiki.rendering.macro.AbstractMacro;
import org.xwiki.rendering.macro.MacroExecutionException;
import org.xwiki.rendering.syntax.Syntax;
import org.xwiki.rendering.syntax.SyntaxType;
import org.xwiki.rendering.transformation.MacroTransformationContext;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Jan 11, 2011  
 */

@Component("pagetree")
public class PageTreeMacro extends AbstractMacro<PageTreeMacroParameters> {
  
  /**
   * The description of the macro
   */
  private static final String DESCRIPTION = "Display a hierachy descendants tree of a specific page";
  
  private static final Syntax XHTML_SYNTAX = new Syntax(SyntaxType.XHTML, "1.0");
  
  /**
   * Used to get the current syntax parser.
   */
  @Requirement
  private ComponentManager componentManager;
  
  @Requirement
  private Execution execution;
  
  private DefaultWikiModel model;
  
  public PageTreeMacro() {
    super("Page Tree", DESCRIPTION, PageTreeMacroParameters.class);
    setDefaultCategory(DEFAULT_CATEGORY_NAVIGATION);
  }

  @Override
  public List<Block> execute(PageTreeMacroParameters parameters,
                             String content,
                             MacroTransformationContext context) throws MacroExecutionException {

    String documentName = parameters.getRoot();
    String startDepth = parameters.getStartDepth();
    model = (DefaultWikiModel) getWikiModel(context);
    WikiPageParams params = model.getWikiMarkupContext(documentName);
    if (StringUtils.EMPTY.equals(documentName)) {
      WikiContext wikiContext = getWikiContext();
      if (wikiContext != null)
        params = wikiContext;
    }
    Block root;
    try {
      root = generateTree(params, startDepth);
      return Collections.singletonList(root);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
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

  protected WikiModel getWikiModel(MacroTransformationContext context) throws MacroExecutionException {
    try {
      return getComponentManager().lookup(WikiModel.class);
    } catch (ComponentLookupException e) {
      throw new MacroExecutionException("Failed to find wiki model", e);
    }
  }
  
  private Block generateTree(WikiPageParams params, String startDepth) throws Exception {
    HashMap<String, Object> context = new HashMap<String, Object>();
    context.put(TreeNode.DEPTH, startDepth);
    TreeNode node = TreeUtils.getDescendants(params, context);
    StringBuilder sb = new StringBuilder();
    WikiContext wikiContext = getWikiContext();   
    List<JsonNodeData> jsonData = TreeUtils.tranformToJson(node, null);
    String treeRestURI = wikiContext.getTreeRestURI();
    sb.append("<div class=\"UITreeExplorer\"> ")
      .append("  <input class=\"ChildrenURL\" type=\"hidden\" value=\"").append(treeRestURI).append("\" />")   
      .append(   buildHierachyNode(jsonData))
      .append("</div>");
    RawBlock testRaw = new RawBlock(sb.toString(), XHTML_SYNTAX);
    return testRaw;
  }  

  public String buildHierachyNode(List<JsonNodeData> jsonData) throws Exception {
    StringBuilder sb = new StringBuilder();
    int size = jsonData.size();
    sb.append("<div class=\"NodeGroup\">");
    for (int i = 0; i < size; i++) {
      sb.append(createNode(jsonData.get(i)));
    }
    sb.append("</div>");
    return sb.toString();
  }

  private String createNode(JsonNodeData jsonData) throws Exception{
    StringBuilder sb = new StringBuilder();
    
    String nodeType = jsonData.getNodeType().toString();
    String nodeTypeCSS = nodeType.substring(0, 1).toUpperCase() + nodeType.substring(1).toLowerCase();
    String iconType = (jsonData.isExpanded() == true) ? "Collapse" : "Expand";
    String lastNodeClass = "";   
    String path = jsonData.getPath().replaceAll("/", ".");
    String param = path +"/";
    String extendParam = jsonData.getExtendParam();
    if (extendParam!=null)
      param += extendParam.replaceAll("/", ".");
    if (jsonData.isLastNode()) {
      lastNodeClass = "LastNode";
    }
    if (!jsonData.isHasChild()) {
      iconType = "Empty";
    }
    int size = jsonData.getChildren().size();
    
  
    sb.append("<div  class=\"").append(lastNodeClass).append(" Node\" >")
      .append("  <div class=\"").append(iconType).append("Icon\" onclick=\"event.cancelBubble=true;  if(eXo.wiki.UITreeExplorer.collapseExpand(this)) return;  eXo.wiki.UITreeExplorer.render('"+ param + "', this)\" >")
      .append("    <div class=\"").append(nodeTypeCSS).append(" TreeNodeType Node \">")
      .append("      <div class=\"NodeLabel\">")
      .append(         getPageURI(jsonData))
      .append("      </div>")
      .append("    </div>")
      .append("  </div>");
    if (size > 0) {
      sb.append(buildHierachyNode(jsonData.getChildren()));
    }
    sb.append("</div>");
    return sb.toString();
  }
  
  private String getPageURI(JsonNodeData jsonData) throws Exception{
    StringBuilder sb = new StringBuilder();
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    WikiPageParams params = Utils.getPageParamsFromPath(URLDecoder.decode(jsonData.getPath(),
                                                                          "utf-8")
                                                                  .replace(".", "/"));
    PageImpl page = (PageImpl) wikiService.getPageById(params.getType(),
                                                       params.getOwner(),
                                                       params.getPageId());
    String pageTitle = page.getContent().getTitle();
    String pageURL = model.getDocumentViewURL(model.getDocumentName(params), null, null);   
    sb.append("<a title=\"").append(pageTitle).append("\" href=\"").append(pageURL).append("\">")
      .append(  pageTitle)
      .append("</a>");
    return sb.toString();
  }  
  
  private WikiContext getWikiContext() {
    ExecutionContext ec = execution.getContext();
    if (ec != null) {
      WikiContext wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
      return wikiContext;
    }
    return null;
  }
  
}
