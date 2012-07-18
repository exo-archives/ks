Summary

    * [KS-SOC integration] Cannot create new topic for space discussion in special case 
    * CCP Issue: N/A
    * Product Jira Issue: KS-4526.
    * Complexity: N/A

Proposal

 
Problem description

What is the problem to fix?
    * Start a clean package
    * Login as administrator (John)
    * Create new space
    * Go to space discussion and start new topic
    * Logout and login as user who is not administrator (Mary)
    * Create new space
    * Go to space discussion and start new topic
          o Result: Show message "You cannot create new topics without permission." - NOK
          o Expected result: User who is not administrator can create topic in space of which he is manager
            Note: This case will not occur if you login as Mary and do this action at the first time.

Fix description

Problem analysis
    * Cache for Category in CategoryDataStorage#clearObjectCache() isn't cleared due to the wrong constructor of ObjectNameKey.
    * In the CachedDataStorage#getObjectNameById(String id, String type), we use now the ObjectNameKey(String id, String type) constructor to instantiate an ObjectNameKey object (to get or put the data from the cached object) instead of the ObjectNameKey(String path) constructor.

How is the problem fixed?
    * Clear cached object by using the right key, which must be constructed by ObjectNameKey(String id, String type). Utils.CATEGORY is the type of ObjectNameKey object.

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * cf. above

Tests performed at Support Level

    * cf. above

Tests performed at QA

    * N/A

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests

    * No

Changes in Selenium scripts 

    * No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: No
    * Data (template, node type) migration/upgrade: No 

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Patch validated

Support Comment

    * Patch validated

QA Feedbacks

    * 
