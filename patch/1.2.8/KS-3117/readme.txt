Summary

    * Status: redundant calls to "Organization Service" in FAQ
    * CCP Issue: CCP-818, Product Jira Issue: KS-3117.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The FAQ module performs multiple unnecessary calls to "Organization Service", which is disturbing if you are connected to the LDAP. 

Fix description

How is the problem fixed?
* Remove id = id.replace ("Membership [", ""). ("]", replace "); in code for get all permission of user.
* Use new logic for get all permission of user.

Patch file: KS-3117.patch

Tests to perform

Reproduction test
* Test all cases of Answers portlet about permission (eg: set private category)

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

    * Function or ClassName change : none

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch approved.

Support Comment
* Patch validated

QA Feedbacks
*
