package com.queue.queuing.io;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.queue.queuing.barcode.DSCode128;
import com.queue.queuing.qrcode.QRCode;

//import android.graphics.Canvas;


/**
 * 画图函数封装
 * 		For 热敏打印机
 * 		连接成功之后，调用此处的函数，即可将要打印的内容画到Bitmap上。 该处函数对文字打印做了优化，提升了效果。
 * 
 * @author 彭大帅
 * 
 */
public class Canvas{

	private static final String TAG = "Canvas";
	
	public IO IO = new IO();
	
	public void Set(IO io) 
	{
		if(null != io)
		{
			IO = io;
		}
	}
	
	public IO GetIO() 
	{
		return IO;
	}
	
	public static final int DIRECTION_LEFT_TO_RIGHT = 0;
	public static final int DIRECTION_BOTTOM_TO_TOP = 1;
	public static final int DIRECTION_RIGHT_TO_LEFT = 2;
	public static final int DIRECTION_TOP_TO_BOTTOM = 3;
	
	public static final int HORIZONTALALIGNMENT_LEFT = -1;
	public static final int HORIZONTALALIGNMENT_CENTER = -2;
	public static final int HORIZONTALALIGNMENT_RIGHT = -3;
	
	public static final int VERTICALALIGNMENT_TOP = -1;
	public static final int VERTICALALIGNMENT_CENTER = -2;
	public static final int VERTICALALIGNMENT_BOTTOM = -3;
	
	public static final int FONTSTYLE_BOLD = 0x08;
	public static final int FONTSTYLE_UNDERLINE = 0x80;
	
	public static final int BARCODE_TYPE_CODE128 = 73;
	
	private Bitmap bitmap;
	
	private android.graphics.Canvas canvas;
	
	private Paint paint;
	private TextPaint textpaint;

	private int dir;

	/**
	 * 初始化指定宽高的画布。后续画图都在此画布范围内展开。
	 * @param width 画布宽度（像素为单位，不超过打印机可打印宽度。建议58毫米打印机使用384,80毫米打印机使用576）
	 * @param height 画布高度
	 */
	public void CanvasBegin(int width, int height)
	{
		bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		canvas = new android.graphics.Canvas(bitmap);
		paint = new Paint();
		textpaint = new TextPaint();
		dir = DIRECTION_LEFT_TO_RIGHT;
		
		paint.setColor(Color.WHITE);
		canvas.drawRect(0, 0, width, height, paint);
		paint.setColor(Color.BLACK);
		textpaint.setColor(Color.BLACK);
	}
	
	/**
	 * 结束该画布。结束之后，只可以调用CanvasPrint进行打印操作。不可做其他操作。
	 */
	public void CanvasEnd()
	{
		textpaint = null;
		paint = null;
		canvas = null;
	}
	
