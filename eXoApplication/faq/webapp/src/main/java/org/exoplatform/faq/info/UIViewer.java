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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 ***************************************************************************/
package org.exoplatform.faq.info;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 24, 2009 - 4:32:48 AM  
 */

@ComponentConfig(
		template =	"app:/templates/faq/webui/UIViewer.gtmpl",
		events = {
				
		}
)
public class UIViewer extends UIContainer {
	private List<CategoryInfo> categoryInfos = new ArrayList<CategoryInfo>();
	public UIViewer() {
  }
	
	public List<CategoryInfo> getCategoryInfoList() {
	  return categoryInfos;
  }
	
	public void setCategoryInfoList(List<CategoryInfo> categoryInfos) {
		this.categoryInfos = categoryInfos;
  }
}
