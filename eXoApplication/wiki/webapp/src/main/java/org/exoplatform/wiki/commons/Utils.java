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
package org.exoplatform.wiki.commons;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.impl.SessionManager;
import org.exoplatform.wiki.webui.UIWikiPageArea;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.WikiMode;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 22, 2010  
 */
public class Utils {
  
  public static String getCurrentRequestURL() throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    HttpServletRequest request = portalRequestContext.getRequest();
    String requestURL = request.getRequestURL().toString();
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    if (!requestURL.contains(pageNodeSelected)) {
      // Happens at the first time processRender() called when add wiki portlet manually
      requestURL = portalRequestContext.getPortalURI() + pageNodeSelected;
    }
    return requestURL;
  }

  public static String getCurrentAjaxRequestURL(WikiMode mode) throws Exception {
    String requestURL = getCurrentRequestURL();
    String currentAction = Utils.getActionFromWikiMode(mode);
    requestURL = requestURL.concat('#' + currentAction);
    return requestURL;
  }

  public static WikiPageParams getCurrentWikiPageParams() throws Exception {
    String requestURL = getCurrentRequestURL();
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    WikiPageParams params = pageResolver.extractWikiPageParams(requestURL);
    HttpServletRequest request = Util.getPortalRequestContext().getRequest();
    Map<String, String[]> paramsMap = request.getParameterMap();
    Set<String> keys = paramsMap.keySet();
    for (String key : keys) {
      params.setParameter(key, paramsMap.get(key));
    }
    return params;
  }

  public static Page getCurrentWikiPage() throws Exception {
    String requestURL = Utils.getCurrentRequestURL();
    Page helpPage = isRenderFullHelpPage();
    if (helpPage != null) {
      return helpPage;
    }
    PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
    Page page = pageResolver.resolve(requestURL);
    return page;
  }
  
  public static Page getCurrentNewDraftWikiPage() throws Exception {
    WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
    return wikiService.getExsitedOrNewDraftPageById(null, null, sessionId);
  }
  
  public static String getDownloadLink(String path, String filename, DownloadService dservice){
    if(dservice == null)dservice = (DownloadService)PortalContainer.getComponent(DownloadService.class) ;
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    try {
      InputStream input = wservice.getAttachmentAsStream(path) ;      
      byte[] attBytes = null;
      if (input != null) {
        attBytes = new byte[input.available()];
        input.read(attBytes);
        ByteArrayInputStream bytearray = new ByteArrayInputStream(attBytes);
        MimeTypeResolver mimeTypeResolver = new MimeTypeResolver() ;
        String mimeType = mimeTypeResolver.getMimeType(filename) ;
        InputStreamDownloadResource dresource = new InputStreamDownloadResource(bytearray, mimeType);
        dresource.setDownloadName(filename);
        return dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      }
    } catch (Exception e) {     
    }
    return null;
  }
  
  public static String getExtension(String filename)throws Exception {
    MimeTypeResolver mimeResolver = new MimeTypeResolver() ;
    try{
      return mimeResolver.getExtension(mimeResolver.getMimeType(filename)) ;
    }catch(Exception e) {
      return mimeResolver.getDefaultMimeType() ;
    }    
  }
  
  public static Wiki getCurrentWiki() throws Exception {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    String wikiType=  Utils.getCurrentWikiPageParams().getType();
    String owner=  Utils.getCurrentWikiPageParams().getOwner();
    return store.getWiki(WikiType.valueOf(wikiType.toUpperCase()), owner);    
  }
    
  public static void setUpWikiContext(UIWikiPortlet wikiPortlet, RenderingService renderingService) throws Exception {
    Execution ec = ((RenderingServiceImpl) renderingService).getExecutionContext();
    if (ec.getContext() == null) {
      ec.setContext(new ExecutionContext());
      WikiContext wikiContext = getCurrentWikiContext(wikiPortlet);
      ec.getContext().setProperty(WikiContext.WIKICONTEXT, wikiContext);
    }
  }
  
  public static void removeWikiContext(RenderingService renderingService) throws Exception {
    Execution ec = ((RenderingServiceImpl) renderingService).getExecutionContext();
    if (ec != null) {
      ec.removeContext();
    }
  }
  
  public static void feedDataForWYSIWYGEditor(UIWikiPageEditForm pageEditForm, String xhtmlContent) throws Exception {
    UIWikiPortlet wikiPortlet = pageEditForm.getAncestorOfType(UIWikiPortlet.class);
    HttpSession session = Util.getPortalRequestContext().getRequest().getSession(false);
    if (xhtmlContent == null) {
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      String markupContent = pageEditForm.getUIFormTextAreaInput(UIWikiPageEditForm.FIELD_CONTENT).getValue();
      String markupSyntax = pageEditForm.getUIFormSelectBox(UIWikiPageEditForm.FIELD_SYNTAX).getValue();
      setUpWikiContext(wikiPortlet, renderingService);
      String htmlContent = renderingService.render(markupContent, markupSyntax, Syntax.ANNOTATED_XHTML_1_0.toIdString());
      removeWikiContext(renderingService);
      session.setAttribute(UIWikiRichTextArea.SESSION_KEY, htmlContent);
    } else {
      session.setAttribute(UIWikiRichTextArea.SESSION_KEY, xhtmlContent);
    }
    
    SessionManager sessionManager = (SessionManager) RootContainer.getComponent(SessionManager.class);
    sessionManager.addSessionContext(session.getId(), getCurrentWikiContext(wikiPortlet));
  }

  public static Page isRenderFullHelpPage() throws Exception {
    WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
    String helpaction = pageParams.getParameter(WikiContext.ACTION);
    String syntaxId = pageParams.getParameter("page");
    if (helpaction!=null&&syntaxId != null&&helpaction.equals("help")  ) {
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      PageImpl syntaxPage = wservice.getHelpSyntaxPage(syntaxId.replace("SLASH", "/").replace("DOT", "."));
      if (syntaxPage!=null)
      {
      PageImpl fullHelpPage= (PageImpl) syntaxPage.getChildPages().values().iterator().next();
      return fullHelpPage;
      }      
    }
    return null;
  }
  public static String getCurrentHierachyPagePath() throws Exception
  {
    String currentWikiName = getCurrentWiki().getOwner();
    String currentWikiPath = ((WikiImpl) getCurrentWiki()).getPath();
    String currentPagePath = ((PageImpl) getCurrentWikiPage()).getPath();    
    String prefixPath = getCurrentWikiPageParams().getType()+ "/" + currentWikiName;
    String result = currentPagePath.replace(currentWikiPath, prefixPath);    
    if (result.equals(prefixPath) ||result.equals(prefixPath +"/"+WikiNodeType.Definition.WIKI_HOME_NAME)) {
      return "";
    }   
    return result;
  }

  public static Preferences getCurrentPreferences() throws Exception {
    WikiImpl currentWiki = (WikiImpl) getCurrentWiki();
    return currentWiki.getPreferences();
  }
 
  public static void reloadWYSIWYGEditor(UIWikiPortlet wikiPortlet) {
    UIWikiPageArea wikiPageArea = wikiPortlet.getChild(UIWikiPageArea.class);
    UIWikiPageEditForm wikiPageEditForm = wikiPageArea.getChild(UIWikiPageEditForm.class);
    UIWikiRichTextArea wikiRichTextArea = wikiPageEditForm.getChild(UIWikiRichTextArea.class);
    wikiRichTextArea.setReloaded(false);
  }
 
  private static WikiContext getCurrentWikiContext(UIWikiPortlet wikiPortlet) throws Exception {
    //
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    UIPortal uiPortal = Util.getUIPortal();
    String portalURI = portalRequestContext.getPortalURI();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    //
    WikiContext wikiContext = new WikiContext();
    wikiContext.setPortalURI(portalURI);
    wikiContext.setPortletURI(pageNodeSelected);
    WikiPageParams params = Utils.getCurrentWikiPageParams();
    wikiContext.setType(params.getType());
    wikiContext.setOwner(params.getOwner());
    if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
      String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
      wikiContext.setPageId(sessionId);
    } else {
      wikiContext.setPageId(params.getPageId());
    }

    return wikiContext;
  }
  
  public static String getCurrentWikiNodeUri() throws Exception {    
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    StringBuilder sb = new StringBuilder(portalRequestContext.getPortalURI());
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    sb.append(pageNodeSelected);   
    return sb.toString();
  }  
  
  public static void redirectToNewPage(WikiPageParams currentPageParams, String newPageId) throws Exception {
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String portalURI = portalRequestContext.getPortalURI();
    UIPortal uiPortal = Util.getUIPortal();
    String pageNodeSelected = uiPortal.getSelectedNode().getUri();
    StringBuilder sb = new StringBuilder();
    sb.append(portalURI);
    sb.append(pageNodeSelected);
    sb.append("/");
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(currentPageParams.getType())) {
      sb.append(currentPageParams.getType().toLowerCase());
      sb.append("/");
      sb.append(org.exoplatform.wiki.utils.Utils.validateWikiOwner(currentPageParams.getType(),
                                                                   currentPageParams.getOwner()));
      sb.append("/");
    }
    sb.append(newPageId);
    portalRequestContext.setResponseComplete(true);
    portalRequestContext.sendRedirect(sb.toString());
  }
  
  public static String createFullRequestAction(String formId,
                                           String action,
                                           String componentId,
                                           String beanId) throws Exception {
    StringBuilder b = new StringBuilder();

    b.append("javascript:eXo.wiki.UIForm.submitPageEvent('").append(formId).append("','");
    b.append(action).append("','");
    b.append("&amp;").append(UIForm.SUBCOMPONENT_ID).append("=").append(componentId);
    if (beanId != null) {
      b.append("&amp;").append(UIComponent.OBJECTID).append("=").append(beanId);
    }
    b.append("')");
    return b.toString();
  }

  public static String getActionFromWikiMode(WikiMode mode) {
    switch (mode) {
    case EDITPAGE:
      return "EditPage";
    case ADDPAGE:
      return "AddPage";
    default:
      return "";
    }
  }

  public static WikiMode getWikiModeFromAction(String action) {
    return WikiMode.valueOf(action.toUpperCase());
  }
}
