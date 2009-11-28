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
package org.exoplatform.forum.rendering;

import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.forum.service.FakeBBCodeService;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.ks.common.bbcode.BBCodeRenderer;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestExtendedBBCodeProvider extends TestCase {

  private BBCodeRenderer renderer;
  private FakeBBCodeService bbcodeService;
  private ExtendedBBCodeProvider provider;
  
  protected void setUp() throws Exception {
    super.setUp();
    bbcodeService = new FakeBBCodeService();  
    provider = new ExtendedBBCodeProvider();
    provider.setBBCodeService(bbcodeService);
    renderer = new BBCodeRenderer();
    renderer.setBbCodeProvider(provider);
  }

    public void testGetBBCodes() throws Exception {
      
      // active BBCodes are cached
      registerBBCode("FOO", "");


      List<BBCode> actual = renderer.getBbcodes();
      assertEquals("FOO", actual.get(0).getId());
      
      // = prefix for options
      registerBBCode("=BAR", "");

      //BBCode alt = renderer.getBbCodeProvider().getBBCode("=BAR");
      //assertTrue(alt.isOption());
      //assertEquals("BAR_option", alt.getId());
      //assertEquals("BAR", alt.getTagName());
    }
  
    
    private void registerBBCode(String tagName, String replacement) {
      BBCode foo = new BBCode();
      foo.setReplacement(replacement);
      foo.setId(tagName);
      foo.setTagName(tagName);  
      foo.setActive(true);
      foo.setOption(tagName.startsWith("="));
      bbcodeService.addBBCode(foo);
    }
    

}
