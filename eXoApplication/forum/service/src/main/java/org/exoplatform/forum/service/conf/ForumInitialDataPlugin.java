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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.service.conf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.ks.common.conf.ManagedPlugin;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform tu.duy@exoplatform.com Dec 7, 2010
 */
@Managed
@NameTemplate( { @Property(key = "service", value = "forum"), @Property(key = "view", value = "plugins"), @Property(key = "name", value = "{Name}") })
@ManagedDescription("Plugin that allows to initialize data for the forum")
public class ForumInitialDataPlugin extends ManagedPlugin {
  private static final String TEXT_XML        = "text/xml";

  private static final String APPLICATION_ZIP = "application/zip";

  private static Log          log             = ExoLogger.getLogger(ForumInitialDataPlugin.class);

  private List<String>        locations;

  private boolean             forceXML        = false;

  public ForumInitialDataPlugin(InitParams params) throws Exception {
    ValuesParam vp1 = params.getValuesParam("locations");
    if (vp1 == null) {
      log.warn("value-param 'location' is missing for " + getName() + ". The plugin will not be used");
    } else {
      this.locations = new ArrayList<String>(vp1.getValues());
    }

  }

  public void setLocation(List<String> locations) {
    this.locations = locations;
  }

  @Managed
  @ManagedDescription("The location where forum an XML export file will be looked")
  public List<String> getLocation() {
    return locations;
  }

  public List<ByteArrayInputStream> importData() throws RuntimeException {
    try {
      ConfigurationManager configurationService = (ConfigurationManager) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ConfigurationManager.class);
      List<ByteArrayInputStream> arrayInputStreams = new ArrayList<ByteArrayInputStream>();
      for (String location : locations) {
        if (!isZip(location)) {
          InputStream bis = configurationService.getInputStream(location);
          byte[] bdata = new byte[bis.available()];
          bis.read(bdata);
          arrayInputStreams.add(new ByteArrayInputStream(bdata));
        } else {
          ZipInputStream zipStream = new ZipInputStream(configurationService.getInputStream(location));
          try {
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
              ByteArrayOutputStream out = new ByteArrayOutputStream();
              int available = -1;
              byte[] data = new byte[2048];
              while ((available = zipStream.read(data, 0, 1024)) > -1) {
                out.write(data, 0, available);
              }
              zipStream.closeEntry();
              out.close();
              arrayInputStreams.add(new ByteArrayInputStream(out.toByteArray()));
            }
          } finally {
            zipStream.close();
          }
        }
      }
      return arrayInputStreams;
    } catch (Exception e) {
      log.error("The plugin " + getName() + " failed to initialize data " + e);
      throw new RuntimeException(e.getCause());
    }
  }

  boolean isZip(String fileName) {
    MimeTypeResolver mimeTypeResolver = new MimeTypeResolver();
    String mimeType = mimeTypeResolver.getMimeType(fileName);
    if (APPLICATION_ZIP.equals(mimeType)) {
      return true;
    } else if (TEXT_XML.equals(mimeType)) {
      return false;
    }
    throw new RuntimeException("The format " + mimeType + " is not supported. Expecting " + APPLICATION_ZIP + " or " + TEXT_XML);
  }

  public String toString() {
    return getName() + " (forceXML=" + forceXML + ",location=" + locations + ")";
  }

}
