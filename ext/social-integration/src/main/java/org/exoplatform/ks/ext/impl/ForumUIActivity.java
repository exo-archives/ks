package org.exoplatform.ks.ext.impl;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.WebuiBindingContext;

@ComponentConfig (
     template = "classpath:groovy/ks/social-integration/plugin/space/ForumUIActivity.gtmpl"
)
public class ForumUIActivity extends BaseKSActivity {

  public ForumUIActivity() {
  }
  
  @SuppressWarnings("unused")
  private String getReplyLink() {
    String link = getViewLink();
    if (!link.endsWith("/"))
      link += "/";
    // add signal to show reply form
    link += "false";
    return link;
  }
  
  private String getViewLink() {
    String link = "";
    if (getActivityParamValue(ForumSpaceActivityPublisher.ACTIVITY_TYPE_KEY).toLowerCase().indexOf("topic") >= 0) {
      link = getActivityParamValue(ForumSpaceActivityPublisher.TOPIC_LINK_KEY);
    } else {
      link = getActivityParamValue(ForumSpaceActivityPublisher.POST_LINK_KEY) + "/" + getActivityParamValue(ForumSpaceActivityPublisher.POST_ID_KEY);
    }
    return link;
  }

  private String getLink(String tagLink, String nameLink) {
    tagLink = StringUtils.replace(tagLink, "{0}", getViewLink());
    return StringUtils.replace(tagLink, "{1}", nameLink);
  }
  
  private String getActivityContentTitle(WebuiBindingContext _ctx, String herf) throws Exception {
    String title = "", linkTag = "";
    try {
      if (getActivityParamValue(ForumSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(ForumSpaceActivityPublisher.ACTIVITYTYPE.AddPost.toString())) {
        title = _ctx.appRes("ForumUIActivity.label.add-post");
        linkTag = getLink(herf, getActivityParamValue(ForumSpaceActivityPublisher.POST_NAME_KEY));
      } else if (getActivityParamValue(ForumSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(ForumSpaceActivityPublisher.ACTIVITYTYPE.UpdatePost.toString())) {
        title = _ctx.appRes("ForumUIActivity.label.update-post");
        linkTag = getLink(herf, getActivityParamValue(ForumSpaceActivityPublisher.POST_NAME_KEY));
      } else if (getActivityParamValue(ForumSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(ForumSpaceActivityPublisher.ACTIVITYTYPE.AddTopic.toString())) {
        title = _ctx.appRes("ForumUIActivity.label.add-topic");
        linkTag = getLink(herf, getActivityParamValue(ForumSpaceActivityPublisher.TOPIC_NAME_KEY));
      } else if (getActivityParamValue(ForumSpaceActivityPublisher.ACTIVITY_TYPE_KEY).equalsIgnoreCase(ForumSpaceActivityPublisher.ACTIVITYTYPE.UpdateTopic.toString())) {
        title = _ctx.appRes("ForumUIActivity.label.update-topic");
        linkTag = getLink(herf, getActivityParamValue(ForumSpaceActivityPublisher.TOPIC_NAME_KEY));
      }
    } catch (Exception e) {
      log.debug("Failed to get activity content and title ", e);
    }
    if (!Utils.isEmpty(title)) {
      title = StringUtils.replace(title, "{0}", getUriOfAuthor());
      title = StringUtils.replace(title, "{1}", linkTag);
    }
    return title;
  }

}
