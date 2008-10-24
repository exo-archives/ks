/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.ks.common.conf;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

/**
 * Created by The eXo Platform SARL
 * Author : Ha Mai
 *					ha.mai@exoplatform.com
 * Oct 16, 2008, 4:25:54 PM
 */
public class RoleRulesPlugin extends BaseComponentPlugin {
	private Map<String, List<String>> rules_ = new LinkedHashMap<String, List<String>>();

  @SuppressWarnings("unchecked")
  public RoleRulesPlugin(InitParams params) throws Exception {
  	ValueParam vlParam = params.getValueParam("role") ;
  	ValuesParam vlsParam = params.getValuesParam("rules") ;     
    rules_.put(vlParam.getValue(), vlsParam.getValues());
  }

  public List<String> getRules(String role) {
    return rules_.get(role);
  }
}
