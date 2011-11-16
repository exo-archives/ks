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
forum, frequency answer and question, wiki 

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

          
    * Find the latest release notes here : http://wiki.exoplatform.org/xwiki/bin/view/KS/Release+Notes
          
=========
3 INSTALL
=========

Find the latest install guide here : http://wiki.exoplatform.org/xwiki/bin/view/KS/KS+2_0+Quick+Install+Guide

- System Requirements
        Web Browser: IE6, IE7, FF2, FF3 (recommended)
        JVM: version 1.6.0_0 or higher
        Application Server : tomcat-6.0.20 and up 
        Building Tools: Maven 3 and up
        

- Knowledge suite quick start guide
  
    +) tomcat: this is main tomcat server include Knowledge web applications and all dependencies.     
    

Need to set the JAVA_HOME variable for run Knowledge suite's servers.
+) How to start Knowledge sute:
   * First thing first you need to give all script files the executable permission if you are in unix family environment.
   Use command: "chmod +x *.sh" (without quote) to have execute permission on these files.
   
   * NOTE for cygwin's user: the JAVA_HOME must be in MS Windows format like: "C:\Program Files\JDK 1.6"
    Example use: export JAVA_HOME=`cygpath -w "$JAVA_HOME"`; to convert unix like format to MS Windows format.
    Make sure you set JAVA_OPTS="-Xshare:auto -Xms256m -Xmx1024m -XX:MaxPermSize=256M "
   
   
   * Start tomcat server
   
     +) On the Windows platform
       Open a DOS prompt command, go to tomcat/bin and type the command:
        "gatein.bat run" for production
        "gatein-dev.bat run" for development 


     +) On Unix/Linux/cygwin
       Open a terminal, go to tomcat/bin and type the command:
         "./gatein.sh run" for production
         "./gatein-dev.sh run" for development
         

To enable mail notifications, quickly you can use your own email account for example :

# EMail
gatein.email.smtp.username=*youracount@server.com*
gatein.email.smtp.password=*yourpassword*
gatein.email.smtp.host=smtp.gmail.com
gatein.email.smtp.port=465
gatein.email.smtp.starttls.enable=true
gatein.email.smtp.auth=true
gatein.email.smtp.socketFactory.port=465
gatein.email.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory

in this file tomcat/gatein/conf/configuration.properties

* Enter one of the following addresses into your browser address bar in local pc:   
   KS demo portal  
      http://localhost:8080/

You can log into the portal with the following accounts: root, john, mary, demo.
All those accounts have the default password "gtn". 

==============
4 Known issues 
==============
 
 
===========================  
5 Other resources and links
===========================

     Company site        http://www.exoplatform.com
     Community JIRA      http://jira.exoplatform.org/browse/KS
     Community site      http://www.exoplatform.org
     Community forum     http://forums.exoplatform.org
     Community gatein    http://www.jboss.org/gatein/ 
     Developers wiki     http://wiki.exoplatform.org/xwiki/bin/view/KS/
     Blog                http://blog.exoplatform.org/tags/ks/
     Download            http://sourceforge.net/projects/exo-knowledge/
     Source              http://svn.exoplatform.org/projects/ks/tags

====================
6 Detailed Changelog
==================== 

- 2.2.5

** Bug
    * [KS-2405] - Unknown error when dry run in Prune setting in special case
    * [KS-2523] - language mistake in Forum Summary.
    * [KS-3053] - Bad language in poll portlet if no permssion
    * [KS-3209] - Set align for the * in BanIP form
    * [KS-3652] - [Wiki]: Function create page by using link should be updated in special case
    * [KS-3801] - Wiki activities seem to be published on user stream
    * [KS-3802] - [Wiki] Exceptions occur when the session is timeout.
    * [KS-3818] - [Jboss] [Answer] XML Parsing Error and Throw exception when click RSS
    * [KS-3831] - [wiki] Wiki application should be localised
    * [KS-3832] - [Wiki] Apply permission for attachments
    * [KS-3837] - Category in forum portlet display error when restricted audience of this category contain group not exist in gatein system and this group contain '_' .
    * [KS-3841] - Wiki loses spontaneously all modifications after 15 min without saving
    * [KS-3843] - Bad search result for Subscription url field on the lastest forum posts gadget
    * [KS-3846] - [Wiki] Links generated by Children macro are broken
    * [KS-3847] - Answer button is hidden when Manage Question in Answer Portlet
    * [KS-3852] - [Wiki] View changes action always show the latest changes
    * [KS-3853] - [Wiki] Permission is not apply for edit title of page
    * [KS-3858] - Pruning topics don't work
    * [KS-3861] - [Mobile]Forum : Missing content of post, content of topic 
    * [KS-3867] - [Forum] Unknown error when edit user profile from User management form
    * [KS-3876] - [Wiki] Apply page permission for restore function
    * [KS-3887] - [Wiki] Wiki-tree and menu are not displayed without refreshing the cache
    * [KS-3889] - No question appeared  when click manage questions 
    * [KS-3893] - Horizontal scrollbars are needed to display large tables
    * [KS-3897] - Alert message "The target of this link cannot be found" when open Forum from a space
    * [KS-3898] - Knowledge services start although profile is not activated in Platform
    * [KS-3900] - [Wiki] Can open link to attach image on spaces
    * [KS-3901] - {code} does not display properly this code paragraph
    * [KS-3902] - Unable to reply in a topic 
    * [KS-3903] - [Wiki] Can not close user,group, membership selector when setting page permission
    * [KS-3921] - Featured Poll : wrong number of voters when multiple choice is selected

** Feedback
    * [KS-3836] - FAQ should display only entries of the space

** Improvement
    * [KS-3710] - [Forum] User's popup UI Improvement
    * [KS-3712] - [Forum] More actions, search and moderation popup UI Improvement
    * [KS-3731] - [Forum] Support wiki markup
    * [KS-3825] - Externalize some JCR Workspace properties
    * [KS-3835] - should notify my steam of posts and wiki edits

- 2.2.4

