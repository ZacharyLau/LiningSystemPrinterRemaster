package com.queue.queuing.io;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

public class UDPDiscovery {

	private static final String TAG = "UDPDiscovery";
	
	private final ReentrantLock locker = new ReentrantLock();
	
	private UDPDiscoveryCallBack ndcb = null;
	
	public void SetNetworkDiscoveryCallBack(UDPDiscoveryCallBack callBack)
	{
		Lock();
		
		try
		{
			ndcb = callBack;
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
	
	public void DiscoveryIpMac(byte[] addr, int port, int timeout)
	{
		Lock();
		
		try
		{
			if (null != ndcb)
			{
				ndcb.onDiscoverStarted();
			}
			
			DatagramSocket mmClientSocket = null;
			try
			{
				mmClientSocket = new DatagramSocket(port, InetAddress.getByAddress(addr));
				mmClientSocket.setBroadcast(true);
				mmClientSocket.setReuseAddress(true);
				mmClientSocket.setSoTimeout(timeout);
				
				byte cmd[] = { 0x45, 0x50, 0x53, 0x4f, 0x4e, 0x50, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
				DatagramPacket sendPacket = new DatagramPacket(cmd, cmd.length, InetAddress.getByName("255.255.255.255"), 3289);
				mmClientSocket.send(sendPacket);
				
				long time = System.currentTimeMillis();
				while ((System.currentTimeMillis() - time) < timeout)
				{
					byte data[] = new byte[1024];
					DatagramPacket recvPacket = new DatagramPacket(data, data.length);
					mmClientSocket.receive(recvPacket);
					if(recvPacket.getLength() == 68)
					{
						byte[] ipBytes = recvPacket.getAddress().getAddress();
						String ip = String.format(Locale.CHINA, "%d.%d.%d.%d", (ipBytes[0] & 0xFFL), (ipBytes[1] & 0xFFL), (ipBytes[2] & 0xFFL), (ipBytes[3] & 0xFFL));
						String mac = String.format(Locale.CHINA, "%02X-%02X-%02X-%02X-%02X-%02X", data[54] & 0xFFL, data[55] & 0xFFL, data[56] & 0xFFL, data[57] & 0xFFL, data[58] & 0xFFL, data[59] & 0xFFL);
						Log.i(TAG, "Discovered" + " IP:" + ip + " MAC:" + mac);
						if (null != ndcb)
						{
							ndcb.onDiscoveredIpMac(mac, ip);
						}
					}
				}
			}
			catch(Exception ex)
			{
				Log.i(TAG, ex.toString());
				
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
				}
			}
			
			if (null != ndcb)
			{
				ndcb.onDiscoverFinished();
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
	}
	
	public void DiscoveryIpName(byte[] addr, int port, int timeout)
	{
		Lock();
		
		try
		{
			if (null != ndcb)
			{
				ndcb.onDiscoverStarted();
			}
			
			DatagramSocket mmClientSocket = null;
			try
			{
				mmClientSocket = new DatagramSocket(port, InetAddress.getByAddress(addr));
				mmClientSocket.setBroadcast(true);
				mmClientSocket.setReuseAddress(true);
				mmClientSocket.setSoTimeout(timeout);
				
				byte cmd[] = new byte[148];
				byte cmd_default[] = new byte[] { 
						0x45, 0x50, 0x53, 0x4f, 0x4e,
                        0x51,
                        0x03, 0x00, 0x00, 0x00,
                        0x00, 0x00, 0x00, (byte) 0x86,
                        0x64, (byte) 0xeb, (byte) 0x8c, 0x2a, 0x2c, (byte) 0xac,
                        0x00};
				System.arraycopy(cmd_default, 0, cmd, 0, cmd_default.length);
				DatagramPacket sendPacket = new DatagramPacket(cmd, cmd.length, InetAddress.getByName("255.255.255.255"), 3289);
				mmClientSocket.send(sendPacket);
				
				long time = System.currentTimeMillis();
				while ((System.currentTimeMillis() - time) < timeout)
				{
					byte data[] = new byte[1024];
					DatagramPacket recvPacket = new DatagramPacket(data, data.length);
					mmClientSocket.receive(recvPacket);
					if(recvPacket.getLength() == 147)
					{
						if ((data[0] == 'E') && 
								(data[1] == 'P') &&
								(data[2] == 'S') &&
								(data[3] == 'O') &&
								(data[4] == 'N') &&
								(data[5] == 'q')) {
							
							int namelen = 0;
							for (int i = 19; i < data.length; ++i) {
								if (data[i] == 0) {
									namelen = i - 19;
									break;
								}
							}
							String name = new String(data, 19, namelen);
							
							byte[] ipBytes = recvPacket.getAddress().getAddress();
							String ip = String.format(Locale.CHINA, "%d.%d.%d.%d", (ipBytes[0] & 0xFFL), (ipBytes[1] & 0xFFL), (ipBytes[2] & 0xFFL), (ipBytes[3] & 0xFFL));
							
							Log.i(TAG, "Discovered" + " IP:" + ip + " Name:" + name);
							
							if (null != ndcb)
							{
								ndcb.onDiscoveredIpName(ip, name);
							}
						}
					}
				}
			}
			catch(Exception ex)
			{
				Log.i(TAG, ex.toString());
				
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
				}
			}
			
			if (null != ndcb)
			{
				ndcb.onDiscoverFinished();
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
	}
	
	public void discoverPrinter(UDPPrinting udp, int timeout, int interval, int max_retry)
	{
		if (null != ndcb)
			ndcb.onDiscoverStarted();
		
		for (int retry = 0; retry < max_retry; ++retry) {
			if (!udp.IsOpened())
				break;
			
			byte cmd[] = { 0x45, 0x50, 0x53, 0x4f, 0x4e, 0x51, 0x03, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
			if (udp.Write(cmd, 0, cmd.length) == cmd.length) {
				long time = System.currentTimeMillis();
				while ((System.currentTimeMillis() - time) < timeout) {
					if (!udp.IsOpened())
						break;
					
					DatagramPacket packet = udp.RecvPacketDirect(1024, interval);
					if (packet != null) {
						byte[] data = packet.getData();
						int length = packet.getLength();
						if (length == 184) {
							if ((data[0] == 'E') && 
									(data[1] == 'P') &&
									(data[2] == 'S') &&
									(data[3] == 'O') &&
									(data[4] == 'N') &&
									(data[5] == 'q')) {
								
								int namelen = 0;
								for (int i = 56; i < data.length; ++i) {
									if (data[i] == 0) {
										namelen = i - 56;
										break;
									}
								}
								String name = new String(data, 56, namelen);
								
								byte[] ipBytes = packet.getAddress().getAddress();
								String ip = String.format(Locale.CHINA, "%d.%d.%d.%d", (ipBytes[0] & 0xFFL), (ipBytes[1] & 0xFFL), (ipBytes[2] & 0xFFL), (ipBytes[3] & 0xFFL));
								String mac = String.format(Locale.CHINA, "%02X-%02X-%02X-%02X-%02X-%02X", data[14] & 0xFFL, data[15] & 0xFFL, data[16] & 0xFFL, data[17] & 0xFFL, data[18] & 0xFFL, data[19] & 0xFFL);
								
								Log.i(TAG, "Discovered" + " MAC:" + mac + " IP:" + ip + " Name:" + name);
								
								if (null != ndcb)
								{
									ndcb.onDiscoveredMacIpName(mac, ip, name);
								}
							}
						}
					}
				}				
			}
		}
		
		if (null != ndcb)
		{
			ndcb.onDiscoverFinished();
		}
	}
	
	public static String discoverPrinterIPByName(String local_ip, int local_port, String dest_ip, int dest_port, String name, int timeout, int interval, int max_retry)
	{
		String ip = null;
		
		UDPPrinting udp = new UDPPrinting();
		if (udp.Open(local_ip, local_port, dest_ip, dest_port)) {
			udp.SetBroadcast(true);
			udp.SetReuseAddress(true);
			
			for (int retry = 0; retry < max_retry; ++retry) {
				if (!udp.IsOpened())
					break;
				
				byte cmd[] = { 0x45, 0x50, 0x53, 0x4f, 0x4e, 0x51, 0x03, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00 };
				if (udp.Write(cmd, 0, cmd.length) == cmd.length) {
					long time = System.currentTimeMillis();
					while ((System.currentTimeMillis() - time) < timeout) {
						if (!udp.IsOpened())
							break;
						
						DatagramPacket packet = udp.RecvPacketDirect(1024, interval);
						if (packet != null) {
							byte[] data = packet.getData();
							int length = packet.getLength();
							if (length == 184) {
								if ((data[0] == 'E') && 
										(data[1] == 'P') &&
										(data[2] == 'S') &&
										(data[3] == 'O') &&
										(data[4] == 'N') &&
										(data[5] == 'q')) {
									
									int namelen = 0;
									for (int i = 56; i < data.length; ++i) {
										if (data[i] == 0) {
											namelen = i - 56;
											break;
										}
									}
									if (name.equals(new String(data, 56, namelen))) {
										byte[] ipBytes = packet.getAddress().getAddress();
										ip = String.format(Locale.CHINA, "%d.%d.%d.%d", (ipBytes[0] & 0xFFL), (ipBytes[1] & 0xFFL), (ipBytes[2] & 0xFFL), (ipBytes[3] & 0xFFL));
										Log.i(TAG, "Discovered " + name + " " + ip);
										break;
									}
								}
							}
						}
					}
					if (ip != null)
						break;
				}
			}
			
			udp.Close();
		}
		
		return ip;
	}
	
	/***
	 * 锁定IO操作
	 */
	protected synchronized void Lock() {
		locker.lock();
	}
	
	/***
	 * 解锁IO操作
	 */
	protected synchronized void Unlock() {
		locker.unlock();
	}
}
