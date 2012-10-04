/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bbcode.core.cache;

import java.util.List;

import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.bbcode.spi.BBCodePlugin;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SAS
 * Author : thanh_vucong
 *          thanh_vucong@exoplatform.com
 * Oct 4, 2012  
 */
public class CachedBBCodeService implements Startable, BBCodeService {

  private static Log LOG = ExoLogger.getLogger(CachedBBCodeService.class);
  private CacheService service;
  private BBCodeService bbCodeService;
  
  
  @Override
  public void registerBBCodePlugin(BBCodePlugin plugin) throws Exception {
    
  }

  @Override
  public void save(List<BBCode> bbcodes) throws Exception {

    
  }

  @Override
  public List<BBCode> getAll() throws Exception {

    return null;
  }

  @Override
  public List<String> getActive() throws Exception {
    return null;
  }

  @Override
  public BBCode findById(String bbcodeId) throws Exception {
    return null;
  }

  @Override
  public void delete(String bbcodeId) throws Exception {
    
  }

  @Override
  public void start() {
    
  }

  @Override
  public void stop() {
    
    
  }

}
