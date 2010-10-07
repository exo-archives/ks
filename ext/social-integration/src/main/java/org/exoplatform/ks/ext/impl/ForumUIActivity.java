package org.exoplatform.ks.ext.impl;

import java.util.Date;
import java.util.Map;


import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumAdministration;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.social.webui.activity.BaseUIActivity;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

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
       @EventConfig(listeners = BaseUIActivity.DeleteCommentActionListener.class, confirm = "UIActivity.msg.Are_You_Sure_To_Delete_This_Comment"),
       @EventConfig(listeners = ForumUIActivity.ToggleReplyActionListener.class),
       @EventConfig(listeners = ForumUIActivity.ReplyActionListener.class)
     }
)
public class ForumUIActivity extends BaseUIActivity {
  
  private boolean displayMoreInfo = false;
  
  /**
   * @return the displayMoreInfo
   */
  public boolean isDisplayMoreInfo() {
    return displayMoreInfo;
  }

  /**
   * @param displayMoreInfo the displayMoreInfo to set
   */
  public void setDisplayMoreInfo(boolean displayMoreInfo) {
    this.displayMoreInfo = displayMoreInfo;
  }

  public void doMore() throws Exception {
    ForumService service = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    
    if (this.getChildById("ReplyTitle" + getId()) == null) {
      UIFormStringInput replyTitle = new UIFormStringInput("ReplyTitle" + getId(),
                                                           "ReplyTitle",
                                                           "RE: "
                                                               + getActivityParamValue(ForumSpaceActivityPublisher.POST_NAME_KEY));
      replyTitle.setLabel("RE: " + getActivityParamValue(ForumSpaceActivityPublisher.POST_NAME_KEY));
      UIFormTextAreaInput replyContent = new UIFormTextAreaInput("ReplyContent" + getId(),
                                                                 "ReplyContent",
                                                                 null);
      addChild(replyTitle);
      addChild(replyContent);
    }
  }
  
  protected String getPostTitle() {
    return this.getUIStringInput("ReplyTitle" + getId()).getValue();
  }
  
  protected String getPostContent() {
    return this.getUIFormTextAreaInput("ReplyContent" + getId()).getValue();
  }
  
  protected void renderReplyTitle() throws Exception {
    this.renderChild("ReplyTitle" + getId());
  }
  
  protected void renderReplyContent() throws Exception {
    this.renderChild("ReplyContent" + getId());
  }
  
  public String getActivityParamValue(String key) {
    String value = null;
    Map<String, String> params = getActivity().getTemplateParams();
    if (params != null) {
      value = params.get(key);
    }

    return value != null ? value : "";
  }
  
  public static class ToggleReplyActionListener extends EventListener<ForumUIActivity> {

    @Override
    public void execute(Event<ForumUIActivity> event) throws Exception {
      ForumUIActivity uiform = event.getSource();
      WebuiRequestContext requestContext = event.getRequestContext();
      uiform.setDisplayMoreInfo(!uiform.isDisplayMoreInfo());
      uiform.doMore();
      requestContext.addUIComponentToUpdateByAjax(uiform);
    }
    
  }
  
  /*=========================================================================================///
   *  the following section is very bad code part. It is copied from Forum webapp
   .
    */
  private String[] getCensoredKeyword() throws Exception {
    ForumAdministration forumAdministration = getApplicationComponent(ForumService.class).getForumAdministration() ;
    String stringKey = forumAdministration.getCensoredKeyword();
    if(stringKey != null && stringKey.length() > 0) {
      stringKey = stringKey.toLowerCase().replaceAll(", ", ",").replaceAll(" ,", ",") ;
      if(stringKey.contains(",")){ 
        stringKey.replaceAll(";", ",") ;
        return stringKey.trim().split(",") ;
      } else { 
        return stringKey.trim().split(";") ;
      }
    }
    return new String[0];
  }
  
  public static boolean isEmpty(String str) {
    if(str == null || str.trim().length() == 0) return true ;
    else return false;
  }
  
