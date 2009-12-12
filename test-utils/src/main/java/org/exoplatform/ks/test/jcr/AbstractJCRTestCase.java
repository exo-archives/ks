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

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * An abstract test that takes care of running the unit tests with the semantic described by the
 * {#link GateInTestClassLoader}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractJCRTestCase extends TestCase
{

   protected AbstractJCRTestCase()
   {
   }

   protected AbstractJCRTestCase(String name)
   {
      super(name);
   }

   @Override
   public void runBare() throws Throwable
   {
      ClassLoader realClassLoader = Thread.currentThread().getContextClassLoader();

      //
      Set<String> rootConfigPaths = new HashSet<String>();
      rootConfigPaths.add("conf/root-configuration.xml");

      //
      Set<String> portalConfigPaths = new HashSet<String>();
      portalConfigPaths.add("conf/portal-configuration.xml");

      //
      EnumMap<ContainerScope, Set<String>> configs = new EnumMap<ContainerScope, Set<String>>(ContainerScope.class);
      configs.put(ContainerScope.ROOT, rootConfigPaths);
      configs.put(ContainerScope.PORTAL, portalConfigPaths);

      //
      ConfiguredBy cfBy = getClass().getAnnotation(ConfiguredBy.class);
      if (cfBy != null)
      {
         for (ConfigurationUnit src : cfBy.value())
         {
            configs.get(src.scope()).add(src.path());
         }
      }

      //
      try
      {
         ClassLoader testClassLoader = new TestClassLoader(realClassLoader, rootConfigPaths, portalConfigPaths);
         Thread.currentThread().setContextClassLoader(testClassLoader);
         super.runBare();
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(realClassLoader);
      }
   }
   
   /**
    * 
    * @return The ManageableRepository for this test
    */
   ManageableRepository getRepo() {
     try {
       PortalContainer container = PortalContainer.getInstance();
       RepositoryService repos = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
       ManageableRepository repo = repos.getDefaultRepository();
       return repo;
     } catch (Exception e) {
       throw new RuntimeException(e);
     }
   }

   /**
    * Get the workspace available for test data
    * @return workspace name
    */
   protected String getWorkspace() {
     return getRepo().getConfiguration().getDefaultWorkspaceName();
   }

   /**
    * Get the repository for the current test
    * @return repository name
    */
   protected String getRepository() {
     return getRepo().getConfiguration().getName();
   }

   /**
    * Asserts a node exists at the given path
    * @param path path relative to root of test workspace
    */
   protected void assertNodeExists(String path) {
     Session session = null;
     try {
       session = getSession();
       boolean exists = session.getRootNode().hasNode(path);
       if (!exists) {
         fail("no node exists at " + path);
       }
     } catch (Exception e) {
       throw new RuntimeException("failed to assert node exists", e);
     }
     finally {
       if (session != null) {
         session.logout();
       }
     }
   }
   
   /**
    * Asserts a node does not exists at the given path
    * @param path relative path to root of test workspace
    */
   protected void assertNodeNotExists(String path) {
     Session session = null;
     try {
       session = getSession();
       boolean exists = session.getRootNode().hasNode(path);
       if (exists) {
         fail("node exists at " + path);
       }
     } catch (Exception e) {
       throw new RuntimeException("failed to assert node exists", e);
     }
     finally {
       if (session != null) {
         session.logout();
       }
     }
   }

   /**
    * Get a session on the test workspace
    * @return a new system session
    * @throws Exception
    */
   protected Session getSession() throws Exception {
     try {
       Session session = getRepo().getSystemSession(getWorkspace());
       return session;
     } catch (Exception e) {
       throw new RuntimeException("failed to initiate JCR session on " + getWorkspace(), e);
     }     

   }
   
   /**
    * Add a new node to a given path. intermediary are created if needed.
    * @param path relative path to root
    * @return the newly added node
    */
   protected Node addNode(String path) {
     return addNode(path, null);
   }

   /**
    * Add a new node to a given path. intermediary are created if needed.
    * @param path relative path to root
    * @param nodetype nodetype for the last node to create 
    * @return the newly added node
    */
   protected Node addNode(String path, String nodetype) {
     Session session = null;
     try {
       session = getSession();
       Node parent = session.getRootNode();
       String[] sections = path.split("/");
       for (String section : sections) {
        if (section.length() > 0 && !parent.hasNode(section)) {
          if (nodetype != null && path.endsWith(section)) {
            parent.addNode(section, nodetype); // add child 
          } else {
            parent.addNode(section);
          }
        }
        parent = parent.getNode(section); // jump into
       }
     session.save();
     return parent;
     }
     catch (Exception e) {
       throw new RuntimeException("failed to add node" + path, e);
     }
     finally {
       if (session != null) {
         session.logout();
       }
     }
   }
   
   /**
    * Add a file at a given path. An nt:file is added at 'path' with a sample text/plain jcr:content for the string "stuff"
    * @param path relative path to root
    * @return the node for the newly added file
    */
   protected Node addFile(String path) {
     Session session = null;
     try {
       session = getSession();
       Node parent = session.getRootNode();
       String[] sections = path.split("/");
       for (String section : sections) {
        if (section.length() > 0 && !parent.hasNode(section)) {
          if (path.endsWith(section)) {
            Node ntfile = parent.addNode(section, "nt:file");
            Node nodeContent = ntfile.addNode("jcr:content", "nt:resource");
            nodeContent.setProperty("jcr:mimeType", "text/plain");
            nodeContent.setProperty("jcr:data", new ByteArrayInputStream("stuff".getBytes()));
            nodeContent.setProperty("jcr:lastModified", Calendar.getInstance().getTimeInMillis());           
          } else {
            parent.addNode(section);
          }
        }
        parent = parent.getNode(section); // jump into
       }
     session.save();
     return parent;
     }
     catch (Exception e) {
       throw new RuntimeException("failed to add node" + path, e);
     }   
   }   
   
}
