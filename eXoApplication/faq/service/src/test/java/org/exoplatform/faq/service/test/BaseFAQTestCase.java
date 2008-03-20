/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.faq.service.test;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.log.LogService;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.test.BasicTestCase;


/**
 * Created by The eXo Platform SAS
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 					
 * july 3, 2007  
 */
public class BaseFAQTestCase extends BasicTestCase {
  
  final protected static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final protected static String NT_FOLDER = "nt:folder".intern() ;
  final protected static String NT_FILE = "nt:file".intern() ;    
  final protected static String ADMIN = "admin".intern() ;
    
  final protected static String DEFAULT_WS = "ws".intern() ;
  final protected static String MAIL_HOME = "mailHome".intern() ;
  
  protected Node rootNode_;
  protected Node systemNode_;
  protected ManageableRepository repository_;
  protected SimpleCredentials credentials_;
  protected PortalContainer manager_;  
  protected OrganizationService orgService_;  
  protected Session session_ ;  
  protected FAQService faqService_ ;
  public void setUp() throws Exception{
    
    LogService logService = 
      (LogService) RootContainer.getInstance().getComponentInstanceOfType(LogService.class); 

    logService.setLogLevel("org.exoplatform.services.jcr", LogService.DEBUG, true);     
    
    manager_ = PortalContainer.getInstance() ;
    if(System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", "src/main/login.conf" );

    credentials_ = new SimpleCredentials("exo", "exo".toCharArray());

    RepositoryService repositoryService = 
      (RepositoryService) manager_.getComponentInstanceOfType(RepositoryService.class);
        
    repository_ = repositoryService.getDefaultRepository();
    faqService_ = (FAQService)manager_.getComponentInstanceOfType(FAQService.class) ;
    
    session_ = repository_.getSystemSession(DEFAULT_WS) ;   
    rootNode_ = session_.getRootNode(); 
    
  }
}