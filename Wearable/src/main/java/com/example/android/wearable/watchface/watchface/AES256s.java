package com.example.android.wearable.watchface.watchface; /**
 * 
 */


import android.util.Log;

import java.security.spec.AlgorithmParameterSpec;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * @author eksql
 *
 */
public class AES256s {
	public static final String ALGORITHM = "AES";
	public static final String TRANSFORMATION = "AES/CBC/NoPadding";
	public static final String CHARSET = "UTF-8";

	public static String getKey(String key) {
		int length = key.length();
		if(length > 32) {
			return key.substring(0, 32);
		}
		int mod = (length % 32);
		
		if(mod==0) {
			return key;
		}
		int size = 32 - mod;
		int index = 0;
		String nkey = key;
		while(index<size) {
			nkey += (index) % 10;
			index++;
		}
		return nkey;
	}
	public static String encrypt(String text, String duid) throws Exception {
		if (text == null || text.length() == 0) {
			return text;
		}
		String encrypted = null;
		byte[] source;
		try {
			String key = getKey(duid);
			source = text.getBytes(AES256s.CHARSET);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(AES256s.CHARSET), AES256s.ALGORITHM);
			
			Cipher cipher = Cipher.getInstance(AES256s.TRANSFORMATION);
			
			byte[] ivBytes = key.substring(0, 16).getBytes(AES256s.CHARSET);
			AlgorithmParameterSpec IVspec = new IvParameterSpec(ivBytes);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IVspec);
			int mod = source.length % 16;
			if (mod != 0) {
				text = String.format(text + "%" + (16 - mod) + "s", " ");
			}
			encrypted = byteArrayToHex(cipher.doFinal(text.getBytes(AES256s.CHARSET)));
		} catch (Exception e) {
			throw new Exception(e);
		}
		return encrypted;
	}
	public static String encrypt(byte[] text, String duid) throws Exception {
		if (text == null || text.length == 0) {
			throw new Exception("text is null");
		}
		String encrypted = null;
		byte[] source = getText(text);
		try {
			String key = getKey(duid);
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes(AES256s.CHARSET), AES256s.ALGORITHM);
			Cipher cipher = Cipher.getInstance(AES256s.TRANSFORMATION);
			
			byte[] ivBytes = key.substring(0, 16).getBytes(AES256s.CHARSET);
			AlgorithmParameterSpec IVspec = new IvParameterSpec(ivBytes);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, IVspec);
			encrypted = byteArrayToHex(cipher.doFinal(source));
		} catch (Exception e) {
			throw new Exception(e);
		}
		return encrypted;
	}

	/**
	 * @param text
	 * @return
	 */
	private static byte[] getText(byte[] text) {
		int len = text.length;
		int mod = len % 16;
		if(mod==0) {
			return text;
		}
		int size = len + (16 - mod);
		byte[] src = new byte[size];
		System.arraycopy(text, 0, src, 0, len);
		while(len<size) {
			src[len] = ' ';
		}
		return src;
	}
	public static String decryptToString(String s, String duid) throws Exception {
		if (s == null || s.length() == 0) {
			return s;
		}
		String decrypted = null;
		SecretKeySpec skeySpec;
		try {
			String key = getKey(duid);

			skeySpec = new SecretKeySpec(key.getBytes(AES256s.CHARSET), AES256s.ALGORITHM);
			Cipher cipher = Cipher.getInstance(AES256s.TRANSFORMATION);

			byte[] ivBytes = key.substring(0, 16).getBytes(AES256s.CHARSET);

			AlgorithmParameterSpec IVspec = new IvParameterSpec(ivBytes);

			cipher.init(Cipher.DECRYPT_MODE, skeySpec, IVspec);

			decrypted = new String(cipher.doFinal(hexToByteArray(s)), AES256s.CHARSET);
			return decrypted;
		} catch (Exception e) {
			throw new Exception(e);
		}
	}
	public static byte[] decrypt(String s, String duid) throws Exception {
		if (s == null || s.length() == 0) {
			return new byte[0];
		}
		byte[] decrypted = null;
		SecretKeySpec skeySpec;
		try {
			String key = getKey(duid);
			skeySpec = new SecretKeySpec(key.getBytes(AES256s.CHARSET), AES256s.ALGORITHM);
			Cipher cipher = Cipher.getInstance(AES256s.TRANSFORMATION);
			
			byte[] ivBytes = key.substring(0, 16).getBytes(AES256s.CHARSET);
			AlgorithmParameterSpec IVspec = new IvParameterSpec(ivBytes);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, IVspec);
			decrypted = cipher.doFinal(hexToByteArray(s));
			return decrypted;
		} catch (Exception e) {
			throw new Exception(e);
		}

	}
	private static byte[] hexToByteArray(String s){
		int len=s.length();
		byte[] data=new byte[len/2];
		for(int i=0; i<len; i+=2){
			data[i/2]=(byte)((Character.digit(s.charAt(i),16)<<4)+Character.digit(s.charAt(i+1),16));

		}
		return data;
	}
/**	private static byte[] hexToByteArray(String s) {
		byte[] retValue = null;
		if (s != null && s.length() != 0) {
			Log.d("AES",s);
			retValue = new byte[s.length() / 2];
			for (int i = 0; i < retValue.length; i++) {
				retValue[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
			}
		}
		return retValue;
	}**/
/**
	private static byte[] hexToByteArray(String s) {
		byte[] retValue = null;
		int temp=0;
		if (s != null && s.length() != 0) {
			for (int i = 0; i < s.length(); i++) {
				if(s.charAt(i)==':') {
					temp = i;
					break;
				}
			}
			Log.d("AES256",String.valueOf(temp));
			retValue = new byte[s.length() / 2];
			for (int i = (temp+3)/2; i < retValue.length-2; i++) {
				Log.d("AES256",String.valueOf(i));
				retValue[i-(temp+3)/2] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
			}
		}
		return retValue;
	}**/
/**
	private static byte[] hexToByteArray(String s) {
		byte[] retValue = null;
		int temp=0;
		if (s != null && s.length() != 0) {
			for (int i = 0; i < s.length(); i++) {
				if(s.charAt(i)==':') {
					temp = i;
					break;
				}
			}
			retValue = new byte[(s.length()-4-(temp*2))/ 2];
			Log.d("hi",String.valueOf(temp));
			Log.d("hi",String.valueOf(retValue.length));

			for (int i = temp+1; i < retValue.length; i++) {
				Log.d("hi",String.valueOf(i));
				retValue[i-temp-1] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);

				Log.d("hi",String.valueOf(retValue[i-temp-1]));
			}

		}
		return retValue;
	}
**/
	private static String byteArrayToHex(byte buf[]) {
		StringBuffer strbuf = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			strbuf.append(String.format("%02x", buf[i]));
		}

		return strbuf.toString();
	}

	public static void main(String[] args) throws Exception {
//		String duid = "08:97:98:0E:E6:DA";
		String duid = "OUM6NUE6NDQ6Qjc6Qjk6OEU";
		String str = "인코딩 필요";

		String key = getKey(duid);
		String aaa = encrypt(str, duid);
//		System.out.println(decrypt(encrypted, key));
	}
}
