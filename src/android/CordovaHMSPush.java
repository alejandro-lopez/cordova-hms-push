package com.uisgr.hmspush;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;
import android.util.Log;
import java.lang.Thread;
import android.app.Activity;
import com.huawei.agconnect.config.*;
import com.huawei.hms.aaid.HmsInstanceId;

/**
 * This class echoes a string called from JavaScript.
 */
public class CordovaHMSPush extends CordovaPlugin {
    public static String token = "";
    private static CordovaHMSPush instance;
    private static Activity activity;
    private CallbackContext initCallback;
    private static String TAG="HMS Push";

    public CordovaHMSPush() {
        instance = this;
    }

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        activity = cordova.getActivity();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("init")) {
            this.init(callbackContext);
            return true;
        }
        return false;
    }

    private void init(CallbackContext callbackContext) {
        Log.i(TAG, "get token: begin");

        // get token
        new Thread() {
            @Override
            public void run() {
                try {
                    String appId = AGConnectServicesConfig.fromContext(activity).getString("client/app_id");
                    token = HmsInstanceId.getInstance(activity).getToken(appId, "HCM");
                    if (!TextUtils.isEmpty(token)) {
                        Log.i(TAG, "get token:" + token);
                        CordovaHMSPush.token = token;
                        callbackContext.success("{status:\"success\"}");
                        CordovaHMSPush.onTokenRegistered(token);
                    }
                } catch (Exception e) {
                    onTokenRegistered(e.getMessage());
                    callbackContext.error("{status:\"failed\"}");
                    Log.i(TAG, "getToken failed, " + e);
                }
            }
        }.start();
        this.initCallback = callbackContext;
    }

    public static void onTokenRegistered(String regId) {
        Log.e(TAG, "-------------onTokenRegistered------------------" + regId);
        if (instance == null) {
            return;
        }
        try {
            JSONObject object = new JSONObject();
            object.put("token",regId);
            String format = "window.cordova.plugins.hmspush.tokenRegistered(%s);";
            final String js = String.format(format, object.toString());
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    instance.webView.loadUrl("javascript:" + js);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
