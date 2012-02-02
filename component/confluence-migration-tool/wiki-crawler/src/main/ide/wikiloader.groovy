import javax.ws.rs.Path
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Consumes
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.PathParam
import javax.ws.rs.QueryParam
import javax.ws.rs.core.Response
import javax.ws.rs.FormParam

import java.util.Map;
import java.util.HashMap;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.api.Attachment;
import org.exoplatform.wiki.mow.api.Wiki;

//List wikis
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.api.WikiStore;
import org.exoplatform.wiki.resolver.TitleResolver;


import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.tree.utils.TreeUtils;

//upload
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.wiki.service.WikiResource;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.commons.utils.MimeTypeResolver;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.DiskFileUpload;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase;
import org.exoplatform.common.http.HTTPStatus;
import org.apache.commons.io.FilenameUtils;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.utils.Utils;

@Path("/wikiloader")
public class WikiLoader {

  public Map pages = new HashMap();
  
  private Page getPageByPath(String path) {
   WikiPageParams pageParam = TreeUtils.getPageParamsFromPath(path.replaceAll("//", "/"))
        
   WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class)
   Page page = (Page) wikiService.getPageById(pageParam.getType(), pageParam.getOwner(), pageParam.getPageId())

   return  page
  }
  
  @GET
  @Path("check")
  public Response checkPath(@QueryParam("path") String path) {
  if ( getPageByPath(path) != null)
     return Response.status(200).entity("OK : Page exists").build();
   return Response.status(404).entity("KO : Page not found").build();
  }

  @GET
  @Path("pageurl")
  @Produces("text/plain")
  public String getPageURL(@QueryParam("path") String path) {
     def page = getPageByPath(path);
     if (page.getName() != null) {
       return page.getName();
     }
     return "Page URL not Found";
  }
  
  @GET
  @Path("atturl")
  @Produces("text/plain")
  public String getAttachmentURL(@QueryParam("path") String path) {
   WikiPageParams pageParam = TreeUtils.getPageParamsFromPath(path.replaceAll("//", "/"))
     return "/upload/" + pageParam.getType() + "/" + pageParam.getOwner() + "/" + pageParam.getPageId();
  }
  
  @GET
  @Path("pages")
  public Response listPages(@QueryParam("path") String path) {
   def page = getPageByPath(path);
   def list = "";
   if ( page != null) {
      Map<String,PageImpl> childPages = page.getChildPages();
      childPages.values().each {
        list += it.getName() + "\n";
      }
      return Response.status(200).entity("Pages : \n" + list).build();
   }
   return Response.status(404).entity("KO : Page not found").build();
  }

  @GET
  @Path("create")
  public String createPage(@QueryParam("path") String path, @QueryParam("name") String pageName) {
   Page wikipage = null;
   
   //getLogger().info("Page creation requested : " + path + "/" + pageName);

   Page parentPage = getPageByPath(path)
   String creator = null;

   try {
   
    if (parentPage != null && parentPage.getWiki() != null) {
     // Check if not exists
     WikiPageParams pageParam = TreeUtils.getPageParamsFromPath(path.replaceAll("//", "/"))
     WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class)
     Page page = (Page) wikiService.getPageById(pageParam.getType(), pageParam.getOwner(), pageName)

     if (page == null) {
         // Create if not exists 
         wikipage = wikiService.createPage(pageParam.getType(), pageParam.getOwner(), pageName, parentPage.getName());
         wikipage.setSyntax("confluence/1.0");
         wikipage.getContent().setText("Page created by WikiLoader author " + wikipage.getAuthor() + ", parent author " + parentPage.getAuthor());
         ConversationState conversationState = ConversationState.getCurrent();
         if (conversationState != null && conversationState.getIdentity() != null) {
            creator = conversationState.getIdentity().getUserId();
         }
         if (creator != null)
            wikipage.setOwner(creator);
         return wikipage.getName();
     } else {
       
     }
     
     System.out.println("WikiLoaderService created page : " + path + "/" + pageName + " on demand of " + creator);
    }
   } catch (Exception e) {
     getLogger().error("Page creation failed : " + pageName + ", cause: " + e.getMessage());
   }
   
   return wikipage != null;
  }

  @POST
  @Path("content")
  public String setContent(@FormParam("path") String path, @FormParam("content") String content) {
   String message = "Please provide a valid path";
   if (path != null) {
      Page wikipage = getPageByPath(path)

      if (wikipage != null) {
         wikipage.getContent().setText(content);
      }   
      return "Content replaced in Page path";
    }
    return message;
  }

  @GET
  @Path("show")
  public String setContent(@QueryParam("path") String path) {
   Page wikipage = getPageByPath(path)

   if (wikipage != null) {
         return wikipage.getContent().getText();
   }
   
   return wikipage != null;
  }

  @GET
  @Path("list")
  public String listWikis() {
    def mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    def model = mowService.getModel();
    def wStore = (WikiStore) model.getWikiStore();
    def wikis = wStore.getWikis();
    
    def list = "";
    wikis.each {
      def path = it.getType() + "/" + it.getOwner() + "/" + it.getWikiHome().getName();
      path = path.replaceAll("//", "/");
      list += path + "\n";
    }
      
    //WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
    return list;
  }

    // save uploaded file to new location
  private void writeToFile(InputStream uploadedInputStream,
    String uploadedFileLocation) {
 
    try {
      OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
      int read = 0;
      byte[] bytes = new byte[1024];
 
      out = new FileOutputStream(new File(uploadedFileLocation));
      while ((read = uploadedInputStream.read(bytes)) != -1) {
        out.write(bytes, 0, read);
      }
      out.flush();
      out.close();
    } catch (IOException e) {
 
      e.printStackTrace();
    }
 
  }

  @POST
  @Path("/upload/{wikiType}/{wikiOwner:.+}/{pageId}/")
  public String upload(@PathParam("wikiType") String wikiType,
                         @PathParam("wikiOwner") String wikiOwner,
                         @PathParam("pageId") String pageId) {
    EnvironmentContext env = EnvironmentContext.getCurrent();
    HttpServletRequest req = (HttpServletRequest) env.get(HttpServletRequest.class);
    boolean isMultipart = FileUploadBase.isMultipartContent(req);

    if (isMultipart) {      
      DiskFileUpload upload = new DiskFileUpload();
      // Parse the request
      try {
        List<FileItem> items = upload.parseRequest(req);
        for (FileItem fileItem : items) {
          InputStream inputStream = fileItem.getInputStream();
          byte[] fileBytes;
          if (inputStream != null) {
            fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
          } else {
            fileBytes = null;
          }
          String fileName = fileItem.getName();
          String fileType = fileItem.getContentType();
          if (fileName != null) {
            // It's necessary because IE posts full path of uploaded files
            fileName = FilenameUtils.getName(fileName);
            fileType = FilenameUtils.getExtension(fileName);
          }

          String mimeType = new MimeTypeResolver().getMimeType(fileName);
          WikiResource attachfile = new WikiResource(mimeType, "UTF-8", fileBytes);
          attachfile.setName(fileName);
          if (attachfile != null) {
            WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
            Page page = wikiService.getPageById(wikiType, wikiOwner, pageId);
            if (page != null) {
              AttachmentImpl att = ((PageImpl) page).createAttachment(attachfile.getName(), attachfile);
              
              ConversationState conversationState = ConversationState.getCurrent();
              String creator = null;
              if (conversationState != null && conversationState.getIdentity() != null) {
                creator = conversationState.getIdentity().getUserId();
              }
              att.setCreator(creator);
              //Utils.reparePermissions(att);
              return "File uploaded : " + fileName;
            }
          }
        }
      } catch (Exception e) {
        return "Error:" + e.getMessage();
      }
    }
    return "Nothing done";
  }

}
