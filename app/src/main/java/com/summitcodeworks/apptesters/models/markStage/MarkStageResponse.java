
package com.summitcodeworks.apptesters.models.markStage;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MarkStageResponse {

    @SerializedName("app_id")
    @Expose
    private Integer appId;
    @SerializedName("create_date")
    @Expose
    private String createDate;
    @SerializedName("stage_id")
    @Expose
    private Integer stageId;
    @SerializedName("stage_no")
    @Expose
    private Integer stageNo;
    @SerializedName("user_id")
    @Expose
    private Integer userId;

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public Integer getStageId() {
        return stageId;
    }

    public void setStageId(Integer stageId) {
        this.stageId = stageId;
    }

    public Integer getStageNo() {
        return stageNo;
    }

    public void setStageNo(Integer stageNo) {
        this.stageNo = stageNo;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

}
