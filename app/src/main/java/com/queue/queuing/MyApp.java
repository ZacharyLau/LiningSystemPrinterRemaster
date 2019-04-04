package com.queue.queuing;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.tencent.sonic.sdk.SonicConfig;
import com.tencent.sonic.sdk.SonicEngine;

/**
 * Created by Administrator on 2018/11/2.
 */

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        initLogger();
        initSonicEngine();

    }



    private void initLogger() {
        Logger.addLogAdapter(new AndroidLogAdapter());
    }


    private void initSonicEngine() {
        // init sonic engine if necessary, or maybe u can do this when application created
        if (!SonicEngine.isGetInstanceAllowed()) {
            SonicEngine.createInstance(new SonicRuntimeImpl(this), new SonicConfig.Builder().build());
        }
    }
}
