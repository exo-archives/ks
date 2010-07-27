Summary

    * Status: Forum- Error when delete topic after moving it
    * CCP Issue: CCPID, Product Jira Issue : KS-2560
    * Complexity: LOW
    * Impacted Client(s): Test Campaign AIO 1.6.5 CR4 TESTVN-759
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    *  After move one post form separate forum in one category to other forum in other category and click to remove button it shows unknown error message 

Fix description

How the problem is fixed ?

    * Update topic , forum and category apter moved

Patch informations:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patches files:
 	 
File KS-2560.patch 	 	  	

    * Properties

Tests to perform

Which test should have detect the issue ?
* Move post, delete post

Is a test missing in the TestCase file ?
* No

Added UnitTest ?
* No

Recommended Performance test?
* No
Documentation changes

Where is the documentation for this feature ?
* No

Changes Needed:
* No
Configuration changes

Is this bug changing the product configuration ?
* No

Describe configuration changes:
* None

Previous configuration will continue to work?
* Yes
Risks and impacts

Is there a risk applying this bug fix ?
* No

Is this bug fix can have an impact on current client projects ?
* Yes

Is there a performance risk/cost?
* No
Validation By PM & Support

PM Comment
*

Support Comment
* Tested and validated by Support
QA Feedbacks

Performed Tests
*

