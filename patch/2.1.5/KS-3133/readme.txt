Summary

    * Status: Forum Bad UI if username is too long
    * CCP Issue: N/A, Product Jira Issue: KS-3133.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    *  Forum Bad UI if screen name is too long

Fix description

How is the problem fixed?

    *  If screen name is too long, we replace by short name  + "..."
      ?
      // function
        static public String getScreenName(String screenName) throws Exception {
          if(!isEmpty(screenName)) {
            String s = screenName.replace("<s>", "").replace("</s>", "").trim();
            if(screenName != null && s.length() > 17 && (s.indexOf(" ") > 17 || s.indexOf(" ") < 0)){
              boolean isDelted = false;
              if(screenName.indexOf("<s>") >= 0) {
                screenName = s;
                isDelted = true;
              }
              screenName = "<span title=\""+screenName+"\">"+((isDelted)?"<s>":"")+
                            getSubString(screenName.trim(), 12)+((isDelted)?"</s></span>":"</span>");
            }
          }
          return screenName ;
        }

Patch files: KS-3133.patch

Tests to perform

Reproduction test
*Steps to reproduce:

1. Login other user and go to forum app,
2. Click label Setting in Forum tools bar.
3. Changed Screen name is too long (ex: most 20 characters), Go to Forum/topic -> post a reply -> if bad UI -> KO, else Screen name display short + "..." -> OK

Tests performed at DevLevel
*  No

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

    * Function or ClassName change: None

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL Review : patch approved

Support Comment
* Support review: Patch validated

QA Feedbacks
*

