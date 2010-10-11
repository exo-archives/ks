Summary

    * Status: Answers: Response for a bad question
    * CCP Issue: CCP-582, Product Jira Issue: KS-2693.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * In the portlet of answers, if two users answer to two questions at the same time, their answers are not attached to the right question.
      To reproduce this issue:

   1. Create two questions (Q1, Q2) in Answers portlet.
   2. From two browsers, 2 users (User 1, User 2) connect to Answers portlet.
   3. User 1 clicks the button "Answer" of Q1
   4. User 2 clicks the button "Answer" of Q2
   5. User 1 adds an answer to Q1 and save it
      => The answer has been attached to Q2
      => The answer's validation is actually attached to the last open question.

Fix description

How is the problem fixed?

    * Fix in UIResponseForm.java: remove static declaration from 2 private fields.

Patch information:
    * Final files to use should be attached to this page (Jira is for the discussion)

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Unit test and reproduction test.

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* None
Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Not detected yet.

Is there a performance risk/cost?
* not detected yet.
Validation (PM/Support/QA)

PM Comment

    * PM review. Patch approved.

Support Comment

    * Patch validated by support

QA Feedbacks
*

