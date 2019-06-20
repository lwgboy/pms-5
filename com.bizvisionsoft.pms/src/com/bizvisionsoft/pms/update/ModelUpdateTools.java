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
		System.out.println("升级文件：" + siteFile);
		Map<String, String> id2Path = new HashMap<String, String>();// 缓存id和path对应关系
		List<Assembly> siteAssemblies = new ArrayList<>(site.getAssyLib().getAssys());
		for (int i = 0; i < siteAssemblies.size(); i++) {
			Assembly a = siteAssemblies.get(i);
			String assyId = a.getId();
			// 获取组件json
			String json = a.toJson();
			// 获取组件md5码
			String md5 = getMD5(json);
			// 检查是否存在相同的组件
			if (md5Path.keySet().contains(md5)) {// 如果存在
				// 获得lib中的路径
				String assyPath = md5Path.get(md5);
				// 保存site文件中对应id的路径
				id2Path.put(assyId, assyPath);
			} else {// 如果不存在
					// 根据name，产生路径和文件名
				String type = Optional.ofNullable(a.getType()).orElse("");
				String assyPath = "/" + a.getName() + "." + type + "assy";
				// 检查lib目录中是否有同名文件
				File assyFile = new File(rootPath + "\\lib" + assyPath);
				if (assyFile.exists()) {// 有重名文件
					assyPath = "/" + siteName + assyPath;
					assyFile = new File(rootPath + "\\lib" + assyPath);// 放置到客户站点目录下
					if (assyFile.exists()) {// 仍然有重名文件
						System.err.println("警告：已忽略重名组件-" + assyPath);// 这个情况即便是在5.x下也是有问题的
						continue;
					}
				}
				id2Path.put(assyId, assyPath);
				md5Path.put(md5, assyPath);
			}
		}
		// 准备保存站点
		// 更改siteJson中对id的引用到path
		String siteJson = site.toJson();
		Iterator<Entry<String, String>> iter = id2Path.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			siteJson = siteJson.replaceAll("\"" + entry.getKey() + "\"", "\"" + entry.getValue() + "\"");
		}
		site = new GsonBuilder().create().fromJson(siteJson, Site.class);
		// 保存组件
		siteAssemblies = new ArrayList<>(site.getAssyLib().getAssys());
		for (int i = 0; i < siteAssemblies.size(); i++) {
			Assembly a = siteAssemblies.get(i);
			// 如果组件的id已经被替换为path的，需要保存，并且从site中去掉
			String path = a.getId();
			if (id2Path.values().contains(path)) {
				try {
					save(rootPath + "\\lib" + path, a.toJson());
					site.getAssyLib().getAssys().remove(a);
				} catch (Exception e) {
					System.err.println("错误：组件保存错误-" + path + ", " + e.getMessage());
				}
			}
		}

		// 如果site下没有组件，去掉目录
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
		bw.write(json.toCharArray()); // 写入char数组
		bw.close();
		fw.close();
	}

	private final static String[] hexArray = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f" };

	/*** * 对指定的字符串进行MD5加密 */
	public static String getMD5(String originString) {
		try { // 创建具有MD5算法的信息摘要
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 使用指定的字节数组对摘要进行最后更新，然后完成摘要计算
			byte[] bytes = md.digest(originString.getBytes());
			// 将得到的字节数组变成字符串返回
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
