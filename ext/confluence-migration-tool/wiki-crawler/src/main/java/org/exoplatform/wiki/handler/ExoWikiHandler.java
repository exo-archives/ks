/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
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
 */
package org.exoplatform.wiki.handler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.jackrabbit.webdav.client.methods.DavMethod;
import org.apache.jackrabbit.webdav.client.methods.MkColMethod;
import org.apache.jackrabbit.webdav.client.methods.PutMethod;
import org.exoplatform.wiki.IWikiHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to interact with an Exo RestService represented by wikiloader.groovy
 * file (deployed one the server side)
 */
public class ExoWikiHandler implements IWikiHandler {

  public static final String  CHAR_TO_REPLACE  = "*'\"+?&";

  private static final Logger LOGGER           = LoggerFactory.getLogger(ExoWikiHandler.class.toString());

  private static final String DEFAULT_ENCODING = "UTF-8";

  private DefaultHttpClient   httpClient;

  private HttpClient          davClient;

  private String              targetHost;

  public ExoWikiHandler(String targetHost) {
    this.targetHost = targetHost;
  }

  public void start(String targetUser, String targetPwd) {
    httpClient = new DefaultHttpClient();
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials(targetUser, targetPwd);
    httpClient.getCredentialsProvider().setCredentials(AuthScope.ANY, creds);

    davClient = new HttpClient();
    davClient.getState().setCredentials(org.apache.commons.httpclient.auth.AuthScope.ANY,
                                        new org.apache.commons.httpclient.UsernamePasswordCredentials(targetUser, targetPwd));
  }

  public void stop() {
    httpClient.getConnectionManager().shutdown();
    davClient.getHttpConnectionManager().closeIdleConnections(1000);
  }

  /**
   * Create a new Page, returns the new name, or null if already exists.
   * 
   * @param path where the page have to be created
   * @param pageName of the page to create
   * @return created or existing page name
   */
  public String createPage(String path, String pageName, boolean hasChildren, String syntax) {
    String createdPageName = null;
    try {
      String normalizedPageTitle = normalizePageName(pageName, false);
      String normalizePageName = normalizePageName(pageName, true);
      int statusCode = getHttpStatusOfPageOnTarget(path, normalizePageName);
      if (statusCode == 404) {

        // LOGGER.info(String.format("[Create] %s/%s", path, pageName));
        HttpGet httpGet = new HttpGet(targetHost + "/rest/wikiloader/create?path=" + URLEncoder.encode(path, DEFAULT_ENCODING) + "&name="
            + URLEncoder.encode(normalizedPageTitle, DEFAULT_ENCODING) + "&syntax=" + syntax);
        // String uri = httpGet.getURI().toString();
        HttpResponse responseCreate = httpClient.execute(httpGet);
        int statusCodeRC1 = responseCreate.getStatusLine().getStatusCode();

        if (statusCodeRC1 == 200) {
          createdPageName = IOUtils.toString(responseCreate.getEntity().getContent());
          if (createdPageName.startsWith("%%")) {
            LOGGER.error(String.format("[Create] Failed to create [Err = %s] - Cause: %s", "" + statusCodeRC1, createdPageName));
            return null;
          }
        } else {
          String message = "";
          if (responseCreate != null && responseCreate.getEntity() != null) {
            message = IOUtils.toString(responseCreate.getEntity().getContent());
          }
          LOGGER.error(String.format("[Create] Failed to create [Err = %i] - Cause: %s", statusCodeRC1, message));
          return null;
        }

        HttpGet httpCheckCreated = new HttpGet(targetHost + "/rest/private/wikiloader/check?path="
            + URLEncoder.encode(path + "/" + createdPageName, DEFAULT_ENCODING));
        HttpResponse responseCheck2 = httpClient.execute(httpCheckCreated);
        int statusCodeRC2 = responseCheck2.getStatusLine().getStatusCode();
        responseCheck2.getEntity().consumeContent();
        if (statusCodeRC2 != 200) {
          LOGGER.info(String.format("[Create] Created page %s/%s not found.", path, pageName));
          return pageName;
        }
        return createdPageName;
      } else if (statusCode == 200) {
        LOGGER.warn(String.format("[Create] Page already created"));
        return null;
      }
    } catch (Exception exception) {
      LOGGER.error(String.format("[Create] Error creating page. Err = ", exception.getMessage()));
    }
    return null;
  }

  public int getHttpStatusOfPageOnTarget(String path, String pageName) throws IOException {
    HttpGet httpCheck = new HttpGet(targetHost + "/rest/wikiloader/check?path=" + URLEncoder.encode(path + "/" + pageName, DEFAULT_ENCODING));
    // String uri = httpCheck.getURI().toString();
    HttpResponse responseCheck = httpClient.execute(httpCheck);
    int statusCode = responseCheck.getStatusLine().getStatusCode();
    responseCheck.getEntity().consumeContent();
    return statusCode;
  }

