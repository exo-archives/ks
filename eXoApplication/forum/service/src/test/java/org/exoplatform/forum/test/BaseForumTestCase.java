/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.commons.logging.Log;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.test.BasicTestCase;


/**
 * Created by The eXo Platform SAS
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 					
 * july 3, 2007  
 */
public class BaseForumTestCase extends BasicTestCase {
  
	protected static Log          log = ExoLogger.getLogger("sample.services.test");  

  protected static RepositoryService   repositoryService;
  protected static StandaloneContainer container;
  
  protected final static String REPO_NAME = "repository".intern();
  protected final static String SYSTEM_WS = "system".intern();
  protected final static String COLLABORATION_WS = "collaboration".intern();
  protected static Node root_ = null;
  protected SessionProvider sProvider;
  private static SessionProviderService sessionProviderService = null;
  
  static {
    // we do this in static to save a few cycles
    initContainer();
    initJCR();
  }


  
  public BaseForumTestCase() throws Exception {    
  }
  
  public void testBaseTestCase() {
  	assertNotNull(repositoryService) ;
  	assertNotNull(container) ;
  }
  
  public void setUp() throws Exception {
    startSystemSession();
  }
  
  public void tearDown() throws Exception {  	
  }
  protected void startSystemSession() throws Exception {
  	sProvider = sessionProviderService.getSystemSessionProvider(null) ;
  }
  protected void startSessionAs(String user) throws Exception{
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sProvider = sessionProviderService.getSessionProvider(null);
  }
  protected void endSession() throws Exception{
    sessionProviderService.removeSessionProvider(null);
    startSystemSession();
  }
  
  
  /**
   * All elements of a list should be contained in the expected array of String
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertContainsAll(String message, List<String> expected, List<String> actual) {
    assertEquals(message, expected.size(), actual.size());
    assertTrue(message,expected.containsAll(actual));
  } 
  
  /**
   * Assertion method on string arrays
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String []expected, String []actual) {
    assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(message, expected[i], actual[i]);
    }
  }
  
  private static void initContainer() {
    try {
      String containerConf = BaseForumTestCase.class.getResource("/conf/portal/test-configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      container = StandaloneContainer.getInstance();      
      String loginConf = Thread.currentThread().getContextClassLoader().getResource("login.conf").toString();
      
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", loginConf);
    }    
    catch (Exception e) {
    	e.printStackTrace();
      throw new RuntimeException("Failed to initialize standalone container: " + e.getMessage(),e);
    }
  }

  private static void initJCR() {
    try {
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    
    // Initialize datas
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(COLLABORATION_WS);
    root_ = session.getRootNode();   
    sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: " + e.getMessage(),e);
    }
  }
}