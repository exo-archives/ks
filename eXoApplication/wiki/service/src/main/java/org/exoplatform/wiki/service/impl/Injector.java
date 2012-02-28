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

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.event.LifeCycleListener;
import org.chromattic.api.event.StateChangeListener;
import org.chromattic.ext.ntdef.NTResource;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTFrozenNode;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersionHistory;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.WatchedMixin;
import org.exoplatform.wiki.mow.core.api.wiki.WikiContainer;
import org.exoplatform.wiki.rendering.RenderingService;
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
  
  private final RenderingService renderingService;
  
  private static final Log log = ExoLogger.getLogger("org.exoplatform.wiki.service.impl.Injector");

  public Injector(MOWService mowService,WikiService wService, RenderingService renderingService) {
    this.mowService = mowService;
    this.wService = wService;
    this.renderingService = renderingService;
  }

  @Override
  public void added(String id, String path, String name, Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
      ((PageImpl) o).setComponentManager(renderingService.getComponentManager());
    }
    if(o instanceof WikiContainer<?>) {
      ((WikiContainer<?>) o).setwService(wService);
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
      ((PageImpl) o).setComponentManager(renderingService.getComponentManager());
    }
    if(o instanceof WikiContainer<?>) {
      ((WikiContainer<?>) o).setwService(wService);
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
      ((PageImpl) o).setComponentManager(renderingService.getComponentManager());
    }
    if(o instanceof WikiContainer<?>) {
      ((WikiContainer<?>) o).setwService(wService);
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
      ((PageImpl) o).setComponentManager(renderingService.getComponentManager());
    }
    if (o instanceof NTFrozenNode) {
      ((NTFrozenNode) o).setMOWService(mowService);
    }
  }

  @Override
  public void propertyChanged(String id, Object o, String propertyName, Object propertyValue) {

    if (o instanceof NTResource) {
      if ("jcr:data".equals(propertyName)) {
        ChromatticSession session = mowService.getSession();
        String path = session.getPath(o);
        if (path.endsWith(WikiNodeType.Definition.CONTENT)) {
          path = path.substring(0, path.lastIndexOf("/"));
          if (path.startsWith("/")) {
            path = path.substring(1);
          }
          AttachmentImpl content = session.findByPath(AttachmentImpl.class, path);
          try {
            PageImpl page = content.getParentPage();
            WatchedMixin mixin = page.getWatchedMixin();
            if (mixin != null) {
              boolean isWatched = !mixin.getWatchers().isEmpty();
              Wiki wiki = page.getWiki();
              boolean isMinorEdit = page.isMinorEdit();
              if (wiki != null && isWatched && !isMinorEdit) {
                NTVersionHistory history = page.getVersionableMixin().getVersionHistory();
                if (history != null && history.getChildren().size() > 1) {
                  Utils.sendMailOnChangeContent(content);
                }
              }
            }
          } catch (Exception e) {
            log.warn("Can not notify wiki page changes by email !", e);
          }
        }
      }
    }
  }
}
