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

import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author:  Dimitri BAELI
 * dbaeli@exoplatform.com
 * Feb 02, 2012
 */
public class MacroExtractorTest extends TestCase {
  public Set<String> extractMacro(String content) {
    Map<String, Integer> result = MacroExtractor.extractMacro(null, content);
    return result.keySet();
  }

  public void testNoneMacro() {
    Set<String> test = extractMacro("aaa xxx bbb");
    TestCase.assertEquals(0, test.size());
  }

  public void testExtractMacroEmpty() {
    Set<String> test = extractMacro("test");
    TestCase.assertEquals(0, test.size());
  }

  public void testNoneMacroWithDash() {
    Set<String> test = extractMacro("Test\n|  instance type|[Large Instance|http://aws.amazon.com/ec2/] (m1.large)");
    TestCase.assertEquals(0, test.size());
  }

  public void testExtractMacroSimple() {
    Set<String> test = extractMacro("{toc}");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("toc"));
  }

  public void testExtractMacroDouble1() {
    Set<String> test = extractMacro("something {macro1} something {macro2} something");
    TestCase.assertEquals(2, test.size());
    TestCase.assertTrue(test.contains("macro1"));
    TestCase.assertTrue(test.contains("macro2"));
  }

  public void testExtractMacroDouble2() {
    Set<String> test = extractMacro("{macro1} something {macro2} something");
    TestCase.assertEquals(2, test.size());
    TestCase.assertTrue(test.contains("macro1"));
    TestCase.assertTrue(test.contains("macro2"));
  }

  public void testExtractMacroDouble3() {
    Set<String> test = extractMacro("something {macro1} something {macro2}");
    TestCase.assertEquals(2, test.size());
    TestCase.assertTrue(test.contains("macro1"));
    TestCase.assertTrue(test.contains("macro2"));
  }

  public void testExtractMacroMultiple3() {
    Set<String> test = extractMacro("something {macro1} something {macro2} something {macro3} something {macro4} something {macro5}");
    TestCase.assertEquals(5, test.size());
    TestCase.assertTrue(test.contains("macro1"));
    TestCase.assertTrue(test.contains("macro2"));
    TestCase.assertTrue(test.contains("macro3"));
    TestCase.assertTrue(test.contains("macro4"));
    TestCase.assertTrue(test.contains("macro5"));
  }

  public void testExtractMacroWithValueCase1() {
    Set<String> test = extractMacro("something {macro1:test}");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("macro1"));
  }

  public void testExtractMacroWithValueCase2() {
    Set<String> test = extractMacro("something {macro1:test|value=1}");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("macro1"));
  }

  public void testExtractMacroWithSpace() {
    Set<String> test = extractMacro("something {macro1 }");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("macro1"));
  }

  public void testExtractMacroWithCode() {
    Set<String> test = extractMacro("something {code} content {test} {code}");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("code"));
  }

  public void testExtractMacroWithCodeJava() {
    Set<String> test = extractMacro("something {code:java} content {test} {code}");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("code"));
  }

  public void testExtractMacroWithCodeTwice() {
    Set<String> test = extractMacro("something {code} content {test} {code} something else {macro1} something {code} content {test} {code}");
    TestCase.assertEquals(2, test.size());
    TestCase.assertTrue(test.contains("macro1"));
    TestCase.assertTrue(test.contains("code"));
  }

  public void testCodeRemoval() {
    String result = MacroExtractor.removeBlocks("code", "aaa{code} xxx {code}bbb");
    TestCase.assertEquals("aaa{code}bbb", result);
  }

  public void testStyleRemoval() {
    String result = MacroExtractor.removeBlocks("style", "aaa{style} xxx {style}bbb");
    TestCase.assertEquals("aaa{style}bbb", result);
  }

  public void testCodeTwiceRemoval() {
    String result = MacroExtractor.removeBlocks("code", "aaa{code} xxx {code}bbb{code} xxx {code}ccc");
    TestCase.assertEquals("aaa{code}bbb{code}ccc", result);
  }

  public void testCodeTwiceRemovalBraquetInside() {
    String result = "aaa{code} xxx {nonmacro} {code}bbb{code} xxx {nonmacro} {code}ccc";
    String content = MacroExtractor.removeBlocks("code", result);
    TestCase.assertEquals("aaa{code}bbb{code}ccc", content);
  }

  public void testCodeTwiceRemovalExtendedMacro() {
    String result = "aaa{code} xxx {code}bbb{code:java} xxx {code}ccc";
    String content = MacroExtractor.removeBlocks("code", result);
    TestCase.assertEquals("aaa{code}bbb{code}ccc", content);
  }

  public void testCodeTwiceRemovalLimits() {
    String result = "{code} xxx {code}bbb{code} xxx {code}";
    String content = MacroExtractor.removeBlocks("code", result);
    TestCase.assertEquals("{code}bbb{code}", content);
    result = "{code} xxx {code}bbb{code:java} xxx {code}";
    content = MacroExtractor.removeBlocks("code", result);
    TestCase.assertEquals("{code}bbb{code}", content);
    result = "{code} xxx {macro} {code}bbb{code:java} xxx {code}";
    content = MacroExtractor.removeBlocks("code", result);
    TestCase.assertEquals("{code}bbb{code}", content);
  }

  public void testEscapedMacro() {
    Set<String> test = extractMacro("aaa\\{escaped}xxx{macro}bbb");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("macro"));
  }

  public void testGliffy() {
    Set<String> test = extractMacro("{gliffy:name=GateIn OAuth diagram|align=left|size=L|version=5}");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("gliffy"));
  }

  public void testEscapedMacro2() {
    Set<String> test = extractMacro("aaa\\{escaped}xxx{macro}bbb\\{escaped2}");
    TestCase.assertEquals(1, test.size());
    TestCase.assertTrue(test.contains("macro"));
  }

  public void testCountMacros() {
    Map<String, Integer> result = MacroExtractor.extractMacro(null, "{macro1} {macro2}bbb{macro1:java} xxx {macro3}");
    TestCase.assertEquals(3, result.keySet().size());
    TestCase.assertEquals(2, (int) result.get("macro1"));
    TestCase.assertEquals(1, (int) result.get("macro2"));
    TestCase.assertEquals(1, (int) result.get("macro3"));
  }

  public void testExtractMacroWithParams() {
    Set<String> result = MacroExtractor.extractMacroWithParams("{macro1} {macro2}bbb{macro1:java}");
    TestCase.assertTrue(result.contains("macro1"));
    TestCase.assertTrue(result.contains("macro2"));
    TestCase.assertTrue(result.contains("macro1:java"));
  }

  public void testNoFormatParsing() {
    Map<String, Integer> result = MacroExtractor.extractMacro(null, "{noformat}commons.upgrade.plugins.order={Plugin1_Name},{Plugin2_Name},{Plugin3_Name}{noformat} ");
    TestCase.assertEquals(1, (int) result.get("noformat"));
    TestCase.assertEquals(null, result.get("Plugin1_Name"));
  }

  public void testNoFormatParsing_ExtractWithParams() {
    Set<String> result = MacroExtractor.extractMacroWithParams("{noformat} text {macro} text {noformat}");
    TestCase.assertTrue(result.contains("noformat"));
    TestCase.assertTrue(!result.contains("macro"));
  }


  public void testNoFormatDoubleBracket() {
    Map<String, Integer> result = MacroExtractor.extractMacro(null, "text {{notamacro}} text");
    TestCase.assertEquals(null, result.get("notamacro"));
  }

  public void testNoFormatDoubleBracket_ExtractWithParams() {
    Set<String> result = MacroExtractor.extractMacroWithParams("text {{notamacro}} text");
    TestCase.assertTrue(!result.contains("notamacro"));
    TestCase.assertTrue(result.isEmpty());
  }

  public void testRemoveBlocks() {
    final String content = MacroExtractor.removeBlocks("noformat", "before{noformat}inside{noformat}after");
    assertEquals("before{noformat}after", content);
  }

  public void testRemoveBlocksExtreme() {
    final String content = MacroExtractor.removeBlocks("noformat", "{noformat}inside{noformat}");
    assertEquals("{noformat}", content);
  }

  public void testExtractMacroCSV() {
    final String content = MacroExtractor.removeBlocks("csv", "{csv}inside{csv}");
    assertEquals("{csv}", content);
  }

  public void testCSVMacro() {
    Set<String> result = MacroExtractor.extractMacroWithParams("{csv} text {macro} text {csv}");
    TestCase.assertTrue(result.contains("csv"));
    TestCase.assertTrue(!result.contains("macro"));

  }
}