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
package org.exoplatform.ks.common.conf;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.ks.common.conf.ManagedPlugin;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * 30-9-2009 - 15:25:33  
 */
public class InitialRSSListener extends ManagedPlugin{
	private boolean isInitRss_ = true ;

	public InitialRSSListener(InitParams params) {
		try {
			isInitRss_ = Boolean.parseBoolean(params.getValueParam("initRss").getValue()) ;
  	}catch(Exception e){}
	}
  
	public boolean isInitRssListener() {
  	return isInitRss_;
  }
}
