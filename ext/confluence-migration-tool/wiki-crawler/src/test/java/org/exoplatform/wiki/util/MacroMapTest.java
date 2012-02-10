/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
package org.exoplatform.wiki.util;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * Created by The eXo Platform SAS
 * Author:  Dimitri BAELI
 * dbaeli@exoplatform.com
 * Feb 02, 2012
 */
public class MacroMapTest extends TestCase {

  public void testMergeMaps() {
    HashMap<String, Integer> sourceMap = new HashMap<String, Integer>();
    sourceMap.put("test", 1);
    sourceMap.put("toto", 1);
    HashMap<String, Integer> targetMap = new HashMap<String, Integer>();
    targetMap.put("test", 1);
    targetMap.put("tata", 1);
    MacroMap.mergeMaps(sourceMap, targetMap);
    TestCase.assertEquals(2, MacroMap.getCount(targetMap, "test"));
    TestCase.assertEquals(1, MacroMap.getCount(targetMap, "toto"));
    TestCase.assertEquals(1, MacroMap.getCount(targetMap, "tata"));
    TestCase.assertEquals(0, MacroMap.getCount(targetMap, "titi"));
  }
}
