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

  private int max_count  = 5;
  public ForumWebservice() {}
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/ks/forum/getmessage/{username}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response getMessage(@URIParam("username") String userName) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    JCRPageList pageList = forumService.getPrivateMessage(SessionProviderFactory.createSystemProvider(), userName, Utils.AGREEMESSAGE) ;
    List<MessageBean> lastMessages = new ArrayList<MessageBean>() ;
    if(pageList != null) {
      int count = 1 ;
      for(Object obj : pageList.getPage(1)) {
        if(count > max_count) break ;
        lastMessages.add(new MessageBean((ForumPrivateMessage)obj)) ;
        count ++ ;
      }
    }
    return Response.Builder.ok(new MessageListBean(lastMessages), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }

}
