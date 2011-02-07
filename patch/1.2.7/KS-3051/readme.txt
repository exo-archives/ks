Summary

    * Status: Impossible to sign in KS standalone after PORTAL-3815
    * CCP Issue: N/A, Product Jira Issue: KS-3051.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* After fixing PORTAL-3815, it is impossible to sign in KS standalone.

When clicking Sign in button in Sign in form, there is a blank page and error in server console.

[ERROR] portal:Lifecycle - template : system:/groovy/portal/webui/workspace/UIExoStart.gtmpl <java.lang.NullPointerException: Cannot invoke method getTime24hFormat() on null object>java.lang.NullPointerException: Cannot invoke method getTime24hFormat() on null object
	at org.codehaus.groovy.runtime.NullObject.invokeMethod(NullObject.java:77)
	at org.codehaus.groovy.runtime.InvokerHelper.invokePogoMethod(InvokerHelper.java:784)
	at org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:758)
	at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:170)
	at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethod0(ScriptBytecodeAdapter.java:198)
	at script1296121845124.run(script1296121845124.groovy:314)
	at org.exoplatform.groovyscript.text.SimpleTemplateEngine$SimpleTemplate$1.writeTo(SimpleTemplateEngine.java:128)
	at org.exoplatform.groovyscript.text.TemplateService.merge(TemplateService.java:72)
	at org.exoplatform.webui.core.lifecycle.Lifecycle.renderTemplate(Lifecycle.java:111)
	at org.exoplatform.webui.core.lifecycle.Lifecycle.processRender(Lifecycle.java:70)
	at org.exoplatform.webui.core.UIComponent.processRender(UIComponent.java:100)
	at org.exoplatform.webui.core.UIContainer.renderChild(UIContainer.java:220)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	at java.lang.reflect.Method.invoke(Method.java:592)

This problem doesn't occur in WCM/AIO, eXo start button and time display are not used in WCM/AIO.
Fix description

Problem analysis
* KS standalone uses UIExoStart template, and has its own portal-configuration.xml.
* In Portal module, this template has been modified in PORTAL-3815, with the parameter initialized in GlobalPortalConfigService.
  The service has been added in PORTAL-3895.

How is the problem fixed?
* Update portal-configuration.xml of KS with the configuration of GlobalPortalConfigService.

Patch file: KS-3051.patch

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* Cf. above.

Will previous configuration continue to work?
* KS stanalone: no.
* AIO: yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PM review: patch approved

Support Comment
* Patch validated by the Support team

QA Feedbacks
*

