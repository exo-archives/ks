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

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Duy Tu
 *          tu.duy@exoplatform.com
 * Nov 10, 2007 
 */
public class FileAttachment {
	private String id ;
	private String path ;
  private String workspace ;
  private String name ;
  private String mimeType ;
  private long size ;
  private InputStream input ;
  
  
  public String getId() { return id ; }
  public void setId(String s) { this.id = s ; }
  
  public String getPath() { return path ; }
  public void setPath(String p) { this.path = p ; }
  
  public String getWorkspace() { return workspace ; }
  public void setWorkspace(String ws) { workspace = ws ; }
  
  public String getMimeType() { return mimeType ; }
  public void setMimeType(String mimeType_) { this.mimeType = mimeType_ ; }
  
  public long getSize() { return size ; }
  public void setSize(long size_) { this.size = size_ ; }
  
  public String getName() { return name ; }
  public void setName(String name_) { this.name = name_ ; }
  
  public InputStream getInputStream() throws Exception { 
  	if(input != null)return input ;
  	else {
  		Node attachment ;
      try{
        attachment = (Node)getSesison().getItem(getPath()) ;
        return attachment.getNode("jcr:content").getProperty("jcr:data").getStream() ;
      }catch (ItemNotFoundException e) {  
        return null ;
      } catch (PathNotFoundException ex) {
        return  null;
      } catch(FileNotFoundException fileNotFoundException) {
        return null;
      }
  	}
  }
  
  public void setInputStream(InputStream in) throws Exception {input = in ; }
  
  private Session getSesison()throws Exception {
    RepositoryService repoService = (RepositoryService)PortalContainer
      .getInstance().getComponentInstanceOfType(RepositoryService.class) ;
    return repoService.getDefaultRepository().getSystemSession(workspace) ;
  }
}
