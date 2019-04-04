package com.queue.queuing.qrcode;

import java.io.UnsupportedEncodingException;

import com.queue.queuing.qrcode.BitBuffer;
import com.queue.queuing.qrcode.QRData;
import com.queue.queuing.qrcode.QRUtil;

/**
 * QR8BitByte
 * @author Kazuhiko Arase 
 */
class QR8BitByte extends com.queue.queuing.qrcode.QRData {
	
	public QR8BitByte(String data) {
		super(Mode.MODE_8BIT_BYTE, data);
	}
	
	public void write(com.queue.queuing.qrcode.BitBuffer buffer) {

		try {

			byte[] data = getData().getBytes(com.queue.queuing.qrcode.QRUtil.getUTF8Encoding());

			for (int i = 0; i < data.length; i++) {
				buffer.put(data[i], 8);
			}
			
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage() );
		}		
	}
	
	public int getLength() {
		try {
			return getData().getBytes(com.queue.queuing.qrcode.QRUtil.getUTF8Encoding()).length;
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage() );
		}
	}
	
}