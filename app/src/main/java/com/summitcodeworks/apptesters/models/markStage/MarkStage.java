
package com.summitcodeworks.apptesters.models.markStage;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MarkStage {

    @SerializedName("header")
    @Expose
    private MarkStageHeader markStageHeader;
    @SerializedName("response")
    @Expose
    private MarkStageResponse markStageResponse;

    public MarkStageHeader getHeader() {
        return markStageHeader;
    }

    public void setHeader(MarkStageHeader markStageHeader) {
        this.markStageHeader = markStageHeader;
    }

    public MarkStageResponse getResponse() {
        return markStageResponse;
    }

    public void setResponse(MarkStageResponse markStageResponse) {
        this.markStageResponse = markStageResponse;
    }

}
