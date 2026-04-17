package com.jiqiu.configapp;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

/**
 * Application class for dynamic receiver registration
 * 动态注册 BroadcastReceiver，避免被第三方 app 发现
 */
public class ConfigApplication extends Application {
    private static final String TAG = "ConfigApplication";
    private static final String ACTION_APPLY_CONFIG = ConfigApplyReceiver.ACTION_APPLY_CONFIG;
    private static final String BROADCAST_PERMISSION = "android.permission.DUMP";
    
    private ConfigApplyReceiver configReceiver;
    
    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Application onCreate - registering receiver dynamically");
        
        // 动态注册 ConfigApplyReceiver
        configReceiver = new ConfigApplyReceiver();
        IntentFilter filter = new IntentFilter(ACTION_APPLY_CONFIG);
        
        // 允许 shell/root 通过广播命中动态 receiver, 具体访问控制在 receiver 内部再做 UID 校验
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(configReceiver, filter, BROADCAST_PERMISSION, (Handler) null, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(configReceiver, filter, BROADCAST_PERMISSION, null);
        }
        Log.i(TAG, "ConfigApplyReceiver registered dynamically, action=" + ACTION_APPLY_CONFIG);
        Log.i(TAG, "Dynamic receiver exported for shell/root broadcasts, permission=" + BROADCAST_PERMISSION);
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        
        // 注销 receiver（注意：onTerminate 在真实设备上通常不会被调用，仅在模拟器中）
        if (configReceiver != null) {
            try {
                unregisterReceiver(configReceiver);
                Log.d(TAG, "Receiver unregistered");
            } catch (IllegalArgumentException e) {
                Log.w(TAG, "Receiver was not registered or already unregistered");
            }
        }
    }
}
