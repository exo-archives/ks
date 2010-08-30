package org.exoplatform.wiki.utils;

import java.util.Collection;
import java.util.Iterator;

import javax.jcr.Node;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.api.WikiType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.GroupWiki;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PortalWiki;
import org.exoplatform.wiki.mow.core.api.wiki.UserWiki;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;

public class Utils {
  
  private static final String JCR_WEBDAV_SERVICE_BASE_URI = "/jcr";
  
  //The path should get from NodeHierarchyCreator 
  public static String getPortalWikisPath() {    
    String path = "/exo:applications/" 
    + WikiNodeType.Definition.WIKI_APPLICATION + "/"
    + WikiNodeType.Definition.WIKIS ; 
    return path ;
  }
  
  public static String validateWikiOwner(String wikiType, String wikiOwner){
    if(wikiType.equals(PortalConfig.GROUP_TYPE)) {
      if(wikiOwner == null || wikiOwner.length() == 0){
        return null;
      }
      if(wikiOwner.startsWith("/")){
        wikiOwner = wikiOwner.substring(1,wikiOwner.length());
      }
      if(wikiOwner.endsWith("/")){
        wikiOwner = wikiOwner.substring(0,wikiOwner.length()-1);
      }
    }
    return wikiOwner;
  }
  
  public static String getDefaultRepositoryWebDavUri() {
    StringBuilder sb = new StringBuilder();
    sb.append("/");
    sb.append(PortalContainer.getCurrentPortalContainerName());
    sb.append("/");
    sb.append(PortalContainer.getCurrentRestContextName());
    sb.append(JCR_WEBDAV_SERVICE_BASE_URI);
    sb.append("/");
    RepositoryService repositoryService = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(RepositoryService.class);
    sb.append(repositoryService.getConfig().getDefaultRepositoryName());
    sb.append("/");
    return sb.toString();
  }
  
  public static void reparePermissions(AttachmentImpl att) throws Exception {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    Node attNode = (Node) store.getSession().getJCRSession().getItem(att.getPath());
    ExtendedNode extNode = (ExtendedNode) attNode;
    if (extNode.canAddMixin("exo:privilegeable"))
      extNode.addMixin("exo:privilegeable");
    String[] arrayPers = { PermissionType.READ };
    extNode.setPermission("any", arrayPers);
    attNode.getSession().save();
  }
  
  public static String getDocumentURL(WikiContext wikiContext) {
    StringBuilder sb = new StringBuilder();
    sb.append(wikiContext.getPortalURI());
    sb.append(wikiContext.getPortletURI());
    sb.append("/");
    if (!PortalConfig.PORTAL_TYPE.equalsIgnoreCase(wikiContext.getType())) {
      sb.append(wikiContext.getType().toLowerCase());
      sb.append("/");
      sb.append(Utils.validateWikiOwner(wikiContext.getType(), wikiContext.getOwner()));
      sb.append("/");
    }
    sb.append(wikiContext.getPageId());
    return sb.toString();
  }
  
  public static String getCurrentUser() {
    try {
      ConversationState conversationState = ConversationState.getCurrent();
      return conversationState.getIdentity().getUserId();
    }catch(Exception e){}
    return "system" ;
  }
  
  public static  Collection<Wiki> getWikisByType(WikiType wikiType)
  {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();   
    return store.getWikiContainer(wikiType).getAllWikis();     
  }
  
  public static WikiPageParams getPageParamsFromPath(String path) {  
    if (path == null) {
      return null;
    }
    WikiPageParams result = new WikiPageParams();
    path= path.trim();
    if (path.indexOf("/") < 0) {
      result.setType(path);
    } else {
      String[] array = path.split("/");
      if (array.length >= 2) {
        result.setType(array[0]);
        result.setOwner(array[1]);
        if (array.length >= 3) {
          result.setPageId(array[array.length - 1]);
        }
      }

    }
    return result;
  }
  
  public static String getPathFromPageParams(WikiPageParams param) {
    if (param.getType() != null & param.getOwner() != null && param.getPageId() != null)
      return param.getType() + "/" + param.getOwner() + "/" + param.getPageId();
    return null;
  }

  public static String getWikiType(Wiki wiki) {
    if (wiki instanceof PortalWiki) {
      return PortalConfig.PORTAL_TYPE;
    } else if (wiki instanceof GroupWiki) {
      return PortalConfig.GROUP_TYPE;
    } else if (wiki instanceof UserWiki) {
      return PortalConfig.USER_TYPE;
    } else {
      return null;
    }
  }
  
  public static Wiki[] getAllWikiSpace() {
    MOWService mowService = (MOWService) PortalContainer.getComponent(MOWService.class);
    WikiStoreImpl store = (WikiStoreImpl) mowService.getModel().getWikiStore();
    return store.getWikis().toArray(new Wiki[]{}) ;
  } 
  
  public static boolean isDescendantPage(PageImpl page, PageImpl parentPage )
  {
    Iterator<PageImpl> iter = parentPage.getChildPages().values().iterator();
    while (iter.hasNext()) {
      PageImpl childpage = (PageImpl) iter.next();
      if (childpage.equals(page))
        return true;
      return isDescendantPage(page,childpage);
    }
    return false;
  }

}
