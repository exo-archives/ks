Summary

    * Status: Label of pop up is inexact when select permission
    * CCP Issue: N/A, Product Jira Issue: KS-2657.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Label of pop up is inexact when select permission
      Steps:

    * Go to Forum or Answer
    * open Add category/Add forum
    * In Permission field:
          o Click on "Select a user" -> A pop up appears with name "UIUserSelector" -> Not OK
          o Click on "Select a role" -> A pop up appears with name "Select a group" instead of "Select a role" -> Not OK

Fix description

How is the problem fixed?

    * Set correct id for UIGroupSelector and UIUserSelector component
    * Update resource bundle

Patch information:
Patch files:
KS-2657.patch  	

Tests to perform

Reproduction test
* Yes

Tests performed at DevLevel
* Yes

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
* Pm review : Patch approved

Support Comment
*Support review : patch validated

QA Feedbacks
*

