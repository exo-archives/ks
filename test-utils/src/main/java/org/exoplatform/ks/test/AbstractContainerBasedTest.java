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

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractContainerBasedTest extends TestCase {

  
  /**
   * Initializes a new ExoContainer and calls {@link #registerComponents(ExoContainer)}, then {@link #doSetUp()}.
   */
  public final void setUp() {
    ExoContainer testContainer = new ExoContainer(new ManagementContextImpl(ManagementFactory.getPlatformMBeanServer(), new HashMap<String,String>()));
    registerComponents(testContainer);
    ExoContainerContext.setCurrentContainer(testContainer);
    doSetUp();
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
  
}
