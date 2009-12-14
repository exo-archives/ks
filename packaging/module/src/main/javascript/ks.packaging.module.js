eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getModule(params) {

  var kernel = params.kernel;
  var core = params.core;
  var eXoPortletContainer = params.eXoPortletContainer;
  var jcr = params.eXoJcr;
  var portal = params.portal;
  var ws = params.ws;
  var module = new Module();

  module.version = "${project.version}" ;
  module.relativeMavenRepo =  "org/exoplatform/ks" ;
  module.relativeSRCRepo =  "ks" ;
  module.name = "ks" ;
    
  module.eXoApplication = {};

	module.eXoApplication.common = new Project("org.exoplatform.ks", "exo.ks.eXoApplication.common","jar",module.version) ; 
  
  module.eXoApplication.faq = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.faq.webapp", "war", module.version).
      addDependency(new Project("rome", "rome", "jar", "0.8")).
	  addDependency(new Project("jdom", "jdom", "jar", "1.0")).
	  addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.faq.service", "jar",  module.version));
	  
  module.eXoApplication.faq.deployName = "faq";


  module.eXoApplication.forum = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.forum.webapp", "war", module.version).       
			addDependency(new Project("org.exoplatform.ws", "exo.ws.frameworks.json", "jar", "1.3.4")).
	    addDependency(ws.frameworks.cometd).
      addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.forum.service", "jar",  module.version));
  module.eXoApplication.forum.deployName = "forum";

/*
  module.eXoApplication.content = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.content.webapp", "war", module.version).
      addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.content.service", "jar",  module.version));
  module.eXoApplication.content.deployName = "content";
  */	  
  module.web = {}
  module.web.ksResources = 
    new Project("org.exoplatform.ks", "exo.ks.web.ksResources", "war", module.version) ;
  module.web.webservice = 
    new Project("org.exoplatform.ks", "exo.ks.web.webservice", "jar",  module.version);
  module.web.ksportal = 
    new Project("org.exoplatform.ks", "exo.ks.web.portal", "exo-portal", module.version).
      addDependency(portal.web.eXoResources) .
      addDependency(portal.web.eXoMacSkin) .
      addDependency(portal.web.eXoVistaSkin) .
	  addDependency(portal.webui.portal) .
      addDependency(jcr.frameworks.command) .
      addDependency(jcr.frameworks.web) ;
  
		module.server = {}
  module.server.tomcat = {}
  module.server.tomcat.patch = 
    new Project("org.exoplatform.ks", "exo.ks.server.tomcat.patch", "jar", module.version);

  module.server.jboss = {}
  module.server.jboss.patch = 
	    new Project("org.exoplatform.ks", "exo.ks.server.jboss.patch", "jar", module.version);
		
  return module;
}
