Summary
	* Issue title Markup is cached in wrong case
	* CCP Issue:  N/A
	* Product Jira Issue: KS-4431.
	* Complexity: N/A

Proposal

Problem description

What is the problem to fix?

Steps to reproduce
	* Create a child page under WikiHome
	* Open WikiHome and modified its content exactly as
		{{children /}}
    	-> The created child page is listed ->OK
    	* Open this child page and modified its content exactly to
    		{{children /}}
    	-> The created child page is listed ->NOK, the markup is cached incorrectly (The rendered content should be empty)

Fix description

Problem analysis
	* Because the caching entity is composed of key and data, it means if we provide same keys, the cache service always return same data. More details about caching mechanism is mentioned at Wiki markup caching
 	* In this case, because the source markup of 2 pages are the same -> Therefore, the same requested key is constructed and the the same data is returned. -> NOK

How is the problem fixed?
	* The markup key should be updated with the page identities to distinguish with others
	* Add new unit test to simulate this case.

Tests to perform

Reproduction test
	* cf.above

Tests performed at DevLevel
	* cf.above

Tests performed at Support Level
	* cf.above

Tests performed at QA
	* cf.above

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* No

Changes in Selenium scripts 
	* No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:
	* No

Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    Function or ClassName change: No
    Data (template, node type) migration/upgrade: No

Is there a performance risk/cost?
	* TQA should be involved to test perf after bug fix

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	* TQA validated the performance improvement
