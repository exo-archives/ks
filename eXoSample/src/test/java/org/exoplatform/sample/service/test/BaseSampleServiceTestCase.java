/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.sample.service.test;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.services.log.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SAS
 * @author : Hung nguyen
 *          hung.nguyen@exoplatform.com
 * May 7, 2008  
 */
public abstract class BaseSampleServiceTestCase extends BasicTestCase {

  protected static Log          log = ExoLogger.getLogger("sample.services.test");  
  protected RepositoryService   repositoryService;
  protected StandaloneContainer container;
  
  protected final String REPO_NAME = "repository".intern();
  protected final String SYSTEM_WS = "system".intern();
  protected final String COLLABORATION_WS = "collaboration".intern();
  protected Node root_ ;

  public void setUp() throws Exception {
    String containerConf = getClass().getResource("/conf/portal/test-configuration.xml").toString();
    String loginConf = Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();

    StandaloneContainer.addConfigurationURL(containerConf);
    container = StandaloneContainer.getInstance();
    
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);

    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    
    // Initialize datas
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    root_ = session.getRootNode();
  }
  
  public void tearDown() throws Exception {
  	//Remove datas  	
  }
}
