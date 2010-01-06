/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jan 6, 2010 - 7:17:11 AM  
 */
public class TestForumTransformHTML extends TestCase{

	private static List<String> bbcs = Arrays.asList(new String[] { "B", "I", "IMG", "CSS", "URL", "LINK", "GOTO", "QUOTE", "LEFT", "CODE"});
	public TestForumTransformHTML() throws Exception {
	  super();
  }
	
	public void testCleanHtmlCode() {
		// for text is empty
		String str = "";
		assertEquals("", ForumTransformHTML.cleanHtmlCode(str, bbcs));
		
		// for text contain bbcode  
		str = "text [b]content[/b] has [I]bbcode[/I], [url]link[/url] and img: [img]http://host.com/abc.jpg[/img]";
		assertEquals("text content has bbcode, link and img: http://host.com/abc.jpg", 
									ForumTransformHTML.cleanHtmlCode(str, bbcs));
		
		// for text contain bbcode and tag html   
		str += " defaul data <a href='http://exoplatform.com'>link</a>. new <b>data</b>" +
											 " test<style>.css{color:blue;}</style>, <script> function a {alert('abc');}</script>tested.";
		assertEquals("text content has bbcode, link and img: http://host.com/abc.jpg defaul data link. new data test, tested.",
										ForumTransformHTML.cleanHtmlCode(str, bbcs));
	}
	
	public void testGetTitleInHTMLCode() {
	  assertEquals("", ForumTransformHTML.getTitleInHTMLCode("", bbcs));
	  assertEquals("1 3", ForumTransformHTML.getTitleInHTMLCode("1  3", bbcs));
	  
	  String title = "title [b]title[/b] <b>title</b>&nbsp;&nbsp;   title<br/>title " + new String(Character.toChars(20)) + "title\t\ntitle";
	  assertEquals("title title title&nbsp; title title title title", ForumTransformHTML.getTitleInHTMLCode(title, bbcs));
  }
	
	public void testRemoveCharterStrange() {
	  assertEquals("", ForumTransformHTML.removeCharterStrange(null));
	  assertEquals("", ForumTransformHTML.removeCharterStrange(""));

	  assertEquals("abc", ForumTransformHTML.removeCharterStrange("abc\n\t" + new String(Character.toChars(30))));
	  assertEquals("abc ", ForumTransformHTML.removeCharterStrange("abc\n" + new String(Character.toChars(32))));
  }
	
	public void testEnCodeHTML() {
		assertEquals("", ForumTransformHTML.enCodeHTML(null));
		// clear space superfluous fix for FCKEditer
		assertEquals("", ForumTransformHTML.enCodeHTML("<p>&nbsp;&nbsp;&nbsp;</p>"));
		assertEquals("", ForumTransformHTML.enCodeHTML("<p>&nbsp;&nbsp;&nbsp;     </p>"));
		assertEquals("abc", ForumTransformHTML.enCodeHTML("<br/><br/><br/><br/>abc"));
		assertEquals("abc", ForumTransformHTML.enCodeHTML("abc<br/><br/><br/><br/>"));
		// encode: '<' --> '&lt;' ; '>' --> '&gt;' ; ''' --> &#39
		assertEquals("&lt;p&gt; test &lt;br/&gt; test &#39test&#39 &lt;div&gt;text&lt;/div&gt;&lt;/p&gt;", 
								ForumTransformHTML.enCodeHTML("<p> test <br/> test 'test' <div>text</div></p>"));
  }
	
	public void testUnCodeHTML() {
		assertEquals("", ForumTransformHTML.unCodeHTML(null));
		assertEquals("<p> test <br/> test 'test' <div>text</div></p>", 
								ForumTransformHTML.unCodeHTML("&lt;p&gt; test &lt;br/&gt; test &#39test&#39 &lt;div&gt;text&lt;/div&gt;&lt;/p&gt;"));
  }

	public void testFixAddBBcodeAction() {
		// value input not null.
		assertEquals("", ForumTransformHTML.fixAddBBcodeAction(""));
		// check for tag <p>
		assertEquals("<p>[quote] test [/quote]</p>s<p>[code] text [/code]</p> <p>[QUOTE] QUOTE[/QUOTE]</p> <p>[CODE] CODE[/CODE]</p>", 
									ForumTransformHTML.fixAddBBcodeAction("<p>[quote]</p> test <p>[/quote]</p>s<p>[code]</p> text <p>[/code]</p> " +
								 "<p>[QUOTE]</p> QUOTE<p>[/QUOTE]</p> <p>[CODE]</p> CODE<p>[/CODE]</p>"));
		// check for tag <span>
		assertEquals("<span>[quote] test [/quote]</span>s<span>[code] text [/code]</span> <span>[QUOTE] QUOTE[/QUOTE]</span> <span>[CODE] CODE[/CODE]</span>", 
				ForumTransformHTML.fixAddBBcodeAction("<span>[quote]</span> test <span>[/quote]</span>s<span>[code]</span> text <span>[/code]</span> " +
						"<span>[QUOTE]</span> QUOTE<span>[/QUOTE]</span> <span>[CODE]</span> CODE<span>[/CODE]</span>"));
  }





}
