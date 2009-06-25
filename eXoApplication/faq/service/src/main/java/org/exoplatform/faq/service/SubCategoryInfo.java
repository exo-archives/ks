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
package org.exoplatform.faq.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Jun 24, 2009 - 3:34:44 AM  
 */
public class SubCategoryInfo {
	private String path;
	private String name;
	private List<QuestionInfo> questionInfos = new ArrayList<QuestionInfo>();
	private List<SubCategoryInfo> subCateInfos = new ArrayList<SubCategoryInfo>();
	
	public SubCategoryInfo() {}
	
	public String getPath() {
  	return path;
  }
	public void setPath(String path) {
  	this.path = path;
  }
	
	public String getName() {
  	return name;
  }
	public void setName(String name) {
  	this.name = name;
  }
}
