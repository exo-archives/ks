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
package org.exoplatform.wiki.webui.popup;

import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIPopupComponent;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.commons.WikiConstants;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.webui.UIWikiMaskWorkspace;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.UIWikiTemplateDescriptionContainer;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.commons.UIWikiTemplateForm;
import org.exoplatform.wiki.webui.control.action.AddPageActionComponent;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieu.lai@exoplatform.com
 * Feb 10, 2011  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/wiki/webui/commons/UIWikiTemplateForm.gtmpl",
    events = {
    @EventConfig(listeners = UIWikiSelectTemplateForm.AddPageWithTemplateActionListener.class),
    @EventConfig(listeners = UIWikiSelectTemplateForm.PreviewTemplateActionListener.class),
    @EventConfig(listeners = UIWikiSelectTemplateForm.SearchTemplateActionListener.class) })
public class UIWikiSelectTemplateForm extends UIWikiTemplateForm implements UIPopupComponent {
 
  public static final String    ACTION_PREVIEW       = "PreviewTemplate";

  // Note this action is change mode one
  public static final String    ACTION_ADD           = AddPageActionComponent.ACTION + WikiConstants.WITH + "Template";

  public static final String    ACTION_SEARCH        = "SearchTemplate";

  public static final String    SELECT_TEMPLATE_ITER = "SelectTemplateIter";

  private static final String[] USER_ACTION          = { ACTION_ADD, ACTION_PREVIEW };
  
  public UIWikiSelectTemplateForm() throws Exception {
    super();
    grid.configure(TEMPLATE_ID, TEMPLATE_FIELD, USER_ACTION);
  }
  
  static public class AddPageWithTemplateActionListener extends EventListener<UIWikiSelectTemplateForm> {
    public void execute(Event<UIWikiSelectTemplateForm> event) throws Exception {
      UIWikiSelectTemplateForm form = event.getSource();
      UIWikiPortlet wikiPortlet = form.getAncestorOfType(UIWikiPortlet.class);
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                                 .getUIStringInput();
      UIFormStringInput descriptionInput = pageEditForm.findComponentById(UIWikiTemplateDescriptionContainer.FIELD_DESCRIPTION);
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
      String templateId = event.getRequestContext().getRequestParameter(OBJECTID);

      String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
      form.wService.createDraftNewPage(sessionId);
      titleInput.setReadOnly(false);
      commentInput.setRendered(false);
      Template template = form.wService.getTemplatePage(Utils.getCurrentWikiPageParams(),
                                                        templateId);     
      titleInput.setValue(template.getTitle());
      descriptionInput.setValue(template.getDescription());
      pageEditForm.setTitle(template.getTitle());
      markupInput.setValue(template.getContent().getText());
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.deActivate();
      wikiPortlet.changeMode(WikiMode.ADDPAGE);
    }
  }
  
  static public class PreviewTemplateActionListener extends EventListener<UIWikiSelectTemplateForm> {
    public void execute(Event<UIWikiSelectTemplateForm> event) throws Exception {
      UIWikiSelectTemplateForm form = event.getSource();
      UIWikiPortlet wikiPortlet = form.getAncestorOfType(UIWikiPortlet.class);
      UIWikiMaskWorkspace mask = wikiPortlet.findFirstComponentOfType(UIWikiMaskWorkspace.class);
      UIWikiPagePreview wikiPagePreview = mask.createUIComponent(UIWikiPagePreview.class,
                                                                 null,
                                                                 null);

      String templateId = event.getRequestContext().getRequestParameter(OBJECTID);
      Template template = form.wService.getTemplatePage(Utils.getCurrentWikiPageParams(),
                                                        templateId);
      AttachmentImpl content = template.getContent();
      wikiPagePreview.renderWikiMarkup(content.getText(), template.getSyntax());
      String pageTitle = template.getTitle();
      if (pageTitle != null) wikiPagePreview.setPageTitle(pageTitle);
      mask.setUIComponent(wikiPagePreview);
      mask.setShow(true);
      event.getRequestContext().addUIComponentToUpdateByAjax(mask);
    }
  }
  
  public void activate() throws Exception {

  }

  public void deActivate() throws Exception {

  }

}
