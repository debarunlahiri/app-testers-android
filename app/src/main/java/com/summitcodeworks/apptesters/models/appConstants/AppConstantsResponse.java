
package com.summitcodeworks.apptesters.models.appConstants;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AppConstantsResponse {

    @SerializedName("constantId")
    @Expose
    private Integer constantId;
    @SerializedName("constantKey")
    @Expose
    private String constantKey;
    @SerializedName("constantValue")
    @Expose
    private String constantValue;
    @SerializedName("useFlag")
    @Expose
    private Boolean useFlag;

    public Integer getConstantId() {
        return constantId;
    }

    public void setConstantId(Integer constantId) {
        this.constantId = constantId;
    }

    public String getConstantKey() {
        return constantKey;
    }

    public void setConstantKey(String constantKey) {
        this.constantKey = constantKey;
    }

    public String getConstantValue() {
        return constantValue;
    }

    public void setConstantValue(String constantValue) {
        this.constantValue = constantValue;
    }

    public Boolean getUseFlag() {
        return useFlag;
    }

    public void setUseFlag(Boolean useFlag) {
        this.useFlag = useFlag;
    }

}
