package com.queue.queuing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.queue.queuing.util.Bills;
import com.queue.queuing.util.Constant;
import com.queue.queuing.util.MyAdapter;
import com.queue.queuing.util.T;
import com.queue.queuing.util.Utils;
import com.printsdk.cmd.PrintCmd;
import com.printsdk.usbsdk.UsbDriver;
import com.printsdk.utils.PrintUtils;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@SuppressLint("SimpleDateFormat")
@SuppressWarnings("unused")
public class TestActivity extends Activity {
	private static final int FILE_SELECT_CODE = 0;  
	private Context mContext;
	final int SERIAL_BAUDRATE = UsbDriver.BAUD115200;
	UsbDriver mUsbDriver;
	UsbDevice mUsbDev1;		//打印机1
	UsbDevice mUsbDev2;		//打印机2
	UsbDevice mUsbDev;
	// Control definition 控件定义
	private Button mInputPrint_btn,mPrintSelfPage,mPrinterInfo,mPrintTest,mPrintTicket,mCheckStatus,mClear;
	private Button setBlackMarkADBtn,setBlackMarkOffset;
	private Button mBmpLoad,mBmpPrint,mClearPath,mAddBmpFilePath,mAddImgFilePath;
	private EditText etWrite,editRecDisp,mBmpPath_et,mImgPath_et;
	private EditText m1DBarType,mQrCodeData,mQrCodeLeft,mQrCodeSize;
	private CheckBox IsAddLoadBmpPath;
	// 代表性的支持安卓USB口打印机QrCode函数调用：MS-D347、MS-D245（N58V）、T500II
	private Button D347QrBtn,D245QrBtn,T500IIQrBtn;
	private Button print1DBarBtn;
	private Button printQrCode;
	private Button printPdf417;
	private Button printImgfileBtn,printConverImgBtn,printSeatBtn,printLabel,printDessity;
	private static ImageView printImg;
	private EditText HTColumn1,HTColumn2,HTColumn3; // 水平制表输入内容与列信息
	private EditText setADValue,setDessity;
	private TextView mHTSeatStr,mMarkADValue,mBlackMartOffset; // 水平制表结果显示 ,黑标AD值
	// Common variables 常用变量全局
	private int rotate = 0;       // 默认为:0, 0 正常、1 90度旋转 
	private int align = 0;        // 默认为:1, 0 靠左、1  居中、2:靠右
	private int underLine = 0;    // 默认为:0, 0 取消、   1 下划1、 2 下划2 
	private int linespace = 40;   // 默认40, 常用：30 40 50 60 行间距
	private int cutter = 0;       // 默认0，  0 全切、1 半切
	static int Number = 1000;
	private int QrCodeLeft = 1;
	private int QrCodeSize = 1;
	
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // 国际化标志时间格式类
	SimpleDateFormat m_sdfDate = new SimpleDateFormat("HH:mm:ss ");     // 国际化标志时间格式类
	private String title = "", strData = "", num = "",codeStr = "";
	
	private UsbManager mUsbManager;
	private UsbDevice m_Device;
	private boolean shareFlag = false;
	private int clickFlag = 1; // 1：BMP；0：IMG
	private static final String ACTION_USB_PERMISSION =  "com.usb.sample.USB_PERMISSION";

	private final static int PID11 = 8211;
    private final static int PID13 = 8213;
    private final static int PID15 = 8215;
    private final static int VENDORID = 1305;
    private int densityValue = 85; // 默认：80,浓度范围：70-200
    // 20180226 连续打印
    private PopupWindow popWindow;
    long intervalTime = 3000;   // 间隔时间:MS
	int executionTimes = 1;     // 执行次数
	private Button btn5;
	private UsbReceiver mUsbReceiver;
	// 20180706
	String OneBarType = "1";
	String iline = "4";
	// 20180710设置区域国家和代码页
	private EditText feedLineNum;
	private Spinner mSpinnerCountry,mSpinnerCodePage;  // 区域国家列表控件
	private Button mSetCountryBtn,mSetCodePageBtn,mPrintCharPaperBtn; // 设置区域国家和代码页;字符页打印
	int countryValue;     // 区域国家列表值
	int CPnumberValue;    // 代码页列表值
	// 20180711 切纸功能与黑标设置
	private Button setBlackMarkBtn,halfCutBtn,totalCutBtn,BalckMarkCutBtn;
	private EditText mFeedDotLength;
	private Spinner mSpinnerBlackMark,mSelectPrintType;
	private int mBMOption = 0; // 黑标是否有效选项
	private Button feedDotBtn,feedBackBtn;
	private Button setBlackMark1,setBlackMark2;
	private LinearLayout LL_first,LL_second,LL_ALL;
//	private CheckBox isExtend,isSimplify,isSimplify2;
	private RadioGroup radioGroup_split;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mContext = this;
        init();// 初始化
    }
    /**
     * 初始化
     */
    private void init() {
    	findView();           // 控件绑定
    	getUsbDriverService();// 创建UsbManager ，权限，广播；usb驱动
    	setListener();        // 监听
    	getMsgByLanguage();   // 常规设置
    	printConnStatus();    // 连接设备 Get UsbDriver(UsbManager) service
	}
	// 绑定控件
	private void findView() {
		// 20170518 路径选择监听及输入框控件Add
		mAddBmpFilePath = (Button) findViewById(R.id.SelectBmpFile);
		mBmpPath_et = (EditText) findViewById(R.id.Nvbmp_path_et);
		mClearPath = (Button) findViewById(R.id.Clear_Path_Btn);
		mAddImgFilePath = (Button) findViewById(R.id.SelectImgFile);
		mImgPath_et = (EditText) findViewById(R.id.Img_path_et);
		// 二维码内容 + 一维码类型选择输入框
		etWrite = (EditText) findViewById(R.id.InputContent_et);   // 小票二维码内容 / 输入打印框
		m1DBarType =  (EditText) findViewById(R.id.TestTimes_et);  // 一维码类型选择输入框
		mQrCodeData = (EditText) findViewById(R.id.qrcode_data_et);// 单独二维码内容
		mQrCodeLeft = (EditText) findViewById(R.id.qrcode_left_et);// 二维码左边距输入框
		mQrCodeSize = (EditText) findViewById(R.id.qrcode_size_et);// 二维码大小输入框
		
		mPrintSelfPage = (Button) findViewById(R.id.PrintSeflPage_btn);
		mPrinterInfo = (Button) findViewById(R.id.PrinterInfo_btn);
		mPrintTest = (Button) findViewById(R.id.PrintTest_btn);
		mPrintTicket = (Button) findViewById(R.id.PrintTicket_btn);
		editRecDisp = (EditText) findViewById(R.id.Get_State_et); // 打印机状态显示框、打印机信息显示框
		mCheckStatus = (Button) findViewById(R.id.Check_Status_btn);
		mClear = (Button) findViewById(R.id.Clear_btn);           // 打印机状态清除按钮
		OneBarType = m1DBarType.getText().toString().trim();
		QrCodeSize = Integer.valueOf(mQrCodeSize.getText().toString().trim());
		etWrite.setText(Constant.WebAddress);
		// 20160826 Add
		mBmpLoad = (Button) findViewById(R.id.Load_nvbmp_btn);
		mBmpPrint = (Button) findViewById(R.id.Print_nvbmp_btn);
		IsAddLoadBmpPath = (CheckBox) findViewById(R.id.Is_AddLoadBmpPath);
		radioGroup_split= (RadioGroup) this.findViewById(R.id.radioGroup_split);
		radioGroup_split.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						// TODO Auto-generated method stub
						RadioButton isExtend, isSimplify, isSimplify2;
						isExtend = (RadioButton) group
								.findViewById(R.id.IsExtend);
						isSimplify = (RadioButton) group
								.findViewById(R.id.IsSimplify);
						isSimplify2 = (RadioButton) group
								.findViewById(R.id.IsSimplify2);
						String gender = isExtend.getText().toString();
						switch (checkedId) {
						case R.id.IsExtend:
							LL_first.setVisibility(View.VISIBLE);
		                	LL_second.setVisibility(View.VISIBLE);
							break;
						case R.id.IsSimplify:
							LL_first.setVisibility(View.GONE);
		                	LL_second.setVisibility(View.VISIBLE);
							break;
						case R.id.IsSimplify2:
							LL_first.setVisibility(View.VISIBLE);
		                	LL_second.setVisibility(View.GONE);
							break;
						default:
							break;
						}
					}
				});
		IsAddLoadBmpPath.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){ 
					mBmpLoad.setVisibility(View.VISIBLE);
					IsAddLoadBmpPath.setText(getString(R.string.Close));
                }else{ 
                	mBmpLoad.setVisibility(View.INVISIBLE);
					IsAddLoadBmpPath.setText(getString(R.string.Open));
                } 
			}
		});
		
		printQrCode =  (Button)findViewById(R.id.Print_QrCode_Btn);
		print1DBarBtn =  (Button)findViewById(R.id.Print_1DBar_Btn);
		printPdf417 =  (Button)findViewById(R.id.Print_PDF417_Btn);
		printImgfileBtn = (Button)findViewById(R.id.Print_Imgfile_Btn);
		printConverImgBtn = (Button)findViewById(R.id.Print_ConverImg_Btn);
		setBlackMarkADBtn =  (Button)findViewById(R.id.Set_ADValue_Btn);
		setBlackMarkOffset =  (Button)findViewById(R.id.Set_BlackMarkOffset_Btn);
		printLabel = (Button)findViewById(R.id.PrintLabel);
		mSelectPrintType = (Spinner)findViewById(R.id.SetSelectPrintType);
		getPrintTypeList(); // 打印相关类型选择
		printDessity = (Button)findViewById(R.id.SetPrintDessityBtn);
		setADValue =  (EditText)findViewById(R.id.SetADValue_et);
		setDessity =  (EditText)findViewById(R.id.print_dessity_et);
		mMarkADValue = (TextView)findViewById(R.id.DisplayAdVlaue_tv);        // AD值输入值
		mBlackMartOffset = (EditText)findViewById(R.id.SetBlackMarkOffset_et);// 黑标偏移量输入值
		// 水平制表
		HTColumn1 =  (EditText)findViewById(R.id.SetColumn1_et);
		HTColumn2 =  (EditText)findViewById(R.id.SetColumn2_et);
		HTColumn3 =  (EditText)findViewById(R.id.SetColumn3_et);
		printSeatBtn = (Button)findViewById(R.id.Print_Seat_Btn);
