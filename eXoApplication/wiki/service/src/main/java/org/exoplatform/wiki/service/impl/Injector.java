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
package org.exoplatform.wiki.service.impl;

import org.chromattic.api.event.LifeCycleListener;
import org.chromattic.api.event.StateChangeListener;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTFrozenNode;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersionHistory;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WatchedMixin;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 10, 2010  
 */
public class Injector implements LifeCycleListener, StateChangeListener {
  
  private final MOWService mowService;

  private final WikiService wService;

  public Injector(MOWService mowService,WikiService wService ) {
    this.mowService = mowService;
    this.wService = wService;
  }

  @Override
  public void added(String id, String path, String name, Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
    }
    if (o instanceof NTFrozenNode) {
      ((NTFrozenNode) o).setMOWService(mowService);
    }
  }

  @Override
  public void created(Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
    }
    if (o instanceof NTFrozenNode) {
      ((NTFrozenNode) o).setMOWService(mowService);
    }
  }

  @Override
  public void loaded(String id, String path, String name, Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
    }
    if (o instanceof NTFrozenNode) {
      ((NTFrozenNode) o).setMOWService(mowService);
    }
  }

  @Override
  public void removed(String id, String path, String name, Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
    }
    if (o instanceof NTFrozenNode) {
      ((NTFrozenNode) o).setMOWService(mowService);
    }
  }

  @Override
  public void propertyChanged(String id, Object o, String propertyName, Object propertyValue) {
    // TODO Auto-generated method stub
    if (o instanceof ContentImpl) {
      if (propertyName.equals(WikiNodeType.Definition.TEXT.toString())) {
        ContentImpl content = (ContentImpl) o;
        try {
          PageImpl page = content.getParent();
          WatchedMixin mixin = page.getWatchedMixin();
          if (mixin != null) {
            boolean isWatched = !mixin.getWatchers().isEmpty();
            Wiki wiki = page.getWiki();
            if (wiki != null && isWatched) {
              NTVersionHistory history = page.getVersionableMixin().getVersionHistory();
              if (history != null && history.getChildren().size() > 1) {
                Utils.sendMailOnChangeContent(content);
              }
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }
 
}
