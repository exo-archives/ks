Summary

    * Status: Impossible access to a forum's category when having the restriction
    * CCP Issue: CCP-959, Product Jira Issue: KS-3364.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Impossible access to a forum's category when having the restriction .

Fix description

How is the problem fixed?

    * Make users have restriction of a category can access to forums.

Patch file: KS-3364.patch

Tests to perform

Reproduction test
Case 1

   1. Add the restriction *:/platform/users (Select by role) to a category of forum
   2. Log in using any user (e.g. demo)
   3. Cannot access to a post in the restricted forum's category although it appears

Case 2

   1. Add the restriction /platform/users (Select by group) to a category of forum
   2. Log in using any user (e.g. demo)
   3. Cannot access to a post in the restricted forum's category although it appears

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

    * Function or ClassName change: No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated

QA Feedbacks
*
