Summary

    * Status: Notification email for questions and answers (KS Answer module)
    * CCP Issue: CCP-650, Product Jira Issue: KS-2825.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * There is a problem of notification when editing or posting a response for a question in a watched category.

Fix description

How is the problem fixed?

    * Edit logic send notification when adding new answers
    * Add default email for configuring email notification.
    * Edit key word of content send email notification.

Patch file: KS-2825.patch

Tests to perform

Reproduction test
To reproduce this problem, there are several use cases:

   1. Create a question and modify it: receive notification
      Then, make a response for this question: receive notification
      Modify the question for the second time: No notification
      Make a second response for this question: No notification
   2. Make three successive modifications of the question: receive notification for all modifications
      Make a response for the question: No notification
   3. If a user posts a response for this question (this question has been posted by some one else): No notification.

Tests performed at DevLevel
*

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

Can this bug fix have any side effects on current client projects?

    * Add new function setEmailDefault in class JCRDataStorage.java.

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PM review: patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
*

