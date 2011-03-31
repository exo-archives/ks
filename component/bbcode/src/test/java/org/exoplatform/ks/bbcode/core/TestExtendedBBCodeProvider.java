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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bbcode.core;

import static org.exoplatform.commons.testing.AssertUtils.assertContains;
import static org.exoplatform.commons.testing.AssertUtils.assertEmpty;
import junit.framework.TestCase;

import org.exoplatform.ks.bbcode.api.BBCode;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestExtendedBBCodeProvider extends TestCase {

  private BBCodeRenderer         renderer;

  private MemoryBBCodeService    bbcodeService;

  private ExtendedBBCodeProvider provider;

  protected void setUp() throws Exception {
    super.setUp();
    bbcodeService = new MemoryBBCodeService();
    provider = new ExtendedBBCodeProvider();
    provider.setBBCodeService(bbcodeService);
    renderer = new BBCodeRenderer();
    renderer.setBbCodeProvider(provider);
  }

  public void testGetBBCodes() throws Exception {

    // active BBCodes are cached
    registerBBCode("FOO", "");

    assertEquals("FOO", provider.getBBCode("FOO").getTagName());

    // = prefix for options
    registerBBCode("BAR=", "");
    assertNotNull(provider.getBBCode("BAR="));
    // BBCode alt = renderer.getBbCodeProvider().getBBCode("=BAR");
    // assertTrue(alt.isOption());
    // assertEquals("BAR_option", alt.getId());
    // assertEquals("BAR", alt.getTagName());
  }

  public void testGetSupportedBBCodes() {
    assertEmpty(provider.getSupportedBBCodes());

    registerBBCode("FOO", "FOO");
    registerOptBBCode("FOO", "FOO-OPT");
    registerBBCode("BAR", "BAR");

    assertContains(provider.getSupportedBBCodes(), "FOO", "BAR");

    BBCode code = provider.getBBCode("FOO=");
    assertNotNull(code.getTagName());

  }

  private void registerBBCode(String tagName, String replacement) {
    BBCode foo = new BBCode();
    foo.setReplacement(replacement);
    foo.setId(tagName + "=");
    foo.setTagName(tagName);
    foo.setActive(true);
    foo.setOption(false);
    bbcodeService.addBBCode(foo);
  }

  private void registerOptBBCode(String tagName, String replacement) {
    BBCode foo = new BBCode();
    foo.setReplacement(replacement);
    foo.setId(tagName + "_option");
    foo.setTagName(tagName);
    foo.setActive(true);
    foo.setOption(true);
    bbcodeService.addBBCode(foo);
  }

}
