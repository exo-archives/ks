package org.exoplatform.wiki.resolver;

import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.wiki.service.WikiPageParams;

public class URLResolver extends Resolver{
  private OrganizationService orgSerivce ;
  public URLResolver(OrganizationService orgSerivce) throws Exception {
    this.orgSerivce = orgSerivce ;
  }
  public WikiPageParams extractPageParams(String requestURL) throws Exception {
    WikiPageParams params = new WikiPageParams() ;
    String uri = extractURI(requestURL) ; 
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
        params.setOwner(extractPortalOwner(requestURL)) ;
        params.setPageId(array[1]) ;
      }
    }else{
      params.setType(PortalConfig.PORTAL_TYPE)  ;      
      params.setOwner(extractPortalOwner(requestURL)) ;
      if(uri.length() > 0) params.setPageId(uri) ;
      else params.setPageId(WikiPageParams.WIKI_HOME) ;
    }
    params.setPageId(TitleResolver.getObjectId(params.getPageId(), true));
    return params;
  }  
  
  private String extractURI(String url) throws Exception{
    String uri = null;
    if(url.indexOf("/wiki/") < 0){
      if(url.indexOf("/wiki") > 0) {
        uri = url.substring(url.indexOf("/wiki") + "/wiki".length()) ;
      }      
    } else{
      uri = url.substring(url.indexOf("/wiki/") + "/wiki/".length()) ;
    }
    
    if(uri != null && uri.length() > 0 && (uri.lastIndexOf("/") + 1) == uri.length()) 
      uri = uri.substring(0, uri.lastIndexOf("/")) ;
    return uri ;
  }
  
  private String extractPortalOwner(String url) throws Exception{
    if(url.indexOf("/wiki") > 0){
      String temp = url.substring(0, url.indexOf("/wiki")) ;
      return temp.substring(temp.lastIndexOf("/") + 1) ;
    }
    return null ;
  }

}
