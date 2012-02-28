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
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.webui.control.UIEditorTabs;
import org.exoplatform.wiki.webui.control.UISubmitToolBar;
import org.exoplatform.wiki.webui.core.UIWikiForm;

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
      @EventConfig(listeners = UIWikiPageEditForm.CloseActionListener.class)      
  }  
)
public class UIWikiPageEditForm extends UIWikiForm {
  
  public static final String UNTITLED                  = "Untitled";

  public static final String FIELD_CONTENT             = "Markup";

  public static final String FIELD_COMMENT             = "Comment";
  
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
  
  static public class CloseActionListener extends EventListener<UIWikiPageEditForm> {
    @Override
    public void execute(Event<UIWikiPageEditForm> event) throws Exception {
      UIWikiSidePanelArea sidePanelForm = event.getSource().getChild(UIWikiSidePanelArea.class);
      sidePanelForm.setRendered(false);
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource());
    }
  }
}
