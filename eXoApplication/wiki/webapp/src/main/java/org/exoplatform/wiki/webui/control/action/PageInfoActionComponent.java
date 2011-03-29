package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.action.core.AbstractEventActionComponent;
import org.exoplatform.wiki.webui.control.filter.IsViewModeFilter;
import org.exoplatform.wiki.webui.control.listener.MoreContainerActionListener;

@ComponentConfig (
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",
  events = {
        @EventConfig(listeners = PageInfoActionComponent.PageInfoActionListener.class)
    }
)
public class PageInfoActionComponent extends AbstractEventActionComponent {
  
  public static final String                   ACTION  = "PageInfo";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsViewModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  @Override
  public String getActionName() {
    return ACTION;
  }

  @Override
  public boolean isAnchor() {
    return true;
  }
  
  public static class PageInfoActionListener extends MoreContainerActionListener<PageInfoActionComponent> {

    /* (non-Javadoc)
     * @see org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener#processEvent(org.exoplatform.webui.event.Event)
     */
    @Override
    protected void processEvent(Event<PageInfoActionComponent> event) throws Exception {
      event.getSource().getAncestorOfType(UIWikiPortlet.class).changeMode(WikiMode.PAGEINFO);
    }
    
  }

}
