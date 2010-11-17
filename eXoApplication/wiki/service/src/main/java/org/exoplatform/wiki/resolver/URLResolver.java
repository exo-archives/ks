package org.exoplatform.wiki.resolver;

import org.exoplatform.portal.config.model.PageNode;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.pom.config.Utils;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.wiki.service.WikiPageParams;

public class URLResolver extends Resolver{
  private OrganizationService orgSerivce ;
  public URLResolver(OrganizationService orgSerivce) throws Exception {
    this.orgSerivce = orgSerivce ;
  }
  public WikiPageParams extractPageParams(String requestURL, PageNode portalPageNode) throws Exception {
    WikiPageParams params = new WikiPageParams() ;
    String wikiPageName;
    if (portalPageNode == null) {
      wikiPageName = "wiki";
    } else {
      wikiPageName = portalPageNode.getUri();
    }
    String uri = extractURI(requestURL, wikiPageName) ; 
    if (uri == null) return params ;
    if(uri.indexOf("/") > 0) {
      String[] array = uri.split("/") ;      
      if(array[0].equals(PortalConfig.USER_TYPE)) {
        params.setType(PortalConfig.USER_TYPE)  ;
        if(array.length >= 3) {
          params.setOwner(array[1]);
          params.setPageId(array[2]);
        }else if(array.length == 2) {
          params.setOwner(array[1]);
          params.setPageId(WikiPageParams.WIKI_HOME);
        }        
      }else if(array[0].equals(PortalConfig.GROUP_TYPE)) {
        params.setType(PortalConfig.GROUP_TYPE)  ;
        String groupId = uri.substring(uri.indexOf("/") + 1) ;
        
        if(orgSerivce.getGroupHandler().findGroupById("/"+groupId) != null) {
          params.setOwner(groupId) ;
          params.setPageId(WikiPageParams.WIKI_HOME) ;
        }else {
          if(groupId.indexOf("/") > 0) {
            String pageId = groupId.substring(groupId.lastIndexOf("/")+ 1) ;
            String owner = groupId.substring(0, groupId.lastIndexOf("/")) ;
            params.setOwner(owner) ;
            if(pageId != null && pageId.length() > 0) params.setPageId(pageId) ;
            else params.setPageId(WikiPageParams.WIKI_HOME) ;
          }else {
            params.setOwner(groupId) ;
            params.setPageId(WikiPageParams.WIKI_HOME) ;
          }
        }
      } else if(array[0].equals(PortalConfig.PORTAL_TYPE)) {
        params.setType(PortalConfig.PORTAL_TYPE)  ;
        params.setOwner(extractPortalOwner(requestURL, wikiPageName)) ;
        params.setPageId(array[1]) ;
      }
    }else{
      if (portalPageNode != null && portalPageNode.getPageReference() != null
          && !portalPageNode.getPageReference().startsWith(PortalConfig.PORTAL_TYPE)) {
        String[] components = Utils.split("::", portalPageNode.getPageReference());
        params.setType(components[0]);
        params.setOwner(components[1].substring(components[1].lastIndexOf("/")));
      } else {
        params.setType(PortalConfig.PORTAL_TYPE);
        params.setOwner(extractPortalOwner(requestURL, wikiPageName));
      }
      if (uri.length() > 0)
        params.setPageId(uri);
      else
        params.setPageId(WikiPageParams.WIKI_HOME);
    }
    params.setPageId(TitleResolver.getObjectId(params.getPageId(), true, false));
    return params;
  }  
  
  private String extractURI(String url, String wikiPageName) throws Exception{
    String uri = null;
    String sign1 = "/" + wikiPageName + "/";
    String sign2 = "/" + wikiPageName;
    if(url.indexOf(sign1) < 0){
      if(url.indexOf(sign2) > 0) {
        uri = url.substring(url.indexOf(sign2) + sign2.length()) ;
      }      
    } else{
      uri = url.substring(url.indexOf(sign1) + sign1.length()) ;
    }
    
    if(uri != null && uri.length() > 0 && (uri.lastIndexOf("/") + 1) == uri.length()) 
      uri = uri.substring(0, uri.lastIndexOf("/")) ;
    return uri ;
  }
  
  private String extractPortalOwner(String url, String wikiPageName) throws Exception{
    String sign = "/" + wikiPageName;
    if(url.indexOf(sign) > 0){
      String temp = url.substring(0, url.indexOf(sign)) ;
      return temp.substring(temp.lastIndexOf("/") + 1) ;
    }
    return null ;
  }

}
