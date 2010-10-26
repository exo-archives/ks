Summary

    * Status: Question with attachment disappears after moved
    * CCP Issue: CCP-641, Product Jira Issue: KS-2645
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * Question with attachment disappears after moved

Fix description

How is the problem fixed?

    * Correct saving FAQ attachment (moveQuestions() function in JCRDataStorage class).

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2645.patch

Tests to perform

Reproduction test
Steps:

   1. Add question (AAA) with attachment and save.
   2. Move question (AAA) to a destination category
      -> move successfully but don't see question (AAA) in the destination category or anywhere -> error.

Tests performed at DevLevel
* Cf. above

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have an impact any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment

    * PM Approved

Support Comment

    * Support review: patch validated

QA Feedbacks
*


