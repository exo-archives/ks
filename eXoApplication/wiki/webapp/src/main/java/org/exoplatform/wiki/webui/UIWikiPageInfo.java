package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.web.application.Parameter;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.commons.VersionNameComparatorDesc;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.service.BreadcrumbData;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.core.UIWikiContainer;
import org.exoplatform.wiki.webui.popup.UIWikiSelectPageForm;
import org.xwiki.rendering.syntax.Syntax;

@ComponentConfig(template = "app:/templates/wiki/webui/UIWikiPageInfo.gtmpl", events = {
    @EventConfig(listeners = UIWikiPageInfo.ViewRevisionActionListener.class),
    @EventConfig(listeners = UIWikiPageInfo.AddRelatedPageActionListener.class),
    @EventConfig(listeners = UIWikiPageInfo.RemoveRelatedPageActionListener.class, confirm = "UIWikiPageInfo.msg.confirm-remove-rpage") })
public class UIWikiPageInfo extends UIWikiContainer {
  private static final Log log                     = ExoLogger.getLogger(UIWikiPageInfo.class);

  private static final int NUMBER_OF_SHOWN_CHANGES = 5;

  private UIWikiBreadCrumb breadcrumb;

  public UIWikiPageInfo() throws Exception {
    super();
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.PAGEINFO });
    breadcrumb = addChild(UIWikiBreadCrumb.class, null, "UIWikiBreadCrumb_PageInfo");
    breadcrumb.setLink(false);
  }

  public List<BreadcrumbData> getBreadcrumbDatas(Page page) {
    WikiService service = getApplicationComponent(WikiService.class);
    WikiPageParams params = org.exoplatform.wiki.utils.Utils.getWikiPageParams(page);
    try {
      return service.getBreadcumb(params.getType(), params.getOwner(), params.getPageId());
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("can not load BreadcrumbData for page [%s]", page.getName()), e);
      }
      return new ArrayList<BreadcrumbData>();
    }
  }

  List<NTVersion> getVersionList(Page page) {
    List<NTVersion> versions = new ArrayList<NTVersion>();
    try {
      PageImpl pageImpl = (PageImpl) page;
      Iterator<NTVersion> iter = pageImpl.getVersionableMixin().getVersionHistory().iterator();
      while (iter.hasNext()) {
        NTVersion version = iter.next();
        if (!("jcr:rootVersion".equals(version.getName()))) {
          versions.add(version);
        }
      }
      Collections.sort(versions, new VersionNameComparatorDesc());
      return versions.subList(0,
                              versions.size() > NUMBER_OF_SHOWN_CHANGES ? NUMBER_OF_SHOWN_CHANGES
                                                                       : versions.size());
    } catch (Exception e) {
      if (log.isWarnEnabled()) {
        log.warn(String.format("getting version list of page %s failed", page.getName()), e);
      }
    }
    return versions;
  }

  /**
   * get ajax link of {@link RemoveRelatedPageActionListener}
   * 
   * @param wikiParams
   * @return
   */
  String getRemovePageActionLink(WikiPageParams wikiParams) {
    Parameter[] params = new Parameter[] {
        new Parameter(RemoveRelatedPageActionListener.WIKI_TYPE, wikiParams.getType()),
        new Parameter(RemoveRelatedPageActionListener.PAGE_OWNER, wikiParams.getOwner()),
        new Parameter(RemoveRelatedPageActionListener.PAGE_ID, wikiParams.getPageId()) };
    try {
      return event("RemoveRelatedPage", null, params);
    } catch (Exception e) {
      if (log.isWarnEnabled())
        log.warn("getting Remove related page failed", e);
      return "";
    }
  }

  public Page getCurrentPage() throws Exception {
    return Utils.getCurrentWikiPage();
  }

  String getPageLink(Page page) throws Exception {
    WikiPageParams params = org.exoplatform.wiki.utils.Utils.getWikiPageParams(page);
    return Utils.getURLFromParams(params);
  }

  String renderHierarchy() throws Exception {
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    Utils.setUpWikiContext(getAncestorOfType(UIWikiPortlet.class), renderingService);
    return renderingService.render("{{pagetree /}}",
                                   Syntax.XWIKI_2_0.toIdString(),
                                   Syntax.XHTML_1_0.toIdString(),
                                   false);
  }

  static public class AddRelatedPageActionListener extends EventListener<UIWikiPageInfo> {
    @Override
    public void execute(Event<UIWikiPageInfo> event) throws Exception {
      UIWikiPageInfo uicomponent = event.getSource();
      UIWikiPortlet wikiPortlet = uicomponent.getAncestorOfType(UIWikiPortlet.class);
      UIWikiRelatedPages relatedCtn = null;
      if (wikiPortlet.getChild(UIWikiMiddleArea.class) != null) {
        relatedCtn = wikiPortlet.getChild(UIWikiMiddleArea.class).getChild(UIWikiRelatedPages.class);
      }
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      UIWikiSelectPageForm selectPageForm = popupContainer.activate(UIWikiSelectPageForm.class, 600);
      selectPageForm.addUpdatedComponent(uicomponent);
      if (relatedCtn != null) selectPageForm.addUpdatedComponent(relatedCtn);
      event.getRequestContext().addUIComponentToUpdateByAjax(popupContainer);
    }
  }

  static public class RemoveRelatedPageActionListener extends EventListener<UIWikiPageInfo> {

    static final String WIKI_TYPE  = "wikitype";

    static final String PAGE_OWNER = "owner";

    static final String PAGE_ID    = "pageid";

    @Override
    public void execute(Event<UIWikiPageInfo> event) throws Exception {
      WebuiRequestContext requestContext = event.getRequestContext();
      UIWikiPageInfo uicomponent = event.getSource();
      UIWikiPortlet wikiPortlet = uicomponent.getAncestorOfType(UIWikiPortlet.class);
      UIWikiRelatedPages relatedCtn = null;
      if (wikiPortlet.getChild(UIWikiMiddleArea.class) != null) {
        relatedCtn = wikiPortlet.getChild(UIWikiMiddleArea.class).getChild(UIWikiRelatedPages.class);
      }
      String wikiType = requestContext.getRequestParameter(WIKI_TYPE);
      String owner = requestContext.getRequestParameter(PAGE_OWNER);
      String pageId = requestContext.getRequestParameter(PAGE_ID);
      try {
        WikiPageParams relatedPageParams = new WikiPageParams(wikiType, owner, pageId);
        WikiService service = uicomponent.getApplicationComponent(WikiService.class);
        service.removeRelatedPage(Utils.getCurrentWikiPageParams(), relatedPageParams);
        if (relatedCtn != null) requestContext.addUIComponentToUpdateByAjax(relatedCtn);
      } catch (Exception e) {
        if (log.isWarnEnabled()) log.warn(String.format("can not remove related page [%s]", pageId), e);
        UIApplication application = uicomponent.getAncestorOfType(UIApplication.class);
        application.addMessage(new ApplicationMessage("can not remove this page", null, ApplicationMessage.WARNING));
        requestContext.addUIComponentToUpdateByAjax(application);
        
      }
      requestContext.addUIComponentToUpdateByAjax(uicomponent);
    }

  }

  static public class ViewRevisionActionListener extends EventListener<UIWikiPageInfo> {
    @Override
    public void execute(Event<UIWikiPageInfo> event) throws Exception {
      UIWikiHistorySpaceArea.viewRevision(event);
    }
  }

}
