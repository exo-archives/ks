/*
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
 */
package org.exoplatform.ks.rss;

import static org.testng.AssertJUnit.assertEquals;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.test.AbstractExoContainerTestCase;
import org.exoplatform.ks.test.ConfigurationUnit;
import org.exoplatform.ks.test.ConfiguredBy;
import org.exoplatform.ks.test.ContainerScope;
import org.exoplatform.ks.test.KernelUtils;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
@ConfiguredBy( {
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml"),
    @ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/rss-configuration.xml") })
public class TestKSRSSServlet extends AbstractExoContainerTestCase {

  
  public void _testOnService() throws Exception {
    final KSRSSServlet servlet = new KSRSSServlet();

    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getPathInfo()).thenReturn("/faq/categories/Categoryf136732fc0a8000d00297cb37949b644");
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    final ExoContainer container = PortalContainer.getInstance();
  
    try {
        servlet.onService(container, request, response);
    } catch (ServletException e) {
      
    }
    //fail("Should have thrown exception");

  }

  @Test
  public void testExtractAppType() {
    KSRSSServlet servlet = new KSRSSServlet();
    assertEquals("faq", servlet.extractAppType("/faq/categories"));
    assertEquals("faq",
                 servlet.extractAppType("/faq/categories/Categoryf136732fc0a8000d00297cb37949b644"));

    assertEquals("forum",
                 servlet.extractAppType("/forum/forumCategoryf138e67cc0a8000d00bb6b574c589a4c"));
    assertEquals("forum", servlet.extractAppType("/forum/forumf13a2450c0a8000d00307a860ed10cbc"));
    assertEquals("forum", servlet.extractAppType("/forum/topicf13b3e2ac0a8000d001b09820ccd9c41"));

    assertEquals("", servlet.extractAppType("/root"));

  }

  @Test
  public void testExtractObjectId() {
    KSRSSServlet servlet = new KSRSSServlet();
    assertEquals("categories", servlet.extractObjectId("/faq/categories"));
    assertEquals("categories/Category001", servlet.extractObjectId("/faq/categories/Category001"));

    assertEquals("forumCategory001", servlet.extractObjectId("/forum/forumCategory001"));
    assertEquals("forum001", servlet.extractObjectId("/forum/forum001"));
    assertEquals("topic001", servlet.extractObjectId("/forum/topic001"));

    assertEquals("root", servlet.extractObjectId("/root"));

  }

  private void registerFakeProvider() {
    InitParams params = new InitParams();
    KernelUtils.addValueParam(params, "defaultProvider", FakeProvider.class.getName());
    Map<String, String> map = new HashMap<String, String>();
    map.put("foo", FakeProvider.class.getName());
    KernelUtils.addPropertiesParam(params, "providers", map);
    FeedResolver resolver = new FeedResolver(params);
    registerComponent(FeedResolver.class, resolver);

  }

  public class FakeProvider implements FeedContentProvider {

    public FakeProvider() {
      
    }
    
    public InputStream getFeedContent(String targetId) {

      return null;
    }

  }

}
