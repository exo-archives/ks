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
package org.exoplatform.faq.service;

import java.io.InputStream;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 10, 2007 
 */
abstract public class FileAttachment {
  private String id ;
  private String name ;
  private String mimeType ;
  private long size ;
   
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getMimeType() { return mimeType ; }
  public void setMimeType(String mimeType_) { this.mimeType = mimeType_ ; }
  
  public long getSize() { return size ; }
  public void setSize(long size_) { this.size = size_ ; }
  
  public String getName() { return name ; }
  public void setName(String name_) { this.name = name_ ; }
  
  public abstract InputStream getInputStream() throws Exception ;
}
