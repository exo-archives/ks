Summary

    * Status: Deleted user issues in forum
    * CCP Issue: N/A, Product Jira Issue: KS-2588.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Problem 1:
         1. Connect as john
         2. Post something in the forum
         3. Connect as root
         4. Delete john
         5. Connect as root
         6. Go to john post and click on him , see his profile -> exception

            [ERROR] portal:Lifecycle - template : app:/templates/forum/webui/popup/UIViewMemberProfile.gtmpl <java.lang.NullPointerException: Cannot invoke method getUserId()
            ull object>java.lang.NullPointerException: Cannot invoke method getUserId() on null object
                    at org.codehaus.groovy.runtime.NullObject.invokeMethod(NullObject.java:77)
                    at org.codehaus.groovy.runtime.InvokerHelper.invokePogoMethod(InvokerHelper.java:784)
                    at org.codehaus.groovy.runtime.InvokerHelper.invokeMethod(InvokerHelper.java:758)
                    at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethodN(ScriptBytecodeAdapter.java:170)
                    at org.codehaus.groovy.runtime.ScriptBytecodeAdapter.invokeMethod0(ScriptBytecodeAdapter.java:198)
                    at script1277203249807.run(script1277203249807.groovy:14)
                    at org.exoplatform.groovyscript.text.SimpleTemplateEngine$SimpleTemplate$1.writeTo(SimpleTemplateEngine.java:128)
                    ...

    * Problem 2:
         1. go to the post
         2. try to reply

            eXo.core.Browser.addOnResizeCallback('mid433809293',function(){eXo.forum.UIForumPortlet.setMaskLayer("UIForumPortlet");}); eXo.forum.UIForumPortlet.submitSearch('QuickSearchForm'); setTimeout('eXo.forum.UIForumPortlet.loadScroll()', 1000); eXo.require('eXo.forum.ForumTotalJob', '/forum/javascript/'); eXo.forum.ForumTotalJob.init("root","6nfcir6ars97"); eXo.forum.UIForumPortlet.initContextMenu('UIForumContainer') ; eXo.require('eXo.forum.webservice.SearchTagName', '/forum/javascript/'); eXo.forum.webservice.SearchTagName.init(); eXo.core.Browser.addOnResizeCallback('mid1516742638',eXo.forum.UIForumPortlet.reSizeImages); eXo.forum.UIForumPortlet.controlWorkSpace(); eXo.core.Browser.addOnLoadCallback('mid641276064',eXo.forum.UIForumPortlet.ReloadImage); eXo.forum.UIForumPortlet.loadTagScroll(); eXo.forum.UIForumPortlet.createLink('UITopicDetail', 'false') ; eXo.forum.UIForumPortlet.submitSearch('SearchInTopic'); eXo.webui.UIPopupWindow.init('UIForumPopupWindow', false, true, true, false); eXo.core.Browser.addOnLoadCallback('mid361052397',function(){eXo.forum.UIForumPortlet.setMaskLayer('UIForumPortlet');}); eXo.webui.UIPopupWindow.init('_31514443', false); eXo.core.Browser.onLoad(); eXo.core.Skin.addSkin('forum_ForumPortlet','/forum/skin/DefaultSkin/webui/Stylesheet-lt.css');

Fix description

How is the problem fixed?

    * Set userId for User-profile of user deleting.

       UserProfile userProfile = new UserProfile();
       userProfile.setUserId(userName) ;
       try {
       ......
       }catch(Exception e) {...
       }
      return userProfile;

Patch information:
Patch files:
KS-2588.patch

Tests to perform

Reproduction test
*

   1. Connect as john
   2. Post something in the forum
   3. Connect as root
   4. Delete john
   5. Connect as root
   6. Go to john post and click on him , see his profile -> show popup userprofile, can not see exception in Terminal.
   7. Go to the post and try to reply, not problem and not see exception.

Tests performed at Dev Level
*

Tests performed at QA/Support Level
*


Documentation changes

Documentation changes:
*No


Configuration changes

Configuration changes:
*No

Will previous configuration continue to work?
*Yes


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * No, Not change function or class name.

Is there a performance risk/cost?
*No


Validation (PM/Support/QA)

PM Comment

    * pm review : patch validated

Support Comment

    * Support review : validated

QA Feedbacks
*

