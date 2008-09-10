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
package org.exoplatform.sample.service.impl;

import java.util.Iterator;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.jcr.RepositoryService;
public class TestPlugin extends BaseComponentPlugin{	

  private RepositoryService repositoryService_ ;  
  private InitParams params_ ;  
  
  public TestPlugin(InitParams params, RepositoryService repositoryService) throws Exception {
    repositoryService_ = repositoryService ;
    params_ = params ;   
  }

  public void init() throws Exception {    
  	ValueParam valueParam = params_.getValueParam("testValueParam") ;
    // TODO something with value params
  	System.out.println("\n\nvalueParam == " + valueParam.getValue()) ;
  	
  	Iterator<ObjectParameter> it = params_.getObjectParamIterator() ;
    while(it.hasNext()) {
    	TestConfig config = (TestConfig) it.next().getObject() ;
    	for(TestConfig.ObjectParam op : config.getObjectParams()) {
    		// TODO something with object params
    		System.out.println("\n\nname  == " + op.getName()) ;
    		System.out.println("value == " + op.getValue() +"\n\n") ;
    	}
    }
  }  
}
