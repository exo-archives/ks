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

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.content.Link;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@PrimaryType(name = "wiki:link")
public abstract class WikiLink extends AbstractContentItem implements Link {

  @Property(name = "alias")
  public abstract String getAlias();

  @Property(name = "target")
  public abstract String getTarget();

  @ManyToOne(type = RelationshipType.REFERENCE)
  @MappedBy("target")
  public abstract PageImpl getTargetPage();

  @Property(name = "text")
  public abstract String getText();

}
