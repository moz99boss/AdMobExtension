package com.admob;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.runtime.*;
import com.google.android.gms.ads.*;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

@DesignerComponent(
    version = 1,
    description = "AdMob Interstitial Ad component for displaying full-screen ads",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")
@UsesLibraries(libraries = "play-services-ads-lite.jar")
public class AdMobInterstitial extends AndroidNonvisibleComponent {
    private final Activity activity;
    private InterstitialAd interstitialAd;
    private String adUnitId = "";
    private String appId = "";
    private boolean testMode = true;

    public AdMobInterstitial(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
    }

    @SimpleFunction(description = "Set your AdMob App ID.")
    public void SetAppId(String appId) {
        this.appId = appId;
    }

    @SimpleFunction(description = "Set your Interstitial Ad Unit ID.")
    public void SetAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    @SimpleFunction(description = "Enable or disable test mode (true = test ads).")
    public void SetTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    @SimpleFunction(description = "Initialize the AdMob SDK with the App ID.")
    public void Initialize() {
        String usedAppId = testMode
            ? "ca-app-pub-3940256099942544~3347511713"
            : appId;

        try {
            MobileAds.initialize(activity, usedAppId, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Log.d("AdMobInterstitial", "AdMob initialized successfully");
                    AdInitialized();
                }
            });
        } catch (Exception e) {
            Log.e("AdMobInterstitial", "Error initializing AdMob: " + e.getMessage());
            AdFailedToLoad("Initialization failed: " + e.getMessage());
        }
    }

    @SimpleFunction(description = "Load the interstitial ad.")
    public void LoadAd() {
        String usedAdUnit = testMode
            ? "ca-app-pub-3940256099942544/1033173712"
            : adUnitId;

        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(activity, usedAdUnit, adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd ad) {
                interstitialAd = ad;
                AdLoaded();
                Log.d("AdMobInterstitial", "Ad loaded successfully");
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                interstitialAd = null;
                String errorMsg = adError.getMessage();
                Log.e("AdMobInterstitial", "Ad failed to load: " + errorMsg);
                AdFailedToLoad(errorMsg);
            }
        });
    }

    @SimpleFunction(description = "Show the interstitial ad if it's ready.")
    public void ShowAd() {
        activity.runOnUiThread(() -> {
            if (interstitialAd != null) {
                interstitialAd.show(activity);
                interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        AdDismissed();
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        AdFailedToLoad(adError.getMessage());
                        interstitialAd = null;
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        AdDisplayed();
                    }
                });
            } else {
                AdFailedToLoad("Ad not ready yet");
            }
        });
    }

    // Events
    @SimpleEvent(description = "Triggered when AdMob SDK initializes successfully.")
    public void AdInitialized() {
        EventDispatcher.dispatchEvent(this, "AdInitialized");
    }

    @SimpleEvent(description = "Triggered when an ad loads successfully.")
    public void AdLoaded() {
        EventDispatcher.dispatchEvent(this, "AdLoaded");
    }

    @SimpleEvent(description = "Triggered when an ad fails to load.")
    public void AdFailedToLoad(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", errorMessage);
    }

    @SimpleEvent(description = "Triggered when an ad is displayed.")
    public void AdDisplayed() {
        EventDispatcher.dispatchEvent(this, "AdDisplayed");
    }

    @SimpleEvent(description = "Triggered when the ad is dismissed by the user.")
    public void AdDismissed() {
        EventDispatcher.dispatchEvent(this, "AdDismissed");
    }
}