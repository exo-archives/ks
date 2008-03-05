/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.crawler.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@exoplatform.com
 * Jul 26, 2006  
 */
@ComponentConfigs({
  @ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/groovy/crawler/webui/UICrawlerPortlet.gtmpl"
  ),
  @ComponentConfig(
    type = UIContainer.class,
    id = "UICrawlerWorkingArea",
    template = "app:/groovy/crawler/webui/UICrawlerWorkingArea.gtmpl"
  )
})
public class UICrawlerPortlet extends UIPortletApplication {
  final  public static String UI_WORKING_AREA_ID = "UICrawlerWorkingArea" ;
  
  public UICrawlerPortlet() throws Exception {
    addChild(UICrawlerNavigation.class, null, null) ;
    
    UIContainer uiContainer = addChild(UIContainer.class, UI_WORKING_AREA_ID, null) ;
    uiContainer.addChild(UICrawlerDescription.class, null, "UICrawlerDescription") ;
    uiContainer.addChild(UICategoryForm.class, null, "UICategoryForm").setRendered(false) ;
    uiContainer.addChild(UISourceForm.class, null, "UISourceForm").setRendered(false) ;
    uiContainer.addChild(UICrawlerNavigationPage.class, null, "UICrawlerNavigationPage").setRendered(false) ;
    uiContainer.addChild(UICrawlerContentList.class, null, "UICrawlerContentList").setRendered(false) ;
    uiContainer.addChild(UICrawlerLoadingConfig.class, null, "UICrawlerLoadingConfig").setRendered(false) ;
    uiContainer.addChild(UICrawlerLoadingInfo.class, null, "UICrawlerLoadingInfo").setRendered(false) ;
    uiContainer.addChild(UICrawlerLoadedNews.class, null, "UICrawlerLoadedNews").setRendered(false) ;
  }
}
