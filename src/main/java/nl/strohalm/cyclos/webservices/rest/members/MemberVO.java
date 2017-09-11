/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.strohalm.cyclos.webservices.rest.members;

/**
 *
 * @author Lue
 */
public class MemberVO {
      private String memberLogin;
            private String fullName;
            private String eMail;
            private String birthday;
            private String gender;
            private String address;
            private String postalCode;
            private String city;
            private String area;
            private String phone;
            private String mobilePhone;
            private String fax;
            private String url;

    public MemberVO(String memberLogin, String fullName, String eMail, String birthday, String gender, String address, String postalCode, String city, String area, String phone, String mobilePhone, String fax, String url) {
        this.memberLogin = memberLogin;
        this.fullName = fullName;
        this.eMail = eMail;
        this.birthday = birthday;
        this.gender = gender;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.area = area;
        this.phone = phone;
        this.mobilePhone = mobilePhone;
        this.fax = fax;
        this.url = url;
    }
            

            public String getMemberLogin() {
                return memberLogin;
            }

            public void setMemberLogin(String memberLogin) {
                this.memberLogin = memberLogin;
            }

            public String getFullName() {
                return fullName;
            }

            public void setFullName(String fullName) {
                this.fullName = fullName;
            }

            public String geteMail() {
                return eMail;
            }

            public void seteMail(String eMail) {
                this.eMail = eMail;
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

            public String getAddress() {
                return address;
            }

            public void setAddress(String address) {
                this.address = address;
            }

            public String getPostalCode() {
                return postalCode;
            }

            public void setPostalCode(String postalCode) {
                this.postalCode = postalCode;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getArea() {
                return area;
            }

            public void setArea(String area) {
                this.area = area;
            }

            public String getPhone() {
                return phone;
            }

            public void setPhone(String phone) {
                this.phone = phone;
            }

            public String getMobilePhone() {
                return mobilePhone;
            }

            public void setMobilePhone(String mobilePhone) {
                this.mobilePhone = mobilePhone;
            }

            public String getFax() {
                return fax;
            }

            public void setFax(String fax) {
                this.fax = fax;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }
            
 }
    