	/**
	 * 打印画布内容
	 * @param nBinaryAlgorithm 	二值化算法	0 使用抖动算法，对彩色图片有较好的效果。 1 使用平均阀值算法，对文本类图片有较好的效果
	 * @param nCompressMethod	压缩算法		0 不使用压缩算法	1 使用压缩算法
	 */
	public void CanvasPrint(int nBinaryAlgorithm, int nCompressMethod) 
	{
		if(!IO.IsOpened())
			return;
		
		IO.Lock();
		
		try
		{
			Bitmap mBitmap = bitmap;
			int nWidth = bitmap.getWidth(); 
			
			int dstw = ((nWidth + 7) / 8) * 8;
			int dsth = mBitmap.getHeight() * dstw / mBitmap.getWidth();
			int [] dst = new int[dstw * dsth];
			
			mBitmap = ImageProcessing.resizeImage(mBitmap, dstw, dsth);
			mBitmap.getPixels(dst, 0, dstw, 0, 0, dstw, dsth);
			byte[] gray = ImageProcessing.GrayImage(dst);
			
			boolean[] dithered = new boolean[dstw * dsth];
			if(nBinaryAlgorithm == 0)
				ImageProcessing.format_K_dither16x16(dstw, dsth, gray, dithered);
			else
				ImageProcessing.format_K_threshold(dstw, dsth, gray, dithered);
			
			byte[] data = null;
			if(nCompressMethod == 0)
				data = ImageProcessing.eachLinePixToCmd(dithered, dstw, 0);
			else
				data = ImageProcessing.eachLinePixToCompressCmd(dithered, dstw);
			
			IO.Write(data, 0, data.length);
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
	}
	
	/**
	 * 设置打印区域方向
	 * @param direction 0 自左向右，1 自下向上，2自右向左，3自上向下
	 */
	public void SetPrintDirection(int direction)
	{
		dir = direction;
	}
	
	private float degreeTo360(float degree)
	{
		if (degree < 0.0)
		{
			while (true) {
				degree += 360.0;
				if (degree >= 0.0) 
					break;
			}
		} else if (degree >= 360.0) {
			while (true) {
				degree -= 360.0;
				if (degree < 360.0) 
					break;
			}
		}
		
		return degree;
	}
	
	private class DXDY
	{
		float dx;
		float dy;
	}
	
	/**
	 * 画布的宽高是（w，h），矩形的宽高是（tw，th），矩形的原点是（x，y），旋转角度是rotation。
	 * 需要将坐标轴translate到（x，y）处，然后旋转。
	 * @param w
	 * @param h
	 * @param x
	 * @param y
	 * @param tw
	 * @param th
	 * @param dxdy
	 * @param rotation
	 */
	private void measureTranslate(float w, float h, float x, float y, float tw, float th, DXDY dxdy, float rotation)
	{
		float dx = x,dy = y;
		
		float abssinth = (float) Math.abs((th * Math.sin(Math.PI / 180 * rotation)));
		float abscosth = (float) Math.abs((th * Math.cos(Math.PI / 180 * rotation)));
		float abssintw = (float) Math.abs((tw * Math.sin(Math.PI / 180 * rotation)));
		float abscostw = (float) Math.abs((tw * Math.cos(Math.PI / 180 * rotation)));
		float dw = abssinth + abscostw;
		float dh = abscosth + abssintw;
		rotation = degreeTo360(rotation);
		
		if(rotation == 0)
		{
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = 0;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = w/2 - tw/2;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w - tw;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = 0;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = h/2 - th/2;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h - th;
		}
		else if (rotation > 0 && rotation < 90)
		{		
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = abssinth;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = w/2 - dw/2 + abssinth;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w - dw + abssinth;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = 0;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = h/2 - dh/2;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h - dh;
		}
		else if (rotation == 90)
		{
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = th;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = w/2 + th/2;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w - y;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = 0;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = h/2 - tw/2;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h - tw;
		}
		else if (rotation > 90 && rotation < 180)
		{
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = dw;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = w/2 + dw/2;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w - y;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = abscosth;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = h/2 - dh/2 + abscosth;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h - dh + abscosth;
		}
		else if (rotation == 180)
		{
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = tw;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = w/2 + tw/2;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = th;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = h/2 + th/2;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h;
		}
		else if (rotation > 180 && rotation < 270)
		{
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = dw - abscosth;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = w/2 + dw/2 - abscosth;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w - abscosth;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = dh;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = h/2 + dh/2;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h;
		}
		else if (rotation == 270)
		{
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = 0;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = (w - th)/2;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w - th;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = tw;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = (h + tw)/2;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h;
		}
		else if (rotation > 270 && rotation < 360)
		{			
			if (x == HORIZONTALALIGNMENT_LEFT)
				dx = 0;
			else if (x == HORIZONTALALIGNMENT_CENTER)
				dx = w/2 - dw/2;
			else if (x == HORIZONTALALIGNMENT_RIGHT)
				dx = w - dw;
			
			if (y == VERTICALALIGNMENT_TOP)
				dy = dh - abscosth;
			else if (y == VERTICALALIGNMENT_CENTER)
				dy = h/2 + dh/2 - abscosth;
			else if (y == VERTICALALIGNMENT_BOTTOM)
				dy = h - abscosth;
		}
		
		dxdy.dx = dx;
		dxdy.dy = dy;
	}
	
	/**
	 * 画文本到画布指定位置
	 * @param text 文本内容
	 * @param x 指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y 指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param rotation 旋转角度（顺时针方向）
	 * @param typeface 字体类型
	 * @param textSize 字体大小
	 * @param nFontStyle 字体风格 0x00 普通 0x08 加粗 0x80 下划线
	 */
	public void DrawText(String text, float x, float y, float rotation, Typeface typeface, float textSize, int nFontStyle)
	{
		paint.setTypeface(typeface);
		paint.setTextSize(textSize);

		paint.setFakeBoldText((nFontStyle & FONTSTYLE_BOLD) != 0);
		paint.setUnderlineText((nFontStyle & FONTSTYLE_UNDERLINE) != 0);
		
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		FontMetricsInt fm = paint.getFontMetricsInt();

		float tw = paint.measureText(text);
		float th = fm.descent - fm.ascent;
		
		DXDY dxdy = new DXDY();
		
		canvas.save();
		if(dir == 0)
			canvas.translate(0, 0);
		else if (dir == 1)
			canvas.translate(0, h);
		else if (dir == 2)
			canvas.translate(w, h);
		else if (dir == 3)
			canvas.translate(w, 0);
		canvas.rotate(dir*-90);
		
		if(dir == 0 || dir == 2)
			measureTranslate(w,h,x,y,tw,th,dxdy,rotation);
		else
			measureTranslate(h,w,x,y,tw,th,dxdy,rotation);
		
		canvas.translate(dxdy.dx, dxdy.dy);
		canvas.rotate(rotation);
		canvas.drawText(text, 0, -fm.ascent, paint);
		canvas.restore();
	}
	
	public void DrawTextAutoNewLine(String text, float x, float y, float rotation, Typeface typeface, float textSize, int nFontStyle, int outerwidth, float spacingmult, float spacingadd)
	{
		textpaint.setTypeface(typeface);
		textpaint.setTextSize(textSize);

		textpaint.setFakeBoldText((nFontStyle & FONTSTYLE_BOLD) != 0);
		textpaint.setUnderlineText((nFontStyle & FONTSTYLE_UNDERLINE) != 0);
		
		StaticLayout m_layout = new StaticLayout(text, textpaint, outerwidth, Alignment.ALIGN_NORMAL, spacingmult, spacingadd, true);
		
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float tw = m_layout.getWidth();
		float th = m_layout.getHeight();
		
		DXDY dxdy = new DXDY();
		
		canvas.save();
		if(dir == 0)
			canvas.translate(0, 0);
		else if (dir == 1)
			canvas.translate(0, h);
		else if (dir == 2)
			canvas.translate(w, h);
		else if (dir == 3)
			canvas.translate(w, 0);
		canvas.rotate(dir*-90);
		
		if(dir == 0 || dir == 2)
			measureTranslate(w,h,x,y,tw,th,dxdy,rotation);
		else
			measureTranslate(h,w,x,y,tw,th,dxdy,rotation);
		
		canvas.translate(dxdy.dx, dxdy.dy);
		canvas.rotate(rotation);
		m_layout.draw(canvas);
		canvas.restore();
	}
	
	/**
	 * 画多行文本
	 * @param text
	 * @param x
	 * @param y
	 * @param rotation
	 * @param typeface
	 * @param textSize
	 * @param nFontStyle
	 * @param rowWidth
	 * @param lineSpacing
	 */
	public void DrawTextMultiLine(String text, float x, float y, float rotation, Typeface typeface, float textSize, int nFontStyle, float rowWidth, float lineSpacing)
	{
		TextPaint textPaint = new TextPaint();
		textPaint.setTypeface(typeface);
		textPaint.setTextSize(textSize);

		textPaint.setFakeBoldText((nFontStyle & FONTSTYLE_BOLD) != 0);
		textPaint.setUnderlineText((nFontStyle & FONTSTYLE_UNDERLINE) != 0);
		
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		StaticLayout layout = new StaticLayout(text, textPaint, (int) rowWidth, Alignment.ALIGN_NORMAL, 0.0F, lineSpacing, true);
		float tw = layout.getWidth();
		float th = layout.getHeight();
		
		DXDY dxdy = new DXDY();
		
		canvas.save();
		if(dir == 0)
			canvas.translate(0, 0);
		else if (dir == 1)
			canvas.translate(0, h);
		else if (dir == 2)
			canvas.translate(w, h);
		else if (dir == 3)
			canvas.translate(w, 0);
		canvas.rotate(dir*-90);
		
		if(dir == 0 || dir == 2)
			measureTranslate(w,h,x,y,tw,th,dxdy,rotation);
		else
			measureTranslate(h,w,x,y,tw,th,dxdy,rotation);
		
		canvas.translate(dxdy.dx, dxdy.dy);
		canvas.rotate(rotation);
		layout.draw(canvas);
		canvas.restore();
	}
	
	/**
	 * 画一条线段
	 * @param startX 线段起点横坐标
	 * @param startY 线段起点纵坐标
	 * @param stopX 线段终点横坐标
	 * @param stopY 线段终点纵坐标
	 */
	public void DrawLine(float startX, float startY, float stopX, float stopY)
	{
		canvas.drawLine(startX, startY, stopX, stopY, paint);
	}

	/**
	 * 画方框（方框线条粗细为1个像素点）
	 * @param left 矩形框左上角横坐标
	 * @param top 矩形框左上角纵坐标
	 * @param right 矩形框右下角横坐标
	 * @param bottom 矩形框右下角纵坐标
	 */
	public void DrawBox(float left, float top, float right, float bottom)
	{
		canvas.drawLine(left, top, right, top, paint);
		canvas.drawLine(right, top, right, bottom, paint);
		canvas.drawLine(right, bottom, left, bottom, paint);
		canvas.drawLine(left, bottom, left, top, paint);
	}
	
	/**
	 * 画矩形
	 * @param left 矩形左上角横坐标
	 * @param top 矩形左上角纵坐标
	 * @param right 矩框右下角横坐标
	 * @param bottom 矩形右下角纵坐标
	 */
	public void DrawRect(float left, float top, float right, float bottom)
	{
		canvas.drawRect(left, top, right, bottom, paint);
	}
	
	/**
	 * 画Bitmap位图
	 * @param bitmap 位图
	 * @param x 指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y 指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param rotation 旋转角度（顺时针方向）
	 */
	public void DrawBitmap(Bitmap bitmap, float x, float y, float rotation)
	{
		float w = canvas.getWidth();
		float h = canvas.getHeight();
		
		float tw = bitmap.getWidth();
		float th = bitmap.getHeight();
		DXDY dxdy = new DXDY();
		
		canvas.save();
		if(dir == 0)
			canvas.translate(0, 0);
		else if (dir == 1)
			canvas.translate(0, h);
		else if (dir == 2)
			canvas.translate(w, h);
		else if (dir == 3)
			canvas.translate(w, 0);
		canvas.rotate(dir*-90);
		
		if(dir == 0 || dir == 2)
			measureTranslate(w,h,x,y,tw,th,dxdy,rotation);
		else
			measureTranslate(h,w,x,y,tw,th,dxdy,rotation);
		
		canvas.translate(dxdy.dx, dxdy.dy);
		canvas.rotate(rotation);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		canvas.restore();
	}
	
	/**
	 * 画二维码
	 * @param text 二维码文本
	 * @param x 指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y 指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param rotation 旋转角度（顺时针方向）
	 * @param unitWidth QR码单元宽度，范围[1,16]。 QR码单元宽度越大，QR码越大。
	 * @param version QR码版本。0表示自动计算版本。 QR码版本越大，能编码的字符就越多，QR码也越大。
	 * @param ecc QR码纠错等级。[1,4]
	 */
	public void DrawQRCode(String text, float x, float y, float rotation, int unitWidth, int version, int ecc)
	{
		Bitmap bitmap = null;
		
		int typeNumber = QRCode.getMinimumQRCodeTypeNumber(text, ecc - 1);
		if(version < typeNumber) // 如果指定了Version但是数据超量，则自动扩充Version
			version = typeNumber;
		
		QRCode codes = QRCode.getFixedSizeQRCode(text, ecc - 1, version);
		if(codes != null)
		{
			Boolean[][] bModules = codes.getModules();
			bitmap = QRModulesToBitmap(bModules, unitWidth);	
		}
		
		DrawBitmap(bitmap, x, y, rotation);
	}
	
	private Bitmap QRModulesToBitmap(Boolean[][] modules, int unitWidth) {

		int height = modules.length * unitWidth;
		int width = height;
		
		int[] pixels = new int[width * height];

		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				pixels[width * y + x] = modules[y/unitWidth][x/unitWidth] ? Color.BLACK
						: Color.WHITE;
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
	/**
	 * 画条码
	 * @param text 条码文本
	 * @param x 指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y 指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param rotation 旋转角度（顺时针方向）
	 * @param unitWidth 条形码单元宽度，范围[1,16]。 条形码单元宽度越大，条形码越大。
	 * @param height 条码高度。（打印机dpi一般是203，这时候8点就是1mm）。设置为80点就是1CM。
	 * @param barcodeType 条码类型。当前只支持CODE128条码，更多条码，后续支持。
	 */
	public void DrawBarcode(String text, float x, float y, float rotation, int unitWidth, int height, int barcodeType)
	{
		Bitmap bitmap = null;
		
		if (barcodeType == BARCODE_TYPE_CODE128)
		{
			DSCode128 code = new DSCode128();
			boolean[] bPattern = code.Encode(text);
			if(bPattern != null)
			{
				bitmap = BPatternToBitmap(bPattern, unitWidth, height);	
			}
		}
		
		DrawBitmap(bitmap, x, y, rotation);
	}
	
	private Bitmap BPatternToBitmap(boolean[] bPattern, int unitWidth, int height)
	{
		int width = unitWidth * bPattern.length;
		
		int[] pixels = new int[width * height];
		
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				pixels[width * y + x] = bPattern[x/unitWidth] ? Color.BLACK
						: Color.WHITE;
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}
	
}
