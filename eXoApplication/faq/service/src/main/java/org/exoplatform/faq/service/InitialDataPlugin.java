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
package org.exoplatform.faq.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.exoplatform.commons.utils.MimeTypeResolver;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.ks.common.conf.ManagedPlugin;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * A plugin to initialize FAQ data from xml export. By default the data will be imported to root category only if it does not already exist
 * <value-params> are : 
 * <ul>
 * <li><strong>location</strong> : configuration url for the data to import</li>
 * <li><strong>forceXML</strong> (optional) : indicates if  XML data should override existing data</li>
 * </ul>
 */
@Managed
@NameTemplate( { @Property(key = "service", value = "faq"), @Property(key = "view", value = "plugins"), @Property(key = "name", value = "{Name}") })
@ManagedDescription("Plugin that allows to initialize default data for the FAQ")
public class InitialDataPlugin extends ManagedPlugin {

  private static final String TEXT_XML        = "text/xml";

  private static final String APPLICATION_ZIP = "application/zip";

  private static Log          log             = ExoLogger.getLogger(InitialDataPlugin.class);

  private String              location;

  private boolean             forceXML        = false;

  public InitialDataPlugin(InitParams params) {

    ValueParam vp1 = params.getValueParam("location");
    if (vp1 == null) {
      log.warn("value-param 'location' is missing for " + getName() + ". The plugin will not be used");
    } else {
      this.location = vp1.getValue();
    }

    ValueParam vp2 = params.getValueParam("forceXML");
    if (vp2 != null) {
      try {
        forceXML = Boolean.valueOf(vp2.getValue());
      } catch (Exception e) {
        log.warn("value-param 'forceXML' is erroneous for " + getName() + ". Expected 'true' or 'false', received '" + vp2.getValue() + "'. Using " + forceXML);
      }
    }

  }

  public void setLocation(String location) {
    this.location = location;
  }

  public void setForceXML(boolean forceXML) {
    this.forceXML = forceXML;
  }

  @Managed
  @ManagedDescription("The location where FAQ an XML export file will be looked")
  public String getLocation() {
    return location;
  }

  @Managed
  @ManagedDescription("Indicate if the data loaded should override any data found in the existing database")
  public boolean isForceXML() {
    return forceXML;
  }

  public boolean importData(FAQService service, ConfigurationManager configurationService) throws RuntimeException {

    try {
      if (location == null) {
        log.warn("No data location provided for " + this);
        return false;
      }

      boolean isZip = isZip(location);

      if (!isZip) {
        throw new RuntimeException("the .zip FAQ export format is expected");
      }

      String categoryId = readCategoryFromZipEntry(configurationService);
      if (categoryId == null) {
        throw new RuntimeException("Could not extract category id from .zip . Is it a real FAQ export?");
      }

      Category oCat = service.getCategoryById(Utils.CATEGORY_HOME + "/" + categoryId);
      if (oCat != null) {
        log.info("FAQ data in " + location + " was not imported. The category '" + oCat.getName() + "' already exists.");
        return false;
      } else {
        InputStream inputStream = configurationService.getInputStream(location);
        boolean result = service.importData(Utils.CATEGORY_HOME, inputStream, isZip);
        return result;
      }

    } catch (Exception e) {
      log.error("The plugin " + getName() + " failed to initialize data " + e);
      throw new RuntimeException(e.getCause());
    }
  }

  private String readCategoryFromZipEntry(ConfigurationManager configurationService) throws Exception {

    InputStream inputStream = null;
    ZipInputStream zipStream = null;
    try {
      inputStream = configurationService.getInputStream(location);
      zipStream = new ZipInputStream(inputStream);
      ZipEntry entry;

      while ((entry = zipStream.getNextEntry()) != null) {
        String name = entry.getName();
        zipStream.closeEntry();
        String result = name.substring(0, name.lastIndexOf(".xml"));
        return result;
      }
      return null;

    } finally {
      safeClose(inputStream);
      safeClose(zipStream);
    }
  }

  private void safeClose(InputStream inputStream) {
    if (inputStream != null) {
      try {
        inputStream.close();
      } catch (IOException e) {
        if (log.isDebugEnabled()) {
          log.debug("Can not close input stream", e);
        }
      }
    }
  }

  /**
   * Look for the name of the category in the input file
   * @param configurationService
   * @return
   */
  protected String readCategoryFromXml(ConfigurationManager configurationService) throws Exception, UnsupportedEncodingException, IOException {
    InputStream inputStream = null;
    try {
      String importedCategoryId = null;
      inputStream = configurationService.getInputStream(location);

      boolean keepReading = true;
      StringBuffer sbuf = new StringBuffer();
      byte[] buf = new byte[1024];

      String patternStr = "name=\"(\\S*)\""; // match stuf like sv:name="CategoryXYZ" and captures CategoryXYZ
      Pattern pattern = Pattern.compile(patternStr);

      while ((inputStream.read(buf) > 0) && keepReading) {
        sbuf.append(new String(buf, "UTF-8"));

        String content = sbuf.substring(0);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
          importedCategoryId = matcher.group(1);
          break; // stop reading
        }
      }
      return importedCategoryId;
    } finally {
      safeClose(inputStream);
    }
  }

  protected void createCategory(FAQService service, String categoryName) throws Exception {
    Category categ = new Category();
    categ.setCreatedDate(new Date());
    categ.setName(categoryName);
    categ.setModerators(new String[0]);
    categ.setIndex(11L);
    service.saveCategory(Utils.CATEGORY_HOME, categ, true);
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
    return getName() + " (forceXML=" + forceXML + ",location=" + location + ")";
  }
}
