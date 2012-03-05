Summary

    * Status: Critical Security issue with forum's getMessage REST API
    * CCP Issue: CCP-809, Product Jira Issue: KS-3099.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
 In the last forum post gadget forum posts from spaces that I'm not part of !
The forum REST service (which is used by the gadget) should not deliver posts that I am not allowed to see.
This is due to The forum's getMessage REST API which returns the latest forum's posts without any permissions check.
It should accept a <username> parameter and return only posts visible by this <username>. 
Fix description

How is the problem fixed?

    * Check permission for user when get post by rest AIP.
      + Add new function List<Post> getRecentPostsForUser(String userName, int number) in ForumService  
      + Changed function getMessage REST API for checking user login.
      + Add new API getPublicMessage in REST API for get post public (use path: /ks/forum/getpublicmessage/{maxcount})

Patch file: KS-3099.patch

Tests to perform

Reproduction test
Steps to reproduce:

    * Login as root
    * Add latest forum posts Gadget to page (View steps to add this gadget as below)
    * Create new private space by root and create within some topics with some posts
    * Root invite mary to join this space
    * Login as mary and accept invitation of root
      ==>neither root neither mary nor any other user can see posts in the gadget.The gadget does not display any post whoever is the connected user.

How to add Latest forum post Gadget to page
Step by step to add:

    * Download the package at here: http://exoplatform.com/company/en/resource-viewer/Plugins/latest-forum-posts-gadget.
    * Login as root PLF
    * Go to My spaces/IDE
    * Window/Select Workspace: portal-system
    * Go to: production/app:Gadget
    * Add the app:latestForumPosts with the same structure of the downloaded package
    * Open the file: app:latestForumPosts/app:data/app:resources/skin/LatestForumPosts.js
      Replace old code:
      ?
      //Old code:
          eXoLastPostsGadget.prototype.ajaxAsyncGetRequest = function(url, callback) {
            var params = {};
            params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
            gadgets.io.makeRequest(url, callback, params);
            return;
          }

      By new code:
      ?
      // New code:
          eXoLastPostsGadget.prototype.ajaxAsyncGetRequest = function(url, callback) {
            var params = {};
            params[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.SIGNED;
            params[gadgets.io.RequestParameters.METHOD] = gadgets.io.MethodType.GET;
            params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.JSON;
            gadgets.io.makeRequest(url, callback, params);
            return;
          }
    * Open new file: Google Gadget and copy the content of the lastesForumPosts.xml to the Google Gadget file. Save it with the same name of xml file (lastestForumPosts.xml)
    * Open the new created GoogleGadget and deploy it
    * Go to ApplicationRegistry/Auto import
    * Go to My sites/intranet/Home
    * Site Editor/Edit page
    * Add Forum post Gadget in the page
    * Save

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

    * Function or ClassName change: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment
* PL review : patch approved

Support Comment
* Support Review:Patch Validated

QA Feedbacks
*

