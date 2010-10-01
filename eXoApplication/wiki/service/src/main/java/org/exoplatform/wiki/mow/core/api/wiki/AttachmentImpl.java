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
package org.exoplatform.wiki.mow.core.api.wiki;

import java.util.Calendar;
import java.util.GregorianCalendar;

import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.api.annotations.WorkspaceName;
import org.chromattic.ext.ntdef.NTFile;
import org.exoplatform.wiki.mow.api.Attachment;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * May, 2010  
 */
@PrimaryType(name = WikiNodeType.WIKI_ATTACHMENT)
public abstract class AttachmentImpl extends NTFile implements Attachment {

  @Name
  public abstract String getName();
  public abstract void setName(String name);
  
  @Path
  public abstract String getPath();
  
  public String getJCRContentPath() {
    return getPath() + "/jcr:content";
  }
  
  @WorkspaceName
  public abstract String getWorkspace();
  
  @Property(name = WikiNodeType.Definition.TITLE)
  public abstract String getTitle();
  public abstract void setTitle(String title);
  
  @Property(name = WikiNodeType.Definition.FILE_TYPE)
  public abstract String getFileType();
  public abstract void setFileType(String fileType);
  
  @Property(name = WikiNodeType.Definition.CREATOR)
  public abstract String getCreator();
  public abstract void setCreator(String creator);
  
  public Calendar getCreatedDate(){
    try {
      Calendar calendar = GregorianCalendar.getInstance() ;
      calendar.setTime(getCreated()) ;
      return calendar ;
    }catch(Exception e) {      
    }    
    return null ;
  }
  
  public long getWeightInBytes(){
    try {
      return getContentResource().getData().length ;
    }catch(Exception e) {      
    }
    return 0 ;
  }
  
  public Calendar getUpdatedDate(){
    try {
      Calendar calendar = GregorianCalendar.getInstance() ;
      calendar.setTime(getLastModified()) ;
      return calendar ;
    }catch(Exception e) {      
    }    
    return null ;
  }
  
  public String getDownloadURL(){
    StringBuilder sb = new StringBuilder();
    sb.append(Utils.getDefaultRepositoryWebDavUri());
    sb.append(getWorkspace());
    sb.append(getPath());
    return sb.toString();
  }
  
  public String getFullTitle() {
    return (getFileType() == null) ? getTitle() : getTitle().concat(getFileType());
  }

  @ManyToOne
  public abstract PageImpl getParentPage();
  
  @Destroy
  public abstract void remove();
}
