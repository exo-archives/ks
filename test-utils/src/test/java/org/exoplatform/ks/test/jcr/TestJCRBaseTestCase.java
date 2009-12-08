/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.ks.test.jcr;


import javax.jcr.Session;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/jcr/jcr-configuration.xml")})
public class TestJCRBaseTestCase extends AbstractJCRBaseTestCase
{

   public void testWorkspace() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      //RepositoryService repos = (RepositoryService) ExoContainerContext.getCurrentContainer().getComponentInstance(RepositoryService.class);
      RepositoryService repos = (RepositoryService)container.getComponentInstanceOfType(RepositoryService.class);
      assertNotNull(repos);
      ManageableRepository repo = repos.getDefaultRepository();
      assertNotNull(repo);
      Session session = repo.getSystemSession("portal-test");
      assertNotNull(session);
      session.logout();
   }
}
