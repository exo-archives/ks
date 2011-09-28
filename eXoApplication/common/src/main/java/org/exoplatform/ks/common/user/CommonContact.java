/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
package org.exoplatform.ks.common.user;

/**
 * 
 * User profile information for forum users.<br><br>
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Aug 21, 2008  
 *
 */
public class CommonContact {
  private String emailAddress = "";
  private String city = "";
  private String country = "";
  private String avatarUrl = "";
  private String birthday = "";
  private String gender = "";
  private String job = "";
  private String workPhone = "";
  private String homePhone = "";
  private String webSite = "";
  private String firstName = "";
  private String lastName = "";
  private String fullName = "";


  public CommonContact() {
    
  }
  
  public String getEmailAddress() {
    return emailAddress;
  }
  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
  }
  public String getCity() {
    return city;
  }
  public void setCity(String city) {
    this.city = city;
  }
  public String getCountry() {
    return country;
  }
  public void setCountry(String country) {
    this.country = country;
  }
  public String getAvatarUrl() {
    return avatarUrl;
  }
  public void setAvatarUrl(String avatarUrl) {
    this.avatarUrl = avatarUrl;
  }
  public String getBirthday() {
    return birthday;
  }
  public void setBirthday(String birthday) {
    this.birthday = birthday;
  }
  public String getGender() {
    return gender;
  }
  public void setGender(String gender) {
    this.gender = gender;
  }
  public String getJob() {
    return job;
  }
  public void setJob(String job) {
    this.job = job;
  }
  public String getWorkPhone() {
    return workPhone;
  }
  public void setWorkPhone(String phone) {
    this.workPhone = phone;
  }
  public String getHomePhone() {
    return homePhone;
  }
  public void setHomePhone(String mobile) {
    this.homePhone = mobile;
  }
  public String getWebSite() {
    return webSite;
  }
  public void setWebSite(String webSite) {
    this.webSite = webSite;
  }

  public void setFirstName(String givenName) {
    this.firstName = givenName;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setLastName(String familyName) {
    this.lastName = familyName;
  }

  public String getLastName() {
    return lastName;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }
}
