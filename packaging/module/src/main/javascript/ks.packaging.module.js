eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params)
{


   var ws = params.ws;
   var portal = params.portal;
   var module = new Module();

   module.version = "${project.version}"; 
   module.relativeMavenRepo = "org/exoplatform/ks";
   module.relativeSRCRepo = "ks";
   module.name = "ks";
 
  	// COMET (required by KS)
  	// TODO, should be passed in params and have its own module .js definition 
  var cometVersion = "${org.exoplatform.comet.version}";
  module.comet = {};

    
  module.comet.cometd =
	new Project("org.exoplatform.comet", "exo-comet-webapp", "war", cometVersion).
    addDependency(new Project("org.mortbay.jetty", "cometd-bayeux", "jar", "6.1.11")).
	addDependency(new Project("org.mortbay.jetty", "jetty-util", "jar", "6.1.11")).
	addDependency(new Project("org.mortbay.jetty", "cometd-api", "jar", "0.9.20080221")).
	addDependency(new Project("org.exoplatform.comet", "exo-comet-service", "jar", cometVersion));  	
	module.comet.cometd.deployName = "cometd";
  // KS

  // KS components
  module.component = {};
  module.component.common = new Project("org.exoplatform.ks", "exo.ks.component.common","jar", module.version);
  module.component.rendering =  new Project("org.exoplatform.ks", "exo.ks.component.rendering","jar", module.version);
  module.component.bbcode =  new Project("org.exoplatform.ks", "exo.ks.component.bbcode","jar", module.version);
  
	
  // KS apps
  module.eXoApplication = {};
  module.eXoApplication.common = new Project("org.exoplatform.ks", "exo.ks.eXoApplication.common","jar", module.version);

  
  // FAQ
  module.eXoApplication.faq = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.faq.webapp", "war", module.version).
      addDependency(new Project("rome", "rome", "jar", "0.9")).
	  addDependency(new Project("jdom", "jdom", "jar", "1.0")).
	  addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.faq.service", "jar",  module.version));
	  
  module.eXoApplication.faq.deployName = "faq";

  // FORUM
  module.eXoApplication.forum = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.forum.webapp", "war", module.version).       
	addDependency(ws.frameworks.json).
	addDependency(module.comet.cometd).
    addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.forum.service", "jar",  module.version));
    
  module.eXoApplication.forum.deployName = "forum";

  // KS we resources and services
  module.web = {}
  module.web.ksResources = 
    new Project("org.exoplatform.ks", "exo.ks.web.ksResources", "war", module.version) ;

   // KS extension for tomcat 
   module.extension = {};
   module.extension.webapp = new Project("org.exoplatform.ks", "exo.ks.extension.webapp", "war", module.version).
   addDependency(new Project("org.exoplatform.ks", "exo.ks.extension.config", "jar", module.version));
   module.extension.webapp.deployName = "ks-extension";
   
   
  // KS demo 
   module.demo = {};
   // demo portal
   module.demo.portal = 
	   new Project("org.exoplatform.ks", "exo.ks.demo.webapp", "war", module.version).
	   addDependency(new Project("org.exoplatform.ks", "exo.ks.demo.config", "jar", module.version));
	   module.demo.portal.deployName = "ksdemo";  
	   
   // demo rest endpoint	   
   module.demo.rest = 
       new Project("org.exoplatform.ks", "exo.ks.demo.rest-war", "war", module.version);
       module.extension.deployName = "rest-ksdemo"; 
       
       
   
   return module;
}
