package com.queue.queuing.io;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
public class CP2102Printing extends IO {
	
    private static final String TAG = "CP2102Printing";
    
	private UsbEndpoint mUsbEndpointOut = null;
	private UsbEndpoint mUsbEndpointIn = null;
	private UsbDeviceConnection mUsbDeviceConnection = null;
	private AtomicBoolean isOpened = new AtomicBoolean(false);
	private AtomicBoolean isReadyRW = new AtomicBoolean(false);
	private IOCallBack cb = null;
	private Vector<Byte> rxBuffer = new Vector<Byte>();
	
	private AtomicLong nIdleTime = new AtomicLong(0);
	
	private final ReentrantLock mCloseLocker = new ReentrantLock();
	
	private String name;
	private String address;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			
			String action = intent.getAction();
			UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

			if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				if (device == null)
					return;
				if (!device.getDeviceName().equalsIgnoreCase(address))
					return;
				Close();
			}
		}

	};
	private IntentFilter filter = new IntentFilter();
	private Context context;
	
	private void RegisterReceiver() {
		if (!filter.hasAction(UsbManager.ACTION_USB_DEVICE_DETACHED))
			filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
		context.registerReceiver(receiver, filter);
		
		Log.i(TAG, "RegisterReceiver");
	}

	private void UnregisterReceiver() {
		try {
			context.unregisterReceiver(receiver);
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		}
		Log.i(TAG, "UnregisterReceiver");
	}
	
	/***
	 * 连接USB打印机
	 * @param manager
	 * @param device
	 * @return
	 */
	public boolean Open(UsbManager manager, UsbDevice device, int baudrate, Context mContext)
	{
		Lock();
		
		try
		{
			if(isOpened.get())
				throw new Exception("Already open");
			
			if (null == mContext)
				throw new Exception("Null Pointer mContext");
			context = mContext;
			
			if (null == device)
				throw new Exception("Null Pointer device");
			address = device.getDeviceName();
			
			name = "VID" + device.getVendorId() + "PID" + device.getProductId();
				
			isReadyRW.set(false);
			
			try
			{
				if(!manager.hasPermission(device))
					throw new Exception("No Permission");
				
				UsbInterface usbInterface = null;
				UsbEndpoint usbEndpointOut = null;
				UsbEndpoint usbEndpointIn = null;
				for (int k = 0; k < device.getInterfaceCount(); k++) 
				{
					usbInterface = device.getInterface(k);
					usbEndpointOut = null;
					usbEndpointIn = null;
					for (int j = 0; j < usbInterface.getEndpointCount(); j++) 
					{
						UsbEndpoint endpoint = usbInterface.getEndpoint(j);
						if (endpoint.getDirection() == UsbConstants.USB_DIR_OUT && endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
						{
							usbEndpointOut = endpoint;
						}
						else if (endpoint.getDirection() == UsbConstants.USB_DIR_IN && endpoint.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK)
						{
							usbEndpointIn = endpoint;
						}
						// 如果找到了符合要求的端点，那么break;
						if ((null != usbEndpointOut) && (null != usbEndpointIn))
							break;
					}
					
					if ((null != usbEndpointOut) && (null != usbEndpointIn))
						break;
				}
				
				if ((null == usbInterface) || (null == usbEndpointOut) || (null == usbEndpointIn))
					throw new Exception("No Endpoint");
				
				UsbDeviceConnection usbDeviceConnection = manager.openDevice(device);
				if (null == usbDeviceConnection)
				{
					throw new Exception("Open Device Failed");
				}
				
				if(!usbDeviceConnection.claimInterface(usbInterface, true))
				{
					usbDeviceConnection.close();
					throw new Exception("ClaimInterface Failed");
				}
				
				mUsbEndpointOut = usbEndpointOut;
				mUsbEndpointIn = usbEndpointIn;
				mUsbDeviceConnection = usbDeviceConnection;
				
				setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_ENABLE);
	            setConfigSingle(SILABSER_SET_MHS_REQUEST_CODE, MCR_ALL | CONTROL_WRITE_DTR | CONTROL_WRITE_RTS);
	            setConfigSingle(SILABSER_SET_BAUDDIV_REQUEST_CODE, BAUD_RATE_GEN_FREQ / DEFAULT_BAUD_RATE);
	            setParameters(DATABITS_8, STOPBITS_1, PARITY_NONE);
	            setChars();
	            setFlow();
	            setBaudRate(baudrate);
				isReadyRW.set(true);
			}
			catch(Exception ex)
			{
				Log.i(TAG, ex.toString());
			}
			
			if (isReadyRW.get())
			{
				Log.v(TAG, "Connected to CP2102 Device");
				rxBuffer.clear();
				
				RegisterReceiver();
				
				boolean bCaysnPrinter = false;
				try
				{
					SharedPreferences records = context.getSharedPreferences(TAG, 0);
					bCaysnPrinter = records.getBoolean(name, false);
				}
				catch(Exception ex)
				{
					Log.v(TAG, ex.toString());
				}
				bCaysnPrinter = true;
				if(!bCaysnPrinter)
				{
					if(1 == PTR_CheckPrinter())
					{
						bCaysnPrinter = true;
					}
					
					if(bCaysnPrinter)
					{
						try
						{
							SharedPreferences records = context.getSharedPreferences(TAG, 0);
							SharedPreferences.Editor editor = records.edit();
							editor.putBoolean(name, bCaysnPrinter);
							editor.commit();
						}
						catch(Exception ex)
						{
							Log.v(TAG, ex.toString());
						}
					}
				}
			}
			
			isOpened.set(isReadyRW.get());
			
			if (null != cb)
			{
				if(isOpened.get())
				{
					cb.OnOpen();
				}
				else
				{
					cb.OnOpenFailed();
				}
			}
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return isOpened.get();
	}

	/***
	 * 关闭连接
	 * 可在任意线程调用
	 * 一旦调用Close，任何IO操作都会立刻退出。
	 */
	public void Close() 
	{
		mCloseLocker.lock();

		try
		{
			try
			{
				setConfigSingle(SILABSER_IFC_ENABLE_REQUEST_CODE, UART_DISABLE);
				if (null != mUsbDeviceConnection)
				{
					mUsbDeviceConnection.close();
				}
			}
			catch(Exception ex)
			{
				Log.i(TAG, ex.toString());
			}
			finally
			{

			}
			
			if (!isReadyRW.get())
				throw new Exception();
			
			mUsbEndpointOut = null;
			mUsbEndpointIn = null;
			mUsbDeviceConnection = null;
			UnregisterReceiver();
			
			isReadyRW.set(false);
			
			if (!isOpened.get())
				throw new Exception();
				
			isOpened.set(false);
			
			if(null != cb)
			{
				cb.OnClose();
			}
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			mCloseLocker.unlock();
		}
	}

	/***
	 * 写数据
	 */
	@Override
	public int Write(byte[] buffer, int offset, int count) 
	{
		if(!isReadyRW.get())
			return -1;
		
		Lock();
		
		int nBytesWritten = 0;
		
		try
		{
			nIdleTime.set(0);

			while (nBytesWritten < count) 
			{
				if(!isReadyRW.get())
					throw new Exception("Not Ready For Read Write");
								
				int nPackageSize = Math.min(mUsbEndpointOut.getMaxPacketSize(), count - nBytesWritten);
				byte[] data = new byte[nPackageSize];
				
				//byte[] data = new byte[count - nBytesWritten];
				System.arraycopy(buffer, offset + nBytesWritten, data, 0, data.length);
								
				int nSended = mUsbDeviceConnection.bulkTransfer(mUsbEndpointOut, data, data.length, Integer.MAX_VALUE);
				if (nSended < 0) 
					throw new Exception("Write Failed");
				else 
					nBytesWritten += nSended;
			}
			
			nIdleTime.set(System.currentTimeMillis());
		}
		catch(Exception ex)
		{
			Log.e(TAG, ex.toString());
			Close();
			
			nBytesWritten = -1;
		}
		finally
		{
			Unlock();
		}
		
		return nBytesWritten;
	}

	/***
	 * 读数据
	 */
	@Override
	public int Read(byte[] buffer, int offset, int count, int timeout)
	{
		if(!isReadyRW.get())
			return -1;
		
		Lock();
		
		int nBytesReaded = 0;
		
		try
		{
			nIdleTime.set(0);
			
			long time = System.currentTimeMillis();

			while ((System.currentTimeMillis() - time) < timeout)
			{
				if(!isReadyRW.get())
					throw new Exception("Not Ready For Read Write");
				
				if (nBytesReaded == count)
					break;

				if (rxBuffer.size() > 0) 
				{
					buffer[offset + nBytesReaded] = rxBuffer.get(0);
					rxBuffer.remove(0);
					nBytesReaded += 1;
				}
				else
				{
					int nPackageSize = mUsbEndpointIn.getMaxPacketSize();
					byte[] receive = new byte[nPackageSize];
					int nReceived = mUsbDeviceConnection.bulkTransfer(mUsbEndpointIn, receive, receive.length, 100);
					if(nReceived > 0)
		            {
			            for(int i = 0; i < nReceived; ++i)
							rxBuffer.add(receive[i]);
		            }
				}
			}
		
			nIdleTime.set(System.currentTimeMillis());
		}
		catch(Exception ex)
		{
			Log.e(TAG, ex.toString());
			Close();
			
			nBytesReaded = -1;
		}
		finally
		{
			Unlock();
		}
		
		return nBytesReaded;
	}
	
	/***
	 * 忽略缓冲区中的数据
	 */
	@Override
	public void SkipAvailable()
	{
		Lock();
		
		try
		{
			rxBuffer.clear();
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
	}	
	
	/***
	 * 是否已连接
	 */
	public boolean IsOpened() 
	{
		return isOpened.get();
	}

	/***
	 * 设置IO回调接口
	 * @param callBack
	 */
	public void SetCallBack(IOCallBack callBack)
	{
		Lock();
		
		try
		{
			cb = callBack;
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
	}
			
	/***
	 * 检查打印机 	-1 无返回	0 有返回，无加密		1 有返回，有加密	
	 * @return
	 */
	private int PTR_CheckEncrypt()
	{
		Lock();
		
		int result = -1;
		
		try
		{
			Random rmByte = new Random(System.currentTimeMillis());
			byte[] data = new byte[]{ 0x1F, 0x28, 0x63, 0x08, 0x00, 0x1b, 0x40, (byte)0xd2, (byte)0xd3, (byte)0xd4, (byte)0xd5, 0x1b, 0x40, 0x00, 0x00, 0x00, 0x00, 0x1d, 0x72, 0x01};
			for(int i = 0; i < 4; ++i)
			{
				data[7+i]= (byte)rmByte.nextInt(0x9);
			}
			byte[] cmd = new byte[60 + data.length];
			System.arraycopy(data, 0, cmd, 60, data.length);
			SkipAvailable();
			if(Write(cmd,0,cmd.length) == cmd.length)
			{
				byte[] rec = new byte[7];
				
				while (Read(rec, 0, 1, 3000) == 1)
				{
					result = 0;
					
					if (rec[0] == 0x63)
					{
						if (Read(rec, 1, 5, 3000) == 5)
						{
							if(rec[1] == 0x5F)
							{
								long v1 = (data[5] & 0x0FFl) << 24 | (data[6] & 0x0FFl) << 16 | (data[7] & 0x0FFl) << 8 | (data[8] & 0x0FFl);
								long v2 = (data[9] & 0x0FFl) << 24 | (data[10] & 0x0FFl) << 16 | (data[11] & 0x0FFl) << 8 | (data[12] & 0x0FFl);
								long vadd = (v1 + v2) & 0x0FFFFFFFFl;
								long vxor = (v1 ^ v2) & 0x0FFFFFFFFl;
								long l1 = v1 & 0xFFFFl;
								long h2 = (v2 >> 16) & 0x0FFFFl;
		
								v1 = (l1 * l1 - h2 * h2) & 0x0FFFFFFFFl;
								v1 = (vadd - vxor - v1) & 0x0FFFFFFFFl;
		
								v2 = (rec[2] & 0x0FFl) << 24 | (rec[3] & 0x0FFl) << 16 | (rec[4] & 0x0FFl) << 8 | (rec[5] & 0x0FFl);
								if (v1 == v2)
								{
									result = 1;
								}
							}
						}
						break;
					}
					else if((rec[0] & 0x90) == 0)
					{
						break;
					}
				}
			}
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return result;
	}
		
	private boolean PTR_CheckKey() 
	{
		Lock();
		
		boolean result = false;
		
		try
		{
			byte[] key = "XSH-KCEC".getBytes();
			byte[] random = new byte[8];
			Random rmByte = new Random(System.currentTimeMillis());
			for(int i = 0; i < 8; ++i)
			{
				random[i]= (byte)rmByte.nextInt(0x9);
			}
			final int HeaderSize = 5;
			byte[] recHeader = new byte[HeaderSize];
			byte[] recData = null;
			int rec = 0;
			int recDataLen = 0;
			byte[] randomlen = new byte[2];
			randomlen[0] = (byte) (random.length & 0xff);
			randomlen[1] = (byte) ((random.length >> 8) & 0xff);
			byte[] data = ByteUtils.byteArraysToBytes(new byte[][] {new byte[] { 0x1f, 0x1f, 0x02 }, randomlen, random, new byte[] { 0x1b, 0x40 } });
			SkipAvailable();
			Write(data, 0, data.length);
			rec = Read(recHeader, 0, HeaderSize, 1000);
			if (rec == HeaderSize) 
			{
				recDataLen = (recHeader[3] & 0xff) + ((recHeader[4] << 8) & 0xff);
				recData = new byte[recDataLen];
				rec = Read(recData, 0, recDataLen, 1000);
				if (rec == recDataLen) 
				{

					byte[] encrypted = recData;
					byte[] decrypted = new byte[encrypted.length + 1];
					/**
					 * 对数据进行解密
					 */
					DES2 des2 = new DES2();
					// 初始化密钥
					des2.yxyDES2_InitializeKey(key);
					des2.yxyDES2_DecryptAnyLength(encrypted, decrypted, encrypted.length);
					result = ByteUtils.bytesEquals(random, 0, decrypted, 0, random.length);
				}
			}
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return result;
	}
	
	private static int nCheckFaildTimes = 0;
	private static int nMaxCheckFailedTimes = 30;
	
	/***
	 * 检查打印机，如果不是本公司打印机，则打印提示内容
	 */
	private int PTR_CheckPrinter()
	{
		Lock();
		
		int check = -1;
		
		try
		{	
			for(int i = 0; i < 3; ++i)
			{
				// 桌面打印机加密命令
				check = PTR_CheckEncrypt();
				if(check == -1)
					continue;
				else
					break;
			}

			// 如果有返回，但是不支持桌面打印机加密，此处测试便携打印机加密命令
			if(check == 0)
			{
				// 便携打印机加密命令
				if(PTR_CheckKey())
					check = 1;
			}
			
			if (check == 1)
			{	// 如果检查成功，则归零
				nCheckFaildTimes = 0;
			}
			else if (check == 0)
			{
				// 如果有返回数据，但是加密失败，则将失败次数加1
				nCheckFaildTimes++;
			}
			
			if(nCheckFaildTimes >= nMaxCheckFailedTimes)
			{
				byte[] header = new byte[] { 0x0d, 0x0a, 0x1b, 0x40, 0x1c, 0x26, 0x1b, 0x39, 0x01 };
				byte[] txt = "----Unknow printer----\r\n".getBytes();
				byte[] cmd = new byte[header.length + txt.length];
				int offset = 0;
				System.arraycopy(header, 0, cmd, offset, header.length); offset += header.length;
				System.arraycopy(txt, 0, cmd, offset, txt.length); offset += txt.length;
				Write(cmd, 0, cmd.length);
			}
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return check;
	}
	
    /** 5 data bits. */
    public static final int DATABITS_5 = 5;

    /** 6 data bits. */
    public static final int DATABITS_6 = 6;

    /** 7 data bits. */
    public static final int DATABITS_7 = 7;

    /** 8 data bits. */
    public static final int DATABITS_8 = 8;

    /** No flow control. */
    public static final int FLOWCONTROL_NONE = 0;

    /** RTS/CTS input flow control. */
    public static final int FLOWCONTROL_RTSCTS_IN = 1;

    /** RTS/CTS output flow control. */
    public static final int FLOWCONTROL_RTSCTS_OUT = 2;

    /** XON/XOFF input flow control. */
    public static final int FLOWCONTROL_XONXOFF_IN = 4;

    /** XON/XOFF output flow control. */
    public static final int FLOWCONTROL_XONXOFF_OUT = 8;

    /** No parity. */
    public static final int PARITY_NONE = 0;

    /** Odd parity. */
    public static final int PARITY_ODD = 1;

    /** Even parity. */
    public static final int PARITY_EVEN = 2;

    /** Mark parity. */
    public static final int PARITY_MARK = 3;

    /** Space parity. */
    public static final int PARITY_SPACE = 4;

    /** 1 stop bit. */
    public static final int STOPBITS_1 = 1;

    /** 1.5 stop bits. */
    public static final int STOPBITS_1_5 = 3;

    /** 2 stop bits. */
    public static final int STOPBITS_2 = 2;
    
    
    private static final int DEFAULT_BAUD_RATE = 9600;
    
    private static final int USB_WRITE_TIMEOUT_MILLIS = 5000;
    
    /*
     * Configuration Request Types
     */
    private static final int REQTYPE_HOST_TO_DEVICE = 0x41;
    
    /*
     * Configuration Request Codes
     */
    private static final int SILABSER_IFC_ENABLE_REQUEST_CODE = 0x00;
    private static final int SILABSER_SET_BAUDDIV_REQUEST_CODE = 0x01;
    private static final int SILABSER_SET_LINE_CTL_REQUEST_CODE = 0x03;
    private static final int SILABSER_SET_MHS_REQUEST_CODE = 0x07;
    private static final int SILABSER_SET_BAUDRATE = 0x1E;
    private static final int CP210X_SET_FLOW = 0x13;
    private static final int CP210X_SET_CHARS = 0x19;
    /*
     * SILABSER_IFC_ENABLE_REQUEST_CODE
     */
    private static final int UART_ENABLE = 0x0001;
    private static final int UART_DISABLE = 0x0000;
    
    /*
     * SILABSER_SET_BAUDDIV_REQUEST_CODE
     */
    private static final int BAUD_RATE_GEN_FREQ = 0x384000;
    
    /*
     * SILABSER_SET_MHS_REQUEST_CODE
     */
    @SuppressWarnings("unused")
	private static final int MCR_DTR = 0x0001;
    @SuppressWarnings("unused")
    private static final int MCR_RTS = 0x0002;
    private static final int MCR_ALL = 0x0003;
    
    private static final int CONTROL_WRITE_DTR = 0x0100;
    private static final int CONTROL_WRITE_RTS = 0x0200;    

    
    private int setConfigSingle(int request, int value) {
        return mUsbDeviceConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, request, value, 
                0, null, 0, USB_WRITE_TIMEOUT_MILLIS);
    }

	private void setFlow() throws IOException {
    	byte[] data = { 0x00, 0x00, 0x00, 0x00, 0x03, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, 0x00, 0x00};
        int ret = mUsbDeviceConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, CP210X_SET_FLOW, 
                0, 0, data, 16, USB_WRITE_TIMEOUT_MILLIS);
        if (ret < 0) {
            throw new IOException("Error setting flow control.");
        }
    }
    
	private void setChars() throws IOException {
    	byte[] data = { 0x1a, 0x00, 0x00, 0x1a, 0x11, 0x13};
        int ret = mUsbDeviceConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, CP210X_SET_CHARS, 
                0, 0, data, data.length, USB_WRITE_TIMEOUT_MILLIS);
        if (ret < 0) {
            throw new IOException("Error setting flow control.");
        }
    }
    
    private void setBaudRate(int baudRate) throws IOException {
        byte[] data = new byte[] {
                (byte) ( baudRate & 0xff),
                (byte) ((baudRate >> 8 ) & 0xff),
                (byte) ((baudRate >> 16) & 0xff),
                (byte) ((baudRate >> 24) & 0xff)
        };
        int ret = mUsbDeviceConnection.controlTransfer(REQTYPE_HOST_TO_DEVICE, SILABSER_SET_BAUDRATE, 
                0, 0, data, 4, USB_WRITE_TIMEOUT_MILLIS);
        if (ret < 0) {
            throw new IOException("Error setting baud rate.");
        }
    }

    private void setParameters(int dataBits, int stopBits, int parity)
            throws IOException {
          
        int configBits = 0;
        
        switch (dataBits) {
            case DATABITS_5:
            	configBits |= 0x0500;
                break;
            case DATABITS_6:
            	configBits |= 0x0600;
                break;
            case DATABITS_7:
            	configBits |= 0x0700;
                break;
            case DATABITS_8:
            	configBits |= 0x0800;
                break;
            default:
            	configBits |= 0x0800;
                break;
        }
        
        switch (parity) {
            case PARITY_ODD:
            	configBits |= 0x0010;
                break;
            case PARITY_EVEN:
            	configBits |= 0x0020;
                break;            
        }
        
        switch (stopBits) {
            case STOPBITS_1:
            	configBits |= 0;
                break;
            case STOPBITS_2:
            	configBits |= 2;
                break;
        }
        
        setConfigSingle(SILABSER_SET_LINE_CTL_REQUEST_CODE, configBits);        
    }

}