  static String buildForumLink(String url, String selectedNode, String portalName, String type, String id) throws Exception { 
    if(url.indexOf(portalName) > 0) {
      if(url.indexOf(portalName + "/" + selectedNode) < 0){
        url = url.replaceFirst(portalName, portalName + "/" + selectedNode) ;
      }                 
    }
    selectedNode = portalName + "/" + selectedNode;
    url = url.substring(0, url.lastIndexOf(selectedNode)+selectedNode.length());
    StringBuilder link = new StringBuilder().append(url);
    if(!isEmpty(type) && !isEmpty(id)){
      if(link.lastIndexOf("/") == (link.length()-1)) link.append(type);
      else link.append("/").append(type);
      if(!id.equals(Utils.FORUM_SERVICE))link.append("/").append(id);
    }
    return link.toString();
  }
  
  public static String createdForumLink(String type, String id) throws Exception {
    PortalRequestContext pContext = (PortalRequestContext)Util.getPortalRequestContext();
    String url = pContext.getRequest().getRequestURL().toString();
    String selectedNode = Util.getUIPortal().getSelectedNode().getUri() ;
    //String portalName = "/" + Util.getUIPortal().getName() ;
    String portalName = pContext.getPortalOwner();
    return buildForumLink(url, selectedNode, portalName, type, id);
  }
  /* ================================================================================ */
  
  public static class ReplyActionListener extends EventListener<ForumUIActivity> {

    @Override
    public void execute(Event<ForumUIActivity> event) throws Exception {
      ForumUIActivity uiform = event.getSource();
      WebuiRequestContext context = event.getRequestContext();
      ForumService service = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      UIApplication uiapp = uiform.getAncestorOfType(UIApplication.class);
      String title = uiform.getPostTitle();
      String content = uiform.getPostContent();
      
      if (title == null || title.trim().length() == 0 || content == null || content.trim().length() == 0) {
        uiapp.addMessage(new ApplicationMessage("ForumUIActivity.msg.reply-empty", null, ApplicationMessage.WARNING));
        context.addUIComponentToUpdateByAjax(uiapp.getUIPopupMessages());
        return;
      }
      
      String link = uiform.createdForumLink("topic", uiform.getActivityParamValue(ForumSpaceActivityPublisher.TOPIC_ID_KEY)).replaceFirst("private", "public");
      
      StringBuffer buffer = new StringBuffer();
      for (int j = 0; j < content.length(); j++) {
        char c = content.charAt(j); 
        if((int)c == 9){
          buffer.append("&nbsp; &nbsp; ") ;
        } else if((int)c == 10){
          buffer.append("<br/>") ;
        } else if((int)c == 60){
          buffer.append("&lt;") ;
        } else if((int)c == 62){
          buffer.append("&gt;") ;
        } else if(c == '\''){
          buffer.append("&apos;") ;
        } else if(c == '&' || (int)c == 38){
          buffer.append("&#x26;");
        } else{
          buffer.append(c) ;
        }
      } 
      
      Post post = new Post();
      post.setName(title);
      post.setMessage(buffer.toString());
      post.setOwner(context.getRemoteUser());
      post.setCreatedDate(new Date());
      
      post.setRemoteAddr(org.exoplatform.ks.common.Utils.getRemoteIP());
      post.setLink(link);
      
      service.savePost(uiform.getActivityParamValue(ForumSpaceActivityPublisher.CATE_ID_KEY),
                       uiform.getActivityParamValue(ForumSpaceActivityPublisher.FORUM_ID_KEY), 
                       uiform.getActivityParamValue(ForumSpaceActivityPublisher.TOPIC_ID_KEY), post, true, 
                       "");
      uiform.setDisplayMoreInfo(false);
      
      
      uiapp.addMessage(new ApplicationMessage("ForumUIActivity.msg.reply-success", new String[] {uiform.getPostTitle()}));
      context.addUIComponentToUpdateByAjax(uiapp.getUIPopupMessages());
      context.addUIComponentToUpdateByAjax(uiform.getParent());
    }
    
  }
}
