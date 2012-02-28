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
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.webui.UIWikiMaskWorkspace;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.control.action.core.AbstractFormActionComponent;
import org.exoplatform.wiki.webui.control.filter.IsEditAddModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIEditorTabsActionListener;
import org.exoplatform.wiki.webui.popup.UIWikiPagePreview;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/AbstractActionComponent.gtmpl",          
  events = {
    @EventConfig(listeners = PreviewPageActionComponent.PreviewPageActionListener.class, phase = Phase.DECODE)
  }
)
public class PreviewPageActionComponent extends AbstractFormActionComponent {
  
  public static final String                   ACTION  = "PreviewPage";

  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditAddModeFilter() });

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
    return false;
  }

  @Override
  public boolean isSubmit() {
    return false;
  }
  
  public static class PreviewPageActionListener extends UIEditorTabsActionListener<PreviewPageActionComponent> {
    @Override
    protected void processEvent(Event<PreviewPageActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      ResourceBundle res = context.getApplicationResourceBundle() ;
      UIWikiMaskWorkspace uiMaskWS = wikiPortlet.getChild(UIWikiMaskWorkspace.class);
      UIWikiPageEditForm wikiPageEditForm = event.getSource().getAncestorOfType(UIWikiPageEditForm.class);
      UIWikiPagePreview wikiPagePreview = uiMaskWS.createUIComponent(UIWikiPagePreview.class, null, null);
      UIWikiRichTextArea wikiRichTextArea = wikiPageEditForm.getChild(UIWikiRichTextArea.class);
      UIWikiPageTitleControlArea wikiPageTitleArea = wikiPageEditForm.getChild(UIWikiPageTitleControlArea.class);
      String markupSyntax = Utils.getDefaultSyntax();
      boolean isRichTextRendered = wikiRichTextArea.isRendered();
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      String markup;
      if (isRichTextRendered) {
        String htmlContent = wikiRichTextArea.getUIFormTextAreaInput().getValue();        
        markup = renderingService.render(htmlContent,
                                         Syntax.XHTML_1_0.toIdString(),
                                         markupSyntax,
                                         false);       
        Utils.feedDataForWYSIWYGEditor(wikiPageEditForm, markup);
      } else {
        UIFormTextAreaInput markupInput = wikiPageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);        
        markup = (markupInput.getValue() == null) ? "" : markupInput.getValue();
      }
      wikiPagePreview.renderWikiMarkup(markup, markupSyntax);
      String pageTitle = wikiPageTitleArea.getTitle();
      if (pageTitle != null) wikiPagePreview.setPageTitle(wikiPageTitleArea.getTitle());
      else {
        wikiPagePreview.setPageTitle(res.getString("UIWikiPageTitleControlArea.label.Untitled"));
      }
      uiMaskWS.setUIComponent(wikiPagePreview);
      uiMaskWS.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMaskWS);
      
      super.processEvent(event);
    }
  }
}
