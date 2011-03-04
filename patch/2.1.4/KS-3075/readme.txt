Summary

    * Status: Problem with French translation in Faq Answer portlet
    * CCP Issue: CCP-796, Product Jira Issue: KS-3075.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Put a new page that contains FAQ answers portlet.
    * Post a question using French language.
      We got:
      "Answers" should be "Réponses"
      "0 minutes ago" shoud be "Il y a 0 minute" (minute > minute, minutes > minutes, hour > heure, hours > heures).

Fix description

How is the problem fixed?

    *  Add missing keys in AnswersPortlet_fr.properties.

Patch file: KS-3075.patch

Tests to perform

Reproduction test
* Cf. above

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
* Function or ClassName change: No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL review: approved

Support Comment
* Support review: Patch validated

QA Feedbacks
*

