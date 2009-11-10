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

import org.exoplatform.ks.test.AbstractWebuiTestCase;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class TestBaseForumForm extends AbstractWebuiTestCase {
  BaseForumForm sampleForm;
  
  protected void doSetUp() {
    sampleForm = new BaseForumForm();
    sampleForm.setId("SampleForm");    
  }
  

  public void testI18n() {
    assertEquals("DoesNotExist", sampleForm.i18n("DoesNotExist"));
    assertEquals("Does.not.exist", sampleForm.i18n("Does.not.exist"));
    getAppRes().put("key","value");
    assertEquals("value", sampleForm.i18n("key"));
  }




  public void testGetLabel() {

    assertEquals("NonExisting", sampleForm.getLabel("NonExisting"));
    
    getAppRes().put("SampleForm.label.Label","value");
    assertEquals("value", sampleForm.getLabel("Label"));
    
    getAppRes().put("SampleForm.label.Key", "whatever");
    assertEquals(sampleForm.getLabel("Key"), sampleForm.i18n("SampleForm.label.Key"));
  }

  

  
}
