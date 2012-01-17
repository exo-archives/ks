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
package org.exoplatform.wiki.webui;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Preferences;
import org.exoplatform.wiki.mow.core.api.wiki.WikiImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.rendering.impl.RenderingServiceImpl;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.control.UISubmitToolBar;
import org.exoplatform.wiki.webui.control.UIEditorTabs;
import org.exoplatform.wiki.webui.core.UISyntaxSelectBoxFactory;
import org.exoplatform.wiki.webui.core.UIWikiForm;
import org.xwiki.context.Execution;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * May 14, 2010  
 */
@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/UIWikiPageEditForm.gtmpl",
  events = {
      @EventConfig(listeners = UIWikiPageEditForm.SelectSyntaxActionListener.class),
      @EventConfig(listeners = UIWikiPageEditForm.CloseActionListener.class)      
  }  
)
public class UIWikiPageEditForm extends UIWikiForm {

  public static final String UNTITLED                  = "Untitled";

  public static final String FIELD_CONTENT             = "Markup";

  public static final String FIELD_COMMENT             = "Comment";

  public static final String FIELD_SYNTAX              = "SyntaxType";

  public static final String TITLE_CONTROL             = "UIWikiPageTitleControlForm_PageEditForm";

  public static final String EDITOR_TABS               = "UIEditorTabs";

  public static final String SUBMIT_TOOLBAR_UPPER      = "UISubmitToolBarUpper";

  public static final String SUBMIT_TOOLBAR_BOTTOM     = "UISubmitToolBarBottom";

  public static final String HELP_PANEL                = "UIWikiSidePanelArea";

  public static final String RICHTEXT_AREA             = "UIWikiRichTextArea";

  public static final String FIELD_TEMPLATEDESCTIPTION = "UIWikiTemplateDescriptionContainer";
  
  private boolean            isTemplate        = false;

  private String             templateId        = StringUtils.EMPTY;

  private String             title;
  
  public static final String CLOSE = "Close";
  
  public UIWikiPageEditForm() throws Exception {
    this.accept_Modes = Arrays.asList(new WikiMode[] { WikiMode.EDITPAGE, WikiMode.ADDPAGE,
        WikiMode.EDITTEMPLATE, WikiMode.ADDTEMPLATE });
    addChild(UIWikiPageTitleControlArea.class, null, TITLE_CONTROL).toInputMode();
    addChild(UISubmitToolBar.class, null, SUBMIT_TOOLBAR_UPPER);
    addChild(UIWikiTemplateDescriptionContainer.class, null, FIELD_TEMPLATEDESCTIPTION);
    addChild(UIEditorTabs.class, null, EDITOR_TABS);
    addChild(UISubmitToolBar.class, null, SUBMIT_TOOLBAR_BOTTOM);
    addChild(UIWikiSidePanelArea.class, null, HELP_PANEL);
    addChild(UIWikiRichTextArea.class, null, RICHTEXT_AREA).setRendered(false);
    UIFormTextAreaInput markupInput = new UIFormTextAreaInput(FIELD_CONTENT, FIELD_CONTENT, "");
    markupInput.setHTMLAttribute("title", getLabel(FIELD_CONTENT));
    addUIFormInput(markupInput);
    UIFormStringInput commentInput = new UIFormStringInput(FIELD_COMMENT, FIELD_COMMENT, "");
    addUIFormInput(commentInput);
    UIFormSelectBox selectSyntax = UISyntaxSelectBoxFactory.newInstance(FIELD_SYNTAX, FIELD_SYNTAX);
    selectSyntax.setOnChange("SelectSyntax");
    this.addChild(selectSyntax);
  }
  
  public void setTitle(String title){ this.title = title ;}
  public String getTitle(){ return title ;}
  
  public boolean isTemplate() {
    return isTemplate;
  }

  public void setTemplate(boolean isTemplate) {
    this.isTemplate = isTemplate;
  }

  public String getTemplateId() {
    return templateId;
  }

  public void setTemplateId(String templateId) {
    this.templateId = templateId;
  }

  public boolean isSidePanelRendered(){
    return getChild(UIWikiSidePanelArea.class).isRendered();
  }
  
  public void reloadSyntax() throws Exception {
    WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
    WikiMode currentMode = getCurrentMode();
    UIFormSelectBox syntaxTypeSelectBox = getUIFormSelectBox(FIELD_SYNTAX);
    Preferences currentPreferences = ((WikiImpl) Utils.getCurrentWiki()).getPreferences();
    boolean allowSelect = currentPreferences.getPreferencesSyntax().getAllowMutipleSyntaxes();
    syntaxTypeSelectBox.setEnable(allowSelect);
    if (currentMode.equals(WikiMode.ADDPAGE)) {
      String currentDefaultSyntaxt = currentPreferences.getPreferencesSyntax().getDefaultSyntax();
      if (currentDefaultSyntaxt == null) {
        currentDefaultSyntaxt = wservice.getDefaultWikiSyntaxId();
      }
      syntaxTypeSelectBox.setValue(currentDefaultSyntaxt);
    }
  }
  
  public static class SelectSyntaxActionListener extends EventListener<UIWikiPageEditForm> {
    @Override
    public void execute(Event<UIWikiPageEditForm> event) throws Exception {
      UIFormSelectBox selectBox = event.getSource().getChildById(FIELD_SYNTAX);
      RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
      Execution ec = ((RenderingServiceImpl) renderingService).getExecution();
      if (ec.getContext() != null) {
        WikiContext wikiContext = (WikiContext) ec.getContext().getProperty(WikiContext.WIKICONTEXT);
        wikiContext.setSyntax(selectBox.getValue());
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
  
  static public class CloseActionListener extends EventListener<UIWikiPageEditForm> {
    @Override
    public void execute(Event<UIWikiPageEditForm> event) throws Exception {
      UIWikiSidePanelArea sidePanelForm = event.getSource().getChild(UIWikiSidePanelArea.class);
      sidePanelForm.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
}
