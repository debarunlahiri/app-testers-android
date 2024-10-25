
package com.summitcodeworks.apptesters.models.userDetails;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserDetails {

    @SerializedName("header")
    @Expose
    private UserDetailsHeader userDetailsHeader;
    @SerializedName("response")
    @Expose
    private UserDetailsResponse userDetailsResponse;

    public UserDetailsHeader getHeader() {
        return userDetailsHeader;
    }

    public void setHeader(UserDetailsHeader userDetailsHeader) {
        this.userDetailsHeader = userDetailsHeader;
    }

    public UserDetailsResponse getResponse() {
        return userDetailsResponse;
    }

    public void setResponse(UserDetailsResponse userDetailsResponse) {
        this.userDetailsResponse = userDetailsResponse;
    }

}
