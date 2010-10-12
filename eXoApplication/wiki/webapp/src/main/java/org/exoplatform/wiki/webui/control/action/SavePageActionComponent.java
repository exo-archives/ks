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

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
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
import org.exoplatform.wiki.commons.Utils;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.rendering.RenderingService;
import org.exoplatform.wiki.resolver.PageResolver;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.webui.UIWikiPageControlArea;
import org.exoplatform.wiki.webui.UIWikiPageEditForm;
import org.exoplatform.wiki.webui.UIWikiPageTitleControlArea;
import org.exoplatform.wiki.webui.UIWikiPortlet;
import org.exoplatform.wiki.webui.UIWikiRichTextArea;
import org.exoplatform.wiki.webui.WikiMode;
import org.exoplatform.wiki.webui.control.filter.IsEditModeFilter;
import org.exoplatform.wiki.webui.control.listener.UIPageToolBarActionListener;
import org.xwiki.rendering.syntax.Syntax;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Apr 26, 2010  
 */
@ComponentConfig(
  events = {
    @EventConfig(listeners = SavePageActionComponent.SavePageActionListener.class, phase = Phase.DECODE)
  }
)
public class SavePageActionComponent extends UIComponent {

  public static final String ACTION = "SavePage";
  
  private static final Log log = ExoLogger.getLogger("wiki:SavePageActionComponent");
  
  private static final List<UIExtensionFilter> FILTERS = Arrays.asList(new UIExtensionFilter[] { new IsEditModeFilter() });

  @UIExtensionFilters
  public List<UIExtensionFilter> getFilters() {
    return FILTERS;
  }
  
  public static class SavePageActionListener extends UIPageToolBarActionListener<SavePageActionComponent> {
    @Override
    protected void processEvent(Event<SavePageActionComponent> event) throws Exception {
      PortalRequestContext prContext = Util.getPortalRequestContext();
      UIWikiPortlet wikiPortlet = event.getSource().getAncestorOfType(UIWikiPortlet.class);
      Utils.reloadWYSIWYGEditor(wikiPortlet);
      UIApplication uiApp = event.getSource().getAncestorOfType(UIApplication.class);
      UIWikiPageTitleControlArea pageTitleControlForm = wikiPortlet.findComponentById(UIWikiPageControlArea.TITLE_CONTROL);
      UIWikiPageEditForm pageEditForm = wikiPortlet.findFirstComponentOfType(UIWikiPageEditForm.class);
      UIWikiRichTextArea wikiRichTextArea = pageEditForm.getChild(UIWikiRichTextArea.class);
      UIFormStringInput titleInput = pageEditForm.getChild(UIWikiPageTitleControlArea.class)
                                                 .getUIStringInput();
      UIFormTextAreaInput markupInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_CONTENT);
      UIFormStringInput commentInput = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_COMMENT);
      UIFormSelectBox syntaxTypeSelectBox = pageEditForm.findComponentById(UIWikiPageEditForm.FIELD_SYNTAX);

