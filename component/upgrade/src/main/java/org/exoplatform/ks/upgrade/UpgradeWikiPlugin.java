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

import org.exoplatform.commons.upgrade.UpgradeProductPlugin;
import org.exoplatform.commons.version.util.VersionComparator;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.core.nodetype.ExtendedNodeTypeManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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
    log.info("\n\n\n\n -----------> End Wiki Migration......\n\n\n");
  }

  @Override
  public boolean shouldProceedToUpgrade(String newVersion, String previousVersion) {
    boolean doUpgrade = VersionComparator.isBefore(previousVersion, "2.2.4-SNAPSHOT")
        && VersionComparator.isBefore("2.2.3", newVersion)
        && VersionComparator.isBefore(newVersion, "2.2.6-SNAPSHOT");
    return doUpgrade;
  }

}
