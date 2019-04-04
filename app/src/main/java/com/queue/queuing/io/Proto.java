package com.queue.queuing.io;

import android.util.Log;

import java.util.List;

/**
 * Prn2103的封装
 * 
 * @author 彭大帅
 * 
 */
public class Proto {

	private static final String TAG = "Proto";

	private IO IO = new IO();

	public void Set(IO io) {
		if (null != io) {
			IO = io;
		}
	}

	public IO GetIO() {
		return IO;
	}

	public boolean SendPackage(int cmd, int para, byte[] buffer, int offset, int count)
	{
		byte[] pcmdbuf = new byte[12 + count];
		if (count > 0)
			System.arraycopy(buffer, offset, pcmdbuf, 12, count);
		
		pcmdbuf[0] = 0x03;
		pcmdbuf[1] = (byte) 0xFF;
        pcmdbuf[2] = (byte)(cmd);
        pcmdbuf[3] = (byte)(cmd >> 8);
        pcmdbuf[4] = (byte)(para);
        pcmdbuf[5] = (byte)(para >> 8);
        pcmdbuf[6] = (byte)(para >> 16);
        pcmdbuf[7] = (byte)(para >> 24);
        pcmdbuf[8] = (byte)(count);
        pcmdbuf[9] = (byte)(count >> 8);
        pcmdbuf[10] = 0;
        pcmdbuf[11] = 0;
        for (int j = 0; j < 10; j++)
            pcmdbuf[10] ^= pcmdbuf[j];
        for (int j = 0; j < count; j++)
        {
            pcmdbuf[11] ^= pcmdbuf[12+j];
        }
		
		if(!IO.IsOpened())
			return false;
		
		IO.Lock();
		
		boolean result = false;
		try
		{
			byte[] rechead = new byte[12];
			
			IO.SkipAvailable();
			if (IO.Write(pcmdbuf, 0, pcmdbuf.length) == pcmdbuf.length)
			{
				int timeout = 3000;
				long beginTime = System.currentTimeMillis();
				while (true)
				{
					if (!IO.IsOpened())
						break;
					
					if (System.currentTimeMillis() - beginTime > timeout)
						break;
					
					int nBytesReaded = IO.Read(rechead, 0, 1, timeout);
					if (nBytesReaded <= 0)
						break;
					
					if (nBytesReaded == 1)
					{
						if (rechead[0] == 0x03)
						{
							if (IO.Read(rechead, 1, 1, timeout) == 1)
							{
								if ((rechead[1] & 0xFF) == 0xFE)
								{
									if (IO.Read(rechead, 2, 10, timeout) == 10)
									{
										int reccmd = (rechead[2] & 0xFF) | (rechead[3] & 0xFF) << 8;
										int recpara = (rechead[4] & 0xFF) | (rechead[5] & 0xFF) << 8 | (rechead[6] & 0xFF) << 16 | (rechead[7] & 0xFF) << 24;
										if((cmd == reccmd) && (para == recpara))
										{
											result = true;
											break;
										}
									}
								}
							}
						}
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
			IO.Unlock();
		}

		return result;
	}
	
	public boolean SendPackage(int cmd, int para, byte[] buffer, int offset, int count, int retry)
	{
		if(!IO.IsOpened())
			return false;
		
		IO.Lock();
		
		boolean result = false;
		
		try {
			for (int i = 0; i < retry; ++i) {
				result = SendPackage(cmd, para, buffer, offset, count);
				if (result)
					break;
				else
					Thread.sleep(100);
			}
			
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}
	
	public interface OnProgressCallBack {
		void OnProgress(double index, double total);
	}

	public boolean Download(byte[] data, int cmd, int para, 
			OnProgressCallBack onProgress) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			if (!SendPackage(0x20,0x00,"DEVICE??".getBytes(),0,8,5))
				throw new Exception("Test Failed");
			
			int nBytesWritten = 0;
			onProgress.OnProgress(nBytesWritten, data.length);
			while (nBytesWritten < data.length) {
				int nPackageSize = Math.min(256, data.length - nBytesWritten);
				if (SendPackage(cmd, para + nBytesWritten, data, nBytesWritten, nPackageSize, 5)) {
					nBytesWritten += nPackageSize;
					onProgress.OnProgress(nBytesWritten, data.length);
				} else {
					throw new Exception("SendPackage Failed");
				}
			}
			result = nBytesWritten == data.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}
	
	public boolean UpdateProgram(byte[] data, 
			OnProgressCallBack onProgress)
	{
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			if (!SendPackage(0x20,0x00,"DEVICE??".getBytes(),0,8,5))
				throw new Exception("Test Failed");
			
			if (!SendPackage(0x2F,0x00,null,0,0,5))
				throw new Exception("Update Cmd 0x2F Failed");
			
			result = Download(data, 0x2E, 0x00, onProgress);
			
			SendPackage(0x2F,0xFFFFFFFF,null,0,0,1);
			
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}
	
	public boolean Downloads(List<byte[]> datas, int[] cmds, int[] paras, OnProgressCallBack onProgress)
	{
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			for (int i = 0; i < datas.size(); ++i) {
				result = Download(datas.get(i), cmds[i], paras[i], onProgress);
				if (result)
					continue;
				else
					break;
			}
			
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}
}
