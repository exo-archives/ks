Summary

    * Status: IE7: Internet Explorer can not open the Internet site when run along time about ( 20 - 30m)
    * CCP Issue: N/A, Product Jira Issue: KS-3052.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The syntax of HTML and other check null logic of code, we pre-check the null case to make sure the logic is true

Fix description

How is the problem fixed?

    * Add check null for category before get to use

Patch information:
Patch files: KS-3052.patch

Tests to perform

Reproduction test

    * The case when add space > create category for space
    * Go to answer portlet stand alone with admin right , remove category
    * Back to answer portlet inside space > error

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

    * Function or ClassName change : None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)
PM Comment
*

Support Comment
*

QA Feedbacks
*

