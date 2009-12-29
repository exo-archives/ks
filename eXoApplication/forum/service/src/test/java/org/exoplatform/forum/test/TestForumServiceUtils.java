package org.exoplatform.forum.test;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.ks.test.AbstractContainerBasedTestCase;
import org.exoplatform.ks.test.AssertUtils;
import org.exoplatform.services.organization.auth.OrganizationAuthenticatorImpl;
import org.exoplatform.services.security.Identity;

public class TestForumServiceUtils extends AbstractContainerBasedTestCase {

	public TestForumServiceUtils() throws Exception {
		super();

	}
	
	
  @Override
  protected void registerComponents(ExoContainer testContainer) {
 
  }
	
	

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

	
	public void testGetPermissionNull() throws Exception {
		List<String> emptyList = ForumServiceUtils.getUserPermission(null);
		assertNotNull(emptyList);
		assertEquals(0, emptyList.size());
	}

	public void testGetPermissionByGroup() throws Exception {
		organizationService.addMemberships("user1", "*:/platform/users");	
		organizationService.addMemberships("user2", "*:/platform/users");
		organizationService.addMemberships("user3", "*:/platform");
		
		assertEquals(2, ForumServiceUtils.getUserPermission(new String [] {"/platform/users"}).size());
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"/platform/users"}), "user1", "user2");
		AssertUtils.assertNotContains(ForumServiceUtils.getUserPermission(new String [] {"/platform/users"}), "user3");	
		AssertUtils.assertContains(ForumServiceUtils.getUserPermission(new String [] {"/platform/users", "/platform"}), "user1", "user2", "user3");	
		
		
	}
	
	
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
	
	public void testGetPermissionByMembership() throws Exception {
		organizationService.addMemberships("user1", "*:/platform/users");
	}
	
	/**
	 *       <object-param>
        <name>cache.config.default</name>
        <description>The default cache configuration</description>
        <object type="org.exoplatform.services.cache.ExoCacheConfig">
          <field name="name"><string>default</string></field>
          <field name="maxSize"><int>300</int></field>
          <field name="liveTime"><long>-1</long></field>
          <field name="distributed"><boolean>false</boolean></field>
          <field name="implementation"><string>org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache</string></field> 
        </object>
      </object-param>
	 */



	
}

