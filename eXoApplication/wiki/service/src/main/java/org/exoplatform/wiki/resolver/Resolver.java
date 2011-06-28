package org.exoplatform.wiki.resolver;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.portal.mop.user.UserNode;
import org.exoplatform.wiki.service.WikiPageParams;

public abstract class Resolver extends BaseComponentPlugin{
  /**
   * 
   * @param requestURL URL of incoming request
   * @param portalUserNode page node that contains wiki portlet  
   * @return
   * @throws Exception
   */
  public abstract WikiPageParams extractPageParams(String requestURL, UserNode portalUserNode) throws Exception ;
  
}
