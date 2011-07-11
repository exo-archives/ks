/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.ks.common.image;

import java.io.Serializable;
import java.util.Collection;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfigPlugin;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 1 Jun 2011  
 */
public class MockCacheService implements CacheService {

  @Override
  public void addExoCacheConfig(ExoCacheConfigPlugin plugin) {
  }

  @Override
  public Collection<ExoCache<? extends Serializable, ?>> getAllCacheInstances() {
    return null;
  }

  @Override
  public <K extends Serializable, V> ExoCache<K, V> getCacheInstance(String region) throws NullPointerException,
                                                                                   IllegalArgumentException {
    return new ConcurrentFIFOExoCache<K, V>(1);
  }

}
