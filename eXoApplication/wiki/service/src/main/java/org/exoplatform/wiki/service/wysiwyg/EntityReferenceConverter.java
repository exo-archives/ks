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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.service.impl.WikiServiceImpl;
import org.xwiki.gwt.wysiwyg.client.wiki.WikiPageReference;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.AttachmentReference;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;

/**
 * Created by The eXo Platform SAS
 * Author : viet nguyen
 *          viet.nguyen@exoplatform.com
 * Aug 12, 2010  
 */
public class EntityReferenceConverter {
  /**
   * Maps entity types to client reference component names.
   */
  private static final Map<EntityType, String> REFERENCE_COMPONENT_NAME;
  
  private static final Log      log               = ExoLogger.getLogger(EntityReferenceConverter.class);

  static {
    REFERENCE_COMPONENT_NAME = new HashMap<EntityType, String>();
    REFERENCE_COMPONENT_NAME.put(org.xwiki.model.EntityType.WIKI, WikiPageReference.WIKI_NAME);
    REFERENCE_COMPONENT_NAME.put(EntityType.SPACE, WikiPageReference.SPACE_NAME);
    REFERENCE_COMPONENT_NAME.put(EntityType.DOCUMENT, WikiPageReference.PAGE_NAME);
    REFERENCE_COMPONENT_NAME.put(EntityType.ATTACHMENT,
                                 org.xwiki.gwt.wysiwyg.client.wiki.AttachmentReference.FILE_NAME);
  }

  /**
   * Converts an entity reference received from the client to an entity
   * reference to be used on the server.
   * 
   * @param clientEntityReference a client-side entity reference
   * @return a server-side entity reference
   */
  public EntityReference convert(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference clientEntityReference) {
    if (clientEntityReference == null) {
      return null;
    }
    EntityReference serverEntityReference = null;
    String wikiName = clientEntityReference.getComponent(REFERENCE_COMPONENT_NAME.get(EntityType.WIKI));
    if (!StringUtils.isEmpty(wikiName)) {
      serverEntityReference = new EntityReference(wikiName, EntityType.WIKI);
    }
    String spaceName = clientEntityReference.getComponent(REFERENCE_COMPONENT_NAME.get(EntityType.SPACE));
    if (!StringUtils.isEmpty(spaceName)) {
      serverEntityReference = new EntityReference(spaceName,
                                                  EntityType.SPACE,
                                                  serverEntityReference);
    }
    String pageName = clientEntityReference.getComponent(REFERENCE_COMPONENT_NAME.get(EntityType.DOCUMENT));
    if (!StringUtils.isEmpty(pageName)) {
      serverEntityReference = new EntityReference(pageName,
                                                  EntityType.DOCUMENT,
                                                  serverEntityReference);
    }
    String fileName = clientEntityReference.getComponent(REFERENCE_COMPONENT_NAME.get(EntityType.ATTACHMENT));
    if (!StringUtils.isEmpty(fileName)) {
      serverEntityReference = new EntityReference(fileName,
                                                  EntityType.ATTACHMENT,
                                                  serverEntityReference);
    }
    return serverEntityReference;
  }

  /**
   * Converts an entity reference used on the server side to an entity reference
   * to be sent to the client.
   * 
   * @param serverEntityReference a server-side entity reference
   * @return the corresponding client-side entity reference
   */
  public org.xwiki.gwt.wysiwyg.client.wiki.EntityReference convert(EntityReference serverEntityReference) {
    org.xwiki.gwt.wysiwyg.client.wiki.EntityReference clientEntityReference = new org.xwiki.gwt.wysiwyg.client.wiki.EntityReference();
    try {
      clientEntityReference.setType(org.xwiki.gwt.wysiwyg.client.wiki.EntityReference.EntityType.valueOf(serverEntityReference.getType()
                                                                                                                              .toString()));
    } catch (Exception e) {
      log.error("Can't set type for client entity reference", e);
      return null;
    }
    EntityReference child = serverEntityReference;
    while (child != null) {
      String componentName = REFERENCE_COMPONENT_NAME.get(child.getType());
      if (componentName != null) {
        clientEntityReference.setComponent(componentName, child.getName());
      }
      child = child.getParent();
    }
    return clientEntityReference;
  }

  /**
   * @param documentReference a document reference
   * @return the corresponding wiki page reference
   */
  public WikiPageReference convert(DocumentReference documentReference) {
    String wikiName = documentReference.getWikiReference().getName();
    String spaceName = documentReference.getLastSpaceReference().getName();
    String pageName = documentReference.getName();
    return new WikiPageReference(wikiName, spaceName, pageName);
  }

  /**
   * @param reference a wiki page reference
   * @return the corresponding document reference
   */
  public DocumentReference convert(WikiPageReference reference) {
    return new DocumentReference(reference.getWikiName(),
                                 reference.getSpaceName(),
                                 reference.getPageName());
  }

  /**
   * @param attachmentReference an attachment reference
   * @return the corresponding client attachment reference
   */
  public org.xwiki.gwt.wysiwyg.client.wiki.AttachmentReference convert(org.xwiki.model.reference.AttachmentReference attachmentReference) {
    return new org.xwiki.gwt.wysiwyg.client.wiki.AttachmentReference(attachmentReference.getName(),
                                                                     convert(attachmentReference.getDocumentReference()));
  }

  /**
   * @param clientAttachmentReference a client attachment reference
   * @return the corresponding server-side attachment reference
   */
  public AttachmentReference convert(org.xwiki.gwt.wysiwyg.client.wiki.AttachmentReference clientAttachmentReference) {
    return new AttachmentReference(clientAttachmentReference.getFileName(),
                                   convert(clientAttachmentReference.getWikiPageReference()));
  }
}