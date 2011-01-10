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
package org.exoplatform.wiki.tree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WikiHome;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 7 Jan 2011  
 */
public class TreeUtils {
  
  public static List<JsonNodeData> getBlockDescendants(WikiPageParams params,
                                                       HashMap<String, Object> context) throws Exception {
    TreeNode treeNode = getDescendants(params, context);
    return tranformToBlock(treeNode, context);
  }
  
  public static TreeNode getDescendants(WikiPageParams params, HashMap<String, Object> context) throws Exception {
    Object wikiObject = Utils.getObjectFromParams(params);
    if (wikiObject instanceof WikiHome) {
      WikiHome wikiHome = (WikiHome) wikiObject;
      WikiHomeTreeNode wikiHomeNode = new WikiHomeTreeNode(wikiHome);
      wikiHomeNode.pushDescendants(context);
      return wikiHomeNode;
    } else if (wikiObject instanceof Page) {
      PageImpl page = (PageImpl) wikiObject;
      PageTreeNode pageNode = new PageTreeNode(page);
      pageNode.pushDescendants(context);
      return pageNode;
    } else if (wikiObject instanceof Wiki) {
      Wiki wiki = (Wiki) wikiObject;
      WikiTreeNode wikiNode = new WikiTreeNode(wiki);
      wikiNode.pushDescendants(context);
      return wikiNode;
    } else if (wikiObject instanceof String) {
      SpaceTreeNode spaceNode = new SpaceTreeNode((String) wikiObject);
      spaceNode.pushDescendants(context);
      return spaceNode;
    }
    return null;
  }
  
  public static List<JsonNodeData> tranformToBlock(TreeNode treeNode, HashMap<String, Object> context) throws Exception {
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    List<JsonNodeData> children = new ArrayList<JsonNodeData>();
    int counter = 1;
    boolean isSelectable = true;
    boolean isLastNode = false;
    PageImpl page = null;
    PageImpl currentPage = null;
    WikiPageParams currentPageParams = null;
    String currentPath = "";
    if (context != null) {
      currentPath = (String) context.get(TreeNode.CURRENT_PATH);
    }
    currentPageParams = Utils.getPageParamsFromPath(currentPath);

    for (TreeNode child : treeNode.getChildren()) {
      isSelectable = true;
      isLastNode = false;
      if (counter >= treeNode.getChildren().size()) {
        isLastNode = true;
      }
      // if (child.getNodeType().equals(TreeNodeType.WIKIHOME)) { isSelectable =
      // true;}
      if (child.getNodeType().equals(TreeNodeType.WIKI)) {
        isSelectable = false;
      } else if (currentPath != "" && child.getNodeType().equals(TreeNodeType.PAGE)) {
        page = ((PageTreeNode) child).getPage();
        currentPage = (PageImpl) wikiService.getPageById(currentPageParams.getType(),
                                                         currentPageParams.getOwner(),
                                                         currentPageParams.getPageId());
        if (currentPage != null
            && (currentPage.equals(page) || Utils.isDescendantPage(page, currentPage)))
          isSelectable = false;
      }
      children.add(new JsonNodeData(child, isLastNode, isSelectable, currentPath, context));
      counter++;
    }
    return children;
  }
  
  public static List<JsonNodeData> tranformToJson(TreeNode treeNode, HashMap<String, Object> context) throws Exception {
    WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                               .getComponentInstanceOfType(WikiService.class);
    List<JsonNodeData> children = new ArrayList<JsonNodeData>();
    int counter = 1;
    boolean isSelectable = true;
    boolean isLastNode = false;
    PageImpl page = null;
    PageImpl currentPage = null;
    WikiPageParams currentPageParams = null;
    String currentPath = "";
    if (context != null) {
      currentPath = (String) context.get(TreeNode.CURRENT_PATH);
    }
    currentPageParams = Utils.getPageParamsFromPath(currentPath);

    for (TreeNode child : treeNode.getChildren()) {
      isSelectable = true;
      isLastNode = false;
      if (counter >= treeNode.getChildren().size()) {
        isLastNode = true;
      }
      // if (child.getNodeType().equals(TreeNodeType.WIKIHOME)) { isSelectable =
      // true;}
      if (child.getNodeType().equals(TreeNodeType.WIKI)) {
        isSelectable = false;
      } else if (currentPath != "" && child.getNodeType().equals(TreeNodeType.PAGE)) {
        page = ((PageTreeNode) child).getPage();
        currentPage = (PageImpl) wikiService.getPageById(currentPageParams.getType(),
                                                         currentPageParams.getOwner(),
                                                         currentPageParams.getPageId());
        if (currentPage != null
            && (currentPage.equals(page) || Utils.isDescendantPage(page, currentPage)))
          isSelectable = false;
      }
      children.add(new JsonNodeData(child, isLastNode, isSelectable, currentPath, context));
      counter++;
    }
    return children;
  }
}
