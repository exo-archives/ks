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

import java.util.Iterator;

import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Aug 6, 2010  
 */
public class SpaceTreeNode extends TreeNode {
  
  public SpaceTreeNode(String name) throws Exception {
    super(name,TreeNodeType.SPACE);   
    this.absPath= name;
    this.relPath= this.absPath;
    this.hasChild = Utils.getWikisByType(WikiType.valueOf(name.toUpperCase())).size()>0;    
  }
  
  public void setChildren() throws Exception
  {
    Iterator<Wiki> childWikiIterator = Utils.getWikisByType(WikiType.valueOf(name.toUpperCase()))
                                            .iterator();
    while (childWikiIterator.hasNext()) {
      WikiTreeNode child = new WikiTreeNode(childWikiIterator.next());
      this.children.add(child);
    }
  }
  
  
  public WikiTreeNode getChildByName(String name) throws Exception {
    for (TreeNode child : children) {
      if (child.getName().equals(name))
        return (WikiTreeNode)child;
    }
    return null;
  }
}
