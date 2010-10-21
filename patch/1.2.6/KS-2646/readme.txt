Summary

    * Status: (ANSWER) Show duplicate question that contains attachment in search result in special case
    * CCP Issue: N/A, Product Jira Issue: KS-2646.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Show duplicate question that contains attachment in search result in a special case

Fix description

How is the problem fixed?

    * Clear item duplicate in search result.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2646.patch

Tests to perform

Reproduction test
Steps:
   1. Add a question (AAA) with attachment
   2. Open Advanced search form
   3. Don't input value into Term field
   4. Select search in [entries] and click Search button
      -> show duplicate question (AAA) in search result -> Not ok.

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

    * No, there's no change in Function or Class Name.

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment

    * On behalf of PM: patch tested, approved

Support Comment

    * Support review: patch validated

QA Feedbacks
*

