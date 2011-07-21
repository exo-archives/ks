Summary

    * Status: Not quite easy to re-order sub-categories of one category
    * CCP Issue: CCP-925, Product Jira Issue: KS-3280.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Not quite easy to re-order sub-categories of one category

Fix description

How is the problem fixed?

    * Edit logic drag and drop category.
    * Edit resetIndex categories when drag/drop category, with 7 cases:
      + Move category from index x (x > 1) to top (new index = 1) .
      + Move category from index x up to y (y < 1 && x < y)
      + Move category from index x down to y (x > y)
      + Move category has index x up to top parent category (new index = 1) .
      + Move category has index x up to parent category with index y (y > 1).
      + Move category in to other category has level same this category.
      + Move category up to parent category and then move it in to other category has level same parent category.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
Aucune pièce jointe sur cette page.
Tests to perform

Reproduction test

    * Create 4 sub-categories from one master category (cat1) , called A, B, C, D (in this order). Categories are displayed: A, B, C, D

Case 1:

   1. Move (using Drag&Drop) cat A between B and C. Categories are displayed: B, A, C, D
   2. Move (using Drag&Drop) cat B between A and C. Categories are displayed: B, A, C, D -> nothing changes
   3. Try (using Drag&Drop) to put cat A back before B. Categories are displayed: B, A, C, D -> nothing changes
   4. Move (using Drag&Drop) cat D between A and C. Categories are displayed: B, D, A, C ->D should be between A and C (and not before A)

Case2:

   1. Drag and drop A to the first place in the categories list which contains cat1 -> A in the second place

More generaly, moving a category is not easy, especially when you try to move first or last category or when you try to move a category near first or last one

Tests performed at DevLevel
* NO

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

    * Function or ClassName change: no change in API.

Is there a performance risk/cost?
* NO

Validation (PM/Support/QA)

PM Comment
* PL review : patch approved

Support Comment
* Patch validated

QA Feedbacks
*
