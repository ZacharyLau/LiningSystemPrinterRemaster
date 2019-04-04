package com.queue.queuing.io;

public class UDPBroadcastProto {

	public static boolean configName(
			UDPPrinting udp,
			byte[] mac, byte[] name, 
			int timeout, int interval, int max_retry) {
		boolean ret = false;
		
		for (int retry = 0; retry < max_retry; ++retry) {
			if (!udp.IsOpened())
				break;
			
			byte[] cmd = new byte[148];
			cmd[0] = 0x45; cmd[1] = 0x50; cmd[2] = 0x53; cmd[3] = 0x4f; cmd[4] = 0x4e;
			cmd[5] = 0x53;
			cmd[6] = 0x03; cmd[13] = (byte) 0x86;
			
	        int offset = 14;
	        System.arraycopy(mac, 0, cmd, offset, 6); offset += 6;
	        System.arraycopy(name, 0, cmd, offset, Math.min(name.length, 127));
	        
	        if (udp.Write(cmd, 0, cmd.length) == cmd.length) {
	        	long time = System.currentTimeMillis();
	    		while ((System.currentTimeMillis() - time) < timeout) {
	    			if (!udp.IsOpened())
	    				break;
	    			
	    			byte data[] = new byte[1024];
	    			int nBytesReaded = udp.RecvDirect(data, 0, data.length, timeout);
					if (nBytesReaded == 148) {
						cmd[5] = 0x73;
						if (ByteUtils.bytesEquals(cmd, 0, data, 0, cmd.length)) {
							ret = true;
							break;
						}
					}
	    		}
	    		if (ret)
	    			break;
	        }
		}
		
		return ret;
	}
	
	public static boolean configIP(
			UDPPrinting udp,
			byte[] mac, boolean dhcp, byte[] ip, byte[] netmask, byte[] gateway, 
			int timeout, int interval, int max_retry) {
		boolean ret = false;
		
		for (int retry = 0; retry < max_retry; ++retry) {
			if (!udp.IsOpened())
				break;
			
			byte[] bFixedIP = { (byte) 0x80, 0x08 };
			byte[] bDhcpIP = { 0x00, 0x0c };
	        byte[] cmd = {
	            0x45, 0x50, 0x53, 0x4f, 0x4e,
	            0x53,
	            0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x15,
	            0x01,
	            0x64, (byte) 0xeb, (byte) 0x8c, 0x2a, 0x2a, (byte) 0xac,
	            0x00, 0x0c,
	            (byte) 0xc0, (byte) 0xa8, 0x0a, (byte) 0xd0,
	            (byte) 0xff, (byte) 0xff, (byte) 0xff, 0x00,
	            0x00, 0x00, 0x00, 0x00
	        };
	        int offset = 15;
	        System.arraycopy(mac, 0, cmd, offset, 6); offset += 6;
	        System.arraycopy(dhcp ? bDhcpIP : bFixedIP, 0, cmd, offset, 2); offset += 2;
	        System.arraycopy(ip, 0, cmd, offset, 4); offset += 4;
	        System.arraycopy(netmask, 0, cmd, offset, 4); offset += 4;
	        System.arraycopy(gateway, 0, cmd, offset, 4); offset += 4;
	        
	        if (udp.Write(cmd, 0, cmd.length) == cmd.length) {
	        	long time = System.currentTimeMillis();
	    		while ((System.currentTimeMillis() - time) < timeout) {
	    			if (!udp.IsOpened())
	    				break;
	    			
	    			byte data[] = new byte[1024];
	    			int nBytesReaded = udp.RecvDirect(data, 0, data.length, timeout);
					if (nBytesReaded == 0x25) {
						if ((data[0] == 'E') && 
								(data[1] == 'P') &&
								(data[2] == 'S') &&
								(data[3] == 'O') &&
								(data[4] == 'N') &&
								(data[5] == 's') &&
								(data[6] == 0) &&
								(data[7] == 0) &&
								(data[8] == 0) &&
								(data[9] == 0x10)) {
							ret = true;
							break;
						}
					}
	    		}
	    		if (ret)
	    			break;
	        }
		}
		
		return ret;
	}
	
	public static boolean confirmConfiguration(
			UDPPrinting udp,
			byte[] mac, 
			int timeout, int interval, int max_retry) {
		boolean ret = false;
		
		for (int retry = 0; retry < max_retry; ++retry) {
			if (!udp.IsOpened())
				break;
			
			byte[] cmd = {
		            0x45, 0x50, 0x53, 0x4f, 0x4e,
		            0x53,
		            0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x0b,
		            0x64, (byte) 0xeb, (byte) 0x8c, 0x2a, 0x2a, (byte) 0xac,
		            0x01, 0x00, 0x12, 0x00, 0x00
		        };
	        int offset = 14;
	        System.arraycopy(mac, 0, cmd, offset, 6); offset += 6;
	        
	        if (udp.Write(cmd, 0, cmd.length) == cmd.length) {
	        	long time = System.currentTimeMillis();
	    		while ((System.currentTimeMillis() - time) < timeout) {
	    			if (!udp.IsOpened())
	    				break;
	    			
	    			byte data[] = new byte[1024];
	    			int nBytesReaded = udp.RecvDirect(data, 0, data.length, timeout);
					if (nBytesReaded == 0x19) {
						if ((data[0] == 'E') && 
								(data[1] == 'P') &&
								(data[2] == 'S') &&
								(data[3] == 'O') &&
								(data[4] == 'N') &&
								(data[5] == 's') &&
								(data[6] == 0) &&
								(data[7] == 0) &&
								(data[8] == 0x1) &&
								(data[9] == 0x0)) {
							ret = true;
							break;
						}
					}
	    		}
	    		if (ret)
	    			break;
	        }
		}
		
		return ret;
	}
	
	
}
