Summary

    * Status: English labels are not translated in French
    * CCP Issue: CCP-470, Product Jira Issue: KS-3003.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Fix bugs of hard-coded labels in Forum portlet after KS-2629.

Fix description

How is the problem fixed?
    * Correct in java files and templates
    * Update ForumPortlet.properties and ForumPortlet_en.properties.

Patch file: KS-3003.patch

Tests to perform

Reproduction test
   1. Login
   2. Change language in French
   3. Go to Forum
      Labels should be translated in French

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
* No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL: Patch approved.

Support Comment
* Support review:patch validated

QA Feedbacks
*

