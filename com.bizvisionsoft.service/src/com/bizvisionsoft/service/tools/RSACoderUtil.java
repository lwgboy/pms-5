package com.bizvisionsoft.service.tools;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RSACoderUtil {
	
	public static Logger logger = LoggerFactory.getLogger(RSACoderUtil.class);

	public static String[] getKeys() {
		try {
			Map<String, Object> keyMap = RSACoder.initKey();
			return new String[] { RSACoder.getPublicKey(keyMap), RSACoder.getPrivateKey(keyMap) };
		} catch (Exception e) {
		}
		return null;
	}

	public static String encryptByPublicKey(String inputStr, String publicKey) {
		try {
			byte[] data = inputStr.getBytes();
			byte[] encodedData = RSACoder.encryptByPublicKey(data, publicKey);
			return new String(encodedData);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decryptByPrivateKey(String encodedString, String privateKey) {
		try {
			byte[] encodedData = encodedString.getBytes();
			byte[] decodedData = RSACoder.decryptByPrivateKey(encodedData, privateKey);
			return new String(decodedData);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static String encryptByPrivateKey(String inputStr, String privateKey) {
		try {
			byte[] data = inputStr.getBytes();
			byte[] encodedData = RSACoder.encryptByPrivateKey(data, privateKey);
			return new String(encodedData);
		} catch (Exception e) {
		}
		return null;
	}

	public static String decryptByPublicKey(String encodedString, String publicKey) {
		try {
			byte[] encodedData = encodedString.getBytes();
			byte[] decodedData = RSACoder.decryptByPublicKey(encodedData, publicKey);
			return new String(decodedData);
		} catch (Exception e) {
		}
		return null;
	}

	// public static void main(String[] args) throws Exception {
	// // 获得密钥
	// String[] keys = getKeys();
	//
	// logger.debug(("public key:\n" + keys[0]);
	// logger.debug(("private key: \n" + keys[1]);
	//
	// logger.debug(("公加私解");
	//
	// String inputStr = "liutao";
	// byte[] data = inputStr.getBytes();
	//
	// byte[] encrypted = RSACoder.encryptByPublicKey(data, keys[0]);
	//
	// byte[] decrypted = RSACoder.decryptByPrivateKey(encrypted, keys[1]);
	//
	// String outputStr = new String(decrypted);
	// logger.debug(( inputStr);
	// logger.debug(( outputStr);
	// }
}