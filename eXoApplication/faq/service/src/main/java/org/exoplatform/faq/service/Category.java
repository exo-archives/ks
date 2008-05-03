/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 **/
package org.exoplatform.faq.service;

import java.util.Date;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Category {
  private String id ;
  private String name ;
  private String description ;
  private boolean isModerateQuestions = true ;
  private String[] moderators ;
  private Date createdDate ;
  
  
  public Category() {
    id = "Category" + IdGenerator.generate() ;
  }
  
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }

  public String getDescription() { return description ; }
  public void setDescription(String description) { this.description = description ; }	
  
  public String[] getModerators() { return moderators ; }
  public void setModerators(String[] mods) { this.moderators = mods ; }
  
  public Date getCreatedDate() { return createdDate ; }
  public void setCreatedDate(Date date) { this.createdDate = date ; }

  public void setModerateQuestions(boolean isMod) { isModerateQuestions = isMod ; }
  public boolean isModerateQuestions() { return isModerateQuestions ; }
  
}
