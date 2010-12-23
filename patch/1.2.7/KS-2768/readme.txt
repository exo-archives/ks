Summary

    * Status: [Answers] An inactivated and/or disapproved question is always visible in FAQ
    * CCP Issue: CCP-638, Product Jira Issue: KS-2768.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * An inactivated or disapproved question is still visible in FAQ page.

Fix description

How is the problem fixed?

    * Check activated/inactivated and approved/disapproved when get list Question/Answer.

      // get list questions

      String strQuery = "[@exo:isActivated='true' and @exo:isApproved='true']";
      ....
      // get list answer
      [@exo:approveResponses='true' and @exo:activateResponses='true']

Patch information:
Patch files: KS-2768.patch

Tests to perform

Reproduction test

    * Steps to reproduce:

   1. Login as root
   2. Go to Groups/Answers and create a new question with 1 (or more) response(s). This question and its responses are activated and approved by default. They're visibles in Answers/FAQ page.
   3. Click on Manage Questions, click Edit this question and uncheck both Approved and Activated. Then Save.
   4. This question is no more visible in module "Answers"
   5. Go to Groups/FAQ : This question is still visible => NOT OK

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No


Documentation changes

Documentation changes:
* No


Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No changed Function or ClassName.

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

