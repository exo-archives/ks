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

import junit.framework.TestCase;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jul 22, 2011  
 */
public class TestUtils extends TestCase {

  public void testIsEmpty() {
    String s = null;
    assertEquals(true, Utils.isEmpty(s));
    s = Utils.EMPTY_STR;
    assertEquals(true, Utils.isEmpty(s));
    s = Utils.SPACE;
    assertEquals(true, Utils.isEmpty(s));
    s = "abc";
    assertEquals(false, Utils.isEmpty(s));
  }

  public void testIsArrayEmpty() {
    String []strs = null;
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[]{};
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[]{Utils.EMPTY_STR};
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[]{Utils.SPACE};
    assertEquals(true, Utils.isEmpty(strs));
    strs = new String[]{"abc"};
    assertEquals(false, Utils.isEmpty(strs));
  }
  
  public void testEncodeSpecialCharInSearchTerm() {
    //test for text null
    String s = null;
    assertEquals("",Utils.encodeSpecialCharInSearchTerm(s));
    //test for text empty
    s = "";
    assertEquals("",Utils.encodeSpecialCharInSearchTerm(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",Utils.encodeSpecialCharInSearchTerm(s));
    // all characters is special characters.
    s = "@#$%^&*()\"/-=~`'.,";
    assertEquals("&#64;&#35;&#36;&#37;&#94;&#38;&#42;&#40;&#41;&#34;&#47;&#45;&#61;&#126;&#96;&apos;&#46;&#44;",Utils.encodeSpecialCharInSearchTerm(s));
    // has ignore special characters.
    s = "abc !#:? =., +;";
    assertEquals("abc !#:? =., +;",Utils.encodeSpecialCharInSearchTerm(s));
    // has ignore and not ignore special characters.
    s = "abc !#: ()\" ' | ] [";
    assertEquals("abc !#: &#40;&#41;&#34; &apos; &#124; &#93; &#91;",Utils.encodeSpecialCharInSearchTerm(s));
  }

  public void testEncodeSpecialCharInTitle() {
    //test for text null
    String s = null;
    assertEquals("",Utils.encodeSpecialCharInTitle(s));
    //test for text empty
    s = "";
    assertEquals("",Utils.encodeSpecialCharInTitle(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",Utils.encodeSpecialCharInTitle(s));
    // has double space .
    s = "   abc   aa s   s";
    assertEquals("abc aa s s", Utils.encodeSpecialCharInTitle(s));
    // has ignore special characters.
    s = "abc !#:?=.,()+; ddd";
    assertEquals("abc !#:?=.,()+; ddd",Utils.encodeSpecialCharInTitle(s));
    // has ignore and not ignore special characters.
    s = "abc !# :?=.,' | ] [";
    assertEquals("abc !# :?=.,&apos; &#124; &#93; &#91;",Utils.encodeSpecialCharInTitle(s));
  }
  
  public void testEncodeSpecialCharInContent() {
    //test for text null
    String s = null;
    assertEquals("",Utils.encodeSpecialCharInContent(s));
    //test for text empty
    s = "";
    assertEquals("",Utils.encodeSpecialCharInContent(s));
    // normal text
    s = "normal text";
    assertEquals("normal text",Utils.encodeSpecialCharInContent(s));
        // has ignore special characters.
    s = "abc &#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_ ddd";
    assertEquals("abc &#<>[]/:?\"=.,*$%()\\+@!^*-}{;`~_ ddd",Utils.encodeSpecialCharInContent(s));
    // has ignore and not ignore special characters.
    s = "abc !# :?=.,' | ] [";
    assertEquals("abc !# :?=.,&apos; &#124; ] [",Utils.encodeSpecialCharInContent(s));
  }
  
  public void testEncodeSpecialCharToHTMLnumber() {
    /* 
     * when test successful encodeSpecialCharInSearchTerm(), encodeSpecialCharInTitle() and
     * encodeSpecialCharInContent, this function encodeSpecialCharToHTMLnumber tested.
    */
  }
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
}
