/**
 * 
 */
package org.exoplatform.webservice.ks.forum;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.common.http.HTTPMethods;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.Post;
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
  
  private String strQuery ;
  private List<Object> ipsToJson = new ArrayList<Object>();
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
    List<Object> lastMessages = new ArrayList<Object>() ;
    if(!list.isEmpty()) {
      for(Post post : list) {
        lastMessages.add(new MessageBean(post)) ;
      }
    }
    return Response.Builder.ok(new BeanToJsons(lastMessages), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }
  
  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/ks/forum/filter/{strIP}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response filterIps(@URIParam("strIP") String str) throws Exception {
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
    return Response.Builder.ok(new BeanToJsons(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/ks/forum/filterIpBanforum/{strForumId}/{strIP}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response filterIpBanForum(@URIParam("strForumId") String forumId, @URIParam("strIP") String str) throws Exception {
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
  	return Response.Builder.ok(new BeanToJsons(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }

  @HTTPMethod(HTTPMethods.GET)
  @URITemplate("/ks/forum/filterTagNameForum/{userAndTopicId}/{strTagName}/")
  @OutputTransformer(Bean2JsonOutputTransformer.class)
  public Response filterTagNameForum(@URIParam("strTagName") String str, @URIParam("userAndTopicId") String userAndTopicId) throws Exception {
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
  	return Response.Builder.ok(new BeanToJsons(ipsToJson), JSON_CONTENT_TYPE).cacheControl(cacheControl).build();
  }

}