** Bug
    * [KS-2414] - Icon of forum is not changed when all topics of it was read
    * [KS-2419] - In Bread cum panel: category name isn't updated after edited
    * [KS-2454] - Answer - Show duplicate user when search user in Email address form
    * [KS-2455] - Still Add related inactivated question
    * [KS-2595] - Post reply is still shown in search result although deleted category which contains this post
    * [KS-2656] - User registering a deleted username can steal previous user questions/answers
    * [KS-2808] - In a forum, when a post have more than one page, if you click on page 2, you are redirect to page 1
    * [KS-2878] - [JCRDataStorage] Could not log forum sort order in forum administration node
    * [KS-2945] - Auto-switch to default language after close [User Setting] form 
    * [KS-2998] - Error UI with Search button in link form. see file attach
    * [KS-3006] - [wiki] Don't show [Download] form when click on image file, *.txt, .. in attachment list
    * [KS-3048] - [KS-Forum] Index of Pending list still is "0" although have posts that pending for approved
    * [KS-3060] - Little error in Manage Category Button ( IE7)
    * [KS-3182] - [Wiki] Error when insert marco with "Error Message" 
    * [KS-3216] - Forum Advanced Search with {Search in = Post} and State kept uncleared
    * [KS-3243] - Error in AddPoll buttion when translated into French
    * [KS-3247] - Show duplicate user's name when search email to send
    * [KS-3251] - [wiki] Must click Add icon twice to add permission for a membership
    * [KS-3253] - [wiki] Missing action "compare with current version" when view a version
    * [KS-3277] - Error when saving the poll gadget on the page
    * [KS-3320] - [KS] User still search topic/post that it has not view right
    * [KS-3331] - [KS-Forum] the number of pages in User Management isn't updated after delete user
    * [KS-3371] - IE7: Don't show attach file link in reply form when open the second times
    * [KS-3373] - Show exception when move forum to category in special case
    * [KS-3374] - Show exception in console when run tomcat 
    * [KS-3427] - [Wiki] Unknown error when click on [View change] link
    * [KS-3490] - [ANSWER][CATEGORY]: Create 2 categorys the same name
    * [KS-3492] - [Wiki] Attach again the file that already attached throw DuplicateNameException
    * [KS-3499] - [ANSWER][CATEGORY]: Category is dupicated.
    * [KS-3500] - [ANSWER][CATEGORY]: Category is dupicated when edit another
    * [KS-3510] - [Forum] Print action on topic doesn't work
    * [KS-3512] - [Forum] Pending list is incorrect some case.
    * [KS-3532] - [Forum] Throw exception and error UI in Forum home when edit a category that deleted by another
    * [KS-3533] - [Wiki]: Unknown error when trying to upload a file doesn't exist
    * [KS-3540] - [Wiki]: Results are different between preview page and view page (after save) when using effect of lists
    * [KS-3541] - [Wiki]: Browse's popup menu is covered by Page Control's components
    * [KS-3544] - [Wiki] [IE7]: Bad UI in Add Template Page
    * [KS-3549] - Value of <Search in> field is false when using Advance search in forum
    * [KS-3640] - [KS-Forum] Error interface
    * [KS-3647] - [Wiki] [FF 3.6]: More menu is covered by WikiBottomArea
    * [KS-3675] - PLF Integrated social with wiki, could not go to wiki page from activity page
    * [KS-3707] - [Forum] Multiple "Spaces" categories are displayed in the forum
    * [KS-3717] - CLONE - FF 3.5: Advanced search icon disappears after switch to French
    * [KS-3725] - [Wiki] Message for Save Permission is unreasonable
    * [KS-3732] - [Forum] Display tag HTML in Split message
    * [KS-3733] - [wiki] page iterator is cover by footer portlet
    * [KS-3736] - [Wiki] Integration problem with the Chatbar
    * [KS-3746] - [wiki] UI to move a wiki page is broken
    * [KS-3749] - [Wiki] Can not save page when edit page by WYSIWYG editor on Safari and Chrome
    * [KS-3750] - Show exception when add wiiki page with content have syntax "[[test]]"
    * [KS-3751] - [Jboss] Throw exception when add page with wiki portlet
    * [KS-3752] - [KS] Forums User management can not search any user
    * [KS-3755] - top of icons of forum toolbar is cut
    * [KS-3764] - Changes in Forum aren't updated to activity stream
    * [KS-3767] - [wiki] Save actions are not correctly displayed when creating a new wiki page
    * [KS-3768] - [wiki] Cannot scroll into the page when only attach one big image
    * [KS-3780] - The posts are displayed with a wrong characters in the activities stream
    * [KS-3787] - [Wiki] Action comment, like doesn't work on wiki activity
    * [KS-3792] - [Wiki] Exception in stack trace when view page which content contains link to page doesn't exist
    * [KS-3794] - Accent are not well encoded in pages' title and content
    * [KS-3795] - Save in WYSIWYG mode does not work
    * [KS-3796] - Firebug, Yslow and Debugbar does not work on PLF 3.5 Beta2 r74739
    * [KS-3798] - [Forum] Do not show information immediately after Add/Edit a BBCode
    * [KS-3804] - Wiki Page permssions do not apply
    * [KS-3805] - Permalink is not well aligned
    * [KS-3812] - Unnecessary bottom line in wiki
    * [KS-3813] - [Forum] Forum activity content is modified when you like/unlike
    * [KS-3816] - [IE7] Very slow loading speed with some pages
    * [KS-3817] - URL in email sent from forum does not contain the hostname : useless
    * [KS-3820] - Auto add user to moderator in forum 
    * [KS-3848] - [Answer] Display errors with category name containing special characters "-", apostroph (')
    * [KS-3849] - Inactive question is still displayed in add related form
    * [KS-3850] - Exception occurs when save related question
    * [KS-3855] - KS : Forum notification has wrong links

** Documentation
    * [KS-3728] - [wiki] Provide an How-to guide for extending macros with additionnal one from external jar/war
    * [KS-3729] - [wiki] Provide an How-to guide for extending actions over a wiki page from external jar/war

** Feedback
    * [KS-3664] - wiki : User selector UI glitches
    * [KS-3777] - Not the same characters text type between the Quick replay and the post
    * [KS-3803] - Confluence Syntax help does not explain links
    * [KS-3806] - Avoid right separator on last toolbar action
    * [KS-3822] - Wiki Image Resize documentation is missing

** Improvement
    * [KS-2912] - [wiki] Add support for Confluence Graphical emoticons
    * [KS-3049] - Change label for Edit Poll form. See file attach
    * [KS-3562] - [Wiki] [Revisions]: Should be disable [Compare Selected] button incase unqualified
    * [KS-3688] - Space Activity Stream tab - tell to which space the message was created for
    * [KS-3713] - [Forum] Gobal colors improvements on the Forum
    * [KS-3756] - Update wiki page activity : View Changes action
    * [KS-3776] - the quick reply text area is too small

- 2.2.3

** Bug
    * [KS-2406] - User still has Moderator right when Admin deleted his right 
    * [KS-2413] - Small error when delete a question language
    * [KS-2416] - Image attachments data of imported data is not displayed
    * [KS-2453] - IE7: Display null when move mouse over Home icon
    * [KS-2565] - Show user name is "null" in answer in special case
    * [KS-2814] - Send question: Show duplicate user name in search result
    * [KS-2859] - Content of message is incorrect when add a censor topic
    * [KS-2960] - IE7: Error UI and lose [Watch], [Bookmark], [RSS] icon in main action bar of category
    * [KS-3009] - Must to double right click on Category name to open menu action
    * [KS-3020] - Reorder the session-config element in ksdemo / web.xml
    * [KS-3021] - ksdemo.properties is missing
    * [KS-3074] - [wiki] Missing search icon
    * [KS-3121] - [Wiki] Missing icon of Page Permission
    * [KS-3122] - [Wiki] Show duplicate username/groupname when add permission for page
    * [KS-3255] - Wiki]IE7: Bulleted is not affected when create new template, add/edit page
    * [KS-3371] - IE7: Don't show attach file link in reply form when open the second times
    * [KS-3374] - Show exception in console when run tomcat 
    * [KS-3440] - Unknown error when search in Answer
    * [KS-3483] - [Wiki] Admin bar's menu is covered by wiki page's components
    * [KS-3503] - [Forum] UI is blocked after edit category setting
    * [KS-3524] - [Wiki]: Category [Others] and Macro [section and column] are disspeared
    * [KS-3536] - [Forum] Displaying Forum jump is unexact when open category from search result
    * [KS-3558] - [Forum] Don't show popup menu when right click on category, forum, topic, post
    * [KS-3559] - [Forum] My Subcriptions tab don't list items watched or subcribed
    * [KS-3631] - [Wiki]: Throw exception when adding Macros [Table of contents]
    * [KS-3671] - [Wiki] Missing resource bundle in Page permission form
    * [KS-3672] - [Answer] Wrong message when delete a question in a specific language
    * [KS-3673] - [Forum] AutoPrune Setting is malfunction
    * [KS-3674] - PLF Integrated social with forum, could not go to created category after created space
    * [KS-3686] - [Forum] [IE7] Apostrophe is converted to [&apos;] when quick reply
    * [KS-3691] - [Poll] Can not edit poll
    * [KS-3700] - [wiki] Comment label is not correctly aligned
    * [KS-3706] - [Forum] Tag popup is not correctly aligned from Chrome
    * [KS-3719] - [Forum] Missing "message field" alert is displayed a second time after fixing it
    * [KS-3722] - [Wiki] Throw exception when click Source Editor incase user doesn't input anything yet
    * [KS-3723] - [Wiki] [IE7] Editing group buttons are displayed wrong position
    * [KS-3727] - [Wiki] Exception was printed to console when save wiki page
    * [KS-3743] - Wiki edits activities are always "wiki content"
    * [KS-3744] - [Forum] Move topic works incorrectly
    * [KS-3745] - [Forum] Can not edit forum
    * [KS-3757] - [Wiki][IE 7] Broken space setting page

