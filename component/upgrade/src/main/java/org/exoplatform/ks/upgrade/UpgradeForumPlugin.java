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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.upgrade;

import java.io.InputStream;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.forum.service.Utils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Oct 4, 2011  
 */
public class UpgradeForumPlugin extends UpgradeProductPlugin {

  private static final Log    log              = ExoLogger.getLogger(UpgradeForumPlugin.class);

  private static final String NEW_DOAMIN_FORUM = "new.domain.forum";

  private String              newDomain        = "";

  private KSDataLocation      dataLocation;

  public UpgradeForumPlugin(InitParams initParams) {
    super(initParams);
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.dataLocation = ((KSDataLocation) container.getComponentInstance(KSDataLocation.class));
    getNewDomain();
  }

  public void processUpgrade(String oldVersion, String newVersion) {
    // Upgrade from KS 2.1.x to 2.2.3
    log.info("\n\n\n\n -----------> processUpgrade Forum Migration......\n\n\n");
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      // register new nodeTypes
      log.info("\n\nRegister new nodeTypes...\n");
      registerNodeTypes("jar:/conf/portal/forum-nodetypes.xml", ExtendedNodeTypeManager.IGNORE_IF_EXISTS);
      registerNodeTypes("jar:/conf/portal/forum-migrate-nodetypes.xml", ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
      log.info("\n\nMigration forum data....\n");
      migrationForumData(sProvider);
      log.info("\n\nMigration space....\n");
      migrationSpaceOfPLF(sProvider);
    } catch (Exception e) {
      log.warn("[UpgradeForumPlugin] Exception when migrate data from 2.1.x to 2.2.3 for Forum.", e);
    } finally {
      sProvider.close();
    }
    log.info("\n\n\n\n -----------> The end Forum Migration......\n\n\n");
  }

  private void getNewDomain() {
    log.info("\nGet new domain for migration forum datas...");
    try {
      Properties props = new Properties(System.getProperties());
      newDomain = props.getProperty(NEW_DOAMIN_FORUM);
      log.info("\nnewDomain: " + newDomain);
    } catch (Exception e) {
      log.warn("Failed to get new domain in system configation. ", e);
    }
  }

  private void migrationForumData(SessionProvider sProvider) throws Exception {
    Node forumHome = getForumHomeNode(sProvider);
    // Migration 2.1.x to 2.2.3
    // properties new: exo:isWatting in nodetype: exo:post
    NodeIterator pIter = getNodeIterator(sProvider, forumHome, Utils.EXO_POST, new StringBuilder(""));
    log.info("\nThe size of list post migration: " + pIter.getSize());
    String link = null;
    while (pIter.hasNext()) {
      Node pNode = pIter.nextNode();
      log.info("Migration node " + pNode.getName() + " properties:  ");
      try {
        pNode.getProperty(Utils.EXO_IS_WAITING);
      } catch (Exception e) {
        pNode.addMixin("exo:forumMigrate");
        pNode.setProperty(Utils.EXO_IS_WAITING, false);
        System.out.print(" exo:isWatting=false");
      }
      // set new link for this post.
      if (!Utils.isEmpty(newDomain)) {
        link = calculateURL(new PropertyReader(pNode).string(Utils.EXO_LINK, ""));
        if (!Utils.isEmpty(link)) {
          pNode.setProperty(Utils.EXO_LINK, link);
          System.out.print(" ,exo:link=" + link);
        }
      }
    }
    // set new link for topics.
    if (!Utils.isEmpty(newDomain)) {
      NodeIterator tIter = getNodeIterator(sProvider, forumHome, Utils.EXO_TOPIC, new StringBuilder(""));
      log.info("The size of list topic migration: " + tIter.getSize());
      while (tIter.hasNext()) {
        Node tNode = tIter.nextNode();
        link = calculateURL(new PropertyReader(tNode).string(Utils.EXO_LINK, ""));
        if (!Utils.isEmpty(link)) {
          tNode.setProperty(Utils.EXO_LINK, link);
          log.info("Migration node " + tNode.getName() + " properties:  ,exo:link=" + link);
        }
      }
    }
    forumHome.getSession().save();
  }

