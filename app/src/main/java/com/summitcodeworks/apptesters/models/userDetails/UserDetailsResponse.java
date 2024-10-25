
package com.summitcodeworks.apptesters.models.userDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetailsResponse {

    @SerializedName("use_flag")
    @Expose
    private Boolean useFlag;
    @SerializedName("user_creation_date")
    @Expose
    private String userCreationDate;
    @SerializedName("user_credits")
    @Expose
    private Integer userCredits;
    @SerializedName("user_email")
    @Expose
    private String userEmail;
    @SerializedName("user_id")
    @Expose
    private Integer userId;
    @SerializedName("user_key")
    @Expose
    private String userKey;
    @SerializedName("user_name")
    @Expose
    private String userName;

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

}