** Feedback
    * [KS-3463] - Forum - Error when deleting a form in bookmark form
    * [KS-3464] - Forum - Title displayed incorrectly
    * [KS-3465] - Forum - Error when deleted(or banned) user account
    * [KS-3661] - useless grey border on top of wiki app
    * [KS-3662] - wiki : remove the side tree in edit mode
    * [KS-3663] - wiki : space permissions screen UI KO
    * [KS-3694] - [wiki] Use black color for the wiki editor
    * [KS-3695] - [wiki] Improve the highlight of the selected wiki page in the wiki-tree
    * [KS-3697] - [wiki] Collapse/expand tree panel in edit mode
    * [KS-3699] - [wiki] Default wiki-tree width is too large

** Improvement
    * [KS-3397] - [wiki] Regroup Actions on the editing UI
    * [KS-3577] - [Forum] Improve label for censored posts
    * [KS-3579] - [FAQ] Remove or change icon to view entries in Search results form
    * [KS-3580] - [FAQ] Prefullfill search term in Advance search form
    * [KS-3658] - [Wiki] Should check file size before upload
    * [KS-3659] - UIPopup - check and improv. the UIPopup calls 
    * [KS-3682] - Soft relooking of Forum app to better fit PLF 3.5 skin
    * [KS-3684] - Improve some templates to fit PLF3.5 skin style
    * [KS-3685] - Change the target xsd on xml file
    * [KS-3687] - [wiki] UI for Save actions are not correctly placed when Help is displayed
    * [KS-3703] - [wiki] When navigate in the wiki, the wiki-tree doesn't save its size 
    * [KS-3704] - [Forum] From any space we should not be able to go back to the Home of the forum
    * [KS-3708] - [Forum] View User Profile must display the social user profile page if social is present
    * [KS-3716] - [Forum] Replace dates by timeIntervals 
    * [KS-3737] - Wiki UI improvement to fit with PLF 3.5

- 2.2.2

** Bug
    * [KS-3138] - Error UI in Forum Toolbar portet in IE7
    * [KS-3453] - [Forum] Not consistent check of the topic title length 
    * [KS-3491] -  [Forum]: Permission is set wrong when edit topic
    * [KS-3502] - [Wiki] [FF 3.6]: Show HTML code when uploading a large file
    * [KS-3509] - [Wiki]: Bad UI [Syntax] and [Comment] are displayed at the same line
    * [KS-3513] - [Wiki]: Related pages must be hidden when user have no right to view
    * [KS-3515] - IE7 [KS-FAQ] Lose background and high light on link to category/question when switch to Edit -> View mode
    * [KS-3521] - [Wiki]: User can attach file although without edit permission
    * [KS-3522] - [Wiki]: Throw exception when adding Macros [children] and [pagetree]
    * [KS-3523] - [ANSWER][CATEGORY] Display alert message error when add user into moderate or Restricted audience.
    * [KS-3524] - [Wiki]: Category [Others] and Macro [section and column] are disspeared
    * [KS-3552] - Not quite easy to re-order sub-categories of one category
    * [KS-3560] - [Forum] Pending list of topic need to be approved is wrong
    * [KS-3578] - [Forum] Wrong labels in Forum edit mode
    * [KS-3581] - [FAQ] Wrong button alignment on Watch Manager form
    * [KS-3585] - [Wiki] The bread crumb panel is break down to 2 lines when edit a wiki page
    * [KS-3599] - [KS][Serror] Exception is thrown after JS script is inserted into Search box of Wiki
    * [KS-3601] - [Forum] Show HTML code in message when rating a topic
    * [KS-3603] - [Forum]: Function "Search this forum" doesn't work
    * [KS-3609] - [Wiki]: Can not add new page by using link
    * [KS-3628] - [Answer]: 2 Button [Close], [Print] appeared suddenly
    * [KS-3629] - Remove "private" in all Rest requests
    * [KS-3639] - [Forum][Answer] Could not do anything after close a pop up
    * [KS-3641] - [Forum]: Menu item "Unwatch" isn't displayed after watching a category at Home page by right click
    * [KS-3642] - [Answer]: Alert message when Drag&Drop category
    * [KS-3643] - [Answer]: Get value wrong for User & Email in Manage Watch screen
    * [KS-3644] - [Answer]: Edit portlet is wrong when disable Comments
    * [KS-3645] - [Answer]: A little UI error in Add category form
    * [KS-3646] - [Forum] [Edit BBCode]: Buttons "Save" & "Close" don't work anymore
    * [KS-3648] - [Answer]: At [Print Question] screen, buttons "Close" & "Print" don't work
    * [KS-3656] - Find and fix all JCR Sessions leaks
    * [KS-3657] - Show double message when saving Edit mode in Edit page and after that user can't do anymore

** Improvement
    * [KS-3388] - [wiki] Add icon in wiki actions menu
    * [KS-3459] - [DOM] UIAction style improvement 
    * [KS-3554] - Check compatible of UI new skin changed for UITabContainer, UIHorizontalTabs, UIPopupWindow
    * [KS-3587] - Performances Issues for QACAP
    * [KS-3610] - Forum Data Injector Improvements
    * [KS-3611] - Wiki Data Injector Improvements
    * [KS-3612] - Answer Data Injector Improvements
    * [KS-3625] - Add priority for skin modules
    * [KS-3634] - Forum does not show login form when user first click to privale link perming from forum 

- 2.2.1

** Bug
    * [KS-2830] - [Forum] Bad UI if username is too long
    * [KS-3069] - KS - IE7: Don't show avatar of user after upload successfull
    * [KS-3202] - redundant calls to "Organization Service"  in FAQ
    * [KS-3208] - Buttons hidden when start a topic in the Forum portlet
    * [KS-3250] - [wiki] Full name for permission entries in Permission form
    * [KS-3310] - [wiki] Wrong encoding when upload a file with special characters
    * [KS-3344] - [Wiki] The table is not displayed after insert
    * [KS-3380] - [KS-Forum] Don't switch status from Unwatch -> Watch after Admin remove email watch in watches list
    * [KS-3381] - [Wiki] Page treemacro is not shown when edit by WYSIWYG editor
    * [KS-3432] - [wiki] Broken page toolbar when stress refresh page many times
    * [KS-3444] - [Wiki] Display incorrect format when create page which use efffect of color and align
    * [KS-3467] - [PLF] Property not found when adding the wiki portlet to a page
    * [KS-3482] - [Answer]: Unknow error incase upload attachment files without extension
    * [KS-3516] - Update wiki namespace url
    * [KS-3518] - [Wiki]: Advance Search is wrong when select a space name
    * [KS-3519] - [KS-Wiki] Show search result is incorrect when search a page that renamed
    * [KS-3534] - [Forum] Don't expand and show sub-groups when select role/group in Permission tab
    * [KS-3542] - KS application links on AdminBar don't work
    * [KS-3545] - Can't load some pages : Forum, Answer, Faq, Poll, Wiki....
    * [KS-3584] - [Wiki]: Root account has no highest right 


** Improvement
    * [KS-3077] - Search result display bad ui and should be improved
    * [KS-3229] - [DOM] UITopicContainer optimization
    * [KS-3230] - [DOM] UICategory optimization
    * [KS-3268] - Multitenancy compatibility on KS
    * [KS-3313] - [wiki] Menu of space settings must be more understandable
    * [KS-3386] - [wiki] Replace the Popup with the wiki space settings by a Settings page
    * [KS-3387] - [wiki] provide some template examples
    * [KS-3389] - [wiki] Remove the upload textfield in attachment section
    * [KS-3451] - [Forum] Display "X time ago" in the Latest post column
    * [KS-3452] - [Forum] Display all avatars with the exact same size
    * [KS-3456] - [wiki] Similar UI for revisions and attachements part of a wiki page details
    * [KS-3525] - Create a Category/Forum/Topic/Post and Question/Answer using a name with some special character



