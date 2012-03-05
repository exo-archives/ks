Summary

    * Status: redundant calls to "Organization Service" in FAQ
    * CCP Issue: N/A, Product Jira Issue: KS-3203.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
The FAQ module performs multiple unnecessary calls to "Organization Service", which is disturbing if you are connected to the LDAP. For example, when opening answers, manage questions, the "getAllGroupAndMembershipOfUser ()" in "FAQServiceUtils" class is called, the algorithm used is as follows:
?
for (Object object: organizationService_.getMembershipHandler (). findMembershipsByUser (userId). toArray ()) {
id = Object.toString ();
id = id.replace ("Membership [", ""). ("]", replace ");
Membership organizationService_.getMembershipHandler = (). findMembership (id);

Why not directly use the objects as follows:
?
for (Object object: organizationService_.getMembershipHandler (). findMembershipsByUser (userId). toArray ()) {
membership = (Membership) object;
Fix description

How is the problem fixed?

    * Improvement use "Organization Service". Use class *org.exoplatform.ks.common.UserHelper* for all processing logic related "Organization Service".

Patch files:KS-3203.patch

Tests to perform

Reproduction test
* Test all case for check permission of user in AnswerPortlet. (Moderators/ Restricted audience of category)

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

    * Function or ClassName change: None

Is there a performance risk/cost?
* Not
Validation (PM/Support/QA)

PM Comment
* PL review : patch approved

Support Comment
* Support team review: Patch validated

QA Feedbacks
*

