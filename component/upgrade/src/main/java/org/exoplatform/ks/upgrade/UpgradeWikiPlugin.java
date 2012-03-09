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
package org.exoplatform.ks.upgrade;

import java.util.Iterator;

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.wiki.mow.api.Model;
import org.exoplatform.wiki.mow.core.api.MOWService;
import org.exoplatform.wiki.mow.core.api.WikiStoreImpl;
import org.exoplatform.wiki.mow.core.api.wiki.PageImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Oct 27, 2011  
 */
public class UpgradeWikiPlugin extends UpgradeProductPlugin {

  private static final Log log = ExoLogger.getLogger(UpgradeWikiPlugin.class);

  public UpgradeWikiPlugin(InitParams initParams) {
    super(initParams);
  }

  @Override
  public void processUpgrade(String oldVersion, String newVersion) {
    log.info(String.format("\n\n\n\n -----------> Migrating data from %s to %s for Wiki......\n\n\n", oldVersion, newVersion));
    try {
      // register new nodeTypes
      log.info("\n\nRegister new nodeTypes...\n");
      UpgradeUtils.registerNodeTypes("jar:/conf/portal/wiki-migrate-nodetypes.xml", ExtendedNodeTypeManager.REPLACE_IF_EXISTS);
    } catch (Exception e) {
      log.warn(String.format("[UpgradeWikiPlugin] Exception when migrate data from %s to %s for Wiki.", oldVersion, newVersion), e);
    }
    
    try {
      log.info("\n\nCheck and remove old help data...\n");
      removeOldHelpData();
    } catch (Exception e) {
      log.warn("[UpgradeWikiPlugin] Exception when Check and remove old help data for wiki:", e);
    }
    
    log.info("\n\n\n\n -----------> End Wiki Migration......\n\n\n");
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    return VersionComparator.isBefore(previousVersion, newVersion);
  }
  
  public void removeOldHelpData() throws Exception {
    RequestLifeCycle.begin(PortalContainer.getInstance());
    
    MOWService mowService = (MOWService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MOWService.class);
    Model model = mowService.getModel();
    WikiStoreImpl wStore = (WikiStoreImpl) model.getWikiStore();
    
    Iterator<PageImpl> syntaxPageIterator = wStore.getHelpPagesContainer().getChildPages().values().iterator();
    while (syntaxPageIterator.hasNext()) {
      PageImpl syntaxPage = syntaxPageIterator.next();
      if (syntaxPage.getName().toLowerCase().indexOf("xwiki") > -1) {
        continue;
      }
      syntaxPage.remove();
    }
    RequestLifeCycle.end();
  }
}
