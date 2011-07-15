Summary

    * Status: RepositoryException: Illegal relPath: "null" when editing a category from the Answer portlet
    * CCP Issue: Product Jira Issue: KS-2872.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* RepositoryException: Illegal relPath: "null" when editing a category from the Answer portlet

Fix description

How is the problem fixed?

    *  Set node name when get root category of FAQService.

Patch file: KS-2872.patch

Tests to perform

Reproduction test
   1. Log in as root/exo
   2. Go to Answers portlet
   3. Select one of available categories (if not, create one)
   4. Click Category > Edit and change one of the fields and click Save.
   5. Exception is produced. Look at the stack trace :

          11:53:43,909 ERROR [JCRDataStorage] Failed to get max index category
          javax.jcr.RepositoryException: Illegal relPath: "null"
          	at org.exoplatform.services.jcr.impl.core.LocationFactory.parseNames(LocationFactory.java:212)
          	at org.exoplatform.services.jcr.impl.core.LocationFactory.parseRelPath(LocationFactory.java:90)
          	at org.exoplatform.services.jcr.impl.core.NodeImpl.getNode(NodeImpl.java:191)
          	at org.exoplatform.faq.service.impl.JCRDataStorage.getMaxindexCategory(JCRDataStorage.java:1852)
          	at org.exoplatform.faq.service.impl.FAQServiceImpl.getMaxindexCategory(FAQServiceImpl.java:924)
          	at org.exoplatform.faq.webui.popup.UICategoryForm.updateAddNew(UICategoryForm.java:107)
          	at org.exoplatform.faq.webui.UIQuestions$EditCategoryActionListener.execute(UIQuestions.java:678)
          	at org.exoplatform.webui.event.Event.broadcast(Event.java:52)

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

    * Function or ClassName change: not change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PL Review : patch validated

Support Comment
* Patch validated

QA Feedbacks
*

