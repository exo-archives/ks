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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.mockito.Mockito;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice
 *         Lamarque</a>
 * @version $Revision$
 */
public class TestKSRSSServlet {

  @Test
  public void testOnService() throws Exception {
    final KSRSSServlet servlet = new KSRSSServlet();

    // fixture
    
    // mock servlet API
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getPathInfo()).thenReturn("/app/objectId");
    
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
    Mockito.when(response.getOutputStream()).thenReturn(outputStream);
   
    // fake resolver
    FeedResolver resolver = new FeedResolver();
    resolver.setDefaultProvider(new FakeContentProvider("test"));
    servlet.setFeedResolver(resolver); 
   
    servlet.onService(PortalContainer.getInstance(), request, response);

    Mockito.verify(outputStream).write("test".getBytes());


  }
  
  @Test(expectedExceptions={ServletException.class})
  public void testNullFeed() throws Exception {
    final KSRSSServlet servlet = new KSRSSServlet();

    // fixture
    
    // mock servlet API
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getPathInfo()).thenReturn("/app/objectId");
    
    final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
    ServletOutputStream outputStream = Mockito.mock(ServletOutputStream.class);
    Mockito.when(response.getOutputStream()).thenReturn(outputStream);
   
    // fake resolver
    FeedResolver resolver = new FeedResolver();
    resolver.setDefaultProvider(new FakeContentProvider(null));
    servlet.setFeedResolver(resolver); // resolver will be taken directly
   
    servlet.onService(PortalContainer.getInstance(), request, response);

  }
  
  class FakeContentProvider implements FeedContentProvider {

    private String result;
    
    public FakeContentProvider(String result) {
      this.result = result;
    }

    public InputStream getFeedContent(String targetId) {
      return (result == null) ? null:  new ByteArrayInputStream(result.getBytes());
    }
    
  }
  
  
  @Test(expectedExceptions={IllegalArgumentException.class})
  public void testNullCheckPathInfo() {
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getPathInfo()).thenReturn(null);
    KSRSSServlet servlet = new KSRSSServlet();
    servlet.checkPathInfo(request);
  }
  
  @Test
  public void testCheckPathInfo() {
    KSRSSServlet servlet = new KSRSSServlet();
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    Mockito.when(request.getPathInfo()).thenReturn("/foo");
    assertEquals("foo",servlet.checkPathInfo(request));
    
    Mockito.when(request.getPathInfo()).thenReturn("/");
    assertEquals("",servlet.checkPathInfo(request));
  }

  @Test
  public void testExtractAppType() {
    KSRSSServlet servlet = new KSRSSServlet();
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    
    Mockito.when(request.getPathInfo()).thenReturn("/faq/categories");
    assertEquals("faq", servlet.extractAppType(request));
    
    Mockito.when(request.getPathInfo()).thenReturn("/faq/categories/Categoryf136732fc0a8000d00297cb37949b644");
    assertEquals("faq",servlet.extractAppType(request));
    Mockito.when(request.getPathInfo()).thenReturn("/forum/forumCategoryf138e67cc0a8000d00bb6b574c589a4c");
    assertEquals("forum",
                 servlet.extractAppType(request));
    Mockito.when(request.getPathInfo()).thenReturn("/forum/forumf13a2450c0a8000d00307a860ed10cbc");
    assertEquals("forum", servlet.extractAppType(request));
    
    Mockito.when(request.getPathInfo()).thenReturn("/forum/topicf13b3e2ac0a8000d001b09820ccd9c41");
    assertEquals("forum", servlet.extractAppType(request));

    Mockito.when(request.getPathInfo()).thenReturn("/root");
    assertEquals("", servlet.extractAppType(request));

  }

  @Test
  public void testExtractObjectId() {
    KSRSSServlet servlet = new KSRSSServlet();
    final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    
    Mockito.when(request.getPathInfo()).thenReturn("/faq/categories");
    assertEquals("categories", servlet.extractObjectId(request));
    
    Mockito.when(request.getPathInfo()).thenReturn("/faq/categories/Category001");
    assertEquals("categories/Category001", servlet.extractObjectId(request));

    Mockito.when(request.getPathInfo()).thenReturn("/forum/forumCategory001");
    assertEquals("forumCategory001", servlet.extractObjectId(request));
    
    Mockito.when(request.getPathInfo()).thenReturn("/forum/forum001");
    assertEquals("forum001", servlet.extractObjectId(request));
    
    Mockito.when(request.getPathInfo()).thenReturn("/forum/topic001");
    assertEquals("topic001", servlet.extractObjectId(request));
    
    Mockito.when(request.getPathInfo()).thenReturn("/root");
    assertEquals("root", servlet.extractObjectId(request));

  }


}
