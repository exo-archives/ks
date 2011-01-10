Summary

    * Status: Version of dependencies is hard coded in pom
    * CCP Issue: N/A, Product Jira Issue: KS-2133.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Version of dependencies is hard coded in pom

Fix description

How is the problem fixed?

    * Use ${project.version} instead of hard-coded version in pom.xml files.

Patch file: KS-2133.patch

Tests to perform

Reproduction test
* Apply the patch then run the clean build with: mvn clean install
* If you finish no error that mean the patch done

Tests performed at DevLevel
*Yes

Tests performed at QA/Support Level
*No

Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
*Yes

Will previous configuration continue to work?
*Yes but have to change

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?
*No

Validation (PM/Support/QA)

PM Comment
* PM review: Patch approved. Not supported, so needn't Support review.

Support Comment
* 

QA Feedbacks
*
