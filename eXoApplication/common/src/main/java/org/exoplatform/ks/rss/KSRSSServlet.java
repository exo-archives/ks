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
package org.exoplatform.ks.rss;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.web.AbstractHttpServlet;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SARL Author : Ha Mai Van ha.mai@exoplatform.com
 * Jan 14, 2009, 8:58:11 AM
 */
@SuppressWarnings("serial")
public class KSRSSServlet extends AbstractHttpServlet {

  private static Log LOG = ExoLogger.getLogger(KSRSSServlet.class);

  private FeedResolver feedResolver;
  
  public void afterInit(ServletConfig config) throws ServletException {
  }

  public void onService(ExoContainer container,
                        HttpServletRequest request,
                        HttpServletResponse response) throws ServletException, IOException {
    
    response.setHeader("Cache-Control", "private max-age=600, s-maxage=120");

 
    String objectId = extractObjectId(request);
    String appType = extractAppType(request);

    FeedContentProvider provider = resolveFeedContentProvider(container, appType);
    if (provider == null) {
      throw new ServletException("'" + appType + "' is not a recognized application");
    }

    InputStream is = provider.getFeedContent(objectId);
    byte[] buf = readStream(is);
    if (buf == null) {
      throw new ServletException("The feed '" + objectId + "' is not available");
    }
    response.setContentType("text/xml");
    ServletOutputStream os = response.getOutputStream();
    os.write(buf);

  }

  String extractAppType(HttpServletRequest request) {
    final String pathInfo = checkPathInfo(request);
    int idx = pathInfo.indexOf("/");
    String appType = (idx <= 0) ? "" : pathInfo.substring(0, idx); 
    return appType;
  }

  String checkPathInfo(HttpServletRequest request) {
    String pathInfo = request.getPathInfo();
    if (pathInfo == null || pathInfo.length() == 0) {
      throw new IllegalArgumentException("Invalid null path in URL");
    }
    pathInfo = pathInfo.substring(1);
    return pathInfo;
  }

  String extractObjectId(HttpServletRequest request) {
    final String pathInfo = checkPathInfo(request);
    int idx = pathInfo.indexOf("/");
    String objectId = (idx <= 0) ? pathInfo : pathInfo.substring(idx + 1);
    return objectId;
  }

  private FeedContentProvider resolveFeedContentProvider(ExoContainer container, String appType) {
    FeedResolver resolver = getFeedResolver();
    FeedContentProvider provider = resolver.resolve(appType);
    return provider;
  }

  private byte[] readStream(InputStream is) throws IOException {
    byte[] buf = null;
    if (is != null) {
      buf = new byte[is.available()];
      is.read(buf);
    }
    return buf;
  }

  public FeedResolver getFeedResolver() {
    if (feedResolver == null) {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      feedResolver = (FeedResolver) container.getComponentInstanceOfType(FeedResolver.class);
    }
    return feedResolver;
  }

  public void setFeedResolver(FeedResolver feedResolver) {
    this.feedResolver = feedResolver;
  }
}
