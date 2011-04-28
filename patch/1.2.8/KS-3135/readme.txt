Summary

    * Status: Forum Bad UI if username is too long
    * CCP Issue: N/A, Product Jira Issue: KS-3135.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Forum Bad UI if username is too long

Fix description

How is the problem fixed?

    *  If username is too long, display short username and show title has content is full username.
    *  If username is short --> display full username.

Patch files: KS-3135.patch

Tests to perform

Reproduction test
*Steps to reproduce:

1. Register a new user with long username (eg. username with 25 characters)
2. Login as this new user
3. Go to Groups/Forum -> post a reply ->bad UI

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
* No
Validation (PM/Support/QA)

PM Comment
* PL review : patch approved

Support Comment
*Support review: Patch validated

QA Feedbacks
*

