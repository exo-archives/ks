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
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 10, 2010  
 */
public class Injector implements LifeCycleListener {

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
  }

  @Override
  public void created(Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
    }
  }

  @Override
  public void loaded(String id, String path, String name, Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
    }
  }

  @Override
  public void removed(String id, String path, String name, Object o) {
    if (o instanceof PageImpl) {
      ((PageImpl) o).setMOWService(mowService);
      ((PageImpl) o).setWikiService(wService);
    }
  }

}
