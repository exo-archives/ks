/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import javax.jcr.Node;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.test.BasicTestCase;


/**
 * Created by The eXo Platform SAS
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 					
 * july 3, 2007  
 */
public class BaseForumTestCase extends BasicTestCase {
  
  final protected static String NT_UNSTRUCTURED = "nt:unstructured".intern() ;
  final protected static String NT_FOLDER = "nt:folder".intern() ;
  final protected static String NT_FILE = "nt:file".intern() ;    
  final protected static String ADMIN = "admin".intern() ;
    
  final protected static String DEFAULT_WS = "production".intern() ;
  final protected static String MAIL_HOME = "mailHome".intern() ;
  
  protected static Log          log = ExoLogger.getLogger("sample.services.test");  
  protected RepositoryService   repositoryService;
  protected StandaloneContainer container;
  
  protected final String REPO_NAME = "repository".intern();
  protected final String SYSTEM_WS = "system".intern();
  protected final String COLLABORATION_WS = "collaboration".intern();
  protected Node root_ ;
  
  protected ForumService forumService_ ;
  protected SessionProvider sProvider_ ;
  public void setUp() throws Exception{
    
  	String containerConf = getClass().getResource("/conf/portal/test-configuration.xml").toString();
    String loginConf = Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();

    StandaloneContainer.addConfigurationURL(containerConf);
    container = StandaloneContainer.getInstance();
    
    if (System.getProperty("java.security.auth.login.config") == null)
      System.setProperty("java.security.auth.login.config", loginConf);

    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    
    // Initialize datas
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
    
    forumService_ = (ForumService) container.getComponentInstanceOfType(ForumService.class) ;

    //    String defaultWS = repositoryService.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
//    Session session = sProvider_.getSession(defaultWS, repositoryService.getCurrentRepository()) ; 
    	
    //Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    //root_ = session.getRootNode();
  }
	  
  
  public void tearDown() throws Exception {
  	//Remove datas  	
  }
}