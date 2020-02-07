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
import java.util.Arrays;
import android.app.Activity;
import com.huawei.hms.push.RemoteMessage;
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
        instance.initCallback = callbackContext;
    }
    public static void deleteToken(CallbackContext callbackContext) {
        new Thread() {
            @Override
            public void run() {
                try {
                    // read from agconnect-services.json
                    String appId = AGConnectServicesConfig.fromContext(activity).getString("client/app_id");
                    HmsInstanceId.getInstance(activity).deleteToken(appId, "HCM");
                    Log.i(TAG, "deleteToken success.");
                    callbackContext.success("{status:\"success\"}");
                } catch (Exception e) {
                    Log.e(TAG, "deleteToken failed." + e);
                    callbackContext.error("{status:\"failed\"}");
                }
            }
        }.start();
        instance.initCallback = callbackContext;
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
    
    public static void onMessageReceived(RemoteMessage message) {
        Log.i(TAG, "onMessageReceived is called");
        if (message == null) {
            Log.e(TAG, "Received message entity is null!");
            return;
        }
        Log.i(TAG, "getCollapseKey: " + message.getCollapseKey()
                + "\n getData: " + message.getData()
                + "\n getFrom: " + message.getFrom()
                + "\n getTo: " + message.getTo()
                + "\n getMessageId: " + message.getMessageId()
                + "\n getOriginalUrgency: " + message.getOriginalUrgency()
                + "\n getUrgency: " + message.getUrgency()
                + "\n getSendTime: " + message.getSentTime()
                + "\n getMessageType: " + message.getMessageType()
                + "\n getTtl: " + message.getTtl());
        RemoteMessage.Notification notification = message.getNotification();
        JSONObject object = new JSONObject();
        if (notification != null) {
            Log.i(TAG, "\n getImageUrl: " + notification.getImageUrl()
                    + "\n getTitle: " + notification.getTitle()
                    + "\n getTitleLocalizationKey: " + notification.getTitleLocalizationKey()
                    + "\n getTitleLocalizationArgs: " + Arrays.toString(notification.getTitleLocalizationArgs())
                    + "\n getBody: " + notification.getBody()
                    + "\n getBodyLocalizationKey: " + notification.getBodyLocalizationKey()
                    + "\n getBodyLocalizationArgs: " + Arrays.toString(notification.getBodyLocalizationArgs())
                    + "\n getIcon: " + notification.getIcon()
                    + "\n getSound: " + notification.getSound()
                    + "\n getTag: " + notification.getTag()
                    + "\n getColor: " + notification.getColor()
                    + "\n getClickAction: " + notification.getClickAction()
                    + "\n getChannelId: " + notification.getChannelId()
                    + "\n getLink: " + notification.getLink()
                    + "\n getNotifyId: " + notification.getNotifyId());
            object.put("title",notification.getTitle());
            object.put("message",notification.getBody());
        }
        
        
        object.put("additionalData",regId);
        String format = "window.cordova.plugins.hmspush.onMessageReceived(%s);";
        final String js = String.format(format, object.toString());
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                instance.webView.loadUrl("javascript:" + js);
            }
        });
        Boolean judgeWhetherIn10s = false;
        // If the messages are not processed in 10 seconds, the app needs to use WorkManager for processing.
        if (judgeWhetherIn10s) {
            startWorkManagerJob(message);
        } else {
            // Process message within 10s
            processWithin10s(message);
        }
    }
    private void startWorkManagerJob(RemoteMessage message) {
        Log.d(TAG, "Start new job processing.");
    }
    private void processWithin10s(RemoteMessage message) {
        Log.d(TAG, "Processing now.");
    }
}
