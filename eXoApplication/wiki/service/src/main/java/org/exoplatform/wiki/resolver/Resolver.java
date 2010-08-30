package org.exoplatform.wiki.resolver;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.wiki.service.WikiPageParams;

public abstract class Resolver extends BaseComponentPlugin{
  
  public abstract WikiPageParams extractPageParams(String requestURL) throws Exception ;
  
}
