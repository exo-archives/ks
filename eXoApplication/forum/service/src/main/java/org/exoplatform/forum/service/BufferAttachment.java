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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.forum.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Ngyen Quang
 *					hung.nguyen@exoplatform.com
 * Jul 9, 2007
 */
public class BufferAttachment extends ForumAttachment {
	
	private byte[] imageBytes ;
	
//	public InputStream getInputStream()throws Exception{
//		/*DownloadService downloadService = (DownloadService)PortalContainer.getComponent(DownloadService.class) ;
//		DownloadResource downloadResource = downloadService.getDownloadResource(getId()) ;
//		downloadResource.getInputStream() ;*/
//		return inputStream ; 
//	}
  public InputStream getInputStream() throws Exception { 
    if(imageBytes != null) return new ByteArrayInputStream(imageBytes) ;
    return null ;
  }
//	public void setInputStream(InputStream is){ inputStream = is ; }
  public void setInputStream(InputStream input) throws Exception {
    if (input != null) {
      imageBytes = new byte[input.available()] ; 
      input.read(imageBytes) ;
    }
    else imageBytes = null ;
  }
}
