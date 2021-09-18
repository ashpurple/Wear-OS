package com.example.android.wearable.watchface;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES256 {
    public static byte[] ivBytes={0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00};
    public static String secretKey="aaaaaaaaaaaaaaaaaaaaaaaa";

    //AES encoding

    public static String AES_Encode(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{

        byte[] textBytes=str.getBytes("UTF-8");
        AlgorithmParameterSpec ivSpec=new IvParameterSpec(ivBytes);
        SecretKeySpec newKey= new SecretKeySpec(secretKey.getBytes("UTF-8"),"AES");
        Cipher cipher=null;
        cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE,newKey,ivSpec);

        return Base64.encodeToString(cipher.doFinal(textBytes),0);

    }

    //AES decoding
    public static String AES_Decode(String str) throws java.io.UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException{
        byte[] textBytes= Base64.decode(str,0);
        AlgorithmParameterSpec ivSpec=new IvParameterSpec(ivBytes);
        SecretKeySpec newKey=new SecretKeySpec(secretKey.getBytes("UTF-8"),"AES");
        Cipher cipher=Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE,newKey,ivSpec);
        return new String(cipher.doFinal(textBytes),"UTF-8");

    }


}
