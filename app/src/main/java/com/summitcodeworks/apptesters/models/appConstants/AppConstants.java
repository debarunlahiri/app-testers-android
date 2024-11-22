
package com.summitcodeworks.apptesters.models.appConstants;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppConstants {

    @SerializedName("header")
    @Expose
    private AppConstantsHeader appConstantsHeader;
    @SerializedName("response")
    @Expose
    private AppConstantsResponse appConstantsResponse;

    public AppConstantsHeader getHeader() {
        return appConstantsHeader;
    }

    public void setHeader(AppConstantsHeader appConstantsHeader) {
        this.appConstantsHeader = appConstantsHeader;
    }

    public AppConstantsResponse getResponse() {
        return appConstantsResponse;
    }

    public void setResponse(AppConstantsResponse appConstantsResponse) {
        this.appConstantsResponse = appConstantsResponse;
    }

}
