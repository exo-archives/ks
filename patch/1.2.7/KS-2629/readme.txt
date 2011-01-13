Summary

    * Status: Labels are not translated in French
    * CCP Issue: CCP-470, Product Jira Issue: KS-2629
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Some labels are not translated in French
Fix description

How is the problem fixed?
In AnswersPortlet_fr.properties, ForumPortlet_fr.properties files:
* Modify some labels:
  - Sauver -> Enregistrer
  - Vérouillé -> Verrouillé
  - Dévérouillé -> Déverrouillé
  - Topic -> Sujet
  - Icon -> Icône
* Translate English labels into French

Patch file: KS-2629.patch

Tests to perform
Tests performed at DevLevel
* Check KS portlets (Forum, Answers) with French as language setting.

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

Can this bug fix have an impact any side effects on current client projects?

    * Function or ClassName change: none

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PM review: patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
*
