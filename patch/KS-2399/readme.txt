Summary

    * Status: dependency cleanup on ks 1.2.x branch
    * CCP Issue: N/A, Product Jira Issue: KS-2399
    * Complexity: N/A
    * Impacted Client(s): N/A
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * In many module of the major part of KS, many dependencies are not declared while they are used in it. Therefore, we need to add missing dependencies that are result of maven command: "mvn dependency:analyze -DoutputXML=true" to module pom and replace hardcode version by a property.

Fix description

How the problem is fixed ?

    *  run mvn command "mvn dependency:analyze -DoutputXML=true" to know what dependencies are missed and update them to suitable module pom file.

Patch informations:

Patches files:
KS-2399.patch 	  	

Tests to perform

Which test should have detect the issue ?
* run command: mvn dependency:analyze -DoutputXML=true

Is a test missing in the TestCase file ?
*

Added UnitTest ?
*

Recommended Performance test?
*


Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration ?
* No

Describe configuration changes:
*

Previous configuration will continue to work?
* Yes


Risks and impacts

Is there a risk applying this bug fix ?
* Not detected yet.

Is this bug fix can have an impact on current client projects ?
* Not detected yet.

Is there a performance risk/cost?
* Not detected yet.


Validation By PM & Support

PM Comment
*

Support Comment
* Validated by Support


QA Feedbacks

Performed Tests
*