//		mHTSeatStr = (TextView)findViewById(R.id.HTSeat_tv);
		mInputPrint_btn = (Button)findViewById(R.id.InputPrint_btn);
		feedLineNum = (EditText)findViewById(R.id.Feedline_num_et);
		feedLineNum.setText("5");
		mSpinnerCountry  = (Spinner)findViewById(R.id.CountrySpinner);
		mSpinnerCodePage = (Spinner)findViewById(R.id.CodePageSpinner);
		mSetCountryBtn = (Button)findViewById(R.id.SetCountryBtn);
		mSetCodePageBtn = (Button)findViewById(R.id.SetCodePageBtn);
		mPrintCharPaperBtn = (Button)findViewById(R.id.PrintCharPageBtn);
		getCountryDataList(); // 区域国家列表
		getCodePageDataList();// 代码页列表
		mFeedDotLength = (EditText)findViewById(R.id.input_feed_distance_et);
		feedDotBtn = (Button)findViewById(R.id.doFeedDot);
		feedBackBtn = (Button)findViewById(R.id.doFeedBack);
		halfCutBtn = (Button)findViewById(R.id.doHalfCut);
		totalCutBtn = (Button)findViewById(R.id.doTotalCut);
		BalckMarkCutBtn = (Button)findViewById(R.id.doBlackMarkCut);
		mSpinnerBlackMark  = (Spinner)findViewById(R.id.SetBlackMarkSpinner);
		setBlackMarkBtn = (Button)findViewById(R.id.SetBlackMarkBtn);
		getBlackMarkOption();
		setBlackMark1  = (Button)findViewById(R.id.Print_Mark1);
		setBlackMark2  = (Button)findViewById(R.id.Print_Mark2);
		LL_first =  (LinearLayout)findViewById(R.id.linear_first);
		LL_second =  (LinearLayout)findViewById(R.id.linear_second);
		LL_ALL =  (LinearLayout)findViewById(R.id.linear_upper);
	}
	
	
	private void getUsbDriverService(){
		mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		mUsbDriver = new UsbDriver(mUsbManager, this);
		PendingIntent permissionIntent1 = PendingIntent.getBroadcast(this, 0,
				new Intent(ACTION_USB_PERMISSION), 0);
		mUsbDriver.setPermissionIntent(permissionIntent1);
		// Broadcast listen for new devices
		
		mUsbReceiver = new UsbReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
		filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		this.registerReceiver(mUsbReceiver, filter);
	}
	// 设置监听
	private void setListener() {
		mInputPrint_btn.setOnClickListener(new PrintClickListener());
		mPrintSelfPage.setOnClickListener(new PrintClickListener());
		mPrinterInfo.setOnClickListener(new PrintClickListener());
		mPrintTest.setOnClickListener(new PrintClickListener());
		mPrintTicket.setOnClickListener(new PrintClickListener());
		mCheckStatus.setOnClickListener(new PrintClickListener());
		mClear.setOnClickListener(new PrintClickListener());
		mBmpLoad.setOnClickListener(new PrintClickListener());
		mBmpPrint.setOnClickListener(new PrintClickListener());

		printQrCode.setOnClickListener(new PrintClickListener());
		printPdf417.setOnClickListener(new PrintClickListener());
		// 打印机各型号一维码各种类型
		print1DBarBtn.setOnClickListener(new PrintClickListener());
		printImgfileBtn.setOnClickListener(new PrintClickListener());
		printConverImgBtn.setOnClickListener(new PrintClickListener());
		setBlackMarkADBtn.setOnClickListener(new PrintClickListener());
		setBlackMarkOffset.setOnClickListener(new PrintClickListener());
		printLabel.setOnClickListener(new PrintClickListener());
		printSeatBtn.setOnClickListener(new PrintClickListener());
		printDessity.setOnClickListener(new PrintClickListener());
		mSetCodePageBtn.setOnClickListener(new PrintClickListener()); 
		mSetCountryBtn.setOnClickListener(new PrintClickListener()); 
		mPrintCharPaperBtn.setOnClickListener(new PrintClickListener());
		feedDotBtn.setOnClickListener(new PrintClickListener()); 
		feedBackBtn.setOnClickListener(new PrintClickListener()); 
		halfCutBtn.setOnClickListener(new PrintClickListener());
		totalCutBtn.setOnClickListener(new PrintClickListener());
		setBlackMarkBtn.setOnClickListener(new PrintClickListener());
		BalckMarkCutBtn.setOnClickListener(new PrintClickListener());
		setBlackMark1.setOnClickListener(new PrintClickListener());
		setBlackMark2.setOnClickListener(new PrintClickListener());
		
		mAddBmpFilePath.setOnClickListener(new BmpBrowerClickListener());
		mAddImgFilePath.setOnClickListener(new BmpBrowerClickListener());
		mClearPath.setOnClickListener(new BmpBrowerClickListener());
		
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
	
    /*
     *  BroadcastReceiver when insert/remove the device USB plug into/from a USB port
     *  创建一个广播接收器接收USB插拔信息：当插入USB插头插到一个USB端口，或从一个USB端口，移除装置的USB插头
     */
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
	                	 T.showShort(TestActivity.this, "permission denied for device");
	                     //Log.d(TAG, "permission denied for device " + device);
	                 }
	             }
	         }  
 		}
 	};
 	
 	
 	class BmpBrowerClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			try {
				switch (view.getId()) {
				case R.id.SelectBmpFile:
					selectBmpFile();
					break;
				case R.id.SelectImgFile:
					selectImgFile();
					break;
				case R.id.Clear_Path_Btn:
					clearAllPath();
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
 	}
 	// 选择bmp图片文件
	private void selectBmpFile() {
		clickFlag = 1;
		showFileChooser();
		String bmpPath = mBmpPath_et.getText().toString().trim();
		Utils.putValue(TestActivity.this, "path", bmpPath);
	}
	// 选择img图片文件
	private void selectImgFile() {
		clickFlag = 0;
		showFileChooser();
	}
	// 清除输入框获取路径
	private void clearAllPath(){
		mBmpPath_et.setText("");
		mImgPath_et.setText("");
	}
 	
	// 1.判断换行数及是否输入为空；2.一维码信息及是否输入为空
	private void setFeedLineNum(){
		OneBarType = m1DBarType.getText().toString().trim();// 一维码信息
		if(TextUtils.isEmpty(OneBarType)){
			T.showShort(TestActivity.this, "输入框不能为空!");
			OneBarType = "1";
		}
		iline = feedLineNum.getText().toString().trim();
		if(TextUtils.isEmpty(iline)){
			T.showShort(TestActivity.this, "输入框不能为空!");
			iline = "5";
		}
	}
	
	class PrintClickListener implements OnClickListener {
		@Override
		public void onClick(View view) {
			setFeedLineNum();// 判断换行数及是否输入为空
			try {
				switch (view.getId()) {
				case R.id.InputPrint_btn:    // 输入内容打印
					printText(etWrite.getText().toString().trim());    // 1.常规热敏非黑标纸案例
//					printMarkText(etWrite.getText().toString().trim());// 2.热敏黑标纸，不干胶案例
					break;
				case R.id.PrintSeflPage_btn: // 自检页打印
					printSelfPageTest();
					break;
				case R.id.PrinterInfo_btn:   // 获取打印机设备信息
					getPrinterInfo();
					break;
				case R.id.PrintTest_btn:     // 文本打印
//					int status = getPrinterStatus2(mUsbDev);
//					if(status != 3){ 
//						getPrintTestData(mUsbDev);
//					}
					int status = PrintCmd.getPrintEndStatus(mUsbDriver);
					if(status != -1){ 
						getPrintTestData(mUsbDev);
						showMessage(getString(R.string.PrintComplete));
					}else{
						showMessage(getString(R.string.PrintException));
					}
					break;
				case R.id.PrintTicket_btn:  // 票据打印
					if(cutter==0)
						cutter = 0;
					else
						cutter = 1;
					selectPrintCase();
					break;
				case R.id.Check_Status_btn: // 查询状态
					checkPrinterStatus(mUsbDev);
					break;
				case R.id.Clear_btn:        // 清除状态
					editRecDisp.setText("");
					break;
				case R.id.Load_nvbmp_btn:   // 下载指定路径的位图保存到打印机
					if(downloadNvBmp())
						T.showShort(TestActivity.this, getString(R.string.Download_bmp_prompt));
					break;
				case R.id.Print_nvbmp_btn:    // 打印位图
					setPrintNvBmp();	
					break;
				case R.id.Print_Imgfile_Btn:  // 打印其他格式图片（jpg/png/bmp等）
					printDiskImgFile();       // 热敏打印机
//					printDiskImgFileZD();     // 针打打印机       su change
//					printSticky();            // 易拓科技不干胶测试 20mm*50mm
					break;
				case R.id.Print_ConverImg_Btn:// 转换后的打印图片（jpg/png/bmp等）
					printConverImg();
//					printDiskImgByViewBitmap(); // 根据布局界面打印
					break;
				case R.id.Set_ADValue_Btn:      // 设置黑标AD值
					setBlackMarkAD();
					break;
				case R.id.Set_BlackMarkOffset_Btn: // 设置黑标切纸偏移量
					int offsetmar = Integer.valueOf(mBlackMartOffset.getText().toString().trim());
					mUsbDriver.write(PrintCmd.SetMarkoffsetprint(offsetmar));
					break;
				case R.id.Print_Seat_Btn:     // 打印水平制表
					printSeat(mUsbDev);
					break;
				case R.id.Print_QrCode_Btn:   // QrCode
					printQrCodeBySizeAndData();	
//					printQrCodeII();
					break;
				case R.id.Print_PDF417_Btn:   // PDF417
					printPdf417Code();
					break;
				case R.id.Print_1DBar_Btn:    // 一维条形码打印
					if(!TextUtils.isEmpty(OneBarType))
						print1DBarByType(Integer.valueOf(OneBarType));
					break;
				case R.id.PrintLabel:    // 其它打印、设置任务
//					openEleLock();               // 1.电子锁
//					openCashBox();               // 2.钱箱
//					printSpecialData(specData2); // 3.指令发送打印
					setPrintType(printType);
					break;	
				case R.id.SetPrintDessityBtn: // 浓度设置
					getDessityType();
					break;
				case R.id.SetCountryBtn:     // 设置区域国家
					setCTAndCP(0,countryValue);
					break;
				case R.id.SetCodePageBtn:     // 设置代码页
					setCTAndCP(1,CPnumberValue);
					// mUsbDriver.write(PrintCmd.SetCodepage(countryValue,CPnumberValue)); // sdk开发里jar包函数
					break;
				case R.id.PrintCharPageBtn:   // 字符页打印
//					mUsbDriver.write(PrintCmd.SetReadZKmode(1));
//					mUsbDriver.write(PrintCmd.PrintString("￥", 0,"ASCII"));
//					mUsbDriver.write(PrintCmd.PrintString("￥", 0,"GB18030"));
//					mUsbDriver.write(PrintCmd.PrintString("￥", 0,"Unicode"));
					printSpecialData(charPageCmd);
//					printDataMoney();
					break;
				case R.id.Print_Mark1:    // 不初始化黑标
					printSetMark(0);
					break;
				case R.id.Print_Mark2:    // 初始化黑标
					printSetMark(1);
					break;
				case R.id.doFeedDot:      // 进纸（输入单位mm，走空白纸）
					printFeedDot_Test();
//					printFeedForward();
					break;
				case R.id.doFeedBack:      // 退纸（输入单位mm，走空白纸）
					printFeedBack();
					break;
				case R.id.doHalfCut:      // 半切
					mUsbDriver.write(PrintCmd.PrintCutpaper(1));
					break;
				case R.id.doTotalCut:     // 全切
					mUsbDriver.write(PrintCmd.PrintCutpaper(0));
					break;
				case R.id.doBlackMarkCut: // 黑标切纸
					printBlackMarkCutPaper();
					break;
				case R.id.SetBlackMarkBtn:// 设置黑标有效或无效
//					openBlackMarkOption(mBMOption);
					setMarkFunction(mBMOption);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void printDataMoney() {
		mUsbDriver.write(PrintCmd.SetReadZKmode(0)); // 进入汉字模式
		mUsbDriver.write(PrintCmd.PrintString("红烧猪蹄    ", 1));
		doSetting();
		mUsbDriver.write(PrintCmd.SetReadZKmode(1));// 退出汉字模式
		mUsbDriver.write(PrintCmd.PrintString("58.0", 1));
		mUsbDriver.write(PrintCmd.SetReadZKmode(0)); // 进入汉字模式
		mUsbDriver.write(PrintCmd.PrintString("肉末茄子  ", 1)); // PrintString 第二个参数1，就是不换行，接着打印￥
		doSetting();
		mUsbDriver.write(PrintCmd.SetReadZKmode(1));// 退出汉字模式
		mUsbDriver.write(PrintCmd.PrintString("18.0", 0));
		setFeedCut(0, 8);
	}
	
	private void setPrintType(int type){
		if(type == 1){
			openEleLock(); // 打开电子锁
		}else if(type == 2){
			openCashBox(); // 打开钱箱
		}else if(type == 3){
			printSpecialData(specData2);// 指令发送打印测试
		}
	}
	
	// 设置黑标是否有效  0 有效，1 无效
	private void setMarkFunction(int type){
		if(type==0){
			mUsbDriver.write(openBlackMark2(0));
		}else{
			mUsbDriver.write(openBlackMark2(1));
		}
	}
	
	// 黑标切纸
	private void printBlackMarkCutPaper(){
		mUsbDriver.write(PrintCmd.PrintString("欢迎使用打印机", 0));
		mUsbDriver.write(PrintCmd.PrintMarkpositioncut());
		mUsbDriver.write(PrintCmd.PrintCutpaper(cutter));
		mUsbDriver.write(PrintCmd.SetClean());
	}
	
    // 打印pdf417二维码
    private void printPdf417Code(){
    	mUsbDriver.write(PrintCmd.SetClean());
		mUsbDriver.write(PrintCmd.SetLeftmargin(20));
		mUsbDriver.write(PrintCmd.setPdf417(2, 100));
		mUsbDriver.write(PrintCmd.PrintPdf417(12, 4, "1234567890abcdefghijklmnopqrst"));
		mUsbDriver.write(PrintCmd.PrintFeedline(6));
		mUsbDriver.write(PrintCmd.SetClean());
		
		mUsbDriver.write(PrintCmd.SetLeftmargin(20));
		mUsbDriver.write(PrintCmd.setPdf417(3, 100));
		mUsbDriver.write(PrintCmd.PrintPdf417(12, 4, "1234567890abcdefghijklmnopqrst"));
		mUsbDriver.write(PrintCmd.PrintFeedline(6));
		mUsbDriver.write(PrintCmd.SetClean());
		
		mUsbDriver.write(PrintCmd.SetLeftmargin(20));
		mUsbDriver.write(PrintCmd.setPdf417(4, 100));
		mUsbDriver.write(PrintCmd.PrintPdf417(12, 4, "1234567890abcdefghijklmnopqrst"));
		mUsbDriver.write(PrintCmd.PrintFeedline(6));
		mUsbDriver.write(PrintCmd.SetClean());
		setFeedCut(cutter, Integer.valueOf(iline));
    }
	// 走细纸（输入框长度）
	private void printFeedDot_Test() {
		mUsbDriver.write(PrintCmd.SetClean());
		int feedLen = Integer.valueOf(mFeedDotLength.getText().toString().trim());
		if(feedLen <= 30){
			mUsbDriver.write(PrintCmd.PrintFeedDot(feedLen*8));
		}else{
			for(int i = 0;i < (feedLen / 30);i++){
//				mUsbDriver.write(PrintCmd.PrintFeedDot(240));
				feedLen = feedLen - (feedLen / 30) * 30;
				if(feedLen < 0){
					mUsbDriver.write(PrintCmd.PrintFeedDot(feedLen*8));
				}
			}
		}
	}
	
	// 不干胶切纸
	private void printMarkText(String data) {
		mUsbDriver.write(PrintCmd.PrintString(data, 0));
		PrintMarkFeedCutpaper();
	}
	private void PrintMarkFeedCutpaper() {
		try {
			mUsbDriver.write(PrintCmd.PrintMarkcutpaper(cutter));
			mUsbDriver.write(PrintCmd.SetClean());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 3.27 打印细走纸
	 * @param Lnumber  范围 0-250（1mm==8个dot）
	 */
	public static byte[] PrintFeedDot(int Lnumber) {
		byte[] bCmd = new byte[3];
		int iIndex = 0;
		bCmd[iIndex++] = 0x1B;
		bCmd[iIndex++] = 0x4A;
		bCmd[iIndex++] = (byte)Lnumber;
		return bCmd;
	}
	
	/**
	 * 1.1.文本打印
	 */
	private void printText(String data) {
		if(!TextUtils.isEmpty(data)){
			mUsbDriver.write(PrintCmd.SetAlignment(align));
			mUsbDriver.write(PrintCmd.PrintString(data, 0));
			setFeedCut(cutter, Integer.valueOf(iline));
		}else{
			T.showShort(TestActivity.this, "对不起,输入框不能为空，请重试！");
		}
		
	}
	
	// 设置 区域 国家和代码页，此设置掉电不保存
	private String specData = "1C 2E 20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f 0a "
			+ "30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f 0a 40 41 "
			+ "42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f 0a 50 51 52 53 "
			+ "54 55 56 57 58 59 5a 5b 5c 5d 5e 5f 0a 60 61 62 63 64 65 "
			+ "66 67 68 69 6a 6b 6c 6d 6e 6f 0a 70 71 72 73 74 75 76 77 "
			+ "78 79 7a 7b 7c 7d 7e 7f 0a 80 81 82 83 84 85 86 87 88 89 "
			+ "8a 8b 8c 8d 8e 8f 0a 90 91 92 93 94 95 96 97 98 99 9a 9b "
			+ "9c 9d 9e 9f 0a a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad "
			+ "ae af 0a b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf "
			+ "0a c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf 0a d0 "
			+ "d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df 0a e0 e1 e2 "
			+ "e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef 0a f0 f1 f2 f3 f4 "
			+ "f5 f6 f7 f8 f9 fa fb fc fd fe ff 0A 1B 4A FF 1B 69";
	private void setCodePage(int country,int codepage){
//		mUsbDriver.write(PrintCmd.SetCodepage(7, 15)); // 7  西班牙  + 15 PC1252
		mUsbDriver.write(PrintCmd.SetClean());
		mUsbDriver.write(getAscllCmd(country));
		mUsbDriver.write(getCodePageCmd(codepage));
	}
	// 设置国家区域和字符页
	private void setCTAndCP(int type,int hex){
		mUsbDriver.write(PrintCmd.SetClean());
		if(type==0){
			mUsbDriver.write(getAscllCmd(hex));
		}else{
			mUsbDriver.write(getCodePageCmd(hex));
		}
	}
	// 测试指令
	private String specData2 = "1b 40 " +
			"20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f " +
			"0a 30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e " +
			"80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f " +
			"0a 90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e " +
			"1b 64 0a " +
			"1b 69"; 
	// 字符页指令
	private String charPageCmd = "1D 4C 30 00 1D 57 40 02 "
				+ "20 21 22 23 24 25 26 27 28 29 2A 2B 2C 2D 2E 2F "
				+ "0A 30 31 32 33 34 35 36 37 38 39 3A 3B 3C 3D 3E "
				+ "3F 0A 40 41 42 43 44 45 46 47 48 49 4A 4B 4C 4D "
				+ "4E 4F 0A 50 51 52 53 54 55 56 57 58 59 5A 5B 5C "
				+ "5D 5E 5F 0A 60 61 62 63 64 65 66 67 68 69 6A 6B "
				+ "6C 6D 6E 6F 0A 70 71 72 73 74 75 76 77 78 79 7A "
				+ "7B 7C 7D 7E 7F 0A 80 81 82 83 84 85 86 87 88 89 "
				+ "8A 8B 8C 8D 8E 8F 0A 90 91 92 93 94 95 96 97 98 "
				+ "99 9A 9B 9C 9D 9E 9F 0A A0 A1 A2 A3 A4 A5 A6 A7 "
				+ "A8 A9 AA AB AC AD AE AF 0A B0 B1 B2 B3 B4 B5 B6 "
				+ "B7 B8 B9 BA BB BC BD BE BF 0A C0 C1 C2 C3 C4 C5 "
				+ "C6 C7 C8 C9 CA CB CC CD CE CF 0A D0 D1 D2 D3 D4 "
				+ "D5 D6 D7 D8 D9 DA DB DC DD DE DF 0A E0 E1 E2 E3 "
				+ "E4 E5 E6 E7 E8 E9 EA EB EC ED EE EF 0A F0 F1 F2 "
				+ "F3 F4 F5 F6 F7 F8 F9 FA FB FC FD FE FF 0A "
				+ "1B 64 0A"; 
	String sCmd = "1b 74 00 1c 2e 9D 1b 64 05"; //1B 40 1C 26
	private void printSpecialData(String data) {
		byte[] label = Utils.getHexCmd(data);
		if(label.length != 0){
			mUsbDriver.write(label, label.length);
			mUsbDriver.write(PrintCmd.PrintCutpaper(0)); // 切纸类型
			mUsbDriver.write(PrintCmd.SetClean()); // 缓存清理
//			setDataFormat();
		}
//		setFeedCut(cutter, Integer.valueOf(iline));
	}
	
	// 页模式模式
	private String specData1="0A 0A 1B 40 61 30 2D" +
			"2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 2D 0A " +
			"1B 57 00 00 00 00 00 01 00 02" +
			"1B 40 1B 4C 1B 54 00 30 30 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F " +
			"50 51 52 53 54 55 31 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 51 52 53 54 55 " +
			"32 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 51 52 53 54 55 " +
			"33 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 51 52 53 54 55 " +
			"34 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 51 52 53 54 55" +
			"35 42 43 44 45 46 47 48 49 4A 4B 4C 4D 4E 4F 50 51 52 53 54 55 " +
			"0C 62 30 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 2A 0A";
	
	// ASCLL码(区域国家)
	private static byte[] getAscllCmd(int country) {
		byte[] bCmd = new byte[5];
		int iIndex = 0;
		bCmd[iIndex++] = 0x13;
		bCmd[iIndex++] = 0x74;
		bCmd[iIndex++] = 0x33;
		bCmd[iIndex++] = 0x55;
//		bCmd[iIndex++] = 0x03; 
		bCmd[iIndex++] = (byte)country; 
		return bCmd;
	}
	// CodePage 代码页设置
	private static byte[] getCodePageCmd(int codepage) {
		byte[] bCmd = new byte[5];
		int iIndex = 0;
		bCmd[iIndex++] = 0x13;
		bCmd[iIndex++] = 0x74;
		bCmd[iIndex++] = 0x33;
		bCmd[iIndex++] = 0x66;
//		bCmd[iIndex++] = 0x1B; 
		bCmd[iIndex++] = (byte)codepage; 
		return bCmd;
	}
	
	private void selectPrintCase() {
		String[] NvBmpNums = null;
		NvBmpNums = new String[] { getString(R.string.case1).toString(),
				getString(R.string.case2).toString(),
				getString(R.string.case3).toString(),
				getString(R.string.case4).toString(),
				getString(R.string.case5).toString(),
				getString(R.string.case6).toString() }; // 对齐方式数组
		AlertDialog.Builder b = new Builder(this);
		b.setTitle(getString(R.string.Select_Print_Case));
		b.setSingleChoiceItems(NvBmpNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							getStrDataByLanguage();
							//Bills.printSmallTicket(mUsbDriver,title,num,codeStr,cutter); // 排队票据
							break;
						case 1:
							Bills.printBankBill(mUsbDriver,cutter);    // 银行交易票据
							break;
						case 2:
							Bills.printShareTicket(mUsbDriver,codeStr,cutter); // 共享小票
							break;
						case 3:
							Bills.printCinemaTicket(mUsbDriver,cutter);  // 电影票样例
							break;
						case 4:
							T.showShort(TestActivity.this, "暂无新票样！");
							break;
						case 5:
							T.showShort(TestActivity.this, "暂无新票样！");
							break;
						default:
							break;
						}
					}
				});
		b.show();
	}


	int pringNum = 3;
	private void printQrCode(){
		mUsbDriver.write(PrintCmd.SetAlignment(1));// 默认为:1, 0 靠左、1  居中、2:靠右
		for(int i=0;i<pringNum;i++){
			mUsbDriver.write(PrintCmd.PrintQrcode(codeStr, 8, 3, 0));// 2表示 二维码尺寸大小  最大3最小1
			mUsbDriver.write(PrintCmd.PrintFeedline(5));
			mUsbDriver.write(PrintCmd.PrintMarkcutpaper(1));  // 切纸类型
		}
		mUsbDriver.write(PrintCmd.PrintMarkcutpaper(cutter));  // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());
	}

	// 打印机信息【指定下载位图的索引】
	String printerInfo = "Get Printer Info failed!";
	private void getPrinterInfo() {
		String[] iFstypeNums = null;
		iFstypeNums = new String[] {
				getString(R.string.iFstype_1).toString(),
				getString(R.string.iFstype_2).toString(),
				getString(R.string.iFstype_3).toString(),
				getString(R.string.iFstype_4).toString(),
				getString(R.string.iFstype_5).toString(),
				//getString(R.string.iFstype_6).toString()
				}; // 对齐方式数组
		AlertDialog.Builder b = new Builder(this);
		b.setTitle(getString(R.string.PrinterInfo_btn));
		b.setSingleChoiceItems(iFstypeNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String title = "";
						switch (which) {
						case 0:
							title = getString(R.string.iFstype_1).toString() + ":";
							printerInfo = title + checkPrinterInfo(1);
							break;
						case 1:
							title = getString(R.string.iFstype_2).toString() + ":";
							printerInfo = title + checkPrinterInfo(2);
							break;
						case 2:
							title = getString(R.string.iFstype_3).toString() + ":";
							printerInfo = title + checkPrinterInfo(3);
							break;
						case 3:
							title = getString(R.string.iFstype_4).toString() + ":";
							//printerInfo = title + checkPrinterInfo(4);
							printerInfo = title + checkPrinterInfo(5);   //2018-03-01
							break;
						case 4:
							title = getString(R.string.iFstype_5).toString() + ":";
							//printerInfo = title + checkPrinterInfo(5);
							printerInfo = title + checkPrinterInfo(6);   //2018-03-01
							break;
						case 5:
							title = getString(R.string.iFstype_6).toString() + ":";
							printerInfo = title + checkPrinterInfo(6);
							break;
						default:
							break;
						}
						editRecDisp.setText(printerInfo);
						dialog.dismiss();
					}
				});
		b.show();
	}

	/**
	 * 打印机信息解析
	 * 参数  iFstype： 1.打印头型号ID,2.类型ID,3.固件版本,4.生产厂商信息,5.打印机型号,6.支持的中文编码格式
	 */
	private String checkPrinterInfo(int iFstype) {
		String iRet = "0";
		byte[] bRead1 = new byte[50];
		byte[] bWrite1 = PrintCmd.GetProductinformation(iFstype);
		if (mUsbDriver.read(bRead1, bWrite1) > 0) {
			iRet = PrintCmd.CheckProductinformation(bRead1);
		}
		if (iRet != "0")
			return iRet;
		return iRet;
	}

	// 水平制表符行列数据转换
	ArrayList<String> list1 = null;
	ArrayList<String> list2 = null;
	ArrayList<String> list3 = null;
	ArrayList<String> list4 = null;
	String[] str1 = {"语文","数学","英语","物理","化学","政治"};
	String[] str2 = {"88","100","96","100","95","65"};
	String[] str3 = {"A-","A+","A+","A+","A-","B-"};
	String[] str4 = {"陈老师","周老师","吴老师","张老师","冯老师","李老师"};
	private ArrayList<String> getSeatColHtData(String[] str) {
		ArrayList<String> list = new ArrayList<String>();
		if (str != null) {
			for (int i = 0; i < 6; i++) {
				list.add(str[i]);
			}
		}
		return list;
	}
	String HTSeatStr1 = "";
	String HTSeatStr2 = "";
	String HTSeatStr3 = "";
	String HTSeatStr4 = "";
	String ht1,ht2,ht3,ht4 = "";
	private void setTransData(String col1, String col2, String col3, UsbDevice usbDev){
		list1 = getSeatColHtData(str1);
		list2 = getSeatColHtData(str2);
		list3 = getSeatColHtData(str3);
		list4 = getSeatColHtData(str4);
		String Col1 = Utils.intToHexString(Integer.valueOf(col1), 1)+ " ";// 转换第1列
		String Col2 = Utils.intToHexString(Integer.valueOf(col2), 1)+ " ";// 转换第2列
		String Col3 = Utils.intToHexString(Integer.valueOf(col3), 1)+ " ";// 转换第3列
		for(int i = 0;i<6;i++){
			HTSeatStr1 = list1.get(i);
			ht1 = Utils.stringTo16Hex(HTSeatStr1);
			HTSeatStr2 = list2.get(i);
			ht2 = Utils.stringTo16Hex(HTSeatStr2);
			HTSeatStr3 = list3.get(i);
			ht3 = Utils.stringTo16Hex(HTSeatStr3);
			HTSeatStr4 = list4.get(i);
			ht4 = Utils.stringTo16Hex(HTSeatStr4);
			String etstring = Col1 + Col2 + Col3 + "00 " + ht1 + "09 " +
					ht2 + "09 " + ht3 + "09 " + ht4 + "0A 0A";
			byte[] seat = Utils.hexStr2Bytesnoenter(etstring);
			if (etstring != null && !"".equals(etstring)) {
				mUsbDriver.write(PrintCmd.SetAlignment(align));
				mUsbDriver.write(PrintCmd.SetLinespace(linespace));
				mUsbDriver.write(PrintCmd.SetHTseat(seat, seat.length),
						seat.length,usbDev);
				mUsbDriver.write(PrintCmd.PrintFeedline(0),usbDev);      // 走纸换行
			}
		}
	}

	// 打印水平制表
	private void printSeat(UsbDevice usbDev) {
		// 获取输入数据
		String col1 = HTColumn1.getText().toString().trim();
		String col2 = HTColumn2.getText().toString().trim();
		String col3 = HTColumn3.getText().toString().trim();
		if(Integer.valueOf(col1)> Integer.valueOf(col2)){
			T.showShort(TestActivity.this, "第1列值不能大于第2列，请重新输入！");
			return;
		}
		if(Integer.valueOf(col2)> Integer.valueOf(col3)){
			T.showShort(TestActivity.this, "第2列值不能大于第3列，请重新输入！");
			return;
		}
		setTransData(col1,col2,col3,usbDev);
		setFeedCut(cutter,usbDev, Integer.valueOf(iline));
	}

	private static void backPaper(UsbDriver mUsbDriver, int len){
		byte[] etBytes = new byte[3];
		int iIndex = 0;
		etBytes[iIndex++] = 0x1B;
		etBytes[iIndex++] = 0x4B;
		etBytes[iIndex++] = (byte)len; // 0x43[67],0x32[50] 0x28[40]
		mUsbDriver.write(etBytes,iIndex);
	}
//	private static void backPaper2(UsbDriver mUsbDriver){
//		byte[] etBytes = new byte[3];
//		int iIndex = 0;
//		etBytes[iIndex++] = 0x1B;
//		etBytes[iIndex++] = 0x4B;
//		etBytes[iIndex++] = 0x55;
//		mUsbDriver.write(etBytes,iIndex);
//	}


	/**
	 * 打印不干胶纸【易拓科技D245测试】
	 */
	private void printSticky(){
		String imgPath = mImgPath_et.getText().toString().trim();
		int count = Integer.valueOf(feedLineNum.getText().toString().trim());
		Bills.printDiskImgFile(imgPath, count, mUsbDriver,cutter);
	}

	// 打印图片文件（png/jpg/bmp）【热敏打印机】
	private void printDiskImgFile(){
		String imgPath = mImgPath_et.getText().toString().trim();
		if("".endsWith(imgPath)){
 			showMessage(getString(R.string.The_path_cannot_be_empty));
 			return;
 		}
		Bitmap inputBmp = Utils.getBitmapData(imgPath);
		if(inputBmp == null)
 			return;
		int[] data = Utils.getPixelsByBitmap(inputBmp);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data,inputBmp.getWidth(),inputBmp.getHeight()));
		setFeedCut(cutter, Integer.valueOf(iline));
		// 2018-03-01 su change
//		setFeedMarkCut(cutter, Integer.valueOf(iline));
	}



	// 打印图片文件（bmp）【针打打印机】
	private void printDiskImgFileZD(){
		String imgPath = mImgPath_et.getText().toString().trim();
		if("".endsWith(imgPath)){
 			showMessage(getString(R.string.The_path_cannot_be_empty));
 			return;
 		}
		mUsbDriver.write(PrintCmd.PrintDiskbmpfileZD(imgPath));
		setFeedCut(cutter, Integer.valueOf(iline));
		// 2018-03-01 su change
//		mUsbDriver.write(PrintCmd.PrintMarkcutpaper(0));
		// 2018-08-04
//		mUsbDriver.write(PrintCmd.PrintMarkpositioncut());
//		mUsbDriver.write(PrintCmd.PrintCutpaper(0));
	}

	/**
	 * 通过截取ViewBitmap 转化为黑白单色色 打印图片
	 * @param layoutId
	 * @return
	 */
	;
	private void printDiskImgByViewBitmap(){
		Bitmap viewBitmap = makeView2Bitmap(LL_ALL);
		String path = Utils.saveMypic(viewBitmap,TestActivity.this);
		if(path==null||"".equals(path)){
			return;
		}
		Bitmap inputBmp = Utils.getBitmapData(path); // 通过路径获取Bitmap
		Bitmap bitmap = Utils.getSinglePic(inputBmp);
		int[] data = Utils.getPixelsByBitmap(bitmap);
		mUsbDriver.write(PrintCmd.PrintDiskImagefile(data,bitmap.getWidth(),bitmap.getHeight()));
		setFeedCut(cutter, Integer.valueOf(iline));
	}


	// 将View布局转化为Bitmap位图数据
	public Bitmap makeView2Bitmap(View view) {
		//View是你需要绘画的View
		int width = view.getWidth();
		int height = view.getHeight();
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawColor(Color.WHITE);      	//如果不设置canvas画布为白色，则生成透明
		view.layout(0, 0 , width, height);
		view.draw(canvas);
		return bitmap;
	}

	// 通过获取布局ID绘制Bitmap位图数据
	private Bitmap getViewBitmap(int layoutId) {
		View view = getLayoutInflater().inflate(layoutId, null);
		int me = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		view.measure(me,me);
		view.layout(0 ,0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();
		return bitmap;
	}

	// 转换后的打印图片（png/jpg/bmp）20171018 + PDF文件打印 20180611
	String qrcode1 = "HF86385845107102538400000006d000000720000000080030723FChQDHUTrFhS9iJcor9DmQ" +
			"==:**********:1:4:0:義大利Bonomi小麥千層餅:1:59.0";
	private void printConverImg() {
		String imgPath = mImgPath_et.getText().toString().trim();
		if("".endsWith(imgPath)){
			showMessage(getString(R.string.The_path_cannot_be_empty));
 			return;
 		}
		// 打印PDF文件
		if (imgPath.toUpperCase().endsWith(".PDF")) {
			int[] data = PrintUtils.getPdfPrintData(imgPath,
					1, 640, 640);
			mUsbDriver.write(PrintCmd.SetLeftmargin(Integer.valueOf(iline)));
			mUsbDriver.write(PrintCmd.PrintDiskImagefile(data, 640, 640));
		// 其他图片文件路径打印
		} else {
			Bitmap inputBmp = Utils.getBitmapData(imgPath);
//			Bitmap inputBmp = Utils.getQRcode(qrcode1, 200, 200);
//			Bitmap inputBmp = Utils.getQRcode(qrcode1, 300, 300);
			if (inputBmp == null)
				return;
			Bitmap bm = Utils.getSinglePic(inputBmp);
			int[] data = Utils.getPixelsByBitmap(bm);
			mUsbDriver.write(PrintCmd.SetLeftmargin(Integer.valueOf(iline)));
			mUsbDriver.write(PrintCmd.PrintDiskImagefile(data, bm.getWidth(),
					bm.getHeight()));
		}
		setFeedCut(cutter, Integer.valueOf(iline));
		mUsbDriver.write(PrintCmd.SetClean());
//		mUsbDriver.write(PrintCmd.PrintMarkpositioncut()); // 2018-08-03广州宇联票样打印修改
//		mUsbDriver.write(PrintCmd.PrintCutpaper(0));       // 2018-08-03广州宇联票样打印修改
	}


	// 通过输入二维码内容和二维码内容打印二维码
	private void printQrCodeBySizeAndData() {
		String code_info = mQrCodeData.getText().toString().trim();
		QrCodeLeft = Integer.valueOf(mQrCodeLeft.getText().toString().trim());
		QrCodeSize = Integer.valueOf(mQrCodeSize.getText().toString().trim());
		// 标准通用左边距（0-27）和大小（1-8）范围
		if(QrCodeLeft > 27)
			QrCodeLeft = 27;
		if(QrCodeLeft < 1)
			QrCodeLeft = 1;
		if(QrCodeSize > 8)
			QrCodeSize = 8;
		if(QrCodeSize < 1)
			QrCodeSize = 1;
		// 2017-12-29 邦众；左边距（1-12）和大小（1-3）范围
//		if(QrCodeLeft > 12)
//			QrCodeLeft = 12;
//		if(QrCodeLeft < 1)
//			QrCodeLeft = 1;
//		if(QrCodeSize > 3)
//			QrCodeSize = 3;
//		if(QrCodeSize < 1)
//			QrCodeSize = 1;
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.PrintQrcode(code_info, QrCodeLeft, QrCodeSize, 0));
		mUsbDriver.write(PrintCmd.PrintQrcode(code_info, QrCodeLeft, QrCodeSize, 1));
//		setFeedMarkCut(cutter, Integer.valueOf(iline));
		setFeedCut(cutter);
	}

	private void printQrCodeII() {
		mUsbDriver.write(PrintCmd.PrintString(Constant.TESTDATA_CN,0));
		mUsbDriver.write(PrintCmd.PrintQrcode("Welcome", 5, 4, 0));
		mUsbDriver.write(PrintCmd.SetLeftmargin(250));
		mUsbDriver.write(PrintCmd.PrintString(Constant.TESTDATA_CN2,0));
		setFeedCut(cutter);
	}

	// 通过类型打印一维码
	private void print1DBarByType(int iBarType) {
		mUsbDriver.write(PrintCmd.SetAlignment(align));
		/*
		 * CODE39:14809966841053
		 * 测试数据 k >= 1  类型: 0 （11 =< k <= 12） 、 1(11 =< k <=12) 、2(12 =< k <= 13) 、7(12 =< k <= 13)
		 * 字符个数 k = 12 均适用
		 */
		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, iBarType, "012345678900"));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "mannings012345678900"));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		mUsbDriver.write(PrintCmd.Print1Dbar(2, 160, 0, 2, 10, "012345678900mannings"));
		setFeedCut(cutter, Integer.valueOf(iline));
