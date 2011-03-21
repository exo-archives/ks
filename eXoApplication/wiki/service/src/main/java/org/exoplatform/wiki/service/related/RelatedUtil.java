package org.exoplatform.wiki.service.related;

import java.util.ArrayList;
import java.util.List;

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
      String title = page.getTitle();
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
    return TreeUtils.getPageParamsFromPath(path);
  }
}
