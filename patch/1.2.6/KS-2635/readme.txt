Summary

    * Status: Update fisheye URL in pom.xml
    * CCP Issue: N/A, Product Jira Issue: KS-2635.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Update scm in pom.xml with new url:

      <scm>
           <connection>scm:svn:http://svn.exoplatform.org/projects/ks/branches/1.2.x</connection>
           <developerConnection>scm:svn:http://svn.exoplatform.org/projects/ks/branches/1.2.x</developerConnection>
          <url>http://fisheye.exoplatform.org/browse/ks/branches/1.2.x</url>
         </scm>
         <issueManagement>
           <system>jira</system>

Fix description

How is the problem fixed?

    * Change this value to right location in svn

      <scm>
           <connection>scm:svn:http://svn.exoplatform.org/projects/ks/branches/1.2.x</connection>
           <developerConnection>scm:svn:http://svn.exoplatform.org/projects/ks/branches/1.2.x</developerConnection>
          <url>http://fisheye.exoplatform.org/browse/ks/branches/1.2.x</url>
         </scm>
         <issueManagement>
           <system>jira</system>

Patch information:
Patch files:
KS-2635.patch

Tests to perform

Reproduction test
* In the phase of commit, the log will be updated in the fisheye.

Tests performed at DevLevel
* No

Tests performed at QA/Support Level
* No


Documentation changes

Documentation changes:
* No


Configuration changes

Configuration changes:
* Yes, the global pom.xml

Will previous configuration continue to work?
* Yes except Fisheye log.


Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment

    * PM review : patch validated

Support Comment
* Support review : patch validated

QA Feedbacks
*

