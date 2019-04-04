package com.queue.queuing.io;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.Random;
import java.util.UUID;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 
 * 蓝牙4.0底层读写封装 For Android 4.3 (API Level 18)
 * 
 * @author 彭大帅
 * 
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BLEPrinting extends IO {

	private static final String TAG = "BLEPrinting";
	private static final UUID SERV_UUID = UUID
			.fromString("e7810a71-73ae-499d-8c15-faa9aef0c3f2");
	private static final UUID CHAR_UUID = UUID
			.fromString("bef8d6c9-9c21-4c9e-b632-bd58c1009f9f");

	private BluetoothGatt g = null;
	private BluetoothGattCharacteristic c;

	private static final int WP_WRITTING = 1;
	private static final int WP_WRITEOK = 2;
	private static final int WP_WRITEERR = 3;
	private AtomicInteger nWriteProcess = new AtomicInteger(WP_WRITEOK);

	private static final int OP_CONNECTING = 1;
	private static final int OP_DISCOVERING = 2;
	private static final int OP_FINISHED = 4;
	private AtomicInteger nOpenProcess = new AtomicInteger(OP_FINISHED);

	private AtomicBoolean isOpened = new AtomicBoolean(false);
	private AtomicBoolean isReadyRW = new AtomicBoolean(false);
	private AtomicBoolean isConnected = new AtomicBoolean(false);
	private IOCallBack cb = null;
	private Vector<Byte> rxBuffer = new Vector<Byte>();

	private AtomicLong nIdleTime = new AtomicLong(0);

	private final ReentrantLock mCloseLocker = new ReentrantLock();

	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {

			Log.i(TAG, "onConnectionStateChange " + " status:" + status
					+ " newState:" + newState);

			if (newState == BluetoothProfile.STATE_CONNECTED) {
				isConnected.set(true);

				if (nOpenProcess.get() == OP_CONNECTING) {
					nOpenProcess.set(OP_DISCOVERING);
					if (!gatt.discoverServices()) {
						nOpenProcess.set(OP_FINISHED);
					}
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				isConnected.set(false);

				if (nOpenProcess.get() != OP_FINISHED) {
					nOpenProcess.set(OP_FINISHED);
				}

				// 有可能在没有完全Open结束的时候，就Disconnect了。
				if (isOpened.get()) {
					Close();
				}
			}
		}

		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			Log.i(TAG, "onServicesDiscovered " + " status:" + status);

			if (nOpenProcess.get() == OP_DISCOVERING) {
				BluetoothGattService bgs = gatt.getService(SERV_UUID);
				if (null != bgs) {
					BluetoothGattCharacteristic bgc = bgs
							.getCharacteristic(CHAR_UUID);
					if (null != bgc) {
						bgc.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
						//bgc.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
						if (gatt.setCharacteristicNotification(bgc, true)) {
							g = gatt;
							c = bgc;
						}
					}
				}

				nOpenProcess.set(OP_FINISHED);
			}
		}

		public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
			// Log.i(TAG, "onCharacteristicRead " + " status:" + status);
			// Log.i(TAG, ByteUtils.bytesToStr(characteristic.getValue()));
		}

		public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
			Log.i(TAG, "onCharacteristicWrite " + " status:" + status);
			Log.i(TAG, ByteUtils.bytesToStr(characteristic.getValue()));
			if (status == BluetoothGatt.GATT_SUCCESS)
				nWriteProcess.set(WP_WRITEOK);
			else
				nWriteProcess.set(WP_WRITEERR);
		}

		public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {

			byte[] buffer = characteristic.getValue();
			int bytesRead = buffer.length;
			String s = "Recv " + bytesRead + " Bytes: ";
			for (int i = 0; i < bytesRead; ++i) {
				rxBuffer.add(buffer[i]);
				s += String.format("%02X ", buffer[i]);
			}
			Log.i(TAG, s);
		}

		public void onDescriptorRead(BluetoothGatt gatt,
                                     BluetoothGattDescriptor descriptor, int status) {
			Log.i(TAG, "onDescriptorRead " + " status:" + status);
		}

		public void onDescriptorWrite(BluetoothGatt gatt,
                                      BluetoothGattDescriptor descriptor, int status) {
			Log.i(TAG, "onDescriptorWrite " + " status:" + status);
		}

		public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
			Log.i(TAG, "onReliableWriteCompleted " + " status:" + status);
		}

		public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
			Log.i(TAG, "onReadRemoteRssi " + " rssi:" + rssi + " status:"
					+ status);
		}
	};

	private Context context;
	
	/***
	 * 连接4.0蓝牙打印机
	 * 
	 * @param BTAddress
	 *            打印机蓝牙MAC地址
	 * @param context
	 *            Context
	 * @return
	 */
	public boolean Open(String BTAddress, Context mContext) {

		Lock();

		try {
			if (isOpened.get())
				throw new Exception("Already open");

			if (null == mContext)
				throw new Exception("Null Pointer mContext");
			context = mContext;
			
			isReadyRW.set(false);

			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();
			if (bluetoothAdapter == null)
				throw new Exception("Null BluetoothAdapter");

			bluetoothAdapter.cancelDiscovery();

			BluetoothDevice device = bluetoothAdapter
					.getRemoteDevice(BTAddress);

			long timeout = 10000;
			long time = System.currentTimeMillis();
			while ((System.currentTimeMillis() - time) < timeout) {
				nOpenProcess.set(OP_CONNECTING);
				BluetoothGatt bluetoothGatt = device.connectGatt(null, false,
						mGattCallback);
				while (true) {
					if (nOpenProcess.get() == OP_FINISHED)
						break;
				}

				if ((g != null) && (c != null)) {
					// 连接成功
					isReadyRW.set(true);
					break;
				} else {
					bluetoothGatt.close();
				}
			}

			if (isReadyRW.get()) {
				Log.v(TAG, "Connected to " + BTAddress);
				rxBuffer.clear();
			}

			isOpened.set(isReadyRW.get());

			if (null != cb) {
				if (isOpened.get()) {
					cb.OnOpen();
				} else {
					cb.OnOpenFailed();
				}
			}
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			Unlock();
		}

		return isOpened.get();
	}

	/***
	 * 关闭连接
	 */
	public void Close() {
		mCloseLocker.lock();

		try {
			try {
				if (null != g) {
					g.close();
				}
			} catch (Exception ex) {
				Log.i(TAG, ex.toString());
			} finally {
				isConnected.set(false);
			}

			if (!isReadyRW.get())
				throw new Exception();

			g = null;
			c = null;

			isReadyRW.set(false);

			if (!isOpened.get())
				throw new Exception();

			isOpened.set(false);

			if (null != cb) {
				cb.OnClose();
			}
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			mCloseLocker.unlock();
		}
	}

	/***
	 * 
	 * @param pack
	 *            数据包
	 * @param timeout
	 *            超时
	 * @return
	 */
	public int WritePack(byte[] pack, int timeout) {
		Lock();

		int nBytesWritten = 0;

		try {
			int count = pack.length;
			if (!c.setValue(pack))
				throw new Exception("c.setValue Failed");

			long time = System.currentTimeMillis();
			while (System.currentTimeMillis() - time < timeout) {
				if (!isConnected.get())
					throw new Exception("Not Connected");

				if (!isReadyRW.get())
					throw new Exception("Not Ready For Read Write");

				nWriteProcess.set(WP_WRITTING);
				if (g.writeCharacteristic(c)) {

					while (true) {
						if (!isConnected.get())
							throw new Exception("Not Connected");

						if (!isReadyRW.get())
							throw new Exception("Not Ready For Read Write");

						if (nWriteProcess.get() != WP_WRITTING)
							break;
					}

					if (nWriteProcess.get() == WP_WRITEOK) {
						nBytesWritten = count;
						break;
					} else {
						WaitMs(20);
					}

				} else {
					WaitMs(20);
				}
			}
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
			Close();

			nBytesWritten = -1;
		} finally {
			Unlock();
		}

		return nBytesWritten;
	}

	/***
	 * 发送数据包，不带流控
	 * 
	 * @param buffer
	 * @param offset
	 * @param count
	 * @return
	 */
	public int Write(byte[] buffer, int offset, int count) {
		if (!isReadyRW.get())
			return -1;

		Lock();

		int nBytesWritten = 0;

		try {
			nIdleTime.set(0);

			while (nBytesWritten < count) {
				int nPacketSize = Math.min(20, count - nBytesWritten);
				byte[] data = new byte[nPacketSize];
				System.arraycopy(buffer, offset + nBytesWritten, data, 0,
						nPacketSize);
				int nSended = WritePack(data, 1000);
				if (nSended < 0)
					throw new Exception("Write Failed");
				else
					nBytesWritten += nSended;
			}

			nIdleTime.set(System.currentTimeMillis());
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
			Close();

			nBytesWritten = -1;
		} finally {
			Unlock();
		}

		return nBytesWritten;
	}

	/***
	 * 读数据
	 */
	@Override
	public int Read(byte[] buffer, int offset, int count, int timeout) {
		if (!isReadyRW.get())
			return -1;

		Lock();

		int nBytesReaded = 0;

		try {
			nIdleTime.set(0);

			long time = System.currentTimeMillis();

			while ((System.currentTimeMillis() - time) < timeout) {
				if (!isConnected.get())
					throw new Exception("Not Connected");

				if (!isReadyRW.get())
					throw new Exception("Not Ready For Read Write");

				if (nBytesReaded == count)
					break;

				if (rxBuffer.size() > 0) {
					buffer[offset + nBytesReaded] = rxBuffer.get(0);
					rxBuffer.remove(0);
					nBytesReaded += 1;
				}
			}

			nIdleTime.set(System.currentTimeMillis());
		} catch (Exception ex) {
			Log.e(TAG, ex.toString());
			Close();

			nBytesReaded = -1;
		} finally {
			Unlock();
		}

		return nBytesReaded;
	}

	/***
	 * 忽略缓冲区中的数据
	 */
	@Override
	public void SkipAvailable() {
		Lock();

		try {
			rxBuffer.clear();
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			Unlock();
		}
	}

	/***
	 * 是否已连接
	 */
	public boolean IsOpened() {
		return isOpened.get();
	}

	/***
	 * 设置IO回调接口
	 * 
	 * @param callBack
	 */
	public void SetCallBack(IOCallBack callBack) {
		Lock();

		try {
			cb = callBack;
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			Unlock();
		}
	}

	/***
	 * 等待MS
	 * 
	 * @param ms
	 */
	private void WaitMs(long ms) {
		long time = System.currentTimeMillis();
		while (System.currentTimeMillis() - time < ms)
			;
	}

	/***
	 * 检查打印机 -1 无返回 0 有返回，无加密 1 有返回，有加密
	 * 
	 * @return
	 */
	private int PTR_CheckEncrypt() {
		Lock();

		int result = -1;

		try {
			Random rmByte = new Random(System.currentTimeMillis());
			byte[] data = new byte[] { 0x1F, 0x28, 0x63, 0x08, 0x00, 0x1b,
					0x40, (byte) 0xd2, (byte) 0xd3, (byte) 0xd4, (byte) 0xd5,
					0x1b, 0x40, 0x00, 0x00, 0x00, 0x00, 0x1d, 0x72, 0x01 };
			for (int i = 0; i < 4; ++i) {
				data[7 + i] = (byte) rmByte.nextInt(0x9);
			}
			byte[] cmd = new byte[60 + data.length];
			System.arraycopy(data, 0, cmd, 60, data.length);
			SkipAvailable();
			if (Write(cmd, 0, cmd.length) == cmd.length) {
				byte[] rec = new byte[7];

				while (Read(rec, 0, 1, 3000) == 1) {
					result = 0;

					if (rec[0] == 0x63) {
						if (Read(rec, 1, 5, 3000) == 5) {
							if (rec[1] == 0x5F) {
								long v1 = (data[5] & 0x0FFl) << 24
										| (data[6] & 0x0FFl) << 16
										| (data[7] & 0x0FFl) << 8
										| (data[8] & 0x0FFl);
								long v2 = (data[9] & 0x0FFl) << 24
										| (data[10] & 0x0FFl) << 16
										| (data[11] & 0x0FFl) << 8
										| (data[12] & 0x0FFl);
								long vadd = (v1 + v2) & 0x0FFFFFFFFl;
								long vxor = (v1 ^ v2) & 0x0FFFFFFFFl;
								long l1 = v1 & 0xFFFFl;
								long h2 = (v2 >> 16) & 0x0FFFFl;

								v1 = (l1 * l1 - h2 * h2) & 0x0FFFFFFFFl;
								v1 = (vadd - vxor - v1) & 0x0FFFFFFFFl;

								v2 = (rec[2] & 0x0FFl) << 24
										| (rec[3] & 0x0FFl) << 16
										| (rec[4] & 0x0FFl) << 8
										| (rec[5] & 0x0FFl);
								if (v1 == v2) {
									result = 1;
								}
							}
						}
						break;
					} else if ((rec[0] & 0x90) == 0) {
						break;
					}
				}
			}
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			Unlock();
		}

		return result;
	}

	private boolean PTR_CheckKey() {
		Lock();

		boolean result = false;

		try {
			byte[] key = "XSH-KCEC".getBytes();
			byte[] random = new byte[8];
			Random rmByte = new Random(System.currentTimeMillis());
			for (int i = 0; i < 8; ++i) {
				random[i] = (byte) rmByte.nextInt(0x9);
			}
			final int HeaderSize = 5;
			byte[] recHeader = new byte[HeaderSize];
			byte[] recData = null;
			int rec = 0;
			int recDataLen = 0;
			byte[] randomlen = new byte[2];
			randomlen[0] = (byte) (random.length & 0xff);
			randomlen[1] = (byte) ((random.length >> 8) & 0xff);
			byte[] data = ByteUtils.byteArraysToBytes(new byte[][] {
					new byte[] { 0x1f, 0x1f, 0x02 }, randomlen, random,
					new byte[] { 0x1b, 0x40 } });
			SkipAvailable();
			Write(data, 0, data.length);
			rec = Read(recHeader, 0, HeaderSize, 1000);
			if (rec == HeaderSize) {
				recDataLen = (recHeader[3] & 0xff)
						+ ((recHeader[4] << 8) & 0xff);
				recData = new byte[recDataLen];
				rec = Read(recData, 0, recDataLen, 1000);
				if (rec == recDataLen) {

					byte[] encrypted = recData;
					byte[] decrypted = new byte[encrypted.length + 1];
					/**
					 * 对数据进行解密
					 */
					DES2 des2 = new DES2();
					// 初始化密钥
					des2.yxyDES2_InitializeKey(key);
					des2.yxyDES2_DecryptAnyLength(encrypted, decrypted,
							encrypted.length);
					result = ByteUtils.bytesEquals(random, 0, decrypted, 0,
							random.length);
				}
			}
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			Unlock();
		}

		return result;
	}

	private static int nCheckFaildTimes = 0;
	private static int nMaxCheckFailedTimes = 30;

	/***
	 * 检查打印机，如果不是本公司打印机，则打印提示内容
	 */
	private int PTR_CheckPrinter() {
		Lock();

		int check = -1;

		try {
			for (int i = 0; i < 3; ++i) {
				// 桌面打印机加密命令
				check = PTR_CheckEncrypt();
				if (check == -1)
					continue;
				else
					break;
			}

			// 如果有返回，但是不支持桌面打印机加密，此处测试便携打印机加密命令
			if (check == 0) {
				// 便携打印机加密命令
				if (PTR_CheckKey())
					check = 1;
			}

			if (check == 1) { // 如果检查成功，则归零
				nCheckFaildTimes = 0;
			} else if (check == 0) {
				// 如果有返回数据，但是加密失败，则将失败次数加1
				nCheckFaildTimes++;
			}

			if (nCheckFaildTimes >= nMaxCheckFailedTimes) {
				byte[] header = new byte[] { 0x0d, 0x0a, 0x1b, 0x40, 0x1c,
						0x26, 0x1b, 0x39, 0x01 };
				byte[] txt = "----Unknow printer----\r\n".getBytes();
				byte[] cmd = new byte[header.length + txt.length];
				int offset = 0;
				System.arraycopy(header, 0, cmd, offset, header.length);
				offset += header.length;
				System.arraycopy(txt, 0, cmd, offset, txt.length);
				offset += txt.length;
				Write(cmd, 0, cmd.length);
			}
		} catch (Exception ex) {
			Log.i(TAG, ex.toString());
		} finally {
			Unlock();
		}

		return check;
	}
}
