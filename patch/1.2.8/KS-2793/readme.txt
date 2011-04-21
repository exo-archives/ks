Summary

    * Status: Manage e.printStacktrace() and replace by logging system for Forum and FAQ
    * CCP Issue: N/A, Product Jira Issue: KS-2793.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Ban this kind of constructs from KS code

try {
 //DO something that can fail with an exception
}
catch (Exception e) {
  e.printStackTrace();
}

When you do this there are multiple issues.

Fix description

How is the problem fixed?

    * Replace e.printStackTrace(); by log.error(" comment ...", e);

Patch file: KS-2793.patch

Tests to perform

Reproduction test

   1. Run server
   2. Check log. When the application has some bug, there will be comment.

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

    * Function or ClassName change : No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*
