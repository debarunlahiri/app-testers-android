
package com.summitcodeworks.apptesters.models.appDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppDetails {

    @SerializedName("header")
    @Expose
    private AppDetailsHeader appDetailsHeader;
    @SerializedName("response")
    @Expose
    private AppDetailsResponse appDetailsResponse;

    public AppDetailsHeader getHeader() {
        return appDetailsHeader;
    }

    public void setHeader(AppDetailsHeader appDetailsHeader) {
        this.appDetailsHeader = appDetailsHeader;
    }

    public AppDetailsResponse getResponse() {
        return appDetailsResponse;
    }

    public void setResponse(AppDetailsResponse appDetailsResponse) {
        this.appDetailsResponse = appDetailsResponse;
    }

}
