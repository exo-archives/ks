/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.webui.extension;

import java.util.ResourceBundle;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiBottomArea;
import org.exoplatform.wiki.webui.UIWikiPageContainer;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.UIWikiSidePanelArea;
import org.exoplatform.wiki.webui.UIWikiTemplateDescriptionContainer;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.commons.UIWikiTemplateForm;


/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 28 Jan 2011  
 */

@ComponentConfig(
  lifecycle = UIFormLifecycle.class,
  template = "app:/templates/wiki/webui/extension/UITemplateSettingForm.gtmpl",
  events = {
    @EventConfig(listeners = UITemplateSettingForm.AddTemplateActionListener.class),
    @EventConfig(listeners = UITemplateSettingForm.EditTemplateActionListener.class),
    @EventConfig(listeners = UITemplateSettingForm.SearchTemplateActionListener.class),
    @EventConfig(listeners = UITemplateSettingForm.DeleteTemplateActionListener.class, confirm = "UITemplateSettingForm.label.DeleteConfirm")
  }
)
public class UITemplateSettingForm extends UIWikiTemplateForm {

  public static final String    ACTION_ADD         = "AddTemplate";

  public static final String    ACTION_DELETE      = "DeleteTemplate";

  public static final String    ACTION_EDIT        = "EditTemplate";
  
  public static final String    ACTION_SEARCH      = "SearchTemplate";

  private static final String[] USER_ACTION        = { ACTION_EDIT, ACTION_DELETE };

  public UITemplateSettingForm() throws Exception {
    super();
    grid.configure(TEMPLATE_ID, TEMPLATE_FIELD, USER_ACTION);
  }
  
  public ResourceBundle getRes() {
    return res;
  }

  public void setRes(ResourceBundle res) {
    this.res = res;
  }

  static public class EditTemplateActionListener extends EventListener<UITemplateSettingForm> {
    public void execute(Event<UITemplateSettingForm> event) throws Exception {
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                                 .getUIStringInput();
      UIFormStringInput descriptionInput = pageEditForm.findComponentById(UIWikiTemplateDescriptionContainer.FIELD_DESCRIPTION);
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
      String templateId = event.getRequestContext().getRequestParameter(OBJECTID);
      pageEditForm.setTemplateId(templateId);
      titleInput.setEditable(true);
      commentInput.setRendered(false);
      Template template = wservice.getTemplatePage(pageParams, templateId);
     
      titleInput.setValue(template.getTitle());
      descriptionInput.setValue(template.getDescription());
      pageEditForm.setTitle(template.getTitle());
      markupInput.setValue(template.getContent().getText());
      
      markupInput.setRendered(true);
      pageEditForm.getChild(UIWikiRichTextArea.class).setRendered(false);
      pageEditForm.getChild(UIWikiSidePanelArea.class).setRendered(true);
      pageEditForm.getAncestorOfType(UIWikiPageContainer.class).getChild(UIWikiBottomArea.class).setRendered(true);
      
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.deActivate();
      wikiPortlet.changeMode(WikiMode.EDITTEMPLATE);
    }
  }

  static public class DeleteTemplateActionListener extends EventListener<UITemplateSettingForm> {
    public void execute(Event<UITemplateSettingForm> event) throws Exception {
      UITemplateSettingForm form = event.getSource();
      WikiPageParams params = Utils.getCurrentWikiPageParams();
      String templateId = event.getRequestContext().getRequestParameter(OBJECTID);
      form.wService.deleteTemplatePage(params.getType(), params.getOwner(), templateId);
      form.initGrid();
      event.getRequestContext().addUIComponentToUpdateByAjax(form);
    }
  }

  static public class AddTemplateActionListener extends EventListener<UITemplateSettingForm> {
    public void execute(Event<UITemplateSettingForm> event) throws Exception {
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      WikiService wservice = (WikiService) PortalContainer.getComponent(WikiService.class);
      ResourceBundle res = event.getSource().getRes();
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                                 .getUIStringInput();
      UIFormStringInput descriptionInput = pageEditForm.findComponentById(UIWikiTemplateDescriptionContainer.FIELD_DESCRIPTION);
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
      String currentDefaultSyntaxt = Utils.getCurrentPreferences()
                                          .getPreferencesSyntax()
                                          .getDefaultSyntax();
      if (currentDefaultSyntaxt == null) {
        currentDefaultSyntaxt = wservice.getDefaultWikiSyntaxId();
      }
      titleInput.setValue(res.getString("UIWikiPageEditForm.label.SampleTemplateTitle"));
      descriptionInput.setValue(res.getString("UIWikiPageEditForm.label.Description"));
      titleInput.setEditable(true);
      commentInput.setRendered(false);
      markupInput.setValue("");
      String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
      wservice.createDraftNewPage(sessionId);
      markupInput.setRendered(true);
      pageEditForm.getChild(UIWikiRichTextArea.class).setRendered(false);
      pageEditForm.getChild(UIWikiSidePanelArea.class).setRendered(true);
      pageEditForm.getAncestorOfType(UIWikiPageContainer.class).getChild(UIWikiBottomArea.class).setRendered(true);
      
      wikiPortlet.changeMode(WikiMode.ADDTEMPLATE);
    }
  }
}
