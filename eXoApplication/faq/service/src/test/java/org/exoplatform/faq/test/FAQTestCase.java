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
package org.exoplatform.faq.test;

import java.util.List;

import junit.framework.AssertionFailedError;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 2, 2009  
 */
public abstract class FAQTestCase extends BasicTestCase {
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
  
  
  protected Object getService(Class clazz) {
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    return container.getComponentInstanceOfType(clazz);
  }

  
  protected void addValueParam(InitParams params, String name, String value) {
    ValueParam param = new ValueParam();
    param.setName(name);
    param.setValue(value);
    params.addParameter(param);
   }

  
  protected void assertException(Closure code) {
    try {
      code.dothis();
    } catch (Exception e) {
      return ;// Exception correctly thrown
    }
    throw new AssertionFailedError("An exception should have been thrown.");
  }
  
}
