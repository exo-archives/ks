package org.exoplatform.wiki.service.jcrext;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;

import org.apache.commons.chain.Context;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.command.action.Action;
import org.exoplatform.services.ext.action.InvocationContext;
import org.exoplatform.services.jcr.observation.ExtendedEvent;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.listener.PageWikiListener;
import org.exoplatform.wiki.utils.Utils;

/**
 * the class for executing {@link PageWikiListener} when having a trigger from jcr node.
 * <br>
 * The listeners will be invoked for adding page when property 'text' of {@link WikiNodeType#WIKI_CONTENT_ITEM} is added.
 * <br>
 * The listeners will be invoked for updating page when property 'text' of {@link WikiNodeType#WIKI_CONTENT_ITEM} is updated.  
 * @author exo
 *
 */
public class PageListenersInAction implements Action {
  
  private static final Log      log               = ExoLogger.getLogger(PageListenersInAction.class);
  
  @Override
  public boolean execute(Context context) throws Exception {
    Object currentItemObj = context.get(InvocationContext.CURRENT_ITEM);
    Object eventObj = context.get(InvocationContext.EVENT);
    ExoContainer container = (ExoContainer) context.get(InvocationContext.EXO_CONTAINER);
    
    
    if (!(currentItemObj instanceof Property)) {
      return false;
    }
    
    Property textProperty = (Property) currentItemObj;
    if (!WikiNodeType.Definition.TEXT.equals(textProperty.getName())) {
      return false;
    }
    
    Node ancestor = textProperty.getParent();
    if (!ancestor.isNodeType(WikiNodeType.WIKI_PAGE_CONTENT)) {
      return false;
    }
    
    ancestor = ancestor.getParent();
    if (!ancestor.isNodeType(WikiNodeType.WIKI_PAGE)) {
      return false;
    }
    
    if (ancestor.isNodeType(WikiNodeType.WIKI_TEMPLATE)) {
      return false;
    }
    
    if (log.isDebugEnabled()) {
      log.debug(String.format("Executing listener [%s] at item [%s] due to event code [%s]", toString(), textProperty.getPath(), eventObj));
    }
    
    WikiService wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
    String jcrPathOfPage = ancestor.getPath();
    String wikiType = "", owner = "";
    try {
      wikiType = Utils.getWikiType(jcrPathOfPage);
      owner = Utils.getSpaceIdByJcrPath(jcrPathOfPage);
    } catch (IllegalArgumentException ie) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("can not get wikiType and owner from [%s]", jcrPathOfPage), ie);
      }
      return false;
    }
    String pageId = ancestor.getName();
    boolean moreVersionsThan1 = false;
    try {
      
      moreVersionsThan1 = ancestor.getVersionHistory().getAllVersions().getSize() > 1;
    } catch (NullPointerException e) {
      if (log.isDebugEnabled()) {
        log.debug(String.format("can not count the number of versions of page [%s]", pageId), e);
      }
    }
    if (Integer.parseInt(eventObj.toString()) == ExtendedEvent.PROPERTY_ADDED) {
      List<PageWikiListener> listeners = wikiService.getPageListeners();
      for (PageWikiListener l : listeners) {
        try {
          l.postAddPage(wikiType, owner, pageId);
        } catch (Exception e) {
          if (log.isWarnEnabled()) {
            log.warn(String.format("executing listener [%s] at property [%s] failed", l.toString(), textProperty.getPath()), e);
          }
        }
      }
    } else if (Integer.parseInt(eventObj.toString()) == ExtendedEvent.PROPERTY_CHANGED && moreVersionsThan1) {
      List<PageWikiListener> listeners = wikiService.getPageListeners();
      for (PageWikiListener l : listeners) {
        try {
          l.postUpdatePage(wikiType, owner, pageId);
        } catch (Exception e) {
          if (log.isWarnEnabled()) {
            log.warn(String.format("executing listener [%s] at property [%s] failed", l.toString(), textProperty.getPath()), e);
          }
        }
      }
    }
    return false;
  }

}
