Summary

    * Status: Error in the order of the tree when adding a category
    * CCP Issue: CCP-1062, Product Jira Issue: KS-4032.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

1. Login
2. Go to Groups/Answers
3. Add Category
Default value of the field "Order": 3 (1.png)
4. Save
Actual result: the new category is classed in the order 2 and not 3 (2.png)
5. Edit the new category
The value of the field "Order" of the new category is 2. (3.png)
6. Edit the value of the field "Order" to 1
Result: the new category takes the first place
7. Edit the order value to 3
The new category is classed in the order 2 (4.png)
8. Edit the value to 4 (while we have 3 categories)
The value found is 2.
Result: the new category takes the 3th place (5.png)

Expected: new category should be placed as the valid input value of the field "Order".
Fix description

How is the problem fixed?

    *  Change logic for save order of category
       + If user input number => total of categories, save the order of this category is total of categories.
       + If user input number < total of categories and > 0, save the order of this category is the number user input and update order all categories by sequential number (1,2,3,....).
       + if user input number < 0, shown message waining.

Patch files:KS-4032.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* cf. above

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

    * Not function or ClassName change

Is there a performance risk/cost?
* Not
Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*

