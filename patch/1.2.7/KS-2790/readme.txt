Summary

    * Status: build and deploy KS 1.2.x failed with clean local repository.
    * CCP Issue: N/A, Product Jira Issue: KS-2790.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Build and deploy KS 1.2.x, by maven command 'mvn clean install -Ppkg -Ddeploy=tomcat', failed with a clean local repository.
      It is because of missing dependencies in deployment phase with exobuild tool.
      To fix this problem, add required dependencies to pom file at "packaging/pkg".

Fix description

How is the problem fixed?

    * Add more definition for 3rd party libraries in main pom
    * Add missing dependencies in packaging/pkg/pom.xml

Patch file: KS-2790.patch

Tests to perform

* If you finish no error that mean the patch done

Tests performed at DevLevel

   1. Apply the patch
   2. Rename or backup the local repository folder
   3. Run the clean build with: mvn clean install -Ppkg -Ddeploy=tomcat

Tests performed at QA/Support Level
* No

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* Yes: add more missing dependency

Will previous configuration continue to work?
* No.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
*

QA Feedbacks
*
