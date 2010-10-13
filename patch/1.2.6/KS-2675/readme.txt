Summary

    * Status: Add missing icon to the "Ban IP" menu item
    * CCP Issue: N/A, Product Jira Issue: KS-2675.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Missing icon for "Ban IP" menu items

Fix description

How is the problem fixed?

    * In file UITopicDetail.gtmpl, modify the code: <div class="ItemIcon"> to <div class="ItemIcon BanIpToolsIcon">

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2675.patch


Tests to perform

Reproduction test
   1. Login as root
   2. Go to Groups/Forum
   3. Open topic Demo data policy and post a reply
   4. Click on IP icon. The "Ban IP" menu will be displayed without icon

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
*No

Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?
* No change in function or class name.

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PM review. Patch approved.

Support Comment
* Support review: patch approved

QA Feedbacks
*

