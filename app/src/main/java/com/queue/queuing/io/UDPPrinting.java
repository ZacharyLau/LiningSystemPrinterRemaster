package com.queue.queuing.io;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

public class UDPPrinting extends IO{

	private static final String TAG = "UDPPrinting";
	
	private DatagramSocket mmClientSocket = null;
	private String IPAddress;
	private int PortNumber;
	
	private AtomicBoolean isOpened = new AtomicBoolean(false);
	
	private Vector<Byte> rxBuffer = new Vector<Byte>();
	
	public boolean Open(String IPAddress, int PortNumber)
	{
		Lock();
		
		try 
		{
			mmClientSocket = new DatagramSocket();
			mmClientSocket.setSoTimeout(5000);
			this.IPAddress = IPAddress;
			this.PortNumber = PortNumber;
			isOpened.set(true);
		}
		catch (Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return isOpened.get();
	}
	
	public boolean Open(String local_address, int local_port, String dest_address, int dest_port)
	{
		Lock();
		
		try 
		{
			mmClientSocket = new DatagramSocket(dest_port, InetAddress.getByName(local_address));
			IPAddress = dest_address;
			PortNumber = dest_port;
			isOpened.set(true);
		}
		catch (Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return isOpened.get();
	}
	
	public boolean SetReuseAddress(boolean reuse)
	{
		try 
		{
			mmClientSocket.setReuseAddress(reuse);
			return true;
		}
		catch (Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		return false;
	}
	
	public boolean SetBroadcast(boolean broadcast)
	{
		try 
		{
			mmClientSocket.setBroadcast(broadcast);
			return true;
		}
		catch (Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		return false;
	}

	/***
	 * 关闭连接
	 */
	public void Close() 
	{
		try
		{
			mmClientSocket.close();
		}
		catch(Exception ex)
		{
			Log.i(TAG, ex.toString());
		}
		finally
		{
			
		}
		isOpened.set(false);
	}

	/***
	 * 写数据
	 */
	@Override
	public int Write(byte[] buffer, int offset, int count) 
	{
		if(!isOpened.get())
			return -1;
		
		Lock();
		
		int nBytesWritten = 0;
		try
		{
			byte cmd[] = new byte[count];
			System.arraycopy(buffer, offset, cmd, 0, count);
			mmClientSocket.send(new DatagramPacket(cmd, cmd.length, InetAddress.getByName(IPAddress), PortNumber));
			nBytesWritten = count;
		}
		catch(Exception ex)
		{
			nBytesWritten = -1;
			Log.i(TAG, ex.toString());
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
		if(!isOpened.get())
			return -1;
		
		Lock();
		
		int nBytesReaded = 0;
		
		try
		{
			long time = System.currentTimeMillis();

			while ((System.currentTimeMillis() - time) < timeout)
			{
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
					byte data[] = new byte[1024];
					DatagramPacket recvPacket = new DatagramPacket(data, data.length);
					mmClientSocket.setSoTimeout(timeout);
					mmClientSocket.receive(recvPacket);
					int nReceived = recvPacket.getLength();
					if(nReceived > 0)
		            {
						String str = "Recv: ";
			            for(int i = 0; i < nReceived; ++i){
							rxBuffer.add(data[i]);
							
							str += String.format(Locale.CHINA, "%02X ", data[i] & 0xFFL);
			            }
			            Log.i(TAG, str);
		            }
				}
			}
		}
		catch(Exception ex)
		{
			nBytesReaded = -1;
			Log.e(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return nBytesReaded;
	}
	
	public int RecvDirect(byte[] buffer, int offset, int count, int timeout)
	{
		if(!isOpened.get())
			return -1;
		
		Lock();
		
		int nBytesReaded = 0;
		
		try
		{
			byte data[] = new byte[count];
			DatagramPacket recvPacket = new DatagramPacket(data, data.length);
			mmClientSocket.setSoTimeout(timeout);
			mmClientSocket.receive(recvPacket);
			nBytesReaded = recvPacket.getLength();
			if(nBytesReaded > 0) {
				System.arraycopy(data, 0, buffer, offset, nBytesReaded);
				/*
				String str = "Recv: ";
	            for(int i = 0; i < nBytesReaded; ++i){
	            	str += String.format(Locale.CHINA, "%02X ", data[i] & 0xFFL);
	            }
	            Log.i(TAG, str);
	            */
            }
		}
		catch(Exception ex)
		{
			Log.e(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return nBytesReaded;
	}
	
	public DatagramPacket RecvPacketDirect(int max_count, int timeout)
	{
		if(!isOpened.get())
			return null;
		
		Lock();
		
		byte data[] = new byte[max_count];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		
		try
		{
			mmClientSocket.setSoTimeout(timeout);
			mmClientSocket.receive(packet);
		}
		catch(Exception ex)
		{
			Log.e(TAG, ex.toString());
		}
		finally
		{
			Unlock();
		}
		
		return packet;
	}
	
	/***
	 * 忽略缓冲区中的数据
	 */
	@Override
	public void SkipAvailable()
	{
		Lock();
		
		rxBuffer.clear();
		
		Unlock();
	}	
	
	/***
	 * 是否已连接
	 */
	public boolean IsOpened() 
	{
		return isOpened.get();
	}
	
}
