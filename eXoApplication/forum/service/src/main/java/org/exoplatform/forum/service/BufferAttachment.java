/***************************************************************************
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.jcr.Node;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Ngyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 9, 2007
 */
public class BufferAttachment extends ForumAttachment {

  private byte[] bytes;

  public InputStream getInputStream() throws Exception {
    if (bytes != null)
      return new ByteArrayInputStream(bytes);
    Node attachment;
    Session session = getSesison();
    try {
      attachment = (Node) session.getItem(getPathNode());
      return attachment.getNode(ForumNodeTypes.JCR_CONTENT).getProperty(ForumNodeTypes.JCR_DATA).getStream();
    } catch (Exception e) {
      return null;
    } finally {
      session.logout();
    }
  }

  private Session getSesison() throws Exception {
    RepositoryService repoService = (RepositoryService) PortalContainer.getInstance().getComponentInstanceOfType(RepositoryService.class);
    return repoService.getCurrentRepository().getSystemSession(getWorkspace());
  }

  public void setInputStream(InputStream input) throws Exception {
    if (input != null) {
      bytes = new byte[input.available()];
      input.read(bytes);
    } else
      bytes = null;
  }
}
