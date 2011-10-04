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
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.jcr.core.nodetype.NodeTypeDataManager;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
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
    if (initParams.containsKey(NEW_DOAMIN_FORUM)) {
      newDomain = initParams.getValueParam(NEW_DOAMIN_FORUM).getValue();
    }
  }

  public void processUpgrade(String oldVersion, String newVersion) {
    // Upgrade from KS 2.1.x to 2.2.3
    log.info("\n\n\n\n -----------> processUpgrade Forum Migration......\n\n\n");
    SessionProvider sProvider = createSystemProvider();
    log.info("==========> wp: " + dataLocation.getWorkspace());
    try {
      Node forumHome = getForumHomeNode(sProvider);
      // register new nodeTypes
      log.info("\n\nRegister new nodeTypes...\n");
      registerNodeTypes("jar:/conf/portal/forum-nodetypes.xml");
      registerNodeTypes("jar:/conf/portal/forum-migrate-nodetypes.xml");
      log.info("\n\nMigration forum data....\n");
      migrationForumData(forumHome);
      log.info("\n\nMigration space....\n");
      migrationSpaceOfPLF(sProvider, forumHome);
    } catch (Exception e) {
      log.warn("[UpgradeForumPlugin] Exception when migrate data from 2.1.x to 2.2.3 for Forum.", e);
    }
    log.info("\n\n\n\n -----------> The end Forum Migration......\n\n\n");
  }

  private void migrationForumData(Node forumHome) throws Exception {
    // Migration 2.1.x to 2.2.3
    // properties new: exo:isWatting in nodetype: exo:post
    NodeIterator pIter = getNodeIterator(forumHome, Utils.EXO_POST, new StringBuilder(""));
    log.info("The size of list post migration: " + pIter.getSize());
    while (pIter.hasNext()) {
      Node pNode = pIter.nextNode();
      if(!pNode.hasProperty(Utils.EXO_IS_WAITING)){
        pNode.addMixin("exo:forumMigrate");
      }
      pNode.setProperty(Utils.EXO_IS_WAITING, new PropertyReader(pNode).bool(Utils.EXO_IS_WAITING, false));
      // set new link for this post.
      if (!Utils.isEmpty(newDomain)) {
        pNode.setProperty(Utils.EXO_LINK, calculateURL(new PropertyReader(pNode).string(Utils.EXO_LINK, "")));
      }
      pNode.save();
    }

    // set new link for topics.
    if (!Utils.isEmpty(newDomain)) {
      NodeIterator tIter = getNodeIterator(forumHome, Utils.EXO_POST, new StringBuilder(""));
      log.info("The size of list topic migration: " + pIter.getSize());
      while (tIter.hasNext()) {
        Node tNode = tIter.nextNode();
        tNode.setProperty(Utils.EXO_LINK, calculateURL(new PropertyReader(tNode).string(Utils.EXO_LINK, "")));
        tNode.save();
      }
    }
  }

  private String calculateURL(String oldUrl) {
    if (!Utils.isEmpty(oldUrl)) {
      oldUrl = newDomain + oldUrl.substring(oldUrl.indexOf("/", 8));
    }
    return oldUrl;
  }

  private void migrationSpaceOfPLF(SessionProvider sProvider, Node forumHome) throws Exception {
    NodeIterator cIter = getOldAllNodeCateSpace(forumHome);
    log.info("Number spaces migration: " + cIter.getSize());
    if (cIter.getSize() > 0) {
      NodeIterator fIter;
      String[] permission;
      PropertyReader reader;
      String newSpPath = null;
      Session session = forumHome.getSession();
      while (cIter.hasNext()) {
        Node cNode = cIter.nextNode();
        reader = new PropertyReader(cNode);
        permission = reader.strings(Utils.EXO_USER_PRIVATE, new String[] { "" });
        log.info("Migration category : " + cNode.getName());
        fIter = cNode.getNodes();
        while (fIter.hasNext()) {
          Node fNode = cIter.nextNode();
          if (fNode.isNodeType(Utils.EXO_FORUM)) {
            fNode.setProperty(Utils.EXO_POSTER, permission);
            fNode.setProperty(Utils.EXO_CREATE_TOPIC_ROLE, permission);
            fNode.setProperty(Utils.EXO_VIEWER, permission);
            fNode.save();
            if (newSpPath == null) {
              newSpPath = getCatNSPNode(sProvider).getPath();
              log.info("Path of category Spaces: " + newSpPath);
            }
            log.info(String.format("move forum %s in to category Spaces", fNode.getName()));
            session.move(fNode.getPath(), newSpPath + "/" + fNode.getName());
          }
        }
        session.save();
      }
    }
  }

  private Node getForumHomeNode(SessionProvider sProvider) throws Exception {
    return getNodeByPath(dataLocation.getForumHomeLocation(), sProvider);
  }

  private Node getCatNSPNode(SessionProvider sProvider) throws Exception {
    Node categoryHome = getNodeByPath(dataLocation.getForumCategoriesLocation(), sProvider);
    Node newSpNode = null;
    try {
      newSpNode = categoryHome.getNode(Utils.CATEGORY + "spaces");
    } catch (PathNotFoundException e) {
      newSpNode = categoryHome.addNode(Utils.CATEGORY + "spaces", Utils.EXO_FORUM_CATEGORY);
      newSpNode.getSession().save();
    }
    return newSpNode;
  }

  private NodeIterator getOldAllNodeCateSpace(Node forumHome) throws Exception {
    StringBuilder strQuery = new StringBuilder("[(@").append(Utils.EXO_NAME).append("='spaces') and (jcr:contains(@").append(Utils.EXO_USER_PRIVATE).append(", 'spaces'))]");
    return getNodeIterator(forumHome, Utils.EXO_FORUM_CATEGORY, strQuery);
  }

  private NodeIterator getNodeIterator(Node node, String nodeType, StringBuilder strQuery) throws Exception {
    QueryManager qm = node.getSession().getWorkspace().getQueryManager();
    StringBuilder pathQuery = new StringBuilder(Utils.JCR_ROOT).append(node.getPath()).append("//element(*,").append(nodeType).append(")").append(strQuery).append(" order by @").append(Utils.EXO_CREATED_DATE).append(" descending");
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

  private void registerNodeTypes(String nodeTypeFilesName) throws Exception {
    ConfigurationManager configurationService = (ConfigurationManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ConfigurationManager.class);
    InputStream inXml = configurationService.getInputStream(nodeTypeFilesName);
    ExtendedNodeTypeManager ntManager = dataLocation.getRepositoryService().getDefaultRepository().getNodeTypeManager();
    log.info("\nTrying register node types from xml-file " + nodeTypeFilesName);
    ntManager.registerNodeTypes(inXml, ExtendedNodeTypeManager.IGNORE_IF_EXISTS, NodeTypeDataManager.TEXT_XML);
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

  private SessionProvider createSystemProvider() {
    SessionProviderService sessionProviderService = (SessionProviderService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SessionProviderService.class);
    return sessionProviderService.getSystemSessionProvider(null);
  }

}
