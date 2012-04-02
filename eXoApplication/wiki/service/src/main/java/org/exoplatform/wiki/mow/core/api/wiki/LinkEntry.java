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
package org.exoplatform.wiki.mow.core.api.wiki;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 11, 2010  
 */
@PrimaryType(name = WikiNodeType.LINK_ENTRY)
public abstract class LinkEntry {
  
  @Property(name = WikiNodeType.Definition.ALIAS)
  public abstract String getAlias();
  public abstract void setAlias(String alias);

  @Property(name = WikiNodeType.Definition.TITLE)
  public abstract String getTitle();
  public abstract void setTitle(String title);
  
  @ManyToOne(type = RelationshipType.PATH)
  @MappedBy(WikiNodeType.Definition.NEW_LINK)
  public abstract LinkEntry getNewLink();
  public abstract void setNewLink(LinkEntry linkEntry);
  
  @Destroy
  public abstract void remove();

}
