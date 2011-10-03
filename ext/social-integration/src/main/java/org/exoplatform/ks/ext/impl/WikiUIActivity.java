package org.exoplatform.ks.ext.impl;


import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;

@ComponentConfig (
    template = "classpath:groovy/ks/social-integration/plugin/space/WikiUIActivity.gtmpl"
)
public class WikiUIActivity extends BaseKSActivity {

  public WikiUIActivity() {
  }
  
  String getActivityMessage(WebuiBindingContext _ctx) throws Exception {
    String activityType = getActivityParamValue(WikiSpaceActivityPublisher.ACTIVITY_TYPE_KEY);
    if (activityType.equalsIgnoreCase(WikiSpaceActivityPublisher.ADD_PAGE_TYPE)) {
      return _ctx.appRes("WikiUIActivity.label.page-create");
    } else if (WikiSpaceActivityPublisher.UPDATE_PAGE_TYPE.equalsIgnoreCase(activityType)) {
      return _ctx.appRes("WikiUIActivity.label.page-update");
    }
    return "";
  }

  String getPageName() {
    return getActivityParamValue(WikiSpaceActivityPublisher.PAGE_TITLE_KEY);
  }

  String getPageURL() {
    return getActivityParamValue(WikiSpaceActivityPublisher.URL_KEY);
  }
  
  String getPageExcerpt(){
    return getActivityParamValue(WikiSpaceActivityPublisher.PAGE_EXCERPT);
  }

}
