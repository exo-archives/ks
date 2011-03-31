/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.ks.bbcode.spi;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

/**
 * Managed plugin that holds registered BBCodes
 */
@Managed
@NameTemplate( { @Property(key = "service", value = "ks"), @Property(key = "view", value = "plugins"), @Property(key = "name", value = "{Name}") })
@ManagedDescription("Plugin that defines the initial BBCodes available")
public class BBCodePlugin extends BaseComponentPlugin {

  private List<BBCodeData> bbcodeData = new ArrayList<BBCodeData>();

  public BBCodePlugin() {
  }

  public BBCodePlugin(InitParams params) {
    bbcodeData = params.getObjectParamValues(BBCodeData.class);
  }

  public List<BBCodeData> getBBCodeData() {
    return bbcodeData;
  }

  public void setBbcodeData(List<BBCodeData> bbcodeDatas) {
    this.bbcodeData = bbcodeDatas;
  }

  @Managed
  @ManagedDescription("Get the list of BBCodes defined in this plugin")
  public List<String> getBBCodes() {
    List<String> result = new ArrayList<String>();
    List<BBCodeData> data = getBBCodeData();
    for (BBCodeData bbCodeData : data) {
      result.add(bbCodeData.getTagName());
    }
    return result;
  }

}
