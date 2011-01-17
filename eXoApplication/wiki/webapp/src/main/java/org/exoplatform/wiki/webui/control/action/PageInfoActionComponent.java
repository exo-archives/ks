package org.exoplatform.wiki.webui.control.action;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.webui.UIWikiPageInfo;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;

@ComponentConfig (
    template = "app:/templates/wiki/webui/action/PageInfoActionComponent.gtmpl",
    events = {
        @EventConfig(listeners = PageInfoActionComponent.PageInfoActionListener.class)
    }
)
public class PageInfoActionComponent extends UIComponent {
  public static class PageInfoActionListener extends UIPageToolBarActionListener<PageInfoActionComponent> {

    /* (non-Javadoc)
     * @see org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener#processEvent(org.exoplatform.webui.event.Event)
     */
    @Override
    protected void processEvent(Event<PageInfoActionComponent> event) throws Exception {
      event.getSource().getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.PAGEINFO);
    }
    
  }
}
