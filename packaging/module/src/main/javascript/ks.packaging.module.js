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

  //WIKI
  module.eXoApplication.wiki = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.wiki.webapp", "war", module.version).
    addDependency(new Project("org.exoplatform.ks", "exo.ks.eXoApplication.wiki.service", "jar",  module.version)).
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.webui.ext", "jar",  "${org.exoplatform.commons.version}")).
    addDependency(new Project("com.google.gwt", "gwt-servlet", "jar",  "${gwt.version}")).
    addDependency(new Project("com.google.gwt", "gwt-user", "jar",  "${gwt.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-web-gwt-wysiwyg-client", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.apache.pdfbox", "fontbox", "jar",  "${org.apache.pdfbox.fontbox.version}")).
    addDependency(new Project("javax.validation", "validation-api", "jar",  "${javax.validation.version}")).
    addDependency(new Project("org.hibernate", "hibernate-validator", "jar",  "${hibernate-validator.version}")).
    addDependency(new Project("org.python", "jython-standalone", "jar",  "${jython-standalone.version}")).
    addDependency(new Project("pygments", "pygments", "jar",  "${pygments.version}")).
    addDependency(new Project("net.sourceforge.htmlcleaner", "htmlcleaner", "jar",  "${net.sourceforge.htmlcleaner.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-configuration-api", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-model", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-context", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-component-api", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-component-default", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-properties", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-xml", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-script", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-api", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-syntax-wikimodel", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-macro-toc", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-macro-box", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-macro-message", "jar",  "${org.xwiki.platform.version}")).
    addDependency(new Project("org.xwiki.platform", "xwiki-core-rendering-macro-code", "jar",  "2.3.2")).
    addDependency(new Project("org.xwiki.contrib", "xwiki-macro-column", "jar",  "${column-macro.version}")).
    addDependency(new Project("org.wikimodel", "org.wikimodel.wem", "jar",  "${org.wikimodel.version}")).
    addDependency(new Project("org.suigeneris", "jrcs.diff", "jar",  "${org.suigeneris.version}")).
    addDependency(new Project("org.suigeneris", "jrcs.rcs", "jar",  "${org.suigeneris.version}")).
    addDependency(new Project("ecs", "ecs", "jar",  "${ecs.version}"));
  module.eXoApplication.wiki.deployName = "wiki";

  // POLL
  module.eXoApplication.poll = 
    new Project("org.exoplatform.ks", "exo.ks.eXoApplication.poll.webapp", "war", module.version) .
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
    new Project("org.exoplatform.ks", "exo.ks.demo.cometd-war", "war", module.version).
    addDependency(new Project("org.mortbay.jetty", "cometd-bayeux", "jar", "6.1.11")).
    addDependency(new Project("org.mortbay.jetty", "jetty-util", "jar", "6.1.11")).
    addDependency(new Project("org.mortbay.jetty", "cometd-api", "jar", "0.9.20080221")).
    addDependency(new Project("org.apache.poi", "poi-ooxml", "jar", "3.6")).
    addDependency(new Project("org.apache.poi", "poi", "jar", "3.6")).
    addDependency(new Project("org.apache.poi", "poi-scratchpad", "jar", "3.6")).
    addDependency(new Project("org.apache.xmlbeans", "xmlbeans", "jar", "2.3.0")).
    addDependency(new Project("org.exoplatform.commons", "exo.platform.commons.comet.service", "jar", "${org.exoplatform.commons.version}"));
  module.demo.cometd.deployName = "cometd-ksdemo";
	   
  // demo rest endpoint	   
  module.demo.rest =
    new Project("org.exoplatform.ks", "exo.ks.demo.rest-ksdemo", "war", module.version).
    addDependency(ws.frameworks.servlet);
  module.extension.deployName = "rest-ksdemo"; 
   
  return module;
}