- Version 2.2.0
** Bug
    * [KS-2268] - Search question by language is incorrect
    * [KS-2362] - IE7: Error when upload avatar in special case
    * [KS-2456] - Problem with loading form when click select user icon in Permission tab of New topic 
    * [KS-2457] - Still can Add related unapproved question for answer in case unapproved question is not allowed to be viewed
    * [KS-2468] - [Forum] Sort topic by Attachment count is unexact in special case 
    * [KS-2470] - [Post reply] is still visible although this topic is closed  in special case
    * [KS-2473] - need to show alert message when save censor in case censor field is empty
    * [KS-2474] - Check last button of page index in forum and topic
    * [KS-2479] - Display wrong when change order value in add new category
    * [KS-2505] - Show duplicate default data in Forum. See file attach
    * [KS-2506] - Moderator can not move question to a category althoug it have add question right
    * [KS-2533] - Quick search don't run when hit [Enter] key in search field
    * [KS-2575] - Normal user don't see topic after topic is rated by other users
    * [KS-2582] - Displaying content of [FAQ] page is empty after delete category
    * [KS-2584] - Using the eXo OrganizationService on GateIn... 
    * [KS-2585] - Can not open question in search list.
    * [KS-2590] - In user management form: can not search user in other pages except on page 1 
    * [KS-2605] - In First time, must to double click on  "Sort" icon to sort answer in question
    * [KS-2611] - Labels are overlapped (FR)
    * [KS-2615] - Forums statistics are wrong after the import of old messages (zip type)
    * [KS-2634] - [Chrome] Forum attachments are downloaded with wrong names
    * [KS-2638] - Users watching Forum topics are receiving notifications of "Undelivered Mail Returned to Sender" when a user's mail is unavailable
    * [KS-2653] - Edit poll in topic isn't updated
    * [KS-2660] - Error in Jboss packaging message
    * [KS-2663] - initialize application data wrong when adding KS application to Social Space.
    * [KS-2668] - Can not add Poll to topic inside Forum porlet
    * [KS-2670] -  Rated topic don't shown in RSS feed
    * [KS-2671] - Question is still [Open question] although answered in another language 
    * [KS-2672] - Can not view post in a forum of space from activity stream
    * [KS-2673] - ANS: Lack of : for initial permission on added category for space
    * [KS-2680] - Wrong URL generated in Forum
    * [KS-2688] - Typo : "Maximum numer of online users was :...
    * [KS-2714] - on ksdemo can not register new user because can not see the text validation
    * [KS-2716] - Impossible to Reinitialize Avatar (FrenchUI), update tu use confirm code from portal
    * [KS-2721] - After edit poll the score change wrong option, fix for multi vote.
    * [KS-2752] - Missing library when trying to build KS
    * [KS-2753] - In French: Show code error when close "User Setting" pop up in special case
    * [KS-2757] - UI error when view poll gadget in edit mode and view vote result 
    * [KS-2761] - Poll porlet: Selected poll isn't shown although it is public in special case
    * [KS-2762] - Poll Porlet: Selected poll is still show although it is deleted
    * [KS-2768] - [Answers] An inactived and/or disapproved question is always visible in FAQ 
    * [KS-2769] - Interface not one inline in space
    * [KS-2782] - [wiki] Miscellaneous UI bugs of wiki portlet
    * [KS-2799] - content page is empty when view the content of first version of a page which has just one version
    * [KS-2800] - [KS] Show message error when add forum from Moderation menu
    * [KS-2810] - with Banned user, should synchronize right click and action bar 
    * [KS-2811] - Total Topics in Forum Statistic isn't updated after delete category
    * [KS-2813] - Show code error in UI when view category that moved to another position
    * [KS-2823] - Watches form is pushed to bottom when upload avater has size about 400kb
    * [KS-2826] - Error with buttons in Submit Question form
    * [KS-2833] - [wiki] Text is outside the boundary
    * [KS-2837] - [wiki] Lost typed data after close help panel
    * [KS-2843] - Error UI in navigation bar. Detail see file attach
    * [KS-2847] - [wiki] change date format in versions list and compare form
    * [KS-2853] - Poll gadget does not work on ksdemo
    * [KS-2860] - exo.ks.ext.social-integration.jar is missing in ear extension packaging
    * [KS-2862] - Can not approve a censor post from pending list
    * [KS-2863] - [wiki] History's changes display wrong
    * [KS-2866] - [wiki] wiki portlet doesn't work on Platform 3.0
    * [KS-2867] - [wiki] wrong URL format when cancel adding page
    * [KS-2871] - Stacktrace on startup
    * [KS-2873] - [KS-Forum] Error UI when there are some private message in Inbox 
    * [KS-2888] - [wiki] restore version problem when page contains macro
    * [KS-2889] - [wiki] Can't get search result when searching a keyword in attachment
    * [KS-2890] - social integration: update new template of activity
    * [KS-2895] - Permission tab: existing role/group is not listed after select user
    * [KS-2896] - UI error with 3.33333333 percent  of poll vote gadget 
    * [KS-2897] - [wiki] Bug unknown error when upload file contains special character in name
    * [KS-2898] - [PLF] Groovy templates mimetype is "application/x-groovy+html"
    * [KS-2901] - [Poll] Display wrong expired date time
    * [KS-2902] - mask layer has error because of changes from portal.
    * [KS-2906] - Answer portlet in edit mode shows the portlet in view mode
    * [KS-2908] - IE7: Error upload form and can not upload
    * [KS-2914] - [Wiki] UI doesn't change after applying new preferences
    * [KS-2915] - [wiki] Couldn't add new page by link
    * [KS-2918] - [Answer] UI component has errors when visited by IE7. 
    * [KS-2920] - multi-line tip does not display properly
    * [KS-2921] - Show error when add new category in case watching an existing category 
    * [KS-2923] - FF: uploaded avatar isn't displayed after refresh browser (F5)
    * [KS-2927] - [wiki] Couldn't rename Wiki Home
    * [KS-2932] - [wiki] Upload icon disappear on Firefox
    * [KS-2940] - Little error in UIRightPopupMenuContainer 
    * [KS-2944] - [wiki] Missing space name in breadcrumb
    * [KS-2948] - Lose upload icon and can not upload file
    * [KS-2983] - [IE7] Little error with [Censored keywords] item in Administration menu
    * [KS-2987] - [Forum] Censor, approve , active do not work when no check any topic
    * [KS-2988] - [wiki] Couldn't link to other page by WYSIWYG editor
    * [KS-2991] - [IE7] Can not add/edit a page
    * [KS-2992] - [wiki] Users and Groups ApplicationData path aren't exsited
    * [KS-2993] - [Forum] Data is pruned after initialized from Zip
    * [KS-2994] - [wiki] make page content editable by webdav
    * [KS-2996] - [Wiki] Suggestion link leads to wrong location
    * [KS-2997] - [Wiki] A lot of dump wikis are created when create link in page.
    * [KS-3004] - toc macro is not rendered with proper indentation
    * [KS-3005] - [wiki] portlet height is not dynamic
    * [KS-3012] - [Wiki] Show "Page not found" when open a page that contains character "&"
    * [KS-3014] - [Wiki] Error when create page in case page's title contains charater "!,? "
    * [KS-3016] - Polls Portlet action buttons are rendered in an ugly way - when integrated in a web site
    * [KS-3018] - Poll portlet header not translated to English or French - UIPollForm
    * [KS-3019] - Delete panel header "options" in the polls edit mode
    * [KS-3028] - Fail to search a keyword - which is contained in answer entry - using Created between/And criteria
    * [KS-3039] - [Wiki] Page Tree doesn't work in Group wiki
    * [KS-3050] - [Wiki] IE7 ui error in contextual menu and forms
    * [KS-3054] - [wiki]Macro browser is broken
    * [KS-3064] - Don't use UIApplicationLifecycle for all WebUI components
    * [KS-3070] - [wiki] table format is broken after switch from WYSIWYG editor to markup editor
    * [KS-3088] - [IE7] Wiki - Add page - The page is broken
    * [KS-3089] - [IE7] Wiki - fail to save a child page
    * [KS-3090] - [IE7] Wiki - Edit page - UI is broken
    * [KS-3093] - Wiki - Deleting attachment will erase the page title and text content
    * [KS-3094] - [IE7] Wiki - Cannot delete page in IE7
    * [KS-3095] - Wiki - Searching for the keyword 'content' always returns Wiki Home page
    * [KS-3110] - Show exception in console after click create new space
    * [KS-3111] - [KS] Show exceptiopn when add new page on wiki portlet
    * [KS-3120] - [ANswers Portlet] PathNotFoundException when answering a question
    * [KS-3123] - [Wiki] User can see page in the tree event they don't have permission to view
    * [KS-3124] - [Wiki] Nothing happen when click to view page's history
    * [KS-3126] - Problem with French translation in Faq Answer portlet
    * [KS-3127] - The max user is not properly set in forum
    * [KS-3128] - Show exception when used wiki (watch page, add page/edit page, preview page ....)
    * [KS-3132] - Display the first template when choose other template to create new page
    * [KS-3144] - IE7: Error UI and lose [Watch], [Bookmark], [RSS] icon in main action bar of category
    * [KS-3151] - Mini calendar is not translated in French somewhere
    * [KS-3152] -   Values of Language field in Add new question form are not translated in French
    * [KS-3161] - [Wiki] Markup area have border when close help panel
    * [KS-3164] - Error when vote in poll with multi choice
    * [KS-3168] - Forum "Private Message" posts are visible in the global activity stream
    * [KS-3169] - Change icons' label and form's name in Permission page form
    * [KS-3170] - Typed content of all form open is lost when change portal language
    * [KS-3171] - User can add/remove related page when user does not have edit permission on this page
    * [KS-3179] - [wiki] Page exsited message is displayed when add new page from template
    * [KS-3181] - [Wiki] The result is not shown when search by page's name
    * [KS-3186] - [Wiki] After viewing content of attached file, attachment form is not shown when click on attachment(s) link
    * [KS-3188] - Need to click twice to add permission for membership of group for page/space
    * [KS-3189] - The difference when add permission for space and page without set role for user/group
    * [KS-3190] - "Exception" message when change name of page is the same with other pages
    * [KS-3193] - [wiki] Left tree navigation doesn't hide 
    * [KS-3194] - [wiki] Page tree doesn't work if page's name contains "."
    * [KS-3202] - redundant calls to "Organization Service"  in FAQ
    * [KS-3208] - Buttons hidden when start a topic in the Forum portlet
    * [KS-3211] - [Wiki] Exception when access wiki page
    * [KS-3212] - RepositoryException when stopping tomcat PLF 3.5 M2
    * [KS-3215] - [Wiki] Bad UI when select 1 macro
    * [KS-3218] - Forum Bookmarks - the page does not react after deleting a bookmark and closing the popup
    * [KS-3219] - [wiki] Headers are incorrect in TOC macro
    * [KS-3220] - [Wiki] Bad UI in Template form
    * [KS-3222] - [wiki] Scroll WYSIWYG editor when Enter pressed
    * [KS-3223] - [wiki] Content is displayed as attachment in form select attachment
    * [KS-3225] - [wiki] Bug when switch to WYSIWYG editor in case the page contains TOC macros
    * [KS-3232] - [WIki] Some problem with Macro
    * [KS-3236] - [Wiki] Splitter is too wide in PLF
    * [KS-3246] - Exception when select the third category(without any question) to view question 
    * [KS-3248] - [Wiki]NullPointerException when create new page using template without changing title
    * [KS-3249] - [Wiki] Exception when create 1 page with normal user
    * [KS-3252] - Error when view a question with attachment
    * [KS-3259] - [wiki] Cannot access the wiki app with IE7
    * [KS-3261] - Unit tests fail in FAQ service
    * [KS-3262] - [KS-wiki] IE7: Show popup message when create/edit a page
    * [KS-3263] - [KS-wiki] IE7: Page tree is not shown at the first time
    * [KS-3264] - [KS-wiki] Content disappeared after insert 1 macro
    * [KS-3265] - [Wiki] Paragraph - missing edit paragraph icon
    * [KS-3266] - [Wiki] There are 2 Rich Text Editors when adding new page using Rich Text
    * [KS-3272] - Polls are visible for all users
    * [KS-3275] - Unknow error when search page by content
    * [KS-3276] - Exception when view topic
    * [KS-3278] - [KS-wiki] Exception when create new page with normal user
    * [KS-3282] - [wiki] Wiki page content is not shown on stream activities
    * [KS-3284] - [Wiki]Error in template form after select Rich text
    * [KS-3286] - RSS is not hidden at the second time
    * [KS-3306] - Bad presentation in the "Preferences" screen when the language is French
    * [KS-3307] - Confusing UI in Preference/MySuscription screen - not working
    * [KS-3312] - [wiki] Change the title of the root page not refreshed in the wiki explorer
    * [KS-3316] - [wiki] Unknow error when modifying the title of the page with inline editing
    * [KS-3321] - [social-integration] Wiki added pages aren't correctly populated in Social
    * [KS-3343] - Show js message when do any action on KS-forum
    * [KS-3354] - [Forum] NullPointerException when save attachments
    * [KS-3356] - [Wiki] IE7: Jump to Wiki Home page when click on revision link of other page
    * [KS-3357] - [Wiki] Cannot search template to create new page
    * [KS-3363] - [Wiki] Content is disappeared when put TOC macro at the begining of page
    * [KS-3365] - [Wiki] The title of template is shown "null" when preview
    * [KS-3370] - CLONE - First/last name should be displayed in vote rather than user name
    * [KS-3376] - [Answer] Show time of last update in question is incorrect
    * [KS-3377] - [KS-Forum] Show exception in console when lock forum after move this forum
    * [KS-3379] - CLONE - [KS][UI] Problem with style overloading in question comment
    * [KS-3398] - [Wiki-IE7] Show all page's content when only select Edit last paragraph in page
    * [KS-3399] - Don't show some portlet ( banner, breadcumbs) in specical case
    * [KS-3400] - Put [Approve] field is incorrect place in Advanced search form
    * [KS-3403] - [wiki] Link in quick search doesn't work
    * [KS-3404] - [Wiki] Can't save page with title "Untitled"
    * [KS-3405] - SMOKE_PLF_INT_04 Question in subcategory of a space is not displayed
    * [KS-3407] -  Popup Resizing problem
    * [KS-3410] - [Answer] IE7: Bad UI in Comment form 
    * [KS-3411] - [Forum] IE7: Error in BanIPs form
    * [KS-3414] - [FAQ] IE7: Error when edit FQA portlet
    * [KS-3415] - [Wiki] IE7: Error when insert macro
    * [KS-3416] - Poll portlet preference modifications are not taken into account in other user sessions
    * [KS-3420] - Add packages in the lib of exo.ks.extension.ear
    * [KS-3421] - [wiki] Error when upload image in new page
    * [KS-3422] - [wiki] Can't view image from another page
    * [KS-3429] - [Wiki] Show content of Wiki Home page after save new page
    * [KS-3435] - [wiki] Revision area display incorect when change page
    * [KS-3436] - [wiki] Unknow error when view change a page has 1 revision
    * [KS-3438] - Unknown error when open Manage Question form
    * [KS-3439] - RepositoryException when selecting a page in the search box
    * [KS-3443] - [Answer] Display message when edit category
    * [KS-3455] - [wiki] Advanced search does not work 
    * [KS-3534] - [Forum] Don't expand and show sub-groups when select role/group in Permission tab

