package org.exoplatform.wiki.webui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.commons.VersionNameComparatorDesc;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.webui.control.UIRelatedPagesContainer;
import org.exoplatform.wiki.webui.control.action.ShowHistoryActionListener;
import org.exoplatform.wiki.webui.control.action.ViewRevisionActionListener;
import org.exoplatform.wiki.webui.core.UIWikiContainer;
import org.xwiki.rendering.syntax.Syntax;

@ComponentConfig(
  template = "app:/templates/wiki/webui/UIWikiPageInfo.gtmpl",
  events = {
    @EventConfig(listeners = ViewRevisionActionListener.class),
    @EventConfig(listeners = ShowHistoryActionListener.class)
  }
)
public class UIWikiPageInfo extends UIWikiContainer {
  private static final Log log                     = ExoLogger.getLogger(UIWikiPageInfo.class);

  private static final int NUMBER_OF_SHOWN_CHANGES = 5;

  public UIWikiPageInfo() throws Exception {
    super();
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.PAGEINFO });
    addChild(UIRelatedPagesContainer.class, null, null);
  }

  List<NTVersion> getVersionList(Page page) {
    List<NTVersion> versions = new ArrayList<NTVersion>();
    try {
      PageImpl pageImpl = (PageImpl) page;
      Iterator<NTVersion> iter = pageImpl.getVersionableMixin().getVersionHistory().iterator();
      while (iter.hasNext()) {
        NTVersion version = iter.next();
        if (!(WikiNodeType.Definition.ROOT_VERSION.equals(version.getName()))) {
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

  String getPageLink(Page page) throws Exception {
    WikiPageParams params = org.exoplatform.wiki.utils.Utils.getWikiPageParams(page);
    return Utils.getURLFromParams(params);
  }

  String renderHierarchy() throws Exception {
    RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
    Utils.setUpWikiContext(getAncestorOfType(UIWikiPortlet.class));
    String content = renderingService.render("{{pagetree /}}",
                                             Syntax.XWIKI_2_0.toIdString(),
                                             Syntax.XHTML_1_0.toIdString(),
                                             false);
    Utils.removeWikiContext();
    return content;
  }
  
  protected Page getCurrentPage() throws Exception {
    return Utils.getCurrentWikiPage();
  }
}
