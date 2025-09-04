package com.nagendra.nagendra;

public class RewardModel {
    private String rewardTitle;
    private String mobile;
    private String upiId;
    private String status;

    public RewardModel(String rewardTitle, String mobile, String upiId, String status) {
        this.rewardTitle = rewardTitle;
        this.mobile = mobile;
        this.upiId = upiId;
        this.status = status;
    }

    public String getRewardTitle() {
        return rewardTitle;
    }

    public String getMobile() {
        return mobile;
    }

    public String getUpiId() {
        return upiId;
    }

    public String getStatus() {
        return status;
    }
}
