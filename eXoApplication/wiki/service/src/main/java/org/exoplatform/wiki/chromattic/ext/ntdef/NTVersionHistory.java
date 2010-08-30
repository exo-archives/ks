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

import java.util.Iterator;
import java.util.Map;

import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToMany;
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
@PrimaryType(name = "nt:versionHistory")
public abstract class NTVersionHistory implements Iterable<NTVersion> {

  @Property(name = "jcr:versionableUuid")
  public abstract String getVersionableUuid();

  public abstract void setVersionableUuid(String versionableUuid);

  @OneToOne
  @Owner
  @MappedBy("jcr:rootVersion")
  public abstract NTVersion getRootVersion();

  public abstract void setRootVersion(NTVersion rootVersion);

  @OneToOne
  @Owner
  @MappedBy("jcr:versionLabels")
  public abstract NTVersionLabels getVersionLabels();

  public abstract void setVersionLabels(NTVersionLabels versionLabels);

  @OneToMany
  public abstract Map<String, NTVersion> getChildren();

  public Iterator<NTVersion> iterator() {
    return getChildren().values().iterator();
  }
  
  public NTVersion getVersion(String versionName) {
    return getChildren().get(versionName);
  }

}
