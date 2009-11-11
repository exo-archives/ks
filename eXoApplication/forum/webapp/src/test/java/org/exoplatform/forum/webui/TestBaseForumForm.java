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

import org.exoplatform.forum.service.FakeForumService;
import org.exoplatform.ks.test.webui.AbstractUIComponentTestCase;

/**
 * 
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestBaseForumForm extends AbstractUIComponentTestCase<BaseForumForm> {

  
  public TestBaseForumForm() throws Exception {
    super();
  }

  public void testI18n() {
    assertEquals("DoesNotExist", component.i18n("DoesNotExist"));
    assertEquals("Does.not.exist", component.i18n("Does.not.exist"));
    setResourceBundleEntry("key","value");
    assertEquals("value", component.i18n("key"));
  }

  public void testGetLabel() {

    assertEquals("NonExisting", component.getLabel("NonExisting"));
    
    setResourceBundleEntry("SampleForm.label.Label","value");
    assertEquals("value", component.getLabel("Label"));
    
    setResourceBundleEntry("SampleForm.label.Key", "whatever");
    assertEquals(component.getLabel("Key"), component.i18n("SampleForm.label.Key"));
  }
  
  
  public void testWarning() {
    component.warning("Message");
    assertApplicationMessage("Message");
  }


  @Override
  protected BaseForumForm createComponent() {
    BaseForumForm sampleForm = new BaseForumForm();
    sampleForm.setId("SampleForm");  
    sampleForm.setForumService(new FakeForumService());
    return sampleForm;
  }


}