** Feedback
    * [KS-2571] - Make sure all wiki formats have style
    * [KS-3023] - [wiki]Page tree panel is too wide

** Improvement
    * [KS-1781] - Simplify Moderation tab on forum settings.
    * [KS-2224] - Align look and feel with gatein
    * [KS-2349] - Make notifications of private posts dinstinguishable from notifications of public posts
    * [KS-2608] - Signature field doesn't allow multilines
    * [KS-2667] - Make notifications of private messages.
    * [KS-2678] - Design new application registry icons for portlets
    * [KS-2679] - Hide left panels on Answers in spaces
    * [KS-2684] - Improve code for rated topic 
    * [KS-2726] - In forum, do not use transfer the email address of the creator of a message
    * [KS-2732] - Make notifications of private messages
    * [KS-2744] - improve about private message and post notify 
    * [KS-2746] - Shortent the breadcrumb when there is too much information to display
    * [KS-2747] - Enable editing the title when double click on the text of the title of wiki page
    * [KS-2749] - Only allow two revisions can be selected at a time when selecting versions to compare
    * [KS-2750] - Advanced Search allowed search by title
    * [KS-2751] - Add summary changes when editing wiki page
    * [KS-2764] - Change content of message when don't select any option to vote
    * [KS-2765] - Optimize Move Page tree to use JSON data
    * [KS-2767] - Social Integration : leverage activity plugin improve for ks
    * [KS-2838] - [wiki] Validation for Page's title and attachment
    * [KS-2851] - [wiki] redesign page toolbar of wiki portlet
    * [KS-2852] - improve about [list] bbcode tag.
    * [KS-2877] - [wiki] Remove unneeded function 'reloadWYSIWYGEditor'
    * [KS-2904] - Make Security Domain configurable
    * [KS-2907] - [wiki] Optimize the way of change Wiki mode by link
    * [KS-2910] - http://localhost:8080 should point to the demo portal
    * [KS-2922] - Remaining dependency declaration for deploy 
    * [KS-2934] - [Wiki] shoudn't show Delete and Move icon in Wiki home page
    * [KS-2942] - Should have nicer default poll page
    * [KS-2963] - Display page creation time and page last modified page
    * [KS-2971] - [wiki] Double click to show & hide navigation tree
    * [KS-2999] - [wiki] improve UI Extension code of wiki toolbar and page toolbar
    * [KS-3008] - [WIKI] should have boundary of header or title when edit selection in wiki  
    * [KS-3024] - [wiki] Full page content in watch email
    * [KS-3032] - [wiki] Page tree should show page in current space only
    * [KS-3043] - [wiki] do not show confirmation on cancel when no change has been made
    * [KS-3174] - [wiki] Bad UI with relative pages in default wiki home
    * [KS-3200] - [wiki] Render macro when editting in WYSIWYG editor
    * [KS-3311] - [wiki] Preview must show the title of the Page
    * [KS-3314] - [wiki] Menu is not consistent 
    * [KS-3317] - [wiki] Search textfields are not consistent in all the wiki
    * [KS-3382] - [wiki] Breadcrumb hide the wrong part of the wiki tree
    * [KS-3383] - [wiki] Scroll Management improvements
    * [KS-3385] - [wiki] Select Menu must be consistent in all the wiki
    * [KS-3391] - [wiki] Use same style for table in all the wiki
    * [KS-3392] - [wiki] Change title of the "History Page" by "Page History"
    * [KS-3395] - [wiki] "Revision" link should display content exactly as the "attachment" link
    * [KS-3396] - [wiki] Move the Delete action into the "More" menu
    * [KS-3431] - Navigation API - new GateIn Implementation

