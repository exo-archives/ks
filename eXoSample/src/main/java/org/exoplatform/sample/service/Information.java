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
package org.exoplatform.sample.service;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */

public class Information {
  private String id ;
  private String name ;
  private String height ;
  private String weight ;
  private String YOB ;
  private String location ;
  
  public Information() {}
  
  public Information(String id, String name, String height, String weight, String yob, String location) {
  	this.id = id ;
  	this.name = name ;
  	this.height = height;
  	this.setWeight(weight);
  	this.setYOB(yob);
  	this.location = location ;
  }
  
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }
  
  public void setHeight(String height) { this.height = height; }
	public String getHeight() { return height; }

	public void setWeight(String weight) { this.weight = weight; }
	public String getWeight() { return weight;  }

	public void setYOB(String yOB) { YOB = yOB; }
	public String getYOB() { return YOB; }

  public void setLocation(String location) { this.location = location ;	}
	public String getLocation() { return location ; }  
  
}
