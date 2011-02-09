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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Map.Entry;

import org.exoplatform.commons.utils.LazyPageList;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.core.UIPopupContainer;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.core.api.wiki.Template;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.service.search.TemplateSearchData;
import org.exoplatform.wiki.service.search.TemplateSearchResult;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiTemplateDescriptionContainer;
import org.exoplatform.wiki.webui.UIWikiTemplateGrid;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.UIWikiPortlet.PopupLevel;
import org.exoplatform.wiki.webui.bean.TemplateBean;
import org.exoplatform.wiki.webui.bean.WikiTemplateListAccess;


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
public class UITemplateSettingForm extends UIForm {
  
  public static final String    TEMPLATE_SEARCHBOX = "TemplateSeachBox";

  public static final String    TEMPLATE_GRID      = "UIWikiTemplateGrid";

  public static final String    TEMPLATE_ITER      = "TemplateIter";

  public static final int       ITEMS_PER_PAGE     = 8;
  
  public static final String    TEMPLATE_ID        = TemplateBean.ID;

  public static final String    TEMPLATE_NAME      = TemplateBean.TITLE;

  public static final String    DESCRIPTION        = TemplateBean.DESCRIPTION;

  public static final String    ACTION_ADD         = "AddTemplate";

  public static final String    ACTION_DELETE      = "DeleteTemplate";

  public static final String    ACTION_EDIT        = "EditTemplate";
  
  public static final String    ACTION_SEARCH      = "SearchTemplate";

  private static final String[] TEMPLATE_FIELD     = { TEMPLATE_NAME, DESCRIPTION };

  private static final String[] USER_ACTION        = { ACTION_EDIT, ACTION_DELETE };

  private UIGrid                grid;

  private WikiService           wService;
  
  private ResourceBundle        res;

  public UITemplateSettingForm() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    res = context.getApplicationResourceBundle();    
    addChild(new UIFormStringInput(TEMPLATE_SEARCHBOX,
                                   null,
                                   res.getString("UITemplateSettingForm.label.Search")));
    grid = addChild(UIWikiTemplateGrid.class, null, TEMPLATE_GRID);
    grid.configure(TEMPLATE_ID, TEMPLATE_FIELD, USER_ACTION);
    grid.getUIPageIterator().setId(TEMPLATE_ITER);
    grid.getUIPageIterator().setParent(this);
    wService = (WikiService) PortalContainer.getComponent(WikiService.class);
    initGrid();
  }

  private void initGrid() throws Exception {
    WikiPageParams params = Utils.getCurrentWikiPageParams();
    Iterator<Entry<String, Template>> iter = wService.getTemplates(params).entrySet().iterator();
    List<TemplateBean> listBean = new ArrayList<TemplateBean>();
    while (iter.hasNext()) {
      Entry<String, Template> entry = iter.next();
      Template template = entry.getValue();
      listBean.add(new TemplateBean(template.getName(),
                                    template.getContent().getTitle(),
                                    template.getDescription()));
    }
    LazyPageList<TemplateBean> lazylist = new LazyPageList<TemplateBean>(new WikiTemplateListAccess(listBean),
                                                                         ITEMS_PER_PAGE);
    grid.getUIPageIterator().setPageList(lazylist);
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
      UIFormSelectBox syntaxTypeSelectBox = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);
      UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
      String templateId = event.getRequestContext().getRequestParameter(OBJECTID);
      pageEditForm.setTemplateId(templateId);
      titleInput.setEditable(true);
      commentInput.setRendered(false);
      Template template = wservice.getTemplatePage(pageParams, templateId);
     
      titleInput.setValue(template.getContent().getTitle());
      descriptionInput.setValue(template.getDescription());
      pageEditForm.setTitle(template.getContent().getTitle());
      markupInput.setValue(template.getContent().getText());
      syntaxTypeSelectBox.setValue(template.getContent().getSyntax());     
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
      UIFormSelectBox syntaxTypeSelectBox = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);
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
      syntaxTypeSelectBox.setValue(currentDefaultSyntaxt);
      syntaxTypeSelectBox.setEnable(Utils.getCurrentPreferences()
                                         .getPreferencesSyntax()
                                         .getAllowMutipleSyntaxes());
      UIPopupContainer popupContainer = wikiPortlet.getPopupContainer(PopupLevel.L1);
      popupContainer.deActivate();
      wikiPortlet.changeMode(WikiMode.ADDTEMPLATE);
    }
  }
  

  static public class SearchTemplateActionListener extends EventListener<UITemplateSettingForm> {
    public void execute(Event<UITemplateSettingForm> event) throws Exception {
      UITemplateSettingForm form = event.getSource();
      WikiPageParams params = Utils.getCurrentWikiPageParams();
      UIFormStringInput searchbox = form.findComponentById(UITemplateSettingForm.TEMPLATE_SEARCHBOX);
      TemplateSearchData data = new TemplateSearchData(searchbox.getValue(),
                                                       params.getType(),
                                                       params.getOwner());
      List<TemplateSearchResult> results = form.wService.searchTemplate(data);
      List<TemplateBean> listBean = new ArrayList<TemplateBean>();
      for (int i = 0; i < results.size(); i++) {
        TemplateSearchResult result = results.get(i);
        listBean.add(new TemplateBean(result.getName(), result.getTitle(), result.getDescription()));
      }
      LazyPageList<TemplateBean> lazylist = new LazyPageList<TemplateBean>(new WikiTemplateListAccess(listBean),
                                                                           ITEMS_PER_PAGE);
      form.grid.getUIPageIterator().setPageList(lazylist);
      event.getRequestContext().addUIComponentToUpdateByAjax(form);
    }
  }
  
}
