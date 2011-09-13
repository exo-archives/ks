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
package org.exoplatform.wiki.webui.control.action;

import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.datamodel.IllegalNameException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.ext.filter.UIExtensionFilter;
import org.exoplatform.webui.ext.filter.UIExtensionFilters;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.NameValidator;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiTemplateDescriptionContainer;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.action.core.AbstractFormActionComponent;
import org.exoplatform.wiki.webui.control.filter.IsEditAddTemplateModeFilter;
import org.exoplatform.wiki.webui.control.listener.UISubmitToolBarActionListener;
import org.exoplatform.wiki.webui.extension.UITemplateSettingForm;


/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * 9 Feb 2011  
 */
@ComponentConfig(
  template = "app:/templates/wiki/webui/control/action/SaveTemplateActionComponent.gtmpl",                   
  events = {
    @EventConfig(listeners = SaveTemplateActionComponent.SaveTemplateActionListener.class, phase = Phase.DECODE)
  }
)
public class SaveTemplateActionComponent extends UIComponent {

  public static final String                   ACTION   = "SaveTemplate";
  
  private static final Log log = ExoLogger.getLogger("wiki:SaveTemplateActionComponent");
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditAddTemplateModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  private boolean isNewMode() {
    return (WikiMode.ADDPAGE.equals(getAncestorOfType(UIWikiPortlet.class).getWikiMode()));
  }  

  private String getActionLink() throws Exception {
    return Utils.createFormActionLink(this, ACTION, ACTION);
  }
  
  private String getPageTitleInputId() {
    return UIWikiPageTitleControlArea.FIELD_TITLEINPUT;
  }

  public static class SaveTemplateActionListener
                                                extends
                                                UISubmitToolBarActionListener<SaveTemplateActionComponent> {
    @Override
    protected void processEvent(Event<SaveTemplateActionComponent> event) throws Exception {
      WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      WikiPageParams pageParams = Utils.getCurrentWikiPageParams();
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                                 .getUIStringInput();
      UIFormStringInput descriptionInput = pageEditForm.findComponentById(UIWikiTemplateDescriptionContainer.FIELD_DESCRIPTION);
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormSelectBox syntaxTypeSelectBox = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);
      try {
        NameValidator.validate(titleInput.getValue());
      } catch (IllegalNameException ex) {
        String msg = ex.getMessage();
        ApplicationMessage appMsg = new ApplicationMessage("WikiPageNameValidator.msg.EmptyTitle",
                                                           null,
                                                           ApplicationMessage.WARNING);
        if (msg != null) {
          Object[] arg = { msg };
          appMsg = new ApplicationMessage("WikiPageNameValidator.msg.Invalid-char",
                                          arg,
                                          ApplicationMessage.WARNING);
        }
        uiApp.addMessage(appMsg);
        event.getRequestContext().setProcessRender(true);
      }
      if (event.getRequestContext().getProcessRender()) {
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        Utils.redirect(pageParams, wikiPortlet.getWikiMode());
        return;
      }
      String title = titleInput.getValue().trim();
      String markup = (markupInput.getValue() == null) ? "" : markupInput.getValue();
      markup = markup.trim();
      String description = descriptionInput.getValue();
      String syntaxId = syntaxTypeSelectBox.getValue();
      String[] msgArg = { title };
      try {
        if (wikiPortlet.getWikiMode() == WikiMode.EDITTEMPLATE) {
          Template template = wikiService.getTemplatePage(pageParams, pageEditForm.getTemplateId());
          wikiService.modifyTemplate(pageParams, template, title, description, markup, syntaxId);          
        } else if (wikiPortlet.getWikiMode() == WikiMode.ADDTEMPLATE) {
          String templateId = TitleResolver.getId(title, false);
          boolean isExist = (wikiService.getTemplatePage(pageParams, templateId) != null);
          if (isExist) {
            log.error("The title '" + title + "' is already existing!");
            uiApp.addMessage(new ApplicationMessage("SavePageAction.msg.warning-page-title-already-exist",
                                                    null,
                                                    ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            return;
          }
          Template template = wikiService.createTemplatePage(title, pageParams);
          template.setDescription(description);
          template.getContent().setText(markup);
          template.setSyntax(syntaxId);
          template.setNonePermission();
          uiApp.addMessage(new ApplicationMessage("SaveTemplateAction.msg.Create-template-successfully",
                                                  msgArg,
                                                  ApplicationMessage.INFO));
        }
      } catch (Exception e) {
        log.error("An exception happens when saving the page with title:" + title, e);
        uiApp.addMessage(new ApplicationMessage("UIPageToolBar.msg.Exception",
                                                null,
                                                ApplicationMessage.ERROR));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
      } finally {
        UITemplateSettingForm uiTemplateSettingForm = wikiPortlet.findFirstComponentOfType(UITemplateSettingForm.class);
        if (uiTemplateSettingForm != null) {
          // Update template list
          uiTemplateSettingForm.initGrid();
        }
        Utils.redirect(pageParams, WikiMode.SPACESETTING);
        super.processEvent(event);
      }
    }
  }
}
