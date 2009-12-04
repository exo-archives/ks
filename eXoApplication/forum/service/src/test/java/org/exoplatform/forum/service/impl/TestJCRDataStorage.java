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

import javax.jcr.Node;

import junit.framework.TestCase;

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
  
}
