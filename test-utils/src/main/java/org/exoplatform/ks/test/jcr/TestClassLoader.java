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

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class TestClassLoader extends ClassLoader {

  private String rootConfigPath;

  private String portalConfigPath;

  public TestClassLoader(ClassLoader realClassLoader, String rootConfigPath, String portalConfigPath) {
    super(realClassLoader);
    this.rootConfigPath = rootConfigPath;
    this.portalConfigPath = portalConfigPath;
  }

  public Enumeration<URL> getResources(String name) throws IOException {
    System.out.println("name = " + name);
    if ("conf/configuration.xml".equals(name)) {
      if (rootConfigPath != null) {
        return super.getResources(rootConfigPath);
      } else {
        return Collections.enumeration(Collections.<URL> emptyList());
      }
    } else if ("conf/portal/configuration.xml".equals(name)) {
      if (portalConfigPath != null) {
        return super.getResources(portalConfigPath);
      } else {
        return Collections.enumeration(Collections.<URL> emptyList());
      }
    } else if ("conf/portal/test-configuration.xml".equals(name)) {
      return new Vector<URL>().elements();
    } else {
      return super.getResources(name);
    }

  }
}
