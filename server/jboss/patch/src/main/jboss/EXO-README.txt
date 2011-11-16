/**
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/

=====================================================
    Release Notes eXo Knowledge Suite Version 2.2.5
=====================================================

===============
1 Introduction
===============

** eXo Knowledge  Suite provides a rich, Web 2.0 browser-based interface with comprehensive
forum, frequency answer and question 

    * Forum : A full featured message boards application that holds on-line discussions and posts user generated content.
    * Answers : A questions/answers application to help finding solutions
    * FAQ : An Answers-based application to publish questions/answers in public FAQs
    * Wiki : A very new application to help you can work on page data with confluence or xWiki syntax or rich text editor  

=============
2 What's new?
=============


    * General
      - Many bugs fixes 
      - Wiki add support for Confluence Graphical emoticons
      - Forum  User's popup UI Improvement
      - Forum  More actions, search and moderation popup UI Improvement
      - Forum  Support wiki markup
      - Regroup Actions on the editing UI
      - Forum Improve label for censored posts
      - Improve some templates to fit PLF3.5 skin style
      - wiki UI for Save actions are not correctly placed when Help is displayed
      - wiki When navigate in the wiki, the wiki-tree doesn't save its size 
      - Forum From any space we should not be able to go back to the Home of the forum
      - Forum  View User Profile must display the social user profile page if social is present
      - Forum  Replace dates by timeIntervals 
      - Wiki UI improvement to fit with PLF 3.5     
     
    * Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/KS/KS+2-0-Beta02+Release+Notes            
          
=========
3 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/KS/KS+2_0+Quick+Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended)
        JVM: version 1.6 or higher
        Application Server : jboss-5.1.0.GA
        Building Tools: Maven 2.2.1 and up
      
- Knowledge suite quick start guide
    +) jobs: this is main jobss server include Knowledge web applications and all dependencies.     

Need to set the JAVA_HOME variable for run Knowledge suite's servers.
+) How to start Knowledge sute:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.6"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
    
   
   * Start jboss server:
   
     +) On the Windows platform
       Open a DOS prompt command, go to jboss/bin and type the command:
         run.bat start

     +) On Unix/Linux/cygwin
       Open a terminal, go to jboss/bin and type the command:
         ./run.sh

===========================  
4 Other resources and links
===========================
* [Community JIRA>http://jira.exoplatform.org/browse/KS]
* [Subversion>http://svn.exoplatform.org/projects/ks/tags/2.0.0-Beta02]
* [Download>http://download.forge.objectweb.org/exoplatform/]
* [Live demo>http://ks.demo.exoplatform.org]
* [Announces>http://blog.exoplatform.org/tags/ks/]
* [Company site>http://www.exoplatform.com]

====================
5 Detailed Changelog 
====================
- Version 2.0.0-Beta02
* Bug
    * [KS-2236] - Exception throws in console when deleting category and question in Answers 
    * [KS-2243] - Added answers in question disappears after discuss question
    * [KS-2291] - Question with multi-language disappears when open its from Search result
    * [KS-2292] - Posts by user disappears after delete post in special case.
    * [KS-2295] - Private message: show code error when delete message in special case
    * [KS-2296] - Private message: Break UI when inbox has more 9 messages
    * [KS-2297] - IE7: Set align in a line for icons in Permission tab. see file attach
    * [KS-2305] - French label problem on forum
    * [KS-2311] - Tomcat KS 2.0 Beta01 doesn't start on linux
    * [KS-2312] - JBoss KS 2.0 Beta01 doesn't start on linux

** Improvement
    * [KS-2244] - Forun : the discussion windows must be resized 

** Task
    * [KS-2000] - JBoss packaging via -Ppkg-jbossas
    * [KS-2300] - Move test-utils into platform/commons/testing
    * [KS-2322] - Using plaform/commos/testing instead of ks.test-utils for KS.

** Sub-task
    * [KS-2302] - Can not view image attached in Answers and Forum application.
    * [KS-2303] - In public mode: result of quick search is incorrect & throw exception in console
    * [KS-2307] - Ban IP don't work
    * [KS-2313] - Update testcase for Beta02

- Version 2.0.0-Beta01

** Bug
    * [KS-1971] - the forum service doesn't take into account user profile modifications 
    * [KS-2155] - Permission tab: User/member/group is still displayed although removed.
    * [KS-2169] - Show "Forum rules" is incorrect in special case
    * [KS-2170] - Show message "You must add a category first. " when add forum although category existed.
    * [KS-2172] - Forum description does not show up correctly when long
    * [KS-2173] - Guest & users still search topic although without view in special case
    * [KS-2174] - Displaying all topics/posts in all forums when keyword search is "Forum"
    * [KS-2185] - Edit poll: show message "Add more option" although there are at least 2 option 
    * [KS-2187] - Guest & normal user can not search topic/post
    * [KS-2196] - Porlet is display error and throw exception in console after delete "Root" category
    * [KS-2197] - Show "Home page" in Add topic form and can not add topic see file attach
    * [KS-2201] -  FAQ - Question link does not wok
    * [KS-2202] - Show pop up message when edit a topic type. see file attach
    * [KS-2213] - Rename popup "Quick search result". See file attach
    * [KS-2214] - "Run,close" button in Run-prune form don't work
    * [KS-2217] - Some bugs with Setting and active prune
    * [KS-2219] - Cannot open the forums created by the random initializer
    * [KS-2220] - Scoping of Edit Mode tab does not work well in special cases
    * [KS-2221] - Send Question: can only send with default language only
    * [KS-2223] - login/logout listener write data on each user login/logout
    * [KS-2227] - Forum : "ban IP" style issue (152087)
    * [KS-2228] - Next button is still visible althoug there is no item that hiding. See file attach
    * [KS-2239] - Forum AuthenticationLogin/Logout listeners generate too much JCR operations/ Many cl*.tmp files generated
    * [KS-2245] - Forum : poll expiration not well localized
    * [KS-2247] - Displaying status of topic is incorrect in special case.
    * [KS-2248] - Always required approve post reply in topic which discussed from question.
    * [KS-2250] - Advanced search: Show duplicate question when search a question which contains attachment
    * [KS-2251] - Guest & normal user can not search category by advanced search
    * [KS-2255] - Can not search category after scoping category in special case
    * [KS-2256] - Still show Question's author in print preview form although it isn't display in question
    * [KS-2267] - Show code error and throw exception in console when view related question in multi-language question
    * [KS-2270] - Show code error when view topic which contains attachment
    * [KS-2271] - Nothing happen when open topic from User management form
    * [KS-2275] - IE 7: Error JS when view topic by user. See file attach
    * [KS-2282] - Topics are disappear and throw exception after delete topic in special case
    * [KS-2285] - Can not click any action in Forum in special case
    * [KS-2288] - Attached image don't shown when view question/topic
    * [KS-2290] - Show message error when click on "Tag" icon. see file attach
    * [KS-2293] - Get link of notification is private when watching category

** Improvement
    * [KS-1975] - FAQ - Language switch
    * [KS-2088] - Round the attachment sizes up to Kb
    * [KS-2215] - Set align for label and "Add" icon in a line. see file attach

** Task
    * [KS-2147] - Deploy ksdemo.xml file to ../Catalina/localhost when deploying tomcat
    * [KS-2209] - Review some class in Answer Portlet.
    * [KS-2246] - FAQ : incorrect label submit new question dialog
    * [KS-2265] - Upgrade to GateIn GA
    * [KS-2283] - Release KS 2.0 Beta01

** Sub-task
    * [KS-2044] - Bug unknown error when setting "Category scoping" in Edit mode
    * [KS-2045] - Show message "The faq no longer exists" when discuss question
    * [KS-2252] - Don't send notify when watching category
    * [KS-2253] - Question with attachment disappears after move
    * [KS-2254] - Don't sent notify when watching category/forum/topic
    * [KS-2257] - Tags auto-suggested don't work


- Version 2.0.0-Alpha03

http://jira.exoplatform.org/secure/ReleaseNote.jspa?version=11271&styleName=Text&projectId=10150&Create=Create

** Bug fixed: 
	KS-1825 - Error messages on startup   
	KS-1962 - ContinuationService not porperly initialized 
	KS-2027 - UI Broken when Property not found exo:joinedDate 
	KS-2061 - Java Heap Space after move Category with mouse 
	KS-2064 - Error JS when add porlet "Answer" in first time 
	KS-2087 - User profile should also show first and last name 
	KS-2099 - Don't send notify for watcher in answers portlet 
	KS-2103 - Menu labels are not resolved 
	KS-2123 - Invalid User reappeared in text box although removed 
	KS-2124 - RSS feed: still displaying topic/post which deleted 
	KS-2127 - English message to translate 
	KS-2145 - Guest & users still search topic which without view in special case 
	KS-2159 - Error UI in Upload File form of Answer porlet. See file attach 
	KS-2160 - Set align for "Unwatch" item in menu action. see file attach 
	KS-2162 - Guest&normal user can not search category/forum/topic 
	KS-2163 - Post reply button is invisible although user login have permission to add post 
	KS-2198 - Close button in quick search resul form don't work 
	KS-2237 - Nothing happen when select forum to discuss 
	KS-2241 - Bug alert erro javaScript when open forum. 

** Improvement: 
	KS-862  - Remove hard coded references to /portal from web\ksportal\src\main\webapp\WEB-INF\conf\script\groovy\SkinConfigScript.groovy   
	KS-1242 - Create Shorter Permlinks 
	KS-1314 - Reuse User Component 
	KS-1884 - Backport (Forum) Translate the word Online Users in FR 
	KS-2073 - Avoid caching organizationService's user data 
	KS-2086 - Users should not be able write private messages to themselves 
	KS-2153 - Need to a space between text in note 
	KS-2161 - Should set Date/Time in a line. see file attach 
	KS-2218 - Administration Menu 

** Task: 
	KS-1926 - Support Java 6   
	KS-1954 - Factorize BBCode Rendering 
	KS-2006 - ks logo for favicon of ks demo 
	KS-2009 - Remove "Manage category" from main tool bar 
	KS-2067 - Rename of some pop up to more clear. See file attach 
	KS-2094 - Unit Test FAQEventQuery 
	KS-2095 - Unit Test ForumEventQuery 
	KS-2098 - Fix integration tests 
	KS-2146 - Rename the rest-war.war to rest-ksdemo.war 
	KS-2154 - Rename pop up "Move post". See file attach 
	KS-2157 - Rename pop up "Split" & "Watches" a topic. see file attach 
	KS-2158 - Upgrade to Gatein beta4 
	KS-2164 - Clean and test ForumTransformHTML 
	KS-2165 - Factorize BaseUIForm class 
	KS-2166 - Leverage UIBaseForm where possible 
	KS-2188 - Upgrade to gatein beta5 
	KS-2192 - Configure the build to enforce the use of java 6 to build and check the compatibility with java 5 APIs 
	KS-2230 - Using ExoContainerContext for get Component, replace for PortalContainer. 
	KS-2232 - Cometd is called too frequently 


- Version 1.2.2


Release Notes - exo-ks - Version ks-1.2.2

** Bug
	 * [KS-1946] - Can't save an FAQ question while focus is in FCK Editor 
	 * [KS-1980] - Wrong number of pages(zero) when using ldap-configuration 
	 * [KS-1986] - Starts in Rate box are hidden. See file attach 
	 * [KS-1994] - Change label in Add category form 
	 * [KS-2011] - RSS feed: Don't get content of category/forum/topic which is default data. 
	 * [KS-2016] - The name of days are not translated 
	 * [KS-2024] - IE7: Show error message when discuss question to forum. See file attach 
	 * [KS-2031] - Redirection to classic portal is hardcoded 
	 * [KS-2034] - Signature could not be saved in Settings 
	 * [KS-2035] - Add watch function is not updated with new email address of root 
	 * [KS-2112] - Unknow error when Preview a quick post 
	 * [KS-2113] - Can NOT create topic in Forum 
	 * [KS-2114] - Small Icon display error in Posts by User form 
	 * [KS-2117] - Answers - Invalid title in Search Result forum 
	 * [KS-2118] - Answers: RSS Feed is not ok with created category and question 
	 * [KS-2119] - Invalid display of some item is Answer popup menu 
	 * [KS-2120] - Ivalid RSS Feed 
	 * [KS-2121] - Signature is not saved and displayed 
	 * [KS-2122] - Rate this topic, star in Rate form is not displayed 
	 * [KS-2125] - Discussion function does not work well 
	 * [KS-2129] - IE7 Attach File form is invalid 
	 * [KS-2130] - Can NOT add Forum Category in special case 

	 
** Improvement 

	 * [KS-1660] - Error UI in list tag pane. See file attach 
	 * [KS-1941] - Make "Post Reply" clickable even for guest users 
	 * [KS-1942] - Make import/export labels clearer 
	 * [KS-1944] - Make error messages clearer 
	 * [KS-1945] - sort user list alphabetically 
	 * [KS-1975] - FAQ - Language switch 
	 * [KS-2003] - Add icon for "Untag" item in menu. See file attach 

	 
** Task
	 * [KS-1734] - Avoid expensive calls to getAll() 
	 * [KS-1895] - Do not use ajax by default 
	 * [KS-1947] - Version on 1.2.x should contain-SNAPSHOT 
	 * [KS-2126] - Upgrade KS branch 1.2.2-SNAPSHOT to use portal 2.5.7-SNAPSHOT 
	 * [KS-2131] - Cleanup the build process to be able to deploy on eXo Nexus with the release plugin for 1.2.x 


- Version 1.2.1 

Release Notes - exo-ks - Version ks-1.2.1

** Bug
 * [KS-1412] - xml markup inside [CODE] is 'swallowed' by WYSIWYG 
 * [KS-1695] - FAQ - In Print Preview mode, unvote function still works 
 * [KS-1735] - FAQ : CSS error on the contact page 
 * [KS-1762] - FAQ: Invalid mouse over effect in the Right click menu/pop up menu 
 * [KS-1764] - In IE7: Lose border in Edit profile form. see file attach 
 * [KS-1765] - In IE 7: Little error in topic type table in Form administration form. See file attach 
 * [KS-1767] - Search icon in forum should not re-use css "search" class, but a different css class 
 * [KS-1774] - Watch body gets "null" 
 * [KS-1776] - IE7- print preview small UI error in case question having comments 
 * [KS-1779] - Upload Icon does not appear in forum attachment 
 * [KS-1810] - Manage Question form: Show all question existing into pending question list. 
 * [KS-1827] - FAQ: Still can move category when only have one category. 
 * [KS-1831] - Question isn't updated after edit category which contains that question. 
 * [KS-1832] - mail mix/is cut with question in faq 
 * [KS-1844] - Can not search question in language which not default by normal user. 
 * [KS-1848] - User still can edit, delete post although without view permission in special case 
 * [KS-1851] - Show confirm message is unexact when delete post from IPBan tab. see file attach 
 * [KS-1852] - Forum & FAQ- QUOTE & CODE tag does not work well in special case 
 * [KS-1853] - Answer, IE7: Show error popup message when select discuss forum function 
 * [KS-1854] - Can't import category in special case 
 * [KS-1855] - Answer: Can not sort category and question 
 * [KS-1856] - Alert message is wrong when import category with wrong data format 
 * [KS-1857] - Exception error when importing the category zip file in special case 
 * [KS-1858] - Icon of topic type is still displayed in topic list although deleted this topic type 
 * [KS-1859] - Answer,FF: Exception when select "Discuss Forum" 
 * [KS-1860] - User role is still "Moderator" although user isn't moderate any category/forum. 
 * [KS-1861] - Forum: Datetime is not changed by new changed timezone in Pending list 
 * [KS-1862] - Edit profile: ticking on [Forum administrator] - checkbox isn't saved in special case 
 * [KS-1863] - User hasn't moderate right in forum although added. 
 * [KS-1864] - FORUM: Selected category is still listed in Moderator category althoug removed. 
 * [KS-1868] - Inspite of being deactivated, the Bold tag still effects 
 * [KS-1869] - Banned user can still send private message from topic content 
 * [KS-1870] - Can NOT activate pruned topics 
 * [KS-1871] - Can't set permission for user when create Forum 
 * [KS-1872] - Can not search Forum by State only 
 * [KS-1873] - Forum: Error when sort Forum/topic 
 * [KS-1874] - Still can create topic in forum by Banned IP in special case 
 * [KS-1875] - Datetime is not changed by new changed timezone when preview post 
 * [KS-1876] - Display value of screen name is incorrect when view topic from Edit profile form 
 * [KS-1877] - Show/hide Forum jump isn't updated after save 
 * [KS-1878] - Show duplicate language in Answer question form. See file attach 
 * [KS-1879] - Added relate question isn't shown after save. 
 * [KS-1881] - Manage question/category function disappears in toolbar although login by administrator 
 * [KS-1888] - CODE is not rendered properly in mail messages 
 * [KS-1889] - Show Forum rules is incorrect when Forum banned by IP 
 * [KS-1891] - Answer of multi-language question isn't displayed in special case 
 * [KS-1898] - In Post by user form: Show value of IP logging is "null" in special case 
 * [KS-1899] - Can not move category to root category 
 * [KS-1903] - Improve the notification messages received by exo forum 
 * [KS-1904] - Jump to last read post is not updated when post is moved 
 * [KS-1905] - Home button does not repect useAjax 
 * [KS-1906] - In FF 3.0: Popup menu is hidden below footer pane. See file attach 
 * [KS-1907] - Answer: Show text is "French" in Send mail template. 
 * [KS-1909] - Attached image in question isn't shown after view. 
 * [KS-1911] - Disapprove & Inactivate answer in Answer form isn't saved. 
 * [KS-1912] - Wrong message and Show pop up menu in category is unexact by other user 
 * [KS-1914] - Answer porlet: Alert message is shown below Import pop up 
 * [KS-1915] - Manage question: Filter question in category is incorrect when login by moderator. 
 * [KS-1916] - Category disappears in category list after drag and drop in special case. 
 * [KS-1917] - Advanced search: Can not search question by language property 
 * [KS-1918] - Normal user can not search question in category which added new 
 * [KS-1919] - Moderate question function don't work 
 * [KS-1923] - Automate the adding multi-language question although only select default language. 
 * [KS-1924] - Problem with validate email field in Submit question form. 
 * [KS-1927] - Need to refresh (F5) browser to make click on topic work 
 * [KS-1929] - Sort answer by rate don't work 
 * [KS-1933] - [BFPME-475] i18n labels 
 * [KS-1946] - Can't save an FAQ question while focus is in FCK Editor 

** Improvement
 * [KS-1365] - Scroll back to top when bentering a category 
 * [KS-1434] - Error in displaying post after being edited with long text for result 
 * [KS-1458] -  Errors relate to Print 
 * [KS-1468] - Add delete and move actions to category contextual menu 
 * [KS-1491] - With Guest, don't should allow submit question 
 * [KS-1547] - Display Page iterator only when needed 
 * [KS-1653] - Should allow select user from available list to Moderator field in Advanced search form. 
 * [KS-1769] - Improve quick search function with special character 
 * [KS-1775] - FAQ - Should show the name of folder in the alert message 
 * [KS-1778] - Direct display of image attachments is too big 
 * [KS-1782] - Email Notification link should open the corresponding forum page 
 * [KS-1783] - Avoid duplicate notifications for watched forum topics 
 * [KS-1798] - Remember current page index during session 
 * [KS-1826] - quick search should run when hit <Enter> key in search field 
 * [KS-1833] - Don't show "Root" category in FAQ application 
 * [KS-1849] - Do not allow 2 categories with same name 
 * [KS-1850] - Don't show alert message when export with no Category 
 * [KS-1897] - FAQ should render BBCode 
 * [KS-1910] - Refresh question list and close viewing question when click on category 
 * [KS-1913] - Unwatch 
 * [KS-1928] - Move form still displays Root 
 * [KS-1941] - Make "Post Reply" clickable even for guest users 

- Version 1.2 Final

Release Notes - exo-ks - Version ks-1.2

** Bug
    * [KS-1104] - export/import the forum content doesn't work
    * [KS-1108] - rename permlink in permalink
    * [KS-1120] - When creating a post with a link [url] or [link] the link is not visible in Preview Mode
    * [KS-1126] - When reconnect to the forum it is not possible to know what I have read or not
    * [KS-1127] - Forum home page: New Topic/ No New Topic are not based on what the user as already read
    * [KS-1241] - Permlink should create public links
    * [KS-1245] - Dialog jumps when typing on Enter
    * [KS-1251] - Temporary files on the filesystem are not deleted after upload
    * [KS-1307] - Unknown error related to Breadcrumb in special case 
    * [KS-1311] - Sign in button is displayed in two lies
    * [KS-1316] - IE6: Error in case long link added
    * [KS-1336] - Ban automatically release is not updated ontime
    * [KS-1355] - IE7: Remove horizonal bar on some forms
    * [KS-1358] - Rename the node type exo:userProfile to avoid conflict with the one defined into the JCR
    * [KS-1359] - Can't view quote in preview form
    * [KS-1360] - Add topic with post notification and then edit the topic, the check bos post notification is not ticked
    * [KS-1362] - Should check both "post reply" and "Message" in case having censor word
    * [KS-1363] - UI bug in privew private post form
    * [KS-1364] - MAC, FF3: Sign In link is dropped down
    * [KS-1366] - still create topic for forum although the forum is locked or closed in special case
    * [KS-1367] - [FQA] has problem when delete the catagory before click [save] button of importing category for FQA 
    * [KS-1368] - [forum] Import form is overlayer on message form
    * [KS-1370] - [forum] show UNKNOW ERROR when edit For deleted category
    * [KS-1373] - Unknown error when edit comment of Question in special case
    * [KS-1374] - Bug unknown error when do some actions in topic in special case.
    * [KS-1375] - Could not configure "Dicuss in Forum" from portlet-preferences.xml
    * [KS-1381] - Search must be scoped
    * [KS-1384] - User profile does not show user account information
    * [KS-1407] - Wrong French Message (answer and question confused)
    * [KS-1408] - Typos and misspelled words in KS portlets
    * [KS-1410] - anonymous user can use account of admins or others
    * [KS-1416] - Execution of "Previous" function in main action panel is incorrect in special case.
    * [KS-1417] - In Breadcum bar: Title of page is moved to right screen. see file attach
    * [KS-1419] - Email which typing directly is removed after select email available from pop up.
    * [KS-1421] - Pop up message: text is overlap. See file attach
    * [KS-1422] - Safari4: Error in showing editor form
    * [KS-1423] - Nothing happen when click on "Your account" link in banner
    * [KS-1424] - Error UI in navigation bar in Mac and Vista skin. See file attach
    * [KS-1425] - Don't display content of "Application registry" page when selected.
    * [KS-1426] - Reply posts count is not correct: the number of reply posts is not increased with new added but decreased with deleted private post 
    * [KS-1427] - Can not preview quick post with less than 4 characters
    * [KS-1428] - Bug unknown error when Create/Edit page wizard
    * [KS-1429] - Can not upload file at the second time after remove the browsed file
    * [KS-1430] - Bug unknow error when Create new portal
    * [KS-1431] - Text in Menu action is overlap.
    * [KS-1432] - Can not add a signature in different lines
    * [KS-1437] - Always export all existing categories although do not choose all
    * [KS-1438] - Error when split topic
    * [KS-1439] - Username of last post is not correct when last post is a private post
    * [KS-1440] - Counting the number of post to display in pages are not the same between inside a specific topic & in the topics list
    * [KS-1441] - Category  isn't show although selected to show.
    * [KS-1442] - Number of pending posts is not updated after delete being hidden post
    * [KS-1443] - Posts counting is incorrect when hidden post is deleted
    * [KS-1444] - Posts order is incorrect when move to new topic in special case
    * [KS-1445] - Error in displaying tag's desc in different lines
    * [KS-1446] - Bug view date time when set ZoneTime.
    * [KS-1447] - Display name of forum isn't exact in special case.
    * [KS-1448] - Do not refresh to remove added vote in special case
    * [KS-1450] - Show message requires to answer the question in default language but the answer form is closed
    * [KS-1451] - Little error when get user profile
    * [KS-1454] - Safari4: Limit of text area to input text
    * [KS-1457] - Safari4: Do not display image for avata when print question
    * [KS-1461] - Colon in Upload file form was dropped down after uploaded file for avata
    * [KS-1462] - In First time, Show alert message and Don't redirects directly to the discussed topic  when Discuss forum.
    * [KS-1463] - Error when edit category order
    * [KS-1464] - Edit mode: Show message require "select forum" while selected forum.
    * [KS-1465] - Category is not selected in left pane when jump to that category from search result
    * [KS-1467] - User profile form isn't display when Profile includes "Birthday" property.
    * [KS-1472] - Cannot to create a new page
    * [KS-1477] - Nothing happen when click on "Discuss forum" icon
    * [KS-1478] - Display the number of topic pending isn't exact when discuss forum.
    * [KS-1479] - Display the number of topic pending isn't exact when discuss forum.
    * [KS-1481] - Still can add discuss question to forum which locked in special case
    * [KS-1482] - Integration with ECM/DMS: Links at the top-right corner of the forum portlet are badly positionned.
    * [KS-1484] - Unknown error  When click on "RSS" button in action bar 
    * [KS-1486] - Can not move question to category although user is administrator.
    * [KS-1487] - Counting the number of question in Category stats pane isn't exact.
    * [KS-1499] - Content of message is unexact in special case
    * [KS-1500] - Can not add poll for topic after remove poll from "Poll porlet"
    * [KS-1501] - "Move & Delete post" action is not working.
    * [KS-1502] - "Topics have" should be moved to a locale properties file
    * [KS-1516] - In IE7: Content of confirm message isn't displayed when delete question. See file attach
    * [KS-1517] - Content of alert message is incorrect in special case
    * [KS-1518] - In IE7: Can not goto Forum & FAQ page when click on "Try now" link from Home page
    * [KS-1522] - In IE 7: Lose style sheet on Tool bar. See file attach
    * [KS-1524] - Unknown error when add topic in special case.
    * [KS-1525] - Imported forum isn't displayed in special case.
    * [KS-1527] - Email address of user don't get to "Watch Tools" form after watch a specific forum.
    * [KS-1544] - Show duplicate post when view RSS feed of topic in special case.
    * [KS-1546] - The number of Pending post isn't count in special case.
    * [KS-1548] - Left pane in FAQ is blank after delete category in special case.
    * [KS-1549] - datetime does not like selected timezone when view posts of specific user
    * [KS-1550] - Question isn't displayed after add answer with option is inactive.
    * [KS-1553] - With normal user, RSS feed is still list answers which disapprove/inactive.
    * [KS-1554] - Error UI in Private message form. See file attach
    * [KS-1555] - Show message "This faq entry no longer exists" when link to FAQ in special case.
    * [KS-1556] - Search result isn't correct when search by "Created after" condition
    * [KS-1557] - Advanced search form disappear when search entries by French/Vietnamese
    * [KS-1558] - Don't show forum which imported from Move topic form in special case.
    * [KS-1559] - Display permlink of search result is incorrect.
    * [KS-1560] - Show message "Could not add the new topic because the forum no longer exists" when split topic in special case
    * [KS-1561] - Split topic form: Should keep ticking on check box when have warning message.
    * [KS-1562] -  Pending list bug when moving post to another topic
    * [KS-1565] - Show message "the forum moved or deleted" when set Edit mode in special case.
    * [KS-1567] - The number of post in topic which merged isn't count.
    * [KS-1568] - Show message and can not search.
    * [KS-1572] - Poll portlet has a fixed width
    * [KS-1576] - Show message "The file has been imported" but category isn't imported in special case
    * [KS-1579] - See "All Posts by" does not show thread starting posts
    * [KS-1580] - Private Mode: Links in "All posts by" and "All topics by" not valid
    * [KS-1597] - Error UI with Page interator after add new tag for topic. see file attach
    * [KS-1598] - Sub-category isn't display after add.
    * [KS-1599] - Added category isn't displayed in special case
    * [KS-1600] - Added question isn't displayed when Question field is blank.
    * [KS-1601] - Ramdom error in the number of "messages of categories" of the home page of the KS Forum, it doesn't show the good number but "-1"
    * [KS-1602] - Forum : some French translations for the wordings of the forum
    * [KS-1603] - Forum : Focus problem when user do an answser
    * [KS-1604] - FAQ : some French translations for the wordings of the forum is missing or bad
    * [KS-1606] - Normal user still see posts which pending for approval in special case.
    * [KS-1607] - Advanced search with creation dates is unclear
    * [KS-1626] - User still can search category which user don't see.
    * [KS-1627] - User management: Forum name is still displayed in Moderator forum field although untick.
    * [KS-1629] - Return value of User title and Forum role are incorrect when edit user's title of normal user.
    * [KS-1630] - Value of User role isn't updated after remove Moderator right.
    * [KS-1632] - Show code error in UI when edit profile of user
    * [KS-1633] - Update email notification isn't saved.
    * [KS-1634] - Editing Root category isn't saved.
    * [KS-1635] - Displaying time in View user profile forum is incorrect.
    * [KS-1636] - Alert message is displayed below Forum administrator form.
    * [KS-1652] - Alert message appears below Add topic/post form when preview.
    * [KS-1655] - multiple cometd instances.
    * [KS-1656] - Error UI in Home page when open control workspace panel. See file attach
    * [KS-1657] - Still show category/forum which hiding in Forum jump list box.
    * [KS-1658] - Content of message is unexact when search question in FAQ
    * [KS-1672] - Scope search in specific category/forum/topic
    * [KS-1674] - In IE7: Show message error when open Administration form. See file attach
    * [KS-1675] - Error when go directly category by link in FAQ notificaiton
    * [KS-1676] - problem on ks resource and make the error on cs when run on webos mode
    * [KS-1678] - Error in UI & exception in console when change title of "root" in Forum Users management
    * [KS-1679] - Forum - topic - post notification setting doesn' t get saved 
    * [KS-1680] - UI view Answer content is small, it has space superfluous. See attachment.
    * [KS-1681] - Check Search function in Email Address form
    * [KS-1684] - When sending an email notification include a link to the forum thread
    * [KS-1686] - Ban IP in specific forum or all forums don't work
    * [KS-1687] - FAQ - WebOS Right click menu is displayed invalidly
    * [KS-1689] - FAQ- portlet setting - alway request reselecting category/forum to discuss
    * [KS-1690] - [KS-Forum]User still view category which user without view right when go by permlink.
    * [KS-1709] - Displaying value of Ban reason is "null" when don't input value in Ban reason field.
    * [KS-1712] - Question isn't displayed in RSS feed
    * [KS-1714] - Print all questions: avatar of user isn't displayed
    * [KS-1719] - unresponsive script
    * [KS-1730] - Topic Type fields are not i18ned
    * [KS-1733] - Show message and can not merge topic
    * [KS-1736] - FAQ : CSS error on the contact page : bad order for the multipage numbers
    * [KS-1738] - User still see topic which without view permission in last post column
    * [KS-1739] - In search result of normal user, still display topic which pending censor
    * [KS-1740] - Unknown error when comment for question by right click
    * [KS-1741] - In the current, Vote question isn't work.
    * [KS-1743] - show message when discuss forum 
    * [KS-1746] - Display name of category isn't exact in special case. see file attach
    * [KS-1747] - In User watch manager form: Show category ID instead of Category name
    * [KS-1748] - Editing question isn't updated after saved
    * [KS-1753] - Forum- THe Button "Go to" (Aller in FR) dosn't work for the first choice ("Acceuil")
    * [KS-1761] - FAQ- Unknown error when edit a question that no longer exists in Manage Questions form
    * [KS-1763] - Can not add multi language for question when edit.
    * [KS-1766] - In IE 7: Error UI in Poll porlet. see file attach
    * [KS-1770] - Imported Category & Question aren't displayed in special case.
    * [KS-1771] - Watched email is still displayed in Manager watch form although deleted.
    * [KS-1772] - Show message "This faq category no longer exists" when edit Root category
    * [KS-1773] - [KS-FAQ] UI error in the Search Result form
    * [KS-1777] - Forum - Advance search - "Clear filelds" button doesn't work in webos
    * [KS-1792] - JCR session leaks
    * [KS-1795] - Bug unknown error when view topic in special case.
    * [KS-1810] - Manage Question form: Show all question existing into pending question list.
    * [KS-1812] - Category isn't viewed property although selected to open from Watch manager form.
    * [KS-1813] - Loosing author when promoting comment as answer
    * [KS-1814] - A normal user can still search a desactivated question
    * [KS-1816] - Add category: Get the number order of category is incorrect after add new question.
    * [KS-1829] - Question list does not refresh
    * [KS-1830] - Exception when guest click on Answer portlet the first time with empty database
    * [KS-1836] - Only root can search in answers
    * [KS-1839] - Vote on answer does not work
    * [KS-1841] - Avatar isn't displayed in question when view
    * [KS-1842] - Move category don't work and show code error in UI
    * [KS-1843] - Still can search questions in category which user has not view right.
    * [KS-1848] - User still can edit, delete post although without view permission in special case
    * [KS-1851] - Show confirm message is unexact when delete post from IPBan tab. see file attach
    * [KS-1852] - Forum & FAQ- QUOTE & CODE tag does not work well in special case
    * [KS-1853] - Answer, IE7: Show error popup message when select discuss forum function
    * [KS-1854] - Can't import category in special case 
    * [KS-1855] - Answer: Can not sort category and question
    * [KS-1856] - Alert message is wrong when import category with wrong data format
    * [KS-1857] - Exception error when importing the category zip file in special case
    * [KS-1858] - Icon of topic type is still displayed in topic list although deleted this topic type
    * [KS-1859] - Answer,FF: Exception when select "Discuss Forum"
    * [KS-1860] - User role is still "Moderator" although user isn't moderate any category/forum.
    * [KS-1861] - Forum: Datetime is not changed by new changed timezone in Pending list
    * [KS-1862] - Edit profile: ticking on [Forum administrator] checkbox isn't saved in special case
    * [KS-1863] - User hasn't moderate right in forum although added.
    * [KS-1864] - FORUM: Selected category is still listed in Moderator category althoug removed.
    * [KS-1868] - Inspite of being deactivated, the Bold tag still effects 
    * [KS-1869] - Banned user can still send private message from topic content
    * [KS-1870] - Can NOT activate pruned topics
    * [KS-1871] - Can't set permission for user when create Forum
    * [KS-1872] - Can not search Forum by State only
    * [KS-1873] - Forum: Error when sort Forum/topic
    * [KS-1875] - Datetime is not changed by new changed timezone when preview post
    * [KS-1876] - Display value of screen name is incorrect when view topic from Edit profile form
    * [KS-1877] - Show/hide Forum jump isn't updated after save
    * [KS-1878] - Show duplicate language in Answer question form. See file attach
    * [KS-1879] - Added relate question isn't shown after save.

** Improvement
    * [KS-779] - Limit upload file size
    * [KS-1243] - Code tag renders too small
    * [KS-1244] - Code tag should not add text
    * [KS-1305] - Add new logo to the product homepage
    * [KS-1306] - View post / reply to post links in email notifications
    * [KS-1309] - Make easy to modify  the content of the moved notification 
    * [KS-1313] - Improve BBCode icons
    * [KS-1329] - Now we can search questions in a day
    * [KS-1333] - Apply Long date format into  the date of Most Users ever online field
    * [KS-1369] - Add path parent Category in Manage questions form.
    * [KS-1372] - Provide * symbol for required field in Send question form
    * [KS-1376] - Move search to be always visible
    * [KS-1378] - Toolbar remains visible
    * [KS-1382] - Forum not displayed when scoped
    * [KS-1383] - User search should match more fields
    * [KS-1409] - Display an icon on topics attaced to a poll
    * [KS-1435] - Smooth Refresh form action when remove attached file from a post
    * [KS-1452] - Change title "Address" to be "Add" in Add user for CC, BCC form when send mail a question
    * [KS-1453] - Should not hide the CC/BCC field after choose user to send mail from contacts list
    * [KS-1459] - Message content is not correct when save an editing comment when it has just been delete by another user
    * [KS-1460] - Should refresh to remove the label "See also" when the only one link to related question is removed after related question was removed
    * [KS-1466] - Use new version cometd in forum application.
    * [KS-1488] - Javascript localization with arguments
    * [KS-1526] - Should allow select categories/forums to export on the time, not export all.
    * [KS-1543] - Init default BBcode via plugin
    * [KS-1545] - Action menu inside a forum: should be done by clicking on line, not only by text
    * [KS-1551] - Allow a normal user to edit its own questions
    * [KS-1552] - Normal user  should not Comment/Vote a pending question
    * [KS-1573] - Show only My tags in topic header
    * [KS-1575] - remove tags column
    * [KS-1581] - "All posts by" links should link to the thread (and not only show the post content)
    * [KS-1683] - expose useAjax in edit mode
    * [KS-1727] - Direct link to RSS Feeds
    * [KS-1729] - Last read post icon after topic title
    * [KS-1737] - Change order of buttons in topic bar
    * [KS-1807] - Don't allow a normal user to approve its own question
    * [KS-1808] - Should allow Admin and Moderator to send pending question
    * [KS-1811] - Normal user should not view  a pending question
    * [KS-1833] - Don't show "Root" category in FAQ application
    * [KS-1838] - Actions of Question contextual menu, should be inlined as icons
    * [KS-1845] - Attachments UI improvements
    * [KS-1846] - Use useAjax in FAQPortlet, add useAjax  in edit mode.
    * [KS-1847] - Make sure all forum data is crawlable
    * [KS-1849] - Do not allow 2 categories with same name
    * [KS-1850] - Don't show alert message when export with no Category
    * [KS-1893] - Confusion when starting new discussion 

** New Feature
    * [KS-71] - Topic subscription UI management
    * [KS-382] - Show tags on thread screen
    * [KS-408] - Simple FAQ list portlet
    * [KS-550] - Auto-prune management UI
    * [KS-599] - Search in attachments
    * [KS-627] - Category Stats pane
    * [KS-1026] - Allow permission management by Category
    * [KS-1118] - Remember UI layout
    * [KS-1124] - RSS Feeds
    * [KS-1132] - Jump to last read post
    * [KS-1133] - New tags management
    * [KS-1134] - Tags suggestions
    * [KS-1169] - Topics types
    * [KS-1250] - Restricted audience on category
    * [KS-1259] - Screen name
    * [KS-1385] - Topic Poll Portlet
    * [KS-1386] - Custom BBCodes : Administration Tab
    * [KS-1476] - Custom BBCodes : Add/Edit BBCode dialog
    * [KS-1530] - Category Scoping
    * [KS-1571] - Portlet preferences for enabled panels
    * [KS-1574] - Untag
    * [KS-1834] - Plugin to load default Data in FAQ


- Version 1.2 RC1

** Bug
    * [KS-1602] - Forum : some French translations for the wordings of the forum
    * [KS-1753] - Forum- THe Button "Go to" (Aller in FR) dosn't work for the first choice ("Acceuil")
    * [KS-1761] - FAQ- Unknown error when edit a question that no longer exists in Manage Questions form
    * [KS-1795] - Bug unknown error when view topic in special case.
    * [KS-1810] - Manage Question form: Show all question existing into pending question list.
    * [KS-1813] - Loosing author when promoting comment as answer
    * [KS-1814] - A normal user can still search a desactivated question
    * [KS-1816] - Add category: Get the number order of category is incorrect after add new question.
    * [KS-1830] - Exception when guest click on Answer portlet the first time with empty database

** Improvement
    * [KS-1807] - Don't allow a normal user to approve its own question
    * [KS-1808] - Should allow Admin and Moderator to send pending question
    * [KS-1811] - Normal user should not view  a pending question

** New Feature
    * [KS-599] - Search in attachments
    * [KS-1834] - Plugin to load default Data in FAQ
