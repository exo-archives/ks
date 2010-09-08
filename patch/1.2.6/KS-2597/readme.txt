Summary

    * Status: Impossible to Reinitialize Avatar (FrenchUI)
    * CCP Issue: CCP-240, Product Jira Issue: KS-2597.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * In French UI, nothing happens when clicking on "réinitialiser l'avatar" , but a javascript error

    * How to reproduce:
         1. Login as root
         2. Go to Groups/Answers
         3. Chose Settings
         4. Click "Update" to upload your avatar
         5. Change language to French
         6. Click on "Réinitialiser" => Nothing happens, we can't reset the avatar => Not OK
            We have a javascript error:

            missing ) after argument list
            [Break on this error] javaScript:if(confirm('Voulez-vous uti...etDefaultAvatar',true)}; return false;

         7. Change language to English
         8. Click on "Reset" => OK, we can reset the avatar

This bug is due to the unescaped apostrophe in the pop-up message:

javaScript:if(confirm('Voulez-vous utiliser l'avatar par défaut?')){javascript:eXo.webui.UIForm.submitForm('answers#UISettingForm','SetDefaultAvatar',true)}; return false;


Fix description

How is the problem fixed?

    * Escape the apostrophe, that is 'Voulez-vous utiliser l'avatar par défaut?' becomes 'Voulez-vous utiliser l\'avatar par défaut?'.

<%=_ctx.appRes("KEY").replaceAll("'", "\\\\'");


Patch information:
Patch files:
KS-2597.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* Cf. above

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

    * OK Fix locally

Support Comment

    * Tested & validated, patch works fine

QA Feedbacks
*

