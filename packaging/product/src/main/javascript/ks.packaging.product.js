eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {

  var product = new Product();
  product.name = "eXoKS" ;
  product.codeRepo = "ks" ;
  product.portalwar = "portal.war" ;
  product.version = "${project.version}" ;
  product.serverPluginVersion = "${org.exoplatform.portal.version}" ;
    
  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws", {kernel : kernel, core : core});
  var eXoPortletContainer = Module.GetModule("portletcontainer", {kernel : kernel, core : core}) ;    
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core, eXoPortletContainer : eXoPortletContainer, eXoJcr : eXoJcr});  
  //var webos = Module.GetModule("webos/trunk", {kernel : kernel, core : core, eXoPortletContainer : eXoPortletContainer, eXoJcr : eXoJcr });
  var ks = Module.GetModule("ks", {kernel : kernel, ws : ws, core : core, eXoPortletContainer : eXoPortletContainer, eXoJcr : eXoJcr, portal : portal});
  

  product.addDependencies(portal.portlet.exoadmin) ;
  product.addDependencies(portal.portlet.web) ;
  product.addDependencies(portal.eXoGadgetServer) ;
	product.addDependencies(portal.eXoGadgets) ;
  product.addDependencies(portal.web.rest) ;
  product.addDependencies(portal.portlet.dashboard) ;
  //product.addDependencies(webos.web.webosResources);
  //product.addDependencies(portal.eXoWidget.web) ;
            
  product.addDependencies(ks.eXoApplication.forum) ;
  product.addDependencies(ks.eXoApplication.faq) ;
  product.addDependencies(ks.eXoApplication.common) ;
  //product.addDependencies(ks.eXoApplication.blog) ;
  //product.addDependencies(ks.eXoApplication.wiki) ;

  product.addDependencies(ks.web.ksportal) ;
  product.addDependencies(ks.web.webservice) ;
  product.addDependencies(ks.web.ksResources) ;
  //product.addDependencies(cs.eXoApplication.contact) ;
  
	product.addServerPatch("tomcat", ks.server.tomcat.patch) ;
  product.addServerPatch("jboss",  ks.server.jboss.patch) ;
  //product.addServerPatch("tomcat", portal.server.tomcat.patch) ;
  //product.addServerPatch("jboss",  portal.server.jboss.patch) ;
  //product.addServerPatch("jonas",  portal.server.jonas.patch) ;
    

  product.module = ks ;
  product.dependencyModule = [kernel, core, eXoPortletContainer, ws, eXoJcr, portal];
    
  return product ;
}
