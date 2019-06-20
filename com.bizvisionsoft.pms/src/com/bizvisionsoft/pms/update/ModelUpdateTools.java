package com.bizvisionsoft.pms.update;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.bizvisionsoft.bruicommons.ModelLoader;
import com.bizvisionsoft.bruicommons.model.Assembly;
import com.bizvisionsoft.bruicommons.model.Site;
import com.google.gson.GsonBuilder;

public class ModelUpdateTools {

	private final static String rootPath = "D:\\github\\pms\\config\\client\\site";

	private static Map<String, String> md5Path = new HashMap<String, String>();

	public static void main(String[] args) {
		File root = new File(rootPath);
		List<File> siteFiles = new ArrayList<>();
		siteFiles.addAll(Arrays.asList(new File(root.getParent() + "\\scheme").listFiles()));
		siteFiles.addAll(Arrays.asList(root.listFiles((d, f) -> f.toLowerCase().endsWith(".site"))));
		siteFiles.forEach(ModelUpdateTools::updateSite);
//		updateSite(new File(rootPath+"/host.site"));
	}

	private static void updateSite(File siteFile) {
		String siteName = siteFile.getName();
		siteName = siteName.substring(0, siteName.indexOf("."));

		Site site = ModelLoader.site(siteFile.getPath());
		System.out.println("�����ļ���" + siteFile);
		Map<String, String> id2Path = new HashMap<String, String>();// ����id��path��Ӧ��ϵ
		List<Assembly> siteAssemblies = new ArrayList<>(site.getAssyLib().getAssys());
		for (int i = 0; i < siteAssemblies.size(); i++) {
			Assembly a = siteAssemblies.get(i);
			String assyId = a.getId();
			// ��ȡ���json
			String json = a.toJson();
			// ��ȡ���md5��
			String md5 = getMD5(json);
			// ����Ƿ������ͬ�����
			if (md5Path.keySet().contains(md5)) {// �������
				// ���lib�е�·��
				String assyPath = md5Path.get(md5);
				// ����site�ļ��ж�Ӧid��·��
				id2Path.put(assyId, assyPath);
			} else {// ���������
					// ����name������·�����ļ���
				String type = Optional.ofNullable(a.getType()).orElse("");
				String assyPath = "/" + a.getName() + "." + type + "assy";
				// ���libĿ¼���Ƿ���ͬ���ļ�
				File assyFile = new File(rootPath + "\\lib" + assyPath);
				if (assyFile.exists()) {// �������ļ�
					assyPath = "/" + siteName + assyPath;
					assyFile = new File(rootPath + "\\lib" + assyPath);// ���õ��ͻ�վ��Ŀ¼��
					if (assyFile.exists()) {// ��Ȼ�������ļ�
						System.err.println("���棺�Ѻ����������-" + assyPath);// ��������������5.x��Ҳ���������
						continue;
					}
				}
				id2Path.put(assyId, assyPath);
				md5Path.put(md5, assyPath);
			}
		}
		// ׼������վ��
		// ����siteJson�ж�id�����õ�path
		String siteJson = site.toJson();
		Iterator<Entry<String, String>> iter = id2Path.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			siteJson = siteJson.replaceAll("\"" + entry.getKey() + "\"", "\"" + entry.getValue() + "\"");
		}
		site = new GsonBuilder().create().fromJson(siteJson, Site.class);
		// �������
		siteAssemblies = new ArrayList<>(site.getAssyLib().getAssys());
		for (int i = 0; i < siteAssemblies.size(); i++) {
			Assembly a = siteAssemblies.get(i);
			// ��������id�Ѿ����滻Ϊpath�ģ���Ҫ���棬���Ҵ�site��ȥ��
			String path = a.getId();
			if (id2Path.values().contains(path)) {
				try {
					save(rootPath + "\\lib" + path, a.toJson());
					site.getAssyLib().getAssys().remove(a);
				} catch (Exception e) {
					System.err.println("��������������-" + path + ", " + e.getMessage());
				}
			}
		}

		// ���site��û�������ȥ��Ŀ¼
		if (site.getAssyLib().getAssys().isEmpty()) {
			site.getRootFolder().children.clear();
		}
		try {
			save(siteFile.getPath(), site.toJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void save(String path, String json) throws Exception {
		File file = new File(path);
		File folder = file.getParentFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
		FileWriter fw = new FileWriter(file);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(json.toCharArray()); // д��char����
		bw.close();
		fw.close();
	}

	private final static String[] hexArray = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/*** * ��ָ�����ַ�������MD5���� */
	public static String getMD5(String originString) {
		try { // ��������MD5�㷨����ϢժҪ
			MessageDigest md = MessageDigest.getInstance("MD5");
			// ʹ��ָ�����ֽ������ժҪ���������£�Ȼ�����ժҪ����
			byte[] bytes = md.digest(originString.getBytes());
			// ���õ����ֽ��������ַ�������
			String s = byteArrayToHex(bytes);
			return s.toUpperCase();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static String byteArrayToHex(byte[] b) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < b.length; i++) {
			sb.append(byteToHex(b[i]));
		}
		return sb.toString();
	}

	private static String byteToHex(byte b) {
		int n = b;
		if (n < 0)
			n = n + 256;
		int d1 = n / 16;
		int d2 = n % 16;
		return hexArray[d1] + hexArray[d2];
	}

}
