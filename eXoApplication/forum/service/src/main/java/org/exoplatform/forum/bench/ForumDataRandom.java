/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.forum.bench;

import java.util.Random;

/**
 * Created by The eXo Platform SAS
 * Author : Vu Duy Tu
 *          tu.duy@exoplatform.com
 * Aug 10, 2011  
 */
public class ForumDataRandom {
  public static final String[] iconsClass = new String[] { "DefaultPageIcon", "AcrobatFile", "AdminView", "AllTags", "AmericanExpressCard",
      "ApplicationGallery", "ApplicationLink", "ApplicationList", "ApplicationSize", "ApplicationTerminal", "ApplicationWorldWide", "BlueInfo",
      "Bomb", "BookmarkPage", "BreakLink", "Brick", "CategoryList", "Checkout", "CheckPage", "ComponentsList", "Config", "CSS", "DiskDrive",
      "DownloadApplication", "DreamweaverApplication", "DynamicContentPage", "Extension", "Factory", "Feature", "Flash", "FlashFile", "FolderLock",
      "FontFile", "Glove", "Home", "HTML", "Humer", "IconsView", "ImageFrame", "Images", "ImagesFolder", "ImageSize", "ImagesList", "Info",
      "InstallerProgress", "JavaPge", "Layout", "Link", "LockPage", "MasterCard", "Music", "MusicDownload", "New", "PackagesList", "PageCategory",
      "PhotoshopFile", "Printer", "Puzzle", "QuicktimeMovieFile", "Rainbow", "RefeshPage", "RSS", "RSSFeed", "SearchFolder", "Security", "Software",
      "Speaker", "Speaker2", "StarAward", "StarPage", "Stats", "TagPage", "TagSign", "Ticket", "TrueTypeFont", "Tux", "Vector", "Version",
      "VisaCard", "WeatherClouds", "WeatherCloudy", "WeatherLightning", "WeatherRain", "WeatherSnow", "WeatherSun", "World", "XHTML", "YellowStar",
      "BlackDownSolidArrow", "BlackLeftSolidArrow", "BlackRightSolidArrow", "BlackUpSolidArrow", "BlueAquaBallBullet", "BlueArrowIn",
      "BlueArrowInOut", "BlueBallBullet", "BlueBranchArrow", "BlueDividedArrow", "BlueDownArrow", "BlueJoinArrow", "BlueRedoArrow",
      "BlueRotateRightArrow", "BlueSquareBullet", "BlueSwitchArrow", "BlueTriangleBullet", "BlueUndoArrow", "BlueUpArrow", "Check", "Check2",
      "Delete", "Forward", "GrassGreenBallBullet", "GreenBallBullet", "GreenSquareBullet", "GreenTriangleBullet", "GreyAquaBallBullet",
      "GreyBallBullet", "GreyLeftShapeArrow", "GreySquareBullet", "GreyTriangleBullet", "RedAquaBallBullet", "RedBallBullet", "RedSquareBullet",
      "RedTriangleBullet", "Reply", "RightShapeArrow", "YellowAquaBallBullet", "YellowBallBullet", "YellowSquareBullet", "YellowTriangleBullet",
      "AddressBook", "ArticleDocument", "At", "Attachment", "Book", "Chair", "ChartBar", "ChartLine", "CoffeeCup", "CoinsCurency", "Computer",
      "Conference", "Date", "DocumentBox", "Door", "DreamweaverFile", "Email", "Envelope", "ExeFile", "FloppyDisk", "Folder1", "Folder2", "Folder3",
      "Folder4", "Folder5", "HomePage", "HotNews", "IDCard", "IllustratorFile", "IndesignFile", "Index", "Keyboard", "Letter", "MacOSXFolder",
      "Mail", "Map", "Monitor", "Mouse", "Newspaper", "Newspaper2", "NotePage", "OpenBook", "OpenEmail", "OpenFolder", "Page", "PaperList",
      "PastePalate", "Picture", "Purchase", "Script", "SearchPage", "ShoppingBag", "Sitemap", "Stats2", "Tag", "Telephone", "TextFile",
      "YellowFolder", "YellowPen", "ZipFile", "Alarm", "Anchor", "Basketball", "Bell", "Billards", "BlueActionWheel", "BlueFlag", "Box", "BriefCase",
      "Calculator", "Calendar", "Cart", "CD", "Clock", "Component", "CropTool", "DeskPhone", "EmptyBox", "Entertainment", "FileBox",
      "FirefoxBrowser", "FolderExport", "FolderImport", "Football", "Gear", "Golf", "GrayWallet", "GreenFlag", "GreyFlag", "HardDisk", "HourGlass",
      "Key", "Light", "Lock", "MacApplicationTool", "MacMonitor", "Magnifier", "MoveTool", "Movie", "Network", "Paste", "PCMonitor", "Pencil",
      "Raquet", "RedFlag", "Ruler", "Search", "Shield", "Shuttlecock", "Soccer", "Sound", "Switcher", "Tennis", "ToolBoxOpen", "UnLock",
      "VolumeControl", "Wand", "Warning", "Webcam", "Wrench", "YellowBulb", "YellowFlag", "Comment", "Contact", "Eye", "Finger", "FolderHolder",
      "Hand", "HandPoint", "Help", "Personal", "Public", "User", "VIPCard"};
  
  private static int length = iconsClass.length;
  private static Random random = new Random();
  
  public static String getClassIcon() {
    return iconsClass[random.nextInt(length)];
  }
  
}