** New Feature
    * [KS-515] - Email Address Picker
    * [KS-1673] - Hilight post with URL
    * [KS-1921] - Revamp question list view
    * [KS-2128] - Initialize from export plugin
    * [KS-2310] - Social Contact provider
    * [KS-2740] - Wiki - Search Context Menu
    * [KS-2848] - [wiki] implement "compare with current version" action on view version form
    * [KS-2886] - [wiki] Page Tree panel
    * [KS-2899] - More Confluence macros
    * [KS-2935] - [wiki] Edit Paragraph
    * [KS-2936] - [wiki] Watch Page
    * [KS-2939] - [wiki] Minor Edit
    * [KS-2964] - [wiki] Page Info
    * [KS-2965] - [wiki] Page Templates
    * [KS-2967] - [wiki] Social Integration
    * [KS-3055] - [wiki] Wiki Permissions


- Version 2.2.0-Beta02

** Bug
    * [KS-2473] - need to show alert message when save censor in case censor field is empty
    * [KS-2988] - [wiki] Couldn't link to other page by WYSIWYG editor
    * [KS-2994] - [wiki] make page content editable by webdav
    * [KS-3004] - toc macro is not rendered with proper indentation
    * [KS-3016] - Polls Portlet action buttons are rendered in an ugly way - when integrated in a web site
    * [KS-3018] - Poll portlet header not translated to English or French - UIPollForm
    * [KS-3019] - Delete panel header "options" in the polls edit mode
    * [KS-3054] - [wiki]Macro browser is broken
    * [KS-3070] - [wiki] table format is broken after switch from WYSIWYG editor to markup editor
    * [KS-3088] - [IE7] Wiki - Add page - The page is broken
    * [KS-3090] - [IE7] Wiki - Edit page - UI is broken
    * [KS-3093] - Wiki - Deleting attachment will erase the page title and text content
    * [KS-3094] - [IE7] Wiki - Cannot delete page in IE7
    * [KS-3095] - Wiki - Searching for the keyword 'content' always returns Wiki Home page
    * [KS-3110] - Show exception in console after click create new space
    * [KS-3111] - [KS] Show exceptiopn when add new page on wiki portlet
    * [KS-3126] - Problem with French translation in Faq Answer portlet
    * [KS-3128] - Show exception when used wiki (watch page, add page/edit page, preview page ....)
    * [KS-3144] - IE7: Error UI and lose [Watch], [Bookmark], [RSS] icon in main action bar of category
    * [KS-3151] - Mini calendar is not translated in French somewhere
    * [KS-3152] -   Values of Language field in Add new question form are not translated in French
    * [KS-3158] - Text in French exceeds border.
    * [KS-3161] - [Wiki] Markup area have border when close help panel
    * [KS-3164] - Error when vote in poll with multi choice
    * [KS-3168] - Forum "Private Message" posts are visible in the global activity stream
    * [KS-3171] - User can add/remove related page when user does not have edit permission on this page
    * [KS-3179] - [wiki] Page exsited message is displayed when add new page from template
    * [KS-3193] - [wiki] Left tree navigation doesn't hide 
    * [KS-3202] - redundant calls to "Organization Service"  in FAQ
    * [KS-3208] - Buttons hidden when start a topic in the Forum portlet
    * [KS-3211] - [Wiki] Exception when access wiki page
    * [KS-3219] - [wiki] Headers are incorrect in TOC macro
    * [KS-3222] - [wiki] Scroll WYSIWYG editor when Enter pressed
    * [KS-3223] - [wiki] Content is displayed as attachment in form select attachment
    * [KS-3225] - [wiki] Bug when switch to WYSIWYG editor in case the page contains TOC macros
    * [KS-3236] - [Wiki] Splitter is too wide in PLF

** Feedback
    * [KS-2571] - Make sure all wiki formats have style

** Improvement
    * [KS-2679] - Hide left panels on Answers in spaces
    * [KS-2999] - [wiki] improve UI Extension code of wiki toolbar and page toolbar
    * [KS-3008] - [WIKI] should have boundary of header or title when edit selection in wiki  
    * [KS-3043] - [wiki] do not show confirmation on cancel when no change has been made
    * [KS-3174] - [wiki] Bad UI with relative pages in default wiki home
    * [KS-3200] - [wiki] Render macro when editting in WYSIWYG editor

- Version 2.2.0-Beta01

Release Notes - eXo Knowledge - Version ks-2.2-beta01

