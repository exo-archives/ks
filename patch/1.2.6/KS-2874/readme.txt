Summary

    * Status: Missing ID of portlet.
    * CCP Issue: N/A, Product Jira Issue: KS-2874.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Nothing happens when clicking on menu action at user name
      Steps:
      Login. 
      Go to Forum home page.
      Click on user name (eg: root) and select view public profile or other items. 
      Nothing happens and Public profile disappears.

Fix description

How is the problem fixed?

    * Pass the dynamic portlet Id to the submit action event 

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2874.patch

Tests to perform

Reproduction test
* Login by user 
* Click to any user in forum statistic space
* And click to any action link in context menu
* Actions have to be done: show send message form, profile form .. 
* Post a topic in Forum, FCK editor tool bar must be shown 
* Post question in Answers, FCK editor tool bar must to be shown

Tests performed at DevLevel
* Cf. above

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

    * Function or ClassName change : None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

