/**
 * 
 */
package org.exoplatform.forum.service.ws;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.conf.UpdateUserProfileJob;
import org.exoplatform.ks.rss.FeedContentProvider;
import org.exoplatform.ks.rss.FeedResolver;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;




/**
 * @author Uoc Nguyen
 * 
 */
@Path("ks/forum")
public class ForumWebservice implements ResourceContainer {

  protected final static String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON;
  final public static String APP_TYPE = "forum".intern();
  private String strQuery ;
  private List<BanIP> ipsToJson = new ArrayList<BanIP>();
  
  private static Log log = ExoLogger.getLogger(ForumWebservice.class);
  
  public ForumWebservice() {}

  @GET
  @Path("getmessage/{maxcount}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getMessage(@PathParam("maxcount") String maxcount) throws Exception {
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
    return Response.ok(new BeanToJsons<MessageBean>(lastMessages), MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
  }



  @GET
  @Path("filter/{strIP}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filterIps(@PathParam("strIP") String str) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    if(str.equals("all")){
      ipsToJson.clear() ;
      List<String> banIps = forumService.getBanList() ;
      for(String ip : banIps) {
        ipsToJson.add(new BanIP(ip)) ;
      }
    } else if(!str.equals(strQuery)){
      ipsToJson.clear() ;
      List<String> banIps = forumService.getBanList() ;
      for(String ip : banIps) {
        if(ip.startsWith(str)) ipsToJson.add(new BanIP(ip)) ;
      }
      strQuery = str ;
    }    
    return Response.ok(new BeanToJsons(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }

  @GET
  @Path("filterIpBanforum/{strForumId}/{strIP}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filterIpBanForum(@PathParam("strForumId") String forumId, @PathParam("strIP") String str) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    if(str.equals("all")){
      ipsToJson.clear() ;
      List<String> banIps = forumService.getForumBanList(forumId) ;
      for(String ip : banIps) {
        ipsToJson.add(new BanIP(ip)) ;
      }
    } else if(!str.equals(strQuery)){
      ipsToJson.clear() ;
      List<String> banIps = forumService.getForumBanList(forumId) ;
      for(String ip : banIps) {
        if(ip.startsWith(str)) ipsToJson.add(new BanIP(ip)) ;
      }
      strQuery = str ;
    }    
    return Response.ok(new BeanToJsons(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }

  @GET
  @Path("filterTagNameForum/{userAndTopicId}/{strTagName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response filterTagNameForum(@PathParam("strTagName") String str, @PathParam("userAndTopicId") String userAndTopicId) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    if(str.equals(" ")){
      ipsToJson.clear() ;
    } else if(str.equals("onclickForm")){
      ipsToJson.clear() ;
      List<String> banIps = forumService.getTagNameInTopic(userAndTopicId);
      for(String ip : banIps) {
        ipsToJson.add(new BanIP(ip)) ;
      }
    } else {
      ipsToJson.clear() ;
      List<String> banIps = forumService.getAllTagName(str, userAndTopicId);
      for(String ip : banIps) {
        if(ip.startsWith(str)) ipsToJson.add(new BanIP(ip)) ;
      }
    }    
    return Response.ok(new BeanToJsons(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }


  @GET
  @Path("rss/{resourceid}")
  @Produces(MediaType.TEXT_XML)
  public Response viewrss(@PathParam("resourceid") String resourceid) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      FeedResolver feedResolver = (FeedResolver) container.getComponentInstanceOfType(FeedResolver.class);
      FeedContentProvider provider = feedResolver.resolve(APP_TYPE);
      InputStream is = provider.getFeedContent(resourceid);
      return Response.ok(is, MediaType.TEXT_XML).cacheControl(cacheControl).build();
    } catch (Exception e) {
      log.trace("\nView RSS fail: " +  e.getMessage() + "\n" + e.getCause());
      return Response.status(Status.INTERNAL_SERVER_ERROR).build() ;
    }
  }
  
  @GET
  @Path("rss/user/{resourceid}")
  @Produces(MediaType.TEXT_XML)
  public Response userrss(@PathParam("resourceid") String resourceid) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      FeedResolver feedResolver = (FeedResolver) container.getComponentInstanceOfType(FeedResolver.class);
      FeedContentProvider provider = feedResolver.resolve("");
      InputStream is = provider.getFeedContent(resourceid);
      return Response.ok(is, MediaType.TEXT_XML).cacheControl(cacheControl).build();
    } catch (Exception e) {
      log.trace("\nGet UserRSS fail: ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build() ;
    }
  }

}
