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
package org.exoplatform.wiki.rendering.impl;

import java.util.List;
import java.util.Properties;

import org.xwiki.configuration.ConfigurationSource;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Oct 25, 2011  
 */
public class DefaultConfigurationSource implements ConfigurationSource {

  @Override
  public <T> T getProperty(String key, T defaultValue) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T getProperty(String key, Class<T> valueClass) {
    return (T) new Properties();
  }

  @Override
  public <T> T getProperty(String key) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<String> getKeys() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean containsKey(String key) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isEmpty() {
    // TODO Auto-generated method stub
    return false;
  }

}
