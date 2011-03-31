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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.rss;

import java.util.HashMap;
import java.util.Map;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class FeedResolver {
  private static Log                  LOG = ExoLogger.getLogger(FeedResolver.class);

  private FeedContentProvider         defaultProvider;

  private Map<String, String> providers;

  public FeedResolver() {
    providers = new HashMap<String, String>();
  }
  
  public FeedResolver(InitParams params) {

    try {
      String impl = params.getValueParam("defaultProvider").getValue();
      defaultProvider = instanciate(impl);
    } catch (Exception e) {
      LOG.error("failed to instanciate default provider", e);
    }
    
    PropertiesParam param = params.getPropertiesParam("providers");
    providers = param.getProperties();
  }

  @SuppressWarnings("unchecked")
  private FeedContentProvider instanciate(String impl) {
    try {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      Class<? extends FeedContentProvider> clazz = (Class<? extends FeedContentProvider>) cl.loadClass(impl);
      FeedContentProvider provider = clazz.newInstance();
      return provider;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public FeedContentProvider resolve(String appType) {
    String impl = providers.get(appType);
    if (impl == null) {
      return defaultProvider;
    }
    FeedContentProvider provider = instanciate(impl);
    return provider;
  }

  public FeedContentProvider getDefaultProvider() {
    return defaultProvider;
  }

  public void setDefaultProvider(FeedContentProvider defaultProvider) {
    this.defaultProvider = defaultProvider;
  }

  public Map<String, String> getProviders() {
    return providers;
  }

  public void setProviders(Map<String, String> providers) {
    this.providers = providers;
  }

}
