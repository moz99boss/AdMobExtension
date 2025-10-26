package com.admob;  // Change this to your own package name

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import com.google.appinventor.components.annotations.*;
import com.google.appinventor.components.runtime.*;

@DesignerComponent(
        version = 1,
        description = "AdMob Banner Extension with App ID, Ad Unit, and Test Mode support.",
        category = ComponentCategory.EXTENSION,
        nonVisible = true,
        iconName = "aiwebres/icon.png")

@SimpleObject(external = true)
public class AdMobBanner extends AndroidNonvisibleComponent {

    private final Activity activity;
    private AdView adView;
    private String appId = "";
    private String adUnitId = "";
    private boolean testMode = false;
    private boolean isInitialized = false;

    public AdMobBanner(ComponentContainer container) {
        super(container.$form());
        this.activity = container.$context();
        Log.d("AdMobBanner", "Extension Loaded");
    }

    // 🔹 Set App ID (can be your real one or dynamic)
    @SimpleFunction(description = "Set your AdMob App ID (e.g., ca-app-pub-xxxxxxxx~yyyyyyyy)")
    public void SetAppId(String id) {
        this.appId = id;
    }

    // 🔹 Set Ad Unit ID
    @SimpleFunction(description = "Set your Ad Unit ID (e.g., ca-app-pub-xxxxxxxx/zzzzzzzz)")
    public void SetAdUnitId(String id) {
        this.adUnitId = id;
    }

    // 🔹 Enable or disable test mode
    @SimpleFunction(description = "Enable test mode (true/false)")
    public void SetTestMode(boolean enabled) {
        this.testMode = enabled;
    }

    // 🔹 Initialize AdMob
    @SimpleFunction(description = "Initialize AdMob with the App ID. Must be called before loading ads.")
    public void Initialize() {
        if (isInitialized) {
            Log.d("AdMobBanner", "Already initialized");
            AdInitialized();
            return;
        }

        String finalAppId = testMode
                ? "ca-app-pub-3940256099942544~3347511713" // ✅ Google Test App ID
                : (appId.isEmpty() ? "ca-app-pub-3940256099942544~3347511713" : appId);

        MobileAds.initialize(activity, finalAppId, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                isInitialized = true;
                Log.d("AdMobBanner", "AdMob initialized with App ID: " + finalAppId);
                AdInitialized();
            }
        });
    }

    // 🔹 Load Banner Ad
    @SimpleFunction(description = "Load and display a banner ad.")
    public void LoadBanner() {
        if (!isInitialized) {
            Log.e("AdMobBanner", "Initialize AdMob first!");
            return;
        }

        if (adUnitId.isEmpty()) {
            Log.e("AdMobBanner", "Ad Unit ID is empty!");
            return;
        }

        activity.runOnUiThread(() -> {
            adView = new AdView(activity);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(testMode
                    ? "ca-app-pub-3940256099942544/6300978111" // ✅ Google Test Banner
                    : adUnitId);

            FrameLayout layout = new FrameLayout(activity);
            layout.addView(adView, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            activity.addContentView(layout, new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));

            AdRequest adRequest = new AdRequest.Builder().build();
            adView.loadAd(adRequest);
            Log.d("AdMobBanner", "Banner loaded");
        });
    }

    // 🔹 Event when AdMob is initialized
    @SimpleEvent(description = "Triggered when AdMob finishes initialization.")
    public void AdInitialized() {
        EventDispatcher.dispatchEvent(this, "AdInitialized");
    }
}