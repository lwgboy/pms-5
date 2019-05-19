package com.bizvisionsoft.service.tools;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public abstract class Coder {
	public static final String KEY_SHA = "SHA"; //$NON-NLS-1$
	public static final String KEY_MD5 = "MD5"; //$NON-NLS-1$

	/**
	 * MAC算法可选以下多种算法
	 * 
	 * <pre>
	 * HmacMD5  
	 * HmacSHA1  
	 * HmacSHA256  
	 * HmacSHA384  
	 * HmacSHA512
	 * </pre>
	 */
	public static final String KEY_MAC = "HmacMD5"; //$NON-NLS-1$

	/**
	 * BASE64解密
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] decryptBASE64(String key) throws Exception {
		return new Base64().decode(key);
	}

	/**
	 * BASE64加密
	 * 
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String encryptBASE64(byte[] key) throws Exception {
		return Base64.encodeBase64String(key);
	}

	/**
	 * MD5加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptMD5(byte[] data) throws Exception {

		MessageDigest md5 = MessageDigest.getInstance(KEY_MD5);
		md5.update(data);

		return md5.digest();

	}
	

	/**
	 * SHA加密
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptSHA(byte[] data) throws Exception {

		MessageDigest sha = MessageDigest.getInstance(KEY_SHA);
		sha.update(data);

		return sha.digest();

	}

	/**
	 * 初始化HMAC密钥
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String initMacKey() throws Exception {
		KeyGenerator keyGenerator = KeyGenerator.getInstance(KEY_MAC);

		SecretKey secretKey = keyGenerator.generateKey();
		return encryptBASE64(secretKey.getEncoded());
	}

	/**
	 * HMAC加密
	 * 
	 * @param data
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static byte[] encryptHMAC(byte[] data, String key) throws Exception {

		SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), KEY_MAC);
		Mac mac = Mac.getInstance(secretKey.getAlgorithm());
		mac.init(secretKey);

		return mac.doFinal(data);

	}
	
	public static void main(String[] args) throws Exception {
		
		String psw;

		System.out.println("\n[BASE 64加密解密]");
		psw = "abc.中文支持";
		System.out.println("原文:"+psw);
		psw = Coder.encryptBASE64(psw.getBytes());
		System.out.println("密文"+psw);
		byte[] res = Coder.decryptBASE64(psw);
		psw = new String(res);
		System.out.println("明文"+psw);
		
		System.out.println("\n[生成MD5码]");
		psw = "abc.中文支持";
		System.out.println("原文:"+psw);
		byte[] bytes = Coder.encryptMD5(psw.getBytes());
		psw = new String(bytes);
		System.out.println("密文"+psw);
		
		System.out.println("\n[RSA加密解密]");
		System.out.println("原文:"+psw);
		String[] keys = RSACoderUtil.getKeys();
		System.out.println("公钥:" + keys[0]);
		System.out.println("私钥:" + keys[1]);

		byte[] data = psw.getBytes();
		byte[] encrypted = RSACoder.encryptByPublicKey(data, keys[0]);
		String text = new String(encrypted);
		System.out.println("密文:"+text);
		System.out.println("密文压缩:"+text);
		text = URLEncoder.encode(text, "utf-8");
		System.out.println("密文转url:"+text);
		System.out.println(text.length());
		text  = URLDecoder.decode(text, "utf-8");
		byte[] decrypted = RSACoder.decryptByPrivateKey(encrypted, keys[1]);
		System.out.println("明文"+new String(decrypted));

	}
}