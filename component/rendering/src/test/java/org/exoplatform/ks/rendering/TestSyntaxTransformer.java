/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.rendering;

import junit.framework.TestCase;

import org.exoplatform.wiki.rendering.converter.ConfluenceToXWiki2Transformer;
import org.xwiki.component.embed.EmbeddableComponentManager;

/**
 * Created by The eXo Platform SAS
 * Author : Tran Hung Phong
 *          phongth@exoplatform.com
 * Feb 27, 2012
 */
public class TestSyntaxTransformer extends TestCase {

  public void testTitle() {
	  assertTransformTest("h2. title", "== title ==");
  }
	
  public void testBold() {
	  assertTransformTest("*bold*", "**bold**");
  }

  public void testPanel() {
    assertTransformTest("{panel}panel{panel}", "{{panel}}panel{{/panel}}");
  }

  public void testPanelAndCode() {
    assertTransformTest("{panel}\npanel\n{code}\ncode\n{code}\npanel\n{panel}",
                        "{{panel}}panel{{code}}code{{/code}}panel{{/panel}}");
  }
  
  public void testCode() {
    assertTransformTest("{code}code{code}", "{{code}}code{{/code}}");
  }

  public void testSectionColumn() {
    assertTransformTest("{section}\n{column:width=50%}column1{column}\n{column:width=50%}column2{column}\n{section}",
                        "{{section}}{{column width=\"50%\"}}column1{{/column}}{{column width=\"50%\"}}column2{{/column}}{{/section}}");
  }

  public void testNoformat() {
    assertTransformTest("{noformat}\ntest{column}column1\n{noformat}",
                        "{{noformat}}test{column}column1{{/noformat}}");
  }

  public void testMacroCSV() {
    assertTransformTest("{csv}test{macro}test{csv}", "{{csv}}test{macro}test{{/csv}}");
  }

  public void testColor() {
    assertTransformTest("{color:#0000FF}*Design*{color}",
                        "{{color value=\"#0000FF\"}}**Design**{{/color}}");
  }

  public void testColorInTitle() {
    assertTransformTest("h2. {color:#0000FF}*Design*{color}",
                        "== {{color value=\"#0000FF\"}}**Design**{{/color}} ==");
  }

  public void testTableWithFormattedContent() {
    assertTransformTest("||H1||H2||\n|List1|Formatted *BOLD*|", "|=H1|=H2|List1|Formatted **BOLD**");
  }

  public void assertTransformTest(String content, String expected) {
    EmbeddableComponentManager ecm = new EmbeddableComponentManager();
    ecm.initialize(Thread.currentThread().getContextClassLoader());
    final String result = ConfluenceToXWiki2Transformer.transformContent(content, ecm).replaceAll("\n", "");
    assertEquals(expected, result); 
  }
}
