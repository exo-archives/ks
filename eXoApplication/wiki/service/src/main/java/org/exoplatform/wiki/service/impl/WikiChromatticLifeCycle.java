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

import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.SessionContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 10, 2010  
 */
public class WikiChromatticLifeCycle extends ChromatticLifeCycle {

  private MOWService mowService;
  
  private WikiService wService;
  
  private RenderingService renderingService;

  public void setMOWService(MOWService mowService) {
    this.mowService = mowService;
  }
  
  public void setWikiService(WikiService wService) {
    this.wService = wService;
  }
  
  public void setRenderingService(RenderingService renderingService) {
    this.renderingService = renderingService;
  }

  public WikiChromatticLifeCycle(InitParams params) {
    super(params);
  }

  @Override
  protected void onOpenSession(SessionContext context) {
    context.getSession().addEventListener(new Injector(mowService, wService, renderingService));
  }

}
