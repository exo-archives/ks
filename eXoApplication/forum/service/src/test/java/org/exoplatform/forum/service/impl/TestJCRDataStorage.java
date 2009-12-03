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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Value;

import junit.framework.TestCase;

import org.exoplatform.forum.service.impl.JCRDataStorage;
import org.exoplatform.ks.test.AssertUtils;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestJCRDataStorage extends TestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testUpdateModeratorInForum() throws Exception {
    Node node  = mock(Node.class);
    String moderators = "exo:moderators";
    JCRDataStorage storage = new JCRDataStorage();
    
    stubProperty(node, moderators, "foo", "bar");
    String[] actual = storage.updateModeratorInForum(node, new String [] {"foo", "zed"});
    assertContains(actual, "foo", "bar", "zed");
    
    Node node2  = mock(Node.class);
    stubNullProperty(node2, moderators);
    String[] actual2 = storage.updateModeratorInForum(node2, new String [] {"foo", "zed"});
    assertContains(actual2,"foo", "zed");
    
    Node node3  = mock(Node.class);
    stubProperty(node, moderators, " ", "bar");
    String[] actual3 = storage.updateModeratorInForum(node3, new String [] {"foo", "zed"});
    assertContains(actual3,"foo", "zed");
  }
  
  /**
   * Stubs a node with a null property. A PathNotFoundException is throow by getProperty() on propName
   * @param node
   * @param propName
   * @throws Exception atually only a PathNodeFoundException
   */
  private void stubNullProperty(Node node, String propName) throws Exception {
    Property prop = mock(Property.class); 
    when(node.getProperty(propName)).thenReturn(prop);
    when(prop.getValues()).thenThrow(new PathNotFoundException());
  }
  
  /**
   * Stub a multi value Strng property
   * @param node 
   * @param propName name of the property
   * @param svalues values to
   * @throws Exception
   */
  private void stubProperty(Node node, String propName, String... svalues) throws Exception{
    Property prop = mock(Property.class); 
    when(node.getProperty(propName)).thenReturn(prop);
    Value[] values = new Value[svalues.length];
     for (int i = 0; i < svalues.length; i++) {
      values[i] = value(svalues[i]);
    }
    when(prop.getValues()).thenReturn(values);
  }

  private Value value(String sValue) throws Exception {
    Value v = mock(Value.class);
    when(v.getString()).thenReturn(sValue);
    return v;
  }
  
}
