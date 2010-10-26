Summary

    * Status: Problem with quote management in forum posts
    * CCP Issue: CCP-572, Product Jira Issue: KS-2676.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix? 
 IE7 cannot decode &apos; for single quote '

Fix description

How is the problem fixed?
* Replace &apos; by &#39;

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2676.patch

Tests to perform

Reproduction test
Under IE7. Steps to reproduce :

   1. Login in Forum portlet.
   2. Go to a topic in a forum
   3. Type a message containing a quote '.

      This character is shown as &apos; -> Not OK.

Tests performed at DevLevel
* No

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

    * No change in function or class name.

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated by PM

Support Comment
* Support review: patch validated

QA Feedbacks
*