** Bug
    * [KS-2833] - [wiki] Text is outside the boundary
    * [KS-2889] - [wiki] Can't get search result when searching a keyword in attachment
    * [KS-2920] - multi-line tip does not display properly
    * [KS-2926] - Exception when start tomcat server , update forum job
    * [KS-2944] - [wiki] Missing space name in breadcrumb
    * [KS-2992] - [wiki] Users and Groups ApplicationData path aren't exsited
    * [KS-2996] - [Wiki] Suggestion link leads to wrong location
    * [KS-2997] - [Wiki] A lot of dump wikis are created when create link in page.
    * [KS-3005] - [wiki] portlet height is not dynamic
    * [KS-3014] - [Wiki] Error when create page in case page's title contains charater "!,? "
    * [KS-3044] - [wiki] NPE in wiki tree in space
    * [KS-3064] - Don't use UIApplicationLifecycle for all WebUI components

** Feedback
    * [KS-3023] - [wiki]Page tree panel is too wide

** Improvement
    * [KS-2746] - Shortent the breadcrumb when there is too much information to display
    * [KS-2934] - [Wiki] shoudn't show Delete and Move icon in Wiki home page
    * [KS-2963] - Display page creation time and page last modified page
    * [KS-2971] - [wiki] Double click to show & hide navigation tree
    * [KS-3032] - [wiki] Page tree should show page in current space only
    * [KS-3056] - Forum's getmessage REST API should return post url instead of topic url

** New Feature
    * [KS-2624] - [wiki] common macros : toc, note, tip, section, colunm, noformat, panel
    * [KS-2886] - [wiki] Page Tree panel
    * [KS-2899] - More Confluence macros
    * [KS-2935] - [wiki] Edit Paragraph
    * [KS-2936] - [wiki] Watch Page
    * [KS-2939] - [wiki] Minor Edit
    * [KS-2964] - [wiki] Page Info
    * [KS-2965] - [wiki] Page Templates
    * [KS-2966] - [wiki] Related Pages
    * [KS-2967] - [wiki] Social Integration

- Version 2.1.1 

** Bug
    * [KS-2685] - After edit poll the score change wrong option, fix for single vote.
    * [KS-2703] - Minor typos in the JCRDataStorage.populateUserProfile war message
    * [KS-2714] - on ksdemo can not register new user because can not see the text validation
    * [KS-2721] - After edit poll the score change wrong option, fix for multi vote.
    * [KS-2734] -  [KS-Forum] Total topic/post in Forum statistics isn't updated after import category/forum
    * [KS-2735] - [KS] Can not move topic to forum that auto-added from Space
    * [KS-2736] - [KS-Answers] Nothing happen when click on [Watches] icon in User setting
    * [KS-2753] - In French: Show code error when close "User Setting" pop up in special case
    * [KS-2755] - Cluster configuration is wrong for Knowledge workspace
    * [KS-2792] - CLONE -FAQ: date in English format (ks 2.1.x)
	* [KS-2794] - Register an account, text validation is not display

** Improvement
    * [KS-2496] - Answers - Possible to configuration  create a question at the root or not
    * [KS-2607] - Deny users from submitting questions in the root category
    * [KS-2697] - Social Integration : leverage activity plugin

-Version 2.1.0 GA

** Bug
    * [KS-2259] - javascript errors on first drop of Forum app
    * [KS-2411] - Error when open form to edit information of watcher
    * [KS-2529] - Cannot select a role
    * [KS-2530] - erroneous warning while going to a restricted category via breadcrumb, despite user is allowed
    * [KS-2537] - Normal user can not search topics in Forum default after move topic in special case
    * [KS-2553] - user registering a past username can steal previous user profile
    * [KS-2554] - User is still view last read post although without view permission.
    * [KS-2556] - "< font" gets inserted in tag autosuggest
    * [KS-2559] - KS, Answer: Do not list Open/Pending question when click on the link
    * [KS-2560] - Error when delete topic after moved
    * [KS-2561] - Forum: User management: error when search user at the second page
    * [KS-2563] - Show message "The target of this link could not be found. The item may have been moved or deleted" when discuss question
    * [KS-2573] - Category/Forum/Topic is still watched although removed all emails  in watches list 
    * [KS-2575] - Normal user don't see topic after topic is rated by other users
    * [KS-2577] - Save moderators in category is not update to forums
    * [KS-2585] - Can not open question in search list.
    * [KS-2587] - Can not open category, forum and topic by permlink when setup host name same selectedNode of navigation.
    * [KS-2590] - In user management form: can not search user in other pages except on page 1 
    * [KS-2600] - Problem for auto prune topic.
    * [KS-2603] - Answer portlet error when view 
    * [KS-2610] - KS Memory Leak caused by unclosed JCR sessions
    * [KS-2615] - Forums statistics are wrong after the import of old messages (zip type)
    * [KS-2632] - Bug unknown error when discuss question
    * [KS-2633] - Bug in Platform when using social's component
    * [KS-2651] - FAQ can not get viewer template and can not how FAQ portlet
    * [KS-2653] - Edit poll in topic isn't updated
    * [KS-2661] - Can not load fck editor 
    * [KS-2662] - Post counter doesn't work after set permissions for forum
    * [KS-2663] - initialize application data wrong when adding KS application to Social Space.
    * [KS-2668] - Can not add Poll to topic inside Forum porlet
    * [KS-2670] -  Rated topic don't shown in RSS feed
    * [KS-2673] - ANS: Lack of : for initial permission on added category for space
    * [KS-2680] - Wrong URL generated in Forum
    * [KS-2687] - Watch email does not show in watches list of topic
    * [KS-2688] - Typo : "Maximum numer of online users was :...
    * [KS-2696] - remove borders on Forum and Answers portlets
    * [KS-2700] - Total answers in question is increased after edit an answer or discuss this question
    * [KS-2706] - Exception when create a new space
    * [KS-2718] - IE6 UI problems

** Documentation
    * [KS-2698] - Reference Guide

** Improvement
    * [KS-2258] - Jump into category/forum after creation
    * [KS-2538] - display "Last Posts" according to current user
    * [KS-2695] - Optimize code in Forum and Answer Listeners in social integration package

** New Feature
    * [KS-602] - create an opensocial gadget
    * [KS-1042] - Syntax coloring for code
    * [KS-1193] - Polling Management
    * [KS-1731] - Default User preferences
    * [KS-1921] - Revamp question list view
    * [KS-1925] - Separate Polls from Forum
    * [KS-2592] - Forum Integration to Spaces
    * [KS-2593] - Answers Integration to Spaces

** Task
    * [KS-2378] - NPE stacktrace at startup
    * [KS-2566] - Multi-tenant compliant repository access
    * [KS-2578] - move all components declared in jars in extension
    * [KS-2583] - Upgrade to Gatein 3.1
    * [KS-2591] - Add plugin to initialize default data when space in social created 
    * [KS-2606] - Improment code for code commited by issue: KS-2258
    * [KS-2652] - update commons version 
    * [KS-2665] - CLONE -Organize css following the introduction of gatein 3.1
    * [KS-2723] - English mistake in activity stream when posting in forum
    
 
- Version 2.1.0-CR03

** Bug
    * [KS-2609] - English label error : "Not moderator"
** Task
    * [KS-2708] - Server console log when add new space in platform
    * [KS-2710] - Remove extension config jar from the ear
    * [KS-2711] - Remove jboss-web.xml from extension war
    * [KS-2713] - Do not create .tar.gz packages
    * [KS-2719] - Release Knowledge 2.1.0-CR03   
    
- Version 2.1.0-CR02
** Bug
    * [KS-2573] - Category/Forum/Topic is still watched although removed all emails  in watches list 
    * [KS-2590] - In user management form: can not search user in other pages except on page 1 
    * [KS-2662] - Post counter doesn't work after set permissions for forum
    * [KS-2670] -  Rated topic don't shown in RSS feed
    * [KS-2688] - Typo : "Maximum numer of online users was :...
    * [KS-2692] - KS not compatible with Java5

