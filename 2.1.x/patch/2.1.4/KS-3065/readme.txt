Summary

    * Status: The max user is not properly set in forum
    * CCP Issue: CCP-786, Product Jira Issue: KS-3065.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The max user in forum is always 1.

Fix description

How is the problem fixed?

    * Change the logic of calculating maximum users in Forum service.

Patch file: KS-3065.patch

Tests to perform

Reproduction test

   1. Login as "john", "mary" and "demo" on three respective browsers.
   2. Number of maximum users is 3
   3. "mary" logout
   4. Number of maximum users is incorrect.

Tests performed at DevLevel
* Cf. above.

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* None
Configuration changes

Configuration changes:
* By default, "org.exoplatform.forum.service.conf.LoginJob" runs every two minutes to update the number of maximum users; therefore, the maximum user number is not updated immediately.
  This period value can be changed in webapps/ks-extension/WEB-INF/ks-extension/ks/forum/statistics-configuration.xml.

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * N/A

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* Patch approved.

Support Comment
* Patch validated.

QA Feedbacks
*

