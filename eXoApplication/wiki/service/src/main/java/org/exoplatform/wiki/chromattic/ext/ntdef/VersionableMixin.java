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
package org.exoplatform.wiki.chromattic.ext.ntdef;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 6, 2010  
 */
@MixinType(name = "mix:versionable")
public abstract class VersionableMixin {

  @OneToOne(type = RelationshipType.EMBEDDED)
  public abstract PageImpl getEntity();

  public abstract void setEntity(PageImpl page);

  @ManyToOne(type = RelationshipType.REFERENCE)
  @MappedBy("jcr:versionHistory")
  public abstract NTVersionHistory getVersionHistory();

  @ManyToOne(type = RelationshipType.REFERENCE)
  @MappedBy("jcr:baseVersion")
  public abstract NTVersion getBaseVersion();

  @Property(name = "jcr:isCheckedOut")
  public abstract boolean isCheckedOut();

  @Property(name = "jcr:predecessors")
  public abstract String[] getPredecessors();

}
