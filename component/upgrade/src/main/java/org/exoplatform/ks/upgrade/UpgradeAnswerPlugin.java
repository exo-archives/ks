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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.faq.service.FAQNodeTypes;
import org.exoplatform.faq.service.Utils;
import org.exoplatform.ks.common.CommonUtils;
import org.exoplatform.ks.common.jcr.KSDataLocation;
import org.exoplatform.ks.common.jcr.PropertyReader;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Oct 4, 2011  
 */
public class UpgradeAnswerPlugin extends UpgradeProductPlugin {

  private static final Log    log              = ExoLogger.getLogger(UpgradeAnswerPlugin.class);

  private static final String GROUP_SPACE_ID   = "/spaces";

  private KSDataLocation      dataLocation;

  public UpgradeAnswerPlugin(InitParams initParams) {
    super(initParams);
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    this.dataLocation = ((KSDataLocation) container.getComponentInstance(KSDataLocation.class));
  }

  public void processUpgrade(String oldVersion, String newVersion) {
    // Upgrade from KS 2.1.x to 2.2.3
    log.info("\n\n\n\n -----------> processUpgrade Answer Migration......\n\n\n");
    SessionProvider sProvider = SessionProvider.createSystemProvider();
    try {
      log.info("\n\nMigration space....\n");
      migrationSpaceOfPLF(sProvider);
    } catch (Exception e) {
      log.warn("[UpgradeAnswerPlugin] Exception when migrate data from 2.1.x to 2.2.3 for Answer.", e);
    } finally {
      sProvider.close();
    }
    log.info("\n\n\n\n -----------> The end Answer Migration......\n\n\n");
  }

  private void migrationSpaceOfPLF(SessionProvider sProvider) throws Exception {
    Node cateHomeNode = getCategoryHomeNode(sProvider);
    NodeIterator cIter = cateHomeNode.getNodes();
    if (cIter.getSize() > 0) {
      String[] permission;
      PropertyReader reader;
      Session session = cateHomeNode.getSession();
      Map<String, String> groupIds = getAllGroupOfSpaces();
      String groupId, nodeName, newId, parentPath = cateHomeNode.getPath();
      while (cIter.hasNext()) {
        Node cNode = cIter.nextNode();
        if (cNode.isNodeType(FAQNodeTypes.EXO_FAQ_CATEGORY)) {
          nodeName = cNode.getName();
          log.info("\nMigration category : " + nodeName);
          reader = new PropertyReader(cNode);
          permission = reader.strings(FAQNodeTypes.EXO_USER_PRIVATE, new String[] { "" });
          groupId = getGroupId(permission);
          if (groupIds.containsKey(groupId)) {
            newId = Utils.CATE_SPACE_ID_PREFIX + groupId;
            try {
              if (groupId.equals(reader.string(FAQNodeTypes.EXO_NAME, ""))) {
                cNode.setProperty(FAQNodeTypes.EXO_NAME, groupIds.get(groupId));
              }
              cNode.setProperty(FAQNodeTypes.EXO_ID, newId);
              cNode.save();
              migradeChildrenItems(cNode, newId);
              if(!nodeName.equals(newId)) {
                session.move(cNode.getPath(), parentPath + "/" + newId);
                session.save();
              }
              log.info(String.format("Rename node of category %s to %s", nodeName, newId));
            } catch (Exception e) {
              log.info(String.format("Failed to rename node of category %s to %s", nodeName, newId));
            }
          }
        }
      }
    }
  }
  
  private void migradeChildrenItems(Node cateNode, String newName) throws Exception {
    NodeIterator iterator = getNodeIterator(cateNode);
    while (iterator.hasNext()) {
      Node cNode = iterator.nextNode();
      try {
        if (cNode.hasProperty(FAQNodeTypes.EXO_CATEGORY_ID)) {
          log.info("\nSet new category id for items children: " + cNode.getName());
          cNode.setProperty(FAQNodeTypes.EXO_CATEGORY_ID, newName);
        }
      } catch (Exception e) {
        log.warn(String.format("Failed to set new name %s for category", newName), e);
      }
    }
    cateNode.save();
  }

  private NodeIterator getNodeIterator(Node node) throws Exception {
    QueryManager qm = node.getSession().getWorkspace().getQueryManager();
    StringBuilder pathQuery = new StringBuilder(FAQNodeTypes.JCR_ROOT).append(node.getNode(Utils.QUESTION_HOME).getPath()).append("//*");
    Query query = qm.createQuery(pathQuery.toString(), Query.XPATH);
    QueryResult result = query.execute();
    return result.getNodes();
  }
  
  private String getGroupId(String[] grs) throws Exception {
    String s;
    for (int i = 0; i < grs.length; i++) {
      s = grs[i];
      if(s.indexOf("/spaces/") >= 0) {
        s = s.substring(s.lastIndexOf("/")+1);
        if(!CommonUtils.isEmpty(s)) return s;
      }
    }
    return "";
  }

  private Node getCategoryHomeNode(SessionProvider sProvider) throws Exception {
    return getNodeByPath(dataLocation.getFaqCategoriesLocation(), sProvider);
  }

  public Node getNodeByPath(String nodePath, SessionProvider sessionProvider) throws Exception {
    if (nodePath.indexOf("/") == 0) {
      nodePath = nodePath.substring(1);
    }
    return getSession(sessionProvider).getRootNode().getNode(nodePath);
  }

  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isBefore(previousVersion, newVersion);
  }

  @SuppressWarnings("deprecation")
  private Session getSession(SessionProvider sProvider) throws Exception {
    return dataLocation.getSessionManager().getSession(sProvider);
  }

  @SuppressWarnings("unchecked")
  private Map<String, String> getAllGroupOfSpaces() throws Exception {
    Map<String, String> groupIds = new HashMap<String, String>();
    try {
      PortalContainer container = PortalContainer.getInstance();
      OrganizationService organizationService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
      ((ComponentRequestLifecycle)organizationService).startRequest(container);
      Group group = organizationService.getGroupHandler().findGroupById(GROUP_SPACE_ID);
      if (group != null) {
        Collection<Group> groups = organizationService.getGroupHandler().findGroups(group);
        for (Group gr : groups) {
          groupIds.put(gr.getGroupName(), gr.getLabel());
        }
      }
      log.info("\n\n ------> all group: " + groupIds.keySet().toString());
      ((ComponentRequestLifecycle)organizationService).endRequest(container);
    } catch (Exception e) {
      log.warn("\nFailed to get all groups in spaces.", e);
    }
    return groupIds;
  }

}
