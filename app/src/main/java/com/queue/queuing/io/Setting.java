package com.queue.queuing.io;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * PrinterSettingTool的封装
 * 
 * @author 彭大帅
 * 
 */
public class Setting {

	private static final String TAG = "Setting";

	private IO IO = new IO();

	public void Set(IO io) {
		if (null != io) {
			IO = io;
		}
	}

	public IO GetIO() {
		return IO;
	}

	public byte[] WrapUSSICmd(byte p0, byte p1, byte[] src_cmd)
	{
		int src_cmd_length = src_cmd.length;
		int datalen = src_cmd_length + 1;
		int totallen = 5 + datalen;
		byte[] dst_cmd = new byte[totallen];
		dst_cmd[0] = 0x1F;
		dst_cmd[1] = 0x28;
		dst_cmd[2] = 0x0F;
		dst_cmd[3] = (byte) (datalen & 0xFFL);
		dst_cmd[4] = (byte) ((datalen & 0xFF00L) >> 8);
		System.arraycopy(src_cmd, 0, dst_cmd, 5, src_cmd_length);
		dst_cmd[totallen - 1] = 0;
		for (int i = 5; i < totallen - 1; ++i)
			dst_cmd[totallen - 1] ^= dst_cmd[i];
		
		return dst_cmd;
	}
	
