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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.webui.UIWikiMaskWorkspace;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.control.filter.IsEditModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;
import org.exoplatform.wiki.webui.popup.UIWikiPagePreview;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = PreviewPageActionComponent.PreviewPageActionListener.class, phase = Phase.DECODE)
  }
)
public class PreviewPageActionComponent extends UIComponent {

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class PreviewPageActionListener extends UIPageToolBarActionListener<PreviewPageActionComponent> {
    @Override
    protected void processEvent(Event<PreviewPageActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiMaskWorkspace uiMaskWS = wikiPortlet.getChild(UIWikiMaskWorkspace.class);
      UIWikiPageEditForm wikiPageEditForm = event.getSource().getAncestorOfType(UIWikiPageEditForm.class);
      
      UIWikiPagePreview wikiPagePreview = uiMaskWS.createUIComponent(UIWikiPagePreview.class, null, null);
      UIWikiRichTextArea wikiRichTextArea = wikiPageEditForm.getChild(UIWikiRichTextArea.class);
      boolean isRichTextRendered = wikiRichTextArea.isRendered();
      if (isRichTextRendered) {
        String htmlContent = wikiRichTextArea.getUIFormTextAreaInput().getValue();
        Utils.feedDataForWYSIWYGEditor(wikiPageEditForm, htmlContent);
        String markupSyntax = wikiPageEditForm.getUIFormSelectBox(UIWikiPageEditForm.FIELD_SYNTAX).getValue();
        RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
        String markupContent = renderingService.render(htmlContent, Syntax.XHTML_1_0.toIdString(), markupSyntax);
        wikiPagePreview.renderWikiMarkup(markupContent, markupSyntax);
      } else {
        UIFormTextAreaInput markupInput = wikiPageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
        UIFormSelectBox syntaxTypeSelectBox = wikiPageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);
        String markup = markupInput.getValue();
        wikiPagePreview.renderWikiMarkup(markup, syntaxTypeSelectBox.getValue());
      }

      uiMaskWS.setUIComponent(wikiPagePreview);
      uiMaskWS.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      
      super.processEvent(event);
    }
  }
}
