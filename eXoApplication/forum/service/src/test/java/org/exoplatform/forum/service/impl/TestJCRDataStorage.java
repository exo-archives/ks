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
package org.exoplatform.forum.service.impl;


import static org.exoplatform.ks.test.AssertUtils.assertContains;
import static org.exoplatform.ks.test.mock.JCRMockUtils.mockNode;
import static org.exoplatform.ks.test.mock.JCRMockUtils.stubNullProperty;
import static org.exoplatform.ks.test.mock.JCRMockUtils.stubProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jcr.Node;
import javax.jcr.Session;

import junit.framework.TestCase;

import org.exoplatform.ks.common.jcr.JCRSessionManager;
import org.exoplatform.ks.common.jcr.JCRTask;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.KSDataLocation.Locations;
import org.mockito.ArgumentCaptor;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestJCRDataStorage extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testUpdateModeratorInForum() throws Exception {
    String moderatorsPropName = "exo:moderators";
    String [] moderators = new String [] {"foo", "zed"};
    JCRDataStorage storage = new JCRDataStorage();

    Node node  = mockNode();
    stubProperty(node, moderatorsPropName, "foo", "bar");
    String[] actual = storage.updateModeratorInForum(node, moderators);
    assertContains(actual, "foo", "bar", "zed");
    
    Node node2  = mockNode();
    stubNullProperty(node2, moderatorsPropName);
    String[] actual2 = storage.updateModeratorInForum(node2, moderators);
    assertContains(actual2,"foo", "zed");
    
    Node node3  = mockNode();
    stubProperty(node3, moderatorsPropName, " ", "bar");
    String[] actual3 = storage.updateModeratorInForum(node3, moderators);
    assertContains(actual3,"foo", "zed");
  }
  
  
  public void testSetTestDefaultAvatar() throws Exception {
    JCRDataStorage storage = new JCRDataStorage();
    KSDataLocation locator = new KSDataLocation(null, null);
    JCRSessionManager sessionManager = stubJCRSessionManager();
    locator.setSessionManager(sessionManager);
    storage.setDataLocator(locator);

    Node avatarHome = stubNodeForPath(Locations.KS_USER_AVATAR, sessionManager);
    storage.setDefaultAvatar("foo");
    verify(avatarHome).hasNode("foo"); // verify we tried to load the node
 

    Node avatar = stubChild(avatarHome, "foo", "nt:file");
    storage.setDefaultAvatar("foo");
    verify(avatar).remove();
    verify(avatarHome).save();
  }
  
  @SuppressWarnings("unchecked")
  private JCRSessionManager stubJCRSessionManager() {
    JCRSessionManager sessionManager = mock(JCRSessionManager.class);
    when(sessionManager.executeAndSave(any(JCRTask.class))).thenCallRealMethod();
    when(sessionManager.execute(any(JCRTask.class))).thenCallRealMethod(); 
    return sessionManager;
  }

  private Node stubChild(Node parent, String name, String type) throws Exception {
    Node child = mock(Node.class);
    when(child.isNodeType(type)).thenReturn(true);
    when(child.getName()).thenReturn(name);
    when(parent.getNode(name)).thenReturn(child);
    when(parent.hasNode(name)).thenReturn(true);
    return child;
  }

  @SuppressWarnings("unchecked")
  private Node stubNodeForPath(String path, JCRSessionManager manager) throws Exception {
    Node node = mock(Node.class);
    Node root = mock(Node.class);
    when(root.getNode(path)).thenReturn(node);
    Session session = mock(Session.class);
    when(session.getRootNode()).thenReturn(root);
    when(manager.createSession()).thenReturn(session);
    when(manager.openSession()).thenReturn(session);
    when(manager.getCurrentSession()).thenReturn(session);
    return node;
  }
  
}
