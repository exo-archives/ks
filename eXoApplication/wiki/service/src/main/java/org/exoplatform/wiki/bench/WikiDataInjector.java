/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.wiki.bench;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.chromattic.ext.ntdef.Resource;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;
import org.exoplatform.wiki.resolver.TitleResolver;
import org.exoplatform.wiki.service.WikiService;

/**
 * <p>
 * Plugin for injecting Wiki data.
 * </p>
 * Created by The eXo Platform SAS
 * @Author : <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Jul 21, 2011  
 */
public class WikiDataInjector extends DataInjector {
  
  public enum CONSTANTS {
    TYPE("type"), DATA("data"), PERM("perm");
    private final String name;

    CONSTANTS(String name) {
      this.name = name;
    }
    
    public String getName() {
      return name;
    }
  };
  
  public static final String             ARRAY_SPLIT   = ",";
  
  private static Log         log             = ExoLogger.getLogger(WikiDataInjector.class);
  
  private WikiService wikiService;

  public WikiDataInjector(WikiService wikiService,  InitParams params) {
    this.wikiService = wikiService;
  }
  
  private void printInputParameters(HashMap<String, String> params) {
    StringBuilder sb = new StringBuilder();
    sb.append("PARAMS: \n");
    Iterator<String> keys = params.keySet().iterator();
    while (keys.hasNext()) {
      String key = keys.next();
      sb.append(String.format("%1$10s    :    %2$10s \n", key, params.get(key)));
    }
    log.info(sb.toString());
  }
  
  private List<Integer> readQuantities(HashMap<String, String> queryParams) {
    String quantitiesString = queryParams.get("q");
    List<Integer> quantities = new LinkedList<Integer>();
    for (String s : quantitiesString.split(ARRAY_SPLIT)) {
      if (s.length() > 0) {
        int quantity = Integer.parseInt(s.trim());
        quantities.add(quantity);
      }
    }
    return quantities;
  }
  
  private List<String> readPrefixes(HashMap<String, String> queryParams) {
    String prefixesString = queryParams.get("pre");
    List<String> prefixes = new LinkedList<String>();
    for (String s : prefixesString.split(ARRAY_SPLIT)) {
      if (s.length() > 0) {
        prefixes.add(s);
      }
    }
    return prefixes;
  }
  
  private String readWikiOwner(HashMap<String, String> queryParams) {
    return queryParams.get("wo");
  }
  
  private String readWikiType(HashMap<String, String> queryParams) {
    return queryParams.get("wt");
  }

  private int readMaxAttachmentIfExist(HashMap<String, String> queryParams) {
    String value = queryParams.get("maxAtt");
    if (value != null)
      return Integer.parseInt(value);
    else return 0;
  }
  
  private int readMaxPagesIfExist(HashMap<String, String> queryParams) {
    String value = queryParams.get("mP");
    if (value != null)
      return Integer.parseInt(value);
    else return 0;
  }
  
  private List<String> readGroupsIfExist(HashMap<String, String> queryParams) {
    List<String> groups = new LinkedList<String>();
    String value = queryParams.get("groups");
    if (value != null) {
      String[] groupsString = value.split(ARRAY_SPLIT);
      for (String s : groupsString) {
        if (s.length() > 0)
          groups.add(s.trim());
      }
    }
    return groups;
  }
  
  private List<String> readUsersIfExist(HashMap<String, String> queryParams) {
    List<String> users = new LinkedList<String>();
    String value = queryParams.get("users");
    if (value != null) {
      String[] groupsString = value.split(ARRAY_SPLIT);
      for (String s : groupsString) {
        if (s.length() > 0)
          users.add(s.trim());
      }
    }
    return users;    
  }
  
  private boolean readRecursive(HashMap<String, String> queryParams) {
    boolean recursive = false;
    String value = queryParams.get("rcs");
    if (value != null) {
      recursive = Boolean.parseBoolean(value);
    }
    return recursive;
  }
  
