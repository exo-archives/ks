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
package org.exoplatform.wiki.service.wysiwyg;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.wiki.mow.core.api.wiki.AttachmentImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.service.WikiContext;
import org.exoplatform.wiki.utils.Utils;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.annotation.Requirement;
import org.xwiki.context.Execution;
import org.xwiki.gwt.wysiwyg.client.wiki.EntityConfig;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.model.reference.EntityReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;
import org.xwiki.rendering.listener.Link;
import org.xwiki.rendering.listener.LinkType;
import org.xwiki.rendering.parser.LinkParser;
import org.xwiki.rendering.renderer.LinkReferenceSerializer;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 12, 2010  
 */
@Component
public class DefaultLinkService implements LinkService {
  /**
   * The attachment URI protocol.
   */
  private static final String ATTACHMENT_URI_PROTOCOL = "attach:";

  /**
   * The image URI protocol.
   */
  private static final String IMAGE_URI_PROTOCOL = "image:";

  /** Execution context handler, needed for accessing the WikiContext. */
  @Requirement
  private Execution execution;
  
  /**
   * The component used to serialize Wiki document references.
   */
  @Requirement("compact")
  private EntityReferenceSerializer<String> entityReferenceSerializer;

  /**
   * The component used to resolve an entity reference relative to another entity reference.
   */
  @Requirement("explicit/reference")
  private EntityReferenceResolver<EntityReference> explicitReferenceEntityReferenceResolver;

  /**
   * The component used to resolve a string entity reference relative to another entity reference.
   */
  @Requirement("explicit")
  private EntityReferenceResolver<String> explicitStringEntityReferenceResolver;

  /**
   * The component used to serialize link references.
   */
  @Requirement
  private LinkReferenceSerializer linkReferenceSerializer;

  /**
   * The component used to parser link references.
   */
  @Requirement("xwiki/2.0")
  private LinkParser linkReferenceParser;

  /**
   * The object used to convert between client-side entity references and server-side entity references.
   */
  private final EntityReferenceConverter entityReferenceConverter = new EntityReferenceConverter();

  /**
   * {@inheritDoc}
   * 
   * @see LinkService#getEntityConfig(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference,
   *      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference)
   */
  public EntityConfig getEntityConfig(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference origin,
      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference destination)
  {
      EntityReference originReference = entityReferenceConverter.convert(origin);
      EntityReference destinationReference = entityReferenceConverter.convert(destination);
      destinationReference =
          explicitReferenceEntityReferenceResolver.resolve(destinationReference, destinationReference.getType(),
              originReference);
      String destRelativeStrRef = this.entityReferenceSerializer.serialize(destinationReference, originReference);

      EntityConfig entityConfig = new EntityConfig();
      entityConfig.setUrl(getEntityURL(destinationReference));
      entityConfig.setReference(getLinkReference(destination.getType(), destRelativeStrRef));
      return entityConfig;
  }

  /**
   * @param entityReference an entity reference
   * @return the URL to access the specified entity
   * @throws Exception 
   */
  private String getEntityURL(EntityReference entityReference)
  {
    org.exoplatform.wiki.service.WikiService wservice = (org.exoplatform.wiki.service.WikiService) PortalContainer.getComponent(org.exoplatform.wiki.service.WikiService.class);
    WikiContext wikiContext = getWikiContext();
    WikiContext context = new WikiContext();
    context.setPortalURI(wikiContext.getPortalURI());
    context.setPortletURI(wikiContext.getPortletURI());
    PageImpl page;
    switch (entityReference.getType()) {
      case DOCUMENT:
        String pageId = entityReference.getName();
        String wikiOwner = entityReference.getParent().getName();
        String wikiType = entityReference.getParent().getParent().getName();
        context.setType(wikiType);
        context.setOwner(wikiOwner);
        context.setPageId(pageId);
        try {
          boolean isPageExisted = wservice.isExisting(wikiType, wikiOwner, pageId);
          if (isPageExisted) {
            return Utils.getDocumentURL(context);
          }
        } catch (Exception e) {}
        return null;
      case ATTACHMENT:
        String attachmentId = entityReference.getName();
        pageId = entityReference.getParent().getName();
        wikiOwner = entityReference.getParent().getParent().getName();
        wikiType = entityReference.getParent().getParent().getParent().getName();
        try {
          page = (PageImpl) wservice.getExsitedOrNewDraftPageById(wikiType, wikiOwner, pageId);
          AttachmentImpl att = page.getAttachment(attachmentId);
          if (att != null) {
            return att.getDownloadURL();
          }
        } catch (Exception e) {}
        return null;
      default:
        return null;
    }
  }

  /**
   * @param entityType the type of linked entity
   * @param relativeStringEntityReference a relative string entity reference
   * @return a link reference that can be used to insert a link to the specified entity
   */
  private String getLinkReference(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType entityType,
      String relativeStringEntityReference)
  {
      Link link = new Link();
      link.setType(entityType == org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.DOCUMENT
          ? LinkType.DOCUMENT : LinkType.URI);
      link.setReference(relativeStringEntityReference);
      String linkReference = linkReferenceSerializer.serialize(link);
      if (entityType == org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.ATTACHMENT) {
          linkReference = ATTACHMENT_URI_PROTOCOL + linkReference;
      }
      return linkReference;
  }

  /**
   * {@inheritDoc}
   * 
   * @see LinkService#parseLinkReference(String, org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType,
   *      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference)
   */
  public org.xwiki.gwt.wysiwyg.client.wiki.EntityReference parseLinkReference(String linkReference,
      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType entityType,
      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference baseReference)
  {
      String fullLinkReference = linkReference;
      // Add the image protocol because the client doesn't provided it.
      if (entityType == org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.IMAGE) {
          fullLinkReference = IMAGE_URI_PROTOCOL + linkReference;
      }
      Link link = linkReferenceParser.parse(fullLinkReference);
      String stringEntityReference = link.getReference();
      // Remove the URI protocol because the link reference parser doesn't do it.
      int uriSchemeDelimiter = stringEntityReference.indexOf(':');
      if (link.getType() == LinkType.URI && uriSchemeDelimiter > -1) {
          stringEntityReference = stringEntityReference.substring(uriSchemeDelimiter + 1);
      }
      org.xwiki.gwt.wysiwyg.client.wiki.EntityReference entityReference =
          entityReferenceConverter.convert(explicitStringEntityReferenceResolver.resolve(stringEntityReference,
              entityReferenceConverter.convert(entityType), entityReferenceConverter.convert(baseReference)));
      entityReference.setType(entityType);
      return entityReference;
  }

  private WikiContext getWikiContext()
  {
      return (WikiContext) execution.getContext().getProperty(WikiContext.WIKICONTEXT);
  }
  
}
