/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.commons.WikiConstants;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.webui.UIWikiBottomArea;
import org.exoplatform.wiki.webui.UIWikiPageContainer;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.UIWikiSidePanelArea;
import org.exoplatform.wiki.webui.control.filter.IsEditAddModeFilter;
import org.exoplatform.wiki.webui.control.filter.IsEditAddPageModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIEditorTabsActionListener;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 31, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/RichTextActionComponent.gtmpl",               
  events = {
    @EventConfig(listeners = RichTextActionComponent.RichTextActionListener.class, phase = Phase.DECODE)
  }
)
public class RichTextActionComponent extends UIComponent {
  
  public static final String                   ACTION           = "RichText";

  public static final String                   RICHTEXT_LABEL   = "RichText";

  public static final String                   SOURCETEXT_LABEL = "SourceEditor";

  protected String                             label            = RICHTEXT_LABEL;

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] {
      new IsEditAddModeFilter(), new IsEditAddPageModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  @Override
  public void processRender(WebuiRequestContext context) throws Exception {
    UIWikiPortlet portlet = this.getAncestorOfType(UIWikiPortlet.class);
    String isMarkupMode = portlet.getUIExtContext().get(WikiConstants.IS_MARKUP).toString();
    if (isMarkupMode != null) {
      if (Boolean.valueOf(isMarkupMode)) {
        label = RICHTEXT_LABEL;
      } else {
        label = SOURCETEXT_LABEL;
      }
    }
    super.processRender(context);
  }

  public static class RichTextActionListener extends UIEditorTabsActionListener<RichTextActionComponent> {
    @Override
    protected void processEvent(Event<RichTextActionComponent> event) throws Exception {
      RichTextActionComponent component = event.getSource();
      UIWikiPageEditForm wikiPageEditForm = component.getAncestorOfType(UIWikiPageEditForm.class);
      UIWikiPageContainer pageCotainer = wikiPageEditForm.getAncestorOfType(UIWikiPageContainer.class);
      UIWikiBottomArea bottomArea = pageCotainer.getChild(UIWikiBottomArea.class);
      UIWikiRichTextArea wikiRichTextArea = wikiPageEditForm.getChild(UIWikiRichTextArea.class);
      UIWikiSidePanelArea wikiSidePanelArea = wikiPageEditForm.getChild(UIWikiSidePanelArea.class);
      boolean isSourceTextRendered = wikiRichTextArea.isRendered();
      wikiRichTextArea.setRendered(!isSourceTextRendered);
      wikiPageEditForm.getUIFormTextAreaInput(UIWikiPageEditForm.FIELD_CONTENT).setRendered(isSourceTextRendered);
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      if (isSourceTextRendered) {
        String htmlContent = wikiRichTextArea.getUIFormTextAreaInput().getValue();
        htmlContent = htmlContent == null ? StringUtils.EMPTY : htmlContent;
        String markupSyntax = Utils.getDefaultSyntax();
        String markupContent = renderingService.render(htmlContent, Syntax.XHTML_1_0.toIdString(), markupSyntax, false);
        wikiPageEditForm.getUIFormTextAreaInput(UIWikiPageEditForm.FIELD_CONTENT).setValue(markupContent);        
        wikiSidePanelArea.setRendered(true);
        bottomArea.setRendered(true);
      } else {
        Utils.feedDataForWYSIWYGEditor(wikiPageEditForm,null);
        wikiSidePanelArea.setRendered(false);
        bottomArea.setRendered(false);
      }
      super.processEvent(event);
      event.getRequestContext().addUIComponentToUpdateByAjax(pageCotainer.getAncestorOfType(UIWikiPortlet.class));
    }
  }
  
}
