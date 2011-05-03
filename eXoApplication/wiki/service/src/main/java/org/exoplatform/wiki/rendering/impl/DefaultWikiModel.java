/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.wiki.rendering.impl;

import java.util.Map;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.service.WikiPageParams;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.context.Execution;
import org.xwiki.context.ExecutionContext;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReferenceResolver;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.rendering.listener.reference.ResourceReference;
import org.xwiki.rendering.listener.reference.ResourceType;
import org.xwiki.rendering.wiki.WikiModel;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Nov
 * 5, 2009
 */
@Component
public class DefaultWikiModel implements WikiModel {

  @Requirement
  private ComponentManager componentManager;
  
  @Requirement
  private Execution execution;
  
  private static final Log LOG = ExoLogger.getLogger(DefaultWikiModel.class);
  
  private static final String DEFAULT_WIKI = "xwiki";
  
  private static final String DEFAULT_SPACE = "Main";
  
  private static final String DEFAULT_PAGE = "WebHome";
      
  private static final String DEFAULT_ATTACHMENT = "filename";

  private static final String wikiSpaceSeparator = ":";

  private static final String spacePageSeparator = ".";
  
  @Override
  public String getDocumentEditURL(ResourceReference documentReference) {
    ExecutionContext ec = execution.getContext();
    WikiContext wikiContext = null;
    if (ec != null) {
      wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
    }
    WikiContext wikiMarkupContext = getWikiMarkupContext(documentReference.getReference(),ResourceType.DOCUMENT);
    if (wikiContext != null) {
      String viewURL = getDocumentViewURL(wikiContext);
      StringBuilder sb = new StringBuilder(viewURL);
      String pageTitle = wikiMarkupContext.getPageTitle();
      String wikiType = wikiMarkupContext.getType();
      String wiki = wikiMarkupContext.getOwner();     
      sb.append("?")
        .append(WikiContext.ACTION)
        .append("=")
        .append(WikiContext.ADDPAGE)
        .append("&")
        .append(WikiContext.PAGETITLE)
        .append("=")
        .append(pageTitle)
        .append("&")
        .append(WikiContext.WIKI)
        .append("=")
        .append(wiki)
        .append("&")
        .append(WikiContext.WIKITYPE)
        .append("=")
        .append(wikiType);
      
      return sb.toString();
    }
    return "";
  }

  @Override
  public String getDocumentViewURL(ResourceReference documentReference) {
    WikiContext wikiMarkupContext = getWikiMarkupContext(documentReference.getReference(),ResourceType.DOCUMENT);
    return getDocumentViewURL(wikiMarkupContext);
  }

