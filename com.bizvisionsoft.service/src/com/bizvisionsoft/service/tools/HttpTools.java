package com.bizvisionsoft.service.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class HttpTools {
	
	/**
	 * 向指定URL发送GET方法的请求
	 *
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * 
	 * @param handleResponse
	 *            处理返回结果
	 * @return URL所代表远程资源的响应结果
	 * @throws IOException
	 */
	public static void httpGet(String url, String param,
			BiConsumer<Map<String, List<String>>, InputStream> handleResponse) throws IOException {
		String urlNameString = url + "?" + param;
		// 打开和URL之间的连接
		URLConnection connection = new URL(urlNameString).openConnection();
		// 设置通用的请求属性
		connection.setRequestProperty("accept", "*/*");
		connection.setRequestProperty("connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		// 建立实际的连接
		connection.connect();

		if (handleResponse != null) {
			Map<String, List<String>> map = connection.getHeaderFields();
			InputStream is = connection.getInputStream();
			handleResponse.accept(map, is);
			if (is != null)
				is.close();
		}
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 *
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL所代表远程资源的响应结果
	 * @throws IOException
	 */
	public static void httpPost(String url, String param,
			BiConsumer<Map<String, List<String>>, InputStream> handleResponse) throws IOException {
		PrintWriter out = null;
		// 打开和URL之间的连接
		URLConnection conn = new URL(url).openConnection();
		// 设置通用的请求属性
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		// 发送POST请求必须设置如下两行
		conn.setDoOutput(true);
		conn.setDoInput(true);
		// 获取URLConnection对象对应的输出流
		out = new PrintWriter(conn.getOutputStream());
		// 发送请求参数
		out.print(param);
		// flush输出流的缓冲
		out.flush();

		if (handleResponse != null) {
			Map<String, List<String>> map = conn.getHeaderFields();
			InputStream is = conn.getInputStream();
			handleResponse.accept(map, is);
			if (is != null)
				is.close();
		}
	}


}
