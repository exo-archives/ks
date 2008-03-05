
/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.crawler.webui ;
import org.exoplatform.webui.config.annotation.ComponentConfig ;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Dung Ha
 *          ha.pham@exoplatform.com
 * Jul 26, 2006  
 */
@ComponentConfig(
  template = "app:/groovy/crawler/webui/UICrawlerLoadingInfo.gtmpl"
)
public class UICrawlerLoadingInfo extends UIComponent {
  
  public UICrawlerLoadingInfo() throws Exception {
    
  }
}

