package org.exoplatform.ks.ext.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.portal.webui.portal.UIPortal;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;

@ComponentConfig (
    lifecycle = UIFormLifecycle.class,    
    template = "classpath:groovy/ks/social-integration/plugin/space/WikiUIActivity.gtmpl",
    events = {
      @EventConfig(listeners = BaseUIActivity.ToggleDisplayLikesActionListener.class),
      @EventConfig(listeners = BaseUIActivity.ToggleDisplayCommentFormActionListener.class),
      @EventConfig(listeners = BaseUIActivity.LikeActivityActionListener.class),
      @EventConfig(listeners = BaseUIActivity.SetCommentListStatusActionListener.class),
      @EventConfig(listeners = BaseUIActivity.PostCommentActionListener.class),
      @EventConfig(listeners = BaseUIActivity.DeleteActivityActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Activity"),
      @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment")
    }
)
public class WikiUIActivity extends BaseUIActivity {
  String getActivityParamValue(String key) {
    String value = null;
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      value = params.get(key);
    }

    return value != null ? value : "";
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
    String pageId = getActivityParamValue(WikiSpaceActivityPublisher.PAGE_ID_KEY);
    String typeId = getActivityParamValue(WikiSpaceActivityPublisher.PAGE_TYPE_KEY);
    String owner = getActivityParamValue(WikiSpaceActivityPublisher.PAGE_OWNER_KEY);
    StringBuilder sb = new StringBuilder();
    
    /*
     * As function Space.getUrl() does not return a absolute link but space name, 
     * a portal uri is used temporary to make link.   
     */
    PortalRequestContext portalRequestContext = Util.getPortalRequestContext();
    String portalURI = portalRequestContext.getPortalURI();
    sb.append(portalURI);
    
    
    sb.append(getActivityParamValue(WikiSpaceActivityPublisher.SPACE_URL_KEY));
    sb.append("/").append(getActivityParamValue(WikiSpaceActivityPublisher.PORTLET_NAME_KEY)).append("/");
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(typeId)) {
      sb.append(typeId.toLowerCase());
      sb.append("/");
      sb.append(org.exoplatform.wiki.utils.Utils.validateWikiOwner(typeId,
                                                                   owner));
      sb.append("/");
    }
    try {
      sb.append(URLEncoder.encode(pageId, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      return sb.toString();
    }
    return sb.toString();
  }
  
}
