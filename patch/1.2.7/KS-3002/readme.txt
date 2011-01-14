Summary

    * Status: Hard-coded prefix of notification mail
    * CCP Issue: CCP-470, Product Jira Issue: KS-3002.
    * Complexity: trivial

The Proposal
Problem description

What is the problem to fix?

   1. Login
   2. Change language in French
   3. Go to Forum
   4. Select a Forum (e.g Live demo)
   5. Select a topic (e.g Demo data policy)
   6. Watch this topic
   7. Add a reply in the topic
   8. Check notification email
      The email subject is "Email notify" --> Not correct.

Fix description

How is the problem fixed?

    * Removed the hard code in java code.

Patch file: KS-3002.patch

Tests to perform

Reproduction test
*
   1. Login
   2. Change language in French
   3. Go to Forum
   4. Select a Forum (e.g Live demo)
   5. Select a topic (e.g Demo data policy)
   6. Watch this topic
   7. Add a reply in the topic
   8. Check notification email --> Not see email subject is "Email notify ..."

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
* PM review: patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
*