** Task
    * [KS-2517] - Temp folder in test phase has been created, when build product  
    * [KS-2702] - Release KS 2.1.0-CR02



- Version 2.1.0-CR01

** Bug
    * [KS-2575] - Normal user don't see topic after topic is rated by other users
    * [KS-2585] - Can not open question in search list.
    * [KS-2653] - Edit poll in topic isn't updated
    * [KS-2661] - Can not load fck editor 
    * [KS-2663] - initialize application data wrong when adding KS application to Social Space.
    * [KS-2668] - Can not add Poll to topic inside Forum porlet
    * [KS-2672] - Can not view post in a forum of space from activity stream
    * [KS-2673] - ANS: Lack of : for initial permission on added category for space
    * [KS-2680] - Wrong URL generated in Forum

** Task
    * [KS-2652] - update commons version 
    * [KS-2677] - base structure for eXo Knowledge Reference Guide
    * [KS-2690] - Release KS 2.1.0-CR01

- Version 2.0.0


** Bug
    * [KS-701] - Post with special format can ruin the font style of the forum 
    * [KS-1880] - Backport Translation and formatting patch
    * [KS-1933] - [BFPME-475] i18n labels
    * [KS-2096] - deletion of a user isn't fully propagated throughout the system
    * [KS-2195] - RSS from forum and faq generate is not true.
    * [KS-2259] - javascript errors on first drop of Forum app
    * [KS-2277] - In IE 7: Border is broken in Notification form. see file attach
    * [KS-2311] - Tomcat KS 2.0 Beta01 doesn't start on linux
    * [KS-2317] - IE 7: Forum's description pane disappears after move scroll bar of browser to bottom.
    * [KS-2361] - Error  when import "Root" category
    * [KS-2386] - Don't show first topic of Forum when get RSS of Category
    * [KS-2395] - deleted topic is restored 
    * [KS-2401] - Answers portlet stays in french when I change language to English
    * [KS-2402] - FAQ - Still possible to create a question at the root
    * [KS-2424] - FAQ- Administrator VS moderator
    * [KS-2437] - Remove the use of exoservice gmail Account from MPailService configuration
    * [KS-2441] - Don't  show topic/post when get RSS of category&Forum
    * [KS-2442] - Don't show question when get RSS of root category
    * [KS-2443] - Still can rate for topic although topic rated
    * [KS-2458] - [Post reply] button is still visible although this user does not have permission to reply topic
    * [KS-2459] -  There is no way to back to Forum home after search into a specific topic
    * [KS-2460] - Can not edit topic with limitation of viewers
    * [KS-2461] - Answer - Still show unapprove question when user is owner of this question
    * [KS-2463] - Don't show date/time when check number of Most Users online
    * [KS-2465] - Show message error JS  when click add category after removed user "demo"
    * [KS-2466] - Answer - Can not sort category/question by created date/alphabet
    * [KS-2467] - Always show total active members is 1 although there are some user are active in forum on different browsers
    * [KS-2469] -  User is still able to move the topic to the forum which this user does not have right to add topic
    * [KS-2471] -  User is still able to move topic to closed and locked forum which user is not the moderator of the destination forum
    * [KS-2472] - User is still able to move topic to the Forum which belongs to the Category that user has been restricted view permission
    * [KS-2475] - Can't search by Answer
    * [KS-2478] - Category's moderator don't see the approve and actvicate options when editing a question in this category
    * [KS-2502] -  Displaying Total topic/post is "0" although there are some posts
    * [KS-2522] - Forum: some translation in French
    * [KS-2528] - French translation encoding is F**ed
    * [KS-2532] - [Answer] show error when get RSS of a category that without sub-categories
    * [KS-2535] - User is still able view post although without view permission in special case

** Feedback
    * [KS-2510] - Create topic is too slow
    * [KS-2525] - "Email notify" prefixes email notifications

** Improvement
    * [KS-1242] - Create Shorter Permlinks
    * [KS-1659] - quotes in preview pane are not displayed
    * [KS-1694] - Variables for notification subject
    * [KS-1728] - Click on user name to display menu
    * [KS-1882] - Translate label (on Forum)
    * [KS-1885] - Change the message related to the Moderation (on Forum)
    * [KS-1890] - Correcting esthetical, spelling and translating some words
    * [KS-2005] - Display  sort order 
    * [KS-2346] - Allow to display question details in FAQ portlet
    * [KS-2527] - Anyone can answer by default

** New Feature
    * [KS-1956] - Configurable JCR data  location 
    * [KS-2218] - New Administration Menu

** Task
    * [KS-2398] - Fix missing dependencies
    * [KS-2524] - remove source button in FCK Editor
    * [KS-2526] - Polish demo homepage
    * [KS-2551] - Use the new logos
    * [KS-2552] - remove redhat mention in footer

- Version 2.0.0-CR2

** Bug
    * [KS-2195] - RSS from forum and faq generate is not true.
    * [KS-2304] - Category is appear again after deleted
    * [KS-2312] - JBoss KS 2.0 Beta01 doesn't start on linux
    * [KS-2371] - Checking session manager problem
    * [KS-2381] - Can't add a poll
    * [KS-2383] - Don't show second question of category  in RSS's content
    * [KS-2389] - Don't show object (category/forum/topic) which got RSS feed in My subscription tab
    * [KS-2393] - In /ksdemo: default data isn't displayed in both Forum & Answer
    * [KS-2394] - sometimes can not add category/forum/topic after do some actions in Forum
    * [KS-2396] - Can not add new Category after deleting Category by the main menu on the action bar
    * [KS-2401] - Answers portlet stays in french when I change language to English
    * [KS-2402] - FAQ - Still possible to create a question at the root
    * [KS-2404] - Some functions are not executed after add category with special character for Restricted Audience field
    * [KS-2410] - Edit question is invalid in special case
    * [KS-2421] - FAQ- Problem of moderate questions 

** Task
    * [KS-1936] - Manage e.printStacktrace() and replace by logging system for Forum and FAQ
    * [KS-2378] - NPE stacktrace at startup


- Version 2.0.0-CR1
  http://jira.exoplatform.org/secure/ReleaseNote.jspa?version=11378&styleName=Text&projectId=10150&Create=Create
** Bug
    * [KS-2203] - Show code error in Rss feed content of forum 
    * [KS-2212] - Don't show questions when get RSS of Answer porlet
    * [KS-2217] - Some bugs with Setting and active prune
    * [KS-2240] - Alert message is shown below Add tag textbox. see file attach
    * [KS-2242] - Show pending index is incorrect in special case. see file attach
    * [KS-2266] - Remove text which unavailable in alert message. see file attach
    * [KS-2269] - Show message "This file has been lost" although this file is existing in attachment
    * [KS-2274] - Split topic pop up is closed after alert message appears
    * [KS-2276] - Category is still displayed although deleted  in special case.
    * [KS-2278] - IE 7: Remove scroll bar at right & bottom of Topic type form
    * [KS-2280] - IE7: Don't keep ticking on check box after next/previous page.
    * [KS-2298] - IE7: List  tag is empty when there are 2 pages post in topic
    * [KS-2299] - Added poll in topic is still displayed although without view topic right.
    * [KS-2315] - IE7: Upload icon isn't displayed. See file attach
    * [KS-2318] - IE7: Error JS when add post reply in special case
    * [KS-2319] - IE7: show message error when setting forum in a special case
    * [KS-2320] - Bug unknown error when view topic in special case 
    * [KS-2334] - Edit mode: Forum is still unchecked although it's showing in special case
    * [KS-2337] - watch category: Email is still listed in watches form although unwatched 
    * [KS-2339] - Attached file in question is lost in special case
    * [KS-2340] - Relate question disappears after move it to a destination category
    * [KS-2345] - Have problem with add post reply in default topic
    * [KS-2347] - Cannot add a new forum
    * [KS-2352] - Error when view topic and throw exception

** Improvement
    * [KS-2309] - In category drop down list: should focus to category which opening.
    * [KS-2338] - Need to change from warning message to info message

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