//		System.out.println("一维码类型：" + iBarType);
//		mUsbDriver.write(PrintCmd.PrintFeedline(3));
//		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, iBarType, "99884882018052321001004120233233854"));// * 10 CODE128 / *4 CODE39
//		mUsbDriver.write(PrintCmd.PrintFeedline(3));
//		mUsbDriver.write(PrintCmd.Print1Dbar(2, 80, 0, 2, iBarType, "mannings017110271472018052317473800088889999"));
//		mUsbDriver.write(PrintCmd.PrintFeedline(3));
//		mUsbDriver.write(PrintCmd.Print1Dbar(2, 162, 0, 2, iBarType, "mannings0171102714720180523174738"));
//		mUsbDriver.write(PrintCmd.PrintFeedline(3));
//		mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, iBarType, "masungbarcode0171102714720180523174738"));
//		setFeedCut(cutter);
	}


	// 打印位图【指定下载位图的索引】
	private void setPrintNvBmp() {
		String[] NvBmpNums = null;
		NvBmpNums = new String[] { getString(R.string.bmp_1).toString(),
				getString(R.string.bmp_2).toString(),
				getString(R.string.bmp_3).toString(),
				getString(R.string.bmp_4).toString(),
				getString(R.string.bmp_5).toString(),
				getString(R.string.bmp_6).toString() }; // 对齐方式数组
		AlertDialog.Builder b = new Builder(this);
		b.setTitle(getString(R.string.Print_Bmp_btn));
		b.setSingleChoiceItems(NvBmpNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							getPrintNvBmp(1);
							break;
						case 1:
							getPrintNvBmp(2);
							break;
						case 2:
							getPrintNvBmp(3);
							break;
						case 3:
							getPrintNvBmp(4);
							break;
						case 4:
							getPrintNvBmp(5);
							break;
						case 5:
							getPrintNvBmp(6);
							break;
						default:
							break;
						}
					}
				});
		b.show();
	}

	// 打印下载位图
	private void getPrintNvBmp(int iNums) {
		byte[] etBytes = PrintCmd.PrintNvbmp(iNums, 48);
		mUsbDriver.write(etBytes);
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		setFeedCut(cutter);
	}
	// 设置下载位图
	private boolean downloadNvBmp() {
		String loadPath = mBmpPath_et.getText().toString().trim();
		if(!"".equalsIgnoreCase(loadPath)){
			int inums = Utils.Count(loadPath, ";");
			byte[] bValue = PrintCmd.SetNvbmp(inums,loadPath);
			if(bValue != null){
				mUsbDriver.write(bValue, bValue.length);
				return true;
			}
		}
		return false;
	}

	// 显示文件选择路径
	private void showFileChooser() {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*.bin");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try {
			startActivityForResult(Intent.createChooser(intent, "Select a BIN file"),FILE_SELECT_CODE);
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "Please install a File Manager.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:
			if (resultCode == RESULT_OK) {
				// Get the Uri of the selected file
				Uri uri = data.getData();
				String path = Utils.getPath(TestActivity.this, uri);
				if(clickFlag==1){
					String sharePath = Utils.getValue(TestActivity.this, "path", "").toString().trim();
					if(!"".equalsIgnoreCase(sharePath)){
						mBmpPath_et.setText(sharePath + path + ";");
					}else{
						mBmpPath_et.setText(path + ";");
					}
				} else {
					mImgPath_et.setText(path);
				}
			}
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	// 状态查询
	private void checkPrinterStatus(UsbDevice usbDev) {
		int iStatus = getPrinterStatus(usbDev);
		if(checkStatus(iStatus)!=0){
			T.showShort(TestActivity.this, "获取失败");
			return;
		}
		T.showShort(TestActivity.this, "获取成功!");
	}

	/**
	 * 1.测试文本打印
	 * @throws UnsupportedEncodingException
	 */
	private void getPrintTestData(UsbDevice usbDev) throws UnsupportedEncodingException {
		int iStatus = getPrinterStatus(usbDev);
		if(checkStatus(iStatus)!=0)
			return;
		String etstring = "";
		if(Utils.isZh(TestActivity.this)){
			etstring = Constant.TESTDATA_CN;
		}else{
			etstring = Constant.TESTDATA_US;
		}
		if (etstring != null && !"".equals(etstring)) {
//			// 1.阿拉伯语
//			String str = "أنا في الصين  ،  الصين  الناس  المحبة للسلام";
//			String str = "حبا العالم كيف حالك؟";
//			String str = "你好，你好吗？";
//			byte[] etBytes1 = PrintCmd.PrintString(str, 0,"CP1256");// 阿拉伯语 (ISO)iso-8859-6/ 阿拉伯语 (Windows)CP1256/阿拉伯语 (DOS)DOS-720
//			mUsbDriver.write(etBytes1, etBytes1.length,usbDev);
//			mUsbDriver.write(PrintCmd.PrintFeedline(2));
//			byte[] etBytes2 = PrintCmd.PrintString(str, 0,"iso-8859-6");
//			mUsbDriver.write(etBytes2, etBytes2.length,usbDev);
//			mUsbDriver.write(PrintCmd.PrintFeedline(2));
//			byte[] etBytes3 = PrintCmd.PrintString(str, 0,"PC864");
//			mUsbDriver.write(etBytes3, etBytes3.length,usbDev);
//			// 2.葡萄牙语
//			mUsbDriver.write(PrintCmd.PrintFeedline(2));
//			String data = "Temperatura Operação 5° C - 45° C," +
//					"Armazenamento -10° C - 50° C ; Fiabilidade";
//			byte[] etBytes4 = PrintCmd.PrintString(data, 0,"CP860");
//			mUsbDriver.write(etBytes4, etBytes4.length,usbDev);
//			mUsbDriver.write(PrintCmd.PrintFeedline(2));
//			// 3.简体中文测试
			setDataFormat(usbDev); // 常规设置
			byte[] etBytes = PrintCmd.PrintString(etstring, 0);
			mUsbDriver.write(etBytes, etBytes.length,usbDev);
			mUsbDriver.write(PrintCmd.SetAlignment(align),usbDev);
			mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "AB1-CD2-EF3"),usbDev);// * 10 CODE128 / *4 CODE39
			setFeedCut(cutter, Integer.valueOf(iline));
//			mUsbDriver.write(PrintCmd.PrintMarkcutpaper(0)); // 不干胶（黑标）切纸
		}
	}


	// 根据系统语言获取测试文本
	private void getStrDataByLanguage(){
		codeStr = etWrite.getText().toString().trim();
		if("".equalsIgnoreCase(codeStr))
			codeStr = Constant.WebAddress;
		if(Utils.isZh(this)){
			title = Constant.TITLE_CN;
			strData = Constant.STRDATA_CN;
		}else {
			title = Constant.TITLE_US;
			strData = Constant.STRDATA_US;
		}
		num = String.valueOf(Number) + "\n\n";
		Number++;
	}
	// 指定设备：走纸换行、切纸类型
	private void setFeedCut(int iMode, UsbDevice usbDev, int iline) {
		mUsbDriver.write(PrintCmd.PrintFeedline(iline),usbDev);      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode),usbDev);  // 切纸类型
	}
	// 固定---走纸换行数量、切纸类型
	private void setFeedCut(int iMode) {
		mUsbDriver.write(PrintCmd.PrintFeedline(5));      // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode));  // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());           // 清除缓存,初始化
	}
	// 指定---走纸换行数量、切纸类型
	private void setFeedCut(int iMode,int num) {
//		if(iMode == 0){
//			T.showShort(TestActivity.this, "全切："  + cutter);
//		}else{
//			T.showShort(TestActivity.this, "半切："  + cutter);
//		}
		mUsbDriver.write(PrintCmd.PrintFeedline(num));   // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode)); // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());           // 清除缓存,初始化
	}
	// 黑标切纸
	private void setFeedMarkCut(int iMode, int num) {
		mUsbDriver.write(PrintCmd.PrintFeedline(num));   // 走纸换行
		mUsbDriver.write(PrintCmd.PrintMarkposition());  // 定位黑标
		mUsbDriver.write(PrintCmd.PrintCutpaper(iMode)); // 切纸类型
		mUsbDriver.write(PrintCmd.SetClean());           // 清除缓存,初始化
	}
	// 常规设置
	private void setDataFormat(UsbDevice usbDev){
		mUsbDriver.write(PrintCmd.SetAlignment(align),usbDev);    // 对齐方式
		mUsbDriver.write(PrintCmd.SetRotate(rotate),usbDev);      // 字体旋转
		mUsbDriver.write(PrintCmd.SetUnderline(underLine),usbDev);// 下划线
		mUsbDriver.write(PrintCmd.SetLinespace(linespace),usbDev);// 行间距
	}
	private void setDataFormat(){
		mUsbDriver.write(PrintCmd.SetAlignment(align));    // 对齐方式
		mUsbDriver.write(PrintCmd.SetRotate(rotate));      // 字体旋转
		mUsbDriver.write(PrintCmd.SetUnderline(underLine));// 下划线
		mUsbDriver.write(PrintCmd.SetLinespace(linespace));// 行间距
	}

	// 常规设置
	private void setClean() {
		mUsbDriver.write(PrintCmd.SetClean());// 清除缓存,初始化
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mUsbReceiver != null){
            unregisterReceiver(mUsbReceiver);
        }
        mUsbDriver = null;
        mUsbDev = null;
        mUsbDev1 = null;
        mUsbDev2 = null;
	}
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    // setting 选项
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.font_0:      // 文字方向
			rotate = 0;
			break;
		case R.id.font_1:
			rotate = 1;
			break;
		case R.id.align_0:     // 对齐方式
			align = 0;
			break;
		case R.id.align_1:
			align = 1;
			break;
		case R.id.align_2:
			align = 2;
			break;
		case R.id.under_0:     // 下划线
			underLine = 0;
			break;
		case R.id.under_1:
			underLine = 1;
			break;
		case R.id.under_2:
			underLine = 2;
			break;
		case R.id.linespace_30:// 行间距
			linespace = 30;
			break;
		case R.id.linespace_40:
			linespace = 40;
			break;
		case R.id.linespace_50:
			linespace = 50;
			break;
		case R.id.linespace_60:
			linespace = 60;
			break;
		case R.id.linespace_70:
			linespace = 70;
			break;
		case R.id.cutter_0:    // 切刀
			cutter = 0;
			break;
		case R.id.cutter_1:
			cutter = 1;
			break;
		case R.id.system_0:    // 退出系统
			System.exit(0);
			break;
		case R.id.system_1:    // 系统版本
			T.showShort(TestActivity.this, "当前版本USB2.2");
			break;
		case R.id.system_2:// 开启黑标
