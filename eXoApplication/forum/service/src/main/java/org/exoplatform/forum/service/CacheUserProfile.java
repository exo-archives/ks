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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.service;

import java.io.Serializable;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Sep 15, 2011  
 */
public class CacheUserProfile {

  private static ExoCache<Serializable, UserProfile> getCache() throws Exception {
    CacheService cacheService = (CacheService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(CacheService.class);
    return cacheService.getCacheInstance("org.exoplatform.forum.UserProfiles");
  }

  /**
   * Store the UserProfile of the user online storage in cache
   * @param userName
   * @param userProfile
   * @throws Exception
   */
  public static void storeInCache(String userName, UserProfile userProfile) throws Exception {
    ExoCache<Serializable, UserProfile> cache = getCache();
    Serializable cacheKey = getCacheKey(userName);
    cache.put(cacheKey, userProfile);
  }

  /**
   * Remove the UserProfile of the user online storage in cache
   * @param userName
   * @throws Exception
   */
  public static void removeInCache(String userName) throws Exception {
    ExoCache<Serializable, UserProfile> cache = getCache();
    Serializable cacheKey = getCacheKey(userName);
    cache.remove(cacheKey);
  }

  /**
   * Remove all UserProfile storage in cache
   * @throws Exception
   */
  public static void clearCache() throws Exception {
    ExoCache<Serializable, UserProfile> cache = getCache();
    cache.clearCache();
  }

  /**
   * Load a UserProfile of the user online from expressions in cache
   * @param userName
   * @return UserProfile
   * @throws Exception
   */
  public static UserProfile getFromCache(String userName) throws Exception {
    if (Utils.isEmpty(userName) || UserProfile.USER_GUEST.equals(userName))
      return null;
    ExoCache<Serializable, UserProfile> cache = getCache();
    Serializable cacheKey = getCacheKey(userName);
    return cache.get(cacheKey);
  }

  private static Serializable getCacheKey(String userName) {
    return userName;
  }
}
