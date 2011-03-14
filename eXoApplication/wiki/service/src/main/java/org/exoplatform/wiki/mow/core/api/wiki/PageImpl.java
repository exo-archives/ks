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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Value;
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
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.AccessControlList;
import org.exoplatform.services.jcr.access.SystemIdentity;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;
import org.exoplatform.wiki.chromattic.ext.ntdef.NTVersion;
import org.exoplatform.wiki.chromattic.ext.ntdef.VersionableMixin;
import org.exoplatform.wiki.mow.api.Page;
import org.exoplatform.wiki.mow.api.Wiki;
import org.exoplatform.wiki.mow.api.WikiNodeType;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.content.ContentImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.PermissionType;
import org.exoplatform.wiki.service.WikiService;
import org.exoplatform.wiki.utils.Utils;

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
  
  private boolean isMinorEdit = false;

  public void setMOWService(MOWService mowService) {
    this.mowService = mowService;
  }
  
  public void setWikiService(WikiService wService) {
    this.wService = wService;
  }

  public ChromatticSession getChromatticSession() {
    return mowService.getSession();
  }
  
  public Session getJCRSession() {
    return getChromatticSession().getJCRSession();
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

  @Property(name = WikiNodeType.Definition.CREATED_DATE)
  public abstract Date getCreatedDate();
  public abstract void setCreatedDate(Date date);
  
  @Property(name = WikiNodeType.Definition.UPDATED_DATE)
  public abstract Date getUpdatedDate();
  
  @Property(name = WikiNodeType.Definition.URL)
  public abstract String getURL();
  public abstract void setURL(String url);
  
  
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
  public abstract WatchedMixin getWatchedMixin();
  public abstract void setWatchedMixin(WatchedMixin mix);
  
  @Create
  protected abstract WatchedMixin createWatchedMixin();
  
  public void makeWatched() {
    WatchedMixin watchedMixin = getWatchedMixin();
    if (watchedMixin == null) {
      watchedMixin = createWatchedMixin();
      setWatchedMixin(watchedMixin);
    }
  }
  
  
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
        att.remove();
      }
    }
    AttachmentImpl file = createAttachment();
    file.setName(TitleResolver.getId(fileName, false));
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
  
  @Property(name = WikiNodeType.Definition.OVERRIDEPERMISSION)
  public abstract boolean getOverridePermission();
  public abstract void setOverridePermission(boolean isOverridePermission);
  
  public boolean hasPermission(PermissionType permissionType) throws Exception {
    String[] permission = new String[] {};
    if (PermissionType.VIEWPAGE.equals(permissionType)) {
      permission = new String[] { org.exoplatform.services.jcr.access.PermissionType.READ };
    } else if (PermissionType.EDITPAGE.equals(permissionType)) {
      permission = new String[] { org.exoplatform.services.jcr.access.PermissionType.ADD_NODE,
          org.exoplatform.services.jcr.access.PermissionType.REMOVE,
          org.exoplatform.services.jcr.access.PermissionType.SET_PROPERTY };
    }

    ExtendedNode pageNode = (ExtendedNode) getJCRPageNode();
    AccessControlList acl = pageNode.getACL();

    ConversationState conversationState = ConversationState.getCurrent();
    Identity user = null;
    if (conversationState != null) {
      user = conversationState.getIdentity();
    } else {
      user = new Identity(SystemIdentity.ANONIM);
    }
    return Utils.hasPermission(acl, permission, user);
  }
  
  public HashMap<String, String[]> getPagePermission() throws Exception {
    ExtendedNode pageNode = (ExtendedNode) getJCRPageNode();
    HashMap<String, String[]> perm = new HashMap<String, String[]>();
    AccessControlList acl = pageNode.getACL();
    List<AccessControlEntry> aceList = acl.getPermissionEntries();
    for (int i = 0, length = aceList.size(); i < length; i++) {
      AccessControlEntry ace = aceList.get(i);
      String[] nodeActions = perm.get(ace.getIdentity());
      List<String> actions = null;
      if (nodeActions != null) {
        actions = new ArrayList<String>(Arrays.asList(nodeActions));
        Arrays.asList(nodeActions);
      } else {
        actions = new ArrayList<String>();
      }
      actions.add(ace.getPermission());
      perm.put(ace.getIdentity(), actions.toArray(new String[5]));
    }
    return perm;
  }

  public void setPagePermission(HashMap<String, String[]> permissions) throws Exception {
    getChromatticSession().save();
    ExtendedNode pageNode = (ExtendedNode) getJCRPageNode();
    if (pageNode.canAddMixin("exo:privilegeable")) {
      pageNode.addMixin("exo:privilegeable");
    }
    if (permissions != null && permissions.size() > 0) {
      pageNode.setPermissions(permissions);
    } else {
      pageNode.clearACL();
      pageNode.setPermission(SystemIdentity.ANY, org.exoplatform.services.jcr.access.PermissionType.ALL);
    }
  }
  
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
    WikiHome wikiHome = getWikiHome();
    if (wikiHome != null) {
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
    return null;
  }

  public WikiHome getWikiHome() {
    PageImpl parent = this.getParentPage();
    if (this instanceof WikiHome) {
      parent = this;
    } else
      while (parent != null && !(parent instanceof WikiHome)) {
        parent = parent.getParentPage();
      }
    return (WikiHome) parent;
  }
  
  public boolean isMinorEdit() {
    return isMinorEdit;
  }

  public void setMinorEdit(boolean isMinorEdit) {
    this.isMinorEdit = isMinorEdit;
  }

  @Destroy
  public abstract void remove();
  
  /**
   * add a related page
   * @param page
   * @return uuid of node of related page if add successfully. <br>
   *         null if add failed.
   * @throws NullPointerException if the param is null
   * @throws Exception when any error occurs.
   */
  public String addRelatedPage(PageImpl page) throws Exception {
    Map<String, Value> referredUUIDs = getReferredUUIDs();
    Session jcrSession = getJCRSession();
    Node myJcrNode = (Node) jcrSession.getItem(getPath());
    Node referredJcrNode = (Node) jcrSession.getItem(page.getPath());
    String referedUUID = referredJcrNode.getUUID();
    if (referredUUIDs.containsKey(referedUUID)) {
      return null;
    }
    Value value2Add = jcrSession.getValueFactory().createValue(referredJcrNode);
    referredUUIDs.put(referedUUID, value2Add);

    myJcrNode.setProperty(WikiNodeType.Definition.RELATION,
                          referredUUIDs.values().toArray(new Value[referredUUIDs.size()]));
    myJcrNode.save();
    return referedUUID;
  }
  
  public List<PageImpl> getRelatedPages() throws Exception {
    List<PageImpl> pages = new ArrayList<PageImpl>();
    Iterator<Entry<String, Value>> refferedIter = getReferredUUIDs().entrySet().iterator();
    ChromatticSession chSession = getChromatticSession();
    while (refferedIter.hasNext()) {
      Entry<String, Value> entry = refferedIter.next();
      PageImpl page = chSession.findById(PageImpl.class, entry.getValue().getString());
      pages.add(page);
    }
    return pages;
  }
  
  /**
   * remove a specified related page.
   * @param page
   * @return uuid of node if related page is removed successfully <br>
   *         null if removing failed.
   * @throws Exception when an error is thrown.
   */
  public String removeRelatedPage(PageImpl page) throws Exception {
    Map<String, Value> referedUUIDs = getReferredUUIDs();
    Session jcrSession = getJCRSession();
    Node referredJcrNode = (Node) jcrSession.getItem(page.getPath());
    Node myJcrNode = (Node) jcrSession.getItem(getPath());
    String referredUUID = referredJcrNode.getUUID();
    if (!referedUUIDs.containsKey(referredUUID)) {
      return null;
    }
    referedUUIDs.remove(referredUUID);
    myJcrNode.setProperty(WikiNodeType.Definition.RELATION,
                          referedUUIDs.values().toArray(new Value[referedUUIDs.size()]));
    myJcrNode.save();
    return referredUUID;
  }

  
  /**
   * get reference uuids of current page
   * @return Map<String, Value> map of referred uuids of current page 
   * @throws Exception when an error is thrown.
   */
  public Map<String, Value> getReferredUUIDs() throws Exception {   
    Session jcrSession = getJCRSession();
    Node myJcrNode = (Node) jcrSession.getItem(getPath());
    Map<String, Value> referedUUIDs = new HashMap<String, Value>();
    if (myJcrNode.hasProperty(WikiNodeType.Definition.RELATION)) {
      Value[] values = myJcrNode.getProperty(WikiNodeType.Definition.RELATION).getValues();
      if (values != null && values.length > 0) {
        for (Value value : values) {
          referedUUIDs.put(value.getString(), value);
        }
      }
    }
    return referedUUIDs;
  }
  
  public void removeAllRelatedPages() throws Exception {
    Session jcrSession = getJCRSession();
    Node myJcrNode = (Node) jcrSession.getItem(getPath());
    myJcrNode.setProperty(WikiNodeType.Definition.RELATION, (Value[]) null);
    myJcrNode.save();
  }
  
}