//			mUsbDriver.write(openBlackMark(0)); // MS-530i
			mUsbDriver.write(openBlackMark2(0));// MS-G530,MS-532IIs,EP802
			break;
		case R.id.system_3:// 关闭黑标
//			mUsbDriver.write(openBlackMark(1)); // MS-530i，
			mUsbDriver.write(openBlackMark2(1));// MS-G530,MS-532IIs,EP802
			break;
		case R.id.system_4:// 连续测试
			//showCustomViewDialog();
			break;
		case R.id.system_5:// 选择设备
//			selectDevice();
			break;
		case R.id.system_6:// 特殊字符指令设置
			doSetting();
			break;
		}
		return super.onOptionsItemSelected(item);
	}


	/**
	 * 打印￥字符 案例
	 */
	private void doSetting() {
		byte[] data = setSpecialText();
		if(data!=null){
			mUsbDriver.write(setSpecialText());
		}
	}

	/**
	 * 设置可打印￥字符
	 * 指令：1b 74 00（选择字符页） 1c 2e（退出中文） 9D（￥） 1b 64 05（走纸）
	 * @return
	 */
	String data = "选择中文退出            ";
	private byte[] setSpecialText() {
		byte[] b_send = new byte[9];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x1b;
		b_send[(iIndex++)] = 0x74;
		b_send[(iIndex++)] = 0x0;
		b_send[(iIndex++)] = 0x1c;
		b_send[(iIndex++)] = 0x2e;
		b_send[(iIndex++)] = (byte) 0x9D;
//		b_send[(iIndex++)] = 0x1b;
//		b_send[(iIndex++)] = 0x64;
//		b_send[(iIndex++)] = 0x5;
		return b_send;
	}

	// 设备选择
	private int dev_flag = 0;
	private void selectDevice() {
		String[] NvBmpNums = null;
		NvBmpNums = new String[] {getString(R.string.dev1).toString(),
				getString(R.string.dev2).toString() }; // 对齐方式数组
		AlertDialog.Builder b = new Builder(this);
		b.setTitle(getString(R.string.Select_Device));
		b.setSingleChoiceItems(NvBmpNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							dev_flag = 1;
							mUsbDev = mUsbDev1;
							break;
						case 1:
							dev_flag = 2;
							mUsbDev = mUsbDev2;
							break;
						default:
							break;
						}
					}
				});
		b.show();
	}
	// -------------------显示消息-----------------------
	private void showMessage(String sMsg) {
		StringBuilder sbMsg = new StringBuilder();
		sbMsg.append(editRecDisp.getText());
		sbMsg.append(m_sdfDate.format(new Date()));
		sbMsg.append(sMsg);
		sbMsg.append("\r\n");
		editRecDisp.setText(sbMsg);
		editRecDisp.setSelection(sbMsg.length(), sbMsg.length());
	}

	// 检测打印机状态
	private int getPrinterStatus(UsbDevice usbDev) {
		int iRet = -1;

		byte[] bRead1 = new byte[1];
		byte[] bWrite1 = PrintCmd.GetStatus1();
		if(mUsbDriver.read(bRead1,bWrite1,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus1(bRead1[0]);
		}

		if(iRet!=0)
			return iRet;

		byte[] bRead2 = new byte[1];
		byte[] bWrite2 = PrintCmd.GetStatus2();
		if(mUsbDriver.read(bRead2,bWrite2,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus2(bRead2[0]);
		}

		if(iRet!=0)
			return iRet;

		byte[] bRead3 = new byte[1];
		byte[] bWrite3 = PrintCmd.GetStatus3();
		if(mUsbDriver.read(bRead3,bWrite3,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus3(bRead3[0]);
		}

		if(iRet!=0)
			return iRet;

		byte[] bRead4 = new byte[1];
		byte[] bWrite4 = PrintCmd.GetStatus4();
		if(mUsbDriver.read(bRead4,bWrite4,usbDev)>0)
		{
			iRet = PrintCmd.CheckStatus4(bRead4[0]);
		}
		return iRet;
	}


	private int checkStatus(int iStatus)
	{
		int iRet = -1;

		StringBuilder sMsg = new StringBuilder();


		//0 打印机正常 、1 打印机未连接或未上电、2 打印机和调用库不匹配
		//3 打印头打开 、4 切刀未复位 、5 打印头过热 、6 黑标错误 、7 纸尽 、8 纸将尽
		switch (iStatus) {
			case 0:
				sMsg.append(normal);       // 正常
				iRet = 0;
				break;
			case 8:
				sMsg.append(paperWillExh); // 纸将尽
				iRet = 0;
				break;
			case 3:
				sMsg.append(printerHeadOpen); //打印头打开
				T.showShort(TestActivity.this, "打印头打开");
				break;
			case 4:
				sMsg.append(cutterNotReset);  //切刀未复位
				break;
			case 5:
				sMsg.append(printHeadOverheated); //打印头过热
				break;
			case 6:
				sMsg.append(blackMarkError);  //黑标错误
				break;
			case 7:
				sMsg.append(paperExh);     // 纸尽==缺纸
				break;
			case 1:
				sMsg.append(notConnectedOrNotPopwer);//打印机未连接或未上电
				break;
			default:
				sMsg.append(abnormal);     // 异常
				break;
		}
		showMessage(sMsg.toString());
//		T.showShort(TestActivity.this, "返回值：" + String.valueOf(iRet));
		return iRet;
	}

	public synchronized void sleep(long msec) {
		try {
			wait(msec);
		} catch (InterruptedException e) {
		}
	}

	@SuppressLint("NewApi")
	public void split_bytes(byte[] bytes, int copies) {
		double split_length = Double.parseDouble(copies + "");
		int array_length = (int) Math.ceil(bytes.length / split_length);
		byte[] result = new byte[copies];
		int from, to;
		for (int i = 0; i < array_length; i++) {
			from = (int) (i * split_length);
			to = (int) (from + split_length);
			if (to > bytes.length)
				to = bytes.length;
			// 将数组分为n段发送,bytes表示发送任务，copies表示每段发送区间或大小
			result = Arrays.copyOfRange(bytes, from, to);
			mUsbDriver.write(PrintCmd.PrintString(new String(result), 0));
			int state = PrintCmd.getPrintEndStatus(mUsbDriver);
			// 判断如果上述查询10次，打印机有1次不返回值，此时查询打印机是否异常：缺纸等
			if(state==-1){
				T.showShort(TestActivity.this,"发送1D 72 01的指令返回：" + state); // 指令发送，打印机返回值
				int iStatus2 = getPrinterStatus(mUsbDev);
				if(checkStatus(iStatus2)!=0)
					return;
			}
		}
	}
	// 通过系统语言判断Message显示
	String receive = "", state = ""; // 接收提示、状态类型
	String normal = "",notConnectedOrNotPopwer = "",notMatch = "",
			printerHeadOpen = "", cutterNotReset = "", printHeadOverheated = "",
			blackMarkError = "",paperExh = "",paperWillExh = "",abnormal = "";
	private void getMsgByLanguage() {
		if (Utils.isZh(this)) {
			receive = Constant.Receive_CN;
			state = Constant.State_CN;
			normal = Constant.Normal_CN;
			notConnectedOrNotPopwer = Constant.NoConnectedOrNoOnPower_CN;
			notMatch = Constant.PrinterAndLibraryNotMatch_CN;
			printerHeadOpen = Constant.PrintHeadOpen_CN;
			cutterNotReset = Constant.CutterNotReset_CN;
			printHeadOverheated = Constant.PrintHeadOverheated_CN;
			blackMarkError = Constant.BlackMarkError_CN;
			paperExh = Constant.PaperExhausted_CN;
			paperWillExh = Constant.PaperWillExhausted_CN;
			abnormal = Constant.Abnormal_CN;
		} else {
			receive = Constant.Receive_US;
			state = Constant.State_US;
			normal = Constant.Normal_US;
			notConnectedOrNotPopwer = Constant.NoConnectedOrNoOnPower_US;
			notMatch = Constant.PrinterAndLibraryNotMatch_US;
			printerHeadOpen = Constant.PrintHeadOpen_US;
			cutterNotReset = Constant.CutterNotReset_US;
			printHeadOverheated = Constant.PrintHeadOverheated_US;
			blackMarkError = Constant.BlackMarkError_US;
			paperExh = Constant.PaperExhausted_US;
			paperWillExh = Constant.PaperWillExhausted_US;
			abnormal = Constant.Abnormal_US;
		}
	}

	/**
	 * 自检页打印
	 */
	private void printSelfPageTest() {
		try {
			byte[] spCmd = PrintCmd.PrintSelfcheck();
			if (spCmd != null) {
				mUsbDriver.write(spCmd);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 检测打印机状态
	private int getPrinterStatus2(UsbDevice usbDev) {
		int iRet = -1;
		byte[] bRead5 = new byte[1];
		byte[] bWrite5 = sendCommand();
		if (mUsbDriver.read(bRead5, bWrite5, usbDev) > 0) {
//			T.showShort(TestActivity.this, "返回值：" + String.valueOf(bRead5[0]));
			iRet = checkStatus(bRead5[0]);
		}
		if (iRet == 0 || iRet > 0)
			return iRet;
		return iRet;
	}
	// 发送打印完成指令 1D 72 01
	private byte[] sendCommand() {
		byte[] b_send = new byte[3];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x1D;
		b_send[(iIndex++)] = 0x72;
		b_send[(iIndex++)] = 0x01;
		return b_send;
	}
	// 解析
	public static int checkStatus(byte bRecv) {
		if ((bRecv & 0x00) == 0x00)
			return 0;  // 打印纸充足
		if ((bRecv & 0x03) == 0x03)
			return 1;  // 打印纸将尽
		if ((bRecv & 0x60) != 0x60)
			return 2;  // 打印机非空闲状态
		return 3;      // 空闲状态
	}

	// 开启黑标功能
	private byte[] openBlackMarkOption(int bmType) {
		byte[] b_send = new byte[4];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x13;
		b_send[(iIndex++)] = 0x74;
		b_send[(iIndex++)] = 0x11;
		b_send[(iIndex++)] = 0x11;
		b_send[(iIndex++)] = (byte) bmType;
		if(bmType == 0)
			b_send[(iIndex++)] = 0x11;        // 有效
		else
			b_send[(iIndex++)] = (byte) 0xEE; // 无效
		return b_send;
	}

	/**
	 * 开启黑标功能指令【MS-530i控制板指令】  "0x13 0x74 0x22 0x23"，设置黑标功能有效  / A3无效
	 * @param bmType  0  黑标有效, 1 黑标无效
	 * @return byte[]
	 */
	private byte[] openBlackMark(int bmType) {
		byte[] b_send = new byte[4];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x13;
		b_send[(iIndex++)] = 0x74;
		b_send[(iIndex++)] = 0x22;
		if(bmType == 0)
			b_send[(iIndex++)] = 0x23;
		else
			b_send[(iIndex++)] = (byte) 0xA3;
		return b_send;
	}

	/**
	 * 开启黑标功能指令【MS-G530控制板指令】  "0x13 0x74 0x44 0x33 0x33"，设置黑标功能有效  / 0xCC无效
	 * @param bmType  0  黑标有效, 1 黑标无效
	 * @return byte[]
	 * 机型：EP802、G530、TS101
	 */
	private byte[] openBlackMark2(int bmType) {
		byte[] b_send = new byte[5];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x13;
		b_send[(iIndex++)] = 0x74;
		b_send[(iIndex++)] = 0x44;
		b_send[(iIndex++)] = 0x33;
		if(bmType == 0)
			b_send[(iIndex++)] = 0x33;
		else
			b_send[(iIndex++)] = (byte) 0xCC;
		return b_send;
	}


	private void setBlackMarkAD() {
        int AD_Value = Integer.valueOf(setADValue.getText().toString().trim());
        if (AD_Value >= 32 || AD_Value <= 128) {
            int mark = mUsbDriver.write(setMarkADLimitValue(AD_Value));
            mMarkADValue.setText("黑标AD值：" + String.valueOf(AD_Value));
            if (mark != 0 || mark > 0) {
                Toast.makeText(this, "返回值：" +
                        String.valueOf(mark), Toast.LENGTH_SHORT).show();
            }
        } else {
        	mMarkADValue.setText("AD值有误，请重试");
        }

    }

    // 设置MIP6 黑标检测的AD界限值
    private static byte[] setMarkADLimitValue(int adLimitValue) {
        byte[] bCmd = new byte[6];
        int iIndex = 0;
        bCmd[iIndex++] = 0x13;
        bCmd[iIndex++] = 0x74;
        bCmd[iIndex++] = 0x11;
        bCmd[iIndex++] = 0x66;
        if (adLimitValue < 32) {
            adLimitValue = 32;
        }
        if (adLimitValue > 128) {
            adLimitValue = 128;
        }
        bCmd[iIndex++] = (byte) adLimitValue;
        return bCmd;
    }

    /**
     * 设置黑标的偏移（切纸)
     *
     * @param mUsbDriver
     */
	public static void setBlackLabelOffset(UsbDriver mUsbDriver, int number) {
		/**
		 * 切纸： 中影 72或者73mm 新办法：77mm 完美金典院线 72mm mc 76mm 标准票纸：88mm
		 * 红色联娱票纸切纸：76mm
		 */
		byte[] etBytes = new byte[6];
		int iIndex = 0;
		byte[] bytes = intToByte(number);
		etBytes[iIndex++] = 0x13;
		etBytes[iIndex++] = 0x74;
		etBytes[iIndex++] = 0x11;
		etBytes[iIndex++] = 0x78;
		etBytes[iIndex++] = bytes[0];
		etBytes[iIndex++] = bytes[1];
		mUsbDriver.write(etBytes, iIndex);
	}

	/**
	 * 十进制转化为十六进制换算
	 * @param number
	 * @return
	 */
	public static byte[] intToByte(int number) {
		byte[] bytes = new byte[2];
		int a = number * 8;
		if (a > 255) {
			bytes[0] = (byte) (a >> 8);
			bytes[1] = (byte) (a << 8 >> 8);
		} else {
			bytes[0] = 0;
			bytes[1] = (byte) a;
		}
		return bytes;
	}

	// =============================连续测试=================================
	/**
	 * 6.连续打印小票
	 */
	private void continuePrintTicket(long interval,int execution) {
		if (interval == 0 || execution == 0) {
			T.showShort(TestActivity.this, "连续打印间隔时间或执行次数不能为空");
			return;
		}
		printTest(interval,execution);
	}
	// 小票连续打印变量
	static int Num = 1;
	private String nums = "";
	private void getPrintNum(){
		nums = String.valueOf(Num);
		Num++;
	}
	// 小票连续打印次数和间隔时间设置
	static boolean timeFlag = true;
	private void printTest(long interTime, int exeTimes) {
		while (timeFlag) {
			sleep(interTime);
			getPrintNum();
			getPrintTest(mUsbDriver, mUsbDev);
			if (exeTimes == Integer.valueOf(Num) - 1) {
				Num = 1;
				break;
			}
			T.showShort(TestActivity.this, "第" + String.valueOf(Num) + "张");
		}
	}

	private void getPrintTest(UsbDriver usbDriver, UsbDevice usbDev) {
		int iStatus = getPrinterStatus(usbDev);
		if(checkStatus(iStatus)!=0)
			return;
		String etstring = "";
		if(Utils.isZh(TestActivity.this)){
			etstring = Constant.TESTDATA_CN;
		}else{
			etstring = Constant.TESTDATA_US;
		}
		if (etstring != null && !"".equals(etstring)) {
			setDataFormat(usbDev); // 常规设置
			usbDriver.write(PrintCmd.PrintString(etstring + "   " + nums, 0),usbDev);
			setFeedCut(cutter,4);
		}
	}

	//声明一个AlertDialog构造器
//    private AlertDialog.Builder builder;
//    EditText interval_et,execution_et;
//	private void showCustomViewDialog() {
//		builder = new AlertDialog.Builder(this);
//		builder.setIcon(R.drawable.ic_launcher);
//		builder.setTitle(R.string.ContinuePrint);
//		/**
//		 * 设置内容区域为自定义View
//		 */
//		LinearLayout printDialog = (LinearLayout) getLayoutInflater().inflate(
//				R.layout.pop_continue_print, null);
//		builder.setView(printDialog);
////		EditText interval_et,execution_et;
//        interval_et =  (EditText)printDialog.findViewById(R.id.interval_time_et);
//        execution_et =  (EditText)printDialog.findViewById(R.id.execution_times_et);
//        Button print = (Button)printDialog.findViewById(R.id.start_print_Btn);
//        print.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//            	if(!printConnStatus()){
//    				return;
//    			}
//                // 执行连续打印操作
//            	intervalTime = Long.valueOf(interval_et.getText().toString().trim());
//         		executionTimes = Integer.valueOf(execution_et.getText().toString().trim());
//                continuePrintTicket(intervalTime,executionTimes);
//            }
//        });
//		builder.setCancelable(true);
//		AlertDialog dialog = builder.create();
//		dialog.show();
//	}
	// =============================结束测试=================================

	// -------20180809--START-TT101长城----------------------------------------
	private void printFeedForward()
	{
		int iValue = 0;
		String strValue = mFeedDotLength.getText().toString().trim();
		try
		{
			iValue = Integer.valueOf(strValue);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		mUsbDriver.write(PrintUtils.PrintFeedForward(iValue));
	}

	private void printFeedBack()
	{
		int iValue = 0;


		String strValue = mFeedDotLength.getText().toString().trim();
		try
		{
			iValue = Integer.valueOf(strValue);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}

		mUsbDriver.write(PrintUtils.PrintFeedBack(iValue));
	}

	// 检测打印机状态_TS101
	private int getPrinterStatus_TS(UsbDevice usbDev) {
		int iRet = -1;

		byte[] bRead = new byte[4];
		byte[] bWrite = PrintCmd.GetStatus();

		if (mUsbDriver.read(bRead, bWrite, usbDev) > 0) {
			iRet = com.printsdk.utils.PrintUtils.CheckStatus_TS(bRead);
		}
		T.showShort(TestActivity.this,
				"状态值:" + iRet + "\r\n返回值10 04 01：" + String.valueOf(bRead[0])
						+ ";10 04 02：" + String.valueOf(bRead[1])
						+ ";10 04 03：" + String.valueOf(bRead[2])
						+ ";10 04 04：" + String.valueOf(bRead[3]));
		return iRet;
	}

	private void printSetMark(int iValue) {
		try {
			byte[] bCmd = new byte[4];
			int iIndex = 0;
			bCmd[iIndex++] = 0x13;
			bCmd[iIndex++] = (byte) 0xA0;
			bCmd[iIndex++] = 0x21;
			if (iValue == 0)
				bCmd[iIndex++] = 0x0;
			else
				bCmd[iIndex++] = 0x1;

			mUsbDriver.write(bCmd);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	// -------20180809--END-------------------------------------------
	/**
	 * 开启钱箱
	 */
	private void openCashBox(){
		byte[] bSend = PrintCmd.SetOpenCashBoxCmd();
		mUsbDriver.write(bSend, bSend.length);
	}
	/**
	 * 开启电子锁
	 */
	private void openEleLock() {
		byte[] bSend = PrintCmd.SetOpenEleLockCmd();
		mUsbDriver.write(bSend, bSend.length);
	}

	private void setPrintDessity(int type) {
		String dessityData = setDessity.getText().toString().trim();
		densityValue = Integer.valueOf(dessityData);
		if (densityValue >= 70 && densityValue <= 200 && !TextUtils.isEmpty(dessityData)) {
			if(type == 0){
				mUsbDriver.write(getPrintDessityI(densityValue));
			}else{
				mUsbDriver.write(getPrintDessityII(densityValue));
			}
		} else {
			T.showShort(TestActivity.this, "浓度值超出范围！正常范围为：70-200;\n或者浓度输入值为空,请检查后重试！");
		}
	}

	// 获取不同系列打印浓度设置类型
	int dType_flag = 1;
	private void getDessityType() {
		String[] NvBmpNums = null;
		NvBmpNums = new String[] {"D347-D245-532i","EP802-532ii-G530"}; // 对齐方式数组
		AlertDialog.Builder b = new Builder(this);
		b.setTitle("浓度设置类型：");
		b.setSingleChoiceItems(NvBmpNums, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							dType_flag = 0;
							setPrintDessity(dType_flag);
							break;
						case 1:
							dType_flag = 1;
							setPrintDessity(dType_flag);
							break;
						default:
							break;
						}
					}
				});
		b.show();
	}
	/*
	 * 设置打印的浓度 指令十六进制 12 7e N(70 <= N <= 200，十进制数) N的值在这个范围内才有效，其他值无效
	 */
	/**
	 * 设置打印浓度   输入框【D347,D245,532i】
	 * @param dessity
	 * @return
	 */
	private byte[] getPrintDessityI(int dessity) {
		byte[] b_send = new byte[3];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x12;
		b_send[(iIndex++)] = 0x7e;
		b_send[(iIndex++)] = (byte) dessity;
		return b_send;
	}
	
	/**
	 * 设置打印浓度2  输入框 【EP802,532ii,G530】
	 * @param dessity
	 * @return
	 */
	private byte[] getPrintDessityII(int dessity) {
		byte[] b_send = new byte[5];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x13;
		b_send[(iIndex++)] = 0x74;
		b_send[(iIndex++)] = 0x44;
		b_send[(iIndex++)] = 0x66;
		b_send[(iIndex++)] = (byte) dessity;
		return b_send;
	}
	
	/**
	 *  退纸指令 1B 4b n（n 以mm为单位，加个退纸指令，打印之前设置；不要退纸太多；以免退出来了）
	 */
	private byte[] setBackPaper(int backlength){
		byte[] b_send = new byte[3];
		int iIndex = 0;
		b_send[(iIndex++)] = 0x1B;
		b_send[(iIndex++)] = 0x4B;
		b_send[(iIndex++)] = (byte) backlength;
		return b_send;
	}
	
	private void getBlackMarkOption(){
		/**打印效果补偿值选项Spinner*/
        final List<String> datas = new ArrayList<>();
        datas.add(getString(R.string.Effective_spinner));
        datas.add(getString(R.string.Invalid_spinner));
        MyAdapter adapter2 = new MyAdapter(this);
        mSpinnerBlackMark.setAdapter(adapter2);
        adapter2.setDatas(datas);
		/** 选项选择监听 */
        mSpinnerBlackMark.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
						switch (position) {
						case 0:
							mBMOption = 0;
							T.showShort(TestActivity.this,
									"选择：" + datas.get(0));
							break;
						case 1:
							mBMOption = 1;
							T.showShort(TestActivity.this,
									"选择：" + datas.get(1));
							break;
						default:
							break;
						}
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}

				});
	}
	
	/**
     * 设置不常用类型打印
     */
	int printType = 1;
    private void getPrintTypeList(){
		/**打印效果补偿值选项Spinner*/
        final List<String> datas = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            datas.add(String.valueOf(i));
        }
        MyAdapter adapter2 = new MyAdapter(this);
        mSelectPrintType.setAdapter(adapter2);
        adapter2.setDatas(datas);
		/** 选项选择监听 */
        mSelectPrintType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
						printType = Integer.valueOf(datas.get(position));
						if(printType==1){
							printLabel.setText("开启电子锁");
							T.showShort(TestActivity.this,"开启电子锁：" + datas.get(position));
						}else if(printType==2){
							printLabel.setText("开启钱箱");
							T.showShort(TestActivity.this,"开启钱箱：" + datas.get(position));
						}else if(printType==3){
							printLabel.setText("指令打印");
							T.showShort(TestActivity.this,"测试输入指令打印：" + datas.get(position));
						}
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						
					}
				});
	}
	
    /**
     * 设置国家区域列表
     */
	private void getCountryDataList(){
        final List<String> datas = new ArrayList<>();
        datas.add("中文");
        datas.add("韩文");
        datas.add("日文");
        datas.add("ASCLL码");
        MyAdapter adapter2 = new MyAdapter(this);
        mSpinnerCountry.setAdapter(adapter2);
        adapter2.setDatas(datas);
		/** 选项选择监听 */
        mSpinnerCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
						switch (position) {
						case 0:
							countryValue = 0;
							T.showShort(TestActivity.this,
									"选择：" + datas.get(0));
							break;
						case 1:
							countryValue = 1;
							T.showShort(TestActivity.this,
									"选择：" + datas.get(1));
							break;
						case 2:
							countryValue = 2;
							T.showShort(TestActivity.this,
									"选择：" + datas.get(2));
							break;
						case 3:
							countryValue = 3;
							T.showShort(TestActivity.this,
									"选择：" + datas.get(3));
							break;
						default:
							break;
						}
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}

				});
	}
	/**
	 * 设置代码页列表
	 */
	private void getCodePageDataList() {
		final List<String> datas = new ArrayList<>();
		datas.add("PC437[美国欧洲标准]");   // 0x00
        datas.add("PC737(Greek)");      // 0x40
        datas.add("PC850(Mutilingual)");// 0x02
        datas.add("PC850(Latin)");      // 0x12
        datas.add("PC855(Bulgarian)");  // 0x3c
        datas.add("PC857(Turkey)");     // 0x3d
        datas.add("PC858");             // 0x13
        datas.add("PC860(Portugal)");   // 0x03
        datas.add("PC861(Icelandic)");  // 0x38
        datas.add("PC862(Hebrew)");     // 0x08
        datas.add("PC863(Canadian)");   // 0x04
        datas.add("PC864(Arabic)");     // 0x3f
        datas.add("PC865(Nordic)");     // 0x05
        
        datas.add("PC866(Russian)");    // 0x3b
        datas.add("PC874(Thai)");       // 0x46
        datas.add("PC1251(Cyrillic)");  // 0x49
        datas.add("PC1252");            // 0x10
        datas.add("PC1255(Israel)");    // 0x20
        datas.add("PC1257");            // 0x19
        datas.add("Iran");              // 0x0a
        datas.add("Katakana");          // 0x01
		MyAdapter adapter2 = new MyAdapter(this);
		mSpinnerCodePage.setAdapter(adapter2);
		adapter2.setDatas(datas);
		/** 选项选择监听 */
		mSpinnerCodePage
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
//						T.showShort(TestActivity.this,
//								"点击了：" + datas.get(position));
//						CPnumberValue = Integer.valueOf(datas.get(position));
						switch (position) {
						case 0:
							CPnumberValue = 0;  // 0x00
							T.showShort(TestActivity.this,
									"选择：" + datas.get(0));
							break;
						case 1:
							CPnumberValue = 64; // 0x40
							T.showShort(TestActivity.this,
									"选择：" + datas.get(1));
							break;
						case 2:
							CPnumberValue = 2;  // 0x02
							T.showShort(TestActivity.this,
									"选择：" + datas.get(2));
							break;
						case 3:
							CPnumberValue = 18; // 0x12
							T.showShort(TestActivity.this,
									"选择：" + datas.get(3));
							break;
						case 4:
							CPnumberValue = 60; // 0x3c
							T.showShort(TestActivity.this,
									"选择：" + datas.get(4));
							break;
						case 5:
							CPnumberValue = 61; // 0x3d
							T.showShort(TestActivity.this,
									"选择：" + datas.get(5));
							break;
						case 6:
							CPnumberValue = 19; // 0x13
							T.showShort(TestActivity.this,
									"选择：" + datas.get(6));
							break;
						case 7:
							CPnumberValue = 3;  // 0x03
							T.showShort(TestActivity.this,
									"选择：" + datas.get(7));
							break;
						case 8:
							CPnumberValue = 56; // 0x38
							T.showShort(TestActivity.this,
									"选择：" + datas.get(8));
							break;
						case 9:
							CPnumberValue = 8;  // 0x08
							T.showShort(TestActivity.this,
									"选择：" + datas.get(9));
							break;
						case 10:
							CPnumberValue = 4;  // 0x04
							T.showShort(TestActivity.this,
									"选择：" + datas.get(10));
							break;
						case 11:
							CPnumberValue = 63; // 0x3f
							T.showShort(TestActivity.this,
									"选择：" + datas.get(11));
							break;
						case 12:
							CPnumberValue = 5;  // 0x05
							T.showShort(TestActivity.this,
									"选择：" + datas.get(12));
							break;
							
						case 13:
							CPnumberValue = 59; // 0x3b
							T.showShort(TestActivity.this,
									"选择：" + datas.get(13));
							break;
						case 14:
							CPnumberValue = 70; // 0x46
							T.showShort(TestActivity.this,
									"选择：" + datas.get(14));
							break;
						case 15:
							CPnumberValue = 73; // 0x49
							T.showShort(TestActivity.this,
									"选择：" + datas.get(15));
							break;
						case 16:
							CPnumberValue = 16; // 0x10
							T.showShort(TestActivity.this,
									"选择：" + datas.get(16));
							break;
						case 17:
							CPnumberValue = 32; // 0x20
							T.showShort(TestActivity.this,
									"选择：" + datas.get(17));
							break;
						case 18:
							CPnumberValue = 25; // 0x19
							T.showShort(TestActivity.this,
									"选择：" + datas.get(18));
							break;
						case 19:
							CPnumberValue = 10; // 0x0a
							T.showShort(TestActivity.this,
									"选择：" + datas.get(19));
							break;
						case 20:
							CPnumberValue = 5; // 0x01
							T.showShort(TestActivity.this,
									"选择：" + datas.get(20));
							break;
						default:
							break;
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}

				});
	}
}
