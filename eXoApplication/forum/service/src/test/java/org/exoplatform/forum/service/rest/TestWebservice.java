/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.forum.service.rest;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.RuntimeDelegate;

import org.exoplatform.forum.service.ws.ForumWebservice;
import org.exoplatform.services.rest.impl.ContainerResponse;
import org.exoplatform.services.rest.impl.MultivaluedMapImpl;
import org.exoplatform.services.rest.impl.RuntimeDelegateImpl;
import org.exoplatform.services.rest.tools.ByteArrayContainerResponseWriter;

/**
 * Created by The eXo Platform SARL Author : Volodymyr Krasnikov
 * volodymyr.krasnikov@exoplatform.com.ua
 */

public class TestWebservice extends AbstractResourceTest {
  ForumWebservice     forurumWebservice;

  static final String baseURI = "";

  public void setUp() throws Exception {
    RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    super.setUp();
    forurumWebservice = (ForumWebservice) container.getComponentInstanceOfType(ForumWebservice.class);
    registry(forurumWebservice);
  }

  public void tearDown() throws Exception {
    super.tearDown();
  }

  public void testCheckPublicRss() throws Exception {
    MultivaluedMap<String, String> h = new MultivaluedMapImpl();
    String username = "root";
    h.putSingle("username", username);
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "http", baseURI, h, null, writer);
    response = service("GET", "http", baseURI, h, null, writer);
    assertNotNull(response);
  }

  public void testGetLastpost() throws Exception {
    // MultivaluedMap<String, String> h = new MultivaluedMapImpl();

    String eventURI = "/ks/forum/getlastpost/5";
    ByteArrayContainerResponseWriter writer = new ByteArrayContainerResponseWriter();
    ContainerResponse response = service("GET", "http", baseURI, null, null, writer);

    assertNotNull(response);
    assertNotSame(Response.Status.NOT_FOUND, response.getStatus());
  }
}
