Summary

    * Status: [Forum]Text in French exceeds border.
    * CCP Issue: N/A, Product Jira Issue: KS-2817.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
After fixing KS-2629, some French texts exceed the border.
Steps to reproduce: login by root, go to Forum portlet.
- "Discussions" and "Messages" exceed the cell borders
- Show all topics started by root: "Supprimer" exceeds the border
- Select a forum: "Réponses" overlaps the combo box icon
- Click Administration, select IP Bans tab:
- Click Posts link of an existing IP: "Supprimer" exceeds the border

Fix description

How is the problem fixed?
Change width values in the corresponding templates:
    * Stylesheet.css
    * UIForumPortlet/Stylesheet.css
    * UICategory.gtmpl
    * UITopicContainer.gtmpl
    * UIPageListTopicByUser.gtmpl
    * UIPageListPostByIP.gtmpl
    * UIForumAdministrationForm.gtmpl
    * UICategories.gtmpl

Patch file:

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No.

Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?
* Function or ClassName change: no

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* Patch approved

Support Comment
* Support patch review: patch validated

QA Feedbacks
*
