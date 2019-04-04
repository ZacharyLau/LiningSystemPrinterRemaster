package com.queue.queuing.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {
	protected static char hexDigits[] = {'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};  
	protected static MessageDigest messageDigest = null;  
    static{  
        try{  
            messageDigest = MessageDigest.getInstance("MD5");  
        }catch (NoSuchAlgorithmException e) {  
            System.err.println(Md5Utils.class.getName()+"初始化失败，MessageDigest不支持MD5Util.");  
            e.printStackTrace();  
        }  
    }  
    
	public static String getMd5String(String str) {  
	       MessageDigest messageDigest = null;  
	       try {  
	            messageDigest = MessageDigest.getInstance("MD5");  
	            messageDigest.reset();  
	            messageDigest.update(str.getBytes("UTF-8"));  
	        } catch (NoSuchAlgorithmException e) {  
	            System.out.println("NoSuchAlgorithmException caught!");  
	            System.exit(-1);  
	        } catch (UnsupportedEncodingException e) {  
	            e.printStackTrace();  
	        }  
	        byte[] byteArray = messageDigest.digest();  
	        StringBuffer md5StrBuff = new StringBuffer();  
	        for (int i = 0; i < byteArray.length; i++) {              
	            if (Integer.toHexString(0xFF & byteArray[i]).length() == 1)  
	                md5StrBuff.append("0").append(Integer.toHexString(0xFF & byteArray[i]));  
	            else  
	                md5StrBuff.append(Integer.toHexString(0xFF & byteArray[i]));  
	        }  
	        return md5StrBuff.toString();  
	    }  
	

	/**
     * 计算文件的MD5
     * @param fileName 文件的绝对路径
     * @return
     * @throws IOException
     */  
    public static String getFileMD5String(String fileName) throws IOException{  
        File f = new File(fileName);  
        return getFileMD5String(f);  
    }  
      
    /**
     * 计算文件的MD5，重载方法
     * @param file 文件对象
     * @return
     * @throws IOException
     */  
    public static String getFileMD5String(File file) throws IOException{  
        FileInputStream in = new FileInputStream(file);  
        FileChannel ch = in.getChannel();  
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());  
        messageDigest.update(byteBuffer);  
        return bufferToHex(messageDigest.digest());  
    }  
      
    private static String bufferToHex(byte bytes[]) {  
       return bufferToHex(bytes, 0, bytes.length);  
    }  
      
    private static String bufferToHex(byte bytes[], int m, int n) {  
       StringBuffer stringbuffer = new StringBuffer(2 * n);  
       int k = m + n;  
       for (int l = m; l < k; l++) {  
        appendHexPair(bytes[l], stringbuffer);  
       }  
       return stringbuffer.toString();  
    }  
      
    private static void appendHexPair(byte bt, StringBuffer stringbuffer) {  
       char c0 = hexDigits[(bt & 0xf0) >> 4];  
       char c1 = hexDigits[bt & 0xf];  
       stringbuffer.append(c0);  
       stringbuffer.append(c1);  
    }  
}
