/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.mow.core.api.content;

import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.content.Content;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@PrimaryType(name = WikiNodeType.WIKI_PAGE_CONTENT)
public abstract class ContentImpl extends AbstractContainerContent implements Content {
  
  @Path
  public abstract String getPath();
  
  @Property(name = WikiNodeType.Definition.TITLE)
  public abstract String getTitle();
  public abstract void setTitle(String title);
  
  @Property(name = WikiNodeType.Definition.SYNTAX)
  public abstract String getSyntax();
  public abstract void setSyntax(String syntax);
  
  @Property(name = WikiNodeType.Definition.TEXT)
  public abstract String getText();
  public abstract void setText(String text);
  
  
  @OneToOne
  @MappedBy(WikiNodeType.Definition.CONTENT)
  public abstract PageImpl getParent();

}
