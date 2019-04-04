package com.queue.queuing.io;

import android.graphics.Bitmap;
import android.util.Log;

import com.queue.queuing.qrcode.QRCode;

/**
 * ESC/POS 页模式打印指令封装
 * 		For 页模式打印机
 * 		连接成功之后，调用此处的函数，即可进行打印。 该处函数对指令集上的指令做了一些封装，简化了使用流程。
 * 
 * @author 彭大帅
 * 
 */
public class Page {

	private static final String TAG = "Page";

	private IO IO = new IO();

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
	
	public static final int FONTSTYLE_BOLD = 0x08;
	public static final int FONTSTYLE_UNDERLINE = 0x100;
	public static final int FONTSTYLE_REVERSE = 0x200;
	public static final int FONTSTYLE_HIGHLIGHT = 0x400;
	
	public static final int FONTTYPE_STANDARD = 0;
	public static final int FONTTYPE_SMALL = 1;
	
	public static final int HRI_FONTTYPE_STANDARD = 0;
	public static final int HRI_FONTTYPE_SMALL = 1;
	
	public static final int HRI_FONTPOSITION_NONE = 0;
	public static final int HRI_FONTPOSITION_ABOVE = 1;
	public static final int HRI_FONTPOSITION_BELOW = 2;
	public static final int HRI_FONTPOSITION_ABOVE_AND_BELOW = 3;
	
	public static final int BARCODE_TYPE_UPCA = 65;
	public static final int BARCODE_TYPE_UPCE = 66;
	public static final int BARCODE_TYPE_EAN13 = 67;
	public static final int BARCODE_TYPE_EAN8 = 68;
	public static final int BARCODE_TYPE_CODE39 = 69;
	public static final int BARCODE_TYPE_ITF = 70;
	public static final int BARCODE_TYPE_CODEBAR = 71;
	public static final int BARCODE_TYPE_CODE93 = 72;
	public static final int BARCODE_TYPE_CODE128 = 73;
	
	public static final int BINARYALGORITHM_DITHER = 0;
	public static final int BINARYALGORITHM_THRESHOLD = 1;
	
	public static final int LINEHEIGHT_DEFAULT = 32;
	
	private int l, t, r, b, dir;
	int baseline = 21;
	int lineheight = LINEHEIGHT_DEFAULT;
	int rightspacing = 0;
	
	/**
	 * 选择页模式
	 * @return
	 */
	public boolean PageEnter()
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			byte buf[] = { 0x1b, 0x40, 0x1d, 0x50, (byte) 0xc8, (byte) 0xc8, 0x1b, 0x4c, 0x1b, 0x33, (byte)(lineheight) };
			int len = buf.length;

			if(IO.Write(buf, 0, len) == len)
				result = true;
			else
				result = false;
			
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	/**
	 * 页模式下打印页面内容
	 * @return
	 */
	public boolean PagePrint()
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			byte buf[] = { 0x1b, 0x0c };
			int len = buf.length;

			result = (IO.Write(buf, 0, len) == len);
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	/**
	 * 退出页模式
	 * @return
	 */
	public boolean PageExit()
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			byte buf[] = { 0x1b, 0x53 };
			int len = buf.length;

			result = (IO.Write(buf, 0, len) == len);
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	/**
	 * 页模式下设置打印区域
	 * @param left 打印区域左上角x坐标
	 * @param top 打印区域左上角y坐标
	 * @param right 打印区域右下角x坐标
	 * @param bottom 打印区域右下角y坐标
	 * @param direction 0 自左向右，1 自下向上，2自右向左，3自上向下
	 * @return
	 */
	public boolean SetPrintArea(int left, int top, int right, int bottom, int direction)
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			byte buf[] = { 0x1b, 0x57, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1b, 0x54, 0x00 };
			int len = buf.length;

			buf[2] = (byte)left;
			buf[3] = (byte)(left >> 8);
			buf[4] = (byte)top;
			buf[5] = (byte)(top >> 8);
			buf[6] = (byte)(right - left);
			buf[7] = (byte)((right - left) >> 8);
			buf[8] = (byte)(bottom - top);
			buf[9] = (byte)((bottom - top) >> 8);

			buf[12] = (byte)direction;

			l = left;
			t = top;
			r = right;
			b = bottom;
			dir = direction;

			result = (IO.Write(buf, 0, len) == len);
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	public boolean SetLineHeight(int nLineHeight)
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			lineheight = nLineHeight;
			
			byte buf[] = { 0x1b, 0x33, (byte)(lineheight) };
			int len = buf.length;

			result = (IO.Write(buf, 0, len) == len);
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	public boolean SetCharacterRightSpacing(int nRightSpacing)
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			rightspacing = nRightSpacing;
			