  private String calculateURL(String oldUrl) {
    if (!Utils.isEmpty(oldUrl) && oldUrl.indexOf(newDomain) < 0) {
      oldUrl = newDomain + oldUrl.substring(oldUrl.indexOf("/", 8));
    }
    return "";
  }

  private void migrationSpaceOfPLF(SessionProvider sProvider) throws Exception {
    Node forumHome = getForumHomeNode(sProvider);
    NodeIterator cIter = getOldAllNodeCateSpace(sProvider, forumHome);
    log.info("\nNumber spaces migration: " + cIter.getSize());
    if (cIter.getSize() > 0) {
      NodeIterator fIter;
      String[] permission;
      PropertyReader reader;
      String fName = null;
      Node newSpNode = getCatNSPNode(sProvider, false);
      String newSpPath = newSpNode.getPath();
      log.info("\nPath of category Spaces: " + newSpPath);
      Session session = forumHome.getSession();
      Map<String, String> scInfo = new HashMap<String, String>();
      try {
        log.info("\nGet info data social ....");
        scInfo = getInfoSocial(sProvider);
        log.info("\nSize of spaces: " + scInfo.size());
      } catch (Exception e) {
        e.printStackTrace();
      }
      Set<String> permissions = new HashSet<String>();
      permissions.add("/spaces");
      while (cIter.hasNext()) {
        Node cNode = cIter.nextNode();
        reader = new PropertyReader(cNode);
        permission = reader.strings(Utils.EXO_USER_PRIVATE, new String[] { "" });
        log.info("Migration category : " + cNode.getName());
        fIter = cNode.getNodes();
        while (fIter.hasNext()) {
          Node fNode = fIter.nextNode();
          if (fNode.isNodeType(Utils.EXO_FORUM) && !hasNodeInSP(sProvider, fNode.getName())) {
            fNode.setProperty(Utils.EXO_POSTER, permission);
            fNode.setProperty(Utils.EXO_CREATE_TOPIC_ROLE, permission);
            fNode.setProperty(Utils.EXO_VIEWER, permission);
            fNode.save();
            fName = fNode.getProperty(Utils.EXO_NAME).getString();
            if (scInfo.containsKey(fName)) {
              fName = Utils.FORUM + scInfo.get(fName);
            } else {
              fName = fNode.getName();
            }
            session.move(fNode.getPath(), newSpPath + "/" + fName);
            session.save();
            log.info(String.format("Move forum %s in to category Spaces with new node name %s", fNode.getName(), fName));
          }
        }
        permissions.addAll(Arrays.asList(permission));
        log.info(String.format("Remove old category space: %s ", cNode.getName()));
        cNode.remove();
        session.save();
      }
      newSpNode.setProperty(Utils.EXO_USER_PRIVATE, permissions.toArray(new String[permissions.size()]));
      forumHome.save();
    }
  }

  private boolean hasNodeInSP(SessionProvider sProvider, String id) {
    try {
      getCatNSPNode(sProvider, false).getNode(id); 
    } catch (Exception e) {
      return false;
    }
    return true;
  }
  
  private Node getForumHomeNode(SessionProvider sProvider) throws Exception {
    return getNodeByPath(dataLocation.getForumHomeLocation(), sProvider);
  }

  private Node getCatNSPNode(SessionProvider sProvider, boolean isClear) throws Exception {
    Node categoryHome = getNodeByPath(dataLocation.getForumCategoriesLocation(), sProvider);
    Node catNode = null;
    String id = Utils.CATEGORY + "spaces";
    try {
      catNode = categoryHome.getNode(id);
      if (isClear) {
        catNode.remove();
        categoryHome.getSession().save();
      }
    } catch (PathNotFoundException e) {
    }
    if (catNode == null) {
      catNode = categoryHome.addNode(id, Utils.EXO_FORUM_CATEGORY);
      catNode.setProperty(Utils.EXO_ID, id);
      catNode.setProperty(Utils.EXO_OWNER, "");
      catNode.setProperty(Utils.EXO_CREATED_DATE, GregorianCalendar.getInstance());
      catNode.setProperty(Utils.EXO_NAME, "Spaces");
      catNode.setProperty(Utils.EXO_CATEGORY_ORDER, 100);
      catNode.setProperty(Utils.EXO_DESCRIPTION, "The category sotorage all forums of spaces.");
      catNode.setProperty(Utils.EXO_MODIFIED_BY, "");
      catNode.setProperty(Utils.EXO_MODIFIED_DATE, GregorianCalendar.getInstance());
      catNode.setProperty(Utils.EXO_USER_PRIVATE, new String[]{"/spaces"});
      catNode.getSession().save();
    }
    return catNode;
  }

