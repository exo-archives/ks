/**
 * 
 */
package org.exoplatform.forum.service.ws;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.resource.ResourceContainer;


/**
 * @author Vu Duy Tu
 * 
 */
@SuppressWarnings("unchecked")
@Path("ks/forum")
public class ForumWebservice implements ResourceContainer {

  protected final static String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON;
  final public static String APP_TYPE = "forum".intern();
  private String strQuery ;
  private List<BanIP> ipsToJson = new ArrayList<BanIP>();
  
  private static Log log = ExoLogger.getLogger(ForumWebservice.class);
  
  public ForumWebservice() {}

  private BeanToJsons<MessageBean> getNewPosts(String userName, int maxcount) throws Exception {
    ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
    List<Post> list = forumService.getRecentPostsForUser(userName, maxcount);
    List<MessageBean> lastMessages = new ArrayList<MessageBean>() ;
    if (list != null) {
      for (Post post : list) {
        post.setLink(post.getLink() + "/" + post.getId());
        lastMessages.add(new MessageBean(post)) ;
      }
    }
    return new BeanToJsons<MessageBean>(lastMessages);
  }
  
  private String getUserId(SecurityContext sc, UriInfo uriInfo) {
    try {
      return sc.getUserPrincipal().getName();
    } catch (NullPointerException e) {
      return getViewerId(uriInfo);
    } catch (Exception e) {
      log.debug("Failed to get user id", e);
      return null;
    }
  }
  
  private String getViewerId(UriInfo uriInfo) {
    URI uri = uriInfo.getRequestUri();
    String requestString = uri.getQuery();
    if (requestString == null) return null;
    String[] queryParts = requestString.split("&");
    for (String queryPart : queryParts) {
      if (queryPart.startsWith("opensocial_viewer_id")) {
        return queryPart.substring(queryPart.indexOf("=") + 1, queryPart.length());
      }
    }
    return null;
  }

  /**
   * The rest can gets response is recent posts for user and limited by number post.
   * 
   * @param maxcount is max number post for render in gadget
   * @param sc is SecurityContext for get userId login when we use rest link to render gadget.
   * @param uriInfo is UriInfo for get userId login when we render gadget via gadgets service
   * @return the response is json-data content list recent post for user.
   * @throws Exception the exception
   */
  @GET
  @Path("getmessage/{maxcount}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getMessage(@PathParam("maxcount") int maxcount, @Context SecurityContext sc,
                                                                  @Context UriInfo uriInfo) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
      String userName = getUserId(sc, uriInfo);
      BeanToJsons<MessageBean> data = getNewPosts(userName, maxcount);
      return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
    } catch (Exception e) {
      log.debug("Failed to get new post by user.");
      return Response.status(HTTPStatus.INTERNAL_ERROR).cacheControl(cacheControl).build();
    }
  }

  /**
   * The rest can gets response is recent public post limited by number post.
   * 
   * @param maxcount is max number post for render in gadget
   * @return the response is json-data content list recent public post.
   * @throws Exception the exception
   */
  @GET
  @Path("getpublicmessage/{maxcount}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPublicMessage(@PathParam("maxcount") int maxcount) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    BeanToJsons<MessageBean> data = getNewPosts(null, maxcount);
    return Response.ok(data, MediaType.APPLICATION_JSON).cacheControl(cacheControl).build();
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
  @Produces(MediaType.APPLICATION_XML)
  public Response viewrss(@PathParam("resourceid") String resourceid) throws Exception {
    CacheControl cacheControl = new CacheControl();
    cacheControl.setNoCache(true);
    cacheControl.setNoStore(true);
    try {
    	ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      InputStream is = forumService.createForumRss(resourceid, "http://www.exoplatform.com");
      return Response.ok(is, MediaType.APPLICATION_XML).cacheControl(cacheControl).build();
//      FeedResolver feedResolver = (FeedResolver) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FeedResolver.class);
//      FeedContentProvider provider = feedResolver.resolve(APP_TYPE);
//      InputStream is = provider.getFeedContent(resourceid);
//      return Response.ok(is, MediaType.TEXT_XML).cacheControl(cacheControl).build();
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
      ForumService forumService = (ForumService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ForumService.class);
      InputStream is = forumService.createUserRss(resourceid, "http://www.exoplatform.com");
      return Response.ok(is, MediaType.APPLICATION_XML).cacheControl(cacheControl).build();
      
//      FeedResolver feedResolver = (FeedResolver) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(FeedResolver.class);
//      FeedContentProvider provider = feedResolver.resolve("");
//      InputStream is = provider.getFeedContent(resourceid);
//      return Response.ok(is, MediaType.TEXT_XML).cacheControl(cacheControl).build();
    } catch (Exception e) {
      log.trace("\nGet UserRSS fail: ", e);
      return Response.status(Status.INTERNAL_SERVER_ERROR).build() ;
    }
  }

}
