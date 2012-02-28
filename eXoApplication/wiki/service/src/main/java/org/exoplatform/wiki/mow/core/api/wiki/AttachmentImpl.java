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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;

import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.api.annotations.WorkspaceName;
import org.chromattic.ext.ntdef.NTFile;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.wiki.mow.api.Attachment;
import org.exoplatform.wiki.mow.api.Permission;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.utils.Utils;

/**
 * Created by The eXo Platform SAS
 * May, 2010  
 */
@PrimaryType(name = WikiNodeType.WIKI_ATTACHMENT)
public abstract class AttachmentImpl extends NTFile implements Attachment, Comparable<AttachmentImpl> {

  private Permission permission = new PermissionImpl();
  
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
  
  public Calendar getCreatedDate() {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(getCreated());
    return calendar;
  }
  
  public long getWeightInBytes() {
    return getContentResource().getData().length;
  }
  
  public Calendar getUpdatedDate() {
    Calendar calendar = GregorianCalendar.getInstance();
    calendar.setTime(getLastModified());
    return calendar;
  }
  
  public String getDownloadURL() {
    StringBuilder sb = new StringBuilder();
    String mimeType = getContentResource().getMimeType();
    PageImpl page = this.getParentPage();
    Wiki wiki = page.getWiki();
    if (mimeType != null && mimeType.startsWith("image/") && wiki != null) {
      // Build REST url to view image
      sb.append(Utils.getDefaultRestBaseURI())
        .append("/wiki/images/")
        .append(wiki.getType())
        .append("/")
        .append(Utils.SPACE)
        .append("/")
        .append(Utils.validateWikiOwner(wiki.getType(), wiki.getOwner()))
        .append("/")
        .append(Utils.PAGE)
        .append("/")
        .append(page.getName())
        .append("/")
        .append(this.getName());
    } else {
      sb.append(Utils.getCurrentRepositoryWebDavUri());
      sb.append(getWorkspace());
      String path = getPath();
      try {
        String parentPath = path.substring(0, path.lastIndexOf("/"));
        sb.append(parentPath + "/" + URLEncoder.encode(getName(), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        sb.append(path);
      }
    }
    return sb.toString();
  }
  
  public String getFullTitle() {
    String fullTitle = (getFileType() == null) ? getTitle() : getTitle().concat(getFileType());
    return (fullTitle != null) ? fullTitle : getName();
  }

  @ManyToOne
  public abstract PageImpl getParentPage();
  
  @Destroy
  public abstract void remove();
  
  public String getText() {
    Resource textContent = getContentResource();
    if (textContent == null) {
      setText("");
      textContent = getContentResource();
    }
    
    try {
      return new String(textContent.getData(), "UTF-8");
    } catch (UnsupportedEncodingException e) {
      return new String(textContent.getData());
    }
  }

  public void setText(String text) {
    text = text != null ? text : "";
    Resource textContent = Resource.createPlainText(text);
    setContentResource(textContent);
  }
  
  public boolean hasPermission(PermissionType permissionType) throws Exception {
    if (permission.getMOWService() == null) {
      permission.setMOWService(getParentPage().getMOWService());
    }
    return permission.hasPermission(permissionType, getPath());
  }
  
  public HashMap<String, String[]> getPermission() throws Exception {
    if (permission.getMOWService() == null) {
      permission.setMOWService(getParentPage().getMOWService());
    }
    return permission.getPermission(getPath());
  }

  public void setPermission(HashMap<String, String[]> permissions) throws Exception {
    if (permission.getMOWService() == null) {
      permission.setMOWService(getParentPage().getMOWService());
    }
    permission.setPermission(permissions, getPath());
  }
  
  @Override
  public int compareTo(AttachmentImpl o) {
    return getName().toLowerCase().compareTo(o.getName().toLowerCase());
  }
}
