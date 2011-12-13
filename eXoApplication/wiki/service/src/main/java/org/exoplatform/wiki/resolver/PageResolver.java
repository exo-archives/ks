package org.exoplatform.wiki.resolver;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;

public class PageResolver {
  
  private Log LOG = ExoLogger.getLogger(PageResolver.class);
  private WikiService wService ;
  private Resolver resolver ;
  
  public PageResolver (WikiService wService) {
    this.wService = wService ; 
  }
  
  public void setResolverPlugin(ComponentPlugin plugin) throws Exception {
    resolver = (Resolver)plugin ;
  }
  
  public WikiPageParams extractWikiPageParams(String requestURI, UserNode portalUserNode){
    try {
      WikiPageParams params;
      if (this.resolver != null) {
        params = this.resolver.extractPageParams(requestURI, portalUserNode);
        return params;
      } else {
        LOG.error("Couldn't extract WikiPageParams for URI: " + requestURI + ". ResolverPlugin is not set!");
        return new WikiPageParams();
      }
    } catch (Exception e) {
      LOG.warn("Couldn't extract WikiPageParams for URI: " + requestURI, e);
      return new WikiPageParams();
    }
  }
  
  public Page resolve(String requestURI, UserNode portalUserNode) throws Exception {
    WikiPageParams params = extractWikiPageParams(requestURI, portalUserNode);
    if (params.getType() == null) {
      LOG.warn("Couldn't resolve URI: " + requestURI);
      return null;
    }

    Page page = wService.getPageById(params.getType(), params.getOwner(), params.getPageId());
    if (LOG.isTraceEnabled()) {
      String message = String.format("Resolved URL: %s. Page %s is returned when providing Params[Type: %s, Owner: %s, PageId: %s]",
                                     requestURI,
                                     page,
                                     params.getType(),
                                     params.getOwner(),
                                     params.getPageId());
      LOG.trace(message);
    }

    return page;
  }
}
