Summary

    * Status: [Answers] Print item is hidden
    * CCP Issue: N/A, Product Jira Issue: KS-2644
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Item Print is hidden since the second time.

Fix description

How is the problem fixed?
* Make UIPopupWindow unresizable to avoid refreshing toolbar when click on Print action again.

Patch file: KS-2644.patch

Tests to perform

Reproduction test
Steps:
   1. Login
   2. Go to Answers portlet
   3. Open a category with some questions
   4. Click Next button (to show hidden label when there's not enough space) in Tool bar and select Print item
      -> Print preview form is displayed -> OK
   5. Close Print preview form
   6. Click Print item again -> nothing happens and that Print item is hidden -> Bug.

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
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*  

