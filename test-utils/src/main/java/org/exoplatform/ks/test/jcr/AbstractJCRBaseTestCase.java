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
package org.exoplatform.ks.test.jcr;

import junit.framework.TestCase;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract test that takes care of running the unit tests with the semantic described by the
 * {#link GateInTestClassLoader}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractJCRBaseTestCase extends TestCase
{

   protected AbstractJCRBaseTestCase()
   {
   }

   protected AbstractJCRBaseTestCase(String name)
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

      //
      try
      {
         ClassLoader testClassLoader = new TestClassLoader(realClassLoader, rootConfigPaths, portalConfigPaths);
         Thread.currentThread().setContextClassLoader(testClassLoader);
         super.runBare();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(realClassLoader);
      }
   }
}
