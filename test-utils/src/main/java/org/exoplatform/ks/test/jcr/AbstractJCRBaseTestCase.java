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

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public abstract class AbstractJCRBaseTestCase extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }
  
  /** . */
  private ManageableRepository repository;
  protected String rootConfigPath;
  protected String portalConfigPath;
  

  public void startJCR() throws Exception {

    // JCR configuration
    String containerConf = Thread.currentThread().getContextClassLoader().getResource("conf/portal/configuration.xml").toString();
    StandaloneContainer.addConfigurationURL(containerConf);

    //
    String loginConf = Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();
    System.setProperty("java.security.auth.login.config", loginConf);

    //
    StandaloneContainer container = StandaloneContainer.getInstance();
    RepositoryService repositoryService = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);

    repository = repositoryService.getDefaultRepository();
  }

  public ManageableRepository getRepository() {
    return repository;
  }

  public String getRepositoryName() {
    return "db1";
  }

  public String getWorkspaceName() {
    return "ws";
  }
  
  public void runBare() throws Throwable {
    ClassLoader realClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      ClassLoader testClassLoader = new TestClassLoader(realClassLoader, rootConfigPath, "conf/jcr/configuration.xml");
      Thread.currentThread().setContextClassLoader(testClassLoader);
      super.runBare();
      
    } finally {
      Thread.currentThread().setContextClassLoader(realClassLoader);
    }
  }
  


}
