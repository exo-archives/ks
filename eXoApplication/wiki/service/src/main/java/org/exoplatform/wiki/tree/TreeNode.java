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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 10, 2010  
 */
public class TreeNode { 

  protected String         name;

  protected String         absPath;

  protected String         relPath;

  protected boolean        hasChild;

  protected TreeNodeType   nodeType;

  protected List<TreeNode> children = new ArrayList<TreeNode>();
  
  public TreeNode(String name, TreeNodeType nodeType) {
    this.name = name;
    this.nodeType = nodeType;
  }
  
  public TreeNodeType getNodeType() {
    return nodeType;
  }

  public void setNodeType(TreeNodeType nodeType) {
    this.nodeType = nodeType;
  }

  public boolean isHasChild() {
    return hasChild;
  }

  public void setHasChild(boolean hasChild) {
    this.hasChild = hasChild;
  }

  public List<TreeNode> getChildren() {
    return children;
  }

  public String getAbsPath() {
    return absPath;
  }

  public void setAbsPath(String absPath) {
    this.absPath = absPath;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getRelPath() {
    return relPath;
  }

}
