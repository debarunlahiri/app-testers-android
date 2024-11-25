
package com.summitcodeworks.apptesters.models.appConstants;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppConstants {

    @SerializedName("header")
    @Expose
    private AppConstantsHeader appConstantsHeader;
    @SerializedName("response")
    @Expose
    private List<AppConstantsResponse> appConstantsResponseList;

    public AppConstantsHeader getHeader() {
        return appConstantsHeader;
    }

    public void setHeader(AppConstantsHeader appConstantsHeader) {
        this.appConstantsHeader = appConstantsHeader;
    }

    public List<AppConstantsResponse> getResponse() {
        return appConstantsResponseList;
    }

    public void setResponse(List<AppConstantsResponse> appConstantsResponseList) {
        this.appConstantsResponseList = appConstantsResponseList;
    }

}
