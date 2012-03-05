Summary

    * Status: ANSWERCATEGORY Display alert message error when add user into moderate or Restricted audience.
    * CCP Issue: N/A, Product Jira Issue: KS-3665.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Login as root
    * Go to Answer page
    * Add a new category
    * Add user into MODERATE or Restricted audience.
      ==> System displays error message:
      ?
      SyntaxError: missing } after function body : eXo.core.Browser.addOnLoadCallback('mid1329488589',function(){eXo.ks.KSUtils.setMaskLayer('SubCategoryForm')  -- 0
      ?
      SyntaxError: syntax error : })  -- 1
      ?
      TypeError: input.form is undefined :
      eXo.webui.UIUserSelector.captureInput("QuickSearch","javascript:eXo.webui.UIForm.submitForm('fcb0cf73-e3a0-450c-b82d-8d8aa201e84c#UIUserSelector','Search',true)")  -- 4

Fix description

How is the problem fixed?

    * Because the quick search component has same ID with a GateIn component, so the GateIn's js in run improperly in this case.
    * Change the ID from "QuickSearch" to "QuickSearchForm" to avoid this confusion

Patch files: KS-3665.patch

Tests to perform

Reproduction test

    * cf.above

Tests performed at DevLevel

    * cf.above

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
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

