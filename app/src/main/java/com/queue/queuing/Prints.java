package com.queue.queuing;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.text.Html;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;

import com.queue.queuing.io.*;
import com.queue.queuing.sdk.Command;
import com.queue.queuing.sdk.ImageAdjust;
import com.queue.queuing.sdk.PrintPicture;
import com.queue.queuing.sdk.PrinterCommand;

import org.json.JSONObject;


public class Prints {

	// with query status
	/*
	public static boolean PrintTicket(Context ctx, Pos pos)
	{
		boolean bPrintResult = false;
		
		byte[] status = new byte[1];
		if(pos.POS_QueryStatus(status, 3000, 2))
		{
			byte[] data = "1234567890\r\n".getBytes();
			pos.GetIO().Write(data, 0, data.length);
		
			bPrintResult = pos.POS_TicketSucceed(0, 30000);
		}
		
		return bPrintResult;
	}
	*/
	
	// simple print
	public static boolean PrintTicket(Context ctx, Pos pos, JSONObject queuePrintingBuffer)
	{  String store_name = "";
		//String store_name2 = "";
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
		//store_name2 = queuePrintingBuffer.getString("store_name2");
		store_tel = queuePrintingBuffer.getString("store_tel");
		store_address = queuePrintingBuffer.getString("store_address");
		line_number = queuePrintingBuffer.getString("number");
		line_people = queuePrintingBuffer.getString("people");
		line_num = queuePrintingBuffer.getString("num");
		line_qrcode = queuePrintingBuffer.getString("qrcode");
		line_start_time = queuePrintingBuffer.getString("start_time");
		language = queuePrintingBuffer.getString("language");

			int bPrintResult = 0;//variable that checks paper

			byte[] status = new byte[1];
			if (pos.POS_RTQueryStatus(status, 1, 3000, 2) && ((status[0] & 0x12) == 0x12)) {
				if ((status[0] & 0x08) == 0) {
					if(pos.POS_QueryStatus(status, 3000, 2)) {
						bPrintResult = 1;

					} else {
						// "查询状态失败";
					}
				} else {
					//"打印机脱机";
				}
			} else {
				//"实时状态查询失败";
			}

			//pos.POS_S_TextOut("REC" + String.format("%03d", 2) + "\r\nPrinter\r\n测试页\r\n\r\n", 0, 1, 1, 0, 0x100);
			if(bPrintResult == 1) {
				if (language.equals("CN")) {
					//pos.POS_FeedLine();

					pos.POS_S_Align(1);
					//pos.POS_TextOut("1234567890\r\n", 0, 0, 0, 0, 0, 0);
					//pos.POS_TextOut("WENWUWENWUWENWU\r\n",0,0,0,0,0,0);
					//pos.POS_TextOut("文吴\r\n",1,0,0,0,0,0);
					//pos.POS_TextOut((store_tel +"\r\n"), 0, 0, 0, 0, 0, 0);
					//pos.POS_TextOut(("取号时间：" + line_start_time + "\r\n").getBytes("GBK").toString(), 0, 0, 0, 0, 0, 0);
					//pos.POS_TextOut("-------------------------------------------\n".getBytes("GBK").toString(),0,0,0,0,0,0);
					//pos.POS_TextOut("WENWUWENWUWENWU\r\n",0,0,0,0,0,0);
					//Bitmap bmBlackWhite = getImageFromAssetsFile(ctx, "blackwhite.png");
					//pos.POS_PrintPicture(bmBlackWhite, 384, 1, 0);
					pos.POS_TextOut((Html.fromHtml(store_name) + "\r\n"), 1, 0, 0, 0, 0, 0);
					pos.POS_TextOut(("电话: " + store_tel + "\r\n"), 1, 0, 0, 0, 0, 0);
					//pos.POS_TextOut(("取号时间：" + line_start_time + "\r\n"),1,0,0,0,0,0);

					pos.POS_TextOut("--------------------------------\r\n", 1, 0, 0, 0, 0, 0);

					pos.POS_TextOut((line_number + "号\r\n"), 1, 0, 1, 1, 0, 0);

					pos.POS_TextOut(("就餐人数：" + line_people + "\r\n"), 1, 0, 0, 0, 0, 0);

					pos.POS_TextOut(("您的前面还有：" + line_num + "位在等待\r\n"), 1, 0, 0, 0, 0, 0);

//			if (!line_qrcode.isEmpty()) {
//				Bitmap mBitmap = getImageFromURL(line_qrcode);
//				if (mBitmap != null) {
//					pos.POS_PrintPicture(mBitmap, 384, 1, 0);
//				}
//			}
					pos.POS_FeedLine();
					pos.POS_S_SetQRcode(line_qrcode, 4, 0, 3);
					pos.POS_S_Align(1);
					pos.POS_TextOut(("扫码了解排队动态\r\n"), 1, 0, 0, 0, 0, 0);
					pos.POS_FeedLine();
					pos.POS_S_Align(1);
					//pos.POS_TextOut(("地址: " + store_address + "\r\n"),1,0,0,0,0,0);
					pos.POS_TextOut(("取号时间：" + line_start_time + "\r\n"), 1, 0, 0, 0, 0, 0);
					pos.POS_TextOut("--------------------------------\r\n", 1, 0, 0, 0, 0, 0);
					pos.POS_TextOut("系统提供方:鹿大胃 647-7729-729\r", 1, 0, 0, 0, 0, 0);

					pos.POS_FeedLine();
					pos.POS_FeedLine();

					pos.POS_FeedLine();
					pos.POS_FeedLine();

				} else {

					pos.POS_S_Align(1);
					pos.POS_TextOut((Html.fromHtml(store_name) + "\r\n"), 1, 0, 0, 0, 0, 0);
					pos.POS_TextOut(("TEL: " + store_tel + "\r\n"), 1, 0, 0, 0, 0, 0);
					pos.POS_TextOut("--------------------------------\r\n", 1, 0, 0, 0, 0, 0);

					pos.POS_TextOut((line_number + "号\r\n"), 1, 0, 1, 1, 0, 0);

					pos.POS_TextOut(("Guest：" + line_people + "\r\n"), 1, 0, 0, 0, 0, 0);

					pos.POS_TextOut(("There are " + line_num + " ahead\r\n"), 1, 0, 0, 0, 0, 0);

					pos.POS_FeedLine();
					pos.POS_S_SetQRcode(line_qrcode, 4, 0, 3);
					pos.POS_S_Align(1);
					pos.POS_TextOut(("Scan to view states\r\n"), 1, 0, 0, 0, 0, 0);
					pos.POS_FeedLine();
					pos.POS_S_Align(1);

					pos.POS_TextOut(("Register at：" + line_start_time + "\r\n"), 1, 0, 0, 0, 0, 0);
					pos.POS_TextOut("--------------------------------\r\n", 1, 0, 0, 0, 0, 0);
					pos.POS_TextOut("Provide by Mr.Moo 647-7729-729\r", 1, 0, 0, 0, 0, 0);

					pos.POS_FeedLine();
					pos.POS_FeedLine();
					pos.POS_FeedLine();
					pos.POS_FeedLine();


				}
			}
			bPrintResult = 0;
			} catch (Exception e) {
				e.printStackTrace();
			}


