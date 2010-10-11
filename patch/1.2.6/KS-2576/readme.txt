Summary

    * Status: Problem to translate label from English to French "UnWatch"
    * CCP Issue: CCP-457, Product Jira Issue: KS-2576.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * When a topic has already been Watched (Suivi), we should get the correct label in French and not "UnWatch".
    * Steps to reproduce:

   1. Login as root
   2. Go to Groups/Forum
   3. Open 1 topic
   4. Change language to French
   5. Click on Suivre in the action bar => Label of this button changes to "UnWatch" => Not OK.

Fix description

How is the problem fixed?

    * Add a new key "UIForumPortlet.label.UnWatch=Arrêter de suivre" to ForumPortlet_fr.properties file.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test
* Cf. above
Tests performed at DevLevel
* Cf. above

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No.
Configuration changes

Configuration changes:
* No.

Will previous configuration continue to work?
* Yes.
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No function or class name changes.

Is there a performance risk/cost?
* No.
Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Review 28-09-2010: patch approved

QA Feedbacks
*

