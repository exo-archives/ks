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


import java.util.HashMap;

import junit.framework.TestCase;

import org.exoplatform.ks.test.mock.MockResourceBundle;
import org.exoplatform.ks.test.mock.MockWebUIRequestContext;
import org.exoplatform.ks.test.mock.MockWebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractWebuiTestCase extends TestCase {

  
  private MockWebuiApplication mockApp;


  public final void setUp() throws Exception {
    
    mockApp = new MockWebuiApplication();
    mockApp.setResourceBundle(new MockResourceBundle(new HashMap<String, Object>()));
    MockWebUIRequestContext context = new MockWebUIRequestContext(mockApp);

    context.setParentAppRequestContext(new MockParentRequestContext(null)); // a webuirequestcotnext requires a parent...

    WebuiRequestContext.setCurrentInstance(context);
    
    doSetUp();
  }
  
  protected void doSetUp() {
    // to be overriden
  }
  

  
  
  /**
   * Convenience method to access the app resource bundle mock
   * @return
   */
  protected MockResourceBundle getAppRes() {
    try {
      return (MockResourceBundle) mockApp.getResourceBundle(null);
    } catch (Exception e) {
      fail(e.getMessage());
    }
    return null;
  }
  
  

  
}
