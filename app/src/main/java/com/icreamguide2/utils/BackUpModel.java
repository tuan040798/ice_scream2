package com.icreamguide2.utils;

public class BackUpModel {
    public String appPackage,newAppPackage,inter,fb_inter,banner,nativeAd,rewarded;
    public Boolean isLive,isAdsShow,isShowGG;

    public BackUpModel(String appPackage, String newAppPackage, String inter, String fb_inter, String banner, String nativeAd, String rewarded, Boolean isLive, Boolean isAdsShow, Boolean isShowGG) {
        this.appPackage = appPackage;
        this.newAppPackage = newAppPackage;
        this.inter = inter;
        this.fb_inter = fb_inter;
        this.banner = banner;
        this.nativeAd = nativeAd;
        this.rewarded = rewarded;
        this.isLive = isLive;
        this.isAdsShow = isAdsShow;
        this.isShowGG = isShowGG;
    }

    public BackUpModel() {
    }
}
