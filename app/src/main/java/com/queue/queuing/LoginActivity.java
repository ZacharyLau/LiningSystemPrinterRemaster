package com.queue.queuing;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Hashtable;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.CallSuper;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.queue.queuing.Beans.Setting;
import com.queue.queuing.Dao.SettingDao;
import com.queue.queuing.Utils.CommonUtil;
import com.queue.queuing.Utils.OkManager;
import com.zj.wfsdk.WifiCommunication;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLException;

import com.queue.queuing.sdk.Command;
import com.queue.queuing.sdk.PrintPicture;
import com.queue.queuing.sdk.PrinterCommand;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.zj.wfsdk.*;
import zj.com.customize.sdk.Other;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements View.OnClickListener {
    // UI references.
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mPrinterIPView;
    private CheckBox cb_remPassword;
    //private RadioGroup radioGrouppinter;
    //private RadioGroup radioGrouppapersize;
    private  RadioButton rb_papersize58;
    private RadioButton rb_papersize80;
    private  RadioButton rb_localprinter;
    private RadioButton rb_networkprinter;
    SettingDao settingDao;
    private Button btn_login;
    private Button btn_test;
    private OkManager manager;
    String TAG = "LoginActivity";
    String userName;
    String passWord;
    String printerIP;

    private ConstraintLayout loginpage;

    private WifiCommunication wfComm = null;
    revMsgThread revThred = null;
    //checkPrintThread cheThread = null;
    private static final int WFPRINTER_REVMSG = 0x06;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //静止虚拟按键

        mUsernameView = (EditText) findViewById(R.id.Username_Edt);
        mPasswordView = (EditText) findViewById(R.id.Password_Edt);
        mPrinterIPView = (EditText) findViewById(R.id.PrinterIP_Edt);
        cb_remPassword = (CheckBox) findViewById(R.id.cb_remPassword);

        //radioGrouppinter = (RadioGroup) findViewById(R.id.radiogroup_pinter);
        //radioGrouppapersize = (RadioGroup) findViewById(R.id.radiogroup_papersize);
        //radioGrouppapersize.setOnCheckedChangeListener(this);
         rb_papersize58 = (RadioButton) findViewById(R.id.radio_papersize58);
         rb_papersize80 = (RadioButton) findViewById(R.id.radio_papersize80);

        rb_localprinter = (RadioButton) findViewById(R.id.radio_LocalPinter);
        rb_networkprinter = (RadioButton) findViewById(R.id.radio_NetworkPinter);

        btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(this);
        btn_test = (Button) findViewById(R.id.btn_test);
        btn_test.setOnClickListener(this);
        settingDao = new SettingDao(LoginActivity.this);
        //实体化，数据的传输
        manager = OkManager.getInstance();

        wfComm = new WifiCommunication(mHandler);


        initEvent();

        try {
            HistoryUserSet();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    //开始的设置好的状态下
    private void HistoryUserSet() throws SQLException {
        Setting setting = settingDao.queryById(1);
        mPrinterIPView.setText(setting.getPrinterIP());
        mUsernameView.setText(setting.getUserName());

        if (setting.getPaperSize()==58) {
            rb_papersize58.setChecked(true);
        } else {
            rb_papersize80.setChecked(true);
        }

        if (setting.getnetworkprinter()==0) {
            rb_localprinter.setChecked(true);
        } else {
            rb_networkprinter.setChecked(true);
        }

        if (Boolean.valueOf(setting.getRemPassword())) {
            mPasswordView.setText(setting.getPassword());
            if (Boolean.valueOf(setting.getAutoLogin())) {
                Login();
            }
        }
    }





    private void initEvent() {
        //btn_login.setOnClickListener(this);
        if (settingDao.queryById(1) == null) {
            Setting setting = new Setting(1, "false", "false",0,1);
            settingDao.insert(setting);
        } else {
            Setting setting = settingDao.queryById(1);
            if (Boolean.valueOf(setting.getRemPassword())) {
                cb_remPassword.setChecked(true);
            } else {
                cb_remPassword.setChecked(false);
            }

        }
        // 复选框的判断
        cb_remPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    Setting setting = settingDao.queryById(1);
                    setting.setRemPassword("true");
                    settingDao.update(setting);
                } else {
                    Setting setting = settingDao.queryById(1);
                    setting.setRemPassword("false");
                    setting.setAutoLogin("false");
                    settingDao.update(setting);
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                Login();
                break;
            case R.id.btn_test:
                String IP = mPrinterIPView.getText().toString();
                wfComm.initSocket(IP, 9100);
        }
    }

    //radio button choose papersize
    public void onRadioButtonClickedpapersize(View view) {

        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.radio_papersize58:
                if (checked)
                    Toast.makeText(getApplicationContext(), "58", Toast.LENGTH_SHORT).show();
                Setting setting = settingDao.queryById(1);
                setting.setPaperSize(58);
                settingDao.update(setting);
                break;
            case R.id.radio_papersize80:
                if (checked)
                    Toast.makeText(getApplicationContext(), "80", Toast.LENGTH_SHORT).show();
                Setting setting2 = settingDao.queryById(1);
                setting2.setPaperSize(80);
                settingDao.update(setting2);
                break;
        }
    }

    public void onRadioButtonClickednetworkprinter(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        switch(view.getId()) {
            case R.id.radio_LocalPinter:
                if (checked)
                    Toast.makeText(getApplicationContext(), "Localprinter", Toast.LENGTH_SHORT).show();
                Setting setting = settingDao.queryById(1);
                setting.setnetworkprinter(0);
                settingDao.update(setting);

                mPrinterIPView.setFocusable(false);
                mPrinterIPView.setFocusableInTouchMode(false);
                break;
            case R.id.radio_NetworkPinter:
                if (checked)
                    Toast.makeText(getApplicationContext(), "Networkprinter", Toast.LENGTH_SHORT).show();
                Setting setting2 = settingDao.queryById(1);
                setting2.setnetworkprinter(1);
                settingDao.update(setting2);

                mPrinterIPView.setFocusableInTouchMode(true);
                mPrinterIPView.setFocusable(true);
                mPrinterIPView.requestFocus();
                break;
        }
    }

    private void Login() {
        wfComm.close();
        //控制显示或隐藏输入法面板的类
        //打开软键盘
        //InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

        //InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //Toast.makeText(getApplicationContext(), "没有键盘", Toast.LENGTH_SHORT).show();
        userName = mUsernameView.getText().toString();
        passWord = mPasswordView.getText().toString();
        printerIP = mPrinterIPView.getText().toString();
        //创捷 json
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("username", userName);
            jsonObj.put("password", passWord);
        } catch( JSONException e) {
        }
       // 传输地址
        String jsonpath = CommonUtil.BaseUrl + "auth/device/login";

        //登陆同步用户数据
        // ok manager
        manager.sendJSONByPost(jsonpath, jsonObj.toString(), new OkManager.Fun4() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Log.i("LoginActivityOnResponse", jsonObject.toString());   //获取JSON字符串
                try {
                    if (jsonObject.has("status")) {
                        if (jsonObject.get("status").toString() .equals("success") ) {
                            JSONObject data = new JSONObject(jsonObject.get("data").toString());
                            String token = data.getString("access_token");
                            String tokenType = data.getString("token_type");
                            String userId = data.getString("user_id");

                            Setting setting = settingDao.queryById(1);
                            setting.setUserName(userName);
                            setting.setPassword(passWord);
                            setting.setPrinterIP(printerIP);
                            setting.setUserId(userId);
                            setting.setToken(tokenType + ' ' + token);
                            settingDao.update(setting);


                                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                                mInputMethodManager.hideSoftInputFromWindow(btn_login.getWindowToken(), 0);
                            if (setting.getnetworkprinter()==1) {

                                Intent intent = new Intent();
                                intent.putExtra("extra", setting.getUserName());
                                intent.setClass(LoginActivity.this, MainActivity.class);      //运行另外一个类的活动
                                startActivityForResult(intent, 1);
                            }else{

                                Intent intent = new Intent();
                                intent.putExtra("extra", setting.getUserName());
                                intent.setClass(LoginActivity.this, MainLocalActivity.class);
                                 //intent.setClass(LoginActivity.this, TestActivity.class);
                                // 运行另外一个类的活动 暂时不用
                                startActivityForResult(intent, 1);

                            }
                        }else{
                            if (jsonObject.has("message")) {
                                Toast.makeText(getApplicationContext(), jsonObject.get("message").toString(), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "服务器返回错误", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "服务器返回错误", Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(getApplicationContext(), "无法访问服务器", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(JSONObject jsonObject) {
                Log.i("LoginActivityOnFailure", jsonObject.toString());   //获取JSON字符串
                try {
                    String error = jsonObject.getString("error");
                    Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 网络操作相关的子线程
     */

    //Runnable更容易实现资源共享，能多个线程同时处理一个资源
    Runnable networkTask = new Runnable() {

        @Override
        public void run() {
            try {
                SendDataByte(Command.FS_and); //中文OK
                SendDataByte(Command.FS_ExclamationMark); //中文OK
                SendDataByte(Command.ESC_Align);
                Command.GS_ExclamationMark[2] = 0x11;
                SendDataByte(Command.GS_ExclamationMark);
                SendDataBig5("打印机工作正常\n");
                SendDataByte(PrinterCommand.POS_Set_PrtAndFeedPaper(48));
                SendDataByte(Command.GS_V_m_n);

            } catch (Exception e) {
                Toast.makeText(LoginActivity.this, "打印碰到异常", Toast.LENGTH_SHORT).show();
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

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
            wfComm.sendMsg(data, "GBK");
        }
    }

   // ？？
    /***********************************************************************************************/
    @SuppressLint("HandlerLeak") private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiCommunication.WFPRINTER_CONNECTED:
                    Toast.makeText(getApplicationContext(), "Connect the WIFI-printer successful",
                            Toast.LENGTH_SHORT).show();

                    revThred = new revMsgThread();
                    revThred.start();

                    new Thread(networkTask).start();
                    break;
                case WifiCommunication.WFPRINTER_DISCONNECTED:
                    revThred.interrupt();
                    break;
                case WifiCommunication.SEND_FAILED:
                    revThred.interrupt();
                    wfComm.close();
                    break;
                case WifiCommunication.WFPRINTER_CONNECTEDERR:
                    Toast.makeText(getApplicationContext(), "连接打印机失败，请重启打印机",Toast.LENGTH_SHORT).show();
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


                    revData = wfComm.revByte();               //非阻塞单个字节接收数据，如需改成非阻塞接收字符串请参考手册
                    if(revData != -1){
                        msg = mHandler.obtainMessage(WFPRINTER_REVMSG);
                        msg.obj = revData;
                        mHandler.sendMessage(msg);
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.d("wifi调试","退出线程");
            }
        }
    }
    public boolean onTouchEvent(MotionEvent event) {
        if(null != this.getCurrentFocus()){
            /**
             * 点击空白位置 隐藏软键盘
             */
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super .onTouchEvent(event);
    }

    public void CheckToken(JSONObject jsonObject) {
        Log.i("LoginActivityOnResponse", jsonObject.toString());   //获取JSON字符串
        try {
            if (jsonObject.has("status")) {
                if (jsonObject.get("status").toString() .equals("success") ) {
                    JSONObject data = new JSONObject(jsonObject.get("data").toString());
                    String token = data.getString("access_token");
                    String tokenType = data.getString("token_type");
                    String userId = data.getString("user_id");

                }else{
                    if (jsonObject.has("message")) {
                        Toast.makeText(getApplicationContext(), jsonObject.get("message").toString(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "服务器返回错误", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(getApplicationContext(), "服务器返回错误", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "无法访问服务器", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}