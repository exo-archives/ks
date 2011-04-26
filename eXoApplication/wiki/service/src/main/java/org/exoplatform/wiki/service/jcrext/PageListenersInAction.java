package org.exoplatform.wiki.service.jcrext;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

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
  
  private static final String SIGN = "executedListeners";
  
  public enum PageEvent {PAGE_ADDED, PAGE_EDITED}
  
  @Override
  public boolean execute(Context context) throws Exception {
    if (context.containsKey(SIGN)) return false;
    Object currentItemObj = context.get(InvocationContext.CURRENT_ITEM);
    Object eventObj = context.get(InvocationContext.EVENT);
    boolean result;
    if ((currentItemObj instanceof Node) && Integer.parseInt(eventObj.toString()) == ExtendedEvent.NODE_ADDED) {
      result = processAddNode(context);
    } else if ((currentItemObj instanceof Property) && Integer.parseInt(eventObj.toString()) == ExtendedEvent.PROPERTY_CHANGED) {
      result = processChangeProperty(context);
    } else {
      throw new IllegalStateException("The listener is not configured properly!");
    }
    context.put(SIGN, true);
    return result;
  }
  
  private Node getPageNode(Node descendant) throws RepositoryException {
    Node pageNode = null;
    if (descendant.isNodeType(WikiNodeType.WIKI_PAGE)) {
      pageNode = descendant;
    }
    if (WikiNodeType.Definition.CONTENT.equals(descendant.getName())) {
      pageNode = descendant.getParent();
    }
    if (descendant.isNodeType("nt:resource")) {
      pageNode = (Node) descendant.getAncestor(descendant.getDepth() - 2);
      if (pageNode != null && !pageNode.isNodeType(WikiNodeType.WIKI_PAGE)) {
        // descendant is another resource but not content of wiki page.
        return null;
      }
    }
    if (pageNode == null) throw new IllegalStateException(String.format("Can not get wiki:page node from [%s]", descendant.getPath()));
    return pageNode;
  }
  
  private boolean executeListeners(Context context, Node pageNode, PageEvent event) throws RepositoryException {
    ExoContainer container = (ExoContainer) context.get(InvocationContext.EXO_CONTAINER);
    WikiService wikiService = (WikiService) container.getComponentInstanceOfType(WikiService.class);
    String pageJcrPath = pageNode.getPath();
    String wikiType, owner, pageId;
    try {
      wikiType = Utils.getWikiType(pageJcrPath);
      owner = Utils.getSpaceIdByJcrPath(pageJcrPath);
      pageId = pageNode.getName();
    } catch (IllegalArgumentException ie) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("can not get wikiType and owner from [%s]", pageJcrPath), ie);
      }
      return false;
    }
    List<PageWikiListener> listeners = wikiService.getPageListeners();
    for (PageWikiListener l : listeners) {
      try {
        if (event == PageEvent.PAGE_ADDED) 
          l.postAddPage(wikiType, owner, pageId, Utils.makeSimplePage(pageNode));
        else if (event == PageEvent.PAGE_EDITED) 
          l.postUpdatePage(wikiType, owner, pageId, Utils.makeSimplePage(pageNode));
      } catch (Exception e) {
        if (log.isWarnEnabled()) {
          log.warn(String.format("executing listener [%s] on [%s] failed", l.toString(), pageNode.getPath()), e);
        }
      }
    }
    return false;
  }
  
  private boolean processAddNode(Context context) throws RepositoryException {
    Object currentItemObj = context.get(InvocationContext.CURRENT_ITEM);
    Node currentNode = (Node) currentItemObj;
    Node pageNode = getPageNode(currentNode);
    if (pageNode == null) return false;
    return executeListeners(context, pageNode, PageEvent.PAGE_ADDED);
  }

  private boolean processChangeProperty(Context context) throws RepositoryException {
    Object currentItemObj = context.get(InvocationContext.CURRENT_ITEM);
    Property currentProperty = (Property) currentItemObj;
    
    Node pageNode = getPageNode(currentProperty.getParent());
    if (pageNode == null) return false;
    pageNode.getVersionHistory().getAllVersions().getSize();
    return executeListeners(context, pageNode, PageEvent.PAGE_EDITED);
  }
  
}
