package com.bizvisionsoft.service.tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileTools {
	
	public static Logger logger = LoggerFactory.getLogger(FileTools.class);

	public static void writeFile(String content, String path, String encoding) throws IOException {
		File file = new File(path);
		file.delete();
		file.createNewFile();
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), encoding));
		writer.write(content);
		writer.close();
	}

	public static String readFile(String path, String encoding) throws IOException {
		String content = "";
		File file = new File(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		String line = null;
		while ((line = reader.readLine()) != null) {
			content += line + "\n";
		}
		reader.close();
		return content;
	}

	public static boolean deleteFolder(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 判断目录或文件是否存在
		if (!file.exists()) { // 不存在返回 false
			return flag;
		} else {
			// 判断是否为文件
			if (file.isFile()) { // 为文件时调用删除文件方法
				return deleteFile(sPath);
			} else { // 为目录时调用删除目录方法
				return deleteDirectory(sPath);
			}
		}
	}

	public static boolean deleteFile(String sPath) {
		boolean flag = false;
		File file = new File(sPath);
		// 路径为文件且不为空则进行删除
		if (file.isFile() && file.exists()) {
			file.delete();
			flag = true;
		}
		return flag;
	}

	public static boolean deleteDirectory(String sPath) {
		// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
		if (!sPath.endsWith(File.separator)) {
			sPath = sPath + File.separator;
		}
		File dirFile = new File(sPath);
		// 如果dir对应的文件不存在，或者不是一个目录，则退出
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		boolean flag = true;
		// 删除文件夹下的所有文件(包括子目录)
		File[] files = dirFile.listFiles();
		for (int i = 0; i < files.length; i++) {
			// 删除子文件
			if (files[i].isFile()) {
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} // 删除子目录
			else {
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前目录
		if (dirFile.delete()) {
			return true;
		} else {
			return false;
		}
	}

	public static String compress(String str) {

		if (str.isEmpty()) {
			return str;
		}

		try {
			ByteArrayOutputStream bos = null;
			GZIPOutputStream os = null; // 使用默认缓冲区大小创建新的输出流
			byte[] bs = null;
			try {
				bos = new ByteArrayOutputStream();
				os = new GZIPOutputStream(bos);
				os.write(str.getBytes()); // 写入输出流
				os.close();
				bos.close();
				bs = bos.toByteArray();
				return new String(bs, "ISO-8859-1"); // 通过解码字节将缓冲区内容转换为字符串
			} finally {
				bs = null;
				bos = null;
				os = null;
			}
		} catch (Exception ex) {
			return str;
		}
	}

	/**
	 * 解压缩字符串
	 * 
	 * @param str
	 *            解压缩的字符串
	 * @return 解压后的字符串
	 */
	public static String decompress(String str) {
		if (str.isEmpty()) {
			return str;
		}

		ByteArrayInputStream bis = null;
		ByteArrayOutputStream bos = null;
		GZIPInputStream is = null;
		byte[] buf = null;
		try {
			bis = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
			bos = new ByteArrayOutputStream();
			is = new GZIPInputStream(bis); // 使用默认缓冲区大小创建新的输入流
			buf = new byte[1024];
			int len = 0;
			while ((len = is.read(buf)) != -1) { // 将未压缩数据读入字节数组
				// 将指定 byte 数组中从偏移量 off 开始的 len 个字节写入此byte数组输出流
				bos.write(buf, 0, len);
			}
			is.close();
			bis.close();
			bos.close();
			return new String(bos.toByteArray()); // 通过解码字节将缓冲区内容转换为字符串
		} catch (Exception ex) {
			return str;
		} finally {
			bis = null;
			bos = null;
			is = null;
			buf = null;
		}
	}

	public static void copyStream(InputStream inputStream, OutputStream outputStream, boolean closeOutputWhenFinish)
			throws IOException {
		try {
			byte[] buffer = new byte[8192];
			boolean finished = false;
			while (!finished) {
				int bytesRead = inputStream.read(buffer);
				if (bytesRead != -1) {
					outputStream.write(buffer, 0, bytesRead);
				} else {
					finished = true;
				}
			}
		} finally {
			if (closeOutputWhenFinish)
				outputStream.close();
		}
	}

	private static final String TEMP_DIRECTORY_PREFIX = "BVS_";

	public static File createTempDirectory(String uid) throws IOException {
		File result = File.createTempFile(TEMP_DIRECTORY_PREFIX, "_" + uid);
		result.delete();
		if (result.mkdir()) {
			result.deleteOnExit();
		} else {
			throw new IOException("无法创建临时文件夹 " + result.getAbsolutePath());
		}
		return result;
	}
	
	public static final String DEFAULT_CONTENT_TYPE_FILE_NAME = "content-type.tmp";
	
	public static String getUploadedLocalFileContentType(File uploadedFile, String defaultType) {
		String contentType = null;
		BufferedReader br = null;
		if (uploadedFile.exists()) {
			File cTypeFile = new File(uploadedFile.getParentFile(), DEFAULT_CONTENT_TYPE_FILE_NAME);
			if (cTypeFile.exists()) {
				try {
					br = new BufferedReader(new InputStreamReader(new FileInputStream(cTypeFile)));
					contentType = br.readLine();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				} finally {
					try {
						if (br != null) {
							br.close();
						}
					} catch (IOException ce) {
						logger.error(ce.getMessage(), ce);
					}
				}
			}
		}
		//TODO 根据文件后缀得到content-type
		return contentType == null ? (defaultType == null ? "application/octet-stream" : defaultType) : contentType;
	}
	
}
