==============================================
    Release Notes - exo-ks - Version 1.2.3
==============================================

===============
1 Introduction
===============
eXo Knowledge Suite (KS) is a suite of collaborative applications oriented to knowledge management. It not only holds on-line discussions and posts user generated content but also answers the most frequently asked questions about sites/services.

KS is made up of three applications:

    * Forum : A full featured message boards application that holds on-line discussions and posts user generated content.
    * Answers : A questions/answers application to help finding solutions
    * FAQ : An Answers-based application to publish questions/answers in public FAQs


=============
2 What's new?
=============

    * General
          o Upgraded to Portal 2.5.8 and dependencies 
          o Fix bugs
    * Forum
          o Resize discussion window 
    * Answers
          o  
    * FAQ
          o Switch language 

=========
3 INSTALL
=========

Follow the KS Quick Install Guide : http://wiki.exoplatform.org/xwiki/bin/view/KS/KS+Quick+Install+Guide

Requirements

    * OS : Windows, Linux or MAC OS.
    * Memory : 1GB of RAM and at least one 1.5 Ghz processor.
    * Disk : 150MB of disk space is necessary for the install
    * Browser : Firefox 3+ or Internet Explorer 7+
    * eXo KS requires a Java 5 environment, make sure the JAVA_HOME environment variable point to a JRE 5 folder
    * the exo server will run on portal 8080, make sure this port is not currently in use

Launch instructions :

    * Using command line, go to $TOMCAT_HOME/bin and start the server :
          o Windows :

            eXo.bat run

          o Linux / Mac OS :

            chmod+x *.sh ; eXo.sh run

    * Wait for the server to start. You should see something like this on the console :

INFO: Server startup in 15359 ms

    * eXo KS is now ready to use. Point your browser to http://localhost:8080/portal



===========
4 RESOURCES
===========

     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org
     Community site      http://www.exoplatform.org
     Developers wiki     http://wiki.exoplatform.org


===========
5 CHANGELOG
===========
- Version 1.2.3

** Bug
    * [KS-1971] - the forum service doesn't take into account user profile modifications 
    * [KS-2077] - Deleted forum message should not appear in "last posts" column
    * [KS-2219] - Cannot open the forums created by the random initializer
    * [KS-2220] - Scoping of Edit Mode tab does not work well in special cases
    * [KS-2227] - Forum : "ban IP" style issue (152087)
    * [KS-2239] - Forum AuthenticationLogin/Logout listeners generate too much JCR operations/ Many cl*.tmp files generated
    * [KS-2245] - Forum : poll expiration not well localized
    * [KS-2285] - Can not click any action in Forum in special case
    * [KS-2289] - Problem with creating default data in ks-plugins-configuration.xml
    * [KS-2305] - French label problem on forum
    * [KS-2347] - Cannot add a new forum


** Improvement
    * [KS-1975] - FAQ - Language switch
    * [KS-2206] - Build, quality and automation improvements
    * [KS-2244] - Forum : the discussion windows must be resized 

** Task
    * [KS-2238] - Forum :Spelling - please correct the expression
    * [KS-2246] - FAQ : incorrect label submit new question dialog


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
    * [KS-2031] - Redirection to classic portal is hard-coded
    * [KS-2034] - Signature could not be saved in Settings
    * [KS-2035] - Add watch function is not updated with new email address of root
    * [KS-2112] - Unknown error when Preview a quick post
    * [KS-2113] - Can NOT create topic in Forum
    * [KS-2114] - Small Icon display error in Posts by User form
    * [KS-2117] - Answers - Invalid title in Search Result forum
    * [KS-2118] - Answers: RSS Feed is not ok with created category and question
    * [KS-2119] - Invalid display of some item is Answer popup menu
    * [KS-2120] - Invalid RSS Feed
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
    * [KS-2003] - Add icon for "Untag" item in menu. See file attach

** Task
    * [KS-1734] - Avoid expensive calls to getAll()
    * [KS-1895] - Do not use ajax by default
    * [KS-1947] - Version on 1.2.x should contain-SNAPSHOT
    * [KS-2126] - Upgrade KS branch 1.2.2-SNAPSHOT to use portal 2.5.7-SNAPSHOT
    * [KS-2131] - Cleanup the build process to be able to deploy on eXo Nexus with the release plugin for 1.2.x
    * [KS-2156] - Release KS 1.2.2


** Sub-task
    * [KS-1965] - [DEV] Fix call getAll() when searching user profiles
    * [KS-1998] - Fix bug when import forum but the data is data of category , forum still allow import.
    * [KS-2132] - Build - Cleanup the profile with properties, remove the reporting and emma config, add parent pom v6
    * [KS-2134] - eXoApplication\blog\service\ is not compiled in 1.2.x -- should we keep that module ?
    * [KS-2135] - eXoApplication\wiki\service\ is not compiled  in 1.2.x -- should we keep that module ?
    * [KS-2137] - Build - Integrate module.js in the project to be used by exopackage and maven-exobuild-plugin
    * [KS-2144] - Use Kernel, Core, JCR, PC, Portal, WebOS SNAPSHOTs
    * [KS-2151] - Can not show attached image & exception in console when view a question



- Version 1.2 Final
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
    * [KS-1355] - IE7: Remove horizontal bar on some forms
    * [KS-1358] - Rename the node type exo:userProfile to avoid conflict with the one defined into the JCR
    * [KS-1359] - Can't view quote in preview form
    * [KS-1360] - Add topic with post notification and then edit the topic, the check bos post notification is not ticked
    * [KS-1362] - Should check both "post reply" and "Message" in case having censor word
    * [KS-1363] - UI bug in preview private post form
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
    * [KS-1763] - Can not add multiple language for question when edit.
    * [KS-1766] - In IE 7: Error UI in Poll porlet. see file attach
    * [KS-1770] - Imported Category & Question aren't displayed in special case.
    * [KS-1771] - Watched email is still displayed in Manager watch form although deleted.
    * [KS-1772] - Show message "This faq category no longer exists" when edit Root category
    * [KS-1773] - [KS-FAQ] UI error in the Search Result form
    * [KS-1777] - Forum - Advance search - "Clear fields" button doesn't work in webos
    * [KS-1792] - JCR session leaks
    * [KS-1795] - Bug unknown error when view topic in special case.
    * [KS-1810] - Manage Question form: Show all question existing into pending question list.
    * [KS-1812] - Category isn't viewed property although selected to open from Watch manager form.
    * [KS-1813] - Loosing author when promoting comment as answer
    * [KS-1814] - A normal user can still search a de-activated  question
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

