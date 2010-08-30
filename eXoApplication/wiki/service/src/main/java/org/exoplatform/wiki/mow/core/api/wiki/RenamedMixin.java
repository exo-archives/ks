/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.exoplatform.wiki.mow.core.api.wiki;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Property;

/**
 * @author <a href="mailto:</a>
 * @version $Revision$
 */
@MixinType(name = "wiki:renamed")
public abstract class RenamedMixin {

  @OneToOne(type = RelationshipType.EMBEDDED)
  public abstract PageImpl getEntity();
  public abstract void setEntity(PageImpl page);

  @Property(name = "oldPageIds")
  public abstract void setOldPageIds(String[] ids);
  public abstract String[] getOldPageIds();  
  
}
