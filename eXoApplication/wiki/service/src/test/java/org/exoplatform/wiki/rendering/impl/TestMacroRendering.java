/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.impl;

import org.xwiki.rendering.syntax.Syntax;


/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 15, 2010  
 */
public class TestMacroRendering extends AbstractRenderingTestCase {
  
  public void testRenderNoteMacro() throws Exception {
    String expectedHtml = "<div class=\"box notemessage\">This is a note.</div>";
    assertEquals(expectedHtml, renderingService.render("{{note}}This is a note.{{/note}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("{note}This is a note.{note}", Syntax.CONFLUENCE_1_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderTipMacro() throws Exception {
    String expectedHtml = "<div class=\"box tipmessage\">This is a tip.</div>";
    assertEquals(expectedHtml, renderingService.render("{{tip}}This is a tip.{{/tip}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("{tip}This is a tip.{tip}", Syntax.CONFLUENCE_1_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderInfoMacro() throws Exception {
    String expectedHtml = "<div class=\"box infomessage\">This is an info.</div>";
    assertEquals(expectedHtml, renderingService.render("{{info}}This is an info.{{/info}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("{info}This is an info.{info}", Syntax.CONFLUENCE_1_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }

  public void testRenderWarningMacro() throws Exception {
    String expectedHtml = "<div class=\"box warningmessage\">This is an warning.</div>";
    assertEquals(expectedHtml, renderingService.render("{{warning}}This is an warning.{{/warning}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("{warning}This is an warning.{warning}", Syntax.CONFLUENCE_1_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }

  public void testRenderErrorMacro() throws Exception {
    String expectedHtml = "<div class=\"box errormessage\">This is an error.</div>";
    assertEquals(expectedHtml, renderingService.render("{{error}}This is an error.{{/error}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }

  public void testRenderCodeMacro() throws Exception {
    String expectedHtml = "<div class=\"box code\"><span style=\"font-weight: bold; color: #008000; \">&lt;html&gt;&lt;head&gt;</span>Cool!<span style=\"font-weight: bold; color: #008000; \">&lt;/head&gt;&lt;/html&gt;</span></div>";
    assertEquals(expectedHtml, renderingService.render("{{code language=\"html\"}}<html><head>Cool!</head></html>{{/code}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderSectionAndColumnMacro() throws Exception {
    String expectedHtml = "<div><div style=\"float:left;width:49.2%;padding-right:1.5%;\"><p>Column one text goes here</p></div><div style=\"float:left;width:49.2%;\"><p>Column two text goes here</p></div><div style=\"clear:both\"></div></div>";
    assertEquals(expectedHtml, renderingService.render("{{section}}\n\n{{column}}Column one text goes here{{/column}}\n\n{{column}}Column two text goes here{{/column}}\n\n{{/section}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("{section}\n{column}Column one text goes here{column}\n{column}Column two text goes here{column}\n{section}", Syntax.CONFLUENCE_1_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderNoFormatMacro() throws Exception {
    String expectedXWikiHtml = "<pre>pre-formatted piece of text so **no** further __formatting__ is done here</pre>";
    String expectedConfluenceHtml = "<pre>pre-formatted piece of text so *no* further _formatting_ is done here</pre>";
    assertEquals(expectedXWikiHtml, renderingService.render("{{noformat}}pre-formatted piece of text so **no** further __formatting__ is done here{{/noformat}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedConfluenceHtml, renderingService.render("{noformat}pre-formatted piece of text so *no* further _formatting_ is done here{noformat}", Syntax.CONFLUENCE_1_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
  public void testRenderPanelMacro() throws Exception {
    String expectedHtml = "<div class=\"panel\"><div class=\"panelHeader\">My Title</div><div class=\"panelContent\"><p>Some text with a title</p></div></div>";
    assertEquals(expectedHtml, renderingService.render("{{panel title=\"My Title\"}}Some text with a title{{/panel}}", Syntax.XWIKI_2_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
    assertEquals(expectedHtml, renderingService.render("{panel:title=My Title}Some text with a title{panel}", Syntax.CONFLUENCE_1_0.toIdString(), Syntax.XHTML_1_0.toIdString()));
  }
  
}
