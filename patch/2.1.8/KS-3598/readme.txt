Summary

    * Status: Backport KS Injectors to 2.1.x
    * CCP Issue: N/A, Product Jira Issue: KS-3598.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * Implement KS injectors.

Fix description

How is the problem fixed?
    * Implement the injector from the API provided in COMMONS-88
    * Create new artifact org.exoplatform.ks:exo.ks.component.injector to package all injectors in KS: Forum, Answer injectors.
    * Each data injector is implemented as plugin attached to org.exoplatform.services.bench.DataInjectorService service and handled via RESTful requests.

Patch file: KS-3598.patch

Tests to perform

Tests performed at DevLevel
*

Tests performed at QA/Support Level
* Following the guide of KS Data Injector

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* New configuration for injectors is added:
?
<component>
    <type>org.exoplatform.services.bench.DataInjectorService</type>
  </component>
 
  <external-component-plugins>
    <target-component>org.exoplatform.services.bench.DataInjectorService</target-component>
    <component-plugin>
      <name>ForumDataInjector</name>
      <set-method>addInjector</set-method>
      <type>org.exoplatform.ks.bench.ForumDataInjector</type>
      <description>inject data for Forum</description>
    </component-plugin>
    <component-plugin>
      <name>AnswerDataInjector</name>
      <set-method>addInjector</set-method>
      <type>org.exoplatform.ks.bench.AnswerDataInjector</type>
      <description>inject data for Answer</description>
    </component-plugin>
  </external-component-plugins>

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*
