package com.example.myapplication;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

public class AES256 {

//	private static final Logger logger = LoggerFactory.getLogger(AES256.class);

//	private final static String key = "0123456789ABCDEF0123456789ABCDEF"; //32 bytes = 256 bits
//	private final static String key = "VZ9s4UHPQmewahBWs0jXclR6SUQ=1234";
//	private final static String iv = "0123456789ABCDEF"; // 16 bytes
	private final static String AES = "AES/CBC/PKCS5Padding";

	/**
	 * @param key  256(32 bytes) or 128(16 bytes) ...
	 * @return
	 * @throws Exception
	 */
	public static SecretKeySpec getKey(String key) throws Exception {
		return new SecretKeySpec(key.getBytes("UTF-8"), "AES");
	}
	public static SecretKeySpec getKey(byte[] key) throws Exception {
		return new SecretKeySpec(key, "AES");
	}
	/**
	 * @param iv  16 bytes
	 * @return
	 * @throws Exception
	 */
	public static AlgorithmParameterSpec getIv(String iv) throws Exception {
		return new IvParameterSpec(iv.getBytes("UTF-8"));
	}
	public static AlgorithmParameterSpec getIv(byte[] iv) throws Exception {
		return new IvParameterSpec(iv);
	}

	/**
	 * -------------------------------------------------------------------------------------------------
	 * @param key
	 * @param iv
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encrypt(String key, String iv, byte[] data) throws Exception {
//		logger.debug("key: {}, iv: {}", key, iv);
		Cipher cipher = Cipher.getInstance(AES);
		cipher.init(Cipher.ENCRYPT_MODE, getKey(key), getIv(iv));
		return cipher.doFinal(data);
	}
	public static byte[] encrypt(String key, byte[] data) throws Exception {
		String iv = key.substring(0,  16);
		return encrypt(key, iv, data);
	}
	public static String encryptToString(String key, byte[] data) throws Exception {
		return new String(encrypt(key, data));
	}

	public static byte[] decrypt(String key, String iv, byte[] data) throws Exception {
//		logger.debug("key: {}, iv: {}", key, iv);
		byte[] encdata = data;
		Cipher cipher = Cipher.getInstance(AES);
		cipher.init(Cipher.DECRYPT_MODE, getKey(key), getIv(iv));
		return cipher.doFinal(encdata);
	}
	public static byte[] decrypt(String key, byte[] data) throws Exception {
		String iv = key.substring(0, 16);
		return decrypt(key, iv, data);
	}
	public static String decryptToString(String key, byte[] data) throws Exception {
		return new String(decrypt(key, data));
	}

	/**
	 * ----------------------------------------------------------------------------------------------------
	 * @param key  256(32 bytes) or 128(16 bytes) ...
	 * @param iv   16 bytes
	 * @param data ...
	 * @return
	 * @throws Exception 
	 */
	public static String encrypt(String key, String iv, String data) throws Exception {
		return new String( encrypt(key, iv, data.getBytes("UTF-8")) );
	}
	public static String encrypt(String key, String data) throws Exception {
		String iv = key.substring(0, 16);
		return encrypt(key, iv, data);
	}

	/**
	 * @param key
	 * @param iv
	 * @param encdata
	 * @return
	 * @throws Exception
	 */
	public static String decrypt(String key, String iv, String encdata) throws Exception {
		return new String( encrypt(key, encdata.getBytes("UTF-8")) );
	}
	public static String decrypt(String key, String encdata) throws Exception {
		String iv = key.substring(0, 16);
		return decrypt(key, iv, encdata);
	}

	//-------------------------------------------------------------------
	private static String makeKey(String key) {
		StringBuilder sb = new StringBuilder(key);
		int index = 0;
		while((sb.length() % 8) > 0) {
			sb.append(index++);
		}
		String mkey = sb.toString();
//		logger.debug("key: {}, make key: {}", key, mkey);
		return mkey;
	}

	/**
	 * encrypt and encode base64
	 * @param key
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encode(String key, byte[] data) throws Exception {
		String duid = makeKey(key);
		return Base64.getEncoder().encode(encrypt(duid, data));
	}
	/**
	 * decode base64 and decrypt
	 * @param key
	 * @param encdata
	 * @return
	 * @throws Exception
	 */
	public static byte[] decode(String key, byte[] encdata) throws Exception {
		String duid = makeKey(key);
		byte[] data = Base64.getDecoder().decode(encdata);
		return decrypt(duid, data);
	}

