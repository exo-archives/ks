/***************************************************************************
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.common.conf.ManagedPlugin;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 23-10-2008 - 07:57:47  
 */
@Managed
@NameTemplate({@Property(key="service", value="faq"), @Property(key="view", value="plugins"), @Property(key="name", value="{Name}")})
@ManagedDescription("Plugin that defines the initial BBCodes available")
public class InitBBCodePlugin extends ManagedPlugin {
	private BBCodePlugin initialData = new BBCodePlugin();
  public InitBBCodePlugin(InitParams params) throws Exception {
  	initialData = (BBCodePlugin)params.getObjectParam("bbcode.default.configuration").getObject();
  }
  
  public BBCodePlugin getBBCodePlugin() {
	  return initialData ;
  }
  
  @Managed
  @ManagedDescription("Get the list of BBCodes defined in this plugin")
  public List<String> getBBCodes() {
    List<String> result = new ArrayList<String>();
   List<BBCodeData> data = initialData.getBbcodeDatas(); 
   for (BBCodeData bbCodeData : data) {
     result.add(bbCodeData.getTagName());
   }
   return result;
  }
  

}
