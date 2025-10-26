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
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.ads.rewarded.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;

@DesignerComponent(
    version = 1,
    description = "AdMob Rewarded Ad component for showing ads that give users rewards after watching.",
    category = ComponentCategory.EXTENSION,
    nonVisible = true,
    iconName = "images/extension.png"
)
@SimpleObject(external = true)
@UsesPermissions(permissionNames = "android.permission.INTERNET, android.permission.ACCESS_NETWORK_STATE")
@UsesLibraries(libraries = "play-services-ads-lite.jar")
public class AdMobRewarded extends AndroidNonvisibleComponent {
    private final Activity activity;
    private RewardedAd rewardedAd;
    private String adUnitId = "";
    private String appId = "";
    private boolean testMode = true;

    public AdMobRewarded(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
    }

    @SimpleFunction(description = "Set your AdMob App ID.")
    public void SetAppId(String appId) {
        this.appId = appId;
    }

    @SimpleFunction(description = "Set your Rewarded Ad Unit ID.")
    public void SetAdUnitId(String adUnitId) {
        this.adUnitId = adUnitId;
    }

    @SimpleFunction(description = "Enable or disable test mode (true = test ads).")
    public void SetTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    @SimpleFunction(description = "Initialize AdMob with your App ID.")
    public void Initialize() {
        String usedAppId = testMode
            ? "ca-app-pub-3940256099942544~3347511713"  // Test App ID
            : appId;

        try {
            MobileAds.initialize(activity, usedAppId, new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    Log.d("AdMobRewarded", "AdMob initialized successfully");
                    AdInitialized();
                }
            });
        } catch (Exception e) {
            Log.e("AdMobRewarded", "Error initializing AdMob: " + e.getMessage());
            AdFailedToLoad("Initialization failed: " + e.getMessage());
        }
    }

    @SimpleFunction(description = "Load a rewarded ad.")
    public void LoadAd() {
        String usedAdUnit = testMode
            ? "ca-app-pub-3940256099942544/5224354917"  // Test Rewarded Ad ID
            : adUnitId;

        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(activity, usedAdUnit, adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                rewardedAd = ad;
                Log.d("AdMobRewarded", "Rewarded ad loaded successfully");
                AdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                rewardedAd = null;
                Log.e("AdMobRewarded", "Rewarded ad failed to load: " + adError.getMessage());
                AdFailedToLoad(adError.getMessage());
            }
        });
    }

    @SimpleFunction(description = "Show the rewarded ad if loaded.")
    public void ShowAd() {
        activity.runOnUiThread(() -> {
            if (rewardedAd != null) {
                rewardedAd.show(activity, new OnUserEarnedRewardListener() {
                    @Override
                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                        String rewardType = rewardItem.getType();
                        int rewardAmount = rewardItem.getAmount();
                        Log.d("AdMobRewarded", "User earned reward: " + rewardAmount + " " + rewardType);
                        RewardEarned(rewardType, rewardAmount);
                    }
                });

                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        AdDisplayed();
                        Log.d("AdMobRewarded", "Rewarded ad displayed");
                    }

                    @Override
                    public void onAdDismissedFullScreenContent() {
                        AdClosed();
                        rewardedAd = null;
                        Log.d("AdMobRewarded", "Rewarded ad dismissed");
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                        AdFailedToLoad(adError.getMessage());
                        rewardedAd = null;
                        Log.e("AdMobRewarded", "Failed to show rewarded ad: " + adError.getMessage());
                    }
                });
            } else {
                AdFailedToLoad("Ad not loaded yet");
                Log.e("AdMobRewarded", "Tried to show ad before loading");
            }
        });
    }

    // Events for Kodular
    @SimpleEvent(description = "Triggered when AdMob initializes successfully.")
    public void AdInitialized() {
        EventDispatcher.dispatchEvent(this, "AdInitialized");
    }

    @SimpleEvent(description = "Triggered when a rewarded ad loads successfully.")
    public void AdLoaded() {
        EventDispatcher.dispatchEvent(this, "AdLoaded");
    }

    @SimpleEvent(description = "Triggered when an ad fails to load.")
    public void AdFailedToLoad(String errorMessage) {
        EventDispatcher.dispatchEvent(this, "AdFailedToLoad", errorMessage);
    }

    @SimpleEvent(description = "Triggered when a rewarded ad is displayed.")
    public void AdDisplayed() {
        EventDispatcher.dispatchEvent(this, "AdDisplayed");
    }

    @SimpleEvent(description = "Triggered when a rewarded ad is closed by the user.")
    public void AdClosed() {
        EventDispatcher.dispatchEvent(this, "AdClosed");
    }

    @SimpleEvent(description = "Triggered when the user earns a reward.")
    public void RewardEarned(String rewardType, int rewardAmount) {
        EventDispatcher.dispatchEvent(this, "RewardEarned", rewardType, rewardAmount);
    }
}