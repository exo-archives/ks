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
package org.exoplatform.wiki.webui;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.impl.SessionManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;

import com.google.gwt.user.client.rpc.IncompatibleRemoteServiceException;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 23, 2010  
 */
public class WikiRemoteServiceServlet extends RemoteServiceServlet {

  /**
   * Field required by all {@link java.io.Serializable} classes.
   */
  private static final long serialVersionUID = 1911611911891893986L;

  /**
   * {@inheritDoc}
   * 
   * @see RemoteServiceServlet#processCall(String)
   */
  public String processCall(String payload) throws SerializationException {
    
    String result;
    PortalContainer portalContainer;
    SessionManager sessionManager;
    String sessionId = getThreadLocalRequest().getSession(false).getId();
    try {
      sessionManager = (SessionManager) RootContainer.getComponent(SessionManager.class);
      portalContainer = RootContainer.getInstance().getPortalContainer(sessionManager.getSessionContainer(sessionId));
    } catch (Exception e) {
      return RPC.encodeResponseForFailure(null, e);
    }
    ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
    ExoContainerContext.setCurrentContainer(portalContainer);

    RequestLifeCycle.begin(portalContainer);

    try {
      RPCRequest req = RPC.decodeRequest(payload, null, this);
      RenderingServiceImpl renderingService = (RenderingServiceImpl) portalContainer.getComponentInstanceOfType(RenderingService.class);
      WikiContext wikiContext = (WikiContext) sessionManager.getSessionContext(sessionId);
      Execution ec = ((RenderingServiceImpl) renderingService).getExecution();
      if (ec.getContext() == null) {
        ec.setContext(new ExecutionContext());
        ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
      }
      RemoteService service = (RemoteService) renderingService.getComponent(req.getMethod().getDeclaringClass());
      result = RPC.invokeAndEncodeResponse(service, req.getMethod(), req.getParameters(), req.getSerializationPolicy());      
    } catch (IncompatibleRemoteServiceException ex) {
      log("IncompatibleRemoteServiceException in the processCall(String) method.", ex);
      result = RPC.encodeResponseForFailure(null, ex);
    } catch (Exception e) {
      log("Exception in the processCall(String) method.", e);
      result = RPC.encodeResponseForFailure(null, e);
    } finally {
      ExoContainerContext.setCurrentContainer(oldContainer);
      RequestLifeCycle.end();
    }

    return result;
  }

}
