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

import org.exoplatform.forum.service.FakeForumService;
import org.exoplatform.ks.common.bbcode.BBCode;
import org.exoplatform.ks.test.webui.AbstractUIComponentTestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestUITopicDetail extends AbstractUIComponentTestCase<UITopicDetail> {

  private FakeForumService service;
  
  public TestUITopicDetail() throws Exception {
    super();
    service = new FakeForumService();;  
  }

  public void testSyncBBCodeCache() throws Exception {
    
    // active BBCodes are cached
    service.setActiveBBCode("FOO");
    component.setIsGetSv(true);
    component.syncBBCodeCache();
    List<BBCode> actual = component.listBBCode;
    assertEquals("FOO", actual.get(0).getId());
    
    // = prefix for options
    service.setActiveBBCode("FOO","=BAR");
    component.setIsGetSv(true);
    component.syncBBCodeCache();
    BBCode alt = component.listBBCode.get(1);
    assertTrue(alt.isOption());
    assertEquals("BAR_option", alt.getId());
    assertEquals("BAR", alt.getTagName());
    
    // is isGetSv = false, won't get from server
    service.setActiveBBCode("FOO","BAR","ZED");
    component.setIsGetSv(false);
    component.syncBBCodeCache();
    assertEquals(2, component.listBBCode.size());

  }
  
  public void testReplaceByBBCode() throws Exception {
    
    service.setActiveBBCode("FOO");
    BBCode foo = new BBCode();
    foo.setReplacement("BAR");
    foo.setId("FOO");
    foo.setTagName("FOO");
    service.setBBCode("FOO", foo);   
    assertEquals("BAR", component.getReplaceByBBCode("[FOO]some[/FOO]"));
  }

  @Override
  protected UITopicDetail createComponent() throws Exception {
    UITopicDetail form =  new UITopicDetail();
    form.setForumService(service);
    return form;
  }




}
