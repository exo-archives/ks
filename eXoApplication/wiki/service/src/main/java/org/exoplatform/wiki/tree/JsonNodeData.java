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
package org.exoplatform.wiki.tree;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Oct 8, 2010  
 */
public class JsonNodeData {  

  protected String         name;

  protected String         absPath;

  protected String         relPath;

  protected boolean        hasChild;

  protected TreeNodeType   nodeType;

  protected boolean        isLastNode;

  protected boolean        isSelectable;

  protected String         currentPagePath;  
  
  public JsonNodeData(TreeNode treeNode,
                      boolean isLastNode,
                      boolean isSelectable,
                      String currentPagePath) throws UnsupportedEncodingException {
    this.name = treeNode.getName();
    this.absPath = treeNode.getAbsPath();
    this.relPath =  URLEncoder.encode(treeNode.getRelPath(), "utf-8");
    this.hasChild = treeNode.isHasChild();
    this.nodeType = treeNode.getNodeType();
    this.isLastNode = isLastNode;
    this.isSelectable = isSelectable;
    this.currentPagePath = URLEncoder.encode( currentPagePath, "utf-8");
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAbsPath() {
    return absPath;
  }

  public void setAbsPath(String absPath) {
    this.absPath = absPath;
  }

  public String getRelPath() {
    return relPath;
  }

  public void setRelPath(String relPath) {
    this.relPath = relPath;
  }

  public boolean isHasChild() {
    return hasChild;
  }

  public void setHasChild(boolean hasChild) {
    this.hasChild = hasChild;
  }

  public TreeNodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(TreeNodeType nodeType) {
    this.nodeType = nodeType;
  }

  public boolean isLastNode() {
    return isLastNode;
  }

  public void setLastNode(boolean isLastNode) {
    this.isLastNode = isLastNode;
  }

  public boolean isSelectable() {
    return isSelectable;
  }

  public void setSelectable(boolean isSelectable) {
    this.isSelectable = isSelectable;
  }

  public String getCurrentPagePath() {
    return currentPagePath;
  }

  public void setCurrentPagePath(String currentPagePath) {
    this.currentPagePath = currentPagePath;
  }  
}
