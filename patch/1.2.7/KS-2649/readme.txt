Summary

    * Status: IE7: Error in Upload file form and can not upload
    * CCP Issue: N/A, Product Jira Issue: KS-2649.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * In IE7, the upload form does not display correctly, there are neither Browse button nor Upload icon. It is therefore impossible to upload a file.

Fix description

How is the problem fixed?

    * Add table-layout to fix UIGrid's style sheet on IE7

Patch information:
    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: KS-2649.patch

Tests to perform

Reproduction test: In Forum portlet (IE7):
* Case 1:
  1. Open a forum
  2. Start a topic
  3. Click "Attach a file" to upload file
  4. There isn't upload icon in the "Attach File" form.

* Case 2:
  1. Go to Manage Category/Import category
  2. Browse button and upload icon disappear => can't import category

* Case 3:
  1. Go to Settings
  2. Click Update to change avatar
  3. Browse button and upload icon disappear (see Forum-Setting-UploadImage.png) => can't upload image

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* N/A

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
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* PM review : patch approved

Support Comment
* Support review : patch validated

QA Feedbacks
*

