package com.queue.queuing;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;


import com.orhanobut.logger.Logger;
import com.printsdk.usbsdk.UsbDriver;
import com.queue.queuing.Beans.Setting;
import com.queue.queuing.Dao.SettingDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONObject;
;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.queue.queuing.io.*;
import com.queue.queuing.util.Bills;
import com.queue.queuing.util.T;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class MainLocalActivity extends Activity { //implements IOCallBack
    ///printer part
    MainLocalActivity mActivity;
   // private Printnew printer;
    private static final int TO_SETTING = 1;
//    private SonicSession sonicSession;
//    private SonicSessionClientImpl sonicSessionClient;

    private String title = "",  num = "",codeStr = "";
    private int cutter = 0;       // 默认0，  0 全切、1 半切

    SettingDao settingDao;
    private String urlString = "https://pos.auroratech.top/queue/index.html"; // release
    private WebView webView;

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    private boolean canGoBack = false;

    private AlertDialog alertDialog;

    //QRcode
    private static final int QR_WIDTH = 350;
    private static final int QR_HEIGHT = 350;

    private JSONObject queuePrintingBuffer = null;
    private Setting setting = null;

    private UsbManager mUsbManager;
    private UsbDriver mUsbDriver;
    UsbDevice mUsbDev1;		//打印机1
    UsbDevice mUsbDev2;		//打印机2
    UsbDevice mUsbDev;
    private UsbReceiver mUsbReceiver;

    final int SERIAL_BAUDRATE = UsbDriver.BAUD115200;
    private static final String ACTION_USB_PERMISSION =  "com.usb.sample.USB_PERMISSION";
    private final static int PID11 = 8211;
    private final static int PID13 = 8213;
    private final static int PID15 = 8215;
    private final static int VENDORID = 1305;

    // 设备list
    private HashMap<String, UsbDevice> deviceList;
    //设备
    Iterator<UsbDevice> deviceIterator;

    ExecutorService es = Executors.newScheduledThreadPool(60);
    Pos mPos = new Pos();
    USBPrinting mUSB = new USBPrinting();
    UsbDevice mDevice = null;

    /******************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View decorView = getWindow().getDecorView();
        //禁止虚拟键盘
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);


        mActivity = this;
        //静止软件盘
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        SettingDao settingDao = new SettingDao(MainLocalActivity.this);
        setting = settingDao.queryById(1);
        urlString = urlString + "?token=" + getStringMD5(setting.getUserId() + setting.getUserId() + "4");
        //开启硬件加速
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setFullScreen();

//        mPos.Set(mUSB);
//        mUSB.SetCallBack(this);


        setContentView(R.layout.activity_main);
        initWebview();
        loadUrl();
        //********************************* INITIALIZING USB PRINTER *********************************
        getUsbDriverService();
        printConnStatus();
        //********************************* INITIALIZING USB PRINTER *********************************

        //printtest();
        //调用下面的代码可以触发开始执行任务：

        // mHanlder.postDelayed(task, 0);//第一次调用,延迟1秒执行task

        //停止执行任务：

        //mHanlder.removeCallbacks(task);

    }

    private void getUsbDriverService(){
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        //******************** from original code of this class *********************************
        deviceList = mUsbManager.getDeviceList();
        deviceIterator = deviceList.values().iterator();
        //******************** from original code of this class *********************************


        mUsbDriver = new UsbDriver(mUsbManager, this);
        PendingIntent permissionIntent1 = PendingIntent.getBroadcast(this, 0,
                new Intent(ACTION_USB_PERMISSION), 0);
        mUsbDriver.setPermissionIntent(permissionIntent1);
        // Broadcast listen for new devices

        mUsbReceiver = new MainLocalActivity.UsbReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        this.registerReceiver(mUsbReceiver, filter);
    }


    // Get UsbDriver(UsbManager) service
    private boolean printConnStatus() {
        boolean blnRtn = false;
        try {
            if (!mUsbDriver.isConnected()) {
                // USB线已经连接
                for (UsbDevice device : mUsbManager.getDeviceList().values()) {
                    if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                            || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                            || (device.getProductId() == PID15 && device.getVendorId() == VENDORID)) {
//						if (!mUsbManager.hasPermission(device)) {
//							break;
//						}
                        blnRtn = mUsbDriver.usbAttached(device);
                        if (blnRtn == false) {
                            break;
                        }
                        blnRtn = mUsbDriver.openUsbDevice(device);

                        // 打开设备
                        if (blnRtn) {
                            if (device.getProductId() == PID11) {
                                mUsbDev1 = device;
                                mUsbDev = mUsbDev1;
                            } else {
                                mUsbDev2 = device;
                                mUsbDev = mUsbDev2;
                            }
                            T.showShort(this, getString(R.string.USB_Driver_Success));
                            break;
                        } else {
                            T.showShort(this, getString(R.string.USB_Driver_Failed));
                            break;
                        }
                    }
                }
            } else {
                blnRtn = true;
            }
        } catch (Exception e) {
            T.showShort(this, e.getMessage());
        }
        return blnRtn;
    }


    private void setFullScreen() {
        //定义Windows 常量
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }


    private void checkNet() {
        Logger.d("checkNet");

        ConnectivityManager manager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
//            Toast.makeText(this, "网络连接连接", Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("开启网络服务");
            builder.setMessage("网络没有连接，请到设置进行网络设置！");
            builder.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if (android.os.Build.VERSION.SDK_INT > 10) {
                                // 3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                                startActivityForResult(new Intent(
                                        android.provider.Settings.ACTION_SETTINGS), TO_SETTING);
                            } else {
                                startActivityForResult(new Intent(
                                        android.provider.Settings.ACTION_WIRELESS_SETTINGS), TO_SETTING);
                            }
                            dialog.cancel();
                        }
                    });

            builder.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
            alertDialog = builder.show();
        }
    }

    /**
     * init webview
     */
    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void initWebview() {
        // init webview

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }

        webView = (WebView) findViewById(R.id.webview);


        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @TargetApi(21)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }
        });

        WebSettings webSettings = webView.getSettings();

        // add java script interface
        // note:if api level lower than 17(android 4.2), addJavascriptInterface has security
        // issue, please use x5 or see https://developer.android.com/reference/android/webkit/
        // WebView.html#addJavascriptInterface(java.lang.Object, java.lang.String)
        webSettings.setJavaScriptEnabled(true);
        webView.removeJavascriptInterface("searchBoxJavaBridge_");