			byte buf[] = { 0x1b, 0x20, (byte)(rightspacing), 0x1c, 0x53, 0x00, (byte)(rightspacing) };
			int len = buf.length;

			result = (IO.Write(buf, 0, len) == len);
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	private int ComputeStringWidth(String pszString, int nEngCharWidth, int nChnCharWidth)
	{
		int nWidth = 0;
		for (int i = 0; i < pszString.length(); ++i)
		{
			if (pszString.charAt(i) < 0x20)
				break;
			else if (pszString.charAt(i) < 0x100)
				nWidth += nEngCharWidth + rightspacing;
			else
				nWidth += nChnCharWidth + rightspacing;
		}
		return nWidth;
	}
	
	/**
	 * 画文本
	 * @param pszString 要打印的内容
	 * @param x 指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y 指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param nWidthScale 指定宽度放大倍数 [0,7]
	 * @param nHeightScale 指定高度放大倍数 [0,7]
	 * @param nFontType 字体类型 0 标准字体 1 压缩字体
	 * @param nFontStyle 指定字体风格，可以为下表中的一个或者若干个（相加即可） 
	 * 					0x00	正常
	 * 					0x08	加粗
	 * 					0x100	下划线
	 * 					0x200	倒置（只在行首有效）
	 * 					0x400	反显（黑底白字）
	 * @return
	 */
	public boolean DrawText(String pszString, int x, int y, int nWidthScale, int nHeightScale, int nFontType, int nFontStyle)
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			// 左对齐
			if (x == -1)
			{
				x = 0;
			}
			else if (x == -2)
			{
				int nWidth = ComputeStringWidth(pszString, 12 * (nWidthScale + 1), 24 * (nWidthScale + 1));
				if (dir == 0 || dir == 2)
				{
					x = ((r - l) - nWidth) / 2;
				}
				else if (dir == 1 || dir == 3)
				{
					x = ((b - t) - nWidth) / 2;
				}
			}
			else if (x == -3)
			{
				int nWidth = ComputeStringWidth(pszString, 12 * (nWidthScale + 1), 24 * (nWidthScale + 1));
				if (dir == 0 || dir == 2)
				{
					x = (r - l) - nWidth;
				}
				else if (dir == 1 || dir == 3)
				{
					x = (b - t) - nWidth;
				}
			}

			if (y >= 0)
			{
				y += 24 * (1 + nHeightScale) - baseline;
			}

			byte bufx[] = { 0x1b, 0x24, (byte)(x), (byte)(x >> 8) };
			byte bufy[] = { 0x1d, 0x24, (byte)(y), (byte)(y >> 8) };
			byte buffont[] = { 0x1b, 0x21, (byte)(nFontType) };
			byte bufscale[] = { 0x1d, 0x21, (byte)(nWidthScale << 4 | nHeightScale) };
			byte bufbold[] = { 0x1b, 0x45,  (byte) ((nFontStyle & FONTSTYLE_BOLD) == FONTSTYLE_BOLD ? 1 : 0) };
			byte bufunderline[] = { 0x1b, 0x2d, (byte) ((nFontStyle & FONTSTYLE_UNDERLINE) == FONTSTYLE_UNDERLINE ? 2 : 0) };
			byte bufreverse[] = { 0x1b, 0x7b, (byte) ((nFontStyle & FONTSTYLE_REVERSE) == FONTSTYLE_REVERSE ? 1 : 0) };
			byte bufhighlight[] = { 0x1d, 0x42, (byte) ((nFontStyle & FONTSTYLE_HIGHLIGHT) == FONTSTYLE_HIGHLIGHT ? 1 : 0) };
			byte head[] = { 0x1c, 0x26, 0x1b, 0x39, 0x01 };
			byte szContent[] = pszString.getBytes();
			
			byte[] buf = byteArraysToBytes(new byte[][]{bufx,bufy,buffont,bufscale,bufbold,bufunderline,bufreverse,bufhighlight,head,szContent});
			int len = buf.length;
			
			result = (IO.Write(buf, 0, len) == len);
			
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	// UPCA码宽固定113
	@SuppressWarnings("unused")
	private int ComputeUPCAWidth(String pszString, int nBarcodeUnitWidth)
	{
		int w = 113;
		return w * nBarcodeUnitWidth;
	}
	
	@SuppressWarnings("unused")
	private int ComputeUPCEWidth(String pszString, int nBarcodeUnitWidth)
	{
		int w = 51;
		return w * nBarcodeUnitWidth;
	}
	
