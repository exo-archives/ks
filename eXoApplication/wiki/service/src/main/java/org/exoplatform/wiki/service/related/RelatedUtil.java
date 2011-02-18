package org.exoplatform.wiki.service.related;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.portal.config.model.PortalConfig;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.tree.utils.TreeUtils;
import org.exoplatform.wiki.utils.Utils;

public final class RelatedUtil {
  private RelatedUtil() {}
  
  public static List<JsonRelatedData> pageImplToJson(List<PageImpl> pages) {
    List<JsonRelatedData> jsonObjs = new ArrayList<JsonRelatedData>();
    for (PageImpl page : pages) {
      String name = page.getName();
      String title = page.getContent().getTitle();
      
      String path = TreeUtils.getPathFromPageParams(Utils.getWikiPageParams(page));
      JsonRelatedData dataObj = new JsonRelatedData(name, title, path);
      jsonObjs.add(dataObj);
    }
    return jsonObjs;
  }
  
  /**
   * convert wiki page info to path string. <br>
   * The format: [wiki type]/[wiki owner]/[page id]
   * @param params
   * @return
   */
  public static String getPath(WikiPageParams params) {
    StringBuilder sb = new StringBuilder();
    if (params.getType() != null) {
      sb.append(params.getType());
    }
    if (params.getOwner() != null) {
      sb.append("/").append(Utils.validateWikiOwner(params.getType(), params.getOwner()));
    }
    if (params.getPageId() != null) {
      sb.append("/").append(params.getPageId());
    }
    return sb.toString();
  }
  
  /**
   * get wiki page params from the path made by {@link #getPath(WikiPageParams)} 
   * @param path made by {@link #getPath(WikiPageParams)}
   * @throws Exception if an error occurs.
   */
  public static WikiPageParams getPageParams(String path) throws Exception {
    if (path == null) {
      return null;
    }
    WikiPageParams result = new WikiPageParams();
    path = path.trim();
    if (path.indexOf("/") < 0) {
      result.setType(path);
    } else {
      String[] array = path.split("/");
      result.setType(array[0]);
      if (array.length < 3) {
        result.setOwner(array[1]);
      } else if (array.length >= 3) {
        if (array[0].equals(PortalConfig.GROUP_TYPE)) {
          OrganizationService oService = (OrganizationService) ExoContainerContext.getCurrentContainer()
                                                                                  .getComponentInstanceOfType(OrganizationService.class);
          String groupId = path.substring(path.indexOf("/"));
          if (oService.getGroupHandler().findGroupById(groupId) != null) {
            result.setOwner(groupId);
          } else {
            result.setPageId(path.substring(path.lastIndexOf("/") + 1));
            String owner = path.substring(path.indexOf("/"), path.lastIndexOf("/"));
            while (oService.getGroupHandler().findGroupById(owner) == null) {
              owner = owner.substring(0,owner.lastIndexOf("/"));
            }
            result.setOwner(owner);
          }
        } else {
          // if (array[0].equals(PortalConfig.PORTAL_TYPE) || array[0].equals(PortalConfig.USER_TYPE))
          result.setOwner(array[1]);
          result.setPageId(array[array.length-1]);
        }
      }
    }
    return result;
  }
}
