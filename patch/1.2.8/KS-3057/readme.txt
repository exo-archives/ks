Summary

    * Status: Advanced Search returns all questions if using (Term=any word, Search in = Entries, Created between/And is filled up)
    * CCP Issue: N/A, Product Jira Issue: KS-3057.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Advanced Search returns all questions if using (Term=any word, Search in = Entries, Created between/And is filled up)

Fix description

How is the problem fixed?

    *  update JCR query for searching: datetime and term constraints is related by "and" operand instead of "or".

Patch files:KS-3057.patch

Tests to perform

Reproduction test
*Steps to reproduce:
1. Launch Answer, create a category, create some questions and answers. Make sure that No category/entry contains the term word that you will use to search.
2. Open form Advance Search. Input any term word. Select Search in = Entries. Input Created between date as a date in past. Input And date as a date in future. Click Search.
3. The failure is: the search returns all questions, while there is no match.

Tests performed at DevLevel
* reproducing test.

* UnitTest

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* None
Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * function org.exoplatform.faq.service.FAQEventQuery#getQuery()

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

