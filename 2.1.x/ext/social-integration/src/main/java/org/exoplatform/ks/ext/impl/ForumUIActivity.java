package org.exoplatform.ks.ext.impl;

import java.util.Map;


import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

@ComponentConfig (
     lifecycle = UIFormLifecycle.class, 
     template = "classpath:groovy/ks/social-integration/plugin/space/ForumUIActivity.gtmpl",
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
public class ForumUIActivity extends BaseUIActivity {
  static private final Log LOG = ExoLogger.getLogger(ForumUIActivity.class);
  
  public String getUriOfAuthor() {
    String userId = getActivity().getUserId();
    try {
      return "<a href='" + getUserProfileUri(userId) + "'>" + getUserFullName(userId)  + "</a>";
    } catch (Exception e) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("can not get Url of user " + userId, e);
      }
      return "";
    }
  }

  private String getViewLink() {
    String link = "";
    if (getActivityParamValue(ForumSpaceActivityPublisher.ACTIVITY_TYPE_KEY).toLowerCase().indexOf("topic") >=0) {
      link = getActivityParamValue(ForumSpaceActivityPublisher.TOPIC_LINK_KEY);
    } else {
      link = getActivityParamValue(ForumSpaceActivityPublisher.POST_LINK_KEY) + "/" + getActivityParamValue(ForumSpaceActivityPublisher.POST_ID_KEY);
    }
    return link;
  }
  
  private String getReplyLink() {
    String link = getViewLink();
    if (!link.endsWith("/"))
      link += "/";
    // add signal to show reply form
    link += "false";
    return link;
  }
  
  public String getActivityParamValue(String key) {
    String value = null;
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      value = params.get(key);
    }

    return value != null ? value : "";
  }
 
  
}
