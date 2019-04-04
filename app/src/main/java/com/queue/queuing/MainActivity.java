package com.queue.queuing;

import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
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
import android.widget.Toast;
import android.content.res.AssetManager;
import android.graphics.drawable.BitmapDrawable;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.orhanobut.logger.Logger;
import com.queue.queuing.Beans.Setting;
import com.queue.queuing.Dao.SettingDao;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;


import com.queue.queuing.sdk.Command;
import com.queue.queuing.sdk.PrintPicture;
import com.queue.queuing.sdk.PrinterCommand;
import com.queue.queuing.sdk.ImageAdjust;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zj.wfsdk.*;


public class MainActivity extends Activity {

    private static final int TO_SETTING = 1;
//    private SonicSession sonicSession;
//    private SonicSessionClientImpl sonicSessionClient;

    //SettingDao settingDao;


    private String urlString = "https://pos.auroratech.top/queue/index.html"; // release

    private WebView webView;

    //记录用户首次点击返回键的时间
    private long firstTime = 0;

    private boolean canGoBack = false;

    private AlertDialog alertDialog;


    private WifiCommunication wfComm = null;

    int  connFlag = 0;
    revMsgThread revThred = null;
    //checkPrintThread cheThread = null;
    private static final int WFPRINTER_REVMSG = 0x06;

    //QRcode
    private static final int QR_WIDTH = 250;
    private static final int QR_HEIGHT = 250;

    private JSONObject queuePrintingBuffer = null;

