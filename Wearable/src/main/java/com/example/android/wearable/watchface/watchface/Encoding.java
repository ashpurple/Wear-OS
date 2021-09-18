/**
 * Encoding.java
 */
package com.example.myapplication;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;

/**
 * @author bbrain
 *
 */
public final class Encoding {

	/**
	 * final String ISO_8859_1 = "ISO-8859-1";
	 */
	public static final String ISO_8859_1 = "ISO-8859-1";
	/**
	 * final String UTF_8 = "UTF-8";
	 */
	public static final String UTF_8 = "UTF-8";
	/**
	 * final String EUC_KR = "EUC-KR";
	 */
	public static final String EUC_KR = "EUC-KR";
	/**
	 * final String KSC5601 = "KSC5601";
	 */
	public static final String KSC5601 = "KSC5601";
	/**
	 * final String UTF_16BE = "UTF-16BE";
	 */
	public static final String UTF_16BE = "UTF-16BE";
	/**
	 *  final String DEFAULT_ENCODING = UTF_8;
	 */
	public static final String DEFAULT_ENCODING = UTF_8;
	/**
	 * final String HEX_STRING = "0123456789ABCDEF";
	 */
	public static final String HEX_STRING = "0123456789ABCDEF";

	/**
	 * @param source
	 * @return the string
	 */
	public static String toKor(String source){
		return conv(source, KSC5601);
	}
	/**
	 * @param source
	 * @return the string
	 */
	public static String toEng(String source){
		return conv(source, ISO_8859_1);
	}
	/**
	 * @param source
	 * @return the string
	 */
	public static String toEuckr(String source){
		return conv(source, EUC_KR);
	}
	/**
	 * @param source
	 * @return the string
	 */
	public static String toUtf8(String source){
		return conv(source, UTF_8);
	}

	/**
	 * @param str
	 * @return the string
	 */
	public static String native2ascii(String str) {
		StringBuffer sb = new StringBuffer();
		char[] chs = str.toCharArray();
		int len    = chs.length;
		for (int i=0; i<len; i++) {
			if (chs[i]<128) {
				sb.append(chs[i]);
			}else if(chs[i]<256) {
				sb.append("\\u00").append(Integer.toString(chs[i], 16));
			}else{
				sb.append("\\u").append(Integer.toString(chs[i], 16));
			}
		}
		return sb.toString();
	}
	/**
	 * @param str
	 * @return the string
	 */
	public static String ascii2native(String str) {
		String hex = HEX_STRING;
		StringBuffer buf = new StringBuffer() ;

		for(int i=0; i<str.length(); i++) {
			char c = str.charAt(i) ;
			if ( c == '\\' && i + 1 <= str.length() && str.charAt(i+1) == '\\' ) {
				buf.append("\\\\") ;
				i += 1 ;
			} else if ( c == '\\' && i + 6 <= str.length() && str.charAt(i+1) == 'u' ) {
				String sub = str.substring(i+2, i+6).toUpperCase() ;
				int i0 = hex.indexOf(sub.charAt(0)) ;
				int i1 = hex.indexOf(sub.charAt(1)) ;
				int i2 = hex.indexOf(sub.charAt(2)) ;
				int i3 = hex.indexOf(sub.charAt(3)) ;

				if ( i0 < 0 || i1 < 0 || i2 < 0 || i3 < 0 ) {
					buf.append("\\u") ;
					i += 1 ;
				} else {
					byte[] data = new byte[2] ;
					data[0] = int2byte(i1 + i0 * 16) ;
					data[1] = int2byte(i3 + i2 * 16) ;
					try{
						buf.append(new String(data, "UTF-16BE").toString()) ;
					} catch(Exception ex) {
						buf.append("\\u" + sub) ;
					}
					i += 5 ;
				}
			} else {
				buf.append(c) ;
			}
		}
		return buf.toString() ;
	}
	/**
	 * @param intValue
	 * @return
	 */
	private static byte int2byte(int intValue) {
		return (byte)( (intValue>127) ? intValue-256 : intValue ) ;
	}
	/**
	 * @param string
	 * @return the string digested with md5
	 */
	public static String md5(String string) {
		StringBuffer sb = new StringBuffer();
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(string.getBytes());
			byte md5Bytes[] = md5.digest();
			for(int i = 0; i < md5Bytes.length; i++){
				sb.append(md5Bytes[i]);
			}
		}
		catch(Exception exception) { }
		return sb.toString();
	}
	/**
	 * @param str
	 * @return the encoded string with UTF8
	 */
	public static String urlEncode(String str) {
		return urlEncode(str, Encoding.DEFAULT_ENCODING);
	}
	/**
	 * @param str
	 * @param enc
	 * @return the encoded string
	 */
	public static String urlEncode(String str, String enc) {
		if (str==null || "".equals(str)){
			return "";
		}
		try {
			return URLEncoder.encode(str, enc);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	/**
	 * @param str
	 * @return the decoded string
	 */
	public static String urlDecode(String str) {
		return urlDecode(str, Encoding.DEFAULT_ENCODING);
	}
	/**
	 * @param str
	 * @param enc
	 * @return the decoded string
	 */
	public static String urlDecode(String str, String enc) {
		if (str==null || "".equals(str)){
			return "";
		}
		try {
			return URLDecoder.decode(str, enc);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	/**
	 * @param source
	 * @param toEncoding
	 * @return the string
	 */
	public static String conv(String source, String toEncoding) {
		String resultString = "";
		try {
			resultString = new String(source.getBytes(), toEncoding);
		}
		catch(UnsupportedEncodingException unsupportedencodingexception) {
			resultString = "";
		}
		catch(Exception e) {
			resultString = null;
		}
		return resultString;
	}
	/**
	 * @param source
	 * @param fromEncoding
	 * @param toEncoding
	 * @return the string
	 */
	public static String conv(String source, String fromEncoding, String toEncoding) {
		String resultString = "";
		try {
			resultString = new String(source.getBytes(fromEncoding), toEncoding);
		}catch(UnsupportedEncodingException unsupportedencodingexception) {
			resultString = "";
		}catch(Exception e) {
			resultString = null;
		}
		return resultString;
	}
}
