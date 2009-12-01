eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {
  var product = new Product();
  
  product.name = "eXoPortal" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "portal" ;//module in modules/portal/module.js
  product.serverPluginVersion = "${org.exoplatform.portal.version}"; // CHANGED for KS to match portal version. It was ${project.version}

  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws", {kernel : kernel, core : core});
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core, eXoJcr : eXoJcr});
  var ks = Module.GetModule("ks", {portal:portal, ws:ws});
  

  
  product.addDependencies(portal.web.rest) ;
  product.addDependencies(portal.portlet.exoadmin) ;
  product.addDependencies(portal.portlet.web) ;
  product.addDependencies(portal.portlet.dashboard) ;
  product.addDependencies(portal.eXoGadgetServer) ;
  product.addDependencies(portal.eXoGadgets) ;
  product.addDependencies(portal.webui.portal);
  
  product.addDependencies(portal.web.eXoResources);

  product.addDependencies(portal.web.portal);
  //product.addDependencies(portal.sample.extension) ; // CHANGED for KS, we don't want a rogue sample
  
  // Portal extension starter required by KS etension
  portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
  portal.starter.deployName = "starter";
  product.addDependencies(portal.starter);

  // cometd (requried for KS)
  product.addDependencies(ks.comet.cometd);
   
  // KS extension
  product.addDependencies(ks.component.common);
  product.addDependencies(ks.component.rendering);
  product.addDependencies(ks.component.bbcode);
  product.addDependencies(ks.eXoApplication.common);
  product.addDependencies(ks.eXoApplication.faq);
  product.addDependencies(ks.eXoApplication.forum);
  product.addDependencies(ks.web.ksResources);  
  product.addDependencies(ks.extension.webapp);

  // KS demo
  product.addDependencies(ks.demo.portal);
  product.addDependencies(ks.demo.rest);
  
  product.addServerPatch("tomcat", portal.server.tomcat.patch) ;
  product.addServerPatch("jboss",  portal.server.jboss.patch) ;
  product.addServerPatch("jbossear",  portal.server.jbossear.patch) ;

  /* cleanup duplicated lib */
  product.removeDependency(new Project("commons-httpclient", "commons-httpclient", "jar", "3.0"));
  product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "3.1"));
  product.removeDependency(new Project("commons-lang", "commons-lang", "jar", "2.3")); // exclusion added by KS. lib dir un tomcat contains versions 2.3 and 2.4. Keeping the newest.
  product.module = ks ;
  product.dependencyModule = [ kernel, core, ws, eXoJcr];

  return product ;
}