  private List<String> readMembershipIfExist(HashMap<String, String> queryParams) {
    List<String> memberships = new LinkedList<String>();
    
    String value = queryParams.get("memship");
    if (value != null) {
      String[] memshipsString = value.split(ARRAY_SPLIT);
      for (String s : memshipsString) {
        if (s.length() > 0)
          memberships.add(s.trim());
      }
    }
    return memberships;
  }
  
  private List<String> readPermission(HashMap<String, String> queryParams) {
    String permString = queryParams.get("perm");
    List<String> permissions = new LinkedList<String>();
    boolean flag = Integer.parseInt(permString.substring(0, 1)) > 0;
    if (flag) // check read permission
      permissions.add(PermissionType.READ);
    
    flag = Integer.parseInt(permString.substring(1, 2)) > 0;
    if (flag) { // check edit permission
      permissions.add(PermissionType.ADD_NODE);
      permissions.add(PermissionType.SET_PROPERTY);
      permissions.add(PermissionType.REMOVE);
    }
    return permissions;
  }
  
  private HashMap<String, String[]> readPermissions(HashMap<String, String> queryParams) {
    HashMap<String, String[]> permissions = new HashMap<String, String[]>();
    List<String> identities = new LinkedList<String>();
    String[] permsArr = readPermission(queryParams).toArray(new String[] {});
    identities.addAll(readGroupsIfExist(queryParams));
    identities.addAll(readUsersIfExist(queryParams));
    identities.addAll(readMembershipIfExist(queryParams));
    for (String identity : identities) {
      permissions.put(identity, permsArr);
    }
    return permissions;
  }
  
   
  private String makeTitle(String prefix, String fatherTitle, int order) {
    return prefix + " " + fatherTitle + " " + order;
  }
  
  private PageImpl createPage(PageImpl father, String title, String wikiOwner, String wikiType, int attSize) throws Exception {
    PageImpl page = (PageImpl) wikiService.createPage(wikiType, wikiOwner, title, father.getName());
    page.getContent().setText(randomParagraphs(10));
    if (attSize > 0) {
      page.createAttachment("att" + IdGenerator.generate() + ".txt",
                            Resource.createPlainText(createTextResource(attSize)));
    }
    return page;
  }
  
  private void generatePages(List<Integer> quantities,
                             List<String> prefixes,
                             int depth,
                             int attSize,
                             int totalPages,
                             String wikiOwner,
                             String wikiType, PageImpl father) throws Exception {
    int numOfPages = quantities.get(depth).intValue();
    String prefix = prefixes.get(depth);
    for (int i = 0; i < numOfPages; i++) {
      String title = makeTitle(prefix, father.getTitle(), i + 1);
      String pageId = TitleResolver.getId(title, true);
      PageImpl page;
      if (wikiService.isExisting(wikiType, wikiOwner, pageId)) {
        page = (PageImpl) wikiService.getPageById(wikiType, wikiOwner, pageId);
      } else {
        page = createPage(father, title, wikiOwner, wikiType, attSize);
      }
      if (page == null) return;
      log.info(String.format("%1$" + ((depth + 1)*4) + "s Process page: %2$s in depth %3$s .......", " ", page.getTitle(), depth + 1));
      if (depth < quantities.size() - 1) {
        generatePages(quantities, prefixes, depth + 1, attSize, totalPages, wikiOwner, wikiType, page);
      }
    }
  }
  
  private void injectData(HashMap<String, String> queryParams) throws Exception {
    log.info("Start to inject data ............... ");
    List<Integer> quantities = readQuantities(queryParams);
    List<String> prefixes = readPrefixes(queryParams);
    int attSize = readMaxAttachmentIfExist(queryParams);
    int totalPages = readMaxPagesIfExist(queryParams);
    String wikiOwner = readWikiOwner(queryParams);
    String wikiType = readWikiType(queryParams);

    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      generatePages(quantities, prefixes, 0, attSize, totalPages, wikiOwner, wikiType, (PageImpl) wikiService.getPageById(wikiType, wikiOwner, null));
    } finally {
      RequestLifeCycle.end();
    }
    