	public static String encode(String key, String data) throws Exception {
		return new String( encode(key, data.getBytes("UTF-8")));
	}
	public static String decode(String key, String data) throws Exception {
		return new String(decode(key, data.getBytes("UTF-8")));
	}

	//-------------------------------------------------------------------
	private static String encode(String key, String str, String charset) {
		System.out.println("key: "+key);
		System.out.println("str: "+str);
		System.out.println("charset: "+charset);
		try {
			System.out.println("urlencode---------");
			String urlencodestr = URLEncoder.encode(str, charset);
			System.out.println("urlencode length: "+urlencodestr.length());
			System.out.println("urlencode str: "+urlencodestr);

			System.out.println("aes encrypt---------");
			byte[] encryptbytes = encrypt(key, urlencodestr.getBytes(charset));
			System.out.println("encrypt length: "+encryptbytes.length);
			System.out.println("encrypt str: "+(new String(encryptbytes)));
			System.out.println("base64 encode---------");
			byte[] baseencodebytes = Base64.getEncoder().encode(encryptbytes);
			String baseencodestr = new String(baseencodebytes);
			return baseencodestr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	private static String decode(String key, String str, String charset) {
		System.out.println("key: "+key);
		System.out.println("str: "+str);
		System.out.println("charset: "+charset);
		try {
			System.out.println("base64 decode---------");
			byte[] basedecodebytes = Base64.getDecoder().decode(str);
			System.out.println("basedecode length: "+basedecodebytes.length);
			System.out.println("basedecode str: "+(new String(basedecodebytes)));
			System.out.println("aes decrypt---------");

			byte[] aesdecbytes = decrypt(key, basedecodebytes);
			String aesdecstr = new String(aesdecbytes);
			System.out.println("decrypt length: "+aesdecbytes.length);
			System.out.println("decrypt str: "+aesdecstr);
			String urldecodestr = URLDecoder.decode(aesdecstr, charset);
			return urldecodestr;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	public static void main(String[] args) throws Exception {
		String hstr = "우리나라 대한민국 삼천리 금수강산 ...!";
//		String estr = "abcdefghijklmnopqrstuvwxyz0123456789";
		String key = "F1fQMpUWTNGTywaNdQS/41R6SUQ=0123";
		String charset = "UTF-8";

		String encstr = encode(key, hstr, charset);
		System.out.println("baseencode length: "+encstr.length());
		System.out.println("baseencode str: "+encstr);

		System.out.println("");
		String decstr = decode(key, encstr, charset);
		System.out.println("urldecode length: "+decstr.length());
		System.out.println("urldecode str: "+decstr);

		System.out.println("");
		encstr = "j2pzNTBGdy2KZuaJEcdI/xAsJS8bAm0oURx6chIF55WhsX0zZKgNorxqbfFGPL55C86fjiS70tI4808btAFVCnRnE/tlEiRoc1DbExCIxb/vqz4O2gWJKBm3BeqquLHEJV00NFfvYgMrkkWEF/dPTHARwMbLMLTqoJ404/plWB2ONNXC8ZsP0LDxGpczJRKwDa+UP+tpp/q1neTCkN+2fgnWbls6/eWStqZXQXhBk5JMSOapct5uZrvNSAwDTN90r7bNQ+9c7VOFCnniNQ/rS9LxqQQRCw8jWckGmWP3syqdcyQsaEgp5Dvms8ppuO1fwI7hzeuysrfe42q5cfsm0RUCsyBQ43pBYKmeocA41/0hE4od9CfgbFc8/2zhagXv40ZlQHPRfBAi8AGnEFprvCTxfveN9Uq+XXiSQLcnpkqM9gWf+nRszVhuL7KMPvF+MG4UUKyDFteGKIDlh6h6pPTmfMDsUlRWhk+rdjTRHQqkGkwEgMgQ6NDmat7UquANSaWoHpsfFKlLxW/jkXLsTU6T0NeXfXx1w9TLM+jtrXQONc1aZ//aMvt/0R+sRWhqH4GIqaxrd9dn2QaIk8CgKNTZ68yX90oWOcuVnFvPlRSzyVVpSndUyrngh5Y2fbqpKWq+j9v+oRSpop5G/sdgsLSDDqvwEEILR+/j4Wc4eS0=";
		decstr = decode(key, encstr, charset);
		System.out.println("urldecode length: "+decstr.length());
		System.out.println("urldecode str: "+decstr);
	}
}
