Summary

    * Status: Improve LDAP Requests for Answers backport 2.1.x
    * CCP Issue: CCP-1032, Product Jira Issue: KS-3606.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * There are LDAP requests that are sent all the time.

Fix description

How is the problem fixed?
* Using ConversationState.getCurrent().getIdentity() to get groups and memberships of current user instead of using OrganizationService.
  - ConversationState.getCurrent().getIdentity().getGroups() instead of using OrganizationService.getGroupHandler().findGroupsOfUser(currentuser).
  - ConversationState.getCurrent().getIdentity().getMemberships() instead of using OrganizationService.getMembershipHandler().findMembershipsByUserAndGroup(currentuser, groupId)
* Using ConversationState.getCurrent().getAttribute(CacheUserProfileFilter.USER_PROFILE) to get User object instead of using OrganizationService.getUserHandler().findUserByName(currentUser).

Patch files: KS-3606.patch

Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

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

    * No effect sides

Is there a performance risk/cost?
* No risk

Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
*

QA Feedbacks
*
Labels parameters

