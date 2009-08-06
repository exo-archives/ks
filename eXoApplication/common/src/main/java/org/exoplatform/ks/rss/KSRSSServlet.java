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
package org.exoplatform.ks.rss;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai Van
 *					ha.mai@exoplatform.com
 * Jan 14, 2009, 8:58:11 AM
 */
public class KSRSSServlet extends HttpServlet {
	public void init(ServletConfig config) throws ServletException {}  
	public void service(HttpServletRequest request, HttpServletResponse response) 
          throws ServletException, IOException {
    response.setHeader("Cache-Control", "private max-age=600, s-maxage=120");
    String pathInfo = request.getPathInfo() ;
    SessionProvider sessionProvider = SessionProvider.createSystemProvider() ;
    try{
    	if(pathInfo != null && pathInfo.length() > 0){
	    	pathInfo = pathInfo.substring(1) ;
	      String appType = "";
	      String objectId = "" ;
	      if(pathInfo.indexOf("/") > 0){
	      	appType = pathInfo.substring(0, pathInfo.indexOf("/"));
	      	objectId = pathInfo.substring(pathInfo.indexOf("/") + 1);
	      }else objectId = pathInfo;
	      RSSProcess process = new RSSProcess(sessionProvider, appType);
	      InputStream is = null;
	      if(appType.equals("faq")) {
	      	//System.out.println("objectiD =======>" + objectId) ;
	      	is = process.getRSSNode(sessionProvider, objectId, appType) ;
		      response.setContentType("text/xml") ;
	      }else if(appType.equals("forum")) { 
	    		is = process.getRSSNode(sessionProvider, objectId, appType) ;
		      response.setContentType("text/xml") ;
	      }else{
	      	if(pathInfo.indexOf("/") > 0){
	      		objectId = pathInfo.substring(pathInfo.lastIndexOf("/") + 1) ;
	      	}else objectId = pathInfo;
	      	is = process.getRSSOfMultiObjects(objectId, sessionProvider);
	      }
	      if(is != null) {
	      	byte[] buf = new byte[is.available()];
	        is.read(buf);
	        ServletOutputStream os = response.getOutputStream();
	        os.write(buf);
	      }else {
	      	byte[] buf = ("This object is hidden or you haven't got permission to view!").getBytes();
	      	ServletOutputStream os = response.getOutputStream();
	        os.write(buf) ;
	      }
    	}
    }catch(Exception e) {
    	e.printStackTrace() ;
      //throw new ServletException(e) ;
    }finally {sessionProvider.close() ;}    		
	}  
}
