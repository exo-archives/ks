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

/**
 * Created by The eXo Platform SAS
 * Author:  Dimitri BAELI
 * dbaeli@exoplatform.com
 * Feb 02, 2012
 */
public class MacroTransformTest extends TestCase {
  public void testNoChange() {
    String macroContent = "{macro}";
    String result = ExoWikiMacroTransformer.transformMacroContent(macroContent);
    TestCase.assertEquals(macroContent, result);
  }

  public void testNoChangeParameters() {
    String macroContent = "{macro:src=test}";
    String result = ExoWikiMacroTransformer.transformMacroContent(macroContent);
    TestCase.assertEquals(macroContent, result);
  }

  public void testChangeOnIFrame() {
    String macroContent = "{iframe:src=test}";
    String macroExpected = "{iframe:src=\"test\"}";
    String result = ExoWikiMacroTransformer.transformMacroContent(macroContent);
    TestCase.assertEquals(macroExpected, result);
  }

  public void testChangeOnIFrameWithParams() {
    String macroContent = "{iframe:src=test|width=123px}";
    String macroExpected = "{iframe:src=\"test\"|width=123px}";
    String result = ExoWikiMacroTransformer.transformMacroContent(macroContent);
    TestCase.assertEquals(macroExpected, result);
  }

  public void testChangeOnIFrameWithQuotes() {
    String macroContent = "{iframe:src=\"test\"}";
    String macroExpected = "{iframe:src=\"test\"}";
    String result = ExoWikiMacroTransformer.transformMacroContent(macroContent);
    TestCase.assertEquals(macroExpected, result);
  }
}
