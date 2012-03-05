Summary

    * Status: Polls are visible for all users
    * CCP Issue: CCP-896, Product Jira Issue: KS-3224.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Create a new Poll, we found that it is visible for all users , even if they are not members on the space on which the poll was created.

1. Login w/john
2. Create a new space
3. Add new forum under the space
4. Create new topic and add a poll to this topic
5. Logout and Login w/mary
6. Go to Dashboard/Add Gadgets: Add Poll Gadget
--> Marry is not a member of new space but she can see all the poll questions of all topics when adding the Poll Gadget --> NOK
Fix description

How is the problem fixed?

 Set permission for display poll in poll gadget via logic:

+ For poll public: Added by the PollPortlet, everyone can viewing it, but only user login can vote it.
+ For poll private: Added by the PollPortlet, only user in group set in poll can viewing, vote it.
+ For poll in forum: Added by the ForumPortlet,

    * It has permission same post in topic parent.
    * It not show for users can not permission to view topic parent.(set by: can view in topic parent, viewer in forum and category parent, user private in category parent.)
    * It not show for normal users when topic parent is closed, inactive, unapproved, waiting for censer or forum parent closed. For moderator of forum parent, it showed but when forum parent is closed, moderator not see it. For administrators of forum application --> all poll of forum application showed.

Patch files: KS-3224.patch

Tests to perform

Reproduction test

 - Go to Dashboard/Add Gadgets: Add Poll Gadget.
 - Check all case about permission for this user login via fixed comment.

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

    * Function or ClassName change:
      - Add new class Utils in package org.exoplatform.poll.service. in service folder.
      - Add new class TestUtils in package org.exoplatform.poll.service in test folder.

Is there a performance risk/cost?
* Infact, the solution for this issue will improve the performance for the CLV portlet.
Validation (PM/Support/QA)

PM Comment
* PL review : patch validated.

Support Comment
* Support review: patch validated

QA Feedbacks
*