	@SuppressWarnings("unused")
	private int ComputeEAN13Width(String pszString, int nBarcodeUnitWidth)
	{
		int EAN13_MODULE = 7;
		int EAN13_DIGITS = 12;
		
		//static bool quiet_zone[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
		int quiet_zone[] = { 0, };
		int lead_trailer[] = {1, 0, 1};
		int separator[] = {0, 1, 0, 1, 0};

		int EAN13_WIDTH = quiet_zone.length * 2 + lead_trailer.length * 2 + separator.length + EAN13_MODULE*EAN13_DIGITS;

		int w = EAN13_WIDTH;
		return w * nBarcodeUnitWidth;
	}
	
	@SuppressWarnings("unused")
	private int ComputeEAN8Width(String pszString, int nBarcodeUnitWidth)
	{
		int w = 67;
		// int w = 7 + 67 + 7;
		return w * nBarcodeUnitWidth;
	}
	
	/**
	 * 画条码。条形码暂时不支持使用对齐方式定位
	 * @param pszString	条码内容
	 * @param x	指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y	指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param nBarcodeUnitWidth	指定条码的基本元素宽度。[2,6]
	 * @param nBarcodeHeight	条码高度[1,255]
	 * @param nHriFontType		指定 HRI（Human Readable Interpretation）字符的字体类型。
	 * 							0x00 标准ASCII	0x01 压缩ASCII
	 * @param nHriFontPosition	指定HRI（Human Readable Interpretation）字符的位置。
	 * 							0x00 不打印	0x01 只在条码上方打印	0x02 只在条码下方打印	0x03 条码上、下方都打印
	 * @param nBarcodeType		条码类型。可以为以下列表中所列值之一。
	 * 							0x41 UPC-A
	 * 							0x42 UPC-C
	 * 							0x43 JAN13(EAN13)
	 * 							0x44 JAN8(EAN8)
	 * 							0x45 CODE39
	 * 							0x46 ITF
	 * 							0x47 CODEBAR
	 * 							0x48 CODE93
	 * 							0x49 CODE 128
	 * @return
	 */
	public boolean DrawBarcode(String pszString, int x, int y, int nBarcodeUnitWidth, int nBarcodeHeight, int nHriFontType, int nHriFontPosition, int nBarcodeType)
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			if (y >= 0)
			{
				int nHriFontHeight = nHriFontType == 0 ? 24 : 17;
				int nHriCharPrint = (int) Math.ceil((nHriFontPosition & 0x3) / 2.0);
				y += nHriFontHeight*nHriCharPrint + nBarcodeHeight - baseline;
				//if (nHriFontPosition == 3)
				//	y += 24;
			}

			byte bufx[] = { 0x1b, 0x24, (byte)(x), (byte)(x >> 8) };
			byte bufy[] = { 0x1d, 0x24, (byte)(y), (byte)(y >> 8) };

			byte head[] = { 0x1d, 0x77, 0x02,
				0x1d, 0x68, (byte) 0xa2,
				0x1d, 0x66, 0x00,
				0x1d, 0x48, 0x02,
				0x1d, 0x6b, 0x00, 0x00, };

			byte szContent[] = pszString.getBytes();
			
			head[2] = (byte)nBarcodeUnitWidth;
			head[5] = (byte)nBarcodeHeight;
			head[8] = (byte)(nHriFontType & 0x1);
			head[11] = (byte)(nHriFontPosition & 0x3);
			head[14] = (byte)(nBarcodeType);
			head[15] = (byte)(szContent.length);

			byte[] buf = byteArraysToBytes(new byte[][]{bufx,bufy,head,szContent});
			int len = buf.length;
			
			result = (IO.Write(buf, 0, len) == len);
			
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	private int ComputeQRCodeWidth(String pszContent, int nQRCodeUnitWidth, int nVersion, int nEcLevel)
	{
		int w = 0;
		
		int typeNumber = QRCode.getMinimumQRCodeTypeNumber(pszContent, nEcLevel - 1);
		if(nVersion < typeNumber) // 如果指定了Version但是数据超量，则自动扩充Version
			nVersion = typeNumber;
		
		QRCode codes = QRCode.getFixedSizeQRCode(pszContent, nEcLevel - 1, nVersion);
		if(codes != null)
		{
			Boolean[][] bModules = codes.getModules();
			w = bModules.length;	
		}

	 	return w * nQRCodeUnitWidth;
	 }
	
