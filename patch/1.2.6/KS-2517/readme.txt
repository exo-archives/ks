Summary

    * Status: Temp folder in test phase has been created, when build product
    * CCP Issue: N/A, Product Jira Issue: KS-2517.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Some temp data folders which are created in test phase, are not placed in the "target" folder. Therefore, we could not remove them by maven's clean command.

Fix description

How is the problem fixed?

    * Change configuration in test packages: declare data folder under "target" folder.

Patch information:
Patch files:
KS-2517.patch 	  	

Tests to perform

Reproduction test
* run build command: mvn clean install. Check folders created after build and are not in "target" folder. (such as: .../eXoApplication/faq/service/temp)

Tests performed at DevLevel
* Unit tests.

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
* Nothing.


Configuration changes

Configuration changes:
* change data folder path in configuration files in test packages.

Will previous configuration continue to work?
* yes


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No, it only affects in test phase.

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment

    * patch approved. Ok for 2.1.0-CR02

Support Comment
* Support review : validated

QA Feedbacks
*

