package com.icreamguide2;

import android.app.Application;

import com.facebook.ads.AudienceNetworkAds;
import com.neufkg.adx.service.AdsExchange;

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        AudienceNetworkAds.initialize(this);
        AdsExchange.init(this, "");
    }
}