    private Setting setting = null;
    /******************************************************************************************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        //静止软件盘
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

        SettingDao settingDao = new SettingDao(MainActivity.this);
        setting = settingDao.queryById(1);
        urlString = urlString + "?user_id=" + setting.getUserId();
        //开启硬件加速
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setFullScreen();
//        createSonicSession();

        setContentView(R.layout.activity_main);
        initWebview();
        loadUrl();
        wfComm = new WifiCommunication(mHandler);

        revThred = new revMsgThread();
        revThred.start();
    }

    private void setFullScreen() {

        //定义Windows 常量
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

//    private void createSonicSession() {
//        sonicSessionClient = null;
//        SonicSessionConfig.Builder sessionConfigBuilder = new SonicSessionConfig.Builder();
//        sessionConfigBuilder.setSupportLocalServer(true);
//
//        sonicSession = SonicEngine.getInstance().createSession(urlString, sessionConfigBuilder.build());
//        if (null != sonicSession) {
//            sonicSession.bindClient(sonicSessionClient = new SonicSessionClientImpl());
//        } else {
//            // this only happen when a same sonic session is already running,
//            // u can comment following codes to feedback as a default mode.
//            // throw new UnknownError("create session fail!");
////            Toast.makeText(this, "create sonic session fail!", Toast.LENGTH_LONG).show();
//            Logger.d("Warning:" + "create sonic session fail!");
//
//        }
//    }


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
//                if (sonicSession != null) {
//                    sonicSession.getSessionClient().pageFinish(url);
//                }
            }

            @TargetApi(21)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return shouldInterceptRequest(view, request.getUrl().toString());
            }

//            @Override
//            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
////                if (sonicSession != null) {
////                    return (WebResourceResponse) sonicSession.getSessionClient().requestResource(url);
////                }
//                return null;
//            }
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

    private class JsInterface {
        final private Context mContext;

        public JsInterface(Context context) {
            this.mContext = context;
        }

        @JavascriptInterface
        public void sendToPrinter(String JSONData) {
            try {
                queuePrintingBuffer = new JSONObject(JSONData);
                printQueue();
            } catch (Exception e) {
                queuePrintingBuffer = null;
                Toast.makeText(mContext, "队列数据格式无效", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void connectPrinter() {
        if (connFlag==0) {
            wfComm.initSocket(setting.getPrinterIP(), 9100);
        }
    }

    private void printQueue() {
        //连接打印机
        if (connFlag==0) {
            connectPrinter();
            return;
        }
        if (queuePrintingBuffer ==  null) {
            return;
        }
        Toast.makeText(MainActivity.this, "正在发送打印机", Toast.LENGTH_SHORT).show();
        new Thread(networkTask).start();
    }

    /**
     * 网络操作相关的子线程
     */
    Runnable networkTask = new Runnable() {
        @Override
        public void run() {
            String store_name = "";
            String store_tel = "";
            String store_address = "";
            String line_number = "";
            String line_people = "";
            String line_num = "";
            String line_qrcode = "";
            String line_start_time = "";
            String language="";
            try {
                store_name = queuePrintingBuffer.getString("store_name");
                store_tel = queuePrintingBuffer.getString("store_tel");
                store_address = queuePrintingBuffer.getString("store_address");
                line_number = queuePrintingBuffer.getString("number");
                line_people = queuePrintingBuffer.getString("people");
                line_num = queuePrintingBuffer.getString("num");
                line_qrcode = queuePrintingBuffer.getString("qrcode");
                line_start_time = queuePrintingBuffer.getString("start_time");
                language = queuePrintingBuffer.getString("language");
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "队列数据解释无效，不能打印", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            SendDataByte(Command.FS_and); //中文OK
            SendDataByte(Command.FS_ExclamationMark); //中文OK
            //is58mm


            int papersize=setting.getPaperSize();

            if (papersize==58) {
                try {
                    if (language.equals("CN")) {
                        Command.ESC_Align[2] = 0x01;
                        SendDataByte(Command.ESC_Align);
                        Command.GS_ExclamationMark[2] = 0x11;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte((Html.fromHtml(store_name) + "\n").getBytes("GBK"));

                        Command.GS_ExclamationMark[2] = 0x00;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("取号时间：" + line_start_time + "\n").getBytes("GBK"));
                        SendDataByte("-------------------------------------------\n".getBytes("GBK"));

                        Command.GS_ExclamationMark[2] = 0x11;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte((line_number + "号\n").getBytes("GBK"));

                        Command.GS_ExclamationMark[2] = 0x00;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("就餐人数：" + line_people + "\n").getBytes("GBK"));

                        SendDataByte(("您的前面还有：" + line_num + "位在等待\n\n").getBytes("GBK"));

                        if (!line_qrcode.isEmpty()) {
                            printQRcode(line_qrcode);
//                        Bitmap mBitmap = getImageFromURL(line_qrcode);
//                        Bitmap backBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                                R.drawable.back2);
//                        Bitmap mNewbitmap=combineBitmap(backBitmap,mBitmap);
//                        int nMode = 0;
//                        //        int nPaperWidth = 384; //58mm
//                        int nImageWidth = 576; //80mm
//                        if (mBitmap != null) {
//
//                            byte[] data = PrintPicture.POS_PrintBMP(mNewbitmap , nImageWidth, nMode);
//                            SendDataByte(data);
//                        }
                        }


                        //wfComm.sndByte(new byte[]{0x1b, 0x4a, 0x30, 0x1d, 0x56, 0x42, 0x01});


                        Command.ESC_Align[2] = 0x01;
                        SendDataByte(Command.ESC_Align);
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("关注即时排队动态，请扫取上方二维码\n\n").getBytes("GBK"));

                        Command.ESC_Align[2] = 0x00;
                        SendDataByte(Command.ESC_Align);
                        Command.GS_ExclamationMark[2] = 0x00;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("商家电话: " + store_tel + "\n").getBytes("GBK"));
                        SendDataByte(("地址: " + store_address + "\n").getBytes("GBK"));

                        SendDataByte("-------------------------------------------\n".getBytes("GBK"));
                        SendDataByte("本系统由鹿大胃提供 联系电话647-7729-729\n\n".getBytes("GBK"));

                        SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
                        SendDataByte(Command.GS_V_m_n);

                        queuePrintingBuffer = null;
                    }else{
                        Command.ESC_Align[2] = 0x01;
                        SendDataByte(Command.ESC_Align);
                        Command.GS_ExclamationMark[2] = 0x11;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte((Html.fromHtml(store_name) + "\n").getBytes("GBK"));

                        Command.GS_ExclamationMark[2] = 0x00;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("取号时间：" + line_start_time + "\n").getBytes("GBK"));
                        SendDataByte("-------------------------------------------\n".getBytes("GBK"));

                        Command.GS_ExclamationMark[2] = 0x11;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte((line_number + "号\n").getBytes("GBK"));

                        Command.GS_ExclamationMark[2] = 0x00;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("就餐人数：" + line_people + "\n").getBytes("GBK"));

                        SendDataByte(("您的前面还有：" + line_num + "位在等待\n\n").getBytes("GBK"));

                        if (!line_qrcode.isEmpty()) {
                            printQRcode(line_qrcode);
//                        Bitmap mBitmap = getImageFromURL(line_qrcode);
//                        Bitmap backBitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(),
//                                R.drawable.back2);
//                        Bitmap mNewbitmap=combineBitmap(backBitmap,mBitmap);
//                        int nMode = 0;
//                        //        int nPaperWidth = 384; //58mm
//                        int nImageWidth = 576; //80mm
//                        if (mBitmap != null) {
//
//                            byte[] data = PrintPicture.POS_PrintBMP(mNewbitmap , nImageWidth, nMode);
//                            SendDataByte(data);
//                        }
                        }