		return pos.GetIO().IsOpened();
	}
	// simple print
	public static boolean Printtest(Context ctx, Pos pos) {
		try {
			pos.POS_FeedLine();
			pos.POS_TextOut("printer work\r\n",0,0,0,0,0,0);
			pos.POS_FeedLine();
			pos.POS_FeedLine();
			pos.POS_FeedLine();
		} catch (Exception e) {
			e.printStackTrace();
		}


		return pos.GetIO().IsOpened();
	}
	
	/**
	 * 从Assets中读取图片
	 */
	public static Bitmap getImageFromAssetsFile(Context ctx, String fileName) {
		Bitmap image = null;
		AssetManager am = ctx.getResources().getAssets();
		try {
			InputStream is = am.open(fileName);
			image = BitmapFactory.decodeStream(is);
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return image;
	}
	
	public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
		// load the origial Bitmap
		Bitmap BitmapOrg = bitmap;

		int width = BitmapOrg.getWidth();
		int height = BitmapOrg.getHeight();
		int newWidth = w;
		int newHeight = h;

		// calculate the scale
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;

		// create a matrix for the manipulation
		Matrix matrix = new Matrix();
		// resize the Bitmap
		matrix.postScale(scaleWidth, scaleHeight);
		// if you want to rotate the Bitmap
		// matrix.postRotate(45);

		// recreate the new Bitmap
		Bitmap resizedBitmap = Bitmap.createBitmap(BitmapOrg, 0, 0, width,
				height, matrix, true);

		// make a Drawable from Bitmap to allow to set the Bitmap
		// to the ImageView, ImageButton or what ever
		return resizedBitmap;
	}
	
	public static Bitmap getTestImage1(int width, int height)
	{
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();

		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, width, height, paint);
		
		paint.setColor(Color.BLACK);
		for(int i = 0; i < 8; ++i)
		{
			for(int x = i; x < width; x += 8)
			{
				for(int y = i; y < height; y += 8)
				{
					canvas.drawPoint(x, y, paint);
				}
			}
		}
		return bitmap;
	}
	
	public static Bitmap getTestImage2(int width, int height)
	{
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();

		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, width, height, paint);
		
		paint.setColor(Color.BLACK);
		for(int y = 0; y < height; y += 4)
		{
			for(int x = y%32; x < width; x += 32)
			{
				canvas.drawRect(x, y, x+4, y+4, paint);
			}
		}
		return bitmap;
	}

	/**
	 * 加载网络图片
	 */
	public static Bitmap getImageFromURL(String fileUrl) {
		Bitmap image = null;

		try {
			byte[] data= ImageAdjust.getImage(fileUrl);
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
}


