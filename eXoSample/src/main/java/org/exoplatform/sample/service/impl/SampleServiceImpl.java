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

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.sample.service.Information;
import org.exoplatform.sample.service.SampleService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.OrganizationService;
import org.picocontainer.Startable;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Mar 04, 2008  
 */

public class SampleServiceImpl implements SampleService, Startable{
  private NodeHierarchyCreator nodeHierarchy_ ;
  private List<TestPlugin> plugins_ = new ArrayList<TestPlugin>();
  
	public SampleServiceImpl(NodeHierarchyCreator nodeHierarchy, OrganizationService orService) throws Exception {
		nodeHierarchy_ = nodeHierarchy ;
		
	}
	
	public void start() {
		try{
      for(TestPlugin plugin : plugins_) {
        plugin.init() ;
      }
    }catch (Exception e) {
      e.printStackTrace();
    }
	}

	public void stop() {
		// TODO Auto-generated method stub		
	}
	
	public void addPlugin(ComponentPlugin plugin) throws Exception {
		if(plugin instanceof TestPlugin) {
			plugins_.add((TestPlugin)plugin) ;
		}
	}
	
	private Node getSampleServiceHome(SessionProvider sProvider) throws Exception {
    Node appNode = nodeHierarchy_.getUserApplicationNode(sProvider, "test") ;
  	try {
      return  appNode.getNode("SampleApp") ;
    } catch (PathNotFoundException ex) {
      Node sampleHome = appNode.addNode("SampleApp", "nt:unstructured") ;
      appNode.getSession().save() ;
      return sampleHome ;
    }  	
  }
	
	public boolean addItem(Information e, SessionProvider sProvider) throws Exception {
		try{
			Node sampleHome = getSampleServiceHome(sProvider)  ;
			Node emp ;
			if(sampleHome.hasNode(e.getId())) {
				emp = sampleHome.getNode(e.getId()) ;				
			}else {
				emp = sampleHome.addNode(e.getId(), "exo:info") ;				
			}
			emp.setProperty("exo:name", e.getName()) ;
			emp.setProperty("exo:height", e.getHeight()) ;
			emp.setProperty("exo:weight", e.getWeight()) ;
			emp.setProperty("exo:YOB", e.getYOB()) ;
			emp.setProperty("exo:location", e.getLocation()) ;
			sampleHome.getSession().save() ;
			//Node newNode = emp.addNode("test", "nt:unstructured") ;
			//emp.save() ;
			return true ;
		}catch(Exception ex) {
			//ex.printStackTrace() ;
			return false ;
		}		
	}

	public Information getItem(String id, SessionProvider sProvider) throws Exception {
		Node sampleHome = getSampleServiceHome(sProvider)  ;
		if(sampleHome.hasNode(id)) {
			Node empNode = sampleHome.getNode(id) ;
			Information emp = new Information();
			emp.setId(id) ;
			emp.setName(empNode.getProperty("exo:name").getString()) ;
			emp.setHeight(empNode.getProperty("exo:height").getString()) ;
			emp.setWeight(empNode.getProperty("exo:weight").getString()) ;
			emp.setYOB(empNode.getProperty("exo:YOB").getString()) ;
			emp.setLocation(empNode.getProperty("exo:location").getString()) ;
			return emp ;
		}
		return null;
	}

	public String getLocation(String id, SessionProvider sProvider) throws Exception {
		Node sampleHome = getSampleServiceHome(sProvider)  ;
		if(sampleHome.hasNode(id)) {
			return sampleHome.getNode(id).getProperty("exo:location").getString() ;
		}
		return null;
	}

	public String getName(String id, SessionProvider sProvider) throws Exception {
		Node sampleHome = getSampleServiceHome(sProvider)  ;
		if(sampleHome.hasNode(id)) {
			return sampleHome.getNode(id).getProperty("exo:name").getString() ;
		}
		return null;
	}
	
	public String getHeight(String id, SessionProvider sProvider) throws Exception {
		Node sampleHome = getSampleServiceHome(sProvider)  ;
		if(sampleHome.hasNode(id)) {
			return sampleHome.getNode(id).getProperty("exo:height").getString() ;
		}
		return null;
	}
	
	public String getWeight(String id, SessionProvider sProvider) throws Exception {
		Node sampleHome = getSampleServiceHome(sProvider)  ;
		if(sampleHome.hasNode(id)) {
			return sampleHome.getNode(id).getProperty("exo:weight").getString() ;
		}
		return null;
	}
	
	public String getYOB(String id, SessionProvider sProvider) throws Exception {
		Node sampleHome = getSampleServiceHome(sProvider)  ;
		if(sampleHome.hasNode(id)) {
			return sampleHome.getNode(id).getProperty("exo:YOB").getString() ;
		}
		return null;
	}	
	
	public void addChild(SessionProvider sProvider) throws Exception {
		try{
			Node sampleHome = getSampleServiceHome(sProvider)  ;
			Node empNode = sampleHome.getNode("x01") ;
			empNode.addNode("child01","nt:unstructured") ;
			empNode.addNode("child01","exo:test") ;			
		}catch(Exception ex) {
			ex.printStackTrace() ;
		}		
	}
}