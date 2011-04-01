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

import java.util.ArrayList;
import java.util.List;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.MixinType;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Property;
import org.exoplatform.wiki.mow.api.WikiNodeType;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 22 Dec 2010  
 */
@MixinType(name = WikiNodeType.WIKI_WATCHED)
public abstract class WatchedMixin {

  @OneToOne(type = RelationshipType.EMBEDDED)
  public abstract PageImpl getEntity();
  public abstract void setEntity(PageImpl page);

 @Property(name = WikiNodeType.Definition.WATCHER) 
 public abstract List<String> getWatchersByChromattic();
 public abstract void setWatchersByChromattic( List<String> watchers);
 
  public List<String> getWatchers() {
    List<String> watchers = getWatchersByChromattic();
    return (watchers != null) ? watchers : new ArrayList<String>();
  }

  public void setWatchers(List<String> watchers) {
    setWatchersByChromattic(watchers);
  }
 
}
