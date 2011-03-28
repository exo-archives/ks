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
package org.exoplatform.faq.service;

import org.exoplatform.commons.testing.AssertUtils;
import org.exoplatform.commons.testing.Closure;
import org.exoplatform.commons.testing.KernelUtils;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.faq.test.FAQServiceTestCase;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 1, 2009  
 */
public class ITInitialDataPlugin extends FAQServiceTestCase {

  private static final String DATAZIP_LOCATION = "jar:/conf/faqdata.zip";

  public ITInitialDataPlugin() throws Exception {
    super();
    faq = (FAQService) KernelUtils.getService(FAQService.class);
    conf = (ConfigurationManager) KernelUtils.getService(ConfigurationManager.class);
  }

  FAQService           faq;

  ConfigurationManager conf;

  InitialDataPlugin    plugin;

  public void setUp() throws Exception {
    InitParams params = new InitParams();
    KernelUtils.addValueParam(params, "location", DATAZIP_LOCATION);
    KernelUtils.addValueParam(params, "forceXML", "true");
    plugin = new InitialDataPlugin(params);
  }

  public void testImportData() throws Exception {
    InitParams params = new InitParams();
    plugin = new InitialDataPlugin(params);

    // .xml format is not allowed
    plugin.setLocation("somefile.xml");
    AssertUtils.assertException(new Closure() {
      public void dothis() {
        plugin.importData(faq, conf);
      }
    });

    plugin.setLocation("someplace that does not exist");
    AssertUtils.assertException(new Closure() {
      public void dothis() {
        plugin.importData(faq, conf);
      }
    });

    // import for real
    plugin.setLocation(DATAZIP_LOCATION);
    assertTrue(plugin.importData(faq, conf));

    plugin.setLocation(DATAZIP_LOCATION);
    assertFalse(plugin.importData(faq, conf));

  }

}
