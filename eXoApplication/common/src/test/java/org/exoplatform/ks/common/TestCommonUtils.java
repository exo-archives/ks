/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.common;

import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 22, 2011  
 */
public class TestCommonUtils extends TestCase {

  public void testIsEmpty() {
    String s = null;
    assertEquals(true, CommonUtils.isEmpty(s));
    s = CommonUtils.EMPTY_STR;
    assertEquals(true, CommonUtils.isEmpty(s));
    s = CommonUtils.SPACE;
    assertEquals(true, CommonUtils.isEmpty(s));
    s = "abc";
    assertEquals(false, CommonUtils.isEmpty(s));
  }

  public void testIsArrayEmpty() {
    String []strs = null;
    assertEquals(true, CommonUtils.isEmpty(strs));
    strs = new String[]{};
    assertEquals(true, CommonUtils.isEmpty(strs));
    strs = new String[]{CommonUtils.EMPTY_STR};
    assertEquals(true, CommonUtils.isEmpty(strs));
    strs = new String[]{CommonUtils.SPACE};
    assertEquals(true, CommonUtils.isEmpty(strs));
    strs = new String[]{"abc"};
    assertEquals(false, CommonUtils.isEmpty(strs));
  }
  
  public void testEncodeSpecialCharInSearchTerm() {
    //test for text null
    String s = null;
    assertEquals("",CommonUtils.encodeSpecialCharInSearchTerm(s));
    //test for text empty
    s = "";
    assertEquals("",CommonUtils.encodeSpecialCharInSearchTerm(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",CommonUtils.encodeSpecialCharInSearchTerm(s));
    // all characters is special characters.
    s = "@#$%^&*()\"/-=~`'.,";
    assertEquals("&#64;&#35;&#36;&#37;&#94;&#38;&#42;&#40;&#41;&#34;&#47;&#45;&#61;&#126;&#96;&#39;&#46;&#44;",CommonUtils.encodeSpecialCharInSearchTerm(s));
    // has ignore special characters.
    s = "abc !#:? =., +;";
    assertEquals("abc !#:? =., +;",CommonUtils.encodeSpecialCharInSearchTerm(s));
    // has ignore and not ignore special characters.
    s = "abc !#: ()\" ' | ] [";
    assertEquals("abc !#: &#40;&#41;&#34; &#39; &#124; &#93; &#91;",CommonUtils.encodeSpecialCharInSearchTerm(s));
  }

  public void testEncodeSpecialCharInTitle() {
    //test for text null
    String s = null;
    assertEquals("",CommonUtils.encodeSpecialCharInTitle(s));
    //test for text empty
    s = "";
    assertEquals("",CommonUtils.encodeSpecialCharInTitle(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",CommonUtils.encodeSpecialCharInTitle(s));
    // has double space .
    s = "   abc   aa s   s";
    assertEquals("abc aa s s", CommonUtils.encodeSpecialCharInTitle(s));
    // has ignore special characters.
    s = "abc !#:?=.,()+; ddd";
    assertEquals("abc !#:?=.,()+; ddd",CommonUtils.encodeSpecialCharInTitle(s));
    // has ignore and not ignore special characters.
    s = "abc !# :?=.,' | ] [";
    assertEquals("abc !# :?=.,&#39; &#124; &#93; &#91;",CommonUtils.encodeSpecialCharInTitle(s));
  }
  
  public void testEncodeSpecialCharInContent() {
    //test for text null
    String s = null;
    assertEquals("",CommonUtils.encodeSpecialCharInContent(s));
    //test for text empty
    s = "";
    assertEquals("",CommonUtils.encodeSpecialCharInContent(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",CommonUtils.encodeSpecialCharInContent(s));
        // has ignore special characters.
    s = "abc &#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_ ddd";
    assertEquals("abc &#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_ ddd",CommonUtils.encodeSpecialCharInContent(s));
    // has ignore and not ignore special characters.
    s = "abc !# :?=.,' | ] [";
    assertEquals("abc !# :?=.,&#39; &#124; ] [",CommonUtils.encodeSpecialCharInContent(s));
  }
  
  public void testEncodeSpecialCharToHTMLnumber() {
    /* 
     * when test successful encodeSpecialCharInSearchTerm(), encodeSpecialCharInTitle() and
     * encodeSpecialCharInContent, this function encodeSpecialCharToHTMLnumber tested.
    */
  }
  
  
  public void testDecodeSpecialCharToHTMLnumber() throws Exception {
    String input = null;
    assertEquals(null, CommonUtils.decodeSpecialCharToHTMLnumber(input));
    input = "";
    assertEquals(input, CommonUtils.decodeSpecialCharToHTMLnumber(input));
    input = "Normal text abc";
    assertEquals(input, CommonUtils.decodeSpecialCharToHTMLnumber(input));
    input = "Text ...&#60;&#64;&#35;&#36;&#37;&#94;&#38;&#42;&#40;&#41;&#34;&#47;&#45;&#61;&#126;&#96;&#39;&#46;&#44;&#62; too";
    assertEquals("Text ...<@#$%^&*()\"/-=~`'.,> too", CommonUtils.decodeSpecialCharToHTMLnumber(input));
    //content extend token
    input = "Text ...&gt;div class=&quot;&amp;XZY&quot;&lt;Test&gt;&#47;div&lt;&#40;&#41;&#34;&#47;&#45;&#61;&#126;&#96;&#39;&#46;&#44;&#60;strong&#62;too&#60;&#47;strong&#62;";
    assertEquals("Text ...<div class=\"&XZY\">Test</div>()\"/-=~`'.,<strong>too</strong>", CommonUtils.decodeSpecialCharToHTMLnumber(input));
    
    // ignore case
    List<String> ig = Arrays.asList(new String[]{"&gt;", "&lt;", "&#46;"});
    assertEquals("Text ...&gt;div class=\"&XZY\"&lt;Test&gt;/div&lt;()\"/-=~`'&#46;,<strong>too</strong>", CommonUtils.decodeSpecialCharToHTMLnumber(input, ig));
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
