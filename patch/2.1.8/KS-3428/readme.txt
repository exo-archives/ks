Summary

    * Status: Error in Manage questions when disable the activated on checkbox
    * CCP Issue: CCP-997, Product Jira Issue: KS-3428.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

1. Login w/Demo
2. Create new Space
3. Go to Answers of the new created space
4. Submit a question
5. Edit question
6. Disable the check box "activated"
-> This question disappears -> OK
7. Click to the Manage question
-> This question is not showed on the screen -> NOK
Fix description

How is the problem fixed?
* Edit bussiness logic in getListCateIdByModerator() method in JCRDataStorage.java file to get all category of moderator

Patch files:KS-3428.patch

Tests to perform

Reproduction test

    * Steps the following to reproduce:
      1. Going to PLF products and login with demo account
      2. Create new Space
      3. Go to Answers application of the new created space
      4. Submit a question
      5. Edit question
      6. Disable the check box "activated"
      --> This question disappears --> OK
      7. Click to the Manage question
      --> This question is showed on the screen --> OK

Tests performed at DevLevel
* cf. above

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

    * Function or ClassName change: None

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

