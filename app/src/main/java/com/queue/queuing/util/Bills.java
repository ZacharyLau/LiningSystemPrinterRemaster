package com.queue.queuing.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.TextUtils;

import com.printsdk.cmd.PrintCmd;
import com.printsdk.usbsdk.UsbDriver;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Bills {
	// 国际化标志时间格式类
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private static Context context;
	public Bills(int cutter){
		
	}
	
	/**
	 * 1.排队小票
	 */
	public static void printSmallTicket(UsbDriver mUsbDriver, String title, String num, String codeStr, int cutter, JSONObject queuePrintingBuffer) {
		String store_name = "";
		//String store_name2 = "";
		String store_tel = "";
		String store_address = "";
		String line_number = "";
		String line_people = "";
		String line_num = "";
		String line_qrcode = "";
		String line_start_time = "";
		String language="";
		try{
			store_name = queuePrintingBuffer.getString("store_name");
			store_tel = queuePrintingBuffer.getString("store_tel");
			store_address = queuePrintingBuffer.getString("store_address");
			line_number = queuePrintingBuffer.getString("number");
			line_people = queuePrintingBuffer.getString("people");
			line_num = queuePrintingBuffer.getString("num");
			line_qrcode = queuePrintingBuffer.getString("qrcode");
			line_start_time = queuePrintingBuffer.getString("start_time");
			language = queuePrintingBuffer.getString("language");


			if(language.equals("CN")) {
				// store name
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				mUsbDriver.write(PrintCmd.PrintString((Html.fromHtml(store_name) + ""), 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				// telephone
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				String printingString = "电话: " + store_tel;
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				//mUsbDriver.write(PrintCmd.SetBold(0));
				//mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				//mUsbDriver.write(PrintCmd.PrintFeedline(1)); // 打印走纸1行
				mUsbDriver.write(PrintCmd.PrintString("------------------------------", 0));
				//mUsbDriver.write(PrintCmd.PrintFeedline(1)); // 打印走纸1行


				//line number
				mUsbDriver.write(PrintCmd.SetBold(1));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(1, 1));
				printingString = line_number + "号";
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				//number of customer
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				printingString = "就餐人数： " + line_people;
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				// number of order in front
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				printingString = "您前面还有： " + line_people + "位在等待";
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				mUsbDriver.write(PrintCmd.PrintFeedline(1)); // 打印走纸1行
				// 二维码
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.PrintQrcode(line_qrcode, 15, 5, 1));           // 【1】MS-D347,13 52指令二维码接口，环绕模式1
//				mUsbDriver.write(PrintCmd.PrintQrcode(line_qrcode, 12, 2, 0));           // 【2】MS-D245,MSP-100二维码，左边距、size、环绕模式0
//				mUsbDriver.write(PrintCmd.PrintQrCodeT500II(5,Constant.WebAddress_zh));// 【3】MS-532II+T500II二维码接口

				mUsbDriver.write(PrintCmd.PrintFeedline(1));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.PrintString("扫码了解排队动态", 0));

				mUsbDriver.write(PrintCmd.PrintFeedline(1));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				// 日期时间
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				printingString = "取号时间： " + sdf.format(new Date()).toString();
				//mUsbDriver.write(PrintCmd.PrintString(sdf.format(new Date()).toString()+ "\n\n", 1));
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				mUsbDriver.write(PrintCmd.PrintString("------------------------------", 0));
				mUsbDriver.write(PrintCmd.PrintString("系统提供：鹿大胃 647-7729-729", 0));
			}else{
				// store name
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				mUsbDriver.write(PrintCmd.PrintString((Html.fromHtml(store_name) + ""), 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				// telephone
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				String printingString = "TEL: " + store_tel;
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				//mUsbDriver.write(PrintCmd.SetBold(0));
				//mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				//mUsbDriver.write(PrintCmd.PrintFeedline(1)); // 打印走纸1行
				mUsbDriver.write(PrintCmd.PrintString("------------------------------", 0));
				//mUsbDriver.write(PrintCmd.PrintFeedline(1)); // 打印走纸1行


				//line number
				mUsbDriver.write(PrintCmd.SetBold(1));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(1, 1));
				printingString = "No."+line_number;
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				//number of customer
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				printingString = "Number of Guest： " + line_people;
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				// number of order in front
				mUsbDriver.write(PrintCmd.SetBold(0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				printingString = "There are " + line_people + " order(s) ahead";
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(0));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				mUsbDriver.write(PrintCmd.PrintFeedline(1)); // 打印走纸1行
				// 二维码
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.PrintQrcode(line_qrcode, 15, 5, 1));           // 【1】MS-D347,13 52指令二维码接口，环绕模式1
//				mUsbDriver.write(PrintCmd.PrintQrcode(line_qrcode, 12, 2, 0));           // 【2】MS-D245,MSP-100二维码，左边距、size、环绕模式0
//				mUsbDriver.write(PrintCmd.PrintQrCodeT500II(5,Constant.WebAddress_zh));// 【3】MS-532II+T500II二维码接口

				mUsbDriver.write(PrintCmd.PrintFeedline(1));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.PrintString("Scan QRcode to check line status", 0));

				mUsbDriver.write(PrintCmd.PrintFeedline(1));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				// 日期时间
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

				printingString = "Ordered at： " + sdf.format(new Date()).toString();
				//mUsbDriver.write(PrintCmd.PrintString(sdf.format(new Date()).toString()+ "\n\n", 1));
				mUsbDriver.write(PrintCmd.PrintString(printingString, 0));
				mUsbDriver.write(PrintCmd.SetAlignment(1));
				mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
				mUsbDriver.write(PrintCmd.PrintString("------------------------------", 0));
				mUsbDriver.write(PrintCmd.PrintString("Provided by Mr.Moo", 0));
				mUsbDriver.write(PrintCmd.PrintString("TEL: 647-7729-729", 0));
			}



			// 一维条码
//			mUsbDriver.write(PrintCmd.SetAlignment(1));
//			mUsbDriver.write(PrintCmd.Print1Dbar(2, 100, 0, 2, 10, "A12345678Z"));// 一维条码打印
//			mUsbDriver.write(PrintCmd.SetAlignment(0));
			// 走纸换行、切纸、清理缓存
			mUsbDriver.write(PrintCmd.PrintMarkcutpaper(cutter)); // 不干胶（黑标）切纸
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * 2.银行小票
	 * @throws UnsupportedEncodingException
	 */
	public static void printBankBill(UsbDriver mUsbDriver, int cutter){
		mUsbDriver.write(PrintCmd.SetAlignment(1));
		mUsbDriver.write(PrintCmd.PrintString("交通银行自助设备交易凭条", 0));
		mUsbDriver.write(PrintCmd.PrintFeedline(3));
		mUsbDriver.write(PrintCmd.SetAlignment(0));
		mUsbDriver.write(PrintCmd.PrintString("  交易类型：交通银行太平洋借记卡发卡确认单" + "\n" + "  电子柜员：ABHB347", 0));
		mUsbDriver.write(PrintCmd.PrintFeedline(2));
		mUsbDriver.write(PrintCmd.PrintString("借记卡号：62226206100212****4"
									+ "\n" + "流水号码：102200ABHB347642959"
									+ "\n" + "开卡日期：20171022           开卡时间：16:16:12"
									+ "\n" + "客户姓名：*成         性别：男"
									+ "\n" + "身份证号：4210831993060****0"
									+ "\n" + "手机号码：186821****1"
									+ "\n" + "机构号码：01421091999        终端号码：8ABHB347"
									+ "\n" + "签约信息：---------------------------"
									+ "\n" + "电话银行：未开通"
									+ "\n" + "自助银行：签约成功"
									+ "\n" + "网上银行：未开通"
									+ "\n" + "手机银行：未开通"
									+ "\n" + " 银信通：未开通"
									+ "\n" + "贷记卡自助还款：未开通"
									+ "\n\n" + "感谢您使用"+"“"+"交通银行自助发卡机"+"” !"
									+ "\n" + "请妥善保管交易凭条；交通银行客户热线：95559"
									, 0));
		mUsbDriver.write(PrintCmd.PrintFeedline(4));
		mUsbDriver.write(PrintCmd.PrintCutpaper(cutter)); // 不干胶（黑标）切纸
	}
	
	/**
	 * 3.共享小票（酷玩共享玩具）
	 */
	public static void printShareTicket(UsbDriver mUsbDriver, String codeStr, int cutter) {
		try{
			// 二维码
			mUsbDriver.write(PrintCmd.SetAlignment(0));
			mUsbDriver.write(PrintCmd.SetSizetext(0, 0));
			mUsbDriver.write(PrintCmd.SetLeftmargin(0));
			mUsbDriver.write(PrintCmd.SetBold(1));
			// 标题
			mUsbDriver.write(PrintCmd.PrintString("酷玩共享玩具", 0));
			// 订单信息
			mUsbDriver.write(PrintCmd.SetBold(0));
			mUsbDriver.write(PrintCmd.PrintString(
					"订单号：1234567890 " +
					"\n用户手机：138*****88" +
					"\n归还时间：" + sdf.format(new Date()).toString(), 0));
//			mUsbDriver.write(PrintCmd.SetLeftmargin(200));
			mUsbDriver.write(PrintCmd.PrintQrcode("酷玩共享玩具", 27, 3, 0));
//			// 用户手机
//			mUsbDriver.write(PrintCmd.PrintString("用户手机：138*****88", 0)); 
//			// 归还日期
//			mUsbDriver.write(PrintCmd.PrintString("归还时间："+ sdf.format(new Date()).toString()
//					+ "\n\n", 1)); 
			// 走纸换行、切纸、清理缓存
			mUsbDriver.write(PrintCmd.PrintFeedline(5)); // 不干胶（黑标）切纸
			mUsbDriver.write(PrintCmd.PrintCutpaper(cutter)); // 不干胶（黑标）切纸
//			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	/**
	 * 4.电影票打印
	 * @param mUsbDriver
	 */
	public static void printCinemaTicket(UsbDriver mUsbDriver, int cutter) {
		byte[] strCmd = new byte[2];
		strCmd[0] = (byte) 0x1B;
		strCmd[1] = (byte) 0x40;
		mUsbDriver.write(strCmd, strCmd.length);
		// 进入页模式
		byte[] strCmd1 = new byte[2];
		strCmd1[0] = (byte) 0x11;
		strCmd1[1] = (byte) 0x21;
		mUsbDriver.write(strCmd1, strCmd1.length);
		// 设置页模式打印高度
		byte[] strCmd2 = new byte[6];
		strCmd2[0] = (byte) 0x11;
		strCmd2[1] = (byte) 0x23;
		strCmd2[2] = (byte) 0x02;
		strCmd2[3] = (byte) 0x40;
		strCmd2[4] = (byte) 0x02;
		strCmd2[5] = (byte) 0x40;
		mUsbDriver.write(strCmd2, strCmd2.length);
		// 设置打印起始点
		byte[] strCmd3 = new byte[6];
		strCmd3[0] = (byte) 0x11;
		strCmd3[1] = (byte) 0x24;
		strCmd3[2] = (byte) 0x00;
		strCmd3[3] = (byte) 0x35;
		strCmd3[4] = (byte) 0x00;
		strCmd3[5] = (byte) 0x65;
		mUsbDriver.write(strCmd3, strCmd3.length);// 影厅-坐标定位
		// 对应影厅【31 BA C5 CC FC--1号厅】
		byte[] strCmd4 = new byte[5];
		strCmd4[0] = (byte) 0x31;
		strCmd4[1] = (byte) 0xBA;
		strCmd4[2] = (byte) 0xC5;
		strCmd4[3] = (byte) 0xCC;
		strCmd4[4] = (byte) 0xFC;
		mUsbDriver.write(strCmd4, strCmd4.length);
		// 设置打印起始点
		byte[] strCmd5 = new byte[6];
		strCmd5[0] = (byte) 0x11;
		strCmd5[1] = (byte) 0x24;
		strCmd5[2] = (byte) 0x01;
		strCmd5[3] = (byte) 0x15;
		strCmd5[4] = (byte) 0x00;
		strCmd5[5] = (byte) 0x65;
		mUsbDriver.write(strCmd5, strCmd5.length);// 时间-坐标定位
		/**
		 *  电影票具体文本内容打印
		 */
		// 对应时间【32 30 31 37 2D 30 31 2D 31 30】
		byte[] strCmd6 = new byte[10];
		strCmd6[0] = (byte) 0x32;
		strCmd6[1] = (byte) 0x30;
		strCmd6[2] = (byte) 0x31;
		strCmd6[3] = (byte) 0x37;
		strCmd6[4] = (byte) 0x2D;
		strCmd6[5] = (byte) 0x30;
		strCmd6[6] = (byte) 0x31;
		strCmd6[7] = (byte) 0x2D;
		strCmd6[8] = (byte) 0x31;
		strCmd6[9] = (byte) 0x30;
		mUsbDriver.write(strCmd6, strCmd6.length); 
		// 字体加粗、文字大小比默认大一倍
		mUsbDriver.write(PrintCmd.SetBold(1));
		mUsbDriver.write(PrintCmd.SetSizetext(1, 1));

		byte[] bByte = new byte[6];
		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x00;
		bByte[3] = (byte) 0x40;
		bByte[4] = (byte) 0x00;
		bByte[5] = (byte) 0x91;
		mUsbDriver.write(bByte, bByte.length);// 电影名称-坐标定位
		// 对应电影名称【BA EC BA A3 D0 D0 B6 AF--红海行动】
		byte[] bByte2 = new byte[9];
		bByte2[0] = (byte) 0xBA;
		bByte2[1] = (byte) 0xEC;
		bByte2[2] = (byte) 0xBA;
		bByte2[3] = (byte) 0xA3;
		bByte2[4] = (byte) 0xD0;
		bByte2[5] = (byte) 0xD0;
		bByte2[6] = (byte) 0xB6;
		bByte2[7] = (byte) 0xAF;
		bByte2[8] = (byte) 0x0A;
		mUsbDriver.write(bByte2, bByte2.length); 
		// 字体不加粗、文字大小默认
		mUsbDriver.write(PrintCmd.SetBold(0));
		mUsbDriver.write(PrintCmd.SetSizetext(0, 0));

		// 设置字形
		// byte[] strCmd8 = new byte[9];
		// strCmd8[0] = (byte) 0x1B;
		// strCmd8[1] = (byte) 0x21;
		// strCmd8[2] = (byte) 0x01;
		// strCmd8[3] = (byte) 0x1C;
		// strCmd8[4] = (byte) 0x21;
		// strCmd8[5] = (byte) 0x01;
		// strCmd8[6] = (byte) 0x1d;
		// strCmd8[7] = (byte) 0x21;
		// strCmd8[8] = (byte) 0x00;
		// mUsbDriver.write(strCmd8,strCmd8.length);
		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x00;
		bByte[3] = (byte) 0x35;
		bByte[4] = (byte) 0x00;
		bByte[5] = (byte) 0xD7;
		mUsbDriver.write(bByte, bByte.length);// 座号-坐标定位
		// 对应座号【30 38 C5 C5 30 39 D7 F9--08排09座】
		byte[] strCmd9 = new byte[8];
		strCmd9[0] = (byte) 0x30;
		strCmd9[1] = (byte) 0x38;
		strCmd9[2] = (byte) 0xC5;
		strCmd9[3] = (byte) 0xC5;
		strCmd9[4] = (byte) 0x30;
		strCmd9[5] = (byte) 0x39;
		strCmd9[6] = (byte) 0xD7;
		strCmd9[7] = (byte) 0xF9;
		mUsbDriver.write(strCmd9, strCmd9.length);

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x00;
		bByte[3] = (byte) 0x35;
		bByte[4] = (byte) 0x00;
		bByte[5] = (byte) 0xFA;
		mUsbDriver.write(bByte, bByte.length);// 票类-坐标定位
		// 对应票类【CD F8 B9 BA--网购】
		byte[] strCmd10 = new byte[4];
		strCmd10[0] = (byte) 0xCD;
		strCmd10[1] = (byte) 0xF8;
		strCmd10[2] = (byte) 0xB9;
		strCmd10[3] = (byte) 0xBA;
		mUsbDriver.write(strCmd10, strCmd10.length);

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x00;
		bByte[3] = (byte) 0x50;
		bByte[4] = (byte) 0x01;
		bByte[5] = (byte) 0x2C;
		mUsbDriver.write(bByte, bByte.length);// 售票时间-坐标定位
		// 对应售票时间【2017-01-10】
		byte[] strCmd11 = new byte[10];
		strCmd11[0] = (byte) 0x32;
		strCmd11[1] = (byte) 0x30;
		strCmd11[2] = (byte) 0x31;
		strCmd11[3] = (byte) 0x37;
		strCmd11[4] = (byte) 0x2D;
		strCmd11[5] = (byte) 0x30;
		strCmd11[6] = (byte) 0x31;
		strCmd11[7] = (byte) 0x2D;
		strCmd11[8] = (byte) 0x31;
		strCmd11[9] = (byte) 0x30;
		mUsbDriver.write(strCmd11, strCmd11.length);

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0x1A;
		bByte[4] = (byte) 0x00;
		bByte[5] = (byte) 0xE2;
		mUsbDriver.write(bByte, bByte.length);// 票价-坐标定位
		// 对应票价【33 30 D4 AA--30元】
		byte[] strCmd13 = new byte[4];
		strCmd13[0] = (byte) 0x33;
		strCmd13[1] = (byte) 0x30;
		strCmd13[2] = (byte) 0xD4;
		strCmd13[3] = (byte) 0xAA;
		mUsbDriver.write(strCmd13, strCmd13.length);

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0x1A;
		bByte[4] = (byte) 0x00;
		bByte[5] = (byte) 0xFA;
		mUsbDriver.write(bByte, bByte.length);// 服务费-坐标定位
		// 对应服务费【30 D4 AA--0元】
		byte[] strCmd15 = new byte[4];
		strCmd15[0] = (byte) 0x30;
		strCmd15[1] = (byte) 0xD4;
		strCmd15[2] = (byte) 0xAA;
		mUsbDriver.write(strCmd15, strCmd15.length);

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0x1A;
		bByte[4] = (byte) 0x01;
		bByte[5] = (byte) 0x2C;
		mUsbDriver.write(bByte, bByte.length);// 工号-坐标定位
		// 对应工号【31 31 30 31 31 32】
		byte[] strCmd17 = new byte[6];
		strCmd17[0] = (byte) 0x31;
		strCmd17[1] = (byte) 0x31;
		strCmd17[2] = (byte) 0x30;
		strCmd17[3] = (byte) 0x31;
		strCmd17[4] = (byte) 0x31;
		strCmd17[5] = (byte) 0x32;
		mUsbDriver.write(strCmd17, strCmd17.length);

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x00;
		bByte[3] = (byte) 0xA5;
		bByte[4] = (byte) 0x01;
		bByte[5] = (byte) 0x7C;
		mUsbDriver.write(bByte, bByte.length);// 二维码-坐标定位
		// 二维码
		String sbData = "深圳市美松科技有限公司";
		mUsbDriver.write(PrintCmd.PrintQrcode(sbData, 15, 6, 1));

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x00;
		bByte[3] = (byte) 0x35;
		bByte[4] = (byte) 0x02;
		bByte[5] = (byte) 0x30;
		mUsbDriver.write(bByte, bByte.length);// 票号-坐标定位
		// 对应票号
		byte[] strCmd19 = new byte[8];
		strCmd19[0] = (byte) 0x30;
		strCmd19[1] = (byte) 0x31;
		strCmd19[2] = (byte) 0x32;
		strCmd19[3] = (byte) 0x33;
		strCmd19[4] = (byte) 0x34;
		strCmd19[5] = (byte) 0x35;
		strCmd19[6] = (byte) 0x36;
		strCmd19[7] = (byte) 0x37;
		mUsbDriver.write(strCmd19, strCmd19.length);
		
		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0xF4;
		bByte[4] = (byte) 0x00;
		bByte[5] = (byte) 0x46;
		mUsbDriver.write(bByte, bByte.length);// 副券影厅-坐标定位
		// 对应副券影厅【31 BA C5 CC FC】
		byte[] strCmd20 = new byte[5];
		strCmd20[0] = (byte) 0x31;
		strCmd20[1] = (byte) 0xBA;
		strCmd20[2] = (byte) 0xC5;
		strCmd20[3] = (byte) 0xCC;
		strCmd20[4] = (byte) 0xFC;
		mUsbDriver.write(strCmd20, strCmd20.length);

		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0xF4;
		bByte[4] = (byte) 0x00;
		bByte[5] = (byte) 0xAF;
		mUsbDriver.write(bByte, bByte.length);// 副券时间-坐标定位
		// 对应副券时间 2017-1 【32 30 31 37 2D 31】
		byte[] strCmd21 = new byte[6];
		strCmd21[0] = (byte) 0x32;
		strCmd21[1] = (byte) 0x30;
		strCmd21[2] = (byte) 0x31;
		strCmd21[3] = (byte) 0x37;
		strCmd21[4] = (byte) 0x2D;
		strCmd21[5] = (byte) 0x31;
		mUsbDriver.write(strCmd21, strCmd21.length); 
		
		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0xF4;
		bByte[4] = (byte) 0x01;
		bByte[5] = (byte) 0x22;
		mUsbDriver.write(bByte, bByte.length);// 副券座号-坐标定位
		// 对应副券座号【 30 38 C5 C5 30 39 -- 08排9】
		byte[] strCmd22 = new byte[6];
		strCmd22[0] = (byte) 0x30;
		strCmd22[1] = (byte) 0x38;
		strCmd22[2] = (byte) 0xC5;
		strCmd22[3] = (byte) 0xC5;
		strCmd22[4] = (byte) 0x30;
		strCmd22[5] = (byte) 0x39;
		mUsbDriver.write(strCmd22, strCmd22.length);
		
		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0xF4;
		bByte[4] = (byte) 0x01;
		bByte[5] = (byte) 0x90;
		mUsbDriver.write(bByte, bByte.length);// 副券票号-坐标定位
		// 对应副券票号
		byte[] strCmd23 = new byte[5];
		strCmd23[0] = (byte) 0x30;
		strCmd23[1] = (byte) 0x31;
		strCmd23[2] = (byte) 0x32;
		strCmd23[3] = (byte) 0x33;
		strCmd23[4] = (byte) 0x34;
		mUsbDriver.write(strCmd23, strCmd23.length);
		
		bByte[0] = (byte) 0x11;
		bByte[1] = (byte) 0x24;
		bByte[2] = (byte) 0x01;
		bByte[3] = (byte) 0xF4;
		bByte[4] = (byte) 0x02;
		bByte[5] = (byte) 0x08;
		mUsbDriver.write(bByte, bByte.length);// 副券票价-坐标定位
		// 对应副券票价
		byte[] strCmd24 = new byte[4];
		strCmd24[0] = (byte) 0x33;
		strCmd24[1] = (byte) 0x30;
		strCmd24[2] = (byte) 0xD4;
		strCmd24[3] = (byte) 0xAA;
		mUsbDriver.write(strCmd24, strCmd24.length);
		// 打印页模式内容
		byte[] strCmd26 = new byte[2];
		strCmd26[0] = (byte) 0x11;
		strCmd26[1] = (byte) 0x0c;
		mUsbDriver.write(strCmd26, strCmd26.length);
		mUsbDriver.write(PrintCmd.PrintFeedline(4)); // 走纸换行
		mUsbDriver.write(PrintCmd.PrintCutpaper(cutter)); // 切纸类型
	}
	
	public static void printDiskImgFile(String imgPath, int count, UsbDriver mUsbDriver, int cutter){
		if(TextUtils.isEmpty(imgPath) && count == 0){
 			T.showShort(context, "路径或者打印数量不能为空!");
 			return;
 		}
		Bitmap inputBmp = Utils.getBitmapData(imgPath);
		if(inputBmp == null)
 			return;
		int[] data = Utils.getPixelsByBitmap(inputBmp);
		for(int i= 0;i<count;i++){
			backPaper(mUsbDriver,70);// 退纸
			new Utils().sleep(200);
			mUsbDriver.write(PrintCmd.SetLeftmargin(25));
			mUsbDriver.write(PrintCmd.PrintDiskImagefile(data,inputBmp.getWidth(),inputBmp.getHeight()));
			mUsbDriver.write(PrintCmd.PrintMarkposition());
		}
//		backPaper(mUsbDriver,150);// 退纸
		mUsbDriver.write(PrintCmd.PrintCutpaper(cutter));
		mUsbDriver.write(PrintCmd.SetClean());
	}
	
	private static void backPaper(UsbDriver mUsbDriver, int len){
		byte[] etBytes = new byte[3];
		int iIndex = 0;
		etBytes[iIndex++] = 0x1B;
		etBytes[iIndex++] = 0x4B;
		etBytes[iIndex++] = (byte)len; // 0x43[67],0x32[50] 0x28[40]
		mUsbDriver.write(etBytes,iIndex);
	}
	
}
