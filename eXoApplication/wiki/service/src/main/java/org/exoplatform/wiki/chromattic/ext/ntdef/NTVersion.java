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

import java.util.Date;

import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jul 6, 2010  
 */
@PrimaryType(name = "nt:version")
public abstract class NTVersion {

  @Name
  public abstract String getName();
  
  @Property(name = "jcr:created")
  public abstract Date getCreated();

  public abstract void setCreated(Date date);

  @Property(name = "jcr:predecessors")
  public abstract String[] getPredecessors();

  @Property(name = "jcr:successors")
  public abstract String[] getSuccessors();

  @OneToOne
  @Owner
  @MappedBy("jcr:frozenNode")
  public abstract NTFrozenNode getNTFrozenNode();

  public abstract void setNTFrozenNode(NTFrozenNode frozenNode);

}
