package com.admob;

import android.app.Activity;
import android.util.Log;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback;

@DesignerComponent(
    version = 1,
    description = "AdMob App Open Ad component (shows ad when app launches)",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")
@UsesLibraries(libraries = "play-services-ads-lite.jar")
public class AdMobAppOpen extends AndroidNonvisibleComponent {
    private final Activity activity;
    private AppOpenAd appOpenAd;
    private String adUnitId = "";
    private boolean testMode = true;

    public AdMobAppOpen(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
    }

    @SimpleFunction(description = "Initialize AdMob App Open system")
    public void Initialize() {
        MobileAds.initialize(activity, initializationStatus -> AdInitialized());
    }

    @SimpleFunction(description = "Set Ad Unit ID")
    public void SetAdUnitId(String id) {
        adUnitId = id;
    }

    @SimpleFunction(description = "Enable or disable test mode")
    public void SetTestMode(boolean enabled) {
        testMode = enabled;
    }

    @SimpleFunction(description = "Load App Open Ad")
    public void LoadAd() {
        String unit = testMode ? "ca-app-pub-3940256099942544/9257395921" : adUnitId;
        AdRequest request = new AdRequest.Builder().build();

        AppOpenAd.load(activity, unit, request, AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                new AppOpenAdLoadCallback() {
                    @Override
                    public void onAdLoaded(AppOpenAd ad) {
                        appOpenAd = ad;
                        AdLoaded();
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
                        AdFailedToLoad(loadAdError.getMessage());
                    }
                });
    }

    @SimpleFunction(description = "Show App Open Ad if loaded")
    public void ShowAd() {
        if (appOpenAd != null) {
            appOpenAd.show(activity);
            AdDisplayed();
            appOpenAd = null;
        } else {
            AdFailedToLoad("Ad not loaded yet");
        }
    }

    @SimpleEvent(description = "Triggered when AdMob is initialized")
    public void AdInitialized() {
        EventDispatcher.dispatchEvent(this, "AdInitialized");
    }

    @SimpleEvent(description = "Triggered when ad is successfully loaded")
    public void AdLoaded() {
        EventDispatcher.dispatchEvent(this, "AdLoaded");
    }

    @SimpleEvent(description = "Triggered when ad fails to load")
    public void AdFailedToLoad(String error) {
        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", error);
    }

    @SimpleEvent(description = "Triggered when ad is displayed")
    public void AdDisplayed() {
        EventDispatcher.dispatchEvent(this, "AdDisplayed");
    }
}