  private NodeIterator getOldAllNodeCateSpace(SessionProvider sProvider, Node forumHome) throws Exception {
    StringBuilder strQuery = new StringBuilder("[(@").append(Utils.EXO_NAME).append("='spaces') and (jcr:contains(@").append(Utils.EXO_USER_PRIVATE).append(", 'spaces'))]");
    return getNodeIterator(sProvider, forumHome, Utils.EXO_FORUM_CATEGORY, strQuery);
  }

  private NodeIterator getNodeIterator(SessionProvider sProvider, Node node, String nodeType, StringBuilder strQuery) throws Exception {
    QueryManager qm = node.getSession().getWorkspace().getQueryManager();
    StringBuilder pathQuery = new StringBuilder(Utils.JCR_ROOT).append(node.getPath()).append("//element(*,").append(nodeType).append(")").append(strQuery).append(" order by @").append(Utils.EXO_NAME).append(" descending");
    Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    if (nodePath.indexOf("/") == 0) {
      nodePath = nodePath.substring(1);
    }
    return getSession(sessionProvider).getRootNode().getNode(nodePath);
  }

  private void registerNodeTypes(String nodeTypeFilesName, int alreadyExistsBehaviour) throws Exception {
    ConfigurationManager configurationService = (ConfigurationManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ConfigurationManager.class);
    InputStream isXml = configurationService.getInputStream(nodeTypeFilesName);
    ExtendedNodeTypeManager ntManager = dataLocation.getRepositoryService().getDefaultRepository().getNodeTypeManager();
    log.info("\nTrying register node types from xml-file " + nodeTypeFilesName);
    ntManager.registerNodeTypes(isXml, alreadyExistsBehaviour, NodeTypeDataManager.TEXT_XML);
    log.info("\nNode types were registered from xml-file " + nodeTypeFilesName);
  }

  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    // boolean doUpgrade = VersionComparator.isSame("0", previousVersion) && ((VersionComparator.isSame("2.2.3-SNAPSHOT", newVersion)) || (VersionComparator.isSame("2.2.3", newVersion)));
    // return doUpgrade;
    return true;
  }

  @SuppressWarnings("deprecation")
  private Session getSession(SessionProvider sProvider) throws Exception {
    return dataLocation.getSessionManager().getSession(sProvider);
  }

  private Map<String, String> getInfoSocial(SessionProvider sProvider) throws Exception {
    NodeIterator csIter = getAllSpace(sProvider);
    Map<String, String> info = new HashMap<String, String>();
    while (csIter.hasNext()) {
      PropertyReader reader = new PropertyReader(csIter.nextNode());
      String prName = reader.string("soc:name");
      String dsName = reader.string("soc:displayName");
      info.put(dsName, prName);
    }
    return info;
  }

  private NodeIterator getAllSpace(SessionProvider sProvider) throws Exception {
    return getNodeIterator(sProvider, getSocialHome(sProvider), "soc:spacedefinition", new StringBuilder());
  }

  private Node getSocialHome(SessionProvider sProvider) throws Exception {
    return getSessionSocial(sProvider).getRootNode().getNode("production/soc:spaces");
  }

  private Session getSessionSocial(SessionProvider sProvider) throws Exception {
    ManageableRepository repository = dataLocation.getRepositoryService().getCurrentRepository();
    return sProvider.getSession("social", repository);
  }

}
