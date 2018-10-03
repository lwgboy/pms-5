package com.bizvisionsoft.service.tools;

import java.security.MessageDigest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
		Logger logger = LoggerFactory.getLogger(Coder.class);
		
		String psw;

		logger.debug("\n[BASE 64加密解密]");
		psw = "abc.中文支持";
		logger.debug("原文:"+psw);
		psw = Coder.encryptBASE64(psw.getBytes());
		logger.debug("密文"+psw);
		byte[] res = Coder.decryptBASE64(psw);
		psw = new String(res);
		logger.debug("明文"+psw);
		
		logger.debug("\n[生成MD5码]");
		psw = "abc.中文支持";
		logger.debug("原文:"+psw);
		byte[] bytes = Coder.encryptMD5(psw.getBytes());
		psw = new String(bytes);
		logger.debug("密文"+psw);
		
		logger.debug("\n[RSA加密解密]");
		psw = "abc.中文支持";
		logger.debug("原文:"+psw);
		String[] keys = RSACoderUtil.getKeys();
		logger.debug("公钥:" + keys[0]);
		logger.debug("私钥:" + keys[1]);

		byte[] data = psw.getBytes();
		byte[] encrypted = RSACoder.encryptByPublicKey(data, keys[0]);
		logger.debug("密文"+new String(encrypted));
		byte[] decrypted = RSACoder.decryptByPrivateKey(encrypted, keys[1]);
		logger.debug("明文"+new String(decrypted));

	}
}