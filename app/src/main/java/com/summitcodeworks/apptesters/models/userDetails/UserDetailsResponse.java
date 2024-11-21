
package com.summitcodeworks.apptesters.models.userDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetailsResponse {

    @SerializedName("useFlag")
    @Expose
    private Boolean useFlag;
    @SerializedName("userCreationDate")
    @Expose
    private String userCreationDate;
    @SerializedName("userCredits")
    @Expose
    private Integer userCredits;
    @SerializedName("userEmail")
    @Expose
    private String userEmail;
    @SerializedName("userId")
    @Expose
    private Integer userId;
    @SerializedName("userKey")
    @Expose
    private String userKey;
    @SerializedName("userName")
    @Expose
    private String userName;
    @SerializedName("userPhotoUrl")
    @Expose
    private String userPhotoUrl;

    public Boolean getUseFlag() {
        return useFlag;
    }

    public void setUseFlag(Boolean useFlag) {
        this.useFlag = useFlag;
    }

    public String getUserCreationDate() {
        return userCreationDate;
    }

    public void setUserCreationDate(String userCreationDate) {
        this.userCreationDate = userCreationDate;
    }

    public Integer getUserCredits() {
        return userCredits;
    }

    public void setUserCredits(Integer userCredits) {
        this.userCredits = userCredits;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUserKey() {
        return userKey;
    }

    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }

    public void setUserPhotoUrl(String userPhotoUrl) {
        this.userPhotoUrl = userPhotoUrl;
    }
}
