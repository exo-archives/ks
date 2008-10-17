/**
 * 
 */
package org.exoplatform.webservice.ks.forum;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumPrivateMessage;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.MessageListBean;
import org.exoplatform.forum.service.MessageBean ;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.rest.CacheControl;
import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.OutputTransformer;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URIParam;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;
import org.exoplatform.ws.frameworks.json.transformer.Bean2JsonOutputTransformer;

/**
 * @author Uoc Nguyen
 * 
 */
public class ForumWebservice implements ResourceContainer {

  protected final static String JSON_CONTENT_TYPE = "application/json";

  public ForumWebservice() {}
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/ks/forum/getmessage/{maxcount}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getMessage(@URIParam("maxcount") String maxcount) throws Exception {
    int counter = 0 ;
    try {
      counter = Integer.parseInt(maxcount);
    } catch (Exception e) {
    }
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    List<Post> list = forumService.getNewPosts(counter) ;
    List<MessageBean> lastMessages = new ArrayList<MessageBean>() ;
    if(!list.isEmpty()) {
      for(Post post : list) {
        lastMessages.add(new MessageBean(post)) ;
      }
    }
    return Response.Builder.ok(new MessageListBean(lastMessages), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }

}
