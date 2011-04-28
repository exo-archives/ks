Summary

    * Status: KS - IE7: Don't show avatar of user after upload successfull
    * CCP Issue: N/A, Product Jira Issue: KS-3118.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  KS - IE7: Don't show avatar of user after upload successfull

Fix description

How is the problem fixed?

    *  Use rest for get link image avatar by function: 

        public static String getImageUrl(String imagePath) throws Exception {
          StringBuilder url = new StringBuilder() ;
          PortalContainer pcontainer =  PortalContainer.getInstance() ;
          try {
            url.append("/").append(pcontainer.getPortalContainerInfo().getContainerName());
          } catch (Exception e) {
            url.append("/portal");
          }
          RepositoryService rService = (RepositoryService)pcontainer.getComponentInstanceOfType(RepositoryService.class) ;
          url.append("/rest/jcr/").append(rService.getCurrentRepository().getConfiguration().getName()).append(imagePath);
          return url.toString();
        }

Patch files: KS-3135.patch

Tests to perform

Reproduction test
* Steps to reproduce:

   1. Login as root
   2. Go to Groups/Answers
   3. Chose Settings
   4. Click Update & browse file for avatar
      => Don't show avatar.

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

    * Function or ClassName change: none

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
*PL review : patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
*

