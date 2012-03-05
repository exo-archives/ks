package org.exoplatform.forum.service;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.commons.testing.AbstractExoContainerTestCase;
import org.exoplatform.commons.testing.AssertUtils;
import org.exoplatform.commons.testing.mock.SimpleMockOrganizationService;
import org.exoplatform.component.test.ConfigurationUnit;
import org.exoplatform.component.test.ConfiguredBy;
import org.exoplatform.component.test.ContainerScope;
import org.exoplatform.services.organization.auth.OrganizationAuthenticatorImpl;
import org.exoplatform.services.security.Identity;
import org.exoplatform.services.security.IdentityRegistry;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@ConfiguredBy({@ConfigurationUnit(scope = ContainerScope.PORTAL, path = "conf/ForumServiceUtils-configuration.xml")})
public class TestForumServiceUtils extends AbstractExoContainerTestCase {

 protected SimpleMockOrganizationService organizationService = null;
 protected IdentityRegistry identityRegistry = null;
	
 @BeforeMethod
  protected void setUp() throws Exception {
   
    //PortalContainer container = PortalContainer.getInstance();
    organizationService =  getComponent(SimpleMockOrganizationService.class);// (SimpleMockOrganizationService)container.getComponentInstanceOfType(OrganizationService.class);
    identityRegistry =  getComponent(IdentityRegistry.class);////(IdentityRegistry)container.getComponentInstanceOfType(IdentityRegistry.class);
  }
  

 @Test
	public void testHasPermission() throws Exception {
		
		String user = "user1";
		organizationService.addMemberships(user, "*:/platform/users");
		
		simulateAuthenticate(user);
		
		assertFalse(ForumServiceUtils.hasPermission(null, user));
		assertFalse(ForumServiceUtils.hasPermission(new String [] {" "}, user));
		assertTrue(ForumServiceUtils.hasPermission(new String [] {user}, user));
		assertFalse(ForumServiceUtils.hasPermission(new String [] {"foo"}, user));
		
		assertTrue(ForumServiceUtils.hasPermission(new String [] {"/platform/users"}, user));
		assertTrue(ForumServiceUtils.hasPermission(new String [] {"/platform/users", user}, user));
		
		// must match one (OR)
		assertTrue(ForumServiceUtils.hasPermission(new String [] {"/platform/users", "user2"}, user));
		
		assertFalse(ForumServiceUtils.hasPermission(new String [] {"/foo"}, user));
		// suspicious * should theorically match 'admin'
		assertTrue(ForumServiceUtils.hasPermission(new String [] {"admin:/platform/users"}, user));
		
		assertTrue(ForumServiceUtils.hasPermission(new String [] {"*:/platform/users"}, user));
		
	}

	private void simulateAuthenticate(String user) throws Exception {
		Identity identity = new OrganizationAuthenticatorImpl(organizationService).createIdentity(user);
		identityRegistry.register(identity);
	}

	
 @Test
	public void testGetPermissionNull() throws Exception {
		List<String> emptyList = ForumServiceUtils.getUserPermission(null);
		assertNotNull(emptyList);
		assertEquals(0, emptyList.size());
	}

 @Test
	public void testGetPermissionByGroup() throws Exception {
		organizationService.addMemberships("user1", "*:/platform/users");	
		organizationService.addMemberships("user2", "*:/platform/users");
		organizationService.addMemberships("user3", "*:/platform");
		
		assertEquals(2, ForumServiceUtils.getUserPermission(new String [] {"/platform/users"}).size());
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"/platform/users"}), "user1", "user2");
		AssertUtils.assertNotContains(ForumServiceUtils.getUserPermission(new String [] {"/platform/users"}), "user3");	
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"/platform/users", "/platform"}), "user1", "user2", "user3");	
	}

 @Test
	public void testGetPermissionByUser() throws Exception {
		organizationService.addMemberships("user1", "*:/platform/users");	
		organizationService.addMemberships("user3", "*:/platform/users");
		
		assertEquals(1, ForumServiceUtils.getUserPermission(new String [] {"user1"}).size());
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"user1"}), "user1");
		
		// matching permission against a user return this user at minimum
		assertEquals(1, ForumServiceUtils.getUserPermission(new String [] {"user2"}).size());
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"user2"}), "user2");
		
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"user1", "user2"}), "user1", "user2");
		
		AssertUtils.assertNotContains(ForumServiceUtils.getUserPermission(new String [] {"user1", "user2"}), "user3");
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"user1", "user2"}), "user1", "user2");
	}
	
 @Test
	public void testGetPermissionByMembership() throws Exception {
    // add user/group/membership
		organizationService.addMemberships("user1", "user:/platform/users");
		organizationService.addMemberships("user2", "user:/platform/users");
		organizationService.addMemberships("user3", "manager:/platform/users");
		
		
		organizationService.addMemberships("user1", "manager:/space/space_user1");
		organizationService.addMemberships("user2", "member:/space/space_user1");
		
		organizationService.addMemberships("user2", "manager:/space/space_user2");
		organizationService.addMemberships("user1", "member:/space/space_user2");
		
		List<String> allUsers = new ArrayList<String>();
		allUsers.add("user1");
		// only user1 is manager of group /space/space_user1
		AssertUtils.assertContainsAll("Not only user1 is manager of group /space/space_user1.", 
		                              ForumServiceUtils.getUserPermission(new String [] {"manager:/space/space_user1"}), allUsers);
		// only user1 is member of group /space/space_user2
		AssertUtils.assertContainsAll("Not only user1 is member of group /space/space_user2.", 
		                              ForumServiceUtils.getUserPermission(new String [] {"member:/space/space_user2"}), allUsers);
		
		allUsers = new ArrayList<String>();
		allUsers.add("user2");
		// only user2 is manager of group /space/space_user2
		AssertUtils.assertContainsAll("Not only user2 is manager of group /space/space_user2.", 
		                              ForumServiceUtils.getUserPermission(new String [] {"manager:/space/space_user2"}), allUsers);
		// only user2 is member of group /space/space_user1
		AssertUtils.assertContainsAll("Not only user2 is member of group /space/space_user1.", 
		                              ForumServiceUtils.getUserPermission(new String [] {"member:/space/space_user1"}), allUsers);

		allUsers.add("user1");
		// user1 and user2 is join group /space/space_user1
		AssertUtils.assertContainsAll("The group /space/space_user1 not contains user1 and user2 ", 
		                              ForumServiceUtils.getUserPermission(new String [] {"*:/space/space_user1"}), allUsers);
		// user1 and user2 is join group /space/space_user2
		AssertUtils.assertContainsAll("The group /space/space_user2 not contains user1 and user2", 
		                              ForumServiceUtils.getUserPermission(new String [] {"*:/space/space_user2"}), allUsers);
	}

 
	
}

