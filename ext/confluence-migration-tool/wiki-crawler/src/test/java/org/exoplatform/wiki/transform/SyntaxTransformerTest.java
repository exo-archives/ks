package org.exoplatform.wiki.transform;

import junit.framework.TestCase;

/**
 */
public class SyntaxTransformerTest extends TestCase {

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
    assertTransformTest("{section}\n{column}column1{column}\n{column}column2{column}\n{section}",
                        "{{section}}{{column}}column1{{/column}}{{column}}column2{{/column}}{{/section}}");
  }

  public void testNoformat() {
    assertTransformTest("{noformat}\ntest{column}column1\n{noformat}",
                        "{{noformat}}test{column}column1{{/noformat}}");
  }

  // public void testQuote() {
  // assertTransformTest("{quote}test\ntest\ntest\ntest{quote}",
  // ">test\n>test");
  // }

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
    final String result = SyntaxTransformer.transformContent(content).replaceAll("\n", "");
    System.out.println(result.replaceAll("\n", ""));
    assertEquals(expected, result); 
  }
}
