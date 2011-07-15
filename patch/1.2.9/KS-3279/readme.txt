Summary

    * Status: Impossible to create a FAQ Category using a name with an apostrophe (')
    * CCP Issue: CCP-924, Product Jira Issue: KS-3279.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Impossible to create a FAQ Category using a name with an apostrophe (')

Fix description

How is the problem fixed?

    * Encode some special character in category name when save category.

Patch file: KS-3279.patch

Tests to perform

Reproduction test

   1. Using Answer Porlet.
   2. Create a category, and put as category name a word containing an apostrophe character (').
   3. Apply category creation =>An error message is displayed (This name is not allowed for a category) and the category is not created

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

    * Function or ClassName change: Not change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Patch validated

QA Feedbacks
*
