
package com.summitcodeworks.apptesters.models.appDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppDetailsResponse {

    @SerializedName("appAppLink")
    @Expose
    private String appAppLink;
    @SerializedName("appCreatedBy")
    @Expose
    private Integer appCreatedBy;
    @SerializedName("appCreatedOn")
    @Expose
    private String appCreatedOn;
    @SerializedName("appCredit")
    @Expose
    private Integer appCredit;
    @SerializedName("appDesc")
    @Expose
    private String appDesc;
    @SerializedName("appDevName")
    @Expose
    private String appDevName;
    @SerializedName("appId")
    @Expose
    private Integer appId;
    @SerializedName("appLogo")
    @Expose
    private String appLogo;
    @SerializedName("appName")
    @Expose
    private String appName;
    @SerializedName("appPkgNme")
    @Expose
    private String appPkgNme;
    @SerializedName("appWebLink")
    @Expose
    private String appWebLink;
    @SerializedName("useFlag")
    @Expose
    private Boolean useFlag;

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
