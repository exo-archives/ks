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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.listener.AddPageActionListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Feb 10, 2011  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/action/AddPageFromBlankActionComponent.gtmpl",
  events = {
     @EventConfig(listeners = AddPageFromBlankActionComponent.AddPageFromBlankActionListener.class)
  }
)
public class AddPageFromBlankActionComponent extends UIComponent {
  
  private static final String                  ADDPAGE_FROMBLANK = "AddPageFromBlank";

  public static final String                   ACTIONNAME        = "AddPage";
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class AddPageFromBlankActionListener extends AddPageActionListener<AddPageFromBlankActionComponent> {
    @Override
    protected void processEvent(Event<AddPageFromBlankActionComponent> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      Map<String, Object> uiExtensionContext = new HashMap<String, Object>();
      uiExtensionContext.put(UIWikiPortlet.class.getName(), wikiPortlet);
      processAddPageAction(uiExtensionContext);
      super.processEvent(event);
    }
  }

  public static void processAddPageAction(Map<String, Object> uiExtensionContext) throws Exception {
    WikiService wservice = (WikiService)PortalContainer.getComponent(WikiService.class) ;
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    ResourceBundle res = context.getApplicationResourceBundle() ;  
    
    UIWikiPortlet wikiPortlet = (UIWikiPortlet) uiExtensionContext.get(UIWikiPortlet.class.getName());    
    String pageTitle = (String) uiExtensionContext.get(WikiContext.PAGETITLE);
    UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
    UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                               .getUIStringInput();
    UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
    UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
    UIFormSelectBox syntaxTypeSelectBox = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);    
    titleInput.setValue(res.getString("UIWikiPageTitleControlArea.label.Untitled"));
    
    titleInput.setEditable(true);
    markupInput.setValue("");
    commentInput.setRendered(false);
    WikiService wikiService = wikiPortlet.getApplicationComponent(WikiService.class);
    String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
    wikiService.createDraftNewPage(sessionId);
    
    String currentDefaultSyntaxt = Utils.getCurrentPreferences().getPreferencesSyntax().getDefaultSyntax();
    if (currentDefaultSyntaxt == null) {
      currentDefaultSyntaxt = wservice.getDefaultWikiSyntaxId();
    }
    
    syntaxTypeSelectBox.setValue(currentDefaultSyntaxt);
    syntaxTypeSelectBox.setEnable(Utils.getCurrentPreferences().getPreferencesSyntax().getAllowMutipleSyntaxes());
    if (pageTitle != null && pageTitle.length() > 0) {
      titleInput.setValue(pageTitle);
      titleInput.setEditable(false);
    }

    UIWikiRichTextArea wikiRichTextArea = pageEditForm.getChild(UIWikiRichTextArea.class);
    if (wikiRichTextArea.isRendered()) {
      Utils.feedDataForWYSIWYGEditor(pageEditForm, null);
    }

    wikiPortlet.changeMode(WikiMode.ADDPAGE);
  }
  
}
