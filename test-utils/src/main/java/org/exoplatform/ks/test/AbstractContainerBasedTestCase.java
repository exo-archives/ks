/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.ks.test;

import java.lang.management.ManagementFactory;
import java.util.HashMap;

import junit.framework.TestCase;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.jmx.ManagementContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.ks.test.mock.SimpleMockOrganizationService;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.impl.CacheServiceImpl;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.security.IdentityRegistry;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractContainerBasedTestCase extends TestCase {

  protected SimpleMockOrganizationService organizationService = null;
  protected CacheService cacheService = null;
  protected IdentityRegistry identityRegistry = null;
  
  
  public AbstractContainerBasedTestCase() throws Exception {
    cacheService = initCacheService();
    identityRegistry = initIdentityRegistry();
    organizationService =  new SimpleMockOrganizationService();
  }
  
  /**
   * Initializes a new ExoContainer bound to the platform MBseanServer. 
   * Calls {@link #registerComponents(ExoContainer)}, then {@link #doSetUp()}.
   */
  public final void setUp() {
    System.out.println(">>>>>>" + System.getProperty("maven.exoplatform.dir"));
    ExoContainer testContainer = new ExoContainer(new ManagementContextImpl(ManagementFactory.getPlatformMBeanServer(), new HashMap<String,String>()));
    registerDefaultComponents(testContainer);
    registerComponents(testContainer);
    ExoContainerContext.setCurrentContainer(testContainer);
    doSetUp();
  }

  private void registerDefaultComponents(ExoContainer testContainer) {
    testContainer.registerComponentInstance(OrganizationService.class, organizationService);
    testContainer.registerComponentInstance(IdentityRegistry.class, identityRegistry);
    testContainer.registerComponentInstance(CacheService.class, cacheService);
  }

  /**
   * 
   * @param testContainer
   */
  protected abstract void registerComponents(ExoContainer testContainer) ;
  
  /**
   * Do be overriden by subclasses that want to do smth additional in setUp() after {@link #registerComponents(ExoContainer)}
   */
  protected void doSetUp() {
    
  }
  
  
  protected CacheService initCacheService() throws Exception {
    InitParams cacheParams = new InitParams();
    ObjectParameter oparam = new ObjectParameter();
    ExoCacheConfig config = new ExoCacheConfig();
    oparam.setName("cache.config.default");
    config.setName("default");
    config.setMaxSize(30);
    config.setLiveTime(300);
    config.setDistributed(false);
    config.setImplementation("org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache"); 
    oparam.setObject(config);
    cacheParams.addParameter(oparam);
    return new CacheServiceImpl(cacheParams);
  }
  
  
  protected IdentityRegistry initIdentityRegistry() {
    return new IdentityRegistry(null);
  }
  
}
