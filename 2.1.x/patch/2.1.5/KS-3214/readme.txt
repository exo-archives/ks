Summary

    * Status: First/last name should be displayed in vote rather than user name
    * CCP Issue: CCP-888, Product Jira Issue: KS-3214.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
I have voted for some posts in forum, and I see a problem when I tried to re-vote.

The message says that I had already vote, but it uses my login name instead my displayed name.

So, in our environment, this is ok because login should be the name, but some customers have identifier like this : 054264 for login.

We should use displayed name for this message.
The desired message would be "According to our records Jack Miller has already rated this topic"
Fix description

How is the problem fixed?

    * Display screen name for this user voted.

Patch files:KS-3214.patch

Tests to perform

Reproduction test
* + Run forum application
  + Created Category/Forum/Topic
  + Open the Topic and click Rate and selected rate topic, after that click again Rate. ===> Message is displayed. If info of user voted show is Screen name ==> OK, if no ==> KO.

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

    * Function or ClassName change: None

Is there a performance risk/cost?
*  No
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

