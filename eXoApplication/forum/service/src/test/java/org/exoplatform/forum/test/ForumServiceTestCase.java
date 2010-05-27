/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.test;

import java.lang.reflect.Field;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.test.BasicTestCase;


/**
 * Created by The eXo Platform SAS
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com 					
 * july 3, 2007  
 */
public abstract class ForumServiceTestCase extends AbstractExoContainerTestCase {
   
  protected static Log          log = ExoLogger.getLogger("sample.services.test");  

  protected static RepositoryService   repositoryService;
  protected static ExoContainer container;
  
  protected final static String REPO_NAME = "repository".intern();
  protected final static String SYSTEM_WS = "system".intern();
  protected final static String KNOWLEDGE_WS = "knowledge".intern();
  protected static Node root_ = null;
  protected SessionProvider sProvider;
  private static SessionProviderService sessionProviderService = null;
  
  static {
    // we do this in static to save a few cycles
    initContainer();
    initJCR();
  }


  
  public ForumServiceTestCase() throws Exception {    
  }
  
  public void setUp() throws Exception {
    startSystemSession();
  }
  
  public void tearDown() throws Exception {

  }
  protected void startSystemSession() {
    sProvider = sessionProviderService.getSystemSessionProvider(null) ;
  }
  protected void startSessionAs(String user) {
    Identity identity = new Identity(user);
    ConversationState state = new ConversationState(identity);
    sessionProviderService.setSessionProvider(null, new SessionProvider(state));
    sProvider = sessionProviderService.getSessionProvider(null);
  }
  protected void endSession() {
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
   // Must clear the top container first otherwise it's not going to work well
      // it's a big ugly but I don't want to change anything in the ExoContainerContext class for now
      // and this is for unit testing
      Field topContainerField = ExoContainerContext.class.getDeclaredField("topContainer");
      topContainerField.setAccessible(true);
      topContainerField.set(null, null);

      // Same remark than above
      Field singletonField = RootContainer.class.getDeclaredField("singleton_");
      singletonField.setAccessible(true);
      singletonField.set(null, null);
      
      // needed otherwise, we cannot call this method twice in the same thread
      Field bootingField = RootContainer.class.getDeclaredField("booting");
      bootingField.setAccessible(true);
      bootingField.set(null, false);
      RootContainer.setInstance(null);
      
      PortalContainer.setInstance(null);
      
      ExoContainerContext.setCurrentContainer(null);
      
      String containerConf = ForumServiceTestCase.class.getClassLoader().getResource("conf/portal/test-configuration.xml").toString();
      StandaloneContainer.addConfigurationURL(containerConf);
      container = StandaloneContainer.getInstance();      
      
      String loginConf = Thread.currentThread().getContextClassLoader().getResource("conf/portal/login.conf").toString();
      
      if (System.getProperty("java.security.auth.login.config") == null)
        System.setProperty("java.security.auth.login.config", loginConf);
    }
    catch (Exception e) {
      log.error("Failed to initialize standalone container: ",e);
    }
  }

  private static void initJCR() {
    try {
    repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    
    // Initialize datas
    Session session = repositoryService.getRepository(REPO_NAME).getSystemSession(KNOWLEDGE_WS);
    root_ = session.getRootNode();   
    sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;   
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to initialize JCR: ",e);
    }
  }
}