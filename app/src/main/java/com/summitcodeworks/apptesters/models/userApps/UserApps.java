
package com.summitcodeworks.apptesters.models.userApps;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserApps {

    @SerializedName("header")
    @Expose
    private UserAppsHeader userAppsHeader;
    @SerializedName("response")
    @Expose
    private List<UserAppsResponse> userAppsResponse;

    public UserAppsHeader getHeader() {
        return userAppsHeader;
    }

    public void setHeader(UserAppsHeader userAppsHeader) {
        this.userAppsHeader = userAppsHeader;
    }

    public List<UserAppsResponse> getResponse() {
        return userAppsResponse;
    }

    public void setResponse(List<UserAppsResponse> userAppsResponse) {
        this.userAppsResponse = userAppsResponse;
    }

}
