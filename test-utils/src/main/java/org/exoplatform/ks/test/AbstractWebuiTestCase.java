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


import org.exoplatform.ks.test.mock.MockWebUIRequestContext;
import org.exoplatform.ks.test.mock.MockWebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractWebuiTestCase extends TestCase {

  
  public final void setUp() throws Exception {
    
    // initializes webui context
    WebuiRequestContext.setCurrentInstance(new MockWebUIRequestContext(new MockWebuiApplication()));
    
    doSetUp();
  }
  
  protected void doSetUp() {
    
  }
  
}
