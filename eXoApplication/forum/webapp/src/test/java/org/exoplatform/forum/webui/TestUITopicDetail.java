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
package org.exoplatform.forum.webui;

import java.util.List;

import org.exoplatform.forum.rendering.ExtendedBBCodeProvider;
import org.exoplatform.forum.service.FakeBBCodeService;
import org.exoplatform.forum.service.FakeForumService;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.ks.common.bbcode.BBCodeRenderer;
import org.exoplatform.ks.rendering.MarkupRenderingService;
import org.exoplatform.ks.rendering.api.Renderer;
import org.exoplatform.ks.test.webui.AbstractUIComponentTestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestUITopicDetail extends AbstractUIComponentTestCase<UITopicDetail> {

  private FakeForumService service;
  private MarkupRenderingService markupRenderingService;
  private FakeBBCodeService bbcodeService;
  
  public TestUITopicDetail() throws Exception {
    super();
    service = new FakeForumService();  
  }

  public void doSetUp() {  
    super.doSetUp();
    this.markupRenderingService = new MarkupRenderingService();
    Renderer bbcodeRenderer = new BBCodeRenderer();
    ExtendedBBCodeProvider provider = new ExtendedBBCodeProvider();
    provider.setBBCodeService(bbcodeService);
    markupRenderingService.registerRenderer(bbcodeRenderer);
    component.setMarkupRenderingService(markupRenderingService);
  }
  
  public void testSyncBBCodeCache() throws Exception {
   
    // active BBCodes are cached
    registerBBCode("FOO", "");
    component.setIsGetSv(true);
    component.syncBBCodeCache();
    List<BBCode> actual = component.listBBCode;
    assertEquals("FOO", actual.get(0).getId());
    
    // = prefix for options
    registerBBCode("=BAR", "");
    component.setIsGetSv(true);
    component.syncBBCodeCache();
    BBCode alt = component.listBBCode.get(1);
    assertTrue(alt.isOption());
    assertEquals("BAR_option", alt.getId());
    assertEquals("BAR", alt.getTagName());
    
    // is isGetSv = false, won't get from server
    registerBBCode("ZED", "");
    
    component.setIsGetSv(false);
    component.syncBBCodeCache();
    assertEquals(2, component.listBBCode.size());
   

  }
  
  public void testReplaceByBBCode() throws Exception {
    registerBBCode("FOO", "BAR");
    assertEquals("BAR", component.getReplaceByBBCode("[FOO]some[/FOO]"));
  }
  
  public void testProcessMarkup() throws Exception {
    registerBBCode("FOO", "BAR");
    String markup = "[FOO]sdsd[/FOO]";
    assertEquals("BAR", component.processMarkup(markup));
    assertEquals(component.getReplaceByBBCode(markup), component.processMarkup(markup));
    
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
  
  @Override
  protected UITopicDetail createComponent() throws Exception {
    UITopicDetail form =  new UITopicDetail();
    form.setForumService(service);
    return form;
  }




}