	/**
	 * 画二维码
	 * @param pszString 二维码文本
	 * @param x 指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y 指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param nQRCodeUnitWidth QR码单元宽度，范围[1,16]。 QR码单元宽度越大，QR码越大。
	 * @param nVersion QR码版本。0表示自动计算版本。 QR码版本越大，能编码的字符就越多，QR码也越大。
	 * @param nEcLevel QR码纠错等级。[1,4]
	 * @return
	 */
	public boolean DrawQRCode(String pszString, int x, int y, int nQRCodeUnitWidth, int nVersion, int nEcLevel)
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			// 左对齐
			if (x == -1)
			{
				x = 0;
			}
			else if (x == -2)
			{
				int nWidth = ComputeQRCodeWidth(pszString, nQRCodeUnitWidth, nVersion, nEcLevel);

				if (dir == 0 || dir == 2)
				{
					x = ((r - l) - nWidth) / 2;
				}
				else if (dir == 1 || dir == 3)
				{
					x = ((b - t) - nWidth) / 2;
				}
			}
			else if (x == -3)
			{
				int nWidth = ComputeQRCodeWidth(pszString, nQRCodeUnitWidth, nVersion, nEcLevel);

				if (dir == 0 || dir == 2)
				{
					x = (r - l) - nWidth;
				}
				else if (dir == 1 || dir == 3)
				{
					x = (b - t) - nWidth;
				}
			}

			if (y >= 0)
			{
				y += lineheight;
			}

			byte bufx[] = { 0x1b, 0x24, (byte)(x), (byte)(x >> 8) };
			byte bufy[] = { 0x1d, 0x24, (byte)(y), (byte)(y >> 8) };

			byte head[] = {
				0x1d, 0x77, 0x02,
				0x1d, 0x6b, 0x61, 0x0a, 0x01, 0x00, 0x00, };

			byte szContent[] = pszString.getBytes();
			
			head[2] = (byte)nQRCodeUnitWidth;

			head[6] = (byte)nVersion;
			head[7] = (byte)nEcLevel;
			head[8] = (byte)(szContent.length);
			head[9] = (byte)(szContent.length >> 8);

			byte[] buf = byteArraysToBytes(new byte[][]{bufx,bufy,head,szContent});
			int len = buf.length;
			
			result = (IO.Write(buf, 0, len) == len);
			
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	/**
	 * 画位图
	 * @param mBitmap 要打印的位图
	 * @param x 指定水平方向的起始点位置离打印区域左边界的点数。（横坐标）
	 * @param y 指定垂直方向的起始点位置离打印区域上边界的点数。（纵坐标）
	 * @param dwWidth 要打印的宽度
	 * @param dwHeight 要打印的高度
	 * @param nBinaryAlgorithm
	 * @return
	 */
	public boolean DrawBitmap(Bitmap mBitmap, int x, int y, int dwWidth, int dwHeight, int nBinaryAlgorithm)
	{
		if(!IO.IsOpened())
			return false;
		
		boolean result = false;
		
		IO.Lock();
		
		try
		{
			// 左对齐
			if (x == -1)
			{
				x = 0;
			}
			else if (x == -2)
			{
				int nWidth = dwWidth;

				if (dir == 0 || dir == 2)
				{
					x = ((r - l) - nWidth) / 2;
				}
				else if (dir == 1 || dir == 3)
				{
					x = ((b - t) - nWidth) / 2;
				}
			}
			else if (x == -3)
			{
				int nWidth = dwWidth;

				if (dir == 0 || dir == 2)
				{
					x = (r - l) - nWidth;
				}
				else if (dir == 1 || dir == 3)
				{
					x = (b - t) - nWidth;
				}
			}

			if (y >= 0)
			{
				y += dwHeight - baseline;
			}

			byte bufx[] = { 0x1b, 0x24, (byte)(x), (byte)(x >> 8) };
			byte bufy[] = { 0x1d, 0x24, (byte)(y), (byte)(y >> 8) };
			
			int dstw = dwWidth;
			int dsth = dwHeight;
			int [] dst = new int[dstw * dsth];
			
			mBitmap = ImageProcessing.resizeImage(mBitmap, dstw, dsth);
			mBitmap.getPixels(dst, 0, dstw, 0, 0, dstw, dsth);
			byte[] gray = ImageProcessing.GrayImage(dst);
			
			boolean[] dithered = new boolean[dstw * dsth];
			if(nBinaryAlgorithm == 0)
				ImageProcessing.format_K_dither16x16(dstw, dsth, gray, dithered);
			else
				ImageProcessing.format_K_threshold(dstw, dsth, gray, dithered);
			
			byte[] data = ImageProcessing.Image1ToTM88IVRasterCmd(dstw, dsth, dithered);
			
			byte[] buf = byteArraysToBytes(new byte[][]{bufx,bufy,data});
			int len = buf.length;
			
			result = (IO.Write(buf, 0, len) == len);
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			IO.Unlock();
		}
		
		return result;
	}
	
	private byte[] byteArraysToBytes(byte[][] data) {
		int length = 0;
		for (int i = 0; i < data.length; i++)
			length += data[i].length;
		byte[] send = new byte[length];
		int k = 0;
		for (int i = 0; i < data.length; i++)
			for (int j = 0; j < data[i].length; j++)
				send[k++] = data[i][j];
		return send;
	}
}

