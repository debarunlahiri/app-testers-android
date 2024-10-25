
package com.summitcodeworks.apptesters.models.userApps;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserAppsResponse {

    @SerializedName("app_app_link")
    @Expose
    private String appAppLink;
    @SerializedName("app_created_by")
    @Expose
    private Integer appCreatedBy;
    @SerializedName("app_created_on")
    @Expose
    private String appCreatedOn;
    @SerializedName("app_dev_name")
    @Expose
    private String appDevName;
    @SerializedName("app_id")
    @Expose
    private Integer appId;
    @SerializedName("app_logo")
    @Expose
    private String appLogo;
    @SerializedName("app_name")
    @Expose
    private String appName;
    @SerializedName("app_pkg_nme")
    @Expose
    private String appPkgNme;
    @SerializedName("app_web_link")
    @Expose
    private String appWebLink;
    @SerializedName("use_flag")
    @Expose
    private Boolean useFlag;
    @SerializedName("app_desc")
    @Expose
    private String appDesc;
    @SerializedName("app_credit")
    @Expose
    private Integer appCredit;

    public Integer getAppCredit() {
        return appCredit;
    }

    public void setAppCredit(Integer appCredit) {
        this.appCredit = appCredit;
    }

    public String getAppDesc() {
        return appDesc;
    }

    public void setAppDesc(String appDesc) {
        this.appDesc = appDesc;
    }

    public String getAppAppLink() {
        return appAppLink;
    }

    public void setAppAppLink(String appAppLink) {
        this.appAppLink = appAppLink;
    }

    public Integer getAppCreatedBy() {
        return appCreatedBy;
    }

    public void setAppCreatedBy(Integer appCreatedBy) {
        this.appCreatedBy = appCreatedBy;
    }

    public String getAppCreatedOn() {
        return appCreatedOn;
    }

    public void setAppCreatedOn(String appCreatedOn) {
        this.appCreatedOn = appCreatedOn;
    }

    public String getAppDevName() {
        return appDevName;
    }

    public void setAppDevName(String appDevName) {
        this.appDevName = appDevName;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getAppLogo() {
        return appLogo;
    }

    public void setAppLogo(String appLogo) {
        this.appLogo = appLogo;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPkgNme() {
        return appPkgNme;
    }

    public void setAppPkgNme(String appPkgNme) {
        this.appPkgNme = appPkgNme;
    }

    public String getAppWebLink() {
        return appWebLink;
    }

    public void setAppWebLink(String appWebLink) {
        this.appWebLink = appWebLink;
    }

    public Boolean getUseFlag() {
        return useFlag;
    }

    public void setUseFlag(Boolean useFlag) {
        this.useFlag = useFlag;
    }

}
