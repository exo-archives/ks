Summary

    * Status: Error in the order of the tree when adding a category
    * CCP Issue: CCP-1062, Product Jira Issue: KS-2943.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * 1. Login
      2. Go to Groups/Answers
      3. Add Category
      Default value of the field "Order": 3
      4. Save
      Actual result: the new category is classed in the order 2 and not 3
      5. Edit the new category
      The value of the field "Order" of the new category is 2.
      6. Edit the value of the field "Order" to 1
      Result: the new category takes the first place
      7. Edit the order value to 3
      The new category is classed in the order 2
      8. Edit the value to 4 (while we have 3 categories)
      The value found is 2.
      Result: the new category takes the 3th place

Expected: new category should be placed as the valid input value of the field "Order".
Fix description

How is the problem fixed?

    * Change logic to set order when adding a new category

Tests to perform

Reproduction test

    * cf. above

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

    * Function or ClassName change: None

Is there a performance risk/cost?

    * Non

Validation (PM/Support/QA)

PM Comment

    * N/A

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

