Summary

    * Status: Show message "This link is not visible anymore" when viewing post by normal users
    * CCP Issue: N/A, Product Jira Issue: KS-2650.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Show message "This link is not visible anymore" when view post by normal user
      Steps:
         1. Login as a normal user
         2. Add some post replies
         3. Then click on [user name] and select [All post by user]
            -> Posts replied by user are displayed in list
         4. Open any post reply -> show message "This link is not visible anymore" and post content isn't show -> NOK

Fix description

How is the problem fixed?

    * Add checking condition when opening a post link.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test

    * Cf. above.

Tests performed at DevLevel

    * Reproduction test, test case.

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

    * Function or Class Name change: no.

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Approved

Support Comment

    * Support review: patch validated

QA Feedbacks
*

