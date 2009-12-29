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

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;


/**
 * A base test class that allows to load an exo container with a selected set of components.
 * It is aimed at running lighter test cases than the StandaloneContainer.
 * <u>Example usage</u> :  
 * <pre>@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.ROOT, path = "conf/custom.xml"),@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/some.xml"),  @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/other.xml")})</pre>
 * TODO : leverage GateIn testing framework. This is a temporary fork of GateIn testing base classes.
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class AbstractExoContainerTestCase extends TestCase {

  protected AbstractExoContainerTestCase()
  {
  }

  protected AbstractExoContainerTestCase(String name)
  {
     super(name);
  }

  @Override
  public void runBare() throws Throwable
  {
     ClassLoader realClassLoader = Thread.currentThread().getContextClassLoader();
     


     //
     Set<String> rootConfigPaths = new HashSet<String>();
     rootConfigPaths.add("conf/root-configuration.xml");

     //
     Set<String> portalConfigPaths = new HashSet<String>();
     portalConfigPaths.add("conf/portal-configuration.xml");

     //
     EnumMap<ContainerScope, Set<String>> configs = new EnumMap<ContainerScope, Set<String>>(ContainerScope.class);
     configs.put(ContainerScope.ROOT, rootConfigPaths);
     configs.put(ContainerScope.PORTAL, portalConfigPaths);

     //
     ConfiguredBy cfBy = getClass().getAnnotation(ConfiguredBy.class);
     if (cfBy != null)
     {
        for (ConfigurationUnit src : cfBy.value())
        {
           configs.get(src.scope()).add(src.path());
        }
     }

     ContainerBuilder builder = new ContainerBuilder().withLoader(realClassLoader);
     
     Set<String> rootConfs = configs.get(ContainerScope.ROOT);
     for (String rootConf : rootConfs) {
      builder.withRoot(rootConf);
     }
     
     Set<String> portalConfs = configs.get(ContainerScope.PORTAL);
     for (String portalConf : portalConfs) {
      builder.withPortal(portalConf);
     }

  }
  
  /**
   * Register a component to the containter
   */
  protected <T, I extends T>void registerComponent(Class<T> clazz, I impl) {
    ExoContainerContext.getCurrentContainer().registerComponentImplementation(impl, clazz);
  }
  
  /**
   * Get a component from current container
   * @param <T> type of component (key)
   * @param <U> type of component implementation (type)
   * @param clazz class of the registered component
   * @return
   */
  @SuppressWarnings("unchecked")
  protected <T,U extends T>U getComponent(Class<T> clazz) {
    //ExoContainer container = ExoContainerContext.getCurrentContainer();
    ExoContainer container = PortalContainer.getInstance();
    return (U) container.getComponentInstanceOfType(clazz);
  }
  
}
