package org.exoplatform.forum.test;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;

import junit.framework.TestCase;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.jmx.ManagementContextImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.forum.service.ForumServiceUtils;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.impl.CacheServiceImpl;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.test.mocks.servlet.MockServletContext;

public class TestForumServiceUtils extends TestCase {

	public TestForumServiceUtils() throws Exception {
		super();
		cacheService = initCacheService();
	}
	
	SimpleMockOrganizationService organizationService = new SimpleMockOrganizationService();
	CacheService cacheService = null;

	
	public void setUp() {
		ExoContainer testContainer = new ExoContainer(new ManagementContextImpl(ManagementFactory.getPlatformMBeanServer(), new HashMap<String,String>()));
		testContainer.registerComponentInstance(OrganizationService.class, organizationService);
		
		testContainer.registerComponentInstance(CacheService.class, cacheService);
		
		ExoContainerContext.setCurrentContainer(testContainer);
		PortalContainer.setInstance(new PortalContainer(testContainer, new MockServletContext("portal")));
	
		
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
	private CacheService initCacheService() throws Exception {
		InitParams cacheParams = new InitParams();
		ObjectParameter oparam = new ObjectParameter();
		ExoCacheConfig config = new ExoCacheConfig();
		oparam.setName("cache.config.default");
		config.setName("default");
		config.setMaxSize(30);
		config.setLiveTime(300);
		config.setDistributed(false);
		config.setImplementation("org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache");	
		oparam.setObject(config);
		cacheParams.addParameter(oparam);
		return new CacheServiceImpl(cacheParams);
	}
	
}

