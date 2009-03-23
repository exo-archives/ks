/*
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
 */
package org.exoplatform.sample.service;

import org.exoplatform.services.jcr.ext.common.SessionProvider;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */

public interface SampleService {
  
	public boolean addItem(Information e, SessionProvider sProvider) throws Exception ;
	public Information getItem(String id, SessionProvider sProvider) throws Exception ;
	
	public String getName(String id, SessionProvider sProvider) throws Exception ;
	public String getHeight(String id, SessionProvider sProvider) throws Exception ;
	public String getWeight(String id, SessionProvider sProvider) throws Exception ;
	public String getYOB(String id, SessionProvider sProvider) throws Exception ;
	public String getLocation(String id, SessionProvider sProvider) throws Exception ;
	public void addChild(SessionProvider sProvider) throws Exception ;

}