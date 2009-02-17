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
package org.exoplatform.faq.webui;

import java.io.IOException;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.faq.service.FAQService;
import org.exoplatform.portal.webui.util.SessionProviderFactory;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai Van
 *					ha.mai@exoplatform.com
 * Jan 14, 2009, 8:58:11 AM
 */
public class IFAQServlet extends HttpServlet {
	public void init(ServletConfig config) throws ServletException {}  
	public void service(HttpServletRequest request, HttpServletResponse response) 
          throws ServletException, IOException {
    response.setHeader("Cache-Control", "private max-age=600, s-maxage=120");
    String pathInfo = request.getPathInfo() ;
    String[] arrayInfo = pathInfo.toString().split("/") ;
    Session session = null ;
    try{
      String portalName = arrayInfo[1] ;
      String categoryId = arrayInfo[2] ;
      PortalContainer pcontainer = getPortalContainer(portalName) ;
      PortalContainer.setInstance(pcontainer) ;
      FAQService faqService = (FAQService)pcontainer.getComponentInstanceOfType(FAQService.class) ;
      
      Node node = faqService.getRSSNode(SessionProviderFactory.createSystemProvider(), categoryId) ;
      if (node == null) throw new Exception("Node not found. ");
      session = node.getSession();
      response.setContentType("text/xml") ;
      InputStream is = node.getProperty("exo:content").getStream();
      byte[] buf = new byte[is.available()];
      is.read(buf);
      ServletOutputStream os = response.getOutputStream();
      os.write(buf);
    }catch(Exception e) {
      throw new ServletException(e) ;
    }finally{
      if(session != null) {
        session.logout() ;
        PortalContainer.setInstance(null) ;
      }
    }    		
	}  
  
  private  PortalContainer getPortalContainer(String portalName) {
    PortalContainer pcontainer =  RootContainer.getInstance().getPortalContainer(portalName) ;
    return pcontainer ;
  }
}