//        intent.putExtra(SonicJavaScriptInterface.PARAM_LOAD_URL_TIME, System.currentTimeMillis());
//        webView.addJavascriptInterface(new SonicJavaScriptInterface(sonicSessionClient, intent), "sonic");

        //在js中调用本地java方法
        webView.addJavascriptInterface(new JsInterface(this), "AndroidWebView");

        // init webview settings
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(false);
        webSettings.setSavePassword(false);
        webSettings.setSaveFormData(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);


    }


    class UsbReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                if(mUsbDriver.usbAttached(intent))
                {
                    UsbDevice device = (UsbDevice) intent
                            .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                            || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                            || (device.getProductId() == PID15 && device.getVendorId() == VENDORID))
                    {
                        if(mUsbDriver.openUsbDevice(device))
                        {
                            if(device.getProductId()==PID11){
                                mUsbDev1 = device;
                                mUsbDev = mUsbDev1;
                            } else {
                                mUsbDev2 = device;
                                mUsbDev = mUsbDev2;
                            }
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent
                        .getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                        || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                        || (device.getProductId() == PID15 && device.getVendorId() == VENDORID))
                {
                    mUsbDriver.closeUsbDevice(device);
                    if(device.getProductId()==PID11)
                        mUsbDev1 = null;
                    else
                        mUsbDev2 = null;
                }
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this)
                {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
                    {
                        if ((device.getProductId() == PID11 && device.getVendorId() == VENDORID)
                                || (device.getProductId() == PID13 && device.getVendorId() == VENDORID)
                                || (device.getProductId() == PID15 && device.getVendorId() == VENDORID))
                        {
                            if (mUsbDriver.openUsbDevice(device)) {
                                if (device.getProductId() == PID11) {
                                    mUsbDev1 = device;
                                    mUsbDev = mUsbDev1;
                                } else {
                                    mUsbDev2 = device;
                                    mUsbDev = mUsbDev2;
                                }
                            }
                        }
                    }
                    else {
                        T.showShort(MainLocalActivity.this, "permission denied for device");
                        //Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    private class JsInterface {
        final private Context mContext;

        public JsInterface(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void sendToPrinter(String JSONData) {
            try {
                queuePrintingBuffer = new JSONObject(JSONData);
                //********************* Start Printing Task
                Bills.printSmallTicket(mUsbDriver,title,num,codeStr,cutter, queuePrintingBuffer); // 排队票据
                //printQueue();
                //*******************************************
                //final boolean bPrintResult = Prints.PrintTicket(getApplicationContext(), pos, queuePrintingBuffer);

                Toast.makeText(MainLocalActivity.this, "cannot print", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                queuePrintingBuffer = null;
                Toast.makeText(mContext, "队列数据格式无效", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

//    private void printQueue() {
//        if (mDevice == null) {
//            deviceList = mUsbManager.getDeviceList();
//            deviceIterator = deviceList.values().iterator();
//            findPrinter();
//            return;
//        }

//        try {
//            es.submit(new TaskPrint(mPos, queuePrintingBuffer));
//
//            queuePrintingBuffer = null;
//            //es.submit(new TaskClose(mUSB));
//        } catch (Exception e) {
//            Toast.makeText(MainLocalActivity.this, "队列数据解释无效，不能打印", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//            return;
//        }
//    }

    private void loadUrl() {
        webView.loadUrl(urlString);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d("onPause");
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d("onStart");
        checkNet();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Logger.d("onMessageEvent");
        alertDialog.dismiss();
        initWebview();
        loadUrl();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && webView.canGoBack()) {
            webView.goBack();// 返回前一个页面
            canGoBack = true;
            return true;
        } else {
            canGoBack = false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (canGoBack) {
                    return true;
                }
                long secondTime = System.currentTimeMillis();
                if (secondTime - firstTime > 2000) {
                    Toast.makeText(MainLocalActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                } else {
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Logger.d("onRestart");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TO_SETTING) {
            Logger.d("onActivityResult");
            alertDialog.dismiss();
            initWebview();
            loadUrl();
        }
    }

    /***
     * 一次只找一个设备
     */
//    private void findPrinter() {
//        if (deviceIterator.hasNext()) {
//            final UsbDevice device = deviceIterator.next();
//            if (mUsbManager.hasPermission(device)) {
//                Logger.d("尝试连接打印机");
//                Logger.d(device);
//                mDevice = device;
//                es.submit(new TaskOpen(mUSB, mUsbManager, device, mActivity));
//            } else {
//                PendingIntent mPermissionIntent = PendingIntent
//                        .getBroadcast(
//                                MainLocalActivity.this,
//                                0,
//                                new Intent(MainLocalActivity.this.getApplicationInfo().packageName), 0);
//                mUsbManager.requestPermission(device, mPermissionIntent);
//                //todo 授权后怎么响应
//            }
//        }
//    }

    // MD5 加密public
    public static String getStringMD5(String sourceStr) {
        String s = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            //这两行代码的作用是：
            // 将bytes数组转换为BigInterger类型。1，表示 +，即正数。
            BigInteger bigInt = new BigInteger(1, md.digest(sourceStr.getBytes()));
            // 通过format方法，获取32位的十六进制的字符串。032,代表高位补0 32位，X代表十六进制的整形数据。
            //为什么是32位？因为MD5算法返回的时一个128bit的整数，我们习惯于用16进制来表示，那就是32位。
            s = String.format("%032x", bigInt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return s;
    }
//
//    public class TaskOpen implements Runnable {
//        USBPrinting usb = null;
//        UsbManager usbManager = null;
//        UsbDevice usbDevice = null;
//        MainLocalActivity activity;
//
//        public TaskOpen(USBPrinting usb, UsbManager usbManager, UsbDevice usbDevice, MainLocalActivity activity) {
//            this.usb = usb;
//            this.usbManager = usbManager;
//            this.usbDevice = usbDevice;
//            this.activity = activity;
//        }

//        public void run() {
//            // TODO Auto-generated method stub
//            usb.Open(usbManager, usbDevice, activity.getApplicationContext());
//        }
//    }

//    public class TaskPrint implements Runnable {
//        Pos pos = null;
//        JSONObject queuePrintingBuffer = null;
//
//
//        public TaskPrint(Pos pos, JSONObject queuePrintingBuffer) {
//            this.pos = pos;
//            this.queuePrintingBuffer = queuePrintingBuffer;
//
//        }

//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//
//            final boolean bPrintResult = Prints.PrintTicket(getApplicationContext(), pos, queuePrintingBuffer);
//            final boolean bIsOpened = pos.GetIO().IsOpened();
//
//            mActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    // TODO Auto-generated method stub
//                    //Toast.makeText(mActivity.getApplicationContext(), bPrintResult ? getResources().getString(R.string.printsuccess) : getResources().getString(R.string.printfailed), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        }
//    }

//    public class TaskClose implements Runnable {
//        USBPrinting usb = null;
//
//        public TaskClose(USBPrinting usb) {
//            this.usb = usb;
//        }
//
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//            usb.Close();
//        }
//
//    }


//    private void printtest() {
//        if (mDevice == null) {
//            deviceList = mUsbManager.getDeviceList();
//            deviceIterator = deviceList.values().iterator();
//            findPrinter();
//            return;
//        }
//
//        try {
//
//
//            queuePrintingBuffer = null;
//            //es.submit(new TaskClose(mUSB));
//        } catch (Exception e) {
//            Toast.makeText(MainLocalActivity.this, "队列数据解释无效，不能打印", Toast.LENGTH_SHORT).show();
//            e.printStackTrace();
//            return;
//        }
//    }

//    public class TasktestPrint implements Runnable {
//        Pos pos = null;
//        JSONObject queuePrintingBuffer = null;
//
//
//        public TasktestPrint(Pos pos, JSONObject queuePrintingBuffer) {
//            this.pos = pos;
//            this.queuePrintingBuffer = queuePrintingBuffer;
//
//        }
//
//        @Override
//        public void run() {
//            // TODO Auto-generated method stub
//
//            final boolean bPrintResult = Prints.Printtest(getApplicationContext(), pos);
//            final boolean bIsOpened = pos.GetIO().IsOpened();
//
//            mActivity.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    // TODO Auto-generated method stub
//                    //Toast.makeText(mActivity.getApplicationContext(), bPrintResult ? getResources().getString(R.string.printsuccess) : getResources().getString(R.string.printfailed), Toast.LENGTH_SHORT).show();
//                }
//            });
//
//        }
//    }

    /**
     * Handler 定时刷新
     */
//    private Handler mHanlder = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case 1:
//                    findPrinter();
//                    break;
//                default:
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };

//    private Runnable task = new Runnable() {
//        @Override
//        public void run() {
//            /**
//             * 此处执行任务
//             * */
//            mHanlder.sendEmptyMessage(1);
//            mHanlder.postDelayed(this, 3 * 1000);//延迟3秒,再次执行task本身,实现了循环的效果
//        }
//    };


//    @Override
//    public void OnOpen() {
//        // TODO Auto-generated method stub
//        this.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
////                mDevice = this.usbDevice;
//                mHanlder.removeCallbacks(task);
//                es.submit(new TasktestPrint(mPos, queuePrintingBuffer));
//                Logger.d("连接打印机成功");
//                printQueue();
//                Toast.makeText(mActivity, "Printer Connected", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    @Override
//    public void OnOpenFailed() {
//        // TODO Auto-generated method stub
//        this.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//                Logger.d("连接打印机失败");
//                mDevice = null;
//                //findPrinter();
//                Toast.makeText(mActivity, "Failed to open printer", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    @Override
//    public void OnClose() {
//        // TODO Auto-generated method stub
//        this.runOnUiThread(new Runnable() {
//
//            @Override
//            public void run() {
//
//            }
//        });
//    }

}
