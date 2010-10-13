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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.version.Version;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.DuplicateNameException;
import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.Destroy;
import org.chromattic.api.annotations.ManyToOne;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.Path;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.chromattic.api.annotations.WorkspaceName;
import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.chromattic.ext.ntdef.VersionableMixin;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiService;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Mar 26, 2010  
 */
@PrimaryType(name = WikiNodeType.WIKI_PAGE)
public abstract class PageImpl implements Page {
  
  private MOWService mowService;
  
  private WikiService wService;

  public void setMOWService(MOWService mowService) {
    this.mowService = mowService;
  }
  
  public void setWikiService(WikiService wService) {
    this.wService = wService;
  }

  public ChromatticSession getChromatticSession() {
    return mowService.getSession();
  }
  
  public WikiService getWikiService(){
    return wService;
  }
  
  private Node getJCRPageNode() throws Exception {
    return (Node) getChromatticSession().getJCRSession().getItem(getPath());
  }
  
  @Name
  public abstract String getName();
  public abstract void setName(String name);
  
  @Path
  public abstract String getPath();

  @WorkspaceName
  public abstract String getWorkspace();
  
  @OneToOne
  @Owner
  @MappedBy(WikiNodeType.Definition.CONTENT)
  protected abstract ContentImpl getContentByChromattic();  
  protected abstract void setContentByChromattic(ContentImpl content);
  @Create
  protected abstract ContentImpl createContent();
  public ContentImpl getContent() {
    ContentImpl content = getContentByChromattic();
    if (content == null) {
      content = createContent();
      setContentByChromattic(content);
    }
    return content;
  }
  
  @Property(name = WikiNodeType.Definition.OWNER)
  public abstract String getOwner();
  public abstract void setOwner(String owner);
  
  @Property(name = WikiNodeType.Definition.AUTHOR)
  public abstract String getAuthor();

  @Property(name = WikiNodeType.Definition.UPDATED_DATE)
  public abstract Date getUpdatedDate();

  @OneToOne(type = RelationshipType.EMBEDDED)
  @Owner
  public abstract MovedMixin getMovedMixin();
  public abstract void setMovedMixin(MovedMixin move);
  
  @OneToOne(type = RelationshipType.EMBEDDED)
  @Owner
  public abstract RemovedMixin getRemovedMixin();
  public abstract void setRemovedMixin(RemovedMixin remove);
  
  @OneToOne(type = RelationshipType.EMBEDDED)
  @Owner
  public abstract RenamedMixin getRenamedMixin();
  public abstract void setRenamedMixin(RenamedMixin mix);
  
  @OneToOne(type = RelationshipType.EMBEDDED)
  @Owner
  public abstract VersionableMixin getVersionableMixin();
  protected abstract void setVersionableMixin(VersionableMixin mix);
  @Create
  protected abstract VersionableMixin createVersionableMixin();
  
  public void makeVersionable() {
    VersionableMixin versionableMixin = getVersionableMixin();
    if (versionableMixin == null) {
      versionableMixin = createVersionableMixin();
      setVersionableMixin(versionableMixin);
    }
  }
  
  //TODO: replace by @Checkin when Chromattic support
  public NTVersion checkin() throws Exception {
    getChromatticSession().save();
    Node pageNode = getJCRPageNode();
    Version newVersion = pageNode.checkin();
    NTVersion ntVersion = getChromatticSession().findByNode(NTVersion.class, newVersion);
    return ntVersion;
  }

  //TODO: replace by @Checkout when Chromattic support
  public void checkout() throws Exception {
    Node pageNode = getJCRPageNode();
    pageNode.checkout();
  }

  //TODO: replace by @Restore when Chromattic support
  public void restore(String versionName, boolean removeExisting) throws Exception {
    Node pageNode = getJCRPageNode();
    pageNode.restore(versionName, removeExisting);
  }
  
  @Create
  public abstract AttachmentImpl createAttachment();
  
  public AttachmentImpl createAttachment(String fileName, Resource contentResource) throws Exception {
    if (fileName == null) {
      throw new NullPointerException();
    }
    Iterator<AttachmentImpl> attIter= getAttachments().iterator();
    while (attIter.hasNext()) {
      AttachmentImpl att = attIter.next();
      if (att.getName().equals(fileName)) {
        return null;
      }
    }
    AttachmentImpl file = createAttachment();
    file.setName(TitleResolver.getObjectId(fileName, false));
    addAttachment(file);
    if (fileName.lastIndexOf(".") > 0) {
      file.setTitle(fileName.substring(0, fileName.lastIndexOf(".")));
      file.setFileType(fileName.substring(fileName.lastIndexOf(".")));
    } else
      file.setTitle(fileName);
    if (contentResource != null) {
      file.setContentResource(contentResource);
    }
    getChromatticSession().save();
    return file;
  }
  
  
  @OneToMany
  public abstract Collection<AttachmentImpl> getAttachments() ;
  
  public AttachmentImpl getAttachment(String attachmentId) {
    for (AttachmentImpl att : getAttachments()) {
      if (att.getName().equals(attachmentId)) {
        return att;
      }
    }
    return null;
  }
  
  public void addAttachment(AttachmentImpl attachment) throws DuplicateNameException {
    getAttachments().add(attachment);
    
  }  
  
  public void removeAttachment(String attachmentId){
    AttachmentImpl attachment = getAttachment(attachmentId);
    if(attachment != null){
      attachment.remove();
    }
  }
  
  @ManyToOne
  public abstract PageImpl getParentPage();
  public abstract void setParentPage(PageImpl page);

  @ManyToOne
  public abstract Trash getTrash();
  public abstract void setTrash(Trash trash);
  
  @OneToMany
  public abstract Map<String, PageImpl> getChildPages();
  
  /*public void addWikiPage(PageImpl wikiPage) throws DuplicateNameException {
    getChildPages().add(wikiPage);
  }*/
  public void addPage(String pageName, PageImpl page) {
    if (pageName == null) {
      throw new NullPointerException();
    }
    if (page == null) {
      throw new NullPointerException();
    }
    Map<String, PageImpl> children = getChildPages();
    if (children.containsKey(pageName)) {
      throw new IllegalStateException();
    }
    children.put(pageName, page);
  }
  
  public void addWikiPage(PageImpl page) {
    if (page == null) {
      throw new NullPointerException();
    }
    addPage(page.getName(), page);
  }
  
  
  public PageImpl getWikiPage(String pageId){
    if(WikiNodeType.Definition.WIKI_HOME_NAME.equalsIgnoreCase(pageId)){
      return this;
    }
    Iterator<PageImpl> iter = getChildPages().values().iterator();
    while(iter.hasNext()) {
      PageImpl page = (PageImpl)iter.next() ;
      if (pageId.equals(page.getName()))  return page ;         
    }
    return null ;
  }
  
  public Wiki getWiki() {
    PageImpl parent = this.getParentPage();
    if (parent == null) {
      parent = this;
    }
    while (!parent.getName().equals(WikiNodeType.Definition.WIKI_HOME_NAME)) {
      parent = parent.getParentPage();
    }
    WikiHome wikiHome = (WikiHome) parent;
    PortalWiki portalWiki = wikiHome.getPortalWiki();
    GroupWiki groupWiki = wikiHome.getGroupWiki();
    UserWiki userWiki = wikiHome.getUserWiki();
    if (portalWiki != null) {
      return portalWiki;
    } else if (groupWiki != null) {
      return groupWiki;
    } else {
      return userWiki;
    }
  }
  
  @Destroy
  public abstract void remove();
}
