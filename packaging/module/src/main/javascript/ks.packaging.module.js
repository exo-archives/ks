eXo.require("eXo.projects.Module");
eXo.require("eXo.projects.Product");

function getModule(params) {
  
  var ws = params.ws;
  var portal = params.portal;
  var module = new Module();

  module.version = "${project.version}";
  module.relativeMavenRepo = "org/exoplatform/ks";
  module.relativeSRCRepo = "ks";
  module.name = "ks";

  var cometVersion = "${org.exoplatform.commons.version}";
  module.comet = {};
  module.comet.cometd =
    new Project("org.exoplatform.commons", "exo.platform.commons.comet.webapp", "war", cometVersion).
    addDependency(new Project("org.mortbay.jetty", "cometd-bayeux", "jar", "6.1.11")).
    addDependency(new Project("org.mortbay.jetty", "jetty-util", "jar", "6.1.11")).
    addDependency(new Project("org.mortbay.jetty", "cometd-api", "jar", "0.9.20080221")).
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.comet.service", "jar", cometVersion));
  module.comet.cometd.deployName = "cometd";
  
  // KS

  // KS components
  module.component = {};
  module.component.common = new Project("org.exoplatform.ks", "exo.ks.component.common", "jar", module.version);
  module.component.rendering = new Project("org.exoplatform.ks", "exo.ks.component.rendering", "jar", module.version);
  module.component.bbcode = new Project("org.exoplatform.ks", "exo.ks.component.bbcode", "jar", module.version);

  // KS apps
  module.eXoApplication = {};
  module.eXoApplication.common = new Project("org.exoplatform.ks", "exo.ks.eXoApplication.common", "jar", module.version);
  
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
    addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.forum.service", "jar",  module.version));
  module.eXoApplication.forum.deployName = "forum";

  // POLL
  module.eXoApplication.poll = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.poll.webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.poll.service", "jar",  module.version));
  module.eXoApplication.poll.deployName = "poll";

  // KS we resources and services
  module.web = {}
  module.web.ksResources = 
    new Project("org.exoplatform.ks", "exo.ks.web.ksResources", "war", module.version) ;

  // KS extension for tomcat 
  module.extension = {};
  module.extension.webapp =
    new Project("org.exoplatform.ks", "exo.ks.extension.webapp", "war", module.version);
  module.extension.webapp.deployName = "ks-extension";
   
  module.server = {}
  module.server.tomcat = {}
  module.server.tomcat.patch =
    new Project("org.exoplatform.ks", "exo.ks.server.tomcat.patch", "jar", module.version);
	
  module.server.jboss = {}
  module.server.jboss.patchear =
    new Project("org.exoplatform.ks", "exo.ks.server.jboss.patch-ear", "jar", module.version);
   
  // KS demo 
  module.demo = {};
  // demo portal
  module.demo.portal =
    new Project("org.exoplatform.ks", "exo.ks.demo.webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.ks", "exo.ks.demo.config", "jar", module.version)).
    addDependency(new Project("org.exoplatform.ks", "exo.ks.ext.social-integration", "jar", module.version));
  module.demo.portal.deployName = "ksdemo";  
	
  module.demo.cometd=
    new Project("org.exoplatform.ks", "exo.ks.demo.cometd-war", "war", module.version);
  module.demo.cometd.deployName = "cometd-ksdemo";
	   
  // demo rest endpoint
  module.demo.rest =
    new Project("org.exoplatform.ks", "exo.ks.demo.rest-ksdemo", "war", module.version).
    addDependency(ws.frameworks.servlet);;
  module.extension.deployName = "rest-ksdemo"; 
   
  return module;
}
