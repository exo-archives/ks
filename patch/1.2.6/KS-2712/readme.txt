Summary

    * Status: FAQ: date always in English format
    * CCP Issue: CCP-595, Product Jira Issue: KS-2712.
    * Fix also: CCP-554 / KS-2655
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * In FAQ, date is always in English format

Fix description

How is the problem fixed?

    * Apply the User setting in Forum application about date time format to Answer application.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2712.patch

Tests to perform

Reproduction test

   1. Switch language to French
   2. Verify in KS preferences if the date is in correct format.
   3. Go to Answers portlet.
   4. Create a question and answer it
   5. Click the user name on the answer to view the profile

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No

Documentation changes

Documentation changes:
* Yes, should add more detail about User setting in Forum application because it will take effect to the Answer view.
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have an impact any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* On behalf of PM: patch tested, approved

Support Comment
* Support review: patch validated

QA Feedbacks
*

