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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.ks.bbcode.core;

import java.util.Collection;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.ks.bbcode.api.BBCode;
import org.exoplatform.ks.bbcode.api.BBCodeService;
import org.exoplatform.ks.bbcode.spi.BBCodeProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Provides Extended BBCodes
 * @author <a href="mailto:patrice.lamarque@exoplatform.com">Patrice Lamarque</a>
 * @version $Revision$
 */
public class ExtendedBBCodeProvider implements BBCodeProvider {

  private static final Log log = ExoLogger.getLogger(ExtendedBBCodeProvider.class);

  protected BBCodeService  bbCodeService;

  protected BBCodeService getBBCodeService() {
    if (bbCodeService == null) {
      bbCodeService = (BBCodeService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(BBCodeService.class);
    }
    return bbCodeService;
  }

  public void setBBCodeService(BBCodeService bbCodeService) {
    this.bbCodeService = bbCodeService;
  }

  public BBCode getBBCode(String tagName) {
    try {
      return bbCodeService.findById(tagName);
    } catch (Exception e) {
      log.error(e);
    }
    return null;
  }

  public Collection<String> getSupportedBBCodes() {
    try {
      return getBBCodeService().getActive();
    } catch (Exception e) {
      log.error(e);
    }
    return null;
  }

}
