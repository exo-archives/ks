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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.xwiki.configuration.ConfigurationSource;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Oct 25, 2011  
 */
public class DefaultConfigurationSource implements ConfigurationSource {

  private Map<String, Object> properties = new HashMap<String, Object>();
  
  public DefaultConfigurationSource() {
    properties.put("rendering.macro.code.pygments.style", "perldoc");
  }

  @Override
  public <T> T getProperty(String key, T defaultValue) {
    return this.properties.containsKey(key) ? (T) this.properties.get(key) : defaultValue;
  }

  @Override
  public <T> T getProperty(String key, Class<T> valueClass) {
    T result = null;
    if (this.properties.containsKey(key)) {
      result = (T) this.properties.get(key);
    } else {
      if (List.class.getName().equals(valueClass.getName())) {
        result = (T) Collections.emptyList();
      } else if (Properties.class.getName().equals(valueClass.getName())) {
        result = (T) new Properties();
      }
    }
    return result;
  }

  @Override
  public <T> T getProperty(String key) {
    return (T) this.properties.get(key);
  }

  @Override
  public List<String> getKeys() {
    return new ArrayList<String>(this.properties.keySet());
  }

  @Override
  public boolean containsKey(String key) {
    return this.properties.containsKey(key);
  }

  @Override
  public boolean isEmpty() {
    return this.properties.isEmpty();
  }
}
