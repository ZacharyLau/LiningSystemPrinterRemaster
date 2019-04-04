package com.queue.queuing.qrcode;

import com.queue.queuing.qrcode.Mode;

/**
 * QRData
 * @author Kazuhiko Arase 
 */
abstract class QRData {
	
	private final int mode;
	
	private final String data;

	protected QRData(int mode, String data) {
		this.mode = mode;
		this.data = data;
	}
	
	public int getMode() {
		return mode;
	}
	
	public String getData() {
		return data;
	}
	
	public abstract int getLength();
	
	public abstract void write(BitBuffer buffer);


	/**
	 * 型番及びモードに対するビット長を取得する。
	 */	
	public int getLengthInBits(int type) {

		if (1 <= type && type < 10) {

			// 1 - 9

			switch(mode) {
			case com.queue.queuing.qrcode.Mode.MODE_NUMBER 		: return 10;
			case com.queue.queuing.qrcode.Mode.MODE_ALPHA_NUM 	: return 9;
			case com.queue.queuing.qrcode.Mode.MODE_8BIT_BYTE	: return 8;
			case com.queue.queuing.qrcode.Mode.MODE_KANJI  		: return 8;
			default :
				throw new IllegalArgumentException("mode:" + mode);
			}

		} else if (type < 27) {

			// 10 - 26

			switch(mode) {
			case com.queue.queuing.qrcode.Mode.MODE_NUMBER 		: return 12;
			case com.queue.queuing.qrcode.Mode.MODE_ALPHA_NUM 	: return 11;
			case com.queue.queuing.qrcode.Mode.MODE_8BIT_BYTE	: return 16;
			case com.queue.queuing.qrcode.Mode.MODE_KANJI  		: return 10;
			default :
				throw new IllegalArgumentException("mode:" + mode);
			}

		} else if (type < 41) {

			// 27 - 40

			switch(mode) {
			case com.queue.queuing.qrcode.Mode.MODE_NUMBER 		: return 14;
			case com.queue.queuing.qrcode.Mode.MODE_ALPHA_NUM	: return 13;
			case com.queue.queuing.qrcode.Mode.MODE_8BIT_BYTE	: return 16;
			case Mode.MODE_KANJI  		: return 12;
			default :
				throw new IllegalArgumentException("mode:" + mode);
			}

		} else {
			throw new IllegalArgumentException("type:" + type);
		}
	}

}