                        //wfComm.sndByte(new byte[]{0x1b, 0x4a, 0x30, 0x1d, 0x56, 0x42, 0x01});


                        Command.ESC_Align[2] = 0x01;
                        SendDataByte(Command.ESC_Align);
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("关注即时排队动态，请扫取上方二维码\n\n").getBytes("GBK"));

                        Command.ESC_Align[2] = 0x00;
                        SendDataByte(Command.ESC_Align);
                        Command.GS_ExclamationMark[2] = 0x00;
                        SendDataByte(Command.GS_ExclamationMark);
                        SendDataByte(("商家电话: " + store_tel + "\n").getBytes("GBK"));
                        SendDataByte(("地址: " + store_address + "\n").getBytes("GBK"));

                        SendDataByte("-------------------------------------------\n".getBytes("GBK"));
                        SendDataByte("本系统由鹿大胃提供 联系电话647-7729-729\n\n".getBytes("GBK"));

                        SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
                        SendDataByte(Command.GS_V_m_n);

                        queuePrintingBuffer = null;

                    }



                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "打印碰到异常", Toast.LENGTH_SHORT).show();
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else {
                try {

                    queuePrintingBuffer = null;
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "打印碰到异常", Toast.LENGTH_SHORT).show();
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    };

    private void loadUrl() {
//        if (sonicSessionClient != null) {
//            sonicSessionClient.bindWebView(webView);
//            sonicSessionClient.clientReady();
//        } else { // default mode
            webView.loadUrl(urlString);
//        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.d("onPause");
        //连接打印机
        wfComm.close();
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d("onStart");
        checkNet();
        EventBus.getDefault().register(this);
        //连接打印机
        connectPrinter();
    }
    @Override
    protected void onResume(){
        super.onResume();
        if (connFlag==0) {
            wfComm.initSocket(setting.getPrinterIP(), 9100);
        }
        //连接打印机
        connectPrinter();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        wfComm.close();
        revThred.interrupt();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(MessageEvent event) {
        Logger.d("onMessageEvent");
        alertDialog.dismiss();
//        createSonicSession();
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
                    Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
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
        //连接打印机
        connectPrinter();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TO_SETTING) {
            Logger.d("onActivityResult");
            alertDialog.dismiss();
//            createSonicSession();
            initWebview();
            loadUrl();
        }
    }

    /***********************************************************************************************/
    @SuppressLint("HandlerLeak") private final  Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiCommunication.WFPRINTER_CONNECTED:
                    connFlag = 1;
                    Toast.makeText(getApplicationContext(), "Connect the WIFI-printer successful",
                            Toast.LENGTH_SHORT).show();
                    printQueue();
                    break;
                case WifiCommunication.WFPRINTER_DISCONNECTED:
                    connFlag = 0;
                    break;
                case WifiCommunication.SEND_FAILED:
                    connFlag = 0;
                    Toast.makeText(getApplicationContext(), "Send Data Failed,please reconnect",
                            Toast.LENGTH_SHORT).show();
                    wfComm.close();
                    break;
                case WifiCommunication.WFPRINTER_CONNECTEDERR:
                    connFlag = 0;
                    Toast.makeText(getApplicationContext(), "Connect the WIFI-printer error1",
                            Toast.LENGTH_SHORT).show();
                    wfComm.close();

                    break;
                case WFPRINTER_REVMSG:
                    byte revData = (byte)Integer.parseInt(msg.obj.toString());
                    if(((revData >> 6) & 0x01) == 0x01)
                        Toast.makeText(getApplicationContext(), "The printer has no paper",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    //打印机线程，连接上打印机时创建，关闭打印机时退出
    class revMsgThread extends Thread {
        @Override
        public void run() {
            try {
                Message msg = new Message();
                int revData;
                while(true) {
                    if (connFlag == 1) {
                        revData = wfComm.revByte();               //非阻塞单个字节接收数据，如需改成非阻塞接收字符串请参考手册
                        if (revData != -1) {
                            msg = mHandler.obtainMessage(WFPRINTER_REVMSG);
                            msg.obj = revData;
                            mHandler.sendMessage(msg);
                            Thread.sleep(500);
                        } else {
                            Thread.sleep(1000);
                        }
                    }
                    else{
                        Thread.sleep(2000);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d("wifi调试","退出线程");
            }
        }
    }
    /****************************************************************************************************/

    /*
     * 打印图片
     */
    private void Print_BMP(ImageView imageViewPicture){

        byte[] buffer = PrinterCommand.POS_Set_PrtInit();
        Bitmap mBitmap = ((BitmapDrawable) imageViewPicture.getDrawable())
                .getBitmap();
        int nMode = 0;
//        int nPaperWidth = 384; //58mm
        int nPaperWidth = 576; //80mm
        if(mBitmap != null)
        {
            byte[] data = PrintPicture.POS_PrintBMP(mBitmap, nPaperWidth, nMode);
            wfComm.sndByte(buffer);
            wfComm.sndByte(data);
            wfComm.sndByte(new byte[]{0x1b, 0x4a, 0x30, 0x1d, 0x56, 0x42, 0x01});
        }
    }

    /************************************************************************************************/
    private void SendDataByte(byte[] data){
        if(data.length>0){
            wfComm.sndByte(data);
        }
    }

    private void SendDataString(String data){
        if(data.length()>0)
            wfComm.sendMsg(data, "GBK");
    }

    //
    private void SendDataBig5(String data) {

        if (data.length() > 0) {
            wfComm.sendMsg(data, "BIG5");
        }
    }

    private void SendDataThai(String data) {

        if (data.length() > 0) {
            wfComm.sendMsg(data, "CP874");
        }
    }

    private void SendDataKor(String data) {

        if (data.length() > 0) {
            wfComm.sendMsg(data, "EUC-KR");
        }
    }
    /************************************************************************************************/
    /*
     * 生成QR图
     */
    private void printQRcode(String text) {
        try {
            // 需要引入zxing包
            QRCodeWriter writer = new QRCodeWriter();

            //   Log.i(TAG, "生成的文本：" + text);
            if (text == null || "".equals(text) || text.length() < 1) {
                Toast.makeText(this, getText(R.string.empty), Toast.LENGTH_SHORT).show();
                return;
            }

            // 把输入的文本转为二维码
            BitMatrix martix = writer.encode(text, BarcodeFormat.QR_CODE,
                    QR_WIDTH, QR_HEIGHT);

            System.out.println("w:" + martix.getWidth() + "h:"
                    + martix.getHeight());

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            BitMatrix bitMatrix = new QRCodeWriter().encode(text,
                    BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);
            int[] pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; y++) {
                for (int x = 0; x < QR_WIDTH; x++) {
                    if (bitMatrix.get(x, y)) {
                        pixels[y * QR_WIDTH + x] = 0xff000000;
                    } else {
                        pixels[y * QR_WIDTH + x] = 0xffffffff;
                    }

                }
            }

            Bitmap bitmap = Bitmap.createBitmap(QR_WIDTH, QR_HEIGHT,
                    Bitmap.Config.ARGB_8888);

            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);
            //imageViewPicture.setImageBitmap(bitmap);

            byte[] data = PrintPicture.POS_PrintBMP(bitmap, QR_WIDTH, 0);
            SendDataByte(data);
            //SendDataByte(new byte[]{0x1b, 0x4a, 0x30, 0x1d, 0x56, 0x42, 0x01 });
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
    //************************************************************************************************//
    /*
     * 调用系统相机
     */
    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, actionCode);
    }

    private void handleSmallCameraPhoto(Intent intent) {
        Bundle extras = intent.getExtras();
        Bitmap mImageBitmap = (Bitmap) extras.get("data");
        //imageViewPicture.setImageBitmap(mImageBitmap);
    }
/****************************************************************************************************/
    /**
     * 加载assets文件资源
     */
    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;

    }

    /**
     * 加载网络图片
     */
    private Bitmap getImageFromURL(String fileUrl) {
        Bitmap image = null;

        try {
            byte[] data=ImageAdjust.getImage(fileUrl);
            Bitmap originImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            image = ImageAdjust.moveBitmap(originImage, 0,  0);
           //image = ImageAdjust.scaleBitmap(originImage,1.8f);
            //image =ImageAdjust.cropBitmap(originImage);
             //image=ImageAdjust.scaleBitmap(originImage,100,72);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
       * 合并两张bitmap为一张
       * @param background
       * @param foreground
       * @return Bitmap
       */
public static Bitmap combineBitmap(Bitmap background, Bitmap foreground){
   int bgWidth = background.getWidth();
   int bgHeight = background.getHeight();
   int fgWidth = foreground.getWidth();
   int fgHeight = foreground.getHeight();
    Bitmap newmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(newmap);
    canvas.drawBitmap(background, 0, 0, null);
    canvas.drawBitmap(foreground, (bgWidth - fgWidth) / 2, (bgHeight - fgHeight) /3, null);
    canvas.save(Canvas.ALL_SAVE_FLAG);
    canvas.restore();
    return newmap;
 }

}