  @Override
  public String getImageURL(ResourceReference imageReference, Map<String, String> parameters) {
    String imageName = imageReference.getReference();
    WikiContext wikiMarkupContext = getWikiMarkupContext(imageName,ResourceType.ATTACHMENT);
    String portalContainerName = PortalContainer.getCurrentPortalContainerName();
    String portalURL = wikiMarkupContext.getPortalURL();
    String domainURL = portalURL.substring(0, portalURL.indexOf(portalContainerName)-1);  
    StringBuilder sb = new StringBuilder();
    sb.append(domainURL).append(Utils.getDefaultRepositoryWebDavUri());
    PageImpl page = null;
    try {
      WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
      page = (PageImpl) wikiService.getExsitedOrNewDraftPageById(wikiMarkupContext.getType(),wikiMarkupContext.getOwner(),wikiMarkupContext.getPageId());
      if (page != null) {
        sb.append(page.getWorkspace());
        sb.append(page.getPath());
        sb.append("/");
        AttachmentImpl att = page.getAttachment(wikiMarkupContext.getAttachmentName());
        if (att != null) {
          sb.append(att.getName());
        }
      }
    } catch (Exception e) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Couldn't get attachment URL for attachment: " + imageName, e);
      }
    }
    return sb.toString();
  }

  @Override
  public String getLinkURL(ResourceReference linkReference) {
    return getImageURL(linkReference, null);
  }

  @Override
  public boolean isDocumentAvailable(ResourceReference documentReference) {
    // Should look for pages in the model with the given title
    // (Page.findPageByTitle())
    Page page = null;
    String documentName = documentReference.getReference();
    ResourceType type = documentReference.getType();
    WikiContext wikiMarkupContext = getWikiMarkupContext(documentName, type);
    try {
      WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer()
                                                                 .getComponentInstanceOfType(WikiService.class);
      if (!Utils.isWikiAvailable(wikiMarkupContext.getType(), wikiMarkupContext.getOwner())) {
        return false;
      } else {
        page = wikiService.getPageById(wikiMarkupContext.getType(),
                                       wikiMarkupContext.getOwner(),
                                       wikiMarkupContext.getPageId());
        if (page == null) {
          return false;
        } 
      }

    } catch (Exception e) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("An exception happened when checking available status of document: "
            + documentName, e);
      }
      return false;
    }
    return true;
  }

  private String getDocumentViewURL(WikiContext context) {
    try {
      WikiService wikiService = (WikiService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(WikiService.class);
      PageImpl page = (PageImpl) wikiService.getRelatedPage(context.getType(), context.getOwner(), context.getPageId());
      if (page != null) {
        Wiki wiki = page.getWiki();
        String wikiType = Utils.getWikiType(wiki);
        String wikiOwner = wiki.getOwner();
        String pageId = page.getName();
        context.setType(wikiType);
        context.setOwner(wikiOwner);
        context.setPageId(pageId);
      }
    } catch (Exception e) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("An exception happened when process broken link.", e);
      }
    }
    return Utils.getDocumentURL(context);
  }
  
  public WikiContext getWikiMarkupContext(String objectName, ResourceType type) {
    WikiContext wikiMarkupContext = new WikiContext();
    try {
      DocumentReferenceResolver<String> stringDocumentReferenceResolver = componentManager.lookup(DocumentReferenceResolver.class);
      AttachmentReferenceResolver<String> stringAttachmentReferenceResolver = componentManager.lookup(AttachmentReferenceResolver.class);
      EntityReference entityReference = null;
      if (ResourceType.DOCUMENT.equals(type)) {
        entityReference = stringDocumentReferenceResolver.resolve(objectName);
      } else if (ResourceType.ATTACHMENT.equals(type)) {
        entityReference = stringAttachmentReferenceResolver.resolve(objectName);
      }
      ExecutionContext ec = execution.getContext();
      WikiContext wikiContext = null;
      if (ec != null) {
        wikiContext = (WikiContext) ec.getProperty(WikiContext.WIKICONTEXT);
      }
      if (entityReference != null) {
        wikiMarkupContext.setType(entityReference.extractReference(EntityType.WIKI).getName());
        wikiMarkupContext.setOwner(entityReference.extractReference(EntityType.SPACE).getName());
        wikiMarkupContext.setPageTitle(entityReference.extractReference(EntityType.DOCUMENT)
                                                      .getName());
        wikiMarkupContext.setPageId(wikiMarkupContext.getPageTitle());
        wikiMarkupContext.setPageId(TitleResolver.getId(wikiMarkupContext.getPageId(), false));
        EntityReference attachmentReference = entityReference.extractReference(EntityType.ATTACHMENT);
        if (attachmentReference != null) {
          wikiMarkupContext.setAttachmentName(attachmentReference.getName());
        }

        if (wikiContext != null) {

          wikiMarkupContext.setPortalURL(wikiContext.getPortalURL());
          wikiMarkupContext.setPortletURI(wikiContext.getPortletURI());

          if (DEFAULT_WIKI.equals(wikiMarkupContext.getType())) {
            wikiMarkupContext.setType(wikiContext.getType());
          }
          if (DEFAULT_SPACE.equals(wikiMarkupContext.getOwner())) {
            wikiMarkupContext.setOwner(wikiContext.getOwner());
          }
          if (DEFAULT_PAGE.equals(wikiMarkupContext.getPageId())) {
            wikiMarkupContext.setPageId(wikiContext.getPageId());
          }

        }
      }
    } catch (Exception e) {
      if (LOG.isWarnEnabled()) {
        LOG.warn("Couldn't get wiki context for markup: " + objectName, e);
      }
    }
    return wikiMarkupContext;
  }
  
  public String getDocumentName(WikiPageParams params) {
    StringBuilder sb = new StringBuilder();
    sb.append(params.getType())
      .append(wikiSpaceSeparator)
      .append(params.getOwner())
      .append(spacePageSeparator)
      .append(params.getPageId());
    return (sb.toString());
  }
  
}
