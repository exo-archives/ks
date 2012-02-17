Summary

    * Status: answer portlet - incorrect behavior
    * CCP Issue: CCP-1187, Product Jira Issue: KS-4051.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

1) user1 inserts question q1: "is John beautiful?"
2) moderator likes q1 and approves it
3) user1 modifies q1 to q2: "John is ugly"

moderator doesn't like q2 (for example q2 isn't a question), and moderator isn't notified of the change: q2 should not be visible until a new approval.

All question visible by users must be approved.
Fix description

How is the problem fixed?

    * Change logic for create/edit question by Moderators and normal user.
      Before apply patch: In the class SaveActionListener, value of isModerate always is "false" when edit question. ==> Never have to show message about the this question edited is unapproved.
      After apply patch: In the class SaveActionListener, value of isModerate will update new from service ==> We have message about the question edited is unapproved when isModerate = true.


Patch files: KS-4051.patch

Tests to perform

Reproduction test

    * Steps to reproduce:

    * Login as john
    * Add new category:
          o Fill the Category's name
          o Checked:
                + Moderate new questions
                + View question authors
                + Moderate answers
    * Logout and login as mary
    * Submit a new question "John is good?" in the created category
    * The created question in status waiting the category's moderator to approve.
    * Logout and login as john
    * John approve the created question
    * Logout and login as mary
    * Edit the question: "John is not good?"
    * Logout and login as john
      --> John (Category's moderator) isn't notified of the change.
      Expected result: The edited question should not be visible until a new approval.

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

    * No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*
Labels parameters

