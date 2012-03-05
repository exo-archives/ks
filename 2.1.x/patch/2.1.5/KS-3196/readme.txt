Summary

    * Status: Poll portlet header not translated to English or French - UIPollForm
    * CCP Issue: N/A, Product Jira Issue: KS-3196.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The header of the portlet should be translated. Currently you just see "UIPollForm".

Fix description

How is the problem fixed?

    * Add new key and add new title for UIPollForm.
    * Add new key for UIFormCheckBoxInput and UIFormMultiValueInputSet for Add Item and Remove Item.

Patch files: KS-3196.patch

Tests to perform

Reproduction test
* Open forum portlet, go to topic and click add poll. Change language to France, see all labels, titles of UIPollForm, if all labels, titles is France language --> OK. 

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

    * Function or ClassName change: none

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

