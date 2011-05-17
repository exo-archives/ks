Summary

    * Status: Answers An inactived and/or disapproved question is always visible in FAQ
    * CCP Issue: N/A, Product Jira Issue: KS-3035.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  An inactived and/or disapproved question is always visible in FAQ

Fix description

How is the problem fixed?

    *  Used jcr query to get only question active and approved for display questions list in FAQ portlet.

Patch files:KS-3035.patch

Tests to perform

Reproduction test
*
Steps to reproduce:
1. Login as root
2. Go to Groups/Answers and create a new question with 1 (or more) response(s). This question and its responses are activated and approved by default. They're visibles in Answers/FAQ page.
3. Click on Manage Questions, click Edit this question and uncheck both Approved and Activated. Then Save.
4. This question is no more visible in module "Answers"
5. Go to Groups/FAQ : This question is still visible => NOT OK

Expected:

    * An inactivated or disapproved question must be invisible in FAQ page.
    * If a question is activated and approved, but its inactivated or disapproved responses must be invisible in FAQ page too.

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

    * Function or ClassName change: not change

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review : patch approved.

Support Comment
* Support review: patch validated

QA Feedbacks
*