      String title = titleInput.getValue().trim();
      if (wikiRichTextArea.isRendered()) {
        RenderingService renderingService = (RenderingService) PortalContainer.getComponent(RenderingService.class);
        String htmlContent = wikiRichTextArea.getUIFormTextAreaInput().getValue();
        String markupContent = renderingService.render(htmlContent,
                                                       Syntax.XHTML_1_0.toIdString(),
                                                       syntaxTypeSelectBox.getValue());
        markupInput.setValue(markupContent);
      }
      String markup = markupInput.getValue();
      try {
        String requestURL = Utils.getCurrentRequestURL();
        String ajaxRequestURL=  Utils.getCurrentAjaxRequestURL(wikiPortlet.getWikiMode());
        PageResolver pageResolver = (PageResolver) PortalContainer.getComponent(PageResolver.class);
        WikiService wikiService = (WikiService) PortalContainer.getComponent(WikiService.class);
        WikiPageParams pageParams = pageResolver.extractWikiPageParams(requestURL);
        Page page = pageResolver.resolve(requestURL);
        String newPageId = TitleResolver.getObjectId(title, false);
        if (wikiPortlet.getWikiMode() == WikiMode.EDITPAGE) {
          boolean isRenameHome = WikiNodeType.Definition.WIKI_HOME_NAME.equals(pageParams.getPageId())
              && !newPageId.equals(pageParams.getPageId());
          if (isRenameHome) {
            uiApp.addMessage(new ApplicationMessage("SavePageAction.msg.Can-not-rename-Wiki-Home",
                                                    null,
                                                    ApplicationMessage.WARNING));
            titleInput.setValue(WikiNodeType.Definition.WIKI_HOME_TITLE);
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            event.getRequestContext().addUIComponentToUpdateByAjax(titleInput);
            prContext.getResponse().sendRedirect(ajaxRequestURL);
            return;
          }
          wikiService.renamePage(pageParams.getType(),
                                 pageParams.getOwner(),
                                 page.getName(),
                                 newPageId,
                                 title);
          page.getContent().setText(markup);
          page.getContent().setComment(commentInput.getValue());
          page.getContent().setSyntax(syntaxTypeSelectBox.getValue());
          pageTitleControlForm.getUIFormInputInfo().setValue(title);

          if (!pageEditForm.getTitle().equals(title)) {
            page.getContent().setTitle(title);
            ((PageImpl) page).checkin();
            ((PageImpl) page).checkout();

            pageParams.setPageId(newPageId);
            Utils.redirectToNewPage(pageParams, URLEncoder.encode(newPageId, "UTF-8"));
          } else {
            ((PageImpl) page).checkin();
            ((PageImpl) page).checkout();
            // the following code line is necessary, otherwise url which is
            // generated from ajax post will be displayed in url bar of browser
            Utils.redirectToNewPage(pageParams, URLEncoder.encode(pageParams.getPageId(), "UTF-8"));
          }

        } else if (wikiPortlet.getWikiMode() == WikiMode.ADDPAGE) {
          if (wikiService.isExisting(pageParams.getType(),
                                     pageParams.getOwner(),
                                     TitleResolver.getObjectId(title, false))) {
            log.error("The title '" + title + "' is already existing!");
            uiApp.addMessage(new ApplicationMessage("SavePageAction.msg.warning-page-title-already-exist",
                                                    null,
                                                    ApplicationMessage.WARNING));
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
            prContext.getResponse().sendRedirect(ajaxRequestURL);
            return;
          }
          String sessionId = Util.getPortalRequestContext().getRequest().getSession(false).getId();
          Page draftPage = wikiService.getExsitedOrNewDraftPageById(null, null, sessionId);
          Page subPage = wikiService.createPage(pageParams.getType(),
                                                pageParams.getOwner(),
                                                title,
                                                page.getName());
          subPage.getContent().setText(markup);
          subPage.getContent().setSyntax(syntaxTypeSelectBox.getValue());
          ((PageImpl) subPage).getAttachments().addAll(((PageImpl) draftPage).getAttachments());
          ((PageImpl) draftPage).remove();
          ((PageImpl) subPage).checkin();
          ((PageImpl) subPage).checkout();
          wikiPortlet.changeMode(WikiMode.VIEW);
          String pageId = TitleResolver.getObjectId(title, false);
          Utils.redirectToNewPage(pageParams, URLEncoder.encode(pageId, "UTF-8"));
          return;
        }

      } catch (Exception e) {
        log.error("An exception happens when saving the page with title:" + title, e);
        uiApp.addMessage(new ApplicationMessage("UIPageToolBar.msg.Exception",
                                                null,
                                                ApplicationMessage.ERROR));
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages());
        return;
      }

      wikiPortlet.changeMode(WikiMode.VIEW);
      super.processEvent(event);
    }
  }
}
