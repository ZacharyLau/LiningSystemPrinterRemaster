package com.queue.queuing.io;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.Log;

import java.util.Vector;

/**
 * 内存打印读写封装，将指令打印到内存中
 * 		
 * @author 彭大帅
 * 
 */
@TargetApi(Build.VERSION_CODES.GINGERBREAD)
public class MemoryPrinting extends IO {
	
	private static final String TAG = "MemoryPrinting";
	
	// 写入缓冲区
	private Vector<Byte> writeBuffer = new Vector<Byte>();
	
	/***
	 * 写数据
	 */
	@Override
	public int Write(byte[] buffer, int offset, int count) 
	{
		int nBytesWritten = 0;
		
		try
		{
			for (nBytesWritten = 0; nBytesWritten < count; ++nBytesWritten)
				writeBuffer.add(buffer[offset + nBytesWritten]);
		}
		catch(Exception ex)
		{
			Log.e(TAG, ex.toString());
			nBytesWritten = -1;
		}
		finally
		{
			
		}
		
		return nBytesWritten;
	}

	/***
	 * 读数据
	 */
	@Override
	public int Read(byte[] buffer, int offset, int count, int timeout)
	{
		int nBytesReaded = 0;
		
		return nBytesReaded;
	}
	
	/***
	 * 忽略缓冲区中的数据
	 */
	@Override
	public void SkipAvailable()
	{
		
	}	
	
	/***
	 * 是否已连接
	 */
	public boolean IsOpened() 
	{
		return true;
	}

	/***
	 * 设置IO回调接口
	 * @param callBack
	 */
	public void SetCallBack(IOCallBack callBack)
	{
		
	}
	
	/**
	 * 获取之前写入的数据，这会清楚已存储的数据。
	 * @return
	 */
	public byte[] GetWriteBuffer()
	{
		int count = writeBuffer.size();
		byte[] buffer = new byte[count];
		for (int i = 0; i < count; ++i)
			buffer[i] = writeBuffer.get(i);
		return buffer;
	}
}
