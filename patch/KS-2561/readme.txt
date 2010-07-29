Summary

    * Status: Forum: User management: error when search user at the second page
    * CCP Issue: CCPID, Product Jira Issue: KS-2561
    * Complexity: HIGH
    
The Proposal
Problem description

What is the problem to fix?

    * Perform action search when stay in page greater than 1 of user management 

Fix description

How is the problem fixed?

    *  Set, update current page when user selects page

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
There are currently no attachments on this page.
Tests to perform

Which test should have detect the issue?
* Add users to the portal to get data over one page

* Go to page 2 or greater page

* Perform search with any search key

Is a test missing in the TestCase file?
* No

Added UnitTest?
* No

Recommended Performance test?
* No
Documentation changes

Where is the documentation for this feature?
* No

Changes Needed:
* No
Configuration changes

Is this bug changing the product configuration?
* No

Describe configuration changes:
* None

Previous configuration will continue to work?
* Yes
Risks and impacts

Is there a risk applying this bug fix?
* No

Can this bug fix have an impact on current client projects?
* Yes

Is there a performance risk/cost?
* No
Validation By PM & Support

PM Comment
*

Support Comment
* Support Patch Review : Tested and Validated
QA Feedbacks

Performed Tests
*
Labels parameters

