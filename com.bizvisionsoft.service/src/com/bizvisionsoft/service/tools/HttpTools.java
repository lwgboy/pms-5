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
	 * ��ָ��URL����GET����������
	 *
	 * @param url
	 *            ���������URL
	 * @param param
	 *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
	 * 
	 * @param handleResponse
	 *            �����ؽ��
	 * @return URL������Զ����Դ����Ӧ���
	 * @throws IOException
	 */
	public static void httpGet(String url, String param,
			BiConsumer<Map<String, List<String>>, InputStream> handleResponse) throws IOException {
		String urlNameString = url + "?" + param;
		// �򿪺�URL֮�������
		URLConnection connection = new URL(urlNameString).openConnection();
		// ����ͨ�õ���������
		connection.setRequestProperty("accept", "*/*");
		connection.setRequestProperty("connection", "Keep-Alive");
		connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		// ����ʵ�ʵ�����
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
	 * ��ָ�� URL ����POST����������
	 *
	 * @param url
	 *            ��������� URL
	 * @param param
	 *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ��
	 * @return URL������Զ����Դ����Ӧ���
	 * @throws IOException
	 */
	public static void httpPost(String url, String param,
			BiConsumer<Map<String, List<String>>, InputStream> handleResponse) throws IOException {
		PrintWriter out = null;
		// �򿪺�URL֮�������
		URLConnection conn = new URL(url).openConnection();
		// ����ͨ�õ���������
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
		conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		// ����POST�������������������
		conn.setDoOutput(true);
		conn.setDoInput(true);
		// ��ȡURLConnection�����Ӧ�������
		out = new PrintWriter(conn.getOutputStream());
		// �����������
		out.print(param);
		// flush������Ļ���
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
