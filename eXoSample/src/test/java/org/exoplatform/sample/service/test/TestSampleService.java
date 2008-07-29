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
package org.exoplatform.sample.service.test;

import org.exoplatform.sample.service.Information;
import org.exoplatform.sample.service.SampleService;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserHandler;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


public class TestSampleService extends BaseSampleServiceTestCase{
	private SampleService sampleService_ ;
	private SessionProvider sProvider_ ;
	OrganizationService service_;
  UserHandler userHandler_ ;
	public void setUp() throws Exception {
    super.setUp() ;
    sampleService_ = (SampleService) container.getComponentInstanceOfType(SampleService.class) ;
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
    service_ = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    userHandler_ = service_.getUserHandler() ; 
  }
  
	public void testSampleService() throws Exception {
  	assertNotNull(sampleService_) ;
  	assertNotNull(sProvider_) ;
  	
  	createUser("test") ;
  	User u = userHandler_.findUserByName("test");    
    assertTrue("Found user instance", u != null);
    assertEquals("Expect user name is: ", "test", u.getUserName());
  	 
  } 
  
	public void testAddItem() throws Exception {
    Information info = new Information("x01", "Thuy Lam", "1m70", "50kg", "1987", "TP HCM") ;
  	assertTrue(sampleService_.addItem(info, sProvider_)) ;
  	assertFalse(sampleService_.addItem(null, sProvider_)) ;
  }
  
  public void testGetItem() throws Exception {
    assertNotNull(sampleService_.getItem("x01", sProvider_)) ;
    assertNull(sampleService_.getItem("xxx", sProvider_)) ;
  }  
  
  public void testGetName() throws Exception {
    String name = sampleService_.getName("x01", sProvider_) ;
  	assertNotNull(name) ;
  	assertEquals(name, "Thuy Lam") ;
  	name = sampleService_.getName("xxx", sProvider_) ;
  	assertNull(name) ;
  }
  
  public void testGetHeight() throws Exception {
    String height = sampleService_.getHeight("x01", sProvider_) ;
  	assertNotNull(height) ;
  	assertEquals(height, "1m70") ;
  	height = sampleService_.getHeight("xxx", sProvider_) ;
  	assertNull(height) ;
  }
  
  public void testGetWeight() throws Exception {
    String weight = sampleService_.getWeight("x01", sProvider_) ;
  	assertNotNull(weight) ;
  	assertEquals(weight, "50kg") ;
  	weight = sampleService_.getWeight("xxx", sProvider_) ;
  	assertNull(weight) ;
  }
  
  public void testGetYOB() throws Exception {
    String yob = sampleService_.getYOB("x01", sProvider_) ;
  	assertNotNull(yob) ;
  	assertEquals(yob, "1987") ;
  	yob = sampleService_.getYOB("xxx", sProvider_) ;
  	assertNull(yob) ;
  }
  
  public void testGetLocation() throws Exception {
  	String location = sampleService_.getLocation("x01", sProvider_) ;
  	assertNotNull(location) ;
  	assertEquals(location, "TP HCM") ;
  	location = sampleService_.getLocation("xxx", sProvider_) ;
  	assertNull(location) ;
  }
  
  public User createUser(String userName) throws Exception {   
    User user = userHandler_.createUserInstance() ;
    user.setUserName(userName) ;
    user.setPassword("default") ;
    user.setFirstName("default") ;
    user.setLastName("default") ;
    user.setEmail("exo@exoportal.org") ;
    userHandler_.createUser(user, true);
    return user ;
  }
}