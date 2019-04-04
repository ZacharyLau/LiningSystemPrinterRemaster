package com.queue.queuing.io;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * NETClient底层读写封装
 * 
 * @author 彭大帅
 * 
 */
@TargetApi(Build.VERSION_CODES.BASE)
public class NETClient extends IO{

	private static final String TAG = "NETClient";
	
	private Socket mmClientSocket = null;
	private DataInputStream is = null;
	private DataOutputStream os = null;
	private AtomicBoolean isOpened = new AtomicBoolean(false);
	private AtomicBoolean isReadyRW = new AtomicBoolean(false);
	private IOCallBack cb = null;
	private Vector<Byte> rxBuffer = new Vector<Byte>();
	
	private AtomicLong nIdleTime = new AtomicLong(0);
	
	private final ReentrantLock mCloseLocker = new ReentrantLock();
	
	/***
	 * 连接网络打印机
	 * @param IPAddress 打印机IP地址
	 * @param PortNumber 打印机端口号（默认9100端口）
	 * @return
	 */
	public boolean Open(String IPAddress, int PortNumber, int Timeout)
	{
		Lock();
		
		try
		{
			if(isOpened.get())
				throw new Exception("Already open");
			
			isReadyRW.set(false);
			
			try 
			{	
				SocketAddress socketAddress = new InetSocketAddress(IPAddress, PortNumber);
				mmClientSocket = new Socket();
				mmClientSocket.connect(socketAddress, Timeout);
				mmClientSocket.setSendBufferSize(512); // 这种设置对WIFI机器也有很好的效果
				mmClientSocket.setTcpNoDelay(true);
				os = new DataOutputStream(mmClientSocket.getOutputStream());
				is = new DataInputStream(mmClientSocket.getInputStream());
				isReadyRW.set(true);
			}
			catch (Exception e)
			{
				Log.e(TAG, e.toString());
				try
				{
					mmClientSocket.close();
				}
				catch(Exception closeException)
				{
					Log.i(TAG, closeException.toString());
				}
				finally
				{
					mmClientSocket = null;
					os = null;
					is = null;
				}
			}
			
			if (isReadyRW.get())
			{
				Log.v(TAG, "Connected to " + IPAddress + ":" + PortNumber);
				rxBuffer.clear();
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
	 */
	public void Close() 
	{
		mCloseLocker.lock();
		
		try
		{
			try
			{
				if (null != mmClientSocket) 
				{
					mmClientSocket.shutdownInput();
				}
			}
			catch(Exception ex)
			{
				Log.i(TAG, ex.toString());
			}
			finally
			{
				
			}
			
			try
			{
				if (null != mmClientSocket) 
				{
					mmClientSocket.shutdownOutput();
				}
			}
			catch(Exception ex)
			{
				Log.i(TAG, ex.toString());
			}
			finally
			{
				
			}
			
			try
			{
				if (null != mmClientSocket) 
				{
					mmClientSocket.close();
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
			
			mmClientSocket = null;
			is = null;
			os = null;
			
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

			os.write(buffer, offset, count);
			os.flush();
			
			nBytesWritten = count;
			
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
					int available = is.available();
					if (available > 0) 
					{
						byte[] receive = new byte[available];
						int nReceived = is.read(receive);
						if(nReceived > 0)
			            {
				            for(int i = 0; i < nReceived; ++i)
								rxBuffer.add(receive[i]);
			            }
					}
					else
					{
						Thread.sleep(1);
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
			is.skip(is.available());
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
	
}
