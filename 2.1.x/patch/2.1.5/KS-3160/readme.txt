Summary

    * Status: Forum "Private Message" posts are visible in the global activity stream
    * CCP Issue: CCP-855, Product Jira Issue: KS-3160.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* Forum "Private Message" posts are visible in the global activity stream.

Fix description

How is the problem fixed?
* Filter all forum's posts and topics which are private, not active, close, waiting for approve from the activity stream.

Patch file: KS-3160.patch

Tests to perform

Reproduction test

   1. Login as root. Open Forum portlet.
   2. Send invitations to demo and john. (logging with demo an john to accept invitation).
   3. Create a space (with root) and add john and demo to this space.
   4. Send a private message to john.
   5. Connect with demo => The private message is shown in his activity stream.

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

    * Function or Class name change: no

Is there a performance risk/cost?
* Not detected yet.

Validation (PM/Support/QA)

PM Comment
* PL review: Patch approved

Support Comment
* Patch validated

QA Feedbacks
*