    log.info("Injecting data has been done successfully!");
  }
  
  private void grantPermission(List<Integer> quantities, List<String> prefixes, int depth, PageImpl father, String wikiOwner, String wikiType, HashMap<String, String[]> permissions, boolean isRecursive) throws Exception {
    int numOfPages = quantities.get(depth).intValue();
    String prefix = prefixes.get(depth);
    for (int i = 0; i < numOfPages; i++) {
      String title = makeTitle(prefix, father.getTitle(), i + 1);
      String pageId = TitleResolver.getId(title, true);
      PageImpl page = (PageImpl) wikiService.getPageById(wikiType, wikiOwner, pageId);
      if (page != null) {
        if (isRecursive || depth == (quantities.size() - 1)) {
          log.info(String.format("Grant permissions %1$s for page: %2$s .........", permissionsToString(permissions), page.getTitle()));
          page.setPagePermission(permissions);
        }
      }
      if (depth < quantities.size() - 1) {
        grantPermission(quantities, prefixes, depth + 1, page, wikiOwner, wikiType, permissions, isRecursive);
      }
    }
  }
  
  private String permissionsToString(HashMap<String, String[]> permission) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> itr = permission.keySet().iterator();
    sb.append("(");
    while (itr.hasNext()) {
      String key = itr.next();
      String[] value = permission.get(key);
      sb.append("[" + key + ":");
      for (String s : value) {
        sb.append(s).append(",");
      }
      sb.delete(sb.length() - 1, sb.length());
      sb.append("],");
    }
    if (sb.length() > 1) sb.delete(sb.length() - 1, sb.length());
    sb.append(")");
    return sb.toString();
  }
  
  private void grantPermission(HashMap<String, String> queryParams) throws Exception {
    log.info("Start to grant permissions ............... ");
    List<Integer> quantities = readQuantities(queryParams);
    List<String> prefixes = readPrefixes(queryParams);
    String wikiOwner = readWikiOwner(queryParams);
    String wikiType = readWikiType(queryParams);
    HashMap<String, String[]> permissions = readPermissions(queryParams);
    boolean isRecursive = readRecursive(queryParams);
    
    RequestLifeCycle.begin(PortalContainer.getInstance());
    try {
      grantPermission(quantities, prefixes, 0, (PageImpl) wikiService.getPageById(wikiType, wikiOwner, null), wikiOwner, wikiType, permissions, isRecursive);
    } finally {
      RequestLifeCycle.end();
    }
    log.info("Permissions have been granted successfully!");
  }
  
  @Override
  public void inject(HashMap<String, String> queryParams) throws Exception {
    printInputParameters(queryParams);
    String type = queryParams.get(CONSTANTS.TYPE.getName());
    if (CONSTANTS.DATA.getName().equalsIgnoreCase(type)) {
      injectData(queryParams);
    } else if (CONSTANTS.PERM.getName().equalsIgnoreCase(type)) {
      grantPermission(queryParams);
    }
  }

  @Override
  public void reject(HashMap<String, String> params) throws Exception {
    log.info("Start to reject data ............. ");
    printInputParameters(params);
    String wikiOwner = readWikiOwner(params);
    String wikiType = readWikiType(params);
    List<Integer> quantities = readQuantities(params);
    List<String> prefixes = readPrefixes(params);
    int numOfPages = quantities.get(0);
    String prefix = prefixes.get(0);
    RequestLifeCycle.begin(PortalContainer.getInstance());
    PageImpl wikiHome = (PageImpl) wikiService.getPageById(wikiType, wikiOwner, null);
    try {
      for (int i = 0; i < numOfPages; i++) {
        String title = makeTitle(prefix, wikiHome.getTitle(), i + 1);
        String pageId = TitleResolver.getId(title, true);
        if (wikiService.getPageById(wikiType, wikiOwner, pageId) != null) {
          if (log.isInfoEnabled()) 
            log.info(String.format("    Delete page: %1$s and its children ...", title));
          wikiService.deletePage(wikiType, wikiOwner, pageId);
        }
      }
    } finally {
      RequestLifeCycle.end();
    }
    log.info("Rejecting data has been done successfully!");
  }

  @Override
  public Log getLog() {
    return log;
  }

  @Override
  public Object execute(HashMap<String, String> params) throws Exception {
    return new Object();
  }

}