	public boolean Setting_Ethernet_IPAddress(String ipAddress) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] ipbytes = IsIPValid(ipAddress);
			if (null == ipbytes)
				throw new Exception("Invalid ip address: " + ipAddress);

			byte[] cmd = { 0x1f, 0x69, 0x00, 0x00, 0x00, 0x00 };
			System.arraycopy(ipbytes, 0, cmd, 2, 4);
			
			cmd = WrapUSSICmd((byte)0x1f, (byte)'s', cmd);
			
			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Ethernet_MACAddress(String macAddress) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] macbytes = IsMACValid(macAddress);
			if (null == macbytes)
				throw new Exception("Invalid mac address: " + macAddress);

			byte[] cmd = { 0x1f, 0x6d, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			System.arraycopy(macbytes, 0, cmd, 2, 6);
			
			cmd = WrapUSSICmd((byte)0x1f, (byte)'s', cmd);

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Ethernet_Speed(int index) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] cmdAutoNegotiation = { 0x1F, 0x70, 0x00, 0x00, (byte) 0x84 };
			byte[] cmd100MFullDuplex = { 0x1F, 0x70, 0x01, 0x01, 0x01 };
			byte[] cmd100MHalfDuplex = { 0x1F, 0x70, 0x01, 0x01, 0x00 };
			byte[] cmd10MFullDuplex = { 0x1F, 0x70, 0x01, 0x00, 0x01 };
			byte[] cmd10MHalfDuplex = { 0x1F, 0x70, 0x01, 0x00, 0x00 };
			byte[][] cmdSpeeds = { cmdAutoNegotiation, cmd100MFullDuplex,
					cmd100MHalfDuplex, cmd10MFullDuplex, cmd10MHalfDuplex };

			if (index < 0 || index > 4)
				throw new Exception("Invalid Parameter index:" + index);

			byte[] cmd = cmdSpeeds[index];
			
			cmd = WrapUSSICmd((byte)0x1f, (byte)'s', cmd);

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Basic_Common(int language, int combaudrate,
			int comparity, int fonttype, int density, int charsperline,
			boolean autoreprintlastreceipt, boolean beeper, boolean drawer,
			boolean cutter) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] cmd1 = { 0x1F, 0x73, 0x02, 0x00, 0x00, 0x00, 0x00, 0x00,
					(byte) 0xFF, 0x00, 0x00, };

			cmd1[2] = (byte) (combaudrate);
			cmd1[3] = (byte) (cutter ? 0 : 1);
			cmd1[4] = (byte) (beeper ? 0 : 1);
			cmd1[5] = (byte) (drawer ? 0 : 1);
			cmd1[6] = (byte) (charsperline);
			cmd1[7] = (byte) (density);
			cmd1[8] = (byte) (language);
			cmd1[9] = (byte) (comparity);
			cmd1[10] = (byte) (fonttype);

			byte[] cmd2 = { 0x1F, 0x72, 0x00 };
			cmd2[2] = (byte) (autoreprintlastreceipt ? 1 : 0);
			
			byte[] cmd = ByteUtils.byteArraysToBytes(new byte[][] {WrapUSSICmd((byte)0x1f, (byte)'s', cmd1), WrapUSSICmd((byte)0x1f, (byte)'s', cmd2)});

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Wifi_SsidAndPassword(String ssid, String password) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] ssidBytes = ssid.getBytes();
			byte[] ssidHead = { 0x1f, 0x77, (byte) (ssidBytes.length) };

			byte[] passwordBytes = password.getBytes();
			byte[] passwordHead = { 0x00, 0x00, 0x00 };
			if (passwordBytes.length > 0) {
				passwordHead[0] = 3;
				passwordHead[1] = 1;
				passwordHead[2] = (byte) (passwordBytes.length);
			}

			int len = ssidHead.length + ssidBytes.length + passwordHead.length
					+ passwordBytes.length;
			byte[] cmd = new byte[len];

			int offset = 0;
			System.arraycopy(ssidHead, 0, cmd, offset, ssidHead.length);
			offset += ssidHead.length;
			System.arraycopy(ssidBytes, 0, cmd, offset, ssidBytes.length);
			offset += ssidBytes.length;
			System.arraycopy(passwordHead, 0, cmd, offset, passwordHead.length);
			offset += passwordHead.length;
			System.arraycopy(passwordBytes, 0, cmd, offset,
					passwordBytes.length);
			offset += passwordBytes.length;

			cmd = WrapUSSICmd((byte)0x1f, (byte)'s', cmd);
			
			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Wifi_IPAddress(String ipAddress) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] ipbytes = IsIPValid(ipAddress);
			if (null == ipbytes)
				throw new Exception("Invalid ip address: " + ipAddress);

			byte[] cmd = { 0x1f, 0x57, 0x49, 0x00, 0x00, 0x00, 0x00 };
			System.arraycopy(ipbytes, 0, cmd, 3, 4);

			cmd = WrapUSSICmd((byte)0x1f, (byte)'s', cmd);
			
			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}
	
	public boolean Setting_Bluetooth_NameAndPassword(String name,
                                                     String password) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] cmdHead = { 0x1f, 0x42 };
			byte[] nameBytes = name.getBytes();
			byte[] passwordBytes = password.getBytes();

			int len = 2 + nameBytes.length + 1 + passwordBytes.length + 1;
			byte[] cmd = new byte[len];

			int offset = 0;
			System.arraycopy(cmdHead, 0, cmd, offset, cmdHead.length);
			offset += cmdHead.length;
			System.arraycopy(nameBytes, 0, cmd, offset, nameBytes.length);
			offset += nameBytes.length + 1;
			System.arraycopy(passwordBytes, 0, cmd, offset,
					passwordBytes.length);
			offset += passwordBytes.length + 1;

			cmd = WrapUSSICmd((byte)0x1f, (byte)'s', cmd);
			
			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_BlackMark_Enable() {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {

			byte[] cmd = { 0x1F, 0x1B, 0x1F, (byte) 0x80, 0x04, 0x05, 0x06,
					0x44 };

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_BlackMark_Disable() {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {

			byte[] cmd = { 0x1F, 0x1B, 0x1F, (byte) 0x80, 0x04, 0x05, 0x06,
					0x66 };

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_BlackMark_Set(int length, int width, int feedcut,
			int feedprint) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			int n;

			byte[] cmdDistance = { 0x1F, 0x1B, 0x1F, (byte) 0x81, 0x04, 0x05,
					0x06, 0x09, 0x60 };
			n = length * 8;
			cmdDistance[8] = (byte) (n >> 8);
			cmdDistance[7] = (byte) (n);

			byte[] cmdWidth = { 0x1F, 0x1B, 0x1F, (byte) 0x82, 0x04, 0x05,
					0x06, 0x00, 0x50 };
			n = width * 8;
			cmdWidth[8] = (byte) (n >> 8);
			cmdWidth[7] = (byte) (n);

			byte[] cmdFeedCut = { 0x1D, 0x28, 0x46, 0x04, 0x00, 0x02, 0x00,
					0x00, 0x00 };
			n = feedcut * 8;
			cmdFeedCut[8] = (byte) (n >> 8);
			cmdFeedCut[7] = (byte) (n);

			byte[] cmdFeedPrint = { 0x1D, 0x28, 0x46, 0x04, 0x00, 0x01, 0x00,
					0x00, 0x00 };
			n = feedprint * 8;
			cmdFeedPrint[8] = (byte) (n >> 8);
			cmdFeedPrint[7] = (byte) (n);

			byte[] cmd = ByteUtils.byteArraysToBytes(new byte[][] {
					cmdDistance, cmdWidth, cmdFeedCut, cmdFeedPrint });

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
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

	public boolean Setting_Firmware_Update(byte[] firmware,
			OnProgressCallBack onProgress) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			long chksumlen = (firmware[7] & 0xFFL) << 24
					| (firmware[6] & 0xFFL) << 16 | (firmware[5] & 0xFFL) << 8
					| (firmware[4] & 0xFFL);
			if (chksumlen != firmware.length)
				throw new Exception("File Check Length Error");

			byte[] cmd = new byte[6 + firmware.length];
			cmd[0] = 0x1f;
			cmd[1] = 0x75;
			cmd[2] = firmware[7];
			cmd[3] = firmware[6];
			cmd[4] = firmware[5];
			cmd[5] = firmware[4];
			System.arraycopy(firmware, 0, cmd, 6, firmware.length);

			int nBytesWritten = 0;
			onProgress.OnProgress(nBytesWritten, cmd.length);
			while (nBytesWritten < cmd.length) {
				int nPackageSize = Math.min(256, cmd.length - nBytesWritten);

				int nSended = IO.Write(cmd, nBytesWritten, nPackageSize);
				if (nSended < 0) {
					throw new Exception("Write Failed");
				} else {
					nBytesWritten += nSended;
					onProgress.OnProgress(nBytesWritten, cmd.length);
				}
				Thread.sleep(10);
			}
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Logo_Download(List<String> files,
                                         OnProgressCallBack onProgress) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			List<byte[]> datas = new ArrayList<byte[]>();

			byte[] head = { 0x1c, 0x71, 0x00 };
			datas.add(head);

			for (int i = 0; i < files.size(); ++i) {
				Bitmap mBitmap = BitmapFactory.decodeFile(files.get(i));

				int dstw = mBitmap.getWidth();
				int dsth = mBitmap.getHeight();

				int[] dst = new int[dstw * dsth];
				mBitmap.getPixels(dst, 0, dstw, 0, 0, dstw, dsth);

				byte[] gray = ImageProcessing.GrayImage(dst);

				boolean[] dithered = new boolean[dstw * dsth];
				ImageProcessing.format_K_threshold(dstw, dsth, gray, dithered);

				byte[] nv = ImageProcessing
						.Image1ToNVData(dstw, dsth, dithered);
				datas.add(nv);
				head[2]++;
			}

			byte[] cmd = ByteUtils.ByteArrayListToBytes(datas);

			int nBytesWritten = 0;
			onProgress.OnProgress(nBytesWritten, cmd.length);
			while (nBytesWritten < cmd.length) {
				int nPackageSize = Math.min(256, cmd.length - nBytesWritten);

				int nSended = IO.Write(cmd, nBytesWritten, nPackageSize);
				if (nSended < 0) {
					throw new Exception("Write Failed");
				} else {
					nBytesWritten += nSended;
					onProgress.OnProgress(nBytesWritten, cmd.length);
				}
				Thread.sleep(10);
			}
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Logo_Print() {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {

			byte[] cmd = new byte[4 * 255];
			int idx = 0;
			for (int i = 1; i <= 255; i++) {
				cmd[idx++] = 0x1C;
				cmd[idx++] = 0x70;
				cmd[idx++] = (byte) i;
				cmd[idx++] = 0x00;
			}

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	public boolean Setting_Hardware_Set(
			boolean bSetBluetooth, int nSetBluetoothValue, 
			boolean bSetCutter, int nSetCutterValue,
			boolean bSetHeatDot, int nSetHeatDotValue, 
			boolean bSetHeatOnTime,	int nSetHeatOnTimeValue, 
			boolean bSetHeatOffTime, int nSetHeatOffTimeValue, 
			boolean bSetSensor, int nSetSensorValue,
			boolean bSetUsb, int nSetUsbValue,
			boolean bSetCutBeep, int nBeepCount, int nBeepOn, int nBeepOff, 
			boolean bSetSpeed, int nSetSpeedValue) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {

			byte[] cmd = new byte[26];
			cmd[0] = 0x1F;
			cmd[1] = 0x53;
			int maskbit = 0;
			boolean[] bmasklst = {bSetBluetooth,bSetCutter,bSetHeatDot,bSetHeatOnTime,bSetHeatOffTime,bSetSensor,bSetUsb,bSetCutBeep,bSetSpeed};
			for(int i = 0; i < bmasklst.length; ++i)
			{
				if(bmasklst[i])
				{
					maskbit |= (1 << i);
				}
			}
			cmd[2] = (byte)((maskbit >> 24) & 0xFFL);
			cmd[3] = (byte)((maskbit >> 16) & 0xFFL);
			cmd[4] = (byte)((maskbit >> 8) & 0xFFL);
			cmd[5] = (byte)((maskbit >> 0) & 0xFFL);
			cmd[6] = cmd[7] = cmd[8] = cmd[9] = 0;
			cmd[10] = (byte) (cmd.length - 12); //参数个数
			
			cmd[11] = (byte)(nSetBluetoothValue);
			cmd[12] = (byte)(nSetCutterValue);
			
			cmd[13] = (byte)(nSetHeatDotValue / 8);
			cmd[14] = (byte)((nSetHeatOnTimeValue >> 8) & 0xFFL);
			cmd[15] = (byte)((nSetHeatOnTimeValue >> 0) & 0xFFL);
			cmd[16] = (byte)((nSetHeatOffTimeValue >> 8) & 0xFFL);
			cmd[17] = (byte)((nSetHeatOffTimeValue >> 0) & 0xFFL);
			
			cmd[18] = (byte)(nSetSensorValue);
			cmd[19] = (byte)(nSetUsbValue);
			
			long snd = ((((nBeepCount&0x7FL)*2) << 24) + ((nBeepOn & 0xFFFL) << 12) + (nBeepOff & 0xFFFL));
			cmd[20] = (byte)((snd >> 24) & 0xFFL);
			cmd[21] = (byte)((snd >> 16) & 0xFFL);
			cmd[22] = (byte)((snd >> 8) & 0xFFL);
			cmd[23] = (byte)((snd >> 0) & 0xFFL);
			
			cmd[24] = (byte)(nSetSpeedValue);
			
			cmd[cmd.length - 1] = 0;
			for(int i = 11; i <= cmd.length - 2; ++i)
				cmd[cmd.length - 1] ^= cmd[i];
			
			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}
	
	public boolean Setting_Mqtt_ServerIPPort(String szip, String szport) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			byte[] ipbytes = IsIPValid(szip);
			if (null == ipbytes)
				throw new Exception("Invalid ip address: " + szip);
			int port = Integer.parseInt(szport);
			
			byte[] cmd = { 0x1f, 0x28, 0x54, 0x06, 0x00, 
					0x00, 0x00, 0x00, 0x00, 
					(byte) ((port >> 8) & 0xFFL), (byte) (port & 0xFFL) };
			System.arraycopy(ipbytes, 0, cmd, 5, 4);

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}
	
	public boolean Setting_Mqtt_UserNamePassword(String user, String password) {
		if (!IO.IsOpened())
			return false;

		boolean result = false;

		IO.Lock();

		try {
			int data_len = user.length() + 1 + password.length();
			byte[] cmd = new byte[5 + data_len];
			int idx = 0;
			cmd[idx++] = 0x1f;
			cmd[idx++] = 0x28;
			cmd[idx++] = 0x51;
			cmd[idx++] = (byte) (data_len & 0xFFL);
			cmd[idx++] = (byte) ((data_len >> 8) & 0xFFL);
			byte[] user_bytes = user.getBytes();
			System.arraycopy(user_bytes, 0, cmd, idx, user_bytes.length);
			idx += user_bytes.length + 1;
			byte[] password_bytes = password.getBytes();
			System.arraycopy(password_bytes, 0, cmd, idx, password_bytes.length);
			idx += 1;

			int nBytesWritten = IO.Write(cmd, 0, cmd.length);
			result = nBytesWritten == cmd.length ? true : false;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			IO.Unlock();
		}

		return result;
	}

	private byte[] IsIPValid(String ip) {
		byte[] ipbytes = new byte[4];
		int valid = 0;
		int s, e;
		String ipstr = ip + ".";
		s = 0;
		for (e = 0; e < ipstr.length(); e++) {
			if ('.' == ipstr.charAt(e)) {
				if ((e - s > 3) || (e - s) <= 0) // 最长3个字符
					return null;

				int ipbyte = -1;
				try {
					ipbyte = Integer.parseInt(ipstr.substring(s, e));
					if (ipbyte < 0 || ipbyte > 255)
						return null;
					else
						ipbytes[valid] = (byte) ipbyte;
				} catch (NumberFormatException exce) {
					return null;
				}
				s = e + 1;
				valid++;
			}
		}
		if (valid == 4)
			return ipbytes;
		else
			return null;
	}

	private byte[] IsMACValid(String mac) {
		byte[] macbytes = new byte[6];
		int valid = 0;
		int s, e;
		String macstr = mac + ":";
		s = 0;
		for (e = 0; e < macstr.length(); e++) {
			if (':' == macstr.charAt(e)) {
				if (e - s != 2) // 最长3个字符
					return null;

				int ipbyte = -1;
				try {
					ipbyte = Integer.parseInt(macstr.substring(s, e), 16);
					if (ipbyte < 0 || ipbyte > 255)
						return null;
					else
						macbytes[valid] = (byte) ipbyte;
				} catch (NumberFormatException exce) {
					return null;
				}
				s = e + 1;
				valid++;
			}
		}
		if (valid == 6)
			return macbytes;
		else
			return null;
	}

}
