Summary

    * Status: (KS-Forum) Topic is still displayed in Last post column although this topic was deleted
    * CCP Issue: N/A, Product Jira Issue: KS-2648.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Topic is still displayed in Last post column although this topic deleted.

Fix description

How is the problem fixed?

    * When opening container categories, clear the list of last post topic.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2648.patch

Tests to perform

Reproduction test
   1. Add category (AAA)
   2. In AAA: add forum (BBB)
   3. In BBB: add topic (CCC)
      -> back to Forum Home: topic (CCC) is shown in Last post column -> OK
   4. Then, delete topic (CCC) and back to Forum Home -> still see topic (CCC) in last post column -> Not OK

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*No.

Configuration changes

Configuration changes:
*No.

Will previous configuration continue to work?
*Yes.

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No, there's no change in Function or Class Name.

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment

    * PM review: patch approved

Support Comment

    * Support review: patch validated

QA Feedbacks
*