  public boolean transferContent(String content, String path) {
    try {
      HttpPost httppost = new HttpPost(targetHost + "/rest/private/wikiloader/content");
      List<NameValuePair> formparams = new ArrayList<NameValuePair>();
      formparams.add(new BasicNameValuePair("path", path));
      formparams.add(new BasicNameValuePair("content", content));
      UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, DEFAULT_ENCODING);
      httppost.setEntity(entity);
      HttpResponse response = httpClient.execute(httppost);
      response.getEntity().consumeContent();
    } catch (IOException exception) {
      LOGGER.error("[Transfert] Error uploading content to : " + path);
    }
    return true;
  }

  public boolean checkPageExists(String path) {
    try {
      HttpGet httpGet = new HttpGet(targetHost + "/rest/private/wikiloader/pageurl?path=" + URLEncoder.encode(path, DEFAULT_ENCODING));
      HttpResponse responseGet = httpClient.execute(httpGet);
      responseGet.getEntity().consumeContent();
      return responseGet.getStatusLine().getStatusCode() == 200;
    } catch (IOException exception) {
      LOGGER.error("[URL] Error getting Url for : " + path);
    }
    return false;
  }

  public boolean uploadAttachment(String targetSpace, String pageName, String attachmentName, String contentType, InputStream stream) {
    try {
      httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
      HttpPost post = new HttpPost(targetHost + "/rest/wikiloader/upload/" + targetSpace + "/" + normalizePageName(pageName, true) + "/");
      MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
      multipartEntity.addPart("File", new InputStreamBody(stream, contentType, normalizePageName(attachmentName, true)));
      post.setEntity(multipartEntity);

      HttpResponse response = httpClient.execute(post);
      String responsePhrase = IOUtils.toString(response.getEntity().getContent());

      int statusCode = response.getStatusLine().getStatusCode();
      LOGGER.info("[Upload] attachment : " + statusCode + " - " + responsePhrase);
      if (statusCode == 200) {
        // TODO Check attachment exists
        return !responsePhrase.startsWith("%%");
      } else {
        LOGGER.error("[Upload] attachment fail : " + statusCode + " - " + responsePhrase);
      }
    } catch (IOException exception) {
      LOGGER.error("[Upload] attachment fail");
    }
    return false;
  }

  public boolean checkAttachmentExists(String targetSpace, String pageName, String attachmentName) {

    try {
      String normalizedPageName = normalizePageName(pageName, true);
      String normalizedAttachmentName = normalizePageName(attachmentName, true);
      String path = targetSpace + "/" + normalizedPageName + "/";
      HttpGet httpGet = new HttpGet(targetHost + "/rest/wikiloader/attachments/" + path);
      HttpResponse responseGet = httpClient.execute(httpGet);
      String response = IOUtils.toString(responseGet.getEntity().getContent());
      String[] attachments = response.split("\\|");

      for (String att : attachments) {
        if (att.equals(normalizedAttachmentName)) {
          return true;
        }
      }
    } catch (IOException exception) {
      LOGGER.error("[URL] Error getting Url for : " + targetSpace);
    }
    return false;
  }

  public boolean uploadDocument(String targetSpace, String path, String name, String contentType, InputStream stream) {

    // Sample
    // /rest/private/jcr/repository/collaboration/Groups/spaces/migration_test_sandbox/Documents

    if (!targetSpace.startsWith("group/spaces/")) {
      return false;
    }
    String space = targetSpace.substring("group/spaces/".length());
    if (space.length() == 0) {
      return false;
    }
    try {
      String targetUrl = targetHost + "/rest/private/jcr/repository/collaboration/Groups/spaces/" + space + "/Documents/";
      String filePath = URLEncoder.encode(path, DEFAULT_ENCODING).replaceAll("%2F", "/");
      String uriFile = targetUrl + filePath + "/" + URLEncoder.encode(name, DEFAULT_ENCODING);

      // Check File exists
      if (checkURIExists(uriFile)) {
        LOGGER.error("[Upload] Document already exists [" + path + "/" + name + "], not replaced.");
      } else {

        String uriPath = targetUrl + filePath + "/";
        // Check parent dir exists
        if (!checkURIExists(uriPath)) {
          // Build intermediate folders if not alredy exists
          String[] pathArray = path.split("/");
          String pathInProgress = targetUrl;
          for (String pathElement : pathArray) {
            pathInProgress += URLEncoder.encode(pathElement, DEFAULT_ENCODING) + "/";
            // Check directory
            if (!checkURIExists(pathInProgress)) {
              // Create dir
              DavMethod mkCol = new MkColMethod(pathInProgress);
              int returnCode = davClient.executeMethod(mkCol);
              if (returnCode != 201) {
                LOGGER.error("[Upload] Error creating directory [" + path + "] [Err:" + returnCode + "]");
                return false;
              }
            }
          }
          LOGGER.info("[Upload] Created directory [" + path + "]");
        }

        // Upload file
        PutMethod method = new PutMethod(uriFile);
        RequestEntity requestEntity = new InputStreamRequestEntity(stream);
        method.setRequestEntity(requestEntity);
        int returnCode = davClient.executeMethod(method);
        if (returnCode == 201) {
          LOGGER.info("[Upload] Document uploaded [" + path + "/" + name + "]");
        } else {
          LOGGER.info("[Upload] Document NOT uploaded [Err=" + returnCode + "]");
        }
      }

      return true;
    } catch (Exception e) {
      LOGGER.error("[Upload] Document upload error [" + path + "/" + name + "] " + e.getMessage());
    }
    return false;
  }

  private boolean checkURIExists(String uriFile) throws IOException, HttpException {
    int returnCode = 0;
    GetMethod propFind = new GetMethod(uriFile);
    returnCode = davClient.executeMethod(propFind);
    davClient.getHttpConnectionManager().closeIdleConnections(0);
    return returnCode == 200;
  }

  public String normalizePageName(String title, boolean replaceSpaces) {
    String replacedChars = CHAR_TO_REPLACE + (replaceSpaces ? " " : "");
    return normalizePageName(title, replacedChars, '_');
  }

  /**
   * Change the name of a page to an acceptable one for the wiki engine
   * 
   * @param title page title
   * @param replacedChars chars to be replaced
   * @param replacementChar the char to use
   * @return new title
   */
  public static String normalizePageName(String title, String replacedChars, char replacementChar) {
    String newContent = title;
    for (int i = 0; i < replacedChars.length(); i++) {
      newContent = newContent.replace(replacedChars.charAt(i), replacementChar);
    }
    return newContent;
  }
}
