package com.queue.queuing.qrcode;

import com.queue.queuing.qrcode.QRMath;

/**
 * Polynomial
 * @author Kazuhiko Arase 
 */
class Polynomial {
	
	private final int[] num;

	public Polynomial(int[] num) {
		this(num, 0);
	}

	public Polynomial(int[] num, int shift) {

		int offset = 0;

		while (offset < num.length && num[offset] == 0) {
			offset++;
		}

		this.num = new int[num.length - offset + shift];
		System.arraycopy(num, offset, this.num, 0, num.length - offset);
	}

	public int get(int index) {
		return num[index];
	}

	public int getLength() {
		return num.length;
	}

	public String toString() {

		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < getLength(); i++) {
			if (i > 0) {
				buffer.append(",");
			}
			buffer.append(get(i) );
		}

		return buffer.toString();
	}
	
	public String toLogString() {

		StringBuilder buffer = new StringBuilder();

		for (int i = 0; i < getLength(); i++) {
			if (i > 0) {
				buffer.append(",");
			}
			buffer.append(com.queue.queuing.qrcode.QRMath.glog(get(i) ) );

		}

		return buffer.toString();
	}
	
	public Polynomial multiply(Polynomial e) {

		int[] num = new int[getLength() + e.getLength() - 1];

		for (int i = 0; i < getLength(); i++) {
			for (int j = 0; j < e.getLength(); j++) {
				num[i + j] ^= com.queue.queuing.qrcode.QRMath.gexp(com.queue.queuing.qrcode.QRMath.glog(get(i) ) + com.queue.queuing.qrcode.QRMath.glog(e.get(j) ) );
			}
		}

		return new Polynomial(num);
	}
	
	public Polynomial mod(Polynomial e) {

		if (getLength() - e.getLength() < 0) {
			return this;
		}

		// 最上位桁の比率
		int ratio = com.queue.queuing.qrcode.QRMath.glog(get(0) ) - com.queue.queuing.qrcode.QRMath.glog(e.get(0) );

		// コピー作成
		int[] num = new int[getLength()];
		for (int i = 0; i < getLength(); i++) {
			num[i] = get(i);
		}
		
		// 引き算して余りを計算
		for (int i = 0; i < e.getLength(); i++) {
			num[i] ^= com.queue.queuing.qrcode.QRMath.gexp(com.queue.queuing.qrcode.QRMath.glog(e.get(i) ) + ratio);
		}

		// 再帰計算
		return new Polynomial(num).mod(e);
	}
}
