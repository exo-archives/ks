/**
 * 
 */
package org.exoplatform.faq.service.ws;
import java.io.InputStream;

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
import org.exoplatform.ks.rss.FeedContentProvider;
import org.exoplatform.ks.rss.FeedResolver;
import org.exoplatform.services.rest.resource.ResourceContainer;




/**
 * @author Uoc Nguyen
 * 
 */
@Path("ks/faq")
public class FAQWebservice implements ResourceContainer {

  protected final static String JSON_CONTENT_TYPE = MediaType.APPLICATION_JSON;
  final public static String APP_TYPE = "faq".intern();
  public FAQWebservice() {}


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
      e.printStackTrace();
      return Response.status(Status.INTERNAL_SERVER_ERROR).build() ;
    }
  }
  

}
