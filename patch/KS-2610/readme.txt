Summary

    * Status: KS Memory Leak caused by unclosed JCR sessions
    * CCP Issue: CCP-503 Product Jira Issue: KS-2610
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Memory leak problem when unclose session after use

Fix description

How is the problem fixed?

    *   Close session after using it

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
There are currently no attachments on this page.
Tests to perform

Tests performed at DevLevel
*  Yes, Run unit test, jcr session detect  (http://wiki.exoplatform.org/xwiki/bin/view/JCR/Session+leak+detector) and selenium automation script test

Tests performed at QA/Support Level
* Yes
Documentation changes

Documentation Changes:
* No
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have an impact on current client projects?

    * Function or ClassName change ? Yes

Is there a performance risk/cost?
* Yes, it might get session close problem because some inner functions call and transaction will be call
Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Support Review 20100726: Refused, it causes some exceptions

* Support Review 20100728: patch validated

QA Feedbacks
*

